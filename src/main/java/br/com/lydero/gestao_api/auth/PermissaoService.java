package br.com.lydero.gestao_api.auth;

import br.com.lydero.gestao_api.modulo.Acao;
import br.com.lydero.gestao_api.modulo.Modulo;
import br.com.lydero.gestao_api.modulo.ModuloRepository;
import br.com.lydero.gestao_api.usuario.Usuario;
import br.com.lydero.gestao_api.usuario.UsuarioPermissao;
import br.com.lydero.gestao_api.usuario.UsuarioPermissaoRepository;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Monta a lista de permissoes do usuario no formato "MODULO:ACAO" (ex.: "ESTOQUE:CRIAR").
 * Administrador recebe todas as acoes de todos os modulos ativos; usuario comum recebe
 * somente o que estiver marcado na matriz (tabela usuario_permissao).
 */
@Service
public class PermissaoService {

    private final ModuloRepository moduloRepository;
    private final UsuarioPermissaoRepository permissaoRepository;

    public PermissaoService(ModuloRepository moduloRepository,
                            UsuarioPermissaoRepository permissaoRepository) {
        this.moduloRepository = moduloRepository;
        this.permissaoRepository = permissaoRepository;
    }

    public List<String> permissoesDe(Usuario usuario) {
        List<Modulo> modulos = moduloRepository.findByAtivoTrueOrderByOrdemAsc();
        Set<String> permissoes = new LinkedHashSet<>();

        if (usuario.isAdmin()) {
            permissoes.add("ROLE_ADMIN");
            for (Modulo modulo : modulos) {
                for (Acao acao : Acao.values()) {
                    permissoes.add(chave(modulo, acao));
                }
            }
            return List.copyOf(permissoes);
        }

        permissoes.add("ROLE_USER");
        Map<Long, Modulo> modulosPorId = new HashMap<>();
        for (Modulo modulo : modulos) {
            modulosPorId.put(modulo.getId(), modulo);
        }
        for (UsuarioPermissao p : permissaoRepository.findByIdUsuarioId(usuario.getId())) {
            Modulo modulo = modulosPorId.get(p.getId().getModuloId());
            if (modulo == null) {
                continue; // modulo desativado ou inexistente
            }
            if (p.isPodeVisualizar()) permissoes.add(chave(modulo, Acao.VISUALIZAR));
            if (p.isPodeCriar()) permissoes.add(chave(modulo, Acao.CRIAR));
            if (p.isPodeEditar()) permissoes.add(chave(modulo, Acao.EDITAR));
            if (p.isPodeExcluir()) permissoes.add(chave(modulo, Acao.EXCLUIR));
        }
        return List.copyOf(permissoes);
    }

    private static String chave(Modulo modulo, Acao acao) {
        return modulo.getCodigo() + ":" + acao.name();
    }
}
