package com.vitoria.ctf.submission;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class ScoreboardService {

    private final SubmissionRepository submissions;

    public ScoreboardService(SubmissionRepository submissions) {
        this.submissions = submissions;
    }

    public List<ScoreboardEntry> current() {
        List<ScoreRow> rows = submissions.scoreboard();
        return IntStream.range(0, rows.size())
                .mapToObj(i -> new ScoreboardEntry(
                        i + 1,
                        rows.get(i).getUsername(),
                        rows.get(i).getScore()))
                .toList();
    }
}
