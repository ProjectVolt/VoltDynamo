package io.github.capure.dynamo.config;

import java.util.List;

import io.github.capure.dynamo.judger.SeccompRule;
import lombok.Getter;

@Getter
public class CppConfig extends LangConfig {
    private final String compileSrc = "main.cpp";
    private final String compileExe = "main";
    private final int compileMaxCpuTime = 10000;
    private final int compileMaxRealTime = 20000;
    private final int compileMaxMemory = 1024 * 1024 * 1024;
    private final String compileCommand = "/usr/bin/g++ -DVOLT -O2 -w -fmax-errors=3 -std=c++20 %s -lm -o %s";

    private final String runCommand = "%s";
    private final SeccompRule runSeccompRule = SeccompRule.SECCOMP_RULE_C_CPP;
    private final List<String> runEnv = getDefaultEnv();
}
