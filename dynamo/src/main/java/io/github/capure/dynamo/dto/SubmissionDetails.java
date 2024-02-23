package io.github.capure.dynamo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.util.Base64;

import lombok.AccessLevel;

@Data
@AllArgsConstructor
public class SubmissionDetails {
    @NonNull
    private Long submissionId;
    @NonNull
    private Long problemId;
    @NonNull
    @Getter(AccessLevel.NONE)
    private String sourceCode;
    @NonNull
    private Language language;
    @NonNull
    private Integer timeLimit;
    @NonNull
    private Integer memoryLimit;

    public String getSourceCode() {
        return new String(Base64.getDecoder().decode(sourceCode));
    }
}
