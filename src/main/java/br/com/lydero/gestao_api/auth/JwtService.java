package br.com.lydero.gestao_api.auth;

import br.com.lydero.gestao_api.usuario.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Gera o token de acesso. A validacao fica por conta do Spring Security (JwtDecoder). */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final long validadeMinutos;

    public JwtService(JwtEncoder encoder,
                      @Value("${app.jwt.expiration-minutes:60}") long validadeMinutos) {
        this.encoder = encoder;
        this.validadeMinutos = validadeMinutos;
    }

    public long validadeSegundos() {
        return validadeMinutos * 60;
    }

    public String gerar(Usuario usuario, List<String> permissoes) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-api")
                .issuedAt(agora)
                .expiresAt(agora.plus(validadeMinutos, ChronoUnit.MINUTES))
                .subject(String.valueOf(usuario.getId()))
                .claim("empresaId", usuario.getEmpresaId())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .claim("tipo", usuario.getTipo())
                .claim("authorities", permissoes)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
