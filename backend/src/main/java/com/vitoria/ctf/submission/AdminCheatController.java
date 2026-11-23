package com.vitoria.ctf.submission;

import com.vitoria.ctf.user.User;
import com.vitoria.ctf.user.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdminCheatController {

    private final CheatAlertRepository alerts;
    private final UserRepository users;

    public AdminCheatController(CheatAlertRepository alerts, UserRepository users) {
        this.alerts = alerts;
        this.users = users;
    }

    @GetMapping("/api/admin/cheats")
    public List<Map<String, Object>> cheats() {
        return alerts.findAll().stream()
                .map(a -> Map.<String, Object>of(
                        "challengeId", a.getChallengeId(),
                        "submitter", nameOf(a.getSubmitterId()),
                        "flagOwner", nameOf(a.getOwnerId()),
                        "at", a.getCreatedAt().toString()))
                .toList();
    }

    private String nameOf(Long userId) {
        return users.findById(userId).map(User::getUsername).orElse("?");
    }
}
