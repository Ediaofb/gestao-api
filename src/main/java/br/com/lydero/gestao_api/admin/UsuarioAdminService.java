package br.com.lydero.gestao_api.admin;

import br.com.lydero.gestao_api.admin.dto.PermissaoDto;
import br.com.lydero.gestao_api.admin.dto.PermissoesRequest;
import br.com.lydero.gestao_api.admin.dto.SenhaRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioAtualizarRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioCriarRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioDetalheDto;
import br.com.lydero.gestao_api.admin.dto.UsuarioResumoDto;
import br.com.lydero.gestao_api.auditoria.AuditoriaService;
import br.com.lydero.gestao_api.common.UsuarioLogado;
import br.com.lydero.gestao_api.modulo.Acao;
import br.com.lydero.gestao_api.modulo.Modulo;
import br.com.lydero.gestao_api.modulo.ModuloRepository;
import br.com.lydero.gestao_api.usuario.Usuario;
import br.com.lydero.gestao_api.usuario.UsuarioPermissao;
import br.com.lydero.gestao_api.usuario.UsuarioPermissaoId;
import br.com.lydero.gestao_api.usuario.UsuarioPermissaoRepository;
import br.com.lydero.gestao_api.usuario.UsuarioRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regras da tela de Administracao > Usuarios e permissoes.
 *
 * Seguranca aplicada aqui (alem do @PreAuthorize nos controllers):
 *  - toda busca e filtrada pela empresa de quem esta logado (isolamento multiempresa);
 *  - quem NAO e administrador nao cria nem altera administradores;
 *  - quem NAO e administrador so concede permissoes que ele mesmo possui
 *    (evita que alguem se promova criando um usuario com mais poder);
 *  - ninguem desativa/rebaixa a si mesmo, e a empresa sempre mantem um administrador ativo.
 */
@Service
public class UsuarioAdminService {

    private static final String MODULO_LOG = "ADMINISTRACAO";
    private static final String ENTIDADE_LOG = "usuario";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPermissaoRepository permissaoRepository;
    private final ModuloRepository moduloRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoria;

    public UsuarioAdminService(UsuarioRepository usuarioRepository,
                               UsuarioPermissaoRepository permissaoRepository,
                               ModuloRepository moduloRepository,
                               PasswordEncoder passwordEncoder,
                               AuditoriaService auditoria) {
        this.usuarioRepository = usuarioRepository;
        this.permissaoRepository = permissaoRepository;
        this.moduloRepository = moduloRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<UsuarioResumoDto> listar(UsuarioLogado quem) {
        return usuarioRepository.findByEmpresaIdOrderByNomeAsc(quem.empresaId()).stream()
                .map(u -> new UsuarioResumoDto(u.getId(), u.getNome(), u.getEmail(), u.getTipo(),
                        u.isAtivo(), u.getUltimoAcesso()))
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDetalheDto buscar(UsuarioLogado quem, Long id) {
        return detalhe(obter(quem, id), moduloRepository.findByAtivoTrueOrderByOrdemAsc());
    }

    // ------------------------------------------------------------------ escrita

    @Transactional
    public UsuarioDetalheDto criar(UsuarioLogado quem, UsuarioCriarRequest req) {
        String tipo = req.tipo() == null ? Usuario.TIPO_COMUM : req.tipo();
        if (!quem.admin() && Usuario.TIPO_ADMIN.equals(tipo)) {
            throw erro(HttpStatus.FORBIDDEN, "Somente administradores podem criar outros administradores");
        }
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um usuario com este e-mail");
        }

        List<Modulo> modulos = moduloRepository.findByAtivoTrueOrderByOrdemAsc();
        List<PermissaoDto> pedidas = req.permissoes() == null ? List.of() : req.permissoes();
        Map<Long, PermissaoDto> matriz = validarMatriz(quem, modulos, pedidas);

        Usuario usuario = new Usuario();
        usuario.setEmpresaId(quem.empresaId());
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));
        usuario.setTipo(tipo);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        if (!usuario.isAdmin()) {
            gravarMatriz(usuario, modulos, matriz);
        }

        auditoria.registrar(quem, "CRIAR", MODULO_LOG, ENTIDADE_LOG, usuario.getId(),
                "Usuario criado: " + email + " (" + tipo + ")");
        return detalhe(usuario, modulos);
    }

    @Transactional
    public UsuarioDetalheDto atualizar(UsuarioLogado quem, Long id, UsuarioAtualizarRequest req) {
        Usuario usuario = obter(quem, id);

        if (!quem.admin() && (usuario.isAdmin() || Usuario.TIPO_ADMIN.equals(req.tipo()))) {
            throw erro(HttpStatus.FORBIDDEN, "Somente administradores podem alterar administradores");
        }
        boolean ehEleMesmo = usuario.getId().equals(quem.id());
        if (ehEleMesmo && !req.ativo()) {
            throw erro(HttpStatus.BAD_REQUEST, "Voce nao pode desativar a si mesmo");
        }

        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (!email.equalsIgnoreCase(usuario.getEmail()) && usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um usuario com este e-mail");
        }

        boolean deixaDeSerAdminAtivo = usuario.isAdmin() && usuario.isAtivo()
                && (!req.ativo() || !Usuario.TIPO_ADMIN.equals(req.tipo()));
        if (deixaDeSerAdminAtivo) {
            if (ehEleMesmo) {
                throw erro(HttpStatus.BAD_REQUEST, "Voce nao pode rebaixar a si mesmo");
            }
            long adminsAtivos = usuarioRepository.countByEmpresaIdAndTipoAndAtivoTrue(
                    quem.empresaId(), Usuario.TIPO_ADMIN);
            if (adminsAtivos <= 1) {
                throw erro(HttpStatus.CONFLICT, "A empresa precisa ter pelo menos um administrador ativo");
            }
        }

        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setTipo(req.tipo());
        usuario.setAtivo(req.ativo());

        auditoria.registrar(quem, "EDITAR", MODULO_LOG, ENTIDADE_LOG, usuario.getId(),
                "Usuario editado: " + email + " (" + req.tipo() + ", " + (req.ativo() ? "ativo" : "inativo") + ")");
        return detalhe(usuario, moduloRepository.findByAtivoTrueOrderByOrdemAsc());
    }

    @Transactional
    public UsuarioDetalheDto salvarPermissoes(UsuarioLogado quem, Long id, PermissoesRequest req) {
        Usuario usuario = obter(quem, id);

        if (usuario.isAdmin()) {
            throw erro(HttpStatus.BAD_REQUEST, "Administradores ja possuem acesso total; nao ha o que marcar");
        }
        if (!quem.admin() && usuario.getId().equals(quem.id())) {
            throw erro(HttpStatus.FORBIDDEN, "Voce nao pode alterar as suas proprias permissoes");
        }

        List<Modulo> modulos = moduloRepository.findByAtivoTrueOrderByOrdemAsc();
        Map<Long, PermissaoDto> matriz = validarMatriz(quem, modulos, req.permissoes());
        gravarMatriz(usuario, modulos, matriz);

        auditoria.registrar(quem, "PERMISSOES", MODULO_LOG, ENTIDADE_LOG, usuario.getId(),
                "Permissoes alteradas de " + usuario.getEmail());
        return detalhe(usuario, modulos);
    }

    @Transactional
    public void redefinirSenha(UsuarioLogado quem, Long id, SenhaRequest req) {
        Usuario usuario = obter(quem, id);
        if (!quem.admin() && usuario.isAdmin()) {
            throw erro(HttpStatus.FORBIDDEN, "Somente administradores podem redefinir a senha de administradores");
        }
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));

        // A senha nunca vai para o log.
        auditoria.registrar(quem, "REDEFINIR_SENHA", MODULO_LOG, ENTIDADE_LOG, usuario.getId(),
                "Senha redefinida de " + usuario.getEmail());
    }

    // ------------------------------------------------------------------ apoio

    /** Busca ja filtrando pela empresa: usuario de outra empresa vira "nao encontrado". */
    private Usuario obter(UsuarioLogado quem, Long id) {
        return usuarioRepository.findByIdAndEmpresaId(id, quem.empresaId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
    }

    /**
     * Confere a matriz enviada: modulos existentes e ativos, sem repeticao e, para quem nao
     * e administrador, sem conceder nada que ele mesmo nao possua. Devolve moduloId -> linha.
     */
    private Map<Long, PermissaoDto> validarMatriz(UsuarioLogado quem, List<Modulo> modulos,
                                                  List<PermissaoDto> pedidas) {
        Map<String, Modulo> porCodigo = new HashMap<>();
        for (Modulo modulo : modulos) {
            porCodigo.put(modulo.getCodigo(), modulo);
        }

        Map<Long, PermissaoDto> matriz = new HashMap<>();
        for (PermissaoDto linha : pedidas) {
            String codigo = linha.modulo().trim().toUpperCase(Locale.ROOT);
            Modulo modulo = porCodigo.get(codigo);
            if (modulo == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Modulo inexistente ou inativo: " + linha.modulo());
            }
            if (matriz.put(modulo.getId(), linha) != null) {
                throw erro(HttpStatus.BAD_REQUEST, "Modulo repetido na lista: " + codigo);
            }
            if (!quem.admin()) {
                exigirQuePossui(quem, codigo, Acao.VISUALIZAR, linha.visualizar());
                exigirQuePossui(quem, codigo, Acao.CRIAR, linha.criar());
                exigirQuePossui(quem, codigo, Acao.EDITAR, linha.editar());
                exigirQuePossui(quem, codigo, Acao.EXCLUIR, linha.excluir());
            }
        }
        return matriz;
    }

    private void exigirQuePossui(UsuarioLogado quem, String codigo, Acao acao, boolean concedida) {
        String permissao = codigo + ":" + acao.name();
        if (concedida && !quem.possui(permissao)) {
            throw erro(HttpStatus.FORBIDDEN,
                    "Voce nao pode conceder uma permissao que voce mesmo nao tem (" + permissao + ")");
        }
    }

    /**
     * Grava a matriz atualizando as linhas existentes e criando as que faltam. Modulo que nao
     * veio na lista fica sem nenhuma permissao. (Sem apagar e reinserir, para nao esbarrar
     * na ordem em que o Hibernate executa insercoes e exclusoes.)
     */
    private void gravarMatriz(Usuario usuario, List<Modulo> modulos, Map<Long, PermissaoDto> matriz) {
        Map<Long, UsuarioPermissao> existentes = new HashMap<>();
        for (UsuarioPermissao p : permissaoRepository.findByIdUsuarioId(usuario.getId())) {
            existentes.put(p.getId().getModuloId(), p);
        }

        List<UsuarioPermissao> paraSalvar = new ArrayList<>();
        for (Modulo modulo : modulos) {
            PermissaoDto linha = matriz.get(modulo.getId());
            UsuarioPermissao linhaBanco = existentes.get(modulo.getId());
            if (linha == null && linhaBanco == null) {
                continue; // nada marcado e nada gravado: nao precisa de linha
            }
            if (linhaBanco == null) {
                linhaBanco = new UsuarioPermissao();
                linhaBanco.setId(new UsuarioPermissaoId(usuario.getId(), modulo.getId()));
            }
            linhaBanco.setPodeVisualizar(linha != null && linha.visualizar());
            linhaBanco.setPodeCriar(linha != null && linha.criar());
            linhaBanco.setPodeEditar(linha != null && linha.editar());
            linhaBanco.setPodeExcluir(linha != null && linha.excluir());
            paraSalvar.add(linhaBanco);
        }
        permissaoRepository.saveAll(paraSalvar);
    }

    private UsuarioDetalheDto detalhe(Usuario usuario, List<Modulo> modulos) {
        Map<Long, UsuarioPermissao> porModulo = new HashMap<>();
        for (UsuarioPermissao p : permissaoRepository.findByIdUsuarioId(usuario.getId())) {
            porModulo.put(p.getId().getModuloId(), p);
        }

        List<PermissaoDto> linhas = new ArrayList<>();
        for (Modulo modulo : modulos) {
            if (usuario.isAdmin()) {
                linhas.add(new PermissaoDto(modulo.getCodigo(), true, true, true, true));
                continue;
            }
            UsuarioPermissao p = porModulo.get(modulo.getId());
            linhas.add(p == null
                    ? new PermissaoDto(modulo.getCodigo(), false, false, false, false)
                    : new PermissaoDto(modulo.getCodigo(), p.isPodeVisualizar(), p.isPodeCriar(),
                            p.isPodeEditar(), p.isPodeExcluir()));
        }
        return new UsuarioDetalheDto(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getTipo(), usuario.isAtivo(), usuario.getUltimoAcesso(), linhas);
    }

    private static ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
