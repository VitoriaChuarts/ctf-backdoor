package com.vitoria.ctf.challenge;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AdminContainerController {

    private final ChallengeRepository challenges;

    public AdminContainerController(ChallengeRepository challenges) {
        this.challenges = challenges;
    }

    @PutMapping("/api/admin/challenges/{id}/container")
    public ResponseEntity<?> attach(@PathVariable Long id, @Valid @RequestBody ContainerRequest req) {
        Challenge challenge = challenges.findById(id).orElse(null);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Desafio não encontrado"));
        }
        challenge.attachContainer(req.image(), req.port());
        challenges.save(challenge);
        return ResponseEntity.ok(Map.of(
                "challengeId", id, "image", req.image(), "port", req.port()));
    }
}
