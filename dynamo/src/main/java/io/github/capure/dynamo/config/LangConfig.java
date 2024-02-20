package io.github.capure.dynamo.config;

import java.util.Arrays;
import java.util.List;

import io.github.capure.dynamo.judger.SeccompRule;

public abstract class LangConfig {
    abstract public String getCompileSrc();

    abstract public String getCompileExe();

    abstract public int getCompileMaxCpuTime();

    abstract public int getCompileMaxRealTime();

    abstract public int getCompileMaxMemory();

    abstract public String getCompileCommand();

    abstract public String getRunCommand();

    abstract public SeccompRule getRunSeccompRule();

    abstract public List<String> getRunEnv();

    protected List<String> getDefaultEnv() {
        return Arrays.asList("LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");
    }
}
