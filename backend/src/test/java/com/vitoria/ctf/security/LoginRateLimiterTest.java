package com.vitoria.ctf.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    @Test
    void bloqueiaNoQuintoErro() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("1.2.3.4|ana");
        }
        assertThat(limiter.isBlocked("1.2.3.4|ana")).isFalse();

        limiter.recordFailure("1.2.3.4|ana");
        assertThat(limiter.isBlocked("1.2.3.4|ana")).isTrue();
    }

    @Test
    void outraChaveNaoEhAfetada() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("1.2.3.4|ana");
        }
        assertThat(limiter.isBlocked("9.9.9.9|ana")).isFalse();
        assertThat(limiter.isBlocked("1.2.3.4|bia")).isFalse();
    }

    @Test
    void loginCorretoLimpaOHistorico() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("1.2.3.4|ana");
        }
        limiter.reset("1.2.3.4|ana");
        assertThat(limiter.isBlocked("1.2.3.4|ana")).isFalse();
    }
}
