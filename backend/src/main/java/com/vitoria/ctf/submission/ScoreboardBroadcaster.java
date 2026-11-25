package com.vitoria.ctf.submission;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ScoreboardBroadcaster {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(List<ScoreboardEntry> initial) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        send(emitter, initial);
        return emitter;
    }

    public void broadcast(List<ScoreboardEntry> board) {
        for (SseEmitter emitter : emitters) {
            send(emitter, board);
        }
    }

    private void send(SseEmitter emitter, List<ScoreboardEntry> board) {
        try {
            emitter.send(SseEmitter.event()
                    .name("scoreboard")
                    .data(board, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            emitters.remove(emitter);
        }
    }
}
