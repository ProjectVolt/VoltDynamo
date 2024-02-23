package io.github.capure.dynamo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.capure.dynamo.dto.Language;
import io.github.capure.dynamo.dto.SubmissionDetails;
import io.github.capure.dynamo.dto.SubmissionResult;
import io.github.capure.dynamo.exception.CompileErrorException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.model.TestCase;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JudgeService.class })
public class JudgeServiceTest {
    @Autowired
    private JudgeService judgeService;

    @MockBean
    private CompilerService compilerService;

    @MockBean
    private TestCaseService testCaseService;

    @Test
    public void judgeSubmissionShouldHandleNoTestCases() throws TestCaseNotFoundException {
        when(testCaseService.findAllByProblemId(1L)).thenThrow(new TestCaseNotFoundException(1L));
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, "", Language.CPP, 1, 1);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());

        when(testCaseService.findAllByProblemId(2L)).thenReturn(new ArrayList<>());
        SubmissionDetails submissionDetails2 = new SubmissionDetails(2L, 2L, "", Language.CPP, 1, 1);
        result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails2));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());
    }

    @Test
    public void judgeSubmissionShouldHandleCompileError() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        when(compilerService.compile(any(), any(), any())).thenThrow(new CompileErrorException("Compile error"));
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, "", Language.CPP, 1, 1);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());
    }
}
