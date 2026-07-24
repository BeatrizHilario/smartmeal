package br.com.smartmeal.smartmeal.controller;

import br.com.smartmeal.smartmeal.model.Usuario;
import br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada;
import br.com.smartmeal.smartmeal.repository.UsuarioRepository;
import br.com.smartmeal.smartmeal.service.ArtificialIntelligenceService;
import br.com.smartmeal.smartmeal.service.DietaService;
import br.com.smartmeal.smartmeal.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import br.com.smartmeal.smartmeal.config.SenhaUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArtificialIntelligenceService artificialIntelligenceService;

    @Autowired
    private DietaService dietaService;

    @PostMapping("/cadastrar")
    public String cadastrar(Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
           String senhaCripto = SenhaUtils.criptografar(usuario.getSenha());
           usuario.setSenha(senhaCripto);

            usuarioService.cadastrarUsuario(usuario);

            redirectAttributes.addFlashAttribute("sucesso", "Conta criada com sucesso! Faça seu login.");
           return "redirect:/login";
        } catch (RuntimeException e) {
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("erro", "Esse e-mail já está em uso ou houve um erro no cadastro.");
            return "redirect:/login";
        }
    }

    @PostMapping("/fazerLogin")
    public String fazerLogin(String email, String senha, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioBanco = usuarioRepository.findByEmail(email);

        if (usuarioBanco == null) {
            redirectAttributes.addFlashAttribute("erro", "Este e-mail não está cadastrado.");
            return "redirect:/login";
        }

        if (SenhaUtils.verificar(senha, usuarioBanco.getSenha())) {
            session.setAttribute("usuarioLogado", usuarioBanco);
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("erro", "Senha incorreta. Tente novamente.");
            return "redirect:/login";
        }

    }

    @GetMapping("/sair")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/completarPerfil")
    public String completarPerfil(Usuario dadosAtualizados, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            if (usuarioLogado == null) return "redirect:/login";

            usuarioLogado.setAlturaCm(dadosAtualizados.getAlturaCm());
            usuarioLogado.setPesoKg(dadosAtualizados.getPesoKg());
            usuarioLogado.setGenero(dadosAtualizados.getGenero());
            usuarioLogado.setNivelAtividade(dadosAtualizados.getNivelAtividade());
            usuarioLogado.setObjetivo(dadosAtualizados.getObjetivo());
            usuarioLogado.setRestricaoAlimentar(dadosAtualizados.getRestricaoAlimentar());
            usuarioLogado.setOrcamentoMaxMensal(dadosAtualizados.getOrcamentoMaxMensal());

            double pesoParaConta = usuarioLogado.getPesoKg().doubleValue();
            usuarioLogado.setMetaAguaMl((int) (pesoParaConta * 35));

            double tmb;

            if ("M".equals(usuarioLogado.getGenero())) {
                tmb = 66.5 + (13.75 * pesoParaConta) + (5.003 * usuarioLogado.getAlturaCm());
            } else {
                tmb = 655.1 + (9.563 * pesoParaConta) + (1.850 * usuarioLogado.getAlturaCm());
            }

            double fatorAtividade = 1.2;
            if ("leve".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.375;
            if ("moderado".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.55;
            if ("intenso".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.725;

            double gastoDiario = tmb * fatorAtividade;
            if ("emagrecer".equals(usuarioLogado.getObjetivo())) gastoDiario -= 500;
            else if ("hipertrofia".equals(usuarioLogado.getObjetivo())) gastoDiario += 500;

            usuarioLogado.setMetaCaloricaKcal((int) gastoDiario);
            usuarioRepository.save(usuarioLogado);

            String dietaHtml = artificialIntelligenceService.gerarDietaPelaIA(usuarioLogado, "Café da Manhã");

            System.out.println("=========================================");
            System.out.println("RESPOSTA DIRETA DA IA PARA O UTILIZADOR: " + usuarioLogado.getIdUsuario());
            System.out.println(dietaHtml);
            System.out.println("=========================================");

            DietaRecomendada novaDieta = new DietaRecomendada();
            novaDieta.setIdUsuario(usuarioLogado.getIdUsuario());
            novaDieta.setDataGeracao(java.time.LocalDateTime.now());
            novaDieta.setPlanoAlimentar(dietaHtml);
            novaDieta.setCustoEstimado(usuarioLogado.getOrcamentoMaxMensal().doubleValue());

            dietaService.salvarDieta(novaDieta);

            session.setAttribute("usuarioLogado", usuarioLogado);
            return "redirect:/dashboard";
        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroIA", "Nossos servidores de IA estão temporariamente sobrecarregados devido ao limite de requisições. Por favor, aguarde 1 minuto e tente novamente.");
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/api/dieta/nova-sugestao")
    @ResponseBody
    public String gerarNovaSugestao(String tipoRefeicao, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "<p class= 'text-red-500'>Sessão expirada. Atualize a página e faça login novamente.</p>";
        }

        String dietaHtml = artificialIntelligenceService.gerarDietaPelaIA(usuarioLogado, tipoRefeicao);

        DietaRecomendada novaDieta = new DietaRecomendada();
        novaDieta.setIdUsuario(usuarioLogado.getIdUsuario());
        novaDieta.setDataGeracao(java.time.LocalDateTime.now());
        novaDieta.setPlanoAlimentar(dietaHtml);
        novaDieta.setCustoEstimado(usuarioLogado.getOrcamentoMaxMensal().doubleValue());

        dietaService.salvarDieta(novaDieta);

        return dietaHtml;
    }

    @PostMapping("/atualizarPerfil")
    public String atualizarPerfil(Usuario dadosAtualizados, String novaSenha, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        usuarioLogado.setNome(dadosAtualizados.getNome());
        usuarioLogado.setAlturaCm(dadosAtualizados.getAlturaCm());
        usuarioLogado.setPesoKg(dadosAtualizados.getPesoKg());
        usuarioLogado.setGenero(dadosAtualizados.getGenero());
        usuarioLogado.setNivelAtividade(dadosAtualizados.getNivelAtividade());
        usuarioLogado.setObjetivo(dadosAtualizados.getObjetivo());
        usuarioLogado.setRestricaoAlimentar(dadosAtualizados.getRestricaoAlimentar());
        usuarioLogado.setOrcamentoMaxMensal(dadosAtualizados.getOrcamentoMaxMensal());

        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            String novaSenhaCripto = SenhaUtils.criptografar(novaSenha);
            usuarioLogado.setSenha(novaSenhaCripto);
        }

        double pesoParaConta = usuarioLogado.getPesoKg().doubleValue();
        usuarioLogado.setMetaCaloricaKcal((int) (pesoParaConta * 35));

        double tmb;
        if ("M".equals(usuarioLogado.getGenero())) {
            tmb = 66.5 + (13.75 * pesoParaConta) + (5.003 * usuarioLogado.getAlturaCm());
        } else {
            tmb = 665.1 + (9.563 * pesoParaConta) + (1.850 * usuarioLogado.getAlturaCm());
        }

        double fatorAtividade = 1.2;
        if ("leve".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.375;
        if ("moderado".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.55;
        if ("intenso".equals(usuarioLogado.getNivelAtividade())) fatorAtividade = 1.725;

        double gastoDiario = tmb * fatorAtividade;
        if ("emagrecer".equals(usuarioLogado.getObjetivo())) gastoDiario -= 500;
        else if ("hipertrofia".equals(usuarioLogado.getObjetivo())) gastoDiario += 500;

        usuarioLogado.setMetaCaloricaKcal((int) gastoDiario);

        usuarioRepository.save(usuarioLogado);
        session.setAttribute("usuarioLogado", usuarioLogado);

        return "redirect:/dashboard";
    }

    @GetMapping("/deletarConta")
    public String deletarConta(HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado != null) {
            try {

                Integer id = usuarioLogado.getIdUsuario();

                dietaService.excluirDietasDoUsuario(Integer.valueOf(id));

                usuarioRepository.deleteById(id);

                session.invalidate();
            }catch (Exception e) {
                System.out.println("ERRO CRÍTICO NA EXCLUSÃO:");
                e.printStackTrace();
                return "redirect:/dashboard?erroExclusao=true";
            }
         }else {
            System.out.println("Aviso: Tentativa de exclusão ignorada porque o usuário na sessão era nulo.");
        }

        return "redirect:/login";
    }

    @PostMapping("/salvar-nova-senha")
    public String salvarNovaSenha(@RequestParam("email") String email, @RequestParam("novaSenha") String novaSenha, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario != null) {
            usuario.setSenha(novaSenha);
            usuarioRepository.save(usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Senha alterada com sucesso! Faça seu login.");
        }else {
            redirectAttributes.addFlashAttribute("erro", "Erro: O e-mail informado não está cadastrado.");
        }

        return "redirect:/";
    }

}
