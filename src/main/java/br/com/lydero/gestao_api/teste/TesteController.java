package br.com.lydero.gestao_api.teste;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints temporarios, so para verificar a matriz de permissoes funcionando.
 * Pode apagar esta classe quando os modulos reais existirem.
 */
@RestController
@RequestMapping("/api/v1/teste")
public class TesteController {

    @GetMapping("/administracao")
    @PreAuthorize("hasAuthority('ADMINISTRACAO:VISUALIZAR')")
    public Map<String, String> administracao() {
        return Map.of("mensagem", "Voce tem acesso a Administracao");
    }

    @GetMapping("/estoque")
    @PreAuthorize("hasAuthority('ESTOQUE:VISUALIZAR')")
    public Map<String, String> estoque() {
        return Map.of("mensagem", "Voce tem acesso ao Estoque");
    }
}
