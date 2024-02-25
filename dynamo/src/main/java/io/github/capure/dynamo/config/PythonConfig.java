package io.github.capure.dynamo.config;

import java.util.List;

import io.github.capure.dynamo.judger.SeccompRule;
import lombok.Getter;

@Getter
public class PythonConfig extends LangConfig {
    private final String compileSrc = "solution.py";
    private final String compileExe = "solution.py";
    private final int compileMaxCpuTime = 3000;
    private final int compileMaxRealTime = 10000;
    private final int compileMaxMemory = 128 * 1024 * 1024;
    private final String compileCommand = "/usr/bin/python3 -m py_compile %s";

    private final String runCommand = "/usr/bin/python3 -BS %s";
    private final SeccompRule runSeccompRule = SeccompRule.SECCOMP_RULE_GENERAL;
    private final List<String> runEnv = getDefaultEnv();
}
