# SinodalReport

API REST de usuários e roles com tela de login, feita para a disciplina de POO.

**Stack:** Java 21, Spring Boot, Spring Data JPA, MySQL (XAMPP), Lombok, Gradle.

## Como rodar

1. Abra o XAMPP Control Panel e clique em **Start** no MySQL.
2. Confira a porta do MySQL (aqui é a 3307) no `application.properties`.
3. Rode a aplicação no IntelliJ (▶ em `SinodalReportApplication`) ou use:

```
.\gradlew bootRun
```

4. Abra http://localhost:8080 e entre com **admin / admin123** (criado na primeira execução).

O banco `sinodal_report` é criado automaticamente. O XAMPP usa usuário `root` com senha vazia. Para outra senha, defina as variáveis `DB_USER` e `DB_PASSWORD`.

**Testes** (usam banco H2 em memória, não precisam do MySQL):

```
.\gradlew test
```

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| POST | /api/auth/login | Login (200 ok, 401 credenciais inválidas) |
| GET | /api/usuarios | Lista usuários |
| GET | /api/usuarios/{id} | Busca por id (404 se não existir) |
| POST | /api/usuarios | Cria usuário (201; 409 se login repetido) |
| PUT | /api/usuarios/{id} | Atualiza usuário |
| DELETE | /api/usuarios/{id} | Remove usuário (204) |
| POST | /api/usuarios/{id}/roles/{nomeRole} | Atribui uma role |
| GET | /api/roles | Lista roles |
| POST | /api/roles | Cria role (201) |

Exemplo no PowerShell:

```
Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method Post -ContentType "application/json" -Body '{"login":"admin","senha":"admin123"}'
```

Os erros voltam em JSON:
`{"status": 404, "erro": "Not Found", "mensagem": "...", "timestamp": "..."}`

## Onde está cada passo do quadro

| Passo | Onde |
|---|---|
| 1. 3 métodos no model | model/Usuario.java: adicionarRole, removerRole, possuiRole |
| 2. Service | service/UsuarioServiceImpl.java, AuthService.java, RoleService.java |
| 3. Controller REST | controller/ (@RestController + @RequestMapping) |
| 4. Interface | service/UsuarioService.java |
| 5. Comunicação com MySQL | src/main/resources/application.properties |
| 6. Interface com o banco | repository/ (interfaces que estendem JpaRepository) |

## Onde está cada conceito de POO

| Conceito | Onde |
|---|---|
| Herança | Pessoa (abstrata) → Usuario (abstrata) → Administrador / Cliente |
| Polimorfismo (sobrescrita) | descreverPerfil(), podeGerenciarUsuarios(), getTipo(); usado em UsuarioResponse.de ao listar |
| Polimorfismo (sobrecarga) | Usuario.possuiRole(String) e possuiRole(Role) |
| Agregação | Usuario ↔ Role (@ManyToMany): a role existe sozinha |
| Associação | RegistroAcesso → Usuario (@ManyToOne), gravado a cada login |
| Interface | UsuarioService / UsuarioServiceImpl; repositórios |
| try/catch | UsuarioServiceImpl.criar/atualizar/remover |
| Exceções + tratamento global | pacote exception/ e GlobalExceptionHandler |
| Logs | @Slf4j nos services, controllers e GlobalExceptionHandler |

Senhas são guardadas com hash BCrypt e nunca aparecem nas respostas da API.

## Importação de usuários por arquivo

O arquivo fica na raiz do projeto e o caminho é configurado em `application.properties`:

```
app.usuarios.arquivo=usuarios.txt
```

**Formato:** uma linha por usuário, campos separados por `;`, nesta ordem:

```
tipo;login;nome;email;senha;roles
CLIENTE;maria;Maria Silva;maria@escola.com;senha123;ROLE_USER
ADMIN;joao;Joao Souza;joao@escola.com;senha123;ROLE_ADMIN,ROLE_USER
```

- `tipo` só pode ser `ADMIN` ou `CLIENTE`.
- `roles` são nomes já cadastrados, separados por vírgula.
- Linha vazia e linha começando com `#` são ignoradas (o cabeçalho também).

**O que é erro de uma linha** (a leitura continua nas próximas): quantidade de campos diferente de 6, tipo desconhecido, e-mail inválido, campo obrigatório vazio, senha com menos de 6 caracteres, login já cadastrado, role inexistente.

**O que é erro do arquivo inteiro:** arquivo inexistente (404) ou sem permissão de leitura.

**Como importar:** `POST /api/usuarios/importar`

```
Invoke-RestMethod -Uri http://localhost:8080/api/usuarios/importar -Method Post
```

A resposta traz o total de linhas lidas, quantas foram gravadas e a lista de falhas (número da linha e motivo).

**Cadastro por JSON:** `POST /api/usuarios`, com os mesmos campos (`roles` é uma lista).

O arquivo e o JSON usam o **mesmo método** `UsuarioService.criar`, então BCrypt, login único e verificação de roles são as mesmas regras nos dois caminhos.