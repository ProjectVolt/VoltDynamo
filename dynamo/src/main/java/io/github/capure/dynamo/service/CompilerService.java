package io.github.capure.dynamo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.capure.dynamo.config.JudgerConfig;
import io.github.capure.dynamo.config.LangConfig;
import io.github.capure.dynamo.exception.CompileErrorException;
import io.github.capure.dynamo.judger.Judger;
import io.github.capure.dynamo.judger.JudgerCommunicationException;
import io.github.capure.dynamo.judger.JudgerErrorException;
import io.github.capure.dynamo.judger.JudgerOptions;
import io.github.capure.dynamo.judger.JudgerResult;
import io.github.capure.dynamo.judger.JudgerResultCode;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CompilerService {
    public String compile(LangConfig config, String sourceDirPath, String outputDirPath) throws CompileErrorException {
        Path executablePath = Paths.get(outputDirPath, config.getCompileExe());
        Path sourceFilePath = Paths.get(sourceDirPath, config.getCompileSrc());
        String commandRaw = String.format(config.getCompileCommand(), sourceFilePath, executablePath);
        List<String> commandList = new ArrayList<>(Arrays.asList(commandRaw.split(" ")));
        Path compilerOut = Paths.get(outputDirPath, "compiler.out");
        List<String> env = Arrays.asList("PATH=" + System.getenv("PATH"));

        JudgerOptions options = JudgerOptions.builder()
                .maxCpuTime(config.getCompileMaxCpuTime())
                .maxRealTime(config.getCompileMaxRealTime())
                .maxMemory(config.getCompileMaxMemory())
                .maxStack(128 * 1024 * 1024)
                .maxOutputSize(20 * 1024 * 1024)
                .exePath(commandList.removeFirst())
                .inputPath(sourceFilePath.toString())
                .outputPath(compilerOut.toString())
                .errorPath(compilerOut.toString())
                .args(commandList)
                .env(env)
                .logPath(JudgerConfig.COMPILER_LOG_PATH)
                .seccompRuleName(null)
                .uid(JudgerConfig.COMPILER_USER_UID)
                .gid(JudgerConfig.COMPILER_USER_GID)
                .build();
        try {
            log.debug("Compiling with options: " + options.toString());
            JudgerResult result = Judger.run(options);
            if (result.getResult() != JudgerResultCode.RESULT_SUCCESS) {
                log.debug("Judger result was not success: " + result.toString());
                if (Files.exists(compilerOut)) {
                    try {
                        String error = new String(Files.readAllBytes(compilerOut)).strip();
                        Files.delete(compilerOut);
                        log.debug("Compiler output: " + error);
                        throw new CompileErrorException("Compile error: ", result, error);
                    } catch (IOException e) {
                        log.error("Error reading compiler output: " + e.getMessage());
                    }
                }
                log.error("Compile error: " + result.toString());
                throw new CompileErrorException("Compile error: ", result);
            } else {
                try {
                    Files.delete(compilerOut);
                    log.debug("Compiler output deleted");
                } catch (IOException e) {
                    log.error("Error deleting compiler output: " + e.getMessage());
                    throw new CompileErrorException("Compile error: unable to delete compiler output");
                }
                return executablePath.toString();
            }
        } catch (JudgerErrorException e) {
            throw new CompileErrorException("Compile error: " + e.getMessage());
        } catch (JudgerCommunicationException e) {
            throw new CompileErrorException("Compile error: " + e.getMessage());
        }
    }
}
