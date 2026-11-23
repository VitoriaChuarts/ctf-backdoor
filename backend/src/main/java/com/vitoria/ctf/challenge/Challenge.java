package com.vitoria.ctf.challenge;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 30)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private int points;

    @Column(length = 64)
    private String flagHash;

    @Column(columnDefinition = "boolean not null default false")
    private boolean dynamicFlag;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Challenge() {
    }

    public Challenge(String title, String description, String category,
                     Difficulty difficulty, int points, String flagHash, boolean dynamicFlag) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.flagHash = flagHash;
        this.dynamicFlag = dynamicFlag;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getPoints() { return points; }
    public String getFlagHash() { return flagHash; }
    public boolean isDynamicFlag() { return dynamicFlag; }
    public boolean isActive() { return active; }
}
