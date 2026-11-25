package com.vitoria.ctf.instance;

import com.vitoria.ctf.challenge.Challenge;
import com.vitoria.ctf.challenge.ChallengeRepository;
import com.vitoria.ctf.challenge.FlagService;
import com.vitoria.ctf.user.User;
import com.vitoria.ctf.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenges/{id}/instance")
public class InstanceController {

    private static final int MAX_INSTANCES_PER_USER = 2;

    private final UserRepository users;
    private final ChallengeRepository challenges;
    private final ChallengeInstanceRepository instances;
    private final DockerService docker;
    private final FlagService flags;
    private final String publicHost;
    private final long ttlMinutes;

    public InstanceController(UserRepository users,
                              ChallengeRepository challenges,
                              ChallengeInstanceRepository instances,
                              DockerService docker,
                              FlagService flags,
                              @Value("${app.docker.public-host}") String publicHost,
                              @Value("${app.instance.ttl-minutes}") long ttlMinutes) {
        this.users = users;
        this.challenges = challenges;
        this.instances = instances;
        this.docker = docker;
        this.flags = flags;
        this.publicHost = publicHost;
        this.ttlMinutes = ttlMinutes;
    }

    @PostMapping
    public ResponseEntity<?> start(@PathVariable Long id, Authentication auth) {
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Challenge challenge = challenges.findById(id)
                .filter(Challenge::isActive)
                .filter(Challenge::hasContainer)
                .orElse(null);
        if (challenge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Desafio sem ambiente"));
        }
        if (!challenge.isDynamicFlag()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Desafio com ambiente precisa de flag dinâmica"));
        }

        Optional<ChallengeInstance> existing = instances.findByUserIdAndChallengeId(user.getId(), id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(view(existing.get()));
        }

        if (instances.countByUserId(user.getId()) >= MAX_INSTANCES_PER_USER) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Você já tem ambientes demais ligados"));
        }

        String name = "ctf-" + id + "-" + user.getId();
        try {
            int port = docker.start(name, challenge.getDockerImage(),
                    challenge.getContainerPort(), flags.flagFor(user.getId(), id));
            ChallengeInstance instance = instances.save(new ChallengeInstance(
                    user.getId(), id, name, port,
                    Instant.now().plus(Duration.ofMinutes(ttlMinutes))));
            return ResponseEntity.status(HttpStatus.CREATED).body(view(instance));
        } catch (IllegalStateException e) {
            docker.removeQuietly(name);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "Não foi possível iniciar o ambiente"));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> stop(@PathVariable Long id, Authentication auth) {
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Optional<ChallengeInstance> existing = instances.findByUserIdAndChallengeId(user.getId(), id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Você não tem ambiente ligado neste desafio"));
        }
        docker.removeQuietly(existing.get().getContainerName());
        instances.delete(existing.get());
        return ResponseEntity.ok(Map.of("stopped", true));
    }

    private Map<String, Object> view(ChallengeInstance instance) {
        return Map.of(
                "url", "http://" + publicHost + ":" + instance.getHostPort(),
                "expiresAt", instance.getExpiresAt().toString());
    }
}
