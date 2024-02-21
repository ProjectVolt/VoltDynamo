package io.github.capure.dynamo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.capure.dynamo.model.TestCase;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    public List<TestCase> findByProblemId(Long problemId);

    public void deleteByIdAndProblemId(Long id, Long problemId);
}
