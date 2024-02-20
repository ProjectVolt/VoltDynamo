package io.github.capure.dynamo.judger;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgerResult {
    @JsonProperty("cpu_time")
    private int cpuTime;
    @JsonProperty("real_time")
    private int realTime;
    private int memory;
    private int signal;
    @JsonProperty("exit_code")
    private int exitCode;
    private JudgerResultError error;
    private JudgerResultCode result;
}
