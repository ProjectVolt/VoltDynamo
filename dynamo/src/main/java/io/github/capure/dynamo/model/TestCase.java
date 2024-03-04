package io.github.capure.dynamo.model;

import io.github.capure.schema.TestCaseEventDetails;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestCase {
    @Id
    private Long id;
    @Column(nullable = false)
    private Long problemId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String input;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String output;
    @Column(nullable = false, columnDefinition = "TEXT")
    private int maxScore;

    public TestCase(TestCaseEventDetails details) {
        this.id = details.getId();
        this.problemId = details.getProblemId();
        this.name = details.getName().toString();
        this.input = details.getInput().toString();
        this.output = details.getOutput().toString();
        this.maxScore = details.getMaxScore();
    }
}
