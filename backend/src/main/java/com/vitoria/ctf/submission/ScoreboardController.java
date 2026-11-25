package com.vitoria.ctf.submission;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
public class ScoreboardController {

    private final ScoreboardService scoreboard;
    private final ScoreboardBroadcaster broadcaster;

    public ScoreboardController(ScoreboardService scoreboard, ScoreboardBroadcaster broadcaster) {
        this.scoreboard = scoreboard;
        this.broadcaster = broadcaster;
    }

    @GetMapping("/api/scoreboard")
    public List<ScoreboardEntry> scoreboard() {
        return scoreboard.current();
    }

    @GetMapping(value = "/api/scoreboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return broadcaster.subscribe(scoreboard.current());
    }
}
