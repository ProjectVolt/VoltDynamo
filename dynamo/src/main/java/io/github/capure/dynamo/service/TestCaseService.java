package io.github.capure.dynamo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.capure.dynamo.exception.CannotSaveTestCaseException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.model.TestCase;
import io.github.capure.dynamo.repository.TestCaseRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TestCaseService {
    @Autowired
    private TestCaseRepository testCaseRepository;

    public void deleteByIdAndProblemId(Long id, Long problemId) throws TestCaseNotFoundException {
        try {
            log.info("Deleting test case with id {} and problem id {}", id, problemId);
            testCaseRepository.deleteByIdAndProblemId(id, problemId);
        } catch (Exception e) {
            log.error("Test case not found. ProblemId: " + problemId + ", id: " + id, e);
            throw new TestCaseNotFoundException(problemId, id);
        }
    }

    public void addTestCase(TestCase testCase) throws CannotSaveTestCaseException {
        try {
            log.info("Saving test case {}", testCase);
            testCaseRepository.save(testCase);
        } catch (Exception e) {
            log.error("Cannot save test case. Test case: " + testCase, e);
            throw new CannotSaveTestCaseException(testCase);
        }
    }

    public List<TestCase> findAllByProblemId(Long problemId) throws TestCaseNotFoundException {
        try {
            log.debug("Finding all test cases for problem id {}", problemId);
            return testCaseRepository.findByProblemId(problemId);
        } catch (Exception e) {
            log.error("Test case not found. ProblemId: " + problemId, e);
            throw new TestCaseNotFoundException(problemId);
        }
    }
}
