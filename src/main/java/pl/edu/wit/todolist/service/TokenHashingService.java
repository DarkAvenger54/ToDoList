package pl.edu.wit.todolist.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class TokenHashingService {
    public String sha256Hex(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }
}
