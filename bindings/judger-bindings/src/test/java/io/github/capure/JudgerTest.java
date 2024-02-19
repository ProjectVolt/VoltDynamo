package io.github.capure;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class JudgerTest {
	@Test
	public void judgerShouldRunWithMinimalOptions() throws JudgerErrorException, JudgerCommunicationException {
		JudgerOptions options = JudgerOptions.builder()
				.exePath("/bin/echo")
				.inputPath("/dev/null")
				.outputPath("/dev/null")
				.errorPath("/dev/null")
				.logPath("./judger_test/judger.log")
				.args(Arrays.asList("hello"))
				.build();
		JudgerResult result = Judger.run(options);
		assertEquals(0, result.getExitCode());
		assertEquals(JudgerResultCode.RESULT_SUCCESS, result.getResult());
		assertEquals(JudgerResultError.ERROR_NONE, result.getError());
	}

	@Test
	public void judgerShouldCompileAndRunCpp() throws JudgerErrorException, JudgerCommunicationException, IOException {
		JudgerOptions options = JudgerOptions.builder()
				.exePath("/usr/bin/g++")
				.inputPath("/dev/null")
				.outputPath("/dev/null")
				.errorPath("./judger_test/compile_error.log")
				.logPath("./judger_test/judger.log")
				.args(List.of("-o", "./judger_test/main", "./judger_test/main.cpp"))
				.seccompRuleName(null)
				.env(Arrays.asList("PATH=" + System.getenv("PATH")))
				.build();
		JudgerResult result = Judger.run(options);
		assertEquals(0, result.getExitCode());
		assertEquals(JudgerResultCode.RESULT_SUCCESS, result.getResult());
		assertEquals(JudgerResultError.ERROR_NONE, result.getError());

		options = JudgerOptions.builder()
				.exePath("./judger_test/main")
				.inputPath("./judger_test/1.in")
				.outputPath("./judger_test/1.out")
				.errorPath("./judger_test/error.log")
				.logPath("./judger_test/judger.log")
				.seccompRuleName(SeccompRule.SECCOMP_RULE_C_CPP)
				.build();
		result = Judger.run(options);
		assertEquals(0, result.getExitCode());
		assertEquals(JudgerResultCode.RESULT_SUCCESS, result.getResult());
		assertEquals(JudgerResultError.ERROR_NONE, result.getError());

		String expected = new String(Files.readAllBytes(Paths.get("./judger_test/1.in"))).strip();
		String actual = new String(Files.readAllBytes(Paths.get("./judger_test/1.out"))).strip();
		assertEquals(expected, actual);
	}
}
