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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JudgeService {
    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private CompilerService compilerService;

    public JudgeService() {
        if (!Files.exists(Paths.get(JudgerConfig.COMPILER_LOG_PATH))) {
            log.info("Compiler log not found, creating new log");
            createCompilerLog();
        }
        if (!Files.exists(Paths.get(JudgerConfig.RUN_LOG_PATH))) {
            log.info("Run log not found, creating new log");
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
            log.error("Failed to create compiler log", e);
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
            log.error("Failed to create run log", e);
            throw new LogCreationFailedException("Failed to create compiler log", e);
        }
    }

    private void cleanUp(Path path) {
        try {
            log.info("Cleaning up temporary files");
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                log.debug("Deleting file: " + file.toString());
                file.setWritable(true);
                file.delete();
            });
        } catch (Exception e) {
            log.error("Failed to clean up temporary files", e);
            throw new FileCleanupException("Failed to clean up temporary files", e);
        }
    }

    private void changePermissions(Path path, Integer user, Integer group, String mode) {
        try {
            log.info("Changing permissions for file: " + path.toString() + " to " + user + ":" + group + " " + mode);
            new ProcessBuilder("chown", Integer.toString(user), path.toString()).start();
            new ProcessBuilder("chgrp", Integer.toString(group), path.toString()).start();
            new ProcessBuilder("chmod", mode, path.toString()).start();
        } catch (Exception e) {
            log.error("Failed to change file permissions", e);
            throw new PermissionChangeFailedException("Failed to change file permissions", e);
        }
    }

    private String compileSubmission(SubmissionDetails submissionDetails) {
        try {
            log.info("Compiling submission: " + submissionDetails.getSubmissionId());
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), JudgerConfig.JUDGER_TEST_COMPILE_PREFIX);
            Path sourceFile = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileSrc());

            log.debug("Writing source code to file: " + sourceFile.toString());
            Files.write(sourceFile, submissionDetails.getSourceCode().getBytes());
            changePermissions(tmp, JudgerConfig.COMPILER_USER_UID, JudgerConfig.COMPILER_USER_GID, "700");
            changePermissions(sourceFile, JudgerConfig.COMPILER_USER_UID, JudgerConfig.COMPILER_USER_GID, "400");

            try {
                log.debug("Compiling source code with lang: " + submissionDetails.getLanguage().getConfig().toString()
                        + " in folder: " + tmp.toString());
                String result = compilerService.compile(submissionDetails.getLanguage().getConfig(), tmp.toString(),
                        tmp.toString());
                if (result == null || !Files.exists(Paths.get(result))) {
                    log.error("Compilation failed, no executable found");
                    throw new CompileErrorException("Compilation failed");
                }
                return result.toString();
            } catch (CompileErrorException e) {
                log.debug("Cleaning up compilation files after exception");
                cleanUp(tmp);
                throw e;
            }
        } catch (CompileErrorException e) {
            log.error("Compilation failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Compilation failed with exception other than CompileErrorException", e);
            throw new CompileErrorException("Compilation failed");
        }
    }

    private TestCaseResult runTestCase(SubmissionDetails submissionDetails, TestCase testCase, String exePath) {
        try {
            log.info("Running test case: " + testCase.getId() + " for submission: "
                    + submissionDetails.getSubmissionId());
            Path tmp = Files.createTempDirectory(Paths.get("/tmp"), JudgerConfig.JUDGER_TEST_RUN_PREFIX);
            String testInput = new String(Base64.getDecoder().decode(testCase.getInput()));
            String testOutput = new String(Base64.getDecoder().decode(testCase.getOutput())).trim();
            Path inPath = Paths.get(tmp.toString(), "in");
            Path outPath = Paths.get(tmp.toString(), "out");
            Path newExePath = Paths.get(tmp.toString(), submissionDetails.getLanguage().getConfig().getCompileExe());

            log.debug("Writing input to file: " + inPath.toString());
            Files.write(inPath, testInput.getBytes());
            log.debug("Writing empty string to the output file: " + outPath.toString());
            Files.write(outPath, "".getBytes());
            log.debug("Copying executable to temporary folder: " + newExePath.toString());
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

            log.debug("Running test case with options: " + options.toString());
            JudgerResult result = Judger.run(options);
            if (result.getResult() != JudgerResultCode.RESULT_SUCCESS) {
                log.info("Test case failed with result: " + result.toString());
                cleanUp(tmp);
                return new TestCaseResult(testCase.getId(), "", result, 0, "error");
            } else {
                log.info("Test case ran successfully");
                Boolean outputExists = Files.exists(outPath);
                String output = outputExists ? Files.readString(outPath).trim() : "";
                Integer score = output.equals(testOutput) ? testCase.getMaxScore() : 0;
                log.debug("Test case score: " + score);

                cleanUp(tmp);

                if (!outputExists || score == 0) {
                    log.debug("Wrong answer");
                    result.setResult(JudgerResultCode.RESULT_WRONG_ANSWER);
                }

                return new TestCaseResult(testCase.getId(), output, result, score, "");
            }
        } catch (Exception e) {
            log.error("Test case failed with exception", e);
            return new TestCaseResult(testCase.getId(), "",
                    new JudgerResult(0, 0, 0, 0, 0, JudgerResultError.ERROR_NONE, JudgerResultCode.RESULT_SYSTEM_ERROR),
                    0, "error: " + e.getMessage());
        }
    }

    public SubmissionResult judgeSubmission(SubmissionDetails submissionDetails) {
        try {
            log.info("Judging submission: " + submissionDetails.getSubmissionId());

            log.debug("Finding test cases for problem: " + submissionDetails.getProblemId());
            List<TestCase> testCases = testCaseService.findAllByProblemId(submissionDetails.getProblemId());
            if (testCases.isEmpty()) {
                log.error("Test cases not found for problem: " + submissionDetails.getProblemId());
                throw new TestCaseNotFoundException(submissionDetails.getProblemId());
            }

            log.debug("Compiling submission: " + submissionDetails.getSubmissionId());
            String exe = compileSubmission(submissionDetails);

            log.debug("Running test cases for submission: " + submissionDetails.getSubmissionId());
            List<TestCaseResult> results = testCases.stream()
                    .map(testCase -> runTestCase(submissionDetails, testCase, exe))
                    .toList();

            log.debug("Checking for errors in test case results");
            Boolean runSuccess = results.stream()
                    .allMatch(result -> result.getJudgerResult().getResult() == JudgerResultCode.RESULT_SUCCESS
                            || result.getJudgerResult().getResult() == JudgerResultCode.RESULT_WRONG_ANSWER);

            log.debug("Checking for incorrect answers in test case results");
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
            log.error("Submission failed with exception", e);
            SubmissionResult result = new SubmissionResult(submissionDetails.getSubmissionId(),
                    submissionDetails.getProblemId(),
                    true, false, false,
                    new ArrayList<>());

            return result;
        }
    }
}
