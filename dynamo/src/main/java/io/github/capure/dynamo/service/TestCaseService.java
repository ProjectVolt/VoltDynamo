package io.github.capure.dynamo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.capure.dynamo.exception.CannotSaveTestCaseException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.model.TestCase;
import io.github.capure.dynamo.repository.TestCaseRepository;

@Service
public class TestCaseService {
    @Autowired
    private TestCaseRepository testCaseRepository;

    public void deleteByIdAndProblemId(Long id, Long problemId) throws TestCaseNotFoundException {
        try {
            testCaseRepository.deleteByIdAndProblemId(id, problemId);
        } catch (Exception e) {
            throw new TestCaseNotFoundException(problemId, id);
        }
    }

    public void addTestCase(TestCase testCase) throws CannotSaveTestCaseException {
        try {
            testCaseRepository.save(testCase);
        } catch (Exception e) {
            throw new CannotSaveTestCaseException(testCase);
        }
    }

    public List<TestCase> findAllByProblemId(Long problemId) throws TestCaseNotFoundException {
        try {
            return testCaseRepository.findByProblemId(problemId);
        } catch (Exception e) {
            throw new TestCaseNotFoundException(problemId);
        }
    }
}
