package br.com.lydero.gestao_api.cliente;

import br.com.lydero.gestao_api.cliente.dto.ClienteDto;
import br.com.lydero.gestao_api.cliente.dto.ClienteRequest;
import br.com.lydero.gestao_api.cliente.dto.ClienteResumoDto;
import br.com.lydero.gestao_api.common.UsuarioLogado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Modulo Clientes. Ver = VISUALIZAR; cadastrar = CRIAR; editar/reativar = EDITAR; inativar = EXCLUIR. */
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('CLIENTES:VISUALIZAR')")
    public ClienteResumoDto resumo(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.resumo(UsuarioLogado.de(jwt, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTES:VISUALIZAR')")
    public List<ClienteDto> listar(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.listar(UsuarioLogado.de(jwt, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES:VISUALIZAR')")
    public ClienteDto buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.buscar(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CLIENTES:CRIAR')")
    public ClienteDto criar(@Valid @RequestBody ClienteRequest req,
                            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.criar(UsuarioLogado.de(jwt, request), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTES:EDITAR')")
    public ClienteDto atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req,
                                @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.atualizar(UsuarioLogado.de(jwt, request), id, req);
    }

    /** Nao apaga: apenas inativa. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('CLIENTES:EXCLUIR')")
    public void inativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        service.inativar(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('CLIENTES:EDITAR')")
    public ClienteDto reativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.reativar(UsuarioLogado.de(jwt, request), id);
    }
}
