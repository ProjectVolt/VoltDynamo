package io.github.capure.dynamo;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class DynamoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamoApplication.class, args);
    }

    @Bean
    public NewTopic submissionTopic() {
        return TopicBuilder
                .name("submissions")
                .replicas(1)
                .partitions(1)
                .build();
    }

    @Bean
    public NewTopic submissionResultTopic() {
        return TopicBuilder
                .name("submission_results")
                .replicas(1)
                .partitions(1)
                .build();
    }

}
