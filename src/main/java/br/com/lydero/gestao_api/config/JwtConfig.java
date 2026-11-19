package br.com.lydero.gestao_api.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Configura a chave e os componentes que assinam (encoder) e validam (decoder) os tokens JWT.
 * Algoritmo: HS256, com a chave vinda da variavel de ambiente JWT_SECRET.
 */
@Configuration
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
    private static final int TAMANHO_MINIMO_BYTES = 32;

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret:}") String secret) {
        byte[] bytes;
        if (secret == null || secret.isBlank()) {
            bytes = new byte[TAMANHO_MINIMO_BYTES];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET nao definido: usando uma chave aleatoria temporaria. "
                    + "Os tokens deixam de valer sempre que a aplicacao reiniciar. "
                    + "Defina JWT_SECRET (minimo de 32 caracteres) para uso continuo.");
        } else {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < TAMANHO_MINIMO_BYTES) {
                throw new IllegalStateException(
                        "JWT_SECRET precisa ter pelo menos 32 caracteres (tem " + bytes.length + ").");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
