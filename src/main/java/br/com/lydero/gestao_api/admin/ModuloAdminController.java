package br.com.lydero.gestao_api.admin;

import br.com.lydero.gestao_api.admin.dto.ModuloDto;
import br.com.lydero.gestao_api.modulo.ModuloRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/modulos")
public class ModuloAdminController {

    private final ModuloRepository moduloRepository;

    public ModuloAdminController(ModuloRepository moduloRepository) {
        this.moduloRepository = moduloRepository;
    }

    /** Modulos ativos, na ordem do menu. Serve para montar as colunas da grade de permissoes. */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRACAO:VISUALIZAR')")
    public List<ModuloDto> listar() {
        return moduloRepository.findByAtivoTrueOrderByOrdemAsc().stream()
                .map(m -> new ModuloDto(m.getId(), m.getCodigo(), m.getNome()))
                .toList();
    }
}
