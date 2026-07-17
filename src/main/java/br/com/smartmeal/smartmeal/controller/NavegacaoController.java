package br.com.smartmeal.smartmeal.controller;

import br.com.smartmeal.smartmeal.model.Usuario;
import br.com.smartmeal.smartmeal.model.dto.DietaResponseDTO;
import br.com.smartmeal.smartmeal.model.nosql.DietaRecomendada;
import br.com.smartmeal.smartmeal.service.ArtificialIntelligenceService;
import br.com.smartmeal.smartmeal.service.DietaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class NavegacaoController {

    @Autowired
    private DietaService dietaService;

    @Autowired
    private ArtificialIntelligenceService artificialIntelligenceService;

    @GetMapping({"/", "/login"})
    public String abrirLogin() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String abrirDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        boolean perfilIncompleto = (usuario.getPesoKg() == null);
        model.addAttribute("perfilIncompleto", perfilIncompleto);
        model.addAttribute("usuario", usuario);

        if(perfilIncompleto) {
            String ideiasReceitas = artificialIntelligenceService.gerarIdeiasEconomicasDoDia("Café da Manhã");
            model.addAttribute("ideiasReceitas", ideiasReceitas);
        }else {
            var dietaPersonalizada = dietaService.buscarPorIdUsuario(usuario.getIdUsuario());
            model.addAttribute("dietaReal", dietaPersonalizada);
        }
        return "dashboard";
    }

    @GetMapping("/api/receitas/aleatorias")
    @ResponseBody
    public String obterNovasIdeias(@RequestParam(defaultValue = "Café da Manhã") String tipoRefeicao, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "Sessão expirada. Faça login novamente.";
        }

        return artificialIntelligenceService.gerarIdeiasEconomicasDoDia(tipoRefeicao);
    }
}
