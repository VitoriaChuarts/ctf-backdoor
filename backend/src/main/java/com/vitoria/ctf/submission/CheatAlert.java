package com.vitoria.ctf.submission;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cheat_alerts")
public class CheatAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long submitterId;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CheatAlert() {
    }

    public CheatAlert(Long challengeId, Long submitterId, Long ownerId) {
        this.challengeId = challengeId;
        this.submitterId = submitterId;
        this.ownerId = ownerId;
    }

    public Long getChallengeId() { return challengeId; }
    public Long getSubmitterId() { return submitterId; }
    public Long getOwnerId() { return ownerId; }
    public Instant getCreatedAt() { return createdAt; }
}
