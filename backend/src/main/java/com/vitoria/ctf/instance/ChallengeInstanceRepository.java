package com.vitoria.ctf.instance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChallengeInstanceRepository extends JpaRepository<ChallengeInstance, Long> {

    Optional<ChallengeInstance> findByUserIdAndChallengeId(Long userId, Long challengeId);

    long countByUserId(Long userId);

    List<ChallengeInstance> findByExpiresAtBefore(Instant moment);
}
