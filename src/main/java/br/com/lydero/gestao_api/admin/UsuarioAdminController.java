package br.com.lydero.gestao_api.admin;

import br.com.lydero.gestao_api.admin.dto.PermissoesRequest;
import br.com.lydero.gestao_api.admin.dto.SenhaRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioAtualizarRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioCriarRequest;
import br.com.lydero.gestao_api.admin.dto.UsuarioDetalheDto;
import br.com.lydero.gestao_api.admin.dto.UsuarioResumoDto;
import br.com.lydero.gestao_api.common.UsuarioLogado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracao > Usuarios e permissoes.
 * Cada metodo exige a permissao do modulo ADMINISTRACAO correspondente a acao.
 * As regras mais finas (ex.: nao conceder o que nao se tem) ficam no service.
 */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
public class UsuarioAdminController {

    private final UsuarioAdminService service;

    public UsuarioAdminController(UsuarioAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRACAO:VISUALIZAR')")
    public List<UsuarioResumoDto> listar(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.listar(UsuarioLogado.de(jwt, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRACAO:VISUALIZAR')")
    public UsuarioDetalheDto buscar(@PathVariable Long id,
                                    @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.buscar(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMINISTRACAO:CRIAR')")
    public UsuarioDetalheDto criar(@Valid @RequestBody UsuarioCriarRequest req,
                                   @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.criar(UsuarioLogado.de(jwt, request), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRACAO:EDITAR')")
    public UsuarioDetalheDto atualizar(@PathVariable Long id,
                                       @Valid @RequestBody UsuarioAtualizarRequest req,
                                       @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.atualizar(UsuarioLogado.de(jwt, request), id, req);
    }

    /** Recebe a grade de checkboxes inteira e a grava (substitui o que havia). */
    @PutMapping("/{id}/permissoes")
    @PreAuthorize("hasAuthority('ADMINISTRACAO:EDITAR')")
    public UsuarioDetalheDto salvarPermissoes(@PathVariable Long id,
                                              @Valid @RequestBody PermissoesRequest req,
                                              @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.salvarPermissoes(UsuarioLogado.de(jwt, request), id, req);
    }

    @PutMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMINISTRACAO:EDITAR')")
    public void redefinirSenha(@PathVariable Long id,
                               @Valid @RequestBody SenhaRequest req,
                               @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        service.redefinirSenha(UsuarioLogado.de(jwt, request), id, req);
    }
}
