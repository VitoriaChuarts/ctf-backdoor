package com.vitoria.ctf.auth;

import com.vitoria.ctf.submission.SubmissionRepository;
import com.vitoria.ctf.user.User;
import com.vitoria.ctf.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class ProgressController {

    private final UserRepository users;
    private final SubmissionRepository submissions;

    public ProgressController(UserRepository users, SubmissionRepository submissions) {
        this.users = users;
        this.submissions = submissions;
    }

    @GetMapping("/api/me/progress")
    public Map<String, Object> progress(Authentication auth) {
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return Map.of("solved", submissions.solvedChallengeIds(user.getId()));
    }
}
