package com.vitoria.ctf.challenge;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChallengeController {

    private final ChallengeRepository challenges;
    private final FlagService flags;

    public ChallengeController(ChallengeRepository challenges, FlagService flags) {
        this.challenges = challenges;
        this.flags = flags;
    }

    @GetMapping("/challenges")
    public List<ChallengeView> list() {
        return challenges.findByActiveTrueOrderByPointsAsc()
                .stream()
                .map(ChallengeView::from)
                .toList();
    }

    @PostMapping("/admin/challenges")
    public ResponseEntity<?> create(@Valid @RequestBody ChallengeRequest req) {
        String hash = null;
        if (!req.dynamicFlag()) {
            if (req.flag() == null || req.flag().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Informe a flag ou use dynamicFlag=true"));
            }
            hash = FlagHasher.sha256(req.flag());
        }

        Challenge challenge = new Challenge(
                req.title(), req.description(), req.category(),
                req.difficulty(), req.points(), hash, req.dynamicFlag());
        challenges.save(challenge);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChallengeView.from(challenge));
    }

    @GetMapping("/admin/challenges/{id}/flag/{userId}")
    public ResponseEntity<?> flagFor(@PathVariable Long id, @PathVariable Long userId) {
        Challenge challenge = challenges.findById(id)
                .filter(Challenge::isDynamicFlag)
                .orElse(null);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Desafio dinâmico não encontrado"));
        }
        return ResponseEntity.ok(Map.of("flag", flags.flagFor(userId, id)));
    }
}
