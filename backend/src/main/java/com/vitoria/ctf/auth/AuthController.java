package com.vitoria.ctf.auth;

import com.vitoria.ctf.security.JwtService;
import com.vitoria.ctf.security.LoginRateLimiter;
import com.vitoria.ctf.user.User;
import com.vitoria.ctf.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final LoginRateLimiter limiter;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                          LoginRateLimiter limiter) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.limiter = limiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String email = req.email().toLowerCase();

        if (users.existsByUsername(req.username()) || users.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Usuário ou e-mail já cadastrado"));
        }

        User user = new User(req.username(), email, encoder.encode(req.password()));
        users.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", user.getId(), "username", user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        String key = http.getRemoteAddr() + "|" + req.username().toLowerCase();
        if (limiter.isBlocked(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", "300")
                    .body(Map.of("error", "Muitas tentativas. Tente novamente em alguns minutos."));
        }
        Optional<User> found = users.findByUsername(req.username());

        if (found.isEmpty() || !encoder.matches(req.password(), found.get().getPasswordHash())) {
            limiter.recordFailure(key);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais inválidas"));
        }

        limiter.reset(key);
        User user = found.get();
        String token = jwt.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
