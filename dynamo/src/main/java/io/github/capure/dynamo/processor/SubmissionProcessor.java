package io.github.capure.dynamo.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.capure.dynamo.dto.SubmissionDetails;
import io.github.capure.dynamo.dto.SubmissionResult;
import io.github.capure.dynamo.service.JudgeService;
import io.github.capure.schema.AvroSubmission;
import io.github.capure.schema.AvroSubmissionResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubmissionProcessor {
    @Autowired
    private KafkaTemplate<String, AvroSubmissionResult> kafkaTemplate;

    @Autowired
    private JudgeService judgeService;

    @KafkaListener(topics = "submissions", groupId = "dynamo")
    @Transactional(transactionManager = "kafkaTransactionManager")
    public void process(ConsumerRecord<String, AvroSubmission> submission) {
        try {
            log.info("Processing submission {}", submission.value().getSubmissionId());
            SubmissionDetails details = new SubmissionDetails(submission.value());
            
            SubmissionResult result = judgeService.judgeSubmission(details);
            
            kafkaTemplate.send("submission_results", result.toAvro());
            log.info("Submission {} processed successfully", submission.value().getSubmissionId());
        } catch (Exception e) {
            log.error("Error processing submission {}", submission.value().getSubmissionId(), e);
            log.info("Rolling back transaction");
            throw e;
        }
    }
}
