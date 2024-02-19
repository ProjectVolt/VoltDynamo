package io.github.capure;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class JudgerOptionsTest {
        @Test
        public void optionsShouldProvideDefaults() {
                JudgerOptions options = JudgerOptions.builder().build();
                assertEquals(-1, options.getMaxCpuTime());
                assertEquals(-1, options.getMaxRealTime());
                assertEquals(-1, options.getMaxMemory());
                assertEquals(-1, options.getMaxStack());
                assertEquals(-1, options.getMaxOutputSize());
                assertEquals(-1, options.getMaxProcessNumber());
                assertEquals(0, options.getUid());
                assertEquals(0, options.getGid());
                assertEquals(0, options.getMemoryLimitCheckOnly());
                assertEquals("judger.log", options.getLogPath());
                assertEquals(SeccompRule.SECCOMP_RULE_GENERAL, options.getSeccompRuleName());
                assertEquals(null, options.getExePath());
                assertEquals(null, options.getInputPath());
                assertEquals(null, options.getOutputPath());
                assertEquals(null, options.getErrorPath());
                assertEquals(0, options.getArgs().size());
                assertEquals(0, options.getEnv().size());
        }

        @Test
        public void optionsToStringShouldBeValid() {
                JudgerOptions options = JudgerOptions.builder()
                                .exePath("a")
                                .inputPath("a")
                                .outputPath("a")
                                .errorPath("a")
                                .build();
                assertEquals(
                                " --uid=0 --gid=0 --memory_limit_check_only=0 --exe_path=a --input_path=a --output_path=a --error_path=a --log_path=judger.log --seccomp_rule_name=general",
                                options.toString());

                List<String> env = new ArrayList<String>();
                env.add("a=1");
                env.add("b=1");
                options = JudgerOptions.builder()
                                .maxCpuTime(1000)
                                .env(env)
                                .exePath("a")
                                .inputPath("a")
                                .outputPath("a")
                                .errorPath("a")
                                .build();
                assertEquals(
                                " --max_cpu_time=1000 --uid=0 --gid=0 --memory_limit_check_only=0 --exe_path=a --input_path=a --output_path=a --error_path=a --log_path=judger.log --env=a=1 --env=b=1 --seccomp_rule_name=general",
                                options.toString());
        }
}
