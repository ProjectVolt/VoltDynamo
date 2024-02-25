package io.github.capure.dynamo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Base64;
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
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.judger.JudgerResultCode;
import io.github.capure.dynamo.model.TestCase;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JudgeService.class, CompilerService.class })
public class JudgeServiceIntegrationTest {
    @Autowired
    private JudgeService judgeService;

    @MockBean
    private TestCaseService testCaseService;

    @Test
    public void integrationJudgeSubmissionShouldHandleCompileErrorC() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("#broken_code".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.C, 1000,
                1024 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());
        assertNotNull(result.getCompileError());
        assertFalse(result.getCompileError().getFatal());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleCompileErrorCPP() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("#broken_code".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.CPP, 1000,
                1024 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());
        assertNotNull(result.getCompileError());
        assertFalse(result.getCompileError().getFatal());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleCompileErrorPython() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("//broken_code".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.PYTHON, 1000,
                1024 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertFalse(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(0, result.getTestResults().size());
        assertNotNull(result.getCompileError());
        assertFalse(result.getCompileError().getFatal());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleRuntimeErrorC() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("int main() { return 0/0; }".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.C, 1000,
                128 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertTrue(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(JudgerResultCode.RESULT_RUNTIME_ERROR,
                result.getTestResults().get(0).getJudgerResult().getResult());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleRuntimeErrorCPP() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("int main() { return 0/0; }".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.CPP, 1000,
                128 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertTrue(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(JudgerResultCode.RESULT_RUNTIME_ERROR,
                result.getTestResults().get(0).getJudgerResult().getResult());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleRuntimeErrorPython() throws TestCaseNotFoundException {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String sourceCode = Base64.getEncoder().encodeToString("print(0/0)".getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.PYTHON, 1000,
                128 * 1024 * 1024);
        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));
        assertTrue(result.getCompileSuccess());
        assertFalse(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(JudgerResultCode.RESULT_RUNTIME_ERROR,
                result.getTestResults().get(0).getJudgerResult().getResult());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleCorrectAnswerC() throws TestCaseNotFoundException {
        String input = Base64.getEncoder().encodeToString("2".getBytes());
        String output = Base64.getEncoder().encodeToString("2".getBytes());
        TestCase testCase = new TestCase(1L, 1L, "test", input, output, 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String testProgram = """
                #include "stdio.h"

                int main() {
                    int a;
                    scanf("%d", &a);
                    printf("%d\\n", a);
                    return 0;
                }
                """;
        String sourceCode = Base64.getEncoder().encodeToString(testProgram.getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.C, 1000,
                128 * 1024 * 1024);

        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));

        assertTrue(result.getCompileSuccess());
        assertTrue(result.getRunSuccess());
        assertTrue(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(10, result.getTestResults().get(0).getScore());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleCorrectAnswerCPP() throws TestCaseNotFoundException {
        String input = Base64.getEncoder().encodeToString("2".getBytes());
        String output = Base64.getEncoder().encodeToString("2".getBytes());
        TestCase testCase = new TestCase(1L, 1L, "test", input, output, 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String testProgram = """
                #include <iostream>
                int main() {
                    int a;
                    std::cin >> a;
                    std::cout << a << std::endl;
                    return 0;
                }
                """;
        String sourceCode = Base64.getEncoder().encodeToString(testProgram.getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.CPP, 1000,
                128 * 1024 * 1024);

        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));

        assertTrue(result.getCompileSuccess());
        assertTrue(result.getRunSuccess());
        assertTrue(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(10, result.getTestResults().get(0).getScore());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleCorrectAnswerPython() throws TestCaseNotFoundException {
        String input = Base64.getEncoder().encodeToString("2".getBytes());
        String output = Base64.getEncoder().encodeToString("2".getBytes());
        TestCase testCase = new TestCase(1L, 1L, "test", input, output, 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase));
        String testProgram = """
                def main():
                    a = input()
                    print(a)


                if __name__ == "__main__":
                    main()
                """;
        String sourceCode = Base64.getEncoder().encodeToString(testProgram.getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.PYTHON, 1000,
                128 * 1024 * 1024);

        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));

        assertTrue(result.getCompileSuccess());
        assertTrue(result.getRunSuccess());
        assertTrue(result.getAnswerSuccess());
        assertEquals(1, result.getTestResults().size());
        assertEquals(10, result.getTestResults().get(0).getScore());
    }

    @Test
    public void integrationJudgeSubmissionShouldHandleWrongAnswer() throws TestCaseNotFoundException {
        String input1 = Base64.getEncoder().encodeToString("2".getBytes());
        String input2 = Base64.getEncoder().encodeToString("2".getBytes());
        String output1 = Base64.getEncoder().encodeToString("4".getBytes());
        String output2 = Base64.getEncoder().encodeToString("2".getBytes());
        TestCase testCase1 = new TestCase(1L, 1L, "test", input1, output1, 10);
        TestCase testCase2 = new TestCase(2L, 1L, "test", input2, output2, 10);
        when(testCaseService.findAllByProblemId(1L)).thenReturn(List.of(testCase1, testCase2));
        String testProgram = """
                #include <iostream>
                int main() {
                    int a;
                    std::cin >> a;
                    std::cout << a << std::endl;
                    return 0;
                }
                """;
        String sourceCode = Base64.getEncoder().encodeToString(testProgram.getBytes());
        SubmissionDetails submissionDetails = new SubmissionDetails(1L, 1L, sourceCode, Language.CPP, 1000,
                128 * 1024 * 1024);

        SubmissionResult result = assertDoesNotThrow(() -> judgeService.judgeSubmission(submissionDetails));

        assertTrue(result.getCompileSuccess());
        assertTrue(result.getRunSuccess());
        assertFalse(result.getAnswerSuccess());
        assertEquals(2, result.getTestResults().size());
        assertEquals(JudgerResultCode.RESULT_WRONG_ANSWER,
                result.getTestResults().get(0).getJudgerResult().getResult());
        assertEquals("2", result.getTestResults().get(0).getOutput());
        assertEquals(0, result.getTestResults().get(0).getScore());
        assertEquals(JudgerResultCode.RESULT_SUCCESS,
                result.getTestResults().get(1).getJudgerResult().getResult());
        assertEquals("2", result.getTestResults().get(1).getOutput());
        assertEquals(10, result.getTestResults().get(1).getScore());
    }

}
