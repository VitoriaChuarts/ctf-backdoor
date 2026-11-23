package com.vitoria.ctf.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByUserIdAndChallengeIdAndCorrectTrue(Long userId, Long challengeId);

    long countByUserIdAndChallengeIdAndSubmittedAtAfter(Long userId, Long challengeId, Instant since);

    @Query("""
            select u.username as username, sum(c.points) as score
            from Submission s, User u, Challenge c
            where s.correct = true and s.userId = u.id and s.challengeId = c.id
            group by u.username
            order by sum(c.points) desc, max(s.submittedAt) asc
            """)
    List<ScoreRow> scoreboard();
}
