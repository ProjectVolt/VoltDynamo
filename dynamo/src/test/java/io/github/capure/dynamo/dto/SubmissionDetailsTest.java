package io.github.capure.dynamo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SubmissionDetailsTest {
    @Test
    public void testGetSourceCode() {
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, "c29tZSBjb2Rl", Language.CPP, 1, 1);
        assertEquals("some code", submissionDetails.getSourceCode());
    }
}
