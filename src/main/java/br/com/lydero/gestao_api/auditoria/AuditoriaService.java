package br.com.lydero.gestao_api.auditoria;

import br.com.lydero.gestao_api.common.UsuarioLogado;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository repository;

    public AuditoriaService(LogAuditoriaRepository repository) {
        this.repository = repository;
    }

    /** Roda dentro da transacao de quem chama: se a operacao falhar, o log tambem nao e gravado. */
    public void registrar(UsuarioLogado quem, String acao, String modulo,
                          String entidade, Object entidadeId, String descricao) {
        LogAuditoria log = new LogAuditoria();
        log.setEmpresaId(quem.empresaId());
        log.setUsuarioId(quem.id());
        log.setAcao(acao);
        log.setModulo(modulo);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId == null ? null : String.valueOf(entidadeId));
        log.setDescricao(descricao == null ? null : cortar(descricao, 255));
        log.setIp(quem.ip());
        repository.save(log);
    }

    private static String cortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
