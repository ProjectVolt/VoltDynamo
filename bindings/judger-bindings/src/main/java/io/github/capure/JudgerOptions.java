package io.github.capure;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JudgerOptions {
    @Builder.Default
    private int maxCpuTime = -1;
    @Builder.Default
    private int maxRealTime = -1;
    @Builder.Default
    private int maxMemory = -1;
    @Builder.Default
    private int maxStack = -1;
    @Builder.Default
    private int maxOutputSize = -1;
    @Builder.Default
    private int maxProcessNumber = -1;
    private String exePath;
    private String inputPath;
    private String outputPath;
    private String errorPath;
    @Builder.Default
    private List<String> args = new ArrayList<>();
    @Builder.Default
    private List<String> env = new ArrayList<>();
    @Builder.Default
    private String logPath = "judger.log";
    @Builder.Default
    private SeccompRule seccompRuleName = SeccompRule.SECCOMP_RULE_GENERAL;
    @Builder.Default
    private int uid = 0;
    @Builder.Default
    private int gid = 0;
    @Builder.Default
    private int memoryLimitCheckOnly = 0;

    public String toString() {
        StringBuffer result = new StringBuffer();

        if (maxCpuTime != -1)
            result.append(" --max_cpu_time=" + maxCpuTime);
        if (maxRealTime != -1)
            result.append(" --max_real_time=" + maxRealTime);
        if (maxMemory != -1)
            result.append(" --max_memory=" + maxMemory);
        if (maxStack != -1)
            result.append(" --max_stack=" + maxStack);
        if (maxOutputSize != -1)
            result.append(" --max_output_size=" + maxOutputSize);
        if (maxProcessNumber != -1)
            result.append(" --max_process_number=" + maxProcessNumber);

        result.append(" --uid=" + uid);
        result.append(" --gid=" + gid);
        result.append(" --memory_limit_check_only=" + memoryLimitCheckOnly);

        if (exePath != null)
            result.append(" --exe_path=" + exePath);
        if (inputPath != null)
            result.append(" --input_path=" + inputPath);
        if (outputPath != null)
            result.append(" --output_path=" + outputPath);
        if (errorPath != null)
            result.append(" --error_path=" + errorPath);

        result.append(" --log_path=" + logPath);

        for (String arg : args) {
            result.append(" --args=" + arg);
        }

        for (String e : env) {
            result.append(" --env=" + e);
        }

        if (seccompRuleName != null)
            result.append(" --seccomp_rule_name=" + seccompRuleName.toString());

        return result.toString();
    }
}
