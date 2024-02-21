package io.github.capure.dynamo.exception;

import io.github.capure.dynamo.model.TestCase;

public class CannotSaveTestCaseException extends Exception {
    public CannotSaveTestCaseException(TestCase testCase) {
        super("Cannot save the test case: problemId " +
                (testCase == null ? null : testCase.getProblemId()) + " id " +
                (testCase == null ? null : testCase.getId()));
    }
}
