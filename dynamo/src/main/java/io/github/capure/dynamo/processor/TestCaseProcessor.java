package io.github.capure.dynamo.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.capure.dynamo.exception.CannotSaveTestCaseException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.model.TestCase;
import io.github.capure.dynamo.service.TestCaseService;
import io.github.capure.schema.TestCaseEvent;
import io.github.capure.schema.TestCaseEventType;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TestCaseProcessor {
    @Autowired
    private TestCaseService testCaseService;

    private void rollbackTransactionInfo(TestCaseEvent event, Exception e) {
        log.error("Error processing test case edit event for test case {}", event.getDetails().getId(), e);
        log.info("Rolling back transaction");
    }

    @KafkaListener(topics = "test_case_edit_events", groupId = "dynamo")
    @Transactional(transactionManager = "kafkaTransactionManager")
    public void process(ConsumerRecord<String, TestCaseEvent> testCaseEvent) {
        TestCaseEvent event = testCaseEvent.value();
        log.info("Processing test case edit event for test case {} - type {}", event.getDetails().getId(),
                event.getType());
        try {
            if (event.getType().equals(TestCaseEventType.ADD)) {
                if (event.getDetails().getName().isEmpty() || event.getDetails().getInput().isEmpty()
                        || event.getDetails().getOutput().isEmpty() || event.getDetails().getMaxScore() == 0) {
                    log.error("Invalid test case details for test case {}", event.getDetails().getId());
                    throw new IllegalArgumentException("One of the fields was empty");
                }
                testCaseService.addTestCase(new TestCase(event.getDetails()));
            } else if (event.getType().equals(TestCaseEventType.DELETE)) {
                testCaseService.deleteByIdAndProblemId(event.getDetails().getId(), event.getDetails().getProblemId());
            }
            log.info("Processed test case edit event for test case {}", event.getDetails().getId());
        } catch (CannotSaveTestCaseException e) {
            log.error("Cannot save test case {} to database", event.getDetails().getId());
            rollbackTransactionInfo(event, e);
            throw new RuntimeException(e);
        } catch (TestCaseNotFoundException e) {
            log.error("Test case {} not found", event.getDetails().getId());
            rollbackTransactionInfo(event, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            rollbackTransactionInfo(event, e);
            throw e;
        }
    }
}
