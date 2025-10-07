package com.abnamro.nl.andes.service;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TempFileStore {

    private final Map<String, Path> tokenToPath = new ConcurrentHashMap<>();

    /** Saves bytes to a temp .xlsx file, returns a download token */
    public String save(byte[] bytes) throws Exception {
        Path temp = Files.createTempFile("andes-intake-", ".xlsx");
        Files.write(temp, bytes);
        String token = UUID.randomUUID().toString();
        tokenToPath.put(token, temp);
        return token;
    }

    /** Resolves a token to a Path (caller can stream it). */
    public Path resolve(String token) {
        return tokenToPath.get(token);
    }

    /** Optionally remove after download */
    public void remove(String token) {
        Path p = tokenToPath.remove(token);
        if (p != null) {
            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
        }
    }
}
