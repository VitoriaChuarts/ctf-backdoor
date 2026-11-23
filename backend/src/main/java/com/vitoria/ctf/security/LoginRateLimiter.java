package com.vitoria.ctf.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Deque<Instant> queue = failures.get(key);
        if (queue == null) {
            return false;
        }
        synchronized (queue) {
            purge(queue);
            return queue.size() >= MAX_FAILURES;
        }
    }

    public void recordFailure(String key) {
        Deque<Instant> queue = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            purge(queue);
            queue.addLast(Instant.now());
        }
    }

    public void reset(String key) {
        failures.remove(key);
    }

    private void purge(Deque<Instant> queue) {
        Instant limit = Instant.now().minus(WINDOW);
        while (!queue.isEmpty() && queue.peekFirst().isBefore(limit)) {
            queue.pollFirst();
        }
    }
}
