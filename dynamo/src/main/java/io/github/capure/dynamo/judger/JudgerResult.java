package io.github.capure.dynamo.judger;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.capure.schema.AvroJudgerResultCode;
import io.github.capure.schema.AvroJudgerResultError;
import io.github.capure.schema.AvroJudgerResult;
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

    public AvroJudgerResult toAvro() {
        return new AvroJudgerResult(cpuTime,
                realTime,
                memory,
                signal,
                exitCode,
                AvroJudgerResultError.valueOf(error.name()),
                AvroJudgerResultCode.valueOf(result.name()));
    }
}
