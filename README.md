# 🎬 MovieFlix API

API REST para gerenciamento de catálogo de filmes, com autenticação stateless via **Spring Security + JWT**.

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Spring Security & JWT — Como Funciona](#-spring-security--jwt--como-funciona)
  - [Fluxo de Autenticação](#fluxo-de-autenticação)
  - [Fluxo de Autorização](#fluxo-de-autorização)
  - [SecurityConfig](#1-securityconfig)
  - [UserDetails & UserDetailsService](#2-userdetails--userdetailsservice)
  - [TokenService — Geração e Validação do JWT](#3-tokenservice--geração-e-validação-do-jwt)
  - [SecurityFilter — Interceptação das Requisições](#4-securityfilter--interceptação-das-requisições)
  - [JWTUserData](#5-jwtuserdata)
- [Endpoints](#-endpoints)
- [Como Executar](#-como-executar)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Documentação Swagger](#-documentação-swagger)

---

## 📌 Sobre o Projeto

O **MovieFlix** é uma API REST construída com **Spring Boot 4** que permite o cadastro e consulta de filmes, categorias e plataformas de streaming. Todos os recursos (exceto registro e login) são protegidos por autenticação **JWT Bearer Token**, implementada manualmente com Spring Security em modo **stateless** — sem sessões no servidor.

---

## 🛠 Tecnologias

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 4.1.0 | Framework base |
| Spring Security | 7.1.0 | Autenticação e autorização |
| Auth0 Java JWT | 4.4.0 | Geração e validação de tokens JWT |
| Spring Data JPA | — | Persistência de dados |
| PostgreSQL | — | Banco de dados relacional |
| Flyway | — | Migrations de banco |
| Springdoc OpenAPI | 2.8.9 | Documentação Swagger UI |
| Lombok | — | Redução de boilerplate |
| BCrypt | — | Hash seguro de senhas |

---

## 🏗 Arquitetura

```
src/main/java/com/movieflix/
├── config/
│   ├── SecurityConfig.java          # Configuração central do Spring Security
│   ├── SecurityFilter.java          # Filtro JWT (OncePerRequestFilter)
│   ├── TokenService.java            # Geração e validação do token JWT
│   ├── JWTUserData.java             # DTO com dados extraídos do token
│   ├── SwaggerConfig.java           # Configuração do OpenAPI
│   └── ApplicationControllerAdvice.java  # Tratamento global de exceções
├── controller/
│   ├── AuthController.java          # Interface (documentação Swagger)
│   ├── AuthControllerImpl.java      # Implementação (register + login)
│   ├── MovieController.java         # Interface
│   ├── MovieControllerImpl.java     # Implementação
│   ├── CategoryController.java      # Interface
│   ├── CategoryControllerImpl.java  # Implementação
│   ├── StreamingController.java     # Interface
│   └── StreamingControllerImpl.java # Implementação
├── entity/
│   └── User.java                    # Entidade que implementa UserDetails
├── service/
│   ├── UserService.java             # Cadastro com hash BCrypt
│   └── AuthService.java            # Implementa UserDetailsService
└── exception/
    └── UsernameOrPasswordInvalidException.java
```

---

## 🔐 Spring Security & JWT — Como Funciona

Esta seção detalha toda a camada de segurança da aplicação.

### Fluxo de Autenticação

```
POST /movieflix/auth/login
         │
         ▼
  AuthControllerImpl
  (AuthenticationManager.authenticate)
         │
         ▼
  AuthService (UserDetailsService)
  └─ Busca o usuário por e-mail no banco
  └─ Spring compara senha com BCrypt
         │
         ▼
  Credenciais válidas?
  ├── ❌ NÃO → lança BadCredentialsException → 400 Bad Request
  └── ✅ SIM → TokenService.generateToken(user)
                    │
                    ▼
              JWT assinado com HMAC256
              retornado no body { "token": "..." }
```

### Fluxo de Autorização

```
Requisição com header:
Authorization: Bearer <token>
         │
         ▼
  SecurityFilter (executa antes de todo controller)
  └─ Extrai o token do header
  └─ TokenService.verifyToken(token)
         │
         ▼
  Token válido?
  ├── ❌ NÃO → segue sem autenticação → Spring retorna 403
  └── ✅ SIM → popula SecurityContextHolder com o usuário
                    │
                    ▼
              Requisição chega ao Controller normalmente
```

---

### 1. SecurityConfig

> `src/main/java/com/movieflix/config/SecurityConfig.java`

Ponto central da configuração de segurança. Define **quais rotas são públicas** e **como a cadeia de filtros funciona**.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())                          // API REST não usa CSRF
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sem sessão
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/movieflix/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/movieflix/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/swagger/**").permitAll()
                .anyRequest().authenticated()                      // Tudo mais exige token
            )
            .addFilterBefore(securityFilter,                       // Injeta o filtro JWT
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();                        // Hash seguro de senhas
    }
}
```

**Pontos importantes:**
- `SessionCreationPolicy.STATELESS` — o servidor **não armazena estado de sessão**. Cada requisição precisa provar sua identidade via token.
- `csrf().disable()` — seguro para APIs REST pois não usamos cookies de sessão.
- `addFilterBefore` — garante que o `SecurityFilter` rode **antes** do filtro padrão de autenticação do Spring.

---

### 2. UserDetails & UserDetailsService

> `src/main/java/com/movieflix/entity/User.java`
> `src/main/java/com/movieflix/service/AuthService.java`

A entidade `User` implementa `UserDetails`, a interface do Spring Security que representa o usuário autenticado.

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    private String email;
    private String password;  // Armazenado com hash BCrypt

    @Override
    public String getUsername() {
        return email;          // Spring usa e-mail como identificador único
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();      // Sem roles nesta versão
    }
    // isAccountNonExpired, isAccountNonLocked, etc → todos retornam true
}
```

O `AuthService` implementa `UserDetailsService` e é chamado pelo `AuthenticationManager` durante o login para **carregar o usuário do banco**:

```java
// AuthService busca o usuário por e-mail (getUsername())
// Spring Security cuida da comparação de senha via PasswordEncoder
```

No cadastro, o `UserService` aplica o **BCrypt** antes de salvar:

```java
public User save(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return repository.save(user);
}
```

---

### 3. TokenService — Geração e Validação do JWT

> `src/main/java/com/movieflix/config/TokenService.java`

Usa a biblioteca **Auth0 Java JWT** para criar e verificar os tokens.

#### Geração do token (login bem-sucedido)

```java
public String generateToken(User user) {
    Algorithm algorithm = Algorithm.HMAC256(secret); // Assina com chave secreta

    return JWT.create()
        .withSubject(user.getEmail())                // Identificador principal
        .withClaim("userId", user.getId())           // Claims personalizados
        .withClaim("name", user.getName())
        .withExpiresAt(Instant.now().plusSeconds(86400)) // Expira em 24h
        .withIssuedAt(Instant.now())
        .withIssuer("API MovieFlix")
        .sign(algorithm);
}
```

#### Estrutura do JWT gerado

```
Header:  { "alg": "HS256", "typ": "JWT" }
Payload: {
    "sub": "user@email.com",   ← e-mail do usuário
    "userId": 1,
    "name": "Igor",
    "iat": 1720876766,         ← emitido em
    "exp": 1720963166,         ← expira em (24h)
    "iss": "API MovieFlix"
}
Signature: HMAC256(header + payload, secret)
```

#### Validação do token (requisições subsequentes)

```java
public Optional<JWTUserData> verifyToken(String token) {
    try {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        DecodedJWT jwt = JWT.require(algorithm).build().verify(token);

        return Optional.of(JWTUserData.builder()
            .id(jwt.getClaim("userId").asLong())
            .name(jwt.getClaim("name").asString())
            .email(jwt.getSubject())
            .build());

    } catch (JWTVerificationException ex) {
        return Optional.empty(); // Token inválido ou expirado
    }
}
```

A chave secreta (`secret`) é injetada via `@Value("${movieflix.security.secret}")` — **nunca hardcodada no código**.

---

### 4. SecurityFilter — Interceptação das Requisições

> `src/main/java/com/movieflix/config/SecurityFilter.java`

Filtro que executa **uma vez por requisição** (`OncePerRequestFilter`) e é responsável por ler o token do header e autenticar o usuário no contexto do Spring.

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    String authorizationHeader = request.getHeader("Authorization");

    if (Strings.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
        String token = authorizationHeader.substring("Bearer ".length());

        Optional<JWTUserData> optJwtUserData = tokenService.verifyToken(token);

        if (optJwtUserData.isPresent()) {
            JWTUserData userData = optJwtUserData.get();

            // Registra o usuário como autenticado no contexto de segurança
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userData, null, null);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    filterChain.doFilter(request, response); // Continua a cadeia de filtros
}
```

**O que acontece se o token for inválido ou ausente?**

O filtro simplesmente não popula o `SecurityContextHolder`. O Spring Security, ao processar a requisição em seguida, percebe que não há autenticação e retorna **`403 Forbidden`**.

---

### 5. JWTUserData

> `src/main/java/com/movieflix/config/JWTUserData.java`

Record imutável que carrega os dados do usuário extraídos do token — elimina a necessidade de ir ao banco a cada requisição:

```java
@Builder
public record JWTUserData(Long id, String name, String email) {}
```

---

## 📡 Endpoints

### 🔓 Públicos (sem token)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/movieflix/auth/register` | Registra um novo usuário |
| `POST` | `/movieflix/auth/login` | Autentica e retorna o JWT |

**Exemplo — Register:**
```json
// POST /movieflix/auth/register
{
  "name": "Igor",
  "email": "igor@email.com",
  "password": "senha123"
}
```

**Exemplo — Login:**
```json
// POST /movieflix/auth/login
{
  "email": "igor@email.com",
  "password": "senha123"
}

// Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 🔒 Protegidos (requerem `Authorization: Bearer <token>`)

#### Filmes — `/movieflix/movie`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/movieflix/movie` | Lista todos os filmes |
| `GET` | `/movieflix/movie/{id}` | Busca filme por ID |
| `GET` | `/movieflix/movie/search?category={id}` | Busca filmes por categoria |
| `POST` | `/movieflix/movie` | Cadastra um novo filme |
| `PUT` | `/movieflix/movie/{id}` | Atualiza um filme |
| `DELETE` | `/movieflix/movie/{id}` | Remove um filme |

#### Categorias — `/movieflix/category`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/movieflix/category` | Lista todas as categorias |
| `GET` | `/movieflix/category/{id}` | Busca categoria por ID |
| `POST` | `/movieflix/category` | Cadastra uma categoria |
| `DELETE` | `/movieflix/category/{id}` | Remove uma categoria |

#### Streamings — `/movieflix/streaming`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/movieflix/streaming` | Lista todas as plataformas |
| `GET` | `/movieflix/streaming/{id}` | Busca plataforma por ID |
| `POST` | `/movieflix/streaming` | Cadastra uma plataforma |
| `DELETE` | `/movieflix/streaming/{id}` | Remove uma plataforma |

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL rodando localmente

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/Igor-Pandolf/movieflix.git
cd movieflix

# 2. Configure as variáveis de ambiente (ver seção abaixo)

# 3. Execute a aplicação
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## ⚙️ Variáveis de Ambiente

Configure as variáveis abaixo antes de executar (via `application.properties`, `.env` ou variáveis do sistema):

| Variável | Descrição | Exemplo |
|---|---|---|
| `spring.datasource.url` | URL do banco PostgreSQL | `jdbc:postgresql://localhost:5432/movieflix` |
| `spring.datasource.username` | Usuário do banco | `postgres` |
| `spring.datasource.password` | Senha do banco | `postgres` |
| `movieflix.security.secret` | Chave secreta para assinar o JWT | `minha-chave-super-secreta-256bits` |

> ⚠️ **Atenção:** nunca exponha o valor de `movieflix.security.secret` em repositórios públicos. Use variáveis de ambiente ou um vault de segredos em produção.

---

## 📖 Documentação Swagger

Com a aplicação em execução, acesse:

```
http://localhost:8080/swagger-ui/index.html
```

Para testar endpoints protegidos diretamente pelo Swagger:

1. Faça login em `POST /movieflix/auth/login` e copie o token retornado.
2. Clique no botão **🔒 Authorize** no topo da página.
3. Cole o token no campo `bearerAuth` (sem o prefixo `Bearer`).
4. Clique em **Authorize** — todos os endpoints protegidos agora estarão acessíveis.

---

## 👤 Autor

**Igor Pandolf**  
📧 pandolf.igor@gmail.com
