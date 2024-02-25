package io.github.capure.dynamo.dto;

import io.github.capure.dynamo.judger.JudgerResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class TestCaseResult {
    @NonNull
    private Long testCaseId;
    @NonNull
    private String output;
    @NonNull
    private JudgerResult judgerResult;
    @NonNull
    private Integer score;
    @NonNull
    private String errorMessage;
}
