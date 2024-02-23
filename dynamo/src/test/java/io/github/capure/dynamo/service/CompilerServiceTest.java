package io.github.capure.dynamo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.capure.dynamo.config.CConfig;
import io.github.capure.dynamo.config.CppConfig;
import io.github.capure.dynamo.config.JudgerConfig;
import io.github.capure.dynamo.config.LangConfig;
import io.github.capure.dynamo.config.PythonConfig;
import io.github.capure.dynamo.exception.CompileErrorException;
import io.github.capure.dynamo.judger.JudgerResultCode;

public class CompilerServiceTest {
    private CompilerService compilerService;

    @BeforeEach
    public void setUp() {
        compilerService = new CompilerService();
    }

    @Test
    public void compileShouldCompileForValidInputC() throws Exception {
        LangConfig config = new CConfig();
        Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test");
        Path copied = Paths.get(tmp.toString(), "main.c");
        Path originalPath = Paths.get(System.getProperty("user.dir"), "judger_test", "main.c");
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), tmp.toString()).start();
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), copied.toString()).start();
        new ProcessBuilder("chmod", "700", tmp.toString()).start();
        new ProcessBuilder("chmod", "400", copied.toString()).start();
        String sourcePath = tmp.toString();
        String outputPath = tmp.toString();

        String result = assertDoesNotThrow(() -> compilerService.compile(config, sourcePath, outputPath));

        assertNotNull(result);
        assertTrue(Files.exists(Paths.get(result)));

        Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
            file.setWritable(true);
            file.delete();
        });
    }

    @Test
    public void compileShouldCompileForValidInputCPP() throws Exception {
        LangConfig config = new CppConfig();
        Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test");
        Path copied = Paths.get(tmp.toString(), "main.cpp");
        Path originalPath = Paths.get(System.getProperty("user.dir"), "judger_test", "main.cpp");
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), tmp.toString()).start();
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), copied.toString()).start();
        new ProcessBuilder("chmod", "700", tmp.toString()).start();
        new ProcessBuilder("chmod", "400", copied.toString()).start();
        String sourcePath = tmp.toString();
        String outputPath = tmp.toString();

        String result = assertDoesNotThrow(() -> compilerService.compile(config, sourcePath, outputPath));

        assertNotNull(result);
        assertTrue(Files.exists(Paths.get(result)));

        Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
            file.setWritable(true);
            file.delete();
        });
    }

    @Test
    public void compileShouldCompileForValidInputPython() throws Exception {
        LangConfig config = new PythonConfig();
        Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test");
        Path copied = Paths.get(tmp.toString(), "solution.py");
        Path originalPath = Paths.get(System.getProperty("user.dir"), "judger_test", "solution.py");
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), tmp.toString()).start();
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), copied.toString()).start();
        new ProcessBuilder("chmod", "700", tmp.toString()).start();
        new ProcessBuilder("chmod", "400", copied.toString()).start();
        String sourcePath = tmp.toString();
        String outputPath = tmp.toString();

        String result = assertDoesNotThrow(() -> compilerService.compile(config, sourcePath, outputPath));

        assertNotNull(result);
        assertTrue(Files.exists(Paths.get(result)));

        Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
            file.setWritable(true);
            file.delete();
        });
    }

    @Test
    public void compileShouldThrowForSyntaxError() throws Exception {
        LangConfig config = new CppConfig();
        Path tmp = Files.createTempDirectory(Paths.get("/tmp"), "judger_test");
        Path copied = Paths.get(tmp.toString(), "main.cpp");
        Path originalPath = Paths.get(System.getProperty("user.dir"), "judger_test", "broken.cpp");
        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), tmp.toString()).start();
        new ProcessBuilder("chown", String.valueOf(JudgerConfig.COMPILER_USER_UID), copied.toString()).start();
        new ProcessBuilder("chmod", "700", tmp.toString()).start();
        new ProcessBuilder("chmod", "400", copied.toString()).start();
        String sourcePath = tmp.toString();
        String outputPath = tmp.toString();

        CompileErrorException exception = assertThrows(CompileErrorException.class,
                () -> compilerService.compile(config, sourcePath, outputPath));

        assertTrue(exception.getMessage().contains("Compile error:"));
        assertTrue(exception.getResult().isPresent());
        assertTrue(exception.getCompilerOutput().isPresent());
        assertEquals(JudgerResultCode.RESULT_RUNTIME_ERROR, exception.getResult().get().getResult());

        Files.walk(tmp).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
            file.setWritable(true);
            file.delete();
        });
    }
}
