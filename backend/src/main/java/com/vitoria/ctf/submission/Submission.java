package com.vitoria.ctf.submission;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    protected Submission() {
    }

    public Submission(Long userId, Long challengeId, boolean correct) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getChallengeId() { return challengeId; }
    public boolean isCorrect() { return correct; }
    public Instant getSubmittedAt() { return submittedAt; }
}
