package br.com.smartmeal.smartmeal.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.*;

public class SenhaUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String criptografar(String senhaLimpa) {
        if (senhaLimpa == null || senhaLimpa.trim().isEmpty()) {
            return null;
        }
        return encoder.encode(senhaLimpa);
    }

    public static boolean verificar(String senhaLimpa, String senhaCriptografada) {
        return encoder.matches(senhaLimpa, senhaCriptografada);
    }
}
