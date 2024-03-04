package io.github.capure.dynamo.dto;

import io.github.capure.schema.AvroSubmissionResult;

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
    private CompileError compileError = new CompileError("", false);
    @NonNull
    private List<TestCaseResult> testResults;

    public AvroSubmissionResult toAvro() {
        return new AvroSubmissionResult(submissionId,
                problemId,
                compileSuccess,
                runSuccess,
                answerSuccess,
                compileError.toAvro(),
                testResults.stream().map(TestCaseResult::toAvro).toList());
    }
}
