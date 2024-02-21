package io.github.capure.dynamo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.capure.dynamo.exception.CannotSaveTestCaseException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.model.TestCase;
import io.github.capure.dynamo.repository.TestCaseRepository;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestCaseService.class })
public class TestCaseServiceTest {
    @MockBean
    TestCaseRepository testCaseRepository;

    @Autowired
    TestCaseService testCaseService;

    @Test
    public void addTestCaseShouldThrowForNullTestCase() {
        TestCase testCase = null;
        when(testCaseRepository.save(any())).thenThrow(new NullPointerException());

        assertThrows(CannotSaveTestCaseException.class, () -> testCaseService.addTestCase(testCase));
    }

    @Test
    public void findAllByProblemIdShouldThrowForNullProblemId() {
        Long problemId = null;
        when(testCaseRepository.findByProblemId(any())).thenThrow(new NullPointerException());

        assertThrows(TestCaseNotFoundException.class, () -> testCaseService.findAllByProblemId(problemId));
    }

    @Test
    public void deleteByIdAndProblemIdShouldThrowForNullId() {
        Long id = null;
        Long problemId = 1L;
        doThrow(new NullPointerException()).when(testCaseRepository).deleteByIdAndProblemId(any(), any());

        assertThrows(TestCaseNotFoundException.class, () -> testCaseService.deleteByIdAndProblemId(id, problemId));
    }

    @Test
    public void findAllByProblemIdShouldReturnForValidProblemId() {
        TestCase testCase = new TestCase(1L, 1L, "test", "in", "out", 10);
        when(testCaseRepository.findByProblemId(1L)).thenReturn(List.of(testCase));

        List<TestCase> testCases = assertDoesNotThrow(() -> testCaseService.findAllByProblemId(1L));
        assertEquals(1, testCases.size());
    }
}
