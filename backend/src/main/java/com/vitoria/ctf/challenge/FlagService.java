package com.vitoria.ctf.challenge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FlagService {

    private static final Pattern FORMAT = Pattern.compile("^CTF\\{u(\\d{1,18})_([0-9a-f]{16})\\}$");

    private final SecretKeySpec key;

    public FlagService(@Value("${app.flag.secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String flagFor(long userId, long challengeId) {
        return "CTF{u" + userId + "_" + mac(userId, challengeId) + "}";
    }

    public Optional<Long> ownerOf(String submitted, long challengeId) {
        Matcher m = FORMAT.matcher(submitted.trim());
        if (!m.matches()) {
            return Optional.empty();
        }
        long userId = Long.parseLong(m.group(1));
        byte[] expected = mac(userId, challengeId).getBytes(StandardCharsets.UTF_8);
        byte[] given = m.group(2).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given) ? Optional.of(userId) : Optional.empty();
    }

    private String mac(long userId, long challengeId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] out = mac.doFinal((userId + ":" + challengeId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(out, 0, 8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
