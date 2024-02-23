package io.github.capure.dynamo.dto;

import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SubmissionResult {
    @NonNull
    private Long submissionId;
    @NonNull
    private Long problemId;
    @NonNull
    private Boolean compileSuccess;
    @NonNull
    private Boolean runSuccess;
    @NonNull
    private Boolean answerSuccess;
    private String compileError;
    @NonNull
    private List<TestCaseResult> testResults;
}
