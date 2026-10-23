# Trabalho Integrador — Spring Boot + MySQL

API REST de usuários e roles com tela de login, feita para a disciplina de POO.
Stack: Java 21, Spring Boot 4, Spring Data JPA, MySQL (XAMPP), Lombok, Gradle.

## Como rodar

1. Inicie o MySQL do XAMPP (pelo app **manager-osx** → aba *Manage Servers* → *MySQL Database* → *Start*, ou pelo terminal):

   ```bash
   sudo /Applications/XAMPP/xamppfiles/xampp startmysql
   ```

   O XAMPP usa usuário `root` com **senha vazia**. O banco `trabalho_integrador` é criado automaticamente.
   Para outra senha: `export DB_PASSWORD=suasenha` (e `DB_USER` para outro usuário).

2. Use o JDK 21 e suba a aplicação:

   ```bash
   export JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-21.0.7/Contents/Home
   ./gradlew bootRun
   ```

3. Abra <http://localhost:8080> e entre com **admin / admin123** (criado na primeira execução).

Testes (usam banco H2 em memória, não precisam do MySQL): `./gradlew test`

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Login (200 ok, 401 credenciais inválidas) |
| GET | `/api/usuarios` | Lista usuários |
| GET | `/api/usuarios/{id}` | Busca por id (404 se não existir) |
| POST | `/api/usuarios` | Cria usuário (201; 409 se login repetido) |
| PUT | `/api/usuarios/{id}` | Atualiza usuário |
| DELETE | `/api/usuarios/{id}` | Remove usuário (204) |
| POST | `/api/usuarios/{id}/roles/{nomeRole}` | Atribui uma role |
| GET | `/api/roles` | Lista roles |
| POST | `/api/roles` | Cria role (201) |

```bash
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"login":"admin","senha":"admin123"}'

curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":"maria@escola.com","login":"maria","senha":"senha123","tipo":"CLIENTE"}'

curl localhost:8080/api/usuarios
curl localhost:8080/api/usuarios/1
curl -X POST localhost:8080/api/usuarios/2/roles/ROLE_ADMIN
curl -X POST localhost:8080/api/roles -H 'Content-Type: application/json' \
  -d '{"nome":"ROLE_PROFESSOR","descricao":"Acesso de professor"}'
```

Erros voltam em JSON: `{"status": 404, "erro": "Not Found", "mensagem": "...", "timestamp": "..."}`.

## Onde está cada passo do quadro

| Passo | Onde |
|---|---|
| 1. 3 métodos no model | `model/Usuario.java`: `adicionarRole`, `removerRole`, `possuiRole`, `podeGerenciarUsuarios` |
| 2. Service | `service/UsuarioServiceImpl.java`, `service/AuthService.java`, `service/RoleService.java` |
| 3. Controller REST | `controller/` — `@RestController` + `@RequestMapping("/api/...")` |
| 4. Interface | `service/UsuarioService.java` (o controller depende da interface) |
| 5. Comunicação com MySQL | `src/main/resources/application.properties` |
| 6. Interface com o banco | `repository/` — interfaces que estendem `JpaRepository` |

## Onde está cada conceito de POO

| Conceito | Onde |
|---|---|
| Herança | `Pessoa` (abstrata) → `Usuario` (abstrata) → `Administrador` / `Cliente` |
| Polimorfismo (sobrescrita) | `descreverPerfil()`, `podeGerenciarUsuarios()`, `getTipo()`; usado em `UsuarioResponse.de` ao listar `List<Usuario>` |
| Polimorfismo (sobrecarga) | `Usuario.possuiRole(String)` e `possuiRole(Role)` |
| Agregação | `Usuario` ↔ `Role` (`@ManyToMany`): a role existe sozinha e é compartilhada |
| Associação | `RegistroAcesso` → `Usuario` (`@ManyToOne`), gravado a cada tentativa de login |
| Interface | `UsuarioService` / `UsuarioServiceImpl`; repositórios |
| try/catch | `UsuarioServiceImpl.criar/atualizar/remover` (converte erro do banco em `RegraNegocioException`) |
| Exceções + tratamento global | `exception/` e `GlobalExceptionHandler` |
| Logs (debug/info/warn/error) | `@Slf4j` nos services, controller e `GlobalExceptionHandler` |

Senhas são guardadas com hash BCrypt e nunca aparecem nas respostas da API.
