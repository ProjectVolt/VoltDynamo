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
import io.github.capure.dynamo.dto.CompileError;
import io.github.capure.dynamo.dto.SubmissionDetails;
import io.github.capure.dynamo.dto.SubmissionResult;
import io.github.capure.dynamo.dto.TestCaseResult;
import io.github.capure.dynamo.exception.CompileErrorException;
import io.github.capure.dynamo.exception.FileCleanupException;
import io.github.capure.dynamo.exception.LogCreationFailedException;
import io.github.capure.dynamo.exception.PermissionChangeFailedException;
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

    public JudgeService() {
        if (!Files.exists(Paths.get(JudgerConfig.COMPILER_LOG_PATH))) {
            createCompilerLog();
        }
        if (!Files.exists(Paths.get(JudgerConfig.RUN_LOG_PATH))) {
            createRunLog();
        }
    }

    private void createCompilerLog() {
        try {
            Files.createDirectories(Paths.get(JudgerConfig.COMPILER_LOG_PATH).getParent());
            changePermissions(Paths.get(JudgerConfig.COMPILER_LOG_PATH).getParent(), JudgerConfig.COMPILER_USER_UID,
                    JudgerConfig.COMPILER_USER_GID, "664");
            Files.createFile(Paths.get(JudgerConfig.COMPILER_LOG_PATH));
            changePermissions(Paths.get(JudgerConfig.COMPILER_LOG_PATH), JudgerConfig.COMPILER_USER_UID,
                    JudgerConfig.COMPILER_USER_GID, "664");
        } catch (Exception e) {
            throw new LogCreationFailedException("Failed to create compiler log", e);
        }
    }

    private void createRunLog() {
        try {
            Files.createDirectories(Paths.get(JudgerConfig.RUN_LOG_PATH).getParent());
            changePermissions(Paths.get(JudgerConfig.RUN_LOG_PATH).getParent(), JudgerConfig.RUN_USER_UID,
                    JudgerConfig.RUN_USER_GID, "664");
            Files.createFile(Paths.get(JudgerConfig.RUN_LOG_PATH));
            changePermissions(Paths.get(JudgerConfig.RUN_LOG_PATH), JudgerConfig.RUN_USER_UID,
                    JudgerConfig.RUN_USER_GID, "664");
        } catch (Exception e) {
            throw new LogCreationFailedException("Failed to create compiler log", e);
        }
    }

    private void cleanUp(Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                file.setWritable(true);
                file.delete();
            });
        } catch (Exception e) {
            throw new FileCleanupException("Failed to clean up temporary files", e);
        }
    }

    private void changePermissions(Path path, Integer user, Integer group, String mode) {
        try {
            new ProcessBuilder("chown", Integer.toString(user), path.toString()).start();
            new ProcessBuilder("chgrp", Integer.toString(group), path.toString()).start();
            new ProcessBuilder("chmod", mode, path.toString()).start();
        } catch (Exception e) {
            throw new PermissionChangeFailedException("Failed to change file permissions", e);
        }
    }

    private String compileSubmission(SubmissionDetails submissionDetails) {
        try {
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), JudgerConfig.JUDGER_TEST_COMPILE_PREFIX);
            Path sourceFile = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileSrc());

            Files.write(sourceFile, submissionDetails.getSourceCode().getBytes());
            changePermissions(tmp, JudgerConfig.COMPILER_USER_UID, JudgerConfig.COMPILER_USER_GID, "700");
            changePermissions(sourceFile, JudgerConfig.COMPILER_USER_UID, JudgerConfig.COMPILER_USER_GID, "400");

            try {
                String result = compilerService.compile(submissionDetails.getLanguage().getConfig(), tmp.toString(),
                        tmp.toString());
                if (result == null || !Files.exists(Paths.get(result))) {
                    throw new CompileErrorException("Compilation failed");
                }
                return result.toString();
            } catch (CompileErrorException e) {
                cleanUp(tmp);
                throw e;
            }
        } catch (CompileErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new CompileErrorException("Compilation failed");
        }
    }

    private TestCaseResult runTestCase(SubmissionDetails submissionDetails, TestCase testCase, String exePath) {
        try {
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), JudgerConfig.JUDGER_TEST_RUN_PREFIX);
            String testInput = new String(Base64.getDecoder().decode(testCase.getInput()));
            String testOutput = new String(Base64.getDecoder().decode(testCase.getOutput())).trim();
            Path inPath = Paths.get(tmp.toString(), "in");
            Path outPath = Paths.get(tmp.toString(), "out");
            Path newExePath = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileExe());

            Files.write(inPath, testInput.getBytes());
            Files.write(outPath, "".getBytes());
            Files.copy(Paths.get(exePath), newExePath, StandardCopyOption.REPLACE_EXISTING);
            changePermissions(tmp, JudgerConfig.RUN_USER_UID, JudgerConfig.RUN_USER_GID, "711");
            changePermissions(newExePath, JudgerConfig.RUN_USER_UID, JudgerConfig.RUN_USER_GID, "555");
            changePermissions(inPath, JudgerConfig.RUN_USER_UID, JudgerConfig.RUN_USER_GID, "400");
            changePermissions(outPath, JudgerConfig.RUN_USER_UID, JudgerConfig.RUN_USER_GID, "711");

            LangConfig config = submissionDetails.getLanguage().getConfig();
            List<String> command = new ArrayList<>();
            command.addAll(List.of(String.format(config.getRunCommand(), newExePath).split(" ")));

            List<String> env = new ArrayList<>();
            env.add("PATH=" + System.getenv("PATH"));
            env.addAll(config.getRunEnv());

            JudgerOptions options = JudgerOptions.builder()
                    .maxCpuTime(submissionDetails.getTimeLimit())
                    .maxRealTime(submissionDetails.getTimeLimit() * 3)
                    .maxMemory(submissionDetails.getMemoryLimit())
                    .maxStack(128 * 1024 * 1024)
                    .maxOutputSize(5 * 1024 * 1024)
                    .exePath(command.removeFirst())
                    .args(command)
                    .env(env)
                    .logPath(JudgerConfig.RUN_LOG_PATH)
                    .seccompRuleName(config.getRunSeccompRule())
                    .inputPath(inPath.toString())
                    .outputPath(outPath.toString())
                    .errorPath(outPath.toString())
                    .uid(JudgerConfig.RUN_USER_UID)
                    .gid(JudgerConfig.RUN_USER_GID)
                    .build();

            JudgerResult result = Judger.run(options);
            if (result.getResult() != JudgerResultCode.RESULT_SUCCESS) {
                cleanUp(tmp);
                return new TestCaseResult(testCase.getId(), "", result, 0, "error");
            } else {
                Boolean outputExists = Files.exists(outPath);
                String output = outputExists ? Files.readString(outPath).trim() : "";
                Integer score = output.equals(testOutput) ? testCase.getMaxScore() : 0;

                cleanUp(tmp);

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

            cleanUp(Paths.get(exe).getParent());

            return new SubmissionResult(submissionDetails.getSubmissionId(), submissionDetails.getProblemId(), true,
                    runSuccess, answerSuccess, results);
        } catch (TestCaseNotFoundException e) {
            SubmissionResult result = new SubmissionResult(submissionDetails.getSubmissionId(),
                    submissionDetails.getProblemId(),
                    false, false, false,
                    new ArrayList<>());
            result.setCompileError(new CompileError("Test cases not found", true));
            return result;
        } catch (CompileErrorException e) {
            SubmissionResult result = new SubmissionResult(submissionDetails.getSubmissionId(),
                    submissionDetails.getProblemId(),
                    false, false, false,
                    new ArrayList<>());
            result.setCompileError(new CompileError(e.getCompilerOutput().orElse(e.getMessage()), false));
            return result;
        } catch (Exception e) {
            SubmissionResult result = new SubmissionResult(submissionDetails.getSubmissionId(),
                    submissionDetails.getProblemId(),
                    true, false, false,
                    new ArrayList<>());

            return result;
        }
    }
}
