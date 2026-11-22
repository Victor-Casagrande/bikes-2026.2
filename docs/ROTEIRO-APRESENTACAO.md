# Roteiro de apresentação — Bikes (Locadora de Bicicletas)

Este roteiro mostra, requisito por requisito da descrição do projeto (`descricao-projeto-bikes.pdf`), **o que executar e qual resposta deve aparecer**. As respostas abaixo são reais: foram capturadas executando o roteiro inteiro na API, começando com o banco vazio (67 passos, todos com o resultado esperado).

- Para executar: Swagger em <http://localhost:8080/docs-bikes.html> ou a coleção do Postman em [`postman/Bikes.postman_collection.json`](../postman/Bikes.postman_collection.json).
- A coluna **Token** diz quem deve estar autenticado (botão **Authorize** do Swagger, com o token obtido em `POST /api/v1/auth`).
- Datas, ids e tokens variam a cada execução; o que importa é o **código HTTP** e a **estrutura** da resposta.
- Senha de todos os usuários do roteiro: `123456`.

## 0. Preparação (antes de chamar o professor)

1. **MySQL ligado:** serviço do MySQL em execução e o Workbench conectando com o usuário configurado no perfil `local`.
2. **Banco vazio (recomendado):** para os ids baterem com este roteiro, apague o banco no Workbench — a API recria as tabelas ao subir:
   ```sql
   DROP DATABASE IF EXISTS bikes;
   ```
3. **Subir a API** (PowerShell, na pasta do projeto; `JAVA_HOME` deve apontar para um Java 25):
   ```
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```
   Ou, no IntelliJ, executar `BikesApplication` com o perfil `local` ativo.
   Pronto quando aparecer `Started BikesApplication`.
4. Abrir o Swagger: <http://localhost:8080/docs-bikes.html>.
5. **O token expira em 20 minutos.** Se uma chamada que funcionava passar a dar `403`, faça o login de novo.

## 1. Requisitos de configuração (Requisito 1)

| O que o PDF pede | Como mostrar |
|---|---|
| Timezone do país | No log da API, os horários aparecem com `-03:00` (classe `SpringTimezoneConfig`, `America/Sao_Paulo`). As datas das locações saem no horário de Brasília. |
| Locale do país | `spring.web.locale=pt_BR` no `application.properties`; as mensagens de validação saem em português (ex.: passo U6). |
| Sistema de auditoria (data de criação/modificação e usuário que criou/modificou) | Depois do roteiro, rodar no Workbench: `SELECT codigo, status, data_criacao, criado_por, data_modificacao, modificado_por FROM bikes.bicicletas;` — `criado_por` = `admin@bikes.com`. Nas locações, `criado_por` = e-mail do cliente. |
| Base de dados para desenvolvimento | MySQL local (banco `bikes`), configurado no `application.properties` / perfil `local`; tabelas criadas pelo JPA. |
| API REST com autenticação JWT | Passos U8 a U12 (login) e todos os `403` sem token. |
| Documentação para o front-end | Swagger em `/docs-bikes.html`, com todos os recursos, exemplos e códigos de resposta. |

## 2. Testes automatizados (end-to-end)

```
.\mvnw.cmd test "-Dtest=UsuarioIT,ClienteIT,BicicletaIT,LocacaoIT"
```
Resultado esperado (usa banco H2 em memória; não mexe no MySQL):
```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.ifc.bikes.BicicletaIT
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.ifc.bikes.ClienteIT
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.ifc.bikes.LocacaoIT
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0 -- in br.edu.ifc.bikes.UsuarioIT
Tests run: 68, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Classe | Requisitos | O que testa |
|---|---|---|
| `UsuarioIT` | 3 | criação (válida, e-mail repetido, inválida), perfil ADMIN, busca por id, alteração de senha (4 casos), listagem só pelo admin |
| `ClienteIT` | 4 | cadastro (válido, sem token, admin, CPF repetido, usuário já cliente, inválido), busca por id, listagem, próprios dados, relatório PDF |
| `BicicletaIT` | 5 | cadastro (válido, manutenção, cliente, sem token, código repetido, inválido), busca, listagem, alteração de status |
| `LocacaoIT` | 6, 7, 8 | solicitação (válida, sem bicicleta livre, sem cadastro, admin, sem token, dados inválidos), consultas, devolução (valor, manutenção, já finalizada, inexistente, outro cliente) e relatório |

## Requisito 3 — Usuário

| O que o PDF pede | Passos |
|---|---|
| E-mail como username, único | U2, U5 |
| Senha de 6 caracteres | U6 |
| Perfil de administrador ou cliente | U1, U2, U7 |
| Criado sem necessidade de autenticação | U1 a U4 (sem token) |
| Localizado pelo identificador gerado | U13 a U16 |
| Pode alterar a senha | U17 a U20 |
| Admin autenticado lista todos os usuários | U21, U22 |
| Documentar os recursos | Swagger: seção *Usuarios* |
| Testes end-to-end | UsuarioIT (17 testes) |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| U1 | Criar o primeiro ADMIN (sem token, pois ainda não há admin) | sem token | `POST /api/v1/usuarios` | **201** |
| U2 | Criar usuário CLIENTE (sem token) | sem token | `POST /api/v1/usuarios` | **201** |
| U3 | Criar outro usuário CLIENTE | sem token | `POST /api/v1/usuarios` | **201** |
| U4 | Criar usuário que não fará cadastro de cliente | sem token | `POST /api/v1/usuarios` | **201** |
| U5 | E-mail repetido | sem token | `POST /api/v1/usuarios` | **409** |
| U6 | E-mail inválido e senha com menos de 6 caracteres | sem token | `POST /api/v1/usuarios` | **422** |
| U7 | Tentar criar ADMIN sem ser admin (já existe um admin) | sem token | `POST /api/v1/usuarios` | **403** |
| U8 | Login de admin@bikes.com | sem token | `POST /api/v1/auth` | **200** |
| U9 | Login de maria@bikes.com | sem token | `POST /api/v1/auth` | **200** |
| U10 | Login de ana@bikes.com | sem token | `POST /api/v1/auth` | **200** |
| U11 | Login de joao@bikes.com | sem token | `POST /api/v1/auth` | **200** |
| U12 | Login com senha errada | sem token | `POST /api/v1/auth` | **400** |
| U13 | Localizar usuário pelo id (ADMIN) | ADMIN (admin@bikes.com) | `GET /api/v1/usuarios/2` | **200** |
| U14 | Cliente localiza o próprio usuário | CLIENTE maria@bikes.com | `GET /api/v1/usuarios/2` | **200** |
| U15 | Cliente tenta ver outro usuário | CLIENTE maria@bikes.com | `GET /api/v1/usuarios/1` | **403** |
| U16 | Id inexistente | ADMIN (admin@bikes.com) | `GET /api/v1/usuarios/999` | **404** |
| U17 | Alterar a própria senha | CLIENTE maria@bikes.com | `PATCH /api/v1/usuarios/2` | **204** |
| U18 | Alterar senha com a senha atual errada | CLIENTE maria@bikes.com | `PATCH /api/v1/usuarios/2` | **400** |
| U19 | Nova senha e confirmação diferentes | CLIENTE maria@bikes.com | `PATCH /api/v1/usuarios/2` | **400** |
| U20 | Voltar a senha para 123456 (para o resto do roteiro) | CLIENTE maria@bikes.com | `PATCH /api/v1/usuarios/2` | **204** |
| U21 | ADMIN lista todos os usuários | ADMIN (admin@bikes.com) | `GET /api/v1/usuarios` | **200** |
| U22 | CLIENTE tenta listar usuários | CLIENTE maria@bikes.com | `GET /api/v1/usuarios` | **403** |

#### U1 — Criar o primeiro ADMIN (sem token, pois ainda não há admin)
`POST /api/v1/usuarios` · Token: sem token · Esperado: **201**

Corpo da requisição:
```json
{
  "username": "admin@bikes.com",
  "password": "123456",
  "role": "ADMIN"
}
```
Resposta (`201`):
```json
{
  "id": 1,
  "username": "admin@bikes.com",
  "role": "ADMIN"
}
```

#### U2 — Criar usuário CLIENTE (sem token)
`POST /api/v1/usuarios` · Token: sem token · Esperado: **201**

Corpo da requisição:
```json
{
  "username": "maria@bikes.com",
  "password": "123456"
}
```
Resposta (`201`):
```json
{
  "id": 2,
  "username": "maria@bikes.com",
  "role": "CLIENTE"
}
```

#### U3 — Criar outro usuário CLIENTE
`POST /api/v1/usuarios` · Token: sem token · Esperado: **201**

Corpo da requisição:
```json
{
  "username": "ana@bikes.com",
  "password": "123456"
}
```
Resposta (`201`):
```json
{
  "id": 3,
  "username": "ana@bikes.com",
  "role": "CLIENTE"
}
```

#### U4 — Criar usuário que não fará cadastro de cliente
`POST /api/v1/usuarios` · Token: sem token · Esperado: **201**

Corpo da requisição:
```json
{
  "username": "joao@bikes.com",
  "password": "123456"
}
```
Resposta (`201`):
```json
{
  "id": 4,
  "username": "joao@bikes.com",
  "role": "CLIENTE"
}
```

#### U5 — E-mail repetido
`POST /api/v1/usuarios` · Token: sem token · Esperado: **409**

Corpo da requisição:
```json
{
  "username": "maria@bikes.com",
  "password": "123456"
}
```
Resposta (`409`):
```json
{
  "message": "O nome do usuário 'maria@bikes.com' já existe",
  "method": "POST",
  "path": "/api/v1/usuarios",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### U6 — E-mail inválido e senha com menos de 6 caracteres
`POST /api/v1/usuarios` · Token: sem token · Esperado: **422**

Corpo da requisição:
```json
{
  "username": "maria@",
  "password": "123"
}
```
Resposta (`422`):
```json
{
  "errors": {
    "password": "A senha deve ter 6 caracteres",
    "username": "O email deve ser válido"
  },
  "message": "Campo(s) inválido(s)",
  "method": "POST",
  "path": "/api/v1/usuarios",
  "status": 422,
  "statusMessage": "Unprocessable Content"
}
```

#### U7 — Tentar criar ADMIN sem ser admin (já existe um admin)
`POST /api/v1/usuarios` · Token: sem token · Esperado: **403**

Corpo da requisição:
```json
{
  "username": "intruso@bikes.com",
  "password": "123456",
  "role": "ADMIN"
}
```
Resposta (`403`):
```json
{
  "message": "Apenas um administrador autenticado pode cadastrar outro administrador",
  "method": "POST",
  "path": "/api/v1/usuarios",
  "status": 403,
  "statusMessage": "Forbidden"
}
```

#### U8 — Login de admin@bikes.com
`POST /api/v1/auth` · Token: sem token · Esperado: **200**

Corpo da requisição:
```json
{
  "username": "admin@bikes.com",
  "password": "123456"
}
```
Resposta (`200`):
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJh..."
}
```

#### U9 — Login de maria@bikes.com
`POST /api/v1/auth` · Token: sem token · Esperado: **200**

Corpo da requisição:
```json
{
  "username": "maria@bikes.com",
  "password": "123456"
}
```
Resposta (`200`):
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJh..."
}
```

#### U10 — Login de ana@bikes.com
`POST /api/v1/auth` · Token: sem token · Esperado: **200**

Corpo da requisição:
```json
{
  "username": "ana@bikes.com",
  "password": "123456"
}
```
Resposta (`200`):
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJh..."
}
```

#### U11 — Login de joao@bikes.com
`POST /api/v1/auth` · Token: sem token · Esperado: **200**

Corpo da requisição:
```json
{
  "username": "joao@bikes.com",
  "password": "123456"
}
```
Resposta (`200`):
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJh..."
}
```

#### U12 — Login com senha errada
`POST /api/v1/auth` · Token: sem token · Esperado: **400**

Corpo da requisição:
```json
{
  "username": "maria@bikes.com",
  "password": "000000"
}
```
Resposta (`400`):
```json
{
  "message": "Credenciais inválidas",
  "method": "POST",
  "path": "/api/v1/auth",
  "status": 400,
  "statusMessage": "Bad Request"
}
```

#### U13 — Localizar usuário pelo id (ADMIN)
`GET /api/v1/usuarios/2` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 2,
  "username": "maria@bikes.com",
  "role": "CLIENTE"
}
```

#### U14 — Cliente localiza o próprio usuário
`GET /api/v1/usuarios/2` · Token: CLIENTE maria@bikes.com · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 2,
  "username": "maria@bikes.com",
  "role": "CLIENTE"
}
```

#### U15 — Cliente tenta ver outro usuário
`GET /api/v1/usuarios/1` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

#### U16 — Id inexistente
`GET /api/v1/usuarios/999` · Token: ADMIN (admin@bikes.com) · Esperado: **404**

Resposta (`404`):
```json
{
  "message": "Usuário com id=999 não encontrado",
  "method": "GET",
  "path": "/api/v1/usuarios/999",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### U17 — Alterar a própria senha
`PATCH /api/v1/usuarios/2` · Token: CLIENTE maria@bikes.com · Esperado: **204**

Corpo da requisição:
```json
{
  "novaSenha": "654321",
  "confirmaSenha": "654321",
  "senhaAtual": "123456"
}
```
Resposta: `204` sem corpo.

#### U18 — Alterar senha com a senha atual errada
`PATCH /api/v1/usuarios/2` · Token: CLIENTE maria@bikes.com · Esperado: **400**

Corpo da requisição:
```json
{
  "novaSenha": "111111",
  "confirmaSenha": "111111",
  "senhaAtual": "000000"
}
```
Resposta (`400`):
```json
{
  "message": "Senha atual incorreta",
  "method": "PATCH",
  "path": "/api/v1/usuarios/2",
  "status": 400,
  "statusMessage": "Bad Request"
}
```

#### U19 — Nova senha e confirmação diferentes
`PATCH /api/v1/usuarios/2` · Token: CLIENTE maria@bikes.com · Esperado: **400**

Corpo da requisição:
```json
{
  "novaSenha": "111111",
  "confirmaSenha": "222222",
  "senhaAtual": "654321"
}
```
Resposta (`400`):
```json
{
  "message": "As senhas não coincidem",
  "method": "PATCH",
  "path": "/api/v1/usuarios/2",
  "status": 400,
  "statusMessage": "Bad Request"
}
```

#### U20 — Voltar a senha para 123456 (para o resto do roteiro)
`PATCH /api/v1/usuarios/2` · Token: CLIENTE maria@bikes.com · Esperado: **204**

Corpo da requisição:
```json
{
  "novaSenha": "123456",
  "confirmaSenha": "123456",
  "senhaAtual": "654321"
}
```
Resposta: `204` sem corpo.

#### U21 — ADMIN lista todos os usuários
`GET /api/v1/usuarios` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
[
  {
    "id": 1,
    "username": "admin@bikes.com",
    "role": "ADMIN"
  },
  {
    "id": 2,
    "username": "maria@bikes.com",
    "role": "CLIENTE"
  },
  {
    "id": 3,
    "username": "ana@bikes.com",
    "role": "CLIENTE"
  },
  {
    "id": 4,
    "username": "joao@bikes.com",
    "role": "CLIENTE"
  }
]
```

#### U22 — CLIENTE tenta listar usuários
`GET /api/v1/usuarios` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

## Requisito 4 — Cliente

| O que o PDF pede | Passos |
|---|---|
| Todas as ações necessitam de autenticação | C3 |
| Cadastro com nome completo e CPF único | C1, C2, C5, C7 |
| Cadastro vinculado ao usuário (1 usuário = 1 cliente) | C6 |
| Apenas o cliente pode se cadastrar | C4 |
| Localizado pelo id — apenas o administrador | C8 a C10 |
| Administrador lista todos os clientes | C11 |
| Cliente recupera os próprios dados | C12, C13 |
| Relatório em PDF (README do professor — Jasper) | C14 |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| C1 | Cliente faz o cadastro (nome completo e CPF) | CLIENTE maria@bikes.com | `POST /api/v1/clientes` | **201** |
| C2 | Outro cliente faz o cadastro | CLIENTE ana@bikes.com | `POST /api/v1/clientes` | **201** |
| C3 | Cadastro sem token | sem token | `POST /api/v1/clientes` | **403** |
| C4 | ADMIN tenta fazer cadastro de cliente | ADMIN (admin@bikes.com) | `POST /api/v1/clientes` | **403** |
| C5 | CPF já cadastrado | joao@bikes.com (sem cadastro de cliente) | `POST /api/v1/clientes` | **409** |
| C6 | Usuário que já tem cadastro de cliente | CLIENTE maria@bikes.com | `POST /api/v1/clientes` | **409** |
| C7 | CPF inválido e nome curto | joao@bikes.com (sem cadastro de cliente) | `POST /api/v1/clientes` | **422** |
| C8 | ADMIN localiza cliente pelo id | ADMIN (admin@bikes.com) | `GET /api/v1/clientes/1` | **200** |
| C9 | CLIENTE tenta localizar pelo id | CLIENTE maria@bikes.com | `GET /api/v1/clientes/1` | **403** |
| C10 | Id de cliente inexistente | ADMIN (admin@bikes.com) | `GET /api/v1/clientes/999` | **404** |
| C11 | ADMIN lista todos os clientes | ADMIN (admin@bikes.com) | `GET /api/v1/clientes` | **200** |
| C12 | Cliente recupera os próprios dados | CLIENTE maria@bikes.com | `GET /api/v1/clientes/detalhes` | **200** |
| C13 | Usuário sem cadastro de cliente pede os próprios dados | joao@bikes.com (sem cadastro de cliente) | `GET /api/v1/clientes/detalhes` | **404** |
| C14 | ADMIN gera o relatório de clientes em PDF | ADMIN (admin@bikes.com) | `GET /api/v1/clientes/relatorio` | **200** |

#### C1 — Cliente faz o cadastro (nome completo e CPF)
`POST /api/v1/clientes` · Token: CLIENTE maria@bikes.com · Esperado: **201**

Corpo da requisição:
```json
{
  "nome": "Maria da Silva",
  "cpf": "52998224725"
}
```
Resposta (`201`):
```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "cpf": "52998224725"
}
```

#### C2 — Outro cliente faz o cadastro
`POST /api/v1/clientes` · Token: CLIENTE ana@bikes.com · Esperado: **201**

Corpo da requisição:
```json
{
  "nome": "Ana Souza",
  "cpf": "11144477735"
}
```
Resposta (`201`):
```json
{
  "id": 2,
  "nome": "Ana Souza",
  "cpf": "11144477735"
}
```

#### C3 — Cadastro sem token
`POST /api/v1/clientes` · Token: sem token · Esperado: **403**

Corpo da requisição:
```json
{
  "nome": "Fulano de Tal",
  "cpf": "39053344705"
}
```
Resposta: `403` sem corpo.

#### C4 — ADMIN tenta fazer cadastro de cliente
`POST /api/v1/clientes` · Token: ADMIN (admin@bikes.com) · Esperado: **403**

Corpo da requisição:
```json
{
  "nome": "Fulano de Tal",
  "cpf": "39053344705"
}
```
Resposta: `403` sem corpo.

#### C5 — CPF já cadastrado
`POST /api/v1/clientes` · Token: joao@bikes.com (sem cadastro de cliente) · Esperado: **409**

Corpo da requisição:
```json
{
  "nome": "João Pereira",
  "cpf": "52998224725"
}
```
Resposta (`409`):
```json
{
  "message": "O CPF '52998224725' já está cadastrado",
  "method": "POST",
  "path": "/api/v1/clientes",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### C6 — Usuário que já tem cadastro de cliente
`POST /api/v1/clientes` · Token: CLIENTE maria@bikes.com · Esperado: **409**

Corpo da requisição:
```json
{
  "nome": "Maria da Silva",
  "cpf": "39053344705"
}
```
Resposta (`409`):
```json
{
  "message": "O usuário 'maria@bikes.com' já possui um cliente cadastrado",
  "method": "POST",
  "path": "/api/v1/clientes",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### C7 — CPF inválido e nome curto
`POST /api/v1/clientes` · Token: joao@bikes.com (sem cadastro de cliente) · Esperado: **422**

Corpo da requisição:
```json
{
  "nome": "Jo",
  "cpf": "12345678900"
}
```
Resposta (`422`):
```json
{
  "errors": {
    "cpf": "O CPF é inválido",
    "nome": "O nome deve ter entre 5 e 100 caracteres"
  },
  "message": "Campo(s) inválido(s)",
  "method": "POST",
  "path": "/api/v1/clientes",
  "status": 422,
  "statusMessage": "Unprocessable Content"
}
```

#### C8 — ADMIN localiza cliente pelo id
`GET /api/v1/clientes/1` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "cpf": "52998224725"
}
```

#### C9 — CLIENTE tenta localizar pelo id
`GET /api/v1/clientes/1` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

#### C10 — Id de cliente inexistente
`GET /api/v1/clientes/999` · Token: ADMIN (admin@bikes.com) · Esperado: **404**

Resposta (`404`):
```json
{
  "message": "Cliente com id=999 não encontrado",
  "method": "GET",
  "path": "/api/v1/clientes/999",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### C11 — ADMIN lista todos os clientes
`GET /api/v1/clientes` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
[
  {
    "id": 1,
    "nome": "Maria da Silva",
    "cpf": "52998224725"
  },
  {
    "id": 2,
    "nome": "Ana Souza",
    "cpf": "11144477735"
  }
]
```

#### C12 — Cliente recupera os próprios dados
`GET /api/v1/clientes/detalhes` · Token: CLIENTE maria@bikes.com · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "cpf": "52998224725"
}
```

#### C13 — Usuário sem cadastro de cliente pede os próprios dados
`GET /api/v1/clientes/detalhes` · Token: joao@bikes.com (sem cadastro de cliente) · Esperado: **404**

Resposta (`404`):
```json
{
  "message": "O usuário autenticado ainda não possui um cliente cadastrado",
  "method": "GET",
  "path": "/api/v1/clientes/detalhes",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### C14 — ADMIN gera o relatório de clientes em PDF
`GET /api/v1/clientes/relatorio` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta: `200` — (arquivo PDF, 41411 bytes, Content-Type: application/pdf)

## Requisito 5 — Bicicleta

| O que o PDF pede | Passos |
|---|---|
| Todas as ações exigem autenticação e são restritas ao administrador | B6, B9 |
| Código único que não é o id | B1, B4 |
| Status livre, ocupada (ALUGADA) ou em manutenção | B1 a B3, B11, B12 |
| Localizada pelo código | B7, B8 |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| B1 | ADMIN cadastra bicicleta LIVRE | ADMIN (admin@bikes.com) | `POST /api/v1/bicicletas` | **201** |
| B2 | ADMIN cadastra outra bicicleta | ADMIN (admin@bikes.com) | `POST /api/v1/bicicletas` | **201** |
| B3 | ADMIN cadastra bicicleta em manutenção | ADMIN (admin@bikes.com) | `POST /api/v1/bicicletas` | **201** |
| B4 | Código repetido | ADMIN (admin@bikes.com) | `POST /api/v1/bicicletas` | **409** |
| B5 | Dados inválidos (código curto e status ALUGADA) | ADMIN (admin@bikes.com) | `POST /api/v1/bicicletas` | **422** |
| B6 | CLIENTE tenta cadastrar | CLIENTE maria@bikes.com | `POST /api/v1/bicicletas` | **403** |
| B7 | Localizar bicicleta pelo código | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas/BK01` | **200** |
| B8 | Código inexistente | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas/BK99` | **404** |
| B9 | CLIENTE tenta consultar (restrito ao ADMIN) | CLIENTE maria@bikes.com | `GET /api/v1/bicicletas/BK01` | **403** |
| B10 | ADMIN lista as bicicletas | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas` | **200** |
| B11 | Colocar BK03 como LIVRE | ADMIN (admin@bikes.com) | `PATCH /api/v1/bicicletas/BK03/status` | **200** |
| B12 | Voltar BK03 para MANUTENCAO | ADMIN (admin@bikes.com) | `PATCH /api/v1/bicicletas/BK03/status` | **200** |

#### B1 — ADMIN cadastra bicicleta LIVRE
`POST /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **201**

Corpo da requisição:
```json
{
  "codigo": "BK01",
  "modelo": "Caloi Explorer",
  "status": "LIVRE"
}
```
Resposta (`201`):
```json
{
  "id": 1,
  "codigo": "BK01",
  "modelo": "Caloi Explorer",
  "status": "LIVRE"
}
```

#### B2 — ADMIN cadastra outra bicicleta
`POST /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **201**

Corpo da requisição:
```json
{
  "codigo": "BK02",
  "modelo": "Sense Impact",
  "status": "LIVRE"
}
```
Resposta (`201`):
```json
{
  "id": 2,
  "codigo": "BK02",
  "modelo": "Sense Impact",
  "status": "LIVRE"
}
```

#### B3 — ADMIN cadastra bicicleta em manutenção
`POST /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **201**

Corpo da requisição:
```json
{
  "codigo": "BK03",
  "modelo": "Oggi Hacker",
  "status": "MANUTENCAO"
}
```
Resposta (`201`):
```json
{
  "id": 3,
  "codigo": "BK03",
  "modelo": "Oggi Hacker",
  "status": "MANUTENCAO"
}
```

#### B4 — Código repetido
`POST /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **409**

Corpo da requisição:
```json
{
  "codigo": "BK01",
  "modelo": "Outra",
  "status": "LIVRE"
}
```
Resposta (`409`):
```json
{
  "message": "A bicicleta com código 'BK01' já está cadastrada",
  "method": "POST",
  "path": "/api/v1/bicicletas",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### B5 — Dados inválidos (código curto e status ALUGADA)
`POST /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **422**

Corpo da requisição:
```json
{
  "codigo": "B1",
  "modelo": "Caloi",
  "status": "ALUGADA"
}
```
Resposta (`422`):
```json
{
  "errors": {
    "codigo": "O código deve ter entre 4 e 10 caracteres",
    "status": "O status deve ser LIVRE ou MANUTENCAO"
  },
  "message": "Campo(s) inválido(s)",
  "method": "POST",
  "path": "/api/v1/bicicletas",
  "status": 422,
  "statusMessage": "Unprocessable Content"
}
```

#### B6 — CLIENTE tenta cadastrar
`POST /api/v1/bicicletas` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Corpo da requisição:
```json
{
  "codigo": "BK09",
  "modelo": "Caloi Elite",
  "status": "LIVRE"
}
```
Resposta: `403` sem corpo.

#### B7 — Localizar bicicleta pelo código
`GET /api/v1/bicicletas/BK01` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "codigo": "BK01",
  "modelo": "Caloi Explorer",
  "status": "LIVRE"
}
```

#### B8 — Código inexistente
`GET /api/v1/bicicletas/BK99` · Token: ADMIN (admin@bikes.com) · Esperado: **404**

Resposta (`404`):
```json
{
  "message": "Bicicleta com código 'BK99' não encontrada",
  "method": "GET",
  "path": "/api/v1/bicicletas/BK99",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### B9 — CLIENTE tenta consultar (restrito ao ADMIN)
`GET /api/v1/bicicletas/BK01` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

#### B10 — ADMIN lista as bicicletas
`GET /api/v1/bicicletas` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
[
  {
    "id": 1,
    "codigo": "BK01",
    "modelo": "Caloi Explorer",
    "status": "LIVRE"
  },
  {
    "id": 2,
    "codigo": "BK02",
    "modelo": "Sense Impact",
    "status": "LIVRE"
  },
  {
    "id": 3,
    "codigo": "BK03",
    "modelo": "Oggi Hacker",
    "status": "MANUTENCAO"
  }
]
```

#### B11 — Colocar BK03 como LIVRE
`PATCH /api/v1/bicicletas/BK03/status` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Corpo da requisição:
```json
{
  "status": "LIVRE"
}
```
Resposta (`200`):
```json
{
  "id": 3,
  "codigo": "BK03",
  "modelo": "Oggi Hacker",
  "status": "LIVRE"
}
```

#### B12 — Voltar BK03 para MANUTENCAO
`PATCH /api/v1/bicicletas/BK03/status` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Corpo da requisição:
```json
{
  "status": "MANUTENCAO"
}
```
Resposta (`200`):
```json
{
  "id": 3,
  "codigo": "BK03",
  "modelo": "Oggi Hacker",
  "status": "MANUTENCAO"
}
```

## Requisito 6 — Locações

| O que o PDF pede | Passos |
|---|---|
| Cliente solicita o aluguel | L1, L3 |
| Sistema verifica se existem bicicletas disponíveis | L1 (escolhe uma livre), L4 (nenhuma livre) |
| Cliente informa os dados do cartão de crédito | L1 (válido), L7 (inválido) |
| Registra data/hora de retirada e data/hora prevista de devolução | L1 |
| Registra data/hora efetiva de devolução | D1 (requisito 7) |
| Bicicleta não participa de duas locações simultâneas | L2, L3, L4 |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| L1 | Cliente solicita locação (sistema escolhe uma livre) | CLIENTE maria@bikes.com | `POST /api/v1/locacoes` | **201** |
| L2 | Bicicleta escolhida ficou ALUGADA | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas/BK01` | **200** |
| L3 | Outro cliente solicita (recebe a próxima livre) | CLIENTE ana@bikes.com | `POST /api/v1/locacoes` | **201** |
| L4 | Sem bicicletas disponíveis (BK03 está em manutenção) | CLIENTE maria@bikes.com | `POST /api/v1/locacoes` | **409** |
| L5 | ADMIN tenta solicitar locação | ADMIN (admin@bikes.com) | `POST /api/v1/locacoes` | **403** |
| L6 | Usuário sem cadastro de cliente | joao@bikes.com (sem cadastro de cliente) | `POST /api/v1/locacoes` | **404** |
| L7 | Cartão inválido e data no passado | CLIENTE maria@bikes.com | `POST /api/v1/locacoes` | **422** |
| L8 | Cliente lista as próprias locações | CLIENTE maria@bikes.com | `GET /api/v1/locacoes/minhas` | **200** |
| L9 | Cliente tenta ver locação de outro | CLIENTE maria@bikes.com | `GET /api/v1/locacoes/2` | **403** |

#### L1 — Cliente solicita locação (sistema escolhe uma livre)
`POST /api/v1/locacoes` · Token: CLIENTE maria@bikes.com · Esperado: **201**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta (`201`):
```json
{
  "id": 1,
  "nomeCliente": "Maria da Silva",
  "codigoBicicleta": "BK01",
  "dataRetirada": "22/11/2026 10:21",
  "dataPrevistaDevolucao": "23/11/2026 18:00",
  "dataDevolucao": null,
  "duracaoMinutos": 0,
  "valor": null,
  "status": "ATIVA",
  "cartaoFinal": "1111"
}
```

#### L2 — Bicicleta escolhida ficou ALUGADA
`GET /api/v1/bicicletas/BK01` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "codigo": "BK01",
  "modelo": "Caloi Explorer",
  "status": "ALUGADA"
}
```

#### L3 — Outro cliente solicita (recebe a próxima livre)
`POST /api/v1/locacoes` · Token: CLIENTE ana@bikes.com · Esperado: **201**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta (`201`):
```json
{
  "id": 2,
  "nomeCliente": "Ana Souza",
  "codigoBicicleta": "BK02",
  "dataRetirada": "22/11/2026 10:21",
  "dataPrevistaDevolucao": "23/11/2026 18:00",
  "dataDevolucao": null,
  "duracaoMinutos": 0,
  "valor": null,
  "status": "ATIVA",
  "cartaoFinal": "1111"
}
```

#### L4 — Sem bicicletas disponíveis (BK03 está em manutenção)
`POST /api/v1/locacoes` · Token: CLIENTE maria@bikes.com · Esperado: **409**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta (`409`):
```json
{
  "message": "Não há bicicletas disponíveis para locação no momento",
  "method": "POST",
  "path": "/api/v1/locacoes",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### L5 — ADMIN tenta solicitar locação
`POST /api/v1/locacoes` · Token: ADMIN (admin@bikes.com) · Esperado: **403**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta: `403` sem corpo.

#### L6 — Usuário sem cadastro de cliente
`POST /api/v1/locacoes` · Token: joao@bikes.com (sem cadastro de cliente) · Esperado: **404**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta (`404`):
```json
{
  "message": "O usuário autenticado ainda não possui um cliente cadastrado",
  "method": "POST",
  "path": "/api/v1/locacoes",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### L7 — Cartão inválido e data no passado
`POST /api/v1/locacoes` · Token: CLIENTE maria@bikes.com · Esperado: **422**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2020-01-01T10:00:00",
  "cartao": {
    "numero": "4111111111111112",
    "titular": "Maria",
    "validade": "01/20",
    "cvv": "12"
  }
}
```
Resposta (`422`):
```json
{
  "errors": {
    "dataPrevistaDevolucao": "A data/hora prevista para devolução deve ser futura",
    "cartao.cvv": "O CVV deve ter 3 ou 4 dígitos",
    "cartao.dentroDaValidade": "O cartão está vencido",
    "cartao.numero": "O número do cartão é inválido"
  },
  "message": "Campo(s) inválido(s)",
  "method": "POST",
  "path": "/api/v1/locacoes",
  "status": 422,
  "statusMessage": "Unprocessable Content"
}
```

#### L8 — Cliente lista as próprias locações
`GET /api/v1/locacoes/minhas` · Token: CLIENTE maria@bikes.com · Esperado: **200**

Resposta (`200`):
```json
[
  {
    "id": 1,
    "nomeCliente": "Maria da Silva",
    "codigoBicicleta": "BK01",
    "dataRetirada": "22/11/2026 10:21",
    "dataPrevistaDevolucao": "23/11/2026 18:00",
    "dataDevolucao": null,
    "duracaoMinutos": 0,
    "valor": null,
    "status": "ATIVA",
    "cartaoFinal": "1111"
  }
]
```

#### L9 — Cliente tenta ver locação de outro
`GET /api/v1/locacoes/2` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

## Requisito 7 — Devoluções

| O que o PDF pede | Passos |
|---|---|
| Registrar a devolução | D1 |
| Calcular o tempo de uso e apresentar o valor a debitar no cartão | D1 (duracaoMinutos e valor) |
| Registrar a data/hora efetiva de devolução | D1 (dataDevolucao) |
| Atualizar o status da bicicleta para livre automaticamente | D2 |
| Se houver problema, permitir status manutenção | D3, D4 |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| D1 | Cliente devolve a bicicleta | CLIENTE maria@bikes.com | `PATCH /api/v1/locacoes/1/devolucao` | **200** |
| D2 | Bicicleta voltou a LIVRE automaticamente | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas/BK01` | **200** |
| D3 | ADMIN devolve informando problema na bicicleta | ADMIN (admin@bikes.com) | `PATCH /api/v1/locacoes/2/devolucao` | **200** |
| D4 | Bicicleta foi para MANUTENCAO | ADMIN (admin@bikes.com) | `GET /api/v1/bicicletas/BK02` | **200** |
| D5 | Devolver de novo uma locação finalizada | CLIENTE maria@bikes.com | `PATCH /api/v1/locacoes/1/devolucao` | **409** |
| D6 | Locação inexistente | ADMIN (admin@bikes.com) | `PATCH /api/v1/locacoes/999/devolucao` | **404** |
| D7 | Nova locação da Maria (para o teste a seguir) | CLIENTE maria@bikes.com | `POST /api/v1/locacoes` | **201** |
| D8 | Outro cliente tenta devolver a locação da Maria | CLIENTE ana@bikes.com | `PATCH /api/v1/locacoes/3/devolucao` | **403** |

#### D1 — Cliente devolve a bicicleta
`PATCH /api/v1/locacoes/1/devolucao` · Token: CLIENTE maria@bikes.com · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "nomeCliente": "Maria da Silva",
  "codigoBicicleta": "BK01",
  "dataRetirada": "22/11/2026 10:21",
  "dataPrevistaDevolucao": "23/11/2026 18:00",
  "dataDevolucao": "22/11/2026 10:21",
  "duracaoMinutos": 0,
  "valor": 10.0,
  "status": "FINALIZADA",
  "cartaoFinal": "1111"
}
```

#### D2 — Bicicleta voltou a LIVRE automaticamente
`GET /api/v1/bicicletas/BK01` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 1,
  "codigo": "BK01",
  "modelo": "Caloi Explorer",
  "status": "LIVRE"
}
```

#### D3 — ADMIN devolve informando problema na bicicleta
`PATCH /api/v1/locacoes/2/devolucao` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Corpo da requisição:
```json
{
  "manutencao": true
}
```
Resposta (`200`):
```json
{
  "id": 2,
  "nomeCliente": "Ana Souza",
  "codigoBicicleta": "BK02",
  "dataRetirada": "22/11/2026 10:21",
  "dataPrevistaDevolucao": "23/11/2026 18:00",
  "dataDevolucao": "22/11/2026 10:21",
  "duracaoMinutos": 0,
  "valor": 10.0,
  "status": "FINALIZADA",
  "cartaoFinal": "1111"
}
```

#### D4 — Bicicleta foi para MANUTENCAO
`GET /api/v1/bicicletas/BK02` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
{
  "id": 2,
  "codigo": "BK02",
  "modelo": "Sense Impact",
  "status": "MANUTENCAO"
}
```

#### D5 — Devolver de novo uma locação finalizada
`PATCH /api/v1/locacoes/1/devolucao` · Token: CLIENTE maria@bikes.com · Esperado: **409**

Resposta (`409`):
```json
{
  "message": "A locação com id=1 já foi finalizada em 22/11/2026 10:21",
  "method": "PATCH",
  "path": "/api/v1/locacoes/1/devolucao",
  "status": 409,
  "statusMessage": "Conflict"
}
```

#### D6 — Locação inexistente
`PATCH /api/v1/locacoes/999/devolucao` · Token: ADMIN (admin@bikes.com) · Esperado: **404**

Resposta (`404`):
```json
{
  "message": "Locação com id=999 não encontrada",
  "method": "PATCH",
  "path": "/api/v1/locacoes/999/devolucao",
  "status": 404,
  "statusMessage": "Not Found"
}
```

#### D7 — Nova locação da Maria (para o teste a seguir)
`POST /api/v1/locacoes` · Token: CLIENTE maria@bikes.com · Esperado: **201**

Corpo da requisição:
```json
{
  "dataPrevistaDevolucao": "2026-11-23T18:00:00",
  "cartao": {
    "numero": "4111111111111111",
    "titular": "Maria da Silva",
    "validade": "12/30",
    "cvv": "123"
  }
}
```
Resposta (`201`):
```json
{
  "id": 3,
  "nomeCliente": "Maria da Silva",
  "codigoBicicleta": "BK01",
  "dataRetirada": "22/11/2026 10:21",
  "dataPrevistaDevolucao": "23/11/2026 18:00",
  "dataDevolucao": null,
  "duracaoMinutos": 0,
  "valor": null,
  "status": "ATIVA",
  "cartaoFinal": "1111"
}
```

#### D8 — Outro cliente tenta devolver a locação da Maria
`PATCH /api/v1/locacoes/3/devolucao` · Token: CLIENTE ana@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

## Requisito 8 — Relatório de locações e devoluções

| O que o PDF pede | Passos |
|---|---|
| Nome do cliente, código da bicicleta, retirada e devolução, duração e status | R1 |
| Restrito ao administrador | R2 |

| Passo | O que fazer | Token | Requisição | Esperado |
|---|---|---|---|---|
| R1 | ADMIN gera o relatório de locações e devoluções | ADMIN (admin@bikes.com) | `GET /api/v1/locacoes/relatorio` | **200** |
| R2 | CLIENTE tenta gerar o relatório | CLIENTE maria@bikes.com | `GET /api/v1/locacoes/relatorio` | **403** |

#### R1 — ADMIN gera o relatório de locações e devoluções
`GET /api/v1/locacoes/relatorio` · Token: ADMIN (admin@bikes.com) · Esperado: **200**

Resposta (`200`):
```json
[
  {
    "id": 3,
    "nomeCliente": "Maria da Silva",
    "codigoBicicleta": "BK01",
    "dataRetirada": "22/11/2026 10:21",
    "dataDevolucao": null,
    "duracaoMinutos": 0,
    "status": "ATIVA"
  },
  {
    "id": 2,
    "nomeCliente": "Ana Souza",
    "codigoBicicleta": "BK02",
    "dataRetirada": "22/11/2026 10:21",
    "dataDevolucao": "22/11/2026 10:21",
    "duracaoMinutos": 0,
    "status": "FINALIZADA"
  },
  {
    "id": 1,
    "nomeCliente": "Maria da Silva",
    "codigoBicicleta": "BK01",
    "dataRetirada": "22/11/2026 10:21",
    "dataDevolucao": "22/11/2026 10:21",
    "duracaoMinutos": 0,
    "status": "FINALIZADA"
  }
]
```

#### R2 — CLIENTE tenta gerar o relatório
`GET /api/v1/locacoes/relatorio` · Token: CLIENTE maria@bikes.com · Esperado: **403**

Resposta: `403` sem corpo.

## Observações para a conversa com o professor

- **Cobrança:** por hora iniciada, mínimo de 1 hora (`bikes.locacao.valor-hora=10.00`). Ex.: 25 min = R$ 10; 1h05 = R$ 20; 3h00 e alguns segundos = R$ 40. No roteiro as devoluções acontecem segundos depois da retirada, por isso `duracaoMinutos` = 0 e valor = R$ 10.
- **Escolha da bicicleta:** o cliente não escolhe; o sistema pega a primeira bicicleta `LIVRE`, com bloqueio no banco (lock pessimista) para que duas solicitações simultâneas não peguem a mesma.
- **Cartão:** validado (número pelo algoritmo de Luhn, validade e CVV); só os 4 últimos dígitos são gravados (`cartaoFinal`).
- **Devolução:** pode ser feita pelo próprio cliente ou pelo admin; com `{"manutencao": true}` a bicicleta vai para `MANUTENCAO`, e o admin a libera depois em `PATCH /api/v1/bicicletas/{codigo}/status`.
- **Primeiro admin:** pode ser criado pela API sem token; a partir daí, só um admin cria outro (passo U7).
