package br.com.lydero.gestao_api.auth;

import br.com.lydero.gestao_api.empresa.Empresa;
import br.com.lydero.gestao_api.empresa.EmpresaRepository;
import br.com.lydero.gestao_api.usuario.Usuario;
import br.com.lydero.gestao_api.usuario.UsuarioRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String MSG_CREDENCIAIS_INVALIDAS = "E-mail ou senha invalidos";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissaoService permissaoService;
    private final JwtService jwtService;

    /** Hash descartavel: gasta o mesmo tempo de calculo mesmo quando o e-mail nao existe. */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository,
                       EmpresaRepository empresaRepository,
                       PasswordEncoder passwordEncoder,
                       PermissaoService permissaoService,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissaoService = permissaoService;
        this.jwtService = jwtService;
        this.hashFalso = passwordEncoder.encode("senha-falsa-para-igualar-tempo");
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

        String hash = usuario != null ? usuario.getSenhaHash() : hashFalso;
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);

        if (usuario == null || !senhaConfere || !usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MSG_CREDENCIAIS_INVALIDAS);
        }

        Empresa empresa = empresaRepository.findById(usuario.getEmpresaId()).orElse(null);
        if (empresa == null || !empresa.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Empresa inativa");
        }

        usuario.setUltimoAcesso(LocalDateTime.now(ZoneOffset.UTC));

        List<String> permissoes = permissaoService.permissoesDe(usuario);
        String token = jwtService.gerar(usuario, permissoes);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.validadeSegundos(),
                new LoginResponse.UsuarioResumo(
                        usuario.getId(), usuario.getEmpresaId(), usuario.getNome(),
                        usuario.getEmail(), usuario.getTipo()),
                permissoes);
    }
}
