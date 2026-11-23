package com.vitoria.ctf.challenge;

public record ChallengeView(
        Long id,
        String title,
        String description,
        String category,
        Difficulty difficulty,
        int points
) {
    public static ChallengeView from(Challenge c) {
        return new ChallengeView(c.getId(), c.getTitle(), c.getDescription(),
                c.getCategory(), c.getDifficulty(), c.getPoints());
    }
}
