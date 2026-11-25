package com.vitoria.ctf.instance;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "challenge_instances")
public class ChallengeInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false, length = 80)
    private String containerName;

    @Column(nullable = false)
    private int hostPort;

    @Column(nullable = false)
    private Instant expiresAt;

    protected ChallengeInstance() {
    }

    public ChallengeInstance(Long userId, Long challengeId, String containerName,
                             int hostPort, Instant expiresAt) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.containerName = containerName;
        this.hostPort = hostPort;
        this.expiresAt = expiresAt;
    }

    public Long getUserId() { return userId; }
    public Long getChallengeId() { return challengeId; }
    public String getContainerName() { return containerName; }
    public int getHostPort() { return hostPort; }
    public Instant getExpiresAt() { return expiresAt; }
}
