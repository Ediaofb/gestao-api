package br.com.lydero.gestao_api.estoque;

import br.com.lydero.gestao_api.auditoria.AuditoriaService;
import br.com.lydero.gestao_api.common.UsuarioLogado;
import br.com.lydero.gestao_api.estoque.dto.EstoqueResumoDto;
import br.com.lydero.gestao_api.estoque.dto.KitItemDto;
import br.com.lydero.gestao_api.estoque.dto.KitItemRequest;
import br.com.lydero.gestao_api.estoque.dto.MovimentacaoDto;
import br.com.lydero.gestao_api.estoque.dto.MovimentacaoRequest;
import br.com.lydero.gestao_api.estoque.dto.ProdutoDto;
import br.com.lydero.gestao_api.estoque.dto.ProdutoRequest;
import br.com.lydero.gestao_api.usuario.Usuario;
import br.com.lydero.gestao_api.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regras do modulo Estoque.
 *
 * Resumo das regras:
 *  - PRODUTO controla estoque; SERVICO nao; KIT nao tem estoque proprio (a disponibilidade
 *    e calculada pelos itens).
 *  - A quantidade de um PRODUTO so muda por movimentacao (fica o historico).
 *  - Margem = (venda - custo) / custo * 100, ou seja, margem SOBRE O CUSTO.
 *  - Nada e apagado: o produto e inativado (preserva historico e futuras vendas).
 *  - Toda consulta e filtrada pela empresa do token.
 */
@Service
public class EstoqueService {

    private static final String MODULO_LOG = "ESTOQUE";
    private static final String ENTIDADE_LOG = "Produto";
    private static final BigDecimal CEM = new BigDecimal("100");

    static final Set<String> UNIDADES =
            Set.of("UN", "KG", "G", "L", "ML", "M", "M2", "CX", "PCT", "DZ", "H");
    static final Set<String> TIPOS =
            Set.of(Produto.TIPO_PRODUTO, Produto.TIPO_SERVICO, Produto.TIPO_KIT);
    static final Set<String> MOTIVOS_BAIXA =
            Set.of("CONSUMO_INTERNO", "PERDA", "AVARIA", "VENCIMENTO", "OUTROS");
    static final Set<String> MOTIVOS_ENTRADA =
            Set.of("REPOSICAO", "DEVOLUCAO", "AJUSTE_CONTAGEM", "OUTROS");

    private final ProdutoRepository produtoRepository;
    private final KitItemRepository kitItemRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoria;

    public EstoqueService(ProdutoRepository produtoRepository,
                          KitItemRepository kitItemRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          UsuarioRepository usuarioRepository,
                          AuditoriaService auditoria) {
        this.produtoRepository = produtoRepository;
        this.kitItemRepository = kitItemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoria = auditoria;
    }

    /** Foto dos produtos da empresa e dos itens dos kits, para calcular tudo em memoria. */
    private record Contexto(Map<Long, Produto> produtos, Map<Long, List<KitItem>> kits) {
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<ProdutoDto> listar(UsuarioLogado quem) {
        Contexto ctx = carregar(quem.empresaId());
        return ctx.produtos().values().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map(p -> paraDto(p, ctx, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoDto buscar(UsuarioLogado quem, Long id) {
        Contexto ctx = carregar(quem.empresaId());
        return paraDto(obter(ctx, id), ctx, true);
    }

    @Transactional(readOnly = true)
    public EstoqueResumoDto resumo(UsuarioLogado quem) {
        Contexto ctx = carregar(quem.empresaId());
        long total = 0;
        long zerados = 0;
        long baixos = 0;
        BigDecimal custo = BigDecimal.ZERO;
        BigDecimal venda = BigDecimal.ZERO;
        for (Produto p : ctx.produtos().values()) {
            if (!p.isAtivo()) {
                continue;
            }
            total++;
            String situacao = situacao(p, quantidadeEfetiva(p, ctx));
            if ("ZERADO".equals(situacao)) {
                zerados++;
            } else if ("BAIXO".equals(situacao)) {
                baixos++;
            }
            if (p.isProduto()) {
                custo = custo.add(p.getQuantidade().multiply(p.getValorCusto()));
                venda = venda.add(p.getQuantidade().multiply(p.getValorVenda()));
            }
        }
        return new EstoqueResumoDto(total, zerados, baixos,
                custo.setScale(2, RoundingMode.HALF_UP), venda.setScale(2, RoundingMode.HALF_UP));
    }

    @Transactional(readOnly = true)
    public List<String> categorias(UsuarioLogado quem) {
        return produtoRepository.categorias(quem.empresaId());
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoDto> movimentacoes(UsuarioLogado quem, Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .filter(p -> p.getEmpresaId().equals(quem.empresaId()))
                .orElseThrow(this::naoEncontrado);
        List<MovimentacaoEstoque> lista = movimentacaoRepository
                .findTop100ByProdutoIdAndEmpresaIdOrderByIdDesc(produto.getId(), quem.empresaId());
        Set<Long> idsUsuarios = lista.stream().map(MovimentacaoEstoque::getUsuarioId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nomes = new HashMap<>();
        for (Usuario u : usuarioRepository.findAllById(idsUsuarios)) {
            nomes.put(u.getId(), u.getNome());
        }
        return lista.stream().map(m -> new MovimentacaoDto(
                m.getId(), m.getTipo(), m.getQuantidade(), m.getQuantidadeAnterior(),
                m.getQuantidadePosterior(), m.getMotivo(), m.getObservacao(),
                m.getUsuarioId() == null ? null : nomes.get(m.getUsuarioId()), m.getCriadoEm())).toList();
    }

    // ------------------------------------------------------------------ escrita

    @Transactional
    public ProdutoDto criar(UsuarioLogado quem, ProdutoRequest req) {
        String tipo = normalizarTipo(req.tipo());
        String codigo = textoOuNulo(req.codigo());
        if (codigo != null && produtoRepository.existsByEmpresaIdAndCodigoIgnoreCase(quem.empresaId(), codigo)) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um item com o codigo " + codigo);
        }

        Produto p = new Produto();
        p.setEmpresaId(quem.empresaId());
        p.setTipo(tipo);
        p.setCodigo(codigo);
        preencherComuns(p, req);

        Contexto ctx = carregar(quem.empresaId());
        List<KitItemRequest> itensReq = List.of();
        if (p.isKit()) {
            itensReq = validarItensKit(req.itens(), ctx, Set.of());
        }
        definirPrecos(p, req, p.isKit() ? custoDosItens(itensReq, ctx) : null);

        BigDecimal inicial = BigDecimal.ZERO;
        if (p.isProduto()) {
            inicial = escala3(req.quantidade() == null ? BigDecimal.ZERO : req.quantidade());
            p.setQuantidadeMinima(escala3(req.quantidadeMinima() == null ? BigDecimal.ZERO : req.quantidadeMinima()));
        }
        p.setQuantidade(BigDecimal.ZERO);
        produtoRepository.save(p);

        if (p.isKit()) {
            for (KitItemRequest ir : itensReq) {
                KitItem ki = new KitItem();
                ki.setKitId(p.getId());
                ki.setItemId(ir.itemId());
                ki.setQuantidade(escala3(ir.quantidade()));
                kitItemRepository.save(ki);
            }
        }
        if (inicial.signum() > 0) {
            p.setQuantidade(inicial);
            registrarMovimento(quem, p, MovimentacaoEstoque.ENTRADA, inicial,
                    BigDecimal.ZERO, inicial, "ESTOQUE_INICIAL", null);
        }

        auditoria.registrar(quem, "CRIAR", MODULO_LOG, ENTIDADE_LOG, p.getId(),
                "Item criado: " + p.getNome() + " (" + p.getTipo() + ")");
        return buscarAposEscrita(quem, p.getId());
    }

    @Transactional
    public ProdutoDto atualizar(UsuarioLogado quem, Long id, ProdutoRequest req) {
        Produto p = produtoRepository.findById(id)
                .filter(x -> x.getEmpresaId().equals(quem.empresaId()))
                .orElseThrow(this::naoEncontrado);
        if (!p.getTipo().equals(normalizarTipo(req.tipo()))) {
            throw erro(HttpStatus.BAD_REQUEST, "Nao e possivel mudar o tipo do item. Crie um novo cadastro.");
        }
        String codigo = textoOuNulo(req.codigo());
        if (codigo != null && produtoRepository
                .existsByEmpresaIdAndCodigoIgnoreCaseAndIdNot(quem.empresaId(), codigo, p.getId())) {
            throw erro(HttpStatus.CONFLICT, "Ja existe um item com o codigo " + codigo);
        }
        p.setCodigo(codigo);
        preencherComuns(p, req);

        Contexto ctx = carregar(quem.empresaId());
        BigDecimal custoKit = null;
        if (p.isKit()) {
            List<KitItem> atuais = ctx.kits().getOrDefault(p.getId(), List.of());
            Set<Long> jaNoKit = atuais.stream().map(KitItem::getItemId).collect(Collectors.toSet());
            List<KitItemRequest> novos = validarItensKit(req.itens(), ctx, jaNoKit);
            custoKit = custoDosItens(novos, ctx);
            sincronizarItensKit(p, atuais, novos);
        } else if (p.isProduto()) {
            p.setQuantidadeMinima(escala3(req.quantidadeMinima() == null ? BigDecimal.ZERO : req.quantidadeMinima()));
        }
        definirPrecos(p, req, custoKit);

        auditoria.registrar(quem, "EDITAR", MODULO_LOG, ENTIDADE_LOG, p.getId(),
                "Item editado: " + p.getNome());
        return buscarAposEscrita(quem, p.getId());
    }

    @Transactional
    public void inativar(UsuarioLogado quem, Long id) {
        Produto p = produtoRepository.findById(id)
                .filter(x -> x.getEmpresaId().equals(quem.empresaId()))
                .orElseThrow(this::naoEncontrado);
        if (!p.isAtivo()) {
            return;
        }
        if (!p.isKit()) {
            List<Long> kitIds = kitItemRepository.findByItemId(p.getId()).stream().map(KitItem::getKitId).toList();
            List<String> kitsAtivos = produtoRepository.findAllById(kitIds).stream()
                    .filter(k -> k.isAtivo() && k.getEmpresaId().equals(quem.empresaId()))
                    .map(Produto::getNome).sorted().toList();
            if (!kitsAtivos.isEmpty()) {
                throw erro(HttpStatus.CONFLICT, "Este item faz parte de kits ativos: "
                        + String.join(", ", kitsAtivos) + ". Retire-o dos kits ou inative os kits antes.");
            }
        }
        p.setAtivo(false);
        auditoria.registrar(quem, "INATIVAR", MODULO_LOG, ENTIDADE_LOG, p.getId(),
                "Item inativado: " + p.getNome());
    }

    @Transactional
    public ProdutoDto reativar(UsuarioLogado quem, Long id) {
        Produto p = produtoRepository.findById(id)
                .filter(x -> x.getEmpresaId().equals(quem.empresaId()))
                .orElseThrow(this::naoEncontrado);
        if (!p.isAtivo()) {
            p.setAtivo(true);
            auditoria.registrar(quem, "REATIVAR", MODULO_LOG, ENTIDADE_LOG, p.getId(),
                    "Item reativado: " + p.getNome());
        }
        return buscarAposEscrita(quem, p.getId());
    }

    /** Entrada ou baixa interna. A linha do produto fica travada ate o fim da transacao. */
    @Transactional
    public ProdutoDto movimentar(UsuarioLogado quem, Long id, MovimentacaoRequest req) {
        String tipo = req.tipo() == null ? "" : req.tipo().trim().toUpperCase(Locale.ROOT);
        boolean entrada = MovimentacaoEstoque.ENTRADA.equals(tipo);
        if (!entrada && !MovimentacaoEstoque.BAIXA_INTERNA.equals(tipo)) {
            throw erro(HttpStatus.BAD_REQUEST, "Tipo de movimentacao invalido (use ENTRADA ou BAIXA_INTERNA)");
        }
        String motivo = req.motivo().trim().toUpperCase(Locale.ROOT);
        if (!(entrada ? MOTIVOS_ENTRADA : MOTIVOS_BAIXA).contains(motivo)) {
            throw erro(HttpStatus.BAD_REQUEST, "Motivo invalido para " + (entrada ? "entrada" : "baixa interna"));
        }
        BigDecimal qtd = escala3(req.quantidade());
        if (qtd.signum() <= 0) {
            throw erro(HttpStatus.BAD_REQUEST, "A quantidade deve ser maior que zero");
        }

        Produto p = produtoRepository.buscarParaAtualizar(id, quem.empresaId()).orElseThrow(this::naoEncontrado);
        if (!p.isProduto()) {
            throw erro(HttpStatus.BAD_REQUEST, "Apenas produtos controlam estoque (servicos e kits nao).");
        }
        if (!p.isAtivo()) {
            throw erro(HttpStatus.CONFLICT, "Item inativo. Reative-o para movimentar o estoque.");
        }
        BigDecimal anterior = p.getQuantidade();
        BigDecimal posterior = entrada ? anterior.add(qtd) : anterior.subtract(qtd);
        if (posterior.signum() < 0) {
            throw erro(HttpStatus.CONFLICT, "Estoque insuficiente. Disponivel: "
                    + anterior.stripTrailingZeros().toPlainString() + " " + p.getUnidade());
        }
        p.setQuantidade(posterior);
        registrarMovimento(quem, p, tipo, qtd, anterior, posterior, motivo, textoOuNulo(req.observacao()));

        auditoria.registrar(quem, entrada ? "ENTRADA_ESTOQUE" : "BAIXA_ESTOQUE", MODULO_LOG, ENTIDADE_LOG, p.getId(),
                (entrada ? "Entrada de " : "Baixa de ") + qtd.stripTrailingZeros().toPlainString() + " "
                        + p.getUnidade() + " em " + p.getNome() + " (" + motivo + ")");
        return buscarAposEscrita(quem, p.getId());
    }

    // ------------------------------------------------------------------ apoio: regras

    private void preencherComuns(Produto p, ProdutoRequest req) {
        p.setNome(req.nome().trim());
        p.setCategoria(textoOuNulo(req.categoria()));
        p.setTamanho(textoOuNulo(req.tamanho()));
        p.setCor(textoOuNulo(req.cor()));
        p.setObservacoes(textoOuNulo(req.observacoes()));
        String un = req.unidade() == null || req.unidade().isBlank()
                ? "UN" : req.unidade().trim().toUpperCase(Locale.ROOT);
        if (!UNIDADES.contains(un)) {
            throw erro(HttpStatus.BAD_REQUEST, "Unidade de medida invalida: " + un);
        }
        p.setUnidade(un);
    }

    /**
     * Custo: informado (PRODUTO/SERVICO) ou calculado (KIT: soma dos itens, fornecido em custoKit).
     * Venda: valorVenda informado OU custo + margem. Um dos dois e obrigatorio.
     */
    private void definirPrecos(Produto p, ProdutoRequest req, BigDecimal custoKit) {
        BigDecimal custo = p.isKit()
                ? custoKit
                : (req.valorCusto() == null ? BigDecimal.ZERO : req.valorCusto());
        custo = custo.setScale(2, RoundingMode.HALF_UP);
        BigDecimal venda;
        if (req.valorVenda() != null) {
            venda = req.valorVenda().setScale(2, RoundingMode.HALF_UP);
        } else if (req.margem() != null) {
            venda = custo.multiply(BigDecimal.ONE.add(req.margem().divide(CEM, 10, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o valor de venda ou a margem");
        }
        // Kit nao guarda custo proprio (e dinamico), mas a coluna e obrigatoria: guarda o ultimo calculado.
        p.setValorCusto(custo);
        p.setValorVenda(venda);
    }

    private List<KitItemRequest> validarItensKit(List<KitItemRequest> itens, Contexto ctx, Set<Long> jaNoKit) {
        if (itens == null || itens.isEmpty()) {
            throw erro(HttpStatus.BAD_REQUEST, "Um kit precisa ter pelo menos um item");
        }
        Set<Long> vistos = new HashSet<>();
        for (KitItemRequest ir : itens) {
            if (!vistos.add(ir.itemId())) {
                throw erro(HttpStatus.BAD_REQUEST, "O mesmo item aparece mais de uma vez no kit");
            }
            Produto item = ctx.produtos().get(ir.itemId());
            if (item == null) {
                throw erro(HttpStatus.BAD_REQUEST, "Item do kit nao encontrado");
            }
            if (item.isKit()) {
                throw erro(HttpStatus.BAD_REQUEST, "Um kit nao pode conter outro kit (" + item.getNome() + ")");
            }
            if (!item.isAtivo() && !jaNoKit.contains(item.getId())) {
                throw erro(HttpStatus.BAD_REQUEST, "O item " + item.getNome() + " esta inativo");
            }
            if (escala3(ir.quantidade()).signum() <= 0) {
                throw erro(HttpStatus.BAD_REQUEST, "Quantidade invalida para " + item.getNome());
            }
        }
        return itens;
    }

    private BigDecimal custoDosItens(List<KitItemRequest> itens, Contexto ctx) {
        BigDecimal total = BigDecimal.ZERO;
        for (KitItemRequest ir : itens) {
            total = total.add(ctx.produtos().get(ir.itemId()).getValorCusto().multiply(escala3(ir.quantidade())));
        }
        return total;
    }

    /** Atualiza os itens do kit no proprio lugar (evita a ordem insert-antes-de-delete do Hibernate). */
    private void sincronizarItensKit(Produto kit, List<KitItem> atuais, List<KitItemRequest> novos) {
        Map<Long, KitItem> porItem = new HashMap<>();
        for (KitItem ki : atuais) {
            porItem.put(ki.getItemId(), ki);
        }
        Set<Long> manter = new HashSet<>();
        for (KitItemRequest ir : novos) {
            manter.add(ir.itemId());
            KitItem existente = porItem.get(ir.itemId());
            if (existente != null) {
                existente.setQuantidade(escala3(ir.quantidade()));
            } else {
                KitItem ki = new KitItem();
                ki.setKitId(kit.getId());
                ki.setItemId(ir.itemId());
                ki.setQuantidade(escala3(ir.quantidade()));
                kitItemRepository.save(ki);
            }
        }
        List<KitItem> remover = atuais.stream().filter(ki -> !manter.contains(ki.getItemId())).toList();
        kitItemRepository.deleteAll(remover);
    }

    private void registrarMovimento(UsuarioLogado quem, Produto p, String tipo, BigDecimal qtd,
                                    BigDecimal anterior, BigDecimal posterior, String motivo, String obs) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setEmpresaId(quem.empresaId());
        m.setProdutoId(p.getId());
        m.setTipo(tipo);
        m.setQuantidade(qtd);
        m.setQuantidadeAnterior(anterior);
        m.setQuantidadePosterior(posterior);
        m.setMotivo(motivo);
        m.setObservacao(obs);
        m.setUsuarioId(quem.id());
        m.setCriadoEm(LocalDateTime.now(ZoneOffset.UTC));
        movimentacaoRepository.save(m);
    }

    // ------------------------------------------------------------------ apoio: calculo e conversao

    private Contexto carregar(Long empresaId) {
        Map<Long, Produto> produtos = new HashMap<>();
        for (Produto p : produtoRepository.findByEmpresaIdOrderByNomeAsc(empresaId)) {
            produtos.put(p.getId(), p);
        }
        List<Long> kitIds = produtos.values().stream().filter(Produto::isKit).map(Produto::getId).toList();
        Map<Long, List<KitItem>> kits = new HashMap<>();
        if (!kitIds.isEmpty()) {
            for (KitItem ki : kitItemRepository.findByKitIdIn(kitIds)) {
                kits.computeIfAbsent(ki.getKitId(), k -> new ArrayList<>()).add(ki);
            }
        }
        return new Contexto(produtos, kits);
    }

    private Produto obter(Contexto ctx, Long id) {
        Produto p = ctx.produtos().get(id);
        if (p == null) {
            throw naoEncontrado();
        }
        return p;
    }

    /** PRODUTO = estoque; KIT = quantos kits da para montar (null se nenhum item controla estoque); SERVICO = null. */
    private BigDecimal quantidadeEfetiva(Produto p, Contexto ctx) {
        if (p.isProduto()) {
            return p.getQuantidade();
        }
        if (!p.isKit()) {
            return null;
        }
        BigDecimal menor = null;
        for (KitItem ki : ctx.kits().getOrDefault(p.getId(), List.of())) {
            Produto item = ctx.produtos().get(ki.getItemId());
            if (item == null || !item.isProduto()) {
                continue;
            }
            BigDecimal montaveis = item.getQuantidade().divide(ki.getQuantidade(), 0, RoundingMode.FLOOR);
            if (menor == null || montaveis.compareTo(menor) < 0) {
                menor = montaveis;
            }
        }
        return menor;
    }

    private BigDecimal custoEfetivo(Produto p, Contexto ctx) {
        if (!p.isKit()) {
            return p.getValorCusto();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (KitItem ki : ctx.kits().getOrDefault(p.getId(), List.of())) {
            Produto item = ctx.produtos().get(ki.getItemId());
            if (item != null) {
                total = total.add(item.getValorCusto().multiply(ki.getQuantidade()));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String situacao(Produto p, BigDecimal qtd) {
        if (qtd == null) {
            return "SEM_CONTROLE";
        }
        if (qtd.signum() <= 0) {
            return "ZERADO";
        }
        if (p.getQuantidadeMinima().signum() > 0 && qtd.compareTo(p.getQuantidadeMinima()) <= 0) {
            return "BAIXO";
        }
        return "OK";
    }

    private BigDecimal margem(BigDecimal custo, BigDecimal venda) {
        if (custo == null || custo.signum() <= 0) {
            return null;
        }
        return venda.subtract(custo).multiply(CEM).divide(custo, 2, RoundingMode.HALF_UP);
    }

    private ProdutoDto paraDto(Produto p, Contexto ctx, boolean comItens) {
        BigDecimal qtd = quantidadeEfetiva(p, ctx);
        BigDecimal custo = custoEfetivo(p, ctx);
        List<KitItemDto> itens = List.of();
        if (comItens && p.isKit()) {
            itens = ctx.kits().getOrDefault(p.getId(), List.of()).stream().map(ki -> {
                Produto item = ctx.produtos().get(ki.getItemId());
                return new KitItemDto(item.getId(), item.getNome(), item.getCodigo(), item.getTipo(),
                        item.getUnidade(), ki.getQuantidade(), item.getValorCusto(), item.getValorVenda(),
                        item.isProduto() ? item.getQuantidade() : null, item.isAtivo());
            }).sorted((a, b) -> a.nome().compareToIgnoreCase(b.nome())).toList();
        }
        return new ProdutoDto(p.getId(), p.getTipo(), p.getCodigo(), p.getNome(), p.getCategoria(),
                p.getUnidade(), custo, p.getValorVenda(), margem(custo, p.getValorVenda()), qtd,
                p.getQuantidadeMinima(), p.getTamanho(), p.getCor(), p.getObservacoes(), p.isAtivo(),
                situacao(p, qtd), itens);
    }

    /** Recarrega depois de escrever, para devolver o que ficou de fato (com kit e margem recalculados). */
    private ProdutoDto buscarAposEscrita(UsuarioLogado quem, Long id) {
        produtoRepository.flush();
        Contexto ctx = carregar(quem.empresaId());
        return paraDto(obter(ctx, id), ctx, true);
    }

    // ------------------------------------------------------------------ utilitarios

    private String normalizarTipo(String tipo) {
        String t = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(t)) {
            throw erro(HttpStatus.BAD_REQUEST, "Tipo invalido (use PRODUTO, SERVICO ou KIT)");
        }
        return t;
    }

    private static BigDecimal escala3(BigDecimal valor) {
        return valor.setScale(3, RoundingMode.HALF_UP);
    }

    private static String textoOuNulo(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim();
        return t.isEmpty() ? null : t;
    }

    private ResponseStatusException naoEncontrado() {
        return erro(HttpStatus.NOT_FOUND, "Item nao encontrado");
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }
}
