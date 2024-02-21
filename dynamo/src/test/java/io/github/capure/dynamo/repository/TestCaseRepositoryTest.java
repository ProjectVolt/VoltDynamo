package io.github.capure.dynamo.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import io.github.capure.dynamo.model.TestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

@DataJpaTest
public class TestCaseRepositoryTest {

	@Autowired
	private TestCaseRepository testCaseRepository;

	@Test
	public void testFindByProblemId() {
		TestCase testCase1 = new TestCase(1L, 1L, "TestName", "Input", "Output", 100);
		TestCase testCase2 = new TestCase(2L, 1L, "OtherTestName", "Input", "Output", 100);
		TestCase testCase3 = new TestCase(3L, 2L, "AnotherProblem", "Input", "Output", 10);
		testCaseRepository.saveAll(List.of(testCase1, testCase2, testCase3));

		List<TestCase> testCases = testCaseRepository.findByProblemId(1L);
		assertEquals(2, testCases.size(), "Should return 2 test cases for problem 1");

		testCases = testCaseRepository.findByProblemId(2L);
		assertEquals(1, testCases.size(), "Should return 1 test case for problem 2");
		assertEquals(10, testCases.get(0).getMaxScore(), "Should return test case with score 10 for problem 2");
	}

	@Test
	public void testDeleteByIdAndProblemId() {
		TestCase testCase1 = new TestCase(1L, 1L, "TestName", "Input", "Output", 100);
		TestCase testCase2 = new TestCase(2L, 1L, "OtherTestName", "Input", "Output", 100);
		TestCase testCase3 = new TestCase(3L, 2L, "AnotherProblem", "Input", "Output", 10);
		testCaseRepository.saveAll(List.of(testCase1, testCase2, testCase3));

		testCaseRepository.deleteByIdAndProblemId(1L, 1L);
		List<TestCase> testCases = testCaseRepository.findByProblemId(1L);
		assertEquals(1, testCases.size(), "Should return 1 test case for problem 1");
		assertEquals(2L, testCases.get(0).getId(), "Should return test case with id 2 for problem 1");
	}

	@Test
	public void addingNullTestCaseShouldThrowException() {
		TestCase testCase = new TestCase(1L, 1L, null, "Input", "Output", 100);
		assertThrows(Exception.class, () -> testCaseRepository.save(testCase));
	}
}