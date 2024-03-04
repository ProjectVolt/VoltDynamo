package io.github.capure.dynamo.processor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.capure.dynamo.exception.CannotSaveTestCaseException;
import io.github.capure.dynamo.exception.TestCaseNotFoundException;
import io.github.capure.dynamo.service.TestCaseService;
import io.github.capure.schema.TestCaseEvent;
import io.github.capure.schema.TestCaseEventDetails;
import io.github.capure.schema.TestCaseEventType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestCaseProcessor.class })
public class TestCaseProcessorTest {
    @Autowired
    TestCaseProcessor testCaseProcessor;

    @MockBean
    TestCaseService testCaseService;

    @Test
    public void shouldProcessDeleteTestCaseEvent() {
        TestCaseEvent event = new TestCaseEvent(TestCaseEventType.DELETE,
                new TestCaseEventDetails(1L, 1L, "", "", "", 0));

        assertDoesNotThrow(() -> testCaseProcessor
                .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null, event)));
    }

    @Test
    public void shouldThrowOnExceptionWhileProccesingDeleteTestCaseEvent() throws TestCaseNotFoundException {
        TestCaseEvent event = new TestCaseEvent(TestCaseEventType.DELETE,
                new TestCaseEventDetails(1L, 1L, "", "", "", 0));

        doThrow(TestCaseNotFoundException.class).when(testCaseService).deleteByIdAndProblemId(1L, 1L);

        assertThrows(java.lang.RuntimeException.class, () -> testCaseProcessor
                .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null, event)));
    }

    @Test
    public void shouldProcessAddTestCaseEvent() {
        TestCaseEvent event = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(1L, 1L, "name", "input", "output", 10));

        assertDoesNotThrow(() -> testCaseProcessor
                .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null, event)));
    }

    @Test
    public void shouldThrowOnExceptionWhileProccesingAddTestCaseEvent() throws CannotSaveTestCaseException {
        TestCaseEvent event = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(1L, 1L, "name", "input", "output", 10));

        doThrow(CannotSaveTestCaseException.class).when(testCaseService).addTestCase(any());

        assertThrows(java.lang.RuntimeException.class, () -> testCaseProcessor
                .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null, event)));
    }

    @Test
    public void shouldThrowOnEmptyPropertyWhileProccesingAddTestCaseEvent() {
        TestCaseEvent event1 = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(1L, 1L, "", "input", "output", 10));
        TestCaseEvent event2 = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(2L, 1L, "name", "", "output", 10));
        TestCaseEvent event3 = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(3L, 1L, "name", "input", "", 10));
        TestCaseEvent event4 = new TestCaseEvent(TestCaseEventType.ADD,
                new TestCaseEventDetails(3L, 1L, "name", "input", "output", 0));

        assertAll(() -> assertThrows(java.lang.IllegalArgumentException.class, () -> testCaseProcessor
                .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null, event1))),
                () -> assertThrows(java.lang.IllegalArgumentException.class, () -> testCaseProcessor
                        .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null,
                                event2))),
                () -> assertThrows(java.lang.IllegalArgumentException.class, () -> testCaseProcessor
                        .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null,
                                event3))),
                () -> assertThrows(java.lang.IllegalArgumentException.class, () -> testCaseProcessor
                        .process(new ConsumerRecord<String, TestCaseEvent>("test_case_edit_events", 0, 0, null,
                                event4))));
    }
}
