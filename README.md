# Library API

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue.svg)
![Maven](https://img.shields.io/badge/Maven-red.svg)

API RESTful para gerenciamento de uma biblioteca, permitindo operações de CRUD para Livros e Autores. Este projeto foi desenvolvido como parte do meu portfólio de backend, demonstrando boas práticas de desenvolvimento, arquitetura limpa e um ciclo de vida de software completo, desde a concepção até o deploy.

**URL do Deploy:** [https://library-api-zmlr.onrender.com/swagger-ui/index.html](https://library-api-zmlr.onrender.com/swagger-ui/index.html)

*(Nota: O deploy gratuito no Render pode levar cerca de 30-60 segundos para "acordar" na primeira requisição após um período de inatividade.)*

---

## 🚀 Features

- **CRUD Completo para Livros e Autores**:
  - **Autores**: Criar, ler, atualizar e deletar autores.
  - **Livros**: Criar, ler, atualizar e deletar livros, com associação a um autor existente.
- **Paginação e Filtros**: Listagem de recursos com suporte a paginação e filtros por nome (para autores) e título (para livros).
- **Validação de Dados**: Validações robustas na camada de API (DTOs) e de persistência (Entidades) para garantir a integridade dos dados.
- **Tratamento de Erros Centralizado**: Respostas de erro padronizadas e claras para cenários como dados inválidos (400), recursos não encontrados (404) e conflitos (409).
- **Documentação de API com Swagger**: Documentação interativa e detalhada para todos os endpoints, incluindo exemplos de requisições e respostas.
- **Containerização com Docker**: Aplicação e banco de dados totalmente containerizados para portabilidade e consistência entre ambientes.

---

## 🏗️ Arquitetura e Padrões

Este projeto foi construído sobre uma base de princípios de software modernos para garantir escalabilidade e manutenibilidade:

- **Arquitetura "Package by Feature"**: O código é organizado em fatias verticais por funcionalidade (`author`, `book`), mantendo alta coesão e baixo acoplamento.
- **Princípios SOLID**: O design do código segue os princípios SOLID para criar um software mais compreensível, flexível e manutenível.
- **Clean Code**: Foco em escrever um código limpo, legível e autoexplicativo.
- **DTO (Data Transfer Object) Pattern**: Desacoplamento total entre as entidades de persistência e a camada de API, prevenindo a exposição de dados internos e criando um contrato de API estável.
- **Testes Abrangentes**:
  - **Testes Unitários**: Cobertura completa da camada de serviço (`Service`) com Mockito para garantir a lógica de negócio.
  - **Testes de Integração**: Cobertura completa da camada de controller (`Controller`) com `@SpringBootTest` e `MockMvc`, validando o fluxo completo da API, desde a requisição HTTP até o banco de dados.

---

## 🛠️ Tecnologias Utilizadas

- **Backend**:
  - [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
  - [Spring Boot 3.3.0](https://spring.io/projects/spring-boot)
  - [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
  - [Hibernate](https://hibernate.org/)
- **Banco de Dados**:
  - [PostgreSQL](https://www.postgresql.org/) (Produção)
  - [H2 Database](https://www.h2database.com/html/main.html) (Testes)
- **Testes**:
  - [JUnit 5](https://junit.org/junit5/)
  - [Mockito](https://site.mockito.org/)
- **Documentação**:
  - [SpringDoc (Swagger UI)](https://springdoc.org/)
- **Build & Dependências**:
  - [Maven](https://maven.apache.org/)
- **DevOps**:
  - [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
  - [Render](https://render.com/) (Plataforma de Deploy)

---

## 💻 Como Executar Localmente

### Pré-requisitos

- [Java 21](https://www.oracle.com/java/technologies/javase/21-relnote-issues.html)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)

### 1. Usando Docker (Recomendado)

Este é o método mais simples e recomendado, pois gerencia tanto a aplicação quanto o banco de dados.

1. **Clone o repositório:**
   ```sh
   git clone https://github.com/seu-usuario/library.git
   cd library
   ```

2. **Suba os contêineres com Docker Compose:**
   ```sh
   docker compose up --build
   ```
   A flag `--build` garante que a imagem da sua aplicação será construída com as últimas alterações do código.

3. **Acesse a aplicação:**
   - **API**: `http://localhost:8080`
   - **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### 2. Executando como um projeto Spring Boot (Sem Docker)

Este método usará o banco de dados em memória H2.

1. **Clone o repositório:**
   ```sh
   git clone https://github.com/seu-usuario/library.git
   cd library
   ```

2. **Execute a aplicação com Maven:**
   ```sh
   ./mvnw spring-boot:run
   ```

3. **Acesse a aplicação:**
   - **API**: `http://localhost:8080`
   - **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
   - **Console H2**: `http://localhost:8080/h2-console` (Use a URL JDBC `jdbc:h2:mem:testdb` para conectar)

---

## 📧 Contato

**Daniel Lira**

- **Email**: [daniel.lira.s23@gmail.com](mailto:daniel.lira.s23@gmail.com)
- **LinkedIn**: [https://www.linkedin.com/in/daniel-lira-s/](https://www.linkedin.com/in/daniel-lira-s/)
- **GitHub**: [https://github.com/liras23](https://github.com/liras23)

---


