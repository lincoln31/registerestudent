package com.example.registerestudent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Seguridad {

    // Aplica la función hash SHA-256 a un texto y lo devuelve en hexadecimal
    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
