package com.vitoria.ctf.challenge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChallengeRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 30) String category,
        @NotNull Difficulty difficulty,
        @Min(10) @Max(1000) int points,
        @Size(min = 8, max = 200) String flag,
        boolean dynamicFlag
) {
}
