package com.vitoria.ctf.submission;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
public class ScoreboardController {

    private final SubmissionRepository submissions;

    public ScoreboardController(SubmissionRepository submissions) {
        this.submissions = submissions;
    }

    @GetMapping("/api/scoreboard")
    public List<ScoreboardEntry> scoreboard() {
        List<ScoreRow> rows = submissions.scoreboard();
        return IntStream.range(0, rows.size())
                .mapToObj(i -> new ScoreboardEntry(
                        i + 1,
                        rows.get(i).getUsername(),
                        rows.get(i).getScore()))
                .toList();
    }
}
