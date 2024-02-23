package io.github.capure.dynamo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.capure.dynamo.config.JudgerConfig;
import io.github.capure.dynamo.config.LangConfig;
import io.github.capure.dynamo.dto.SubmissionDetails;
import io.github.capure.dynamo.dto.SubmissionResult;
import io.github.capure.dynamo.dto.TestCaseResult;
import io.github.capure.dynamo.exception.CompileErrorException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.judger.Judger;
import io.github.capure.dynamo.judger.JudgerOptions;
import io.github.capure.dynamo.judger.JudgerResult;
import io.github.capure.dynamo.judger.JudgerResultCode;
import io.github.capure.dynamo.judger.JudgerResultError;
import io.github.capure.dynamo.model.TestCase;

@Service
public class JudgeService {
    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private CompilerService compilerService;

    private String compileSubmission(SubmissionDetails submissionDetails) {
        try {
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test");
            Path sourceFile = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileSrc());
            Files.write(sourceFile, submissionDetails.getSourceCode().getBytes());
            new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), tmp.toString()).start();
            new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), sourceFile.toString()).start();
            new ProcessBuilder("chmod", "700", tmp.toString()).start();
            new ProcessBuilder("chmod", "400", sourceFile.toString()).start();
            try {
                String result = compilerService.compile(submissionDetails.getLanguage().getConfig(), tmp.toString(),
                        tmp.toString());
                if (result == null || !Files.exists(Paths.get(result))) {
                    throw new CompileErrorException("Compilation failed");
                }
                return result.toString();
            } catch (CompileErrorException e) {
                Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                    file.setWritable(true);
                    file.delete();
                });
                throw e;
            }
        } catch (Exception e) {
            throw new CompileErrorException("Compilation failed");
        }
    }

    private TestCaseResult runTestCase(SubmissionDetails submissionDetails, TestCase testCase, String exePath) {
        try {
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test_run");
            String testInput = new String(Base64.getDecoder().decode(testCase.getInput()));
            String testOutput = new String(Base64.getDecoder().decode(testCase.getOutput())).trim();
            Path inPath = Paths.get(tmp.toString(), "in");
            Path outPath = Paths.get(tmp.toString(), "out");
            Path newExePath = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileExe());
            Files.write(inPath, testInput.getBytes());
            Files.write(outPath, "".getBytes());
            Files.copy(Paths.get(exePath), newExePath, StandardCopyOption.REPLACE_EXISTING);

            new ProcessBuilder("chown", String.valueOf(JudgerConfig.RUN_USER_UID), tmp.toString()).start();
            new ProcessBuilder("chmod", "711", String.valueOf(JudgerConfig.RUN_USER_UID), tmp.toString()).start();
            new ProcessBuilder("chown", String.valueOf(JudgerConfig.RUN_USER_UID), newExePath.toString()).start();
            new ProcessBuilder("chmod", "555", newExePath.toString()).start();
            new ProcessBuilder("chown", String.valueOf(JudgerConfig.RUN_USER_UID), inPath.toString()).start();
            new ProcessBuilder("chmod", "400", inPath.toString()).start();
            new ProcessBuilder("chown", String.valueOf(JudgerConfig.RUN_USER_UID), outPath.toString()).start();
            new ProcessBuilder("chmod", "711", outPath.toString()).start();

            String command = String.format("%s", newExePath);
            LangConfig config = submissionDetails.getLanguage().getConfig();
            List<String> env = new ArrayList<>();
            env.add("PATH=" + System.getenv("PATH"));
            env.addAll(config.getRunEnv());

            JudgerOptions options = JudgerOptions.builder()
                    .maxCpuTime(submissionDetails.getTimeLimit())
                    .maxRealTime(submissionDetails.getTimeLimit() * 3)
                    .maxMemory(submissionDetails.getMemoryLimit())
                    .maxStack(128 * 1024 * 1024)
                    .maxOutputSize(5 * 1024 * 1024)
                    .exePath(command)
                    .env(env)
                    .logPath(Paths.get(tmp.toString(), "run.log").toString())
                    .seccompRuleName(config.getRunSeccompRule())
                    .inputPath(inPath.toString())
                    .outputPath(outPath.toString())
                    .errorPath(outPath.toString())
                    .uid(JudgerConfig.RUN_USER_UID)
                    .gid(JudgerConfig.RUN_USER_GID)
                    .build();

            JudgerResult result = Judger.run(options);
            if (result.getResult() != JudgerResultCode.RESULT_SUCCESS) {
                Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                    file.setWritable(true);
                    file.delete();
                });
                return new TestCaseResult(testCase.getId(), "", result, 0, "error");
            } else {
                Boolean outputExists = Files.exists(outPath);
                String output = outputExists ? Files.readString(outPath).trim() : "";
                Integer score = output.equals(testOutput) ? testCase.getMaxScore() : 0;

                Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                    file.setWritable(true);
                    file.delete();
                });

                if (!outputExists || score == 0) {
                    result.setResult(JudgerResultCode.RESULT_WRONG_ANSWER);
                }

                return new TestCaseResult(testCase.getId(), output, result, score, "");
            }
        } catch (Exception e) {
            return new TestCaseResult(testCase.getId(), "",
                    new JudgerResult(0, 0, 0, 0, 0, JudgerResultError.ERROR_NONE, JudgerResultCode.RESULT_SYSTEM_ERROR),
                    0, "error: " + e.getMessage());
        }
    }

    public SubmissionResult judgeSubmission(SubmissionDetails submissionDetails) {
        try {
            List<TestCase> testCases = testCaseService.findAllByProblemId(submissionDetails.getProblemId());
            if (testCases.isEmpty()) {
                throw new TestCaseNotFoundException(submissionDetails.getProblemId());
            }
            String exe = compileSubmission(submissionDetails);
            List<TestCaseResult> results = testCases.stream()
                    .map(testCase -> runTestCase(submissionDetails, testCase, exe))
                    .toList();
            Boolean runSuccess = results.stream()
                    .allMatch(result -> result.getJudgerResult().getResult() == JudgerResultCode.RESULT_SUCCESS
                            || result.getJudgerResult().getResult() == JudgerResultCode.RESULT_WRONG_ANSWER);
            Boolean answerSuccess = results.stream()
                    .allMatch(result -> result.getJudgerResult().getResult() == JudgerResultCode.RESULT_SUCCESS);

            Files.walk(Paths.get(exe).getParent()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                file.setWritable(true);
                file.delete();
            });

            return new SubmissionResult(submissionDetails.getSubmissionId(), submissionDetails.getProblemId(), true,
                    runSuccess, answerSuccess, results);
        } catch (Exception e) {
            Boolean isCompileError = e instanceof CompileErrorException;
            Boolean compileSuccess = !isCompileError && !(e instanceof TestCaseNotFoundException);

            SubmissionResult result = new SubmissionResult(submissionDetails.getSubmissionId(),
                    submissionDetails.getProblemId(),
                    compileSuccess, false, false,
                    new ArrayList<>());
            if (isCompileError) {
                result.setCompileError(e.getMessage());
            }
            return result;
        }
    }
}
