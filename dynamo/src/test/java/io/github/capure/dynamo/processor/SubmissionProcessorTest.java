package io.github.capure.dynamo.processor;

import io.github.capure.dynamo.dto.CompileError;
import io.github.capure.dynamo.dto.SubmissionResult;
import io.github.capure.dynamo.service.JudgeService;
import io.github.capure.schema.AvroLanguage;
import io.github.capure.schema.AvroSubmission;
import io.github.capure.schema.AvroSubmissionResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { SubmissionProcessor.class })
public class SubmissionProcessorTest {
    @Autowired
    SubmissionProcessor submissionProcessor;

    @MockBean
    KafkaTemplate<String, AvroSubmissionResult> kafkaTemplate;

    @MockBean
    JudgeService judgeService;

    @Test
    public void shouldProcessSubmission() {
        String source = Base64.getEncoder().encodeToString("int main() {return 0;}".getBytes());
        AvroSubmission submission = new AvroSubmission();
        submission.setSubmissionId(1L);
        submission.setProblemId(1L);
        submission.setSourceCode(source);
        submission.setLanguage(AvroLanguage.CPP);
        submission.setTimeLimit(1000 * 100);
        submission.setMemoryLimit(1024 * 1024 * 256);
        
        SubmissionResult result = new SubmissionResult(1L, 1L, true, true, true, List.of());
        result.setCompileError(new CompileError("", false));

        when(judgeService.judgeSubmission(any())).thenReturn(result);

        submissionProcessor.process(new ConsumerRecord<String,AvroSubmission>("submissions", 0, 0, null, submission));
        
        verify(kafkaTemplate).send(eq("submission_results"), isA(AvroSubmissionResult.class));
    }
}