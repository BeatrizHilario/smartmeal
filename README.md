# SmartMeal - Monitorização Nutricional e Recomendações com IA

> Aplicação web desenvolvida em **Java** e **Spring Boot** para acompanhamento de rotina alimentar, cálculo de metas calóricas e geração de dietas personalizadas através da **API Google Gemini**.

<img width="600" height="337" alt="smartmeal-presentation" src="https://github.com/user-attachments/assets/6f7ec35e-e04e-482b-b8a4-0f572b1b1633" />


<p>
  🌐 <strong>Deploy em Produção:</strong> <a href="https://smartmeal-dpie.onrender.com" target="_blank">Acessar o SmartMeal</a>
</p>

---

## 📌 Visão Geral

O **SmartMeal** combina cálculo nutricional preciso com inteligência artificial generativa para apoiar o utilizador na gestão da sua alimentação diária. A aplicação analisa o perfil físico, restrições e preferências alimentares, automatizando sugestões de refeições com suporte a uma base de dados nutricional estruturada.

---

## 🚀 Funcionalidades Principais

- **Autenticação e Perfil:** Registo seguro de utilizadores com encriptação de credenciais e gestão de dados antropométricos.
- **Diário Alimentar:** Acompanhamento de refeições diárias e monitorização do consumo calórico e macronutrientes em tempo real.
- **Recomendações com Inteligência Artificial:** Integração com a API Google Gemini para elaborar planos alimentares dinâmicos de acordo com metas de saúde e restrições de paladar.
- **Cache de Alimentos:** Mecanismo de persistência para otimizar pesquisas de itens e acelerar o cálculo das tabelas nutricionais.
- **Interface Web Dinâmica:** Páginas responsivas renderizadas via Thymeleaf, HTML5, CSS3 e JavaScript Vanilla.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.5.16
- **Persistência Relacional (SQL):** PostgreSQL (Supabase) via Spring Data JPA
- **Persistência Não-Relacional (NoSQL):** MongoDB para registos históricos e flexibilidade de documentos de dietas
- **Inteligência Artificial:** Google Gemini API
- **Frontend:** Thymeleaf, CSS3, JavaScript
- **Containerização & Deploy:** Docker e Render

---

## 📁 Arquitetura do Projeto

```text
br.com.smartmeal.smartmeal/
├── config/         # Configurações de segurança e encriptação (SenhaUtils)
├── controller/     # Controladores MVC e endpoints (Diario, Dieta, Usuario, Navegacao)
├── model/          # Entidades relacionais JPA (Usuario, TabelaNutricional, RestricaoPaladar)
│   ├── dto/        # Objetos de transferência de dados (DietaResponseDTO, PlanoAlimentarDTO)
│   └── nosql/      # Documentos MongoDB (DietaRecomendada, RegistroDiario)
├── repository/     # Repositórios Spring Data JPA e NoSQL (MongoDB)
└── service/        # Regras de negócio e integração com a API Gemini
```

## 🌐 Acesso à Aplicação

O projeto está hospedado e disponível para testes em ambiente de produção:

🔗 **Link do Projeto:** [smartmeal-dpie.onrender.com](https://smartmeal-dpie.onrender.com)

## 👩‍💻 Autora

Desenvolvido por **Beatriz Hilario**

• [Linkedin](https://www.linkedin.com/in/beatriz-hilario-pinto-46b948303/) • [Portfólio](https://portfoliobeatrizhilario.vercel.app)
