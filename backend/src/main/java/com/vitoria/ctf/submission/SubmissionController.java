package com.vitoria.ctf.submission;

import com.vitoria.ctf.challenge.Challenge;
import com.vitoria.ctf.challenge.ChallengeRepository;
import com.vitoria.ctf.challenge.FlagHasher;
import com.vitoria.ctf.challenge.FlagService;
import com.vitoria.ctf.user.User;
import com.vitoria.ctf.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenges")
public class SubmissionController {

    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;

    private final UserRepository users;
    private final ChallengeRepository challenges;
    private final SubmissionRepository submissions;
    private final CheatAlertRepository alerts;
    private final FlagService flags;
    private final ScoreboardService scoreboard;
    private final ScoreboardBroadcaster broadcaster;

    public SubmissionController(UserRepository users,
                                ChallengeRepository challenges,
                                SubmissionRepository submissions,
                                CheatAlertRepository alerts,
                                FlagService flags,
                                ScoreboardService scoreboard,
                                ScoreboardBroadcaster broadcaster) {
        this.users = users;
        this.challenges = challenges;
        this.submissions = submissions;
        this.alerts = alerts;
        this.flags = flags;
        this.scoreboard = scoreboard;
        this.broadcaster = broadcaster;
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id,
                                    @Valid @RequestBody SubmitRequest req,
                                    Authentication auth) {
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Challenge challenge = challenges.findById(id)
                .filter(Challenge::isActive)
                .orElse(null);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Desafio não encontrado"));
        }

        if (submissions.existsByUserIdAndChallengeIdAndCorrectTrue(user.getId(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Você já resolveu este desafio"));
        }

        long recent = submissions.countByUserIdAndChallengeIdAndSubmittedAtAfter(
                user.getId(), id, Instant.now().minusSeconds(60));
        if (recent >= MAX_ATTEMPTS_PER_MINUTE) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "60")
                    .body(Map.of("error", "Muitas tentativas. Aguarde 1 minuto."));
        }

        boolean correct;
        if (challenge.isDynamicFlag()) {
            Optional<Long> owner = flags.ownerOf(req.flag(), id);
            correct = owner.isPresent() && owner.get().equals(user.getId());
            if (owner.isPresent() && !owner.get().equals(user.getId())) {
                alerts.save(new CheatAlert(id, user.getId(), owner.get()));
            }
        } else {
            correct = FlagHasher.matches(req.flag(), challenge.getFlagHash());
        }

        submissions.save(new Submission(user.getId(), id, correct));

        if (correct) {
            broadcaster.broadcast(scoreboard.current());
            return ResponseEntity.ok(Map.of("correct", true, "points", challenge.getPoints()));
        }
        return ResponseEntity.ok(Map.of("correct", false));
    }
}
