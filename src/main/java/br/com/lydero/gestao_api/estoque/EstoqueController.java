package br.com.lydero.gestao_api.estoque;

import br.com.lydero.gestao_api.common.UsuarioLogado;
import br.com.lydero.gestao_api.estoque.dto.EstoqueResumoDto;
import br.com.lydero.gestao_api.estoque.dto.MovimentacaoDto;
import br.com.lydero.gestao_api.estoque.dto.MovimentacaoRequest;
import br.com.lydero.gestao_api.estoque.dto.ProdutoDto;
import br.com.lydero.gestao_api.estoque.dto.ProdutoRequest;
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

/**
 * Modulo Estoque. Permissoes: ver = VISUALIZAR; cadastrar = CRIAR; editar, reativar e
 * movimentar (entrada/baixa) = EDITAR; inativar = EXCLUIR.
 */
@RestController
@RequestMapping("/api/v1/estoque")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public EstoqueResumoDto resumo(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.resumo(UsuarioLogado.de(jwt, request));
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public List<String> categorias(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.categorias(UsuarioLogado.de(jwt, request));
    }

    @GetMapping("/produtos")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public List<ProdutoDto> listar(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.listar(UsuarioLogado.de(jwt, request));
    }

    @GetMapping("/produtos/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public ProdutoDto buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.buscar(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping("/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ESTOQUE:CRIAR')")
    public ProdutoDto criar(@Valid @RequestBody ProdutoRequest req,
                            @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.criar(UsuarioLogado.de(jwt, request), req);
    }

    @PutMapping("/produtos/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE:EDITAR')")
    public ProdutoDto atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest req,
                                @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.atualizar(UsuarioLogado.de(jwt, request), id, req);
    }

    /** Nao apaga: apenas inativa o item (o historico fica preservado). */
    @DeleteMapping("/produtos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ESTOQUE:EXCLUIR')")
    public void inativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        service.inativar(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping("/produtos/{id}/reativar")
    @PreAuthorize("hasAuthority('ESTOQUE:EDITAR')")
    public ProdutoDto reativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.reativar(UsuarioLogado.de(jwt, request), id);
    }

    @GetMapping("/produtos/{id}/movimentacoes")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public List<MovimentacaoDto> movimentacoes(@PathVariable Long id,
                                               @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.movimentacoes(UsuarioLogado.de(jwt, request), id);
    }

    @PostMapping("/produtos/{id}/movimentacoes")
    @PreAuthorize("hasAuthority('ESTOQUE:EDITAR')")
    public ProdutoDto movimentar(@PathVariable Long id, @Valid @RequestBody MovimentacaoRequest req,
                                 @AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        return service.movimentar(UsuarioLogado.de(jwt, request), id, req);
    }
}
