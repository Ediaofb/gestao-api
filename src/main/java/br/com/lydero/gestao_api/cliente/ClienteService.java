package br.com.lydero.gestao_api.cliente;

import br.com.lydero.gestao_api.auditoria.AuditoriaService;
import br.com.lydero.gestao_api.cliente.dto.ClienteDto;
import br.com.lydero.gestao_api.cliente.dto.ClienteRequest;
import br.com.lydero.gestao_api.cliente.dto.ClienteResumoDto;
import br.com.lydero.gestao_api.common.UsuarioLogado;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regras do cadastro de clientes. Toda consulta e filtrada pela empresa do token.
 * Nada e apagado: o cliente e inativado (as vendas futuras continuam apontando para ele).
 */
@Service
public class ClienteService {

    private static final String MODULO_LOG = "CLIENTES";
    private static final String ENTIDADE_LOG = "Cliente";

    private final ClienteRepository repository;
    private final AuditoriaService auditoria;
    private final ZoneId fuso;

    public ClienteService(ClienteRepository repository, AuditoriaService auditoria,
                          @Value("${app.timezone:America/Sao_Paulo}") String fuso) {
        this.repository = repository;
        this.auditoria = auditoria;
        this.fuso = ZoneId.of(fuso);
    }

    @Transactional(readOnly = true)
    public List<ClienteDto> listar(UsuarioLogado quem) {
        return repository.findByEmpresaIdOrderByNomeAsc(quem.empresaId()).stream().map(this::paraDto).toList();
    }

    @Transactional(readOnly = true)
    public ClienteDto buscar(UsuarioLogado quem, Long id) {
        return paraDto(obter(quem, id));
    }

    @Transactional(readOnly = true)
    public ClienteResumoDto resumo(UsuarioLogado quem) {
        LocalDate hoje = LocalDate.now(fuso);
        long ativos = 0;
        long inativos = 0;
        long mes = 0;
        long hojeQtd = 0;
        for (Cliente c : repository.findByEmpresaIdOrderByNomeAsc(quem.empresaId())) {
            if (!c.isAtivo()) {
                inativos++;
                continue;
            }
            ativos++;
            LocalDate nasc = c.getDataNascimento();
            if (nasc != null && nasc.getMonthValue() == hoje.getMonthValue()) {
                mes++;
                if (nasc.getDayOfMonth() == hoje.getDayOfMonth()) {
                    hojeQtd++;
                }
            }
        }
        return new ClienteResumoDto(ativos, inativos, mes, hojeQtd);
    }

    @Transactional
    public ClienteDto criar(UsuarioLogado quem, ClienteRequest req) {
        Cliente c = new Cliente();
        c.setEmpresaId(quem.empresaId());
        preencher(c, req);
        if (c.getDocumento() != null && repository.existsByEmpresaIdAndDocumento(quem.empresaId(), c.getDocumento())) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um cliente com este CPF/CNPJ");
        }
        repository.save(c);
        auditoria.registrar(quem, "CRIAR", MODULO_LOG, ENTIDADE_LOG, c.getId(), "Cliente criado: " + c.getNome());
        return paraDto(c);
    }

    @Transactional
    public ClienteDto atualizar(UsuarioLogado quem, Long id, ClienteRequest req) {
        Cliente c = obter(quem, id);
        preencher(c, req);
        if (c.getDocumento() != null
                && repository.existsByEmpresaIdAndDocumentoAndIdNot(quem.empresaId(), c.getDocumento(), c.getId())) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um cliente com este CPF/CNPJ");
        }
        auditoria.registrar(quem, "EDITAR", MODULO_LOG, ENTIDADE_LOG, c.getId(), "Cliente editado: " + c.getNome());
        return paraDto(c);
    }

    @Transactional
    public void inativar(UsuarioLogado quem, Long id) {
        Cliente c = obter(quem, id);
        if (c.isAtivo()) {
            c.setAtivo(false);
            auditoria.registrar(quem, "INATIVAR", MODULO_LOG, ENTIDADE_LOG, c.getId(), "Cliente inativado: " + c.getNome());
        }
    }

    @Transactional
    public ClienteDto reativar(UsuarioLogado quem, Long id) {
        Cliente c = obter(quem, id);
        if (!c.isAtivo()) {
            c.setAtivo(true);
            auditoria.registrar(quem, "REATIVAR", MODULO_LOG, ENTIDADE_LOG, c.getId(), "Cliente reativado: " + c.getNome());
        }
        return paraDto(c);
    }

    // ------------------------------------------------------------------ apoio

    private Cliente obter(UsuarioLogado quem, Long id) {
        return repository.findByIdAndEmpresaId(id, quem.empresaId())
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
    }

    private void preencher(Cliente c, ClienteRequest req) {
        String tipo = req.tipoPessoa() == null || req.tipoPessoa().isBlank()
                ? Cliente.FISICA : req.tipoPessoa().trim().toUpperCase(Locale.ROOT);
        if (!Cliente.FISICA.equals(tipo) && !Cliente.JURIDICA.equals(tipo)) {
            throw erro(HttpStatus.BAD_REQUEST, "Tipo de pessoa invalido (use FISICA ou JURIDICA)");
        }
        c.setTipoPessoa(tipo);
        c.setNome(req.nome().trim());
        c.setDocumento(validarDocumento(tipo, req.documento()));
        c.setDataNascimento(validarNascimento(req.dataNascimento()));
        c.setTelefone(texto(req.telefone()));
        c.setTelefone2(texto(req.telefone2()));
        c.setEmail(texto(req.email()));
        c.setCep(texto(req.cep()));
        c.setLogradouro(texto(req.logradouro()));
        c.setNumero(texto(req.numero()));
        c.setComplemento(texto(req.complemento()));
        c.setBairro(texto(req.bairro()));
        c.setCidade(texto(req.cidade()));
        String uf = texto(req.uf());
        if (uf != null && !uf.matches("[A-Za-z]{2}")) {
            throw erro(HttpStatus.BAD_REQUEST, "UF deve ter 2 letras");
        }
        c.setUf(uf == null ? null : uf.toUpperCase(Locale.ROOT));
        String url = texto(req.localizacaoUrl());
        if (url != null && !url.matches("(?i)^https?://\\S+$")) {
            throw erro(HttpStatus.BAD_REQUEST, "O link da localizacao deve comecar com http:// ou https://");
        }
        c.setLocalizacaoUrl(url);
        c.setInstagram(texto(req.instagram()));
        c.setFacebook(texto(req.facebook()));
        c.setOutrasRedes(texto(req.outrasRedes()));
        c.setObservacoes(texto(req.observacoes()));
    }

    private LocalDate validarNascimento(LocalDate data) {
        if (data != null && data.isAfter(LocalDate.now(fuso))) {
            throw erro(HttpStatus.BAD_REQUEST, "A data de nascimento nao pode ser no futuro");
        }
        return data;
    }

    /** Guarda so os digitos e confere o tamanho e os digitos verificadores. */
    private String validarDocumento(String tipo, String bruto) {
        String digitos = bruto == null ? "" : bruto.replaceAll("\\D", "");
        if (digitos.isEmpty()) {
            return null;
        }
        boolean fisica = Cliente.FISICA.equals(tipo);
        boolean valido = fisica ? cpfValido(digitos) : cnpjValido(digitos);
        if (!valido) {
            throw erro(HttpStatus.BAD_REQUEST, fisica ? "CPF invalido" : "CNPJ invalido");
        }
        return digitos;
    }

    static boolean cpfValido(String d) {
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digito(d, 9, 10) == d.charAt(9) - '0' && digito(d, 10, 11) == d.charAt(10) - '0';
    }

    private static int digito(String d, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    static boolean cnpjValido(String d) {
        if (d.length() != 14 || d.chars().distinct().count() == 1) {
            return false;
        }
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return dvCnpj(d, p1) == d.charAt(12) - '0' && dvCnpj(d, p2) == d.charAt(13) - '0';
    }

    private static int dvCnpj(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (d.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static String texto(String t) {
        if (t == null) {
            return null;
        }
        String s = t.trim();
        return s.isEmpty() ? null : s;
    }

    private ClienteDto paraDto(Cliente c) {
        return new ClienteDto(c.getId(), c.getTipoPessoa(), c.getNome(), c.getDocumento(), c.getDataNascimento(),
                c.getTelefone(), c.getTelefone2(), c.getEmail(), c.getCep(), c.getLogradouro(), c.getNumero(),
                c.getComplemento(), c.getBairro(), c.getCidade(), c.getUf(), c.getLocalizacaoUrl(),
                c.getInstagram(), c.getFacebook(), c.getOutrasRedes(), c.getObservacoes(), c.isAtivo());
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
