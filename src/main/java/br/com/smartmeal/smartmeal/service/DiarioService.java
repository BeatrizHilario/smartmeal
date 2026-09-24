package br.com.smartmeal.smartmeal.service;

import br.com.smartmeal.smartmeal.model.CacheAlimento;
import br.com.smartmeal.smartmeal.model.TabelaNutricional;
import br.com.smartmeal.smartmeal.model.nosql.RegistroDiario;
import br.com.smartmeal.smartmeal.repository.CacheAlimentoRepository;
import br.com.smartmeal.smartmeal.repository.TabelaNutricionalRepository;
import br.com.smartmeal.smartmeal.repository.nosql.RegistroDiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiarioService {

    @Autowired
    private RegistroDiarioRepository repository;

    @Autowired
    private ArtificialIntelligenceService aiService;

    @Autowired
    private TabelaNutricionalRepository tabelaNutricionalRepository;

    @Autowired
    private CacheAlimentoRepository cacheAlimentoRepository;

    public void registrarRefeicaoSugerida(String usuarioId, String tipoRefeicao, String descricao, Integer calorias) {
        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setData(LocalDate.now());
        registro.setTipoRefeicao(tipoRefeicao);
        registro.setDescricao(descricao);
        registro.setCalorias(calorias);
        registro.setSugestaoIa(true);

        repository.save(registro);
    }

    public void registrarRefeicaoManual(String usuarioId, String tipoRefeicao, String descricao) {
        int caloriasCalculadas = calcularCaloriasHibrido(descricao);

        RegistroDiario registro = new RegistroDiario();
        registro.setUsuarioId(usuarioId);
        registro.setData(LocalDate.now());
        registro.setTipoRefeicao(tipoRefeicao);
        registro.setDescricao(descricao);
        registro.setCalorias(caloriasCalculadas);
        registro.setSugestaoIa(false);

        repository.save(registro);
    }

    private int calcularCaloriasHibrido(String descricaoCompleta) {
        if (descricaoCompleta == null || descricaoCompleta.isBlank()) {
            return 0;
        }

        // 1. CHECAGEM NO CACHE: prato completo já calculado anteriormente
        String chaveDescricao = descricaoCompleta.trim().toLowerCase();
        Optional<CacheAlimento> cacheRefeicao = cacheAlimentoRepository.findByNomeIgnoreCase(chaveDescricao);
        if (cacheRefeicao.isPresent() && cacheRefeicao.get().getCaloriasPor100g() != null) {
            int calorias = cacheRefeicao.get().getCaloriasPor100g().intValue();
            System.out.println("[CACHE COMPLETO] Encontrado: " + chaveDescricao + " -> " + calorias + " kcal");
            return calorias;
        }

        String[] itens = descricaoCompleta.split(",");
        double totalCalorias = 0.0;
        List<String> itensNaoEncontrados = new ArrayList<>();

        Pattern pattern = Pattern.compile("^\\s*([0-9]+(?:[.,][0-9]+)?)\\s*(g|ml|colher\\(es\\) de sopa|unidade\\(s\\)|fatia\\(s\\)|concha\\(s\\))\\s*(?:de\\s+)?(.+)$", Pattern.CASE_INSENSITIVE);

        for (String itemStr : itens) {
            String itemLimpo = itemStr.trim();
            Matcher matcher = pattern.matcher(itemLimpo);

            if (matcher.matches()) {
                double quantidade = Double.parseDouble(matcher.group(1).replace(",", "."));
                String unidade = matcher.group(2).toLowerCase();
                String nomeAlimento = matcher.group(3).trim();

                // Checagem no cache para o item avulso
                Optional<CacheAlimento> cacheItem = cacheAlimentoRepository.findByNomeIgnoreCase(itemLimpo.toLowerCase());
                if (cacheItem.isPresent() && cacheItem.get().getCaloriasPor100g() != null) {
                    double calCache = cacheItem.get().getCaloriasPor100g().doubleValue();
                    System.out.println("[CACHE ITEM] " + itemLimpo + " -> " + calCache + " kcal");
                    totalCalorias += calCache;
                    continue;
                }

                double pesoEmGramas = converterParaGramas(quantidade, unidade);
                Optional<TabelaNutricional> alimentoOpt = tabelaNutricionalRepository.buscarPorNomeAproximado(nomeAlimento);

                if (alimentoOpt.isPresent() && alimentoOpt.get().getCalorias100g() != null) {
                    double cal100g = alimentoOpt.get().getCalorias100g().doubleValue();
                    double calItem = (pesoEmGramas * cal100g) / 100.0;
                    totalCalorias += calItem;
                    System.out.println("[TACO LOCAL] Encontrado: " + alimentoOpt.get().getNome() + " | " + calItem + " kcal");

                    salvarNoCache(itemLimpo, calItem, alimentoOpt.get().getProteinas100g(), alimentoOpt.get().getCarboidratos100g(), alimentoOpt.get().getGorduras100g(), "TACO");
                } else {
                    itensNaoEncontrados.add(itemLimpo);
                }
            } else {
                itensNaoEncontrados.add(itemLimpo);
            }
        }

        // Fallback para IA se houver pendências
        if (!itensNaoEncontrados.isEmpty()) {
            String pendencias = String.join(", ", itensNaoEncontrados);
            System.out.println("[FALLBACK IA] Calculando itens ausentes via IA: " + pendencias);
            try {
                Integer caloriasIa = aiService.estimarCaloriasRefeicaoLivre(pendencias);
                if (caloriasIa != null) {
                    totalCalorias += caloriasIa;
                }
            } catch (Exception e) {
                System.err.println("Erro no fallback da IA: " + e.getMessage());
                totalCalorias += (itensNaoEncontrados.size() * 150.0);
            }
        }

        int caloriasFinais = (int) Math.round(totalCalorias);

        // Salva a refeição completa combinada no cache para consultas futuras idênticas
        salvarNoCache(chaveDescricao, (double) caloriasFinais, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "CALCULADO");

        return caloriasFinais;
    }

    private void salvarNoCache(String nome, double calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras, String fonte) {
        try {
            if (cacheAlimentoRepository.findByNomeIgnoreCase(nome).isEmpty()) {
                CacheAlimento cache = new CacheAlimento();
                // Garante que o nome não exceda o limite de 100 caracteres da coluna
                cache.setNome(nome.length() > 100 ? nome.substring(0, 100) : nome);
                cache.setCaloriasPor100g(BigDecimal.valueOf(calorias));
                cache.setProteinasG(proteinas != null ? proteinas : BigDecimal.ZERO);
                cache.setCarboidratosG(carboidratos != null ? carboidratos : BigDecimal.ZERO);
                cache.setGordurasG(gorduras != null ? gorduras : BigDecimal.ZERO);
                cache.setPrecoMedioBase(BigDecimal.ZERO);
                cache.setDataUltimaAtualizacao(LocalDate.now());
                cache.setFonteDados(fonte);

                cacheAlimentoRepository.save(cache);
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar cache: " + e.getMessage());
        }
    }

    private double converterParaGramas(double quantidade, String unidade) {
        return switch (unidade) {
            case "g", "ml" -> quantidade;
            case "colher(es) de sopa" -> quantidade * 15.0;
            case "unidade(s)" -> quantidade * 50.0;
            case "fatia(s)" -> quantidade * 30.0;
            case "concha(s)" -> quantidade * 130.0;
            default -> quantidade;
        };
    }

    public List<RegistroDiario> buscarRefeicoesDoDia(String usuarioId, LocalDate data) {
        return repository.findByUsuarioIdAndData(usuarioId, data);
    }

    public boolean isRefeicaoRegistrada(String usuarioId, LocalDate data, String tipoRefeicao) {
        RegistroDiario refeicao = repository.findByUsuarioIdAndDataAndTipoRefeicao(usuarioId, data, tipoRefeicao);
        return refeicao != null;
    }

    public void excluirRefeicao(String registroId) {
        repository.deleteById(registroId);
    }
}