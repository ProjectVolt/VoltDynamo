package io.github.capure.dynamo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)
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
}
