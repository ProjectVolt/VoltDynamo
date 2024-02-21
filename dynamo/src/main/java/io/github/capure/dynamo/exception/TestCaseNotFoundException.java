package io.github.capure.dynamo.exception;

public class TestCaseNotFoundException extends Exception {
    public TestCaseNotFoundException(Long problemId, Long id) {
        super("Test case with problemId " + problemId + " and id " + id + " not found");
    }

    public TestCaseNotFoundException(Long problemId) {
        super("Test cases with problemId " + problemId + " not found");
    }
}
