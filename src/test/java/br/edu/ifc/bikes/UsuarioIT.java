package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDto;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/usuarios/usuarios-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/usuarios/usuarios-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UsuarioIT extends RuntimeException {
    @LocalServerPort
    int port;

    WebTestClient testClient;

    @BeforeEach
    void setUp(){
        this.testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
    @Test
    public void criarUsuario_UsernamePasswordInvalidos_RetornaUsuarioCriado(){
        UsuarioResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("fabio@gmail.com", "123456"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.username()).isEqualTo("fabio@gmail.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.role()).isEqualTo("CLIENTE");
    }

    @Test
    public void criarUsuario_UsernameExistente_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();



        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void criarUsuario_UsernameInvalido_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("jose@", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("jose@gmail.", "123456"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void buscarUsuario_IdExistente_RetornaUsuario(){
        UsuarioResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();


        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.id()).isEqualTo(100);
        org.assertj.core.api.Assertions.assertThat(responseBody.username()).isEqualTo("jose@gmail.com");
        org.assertj.core.api.Assertions.assertThat(responseBody.role()).isEqualTo("ADMIN");
    }

    @Test
    public void buscarUsuario_IdInexistente_RetornaUsuario(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/usuarios/110")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void editarSenha_ComDadosValidos_RetornaStatus204(){
        testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("654321", "654321", "123456"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void editarSenha_SenhaAtualInvalida_RetornaErrorMessageStatus400(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("654321", "654321", "000000"))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void editarSenha_SenhaEConfirmacaoNaoConferem_RetornaErrorMessageStatus400(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("654321", "999999", "123456"))
                .exchange()
                .expectStatus().isEqualTo(400)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(400);
    }

    @Test
    public void editarSenha_CamposInvalidos_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("", "", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("12345", "12345", "12345"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .patch()
                .uri("/api/v1/usuarios/100")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioSenhaDto("1234567", "1234567", "1234567"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test //O ADMINISTRADOR AUTENTICADO LISTA TODOS OS USUÁRIOS
    public void listarUsuarios_PeloAdmin_RetornaListaStatus200(){
        java.util.List<UsuarioResponseDTO> responseBody = testClient
                .get()
                .uri("/api/v1/usuarios")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody).hasSize(3);
        org.assertj.core.api.Assertions.assertThat(responseBody)
                .extracting(UsuarioResponseDTO::username)
                .containsExactlyInAnyOrder("jose@gmail.com", "maria@gmail.com", "joao@gmail.com");
    }

    @Test
    public void listarUsuarios_UsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/usuarios")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void listarUsuarios_SemAutenticacao_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/usuarios")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ===== Perfil (role) na criação de usuário

    @Test //UM ADMIN AUTENTICADO PODE CADASTRAR OUTRO ADMIN
    public void criarUsuario_AdminPeloAdmin_RetornaUsuarioAdminStatus201(){
        UsuarioResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new UsuarioRequestDTO("ana.admin@gmail.com", "123456", "ADMIN"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.role()).isEqualTo("ADMIN");
    }

    @Test //JÁ EXISTE ADMIN: SEM TOKEN OU COM TOKEN DE CLIENTE NÃO É POSSÍVEL CRIAR OUTRO
    public void criarUsuario_AdminSemSerAdmin_RetornaErrorMessageStatus403(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("intruso@gmail.com", "123456", "ADMIN"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(403);

        testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new UsuarioRequestDTO("intruso@gmail.com", "123456", "ROLE_ADMIN"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test //SEM NENHUM ADMIN NO BANCO, O PRIMEIRO PODE SER CRIADO SEM TOKEN
    @Sql(scripts = "/sql/usuarios/usuarios-delete.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/usuarios/usuarios-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void criarUsuario_PrimeiroAdminSemToken_RetornaUsuarioAdminStatus201(){
        UsuarioResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("primeiro.admin@gmail.com", "123456", "ADMIN"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.role()).isEqualTo("ADMIN");
    }

    @Test //CABEÇALHO "Bearer " SEM TOKEN É TRATADO COMO REQUISIÇÃO SEM AUTENTICAÇÃO (E NÃO ERRO 500)
    public void requisicao_BearerVazio_TratadaComoSemToken(){
        testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ")
                .bodyValue(new UsuarioRequestDTO("novo@gmail.com", "123456"))
                .exchange()
                .expectStatus().isCreated();

        testClient
                .get()
                .uri("/api/v1/usuarios")
                .header("Authorization", "Bearer ")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarUsuario_RoleInvalida_RetornaErrorMessageStatus422(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO("novo@gmail.com", "123456", "GERENTE"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        org.assertj.core.api.Assertions.assertThat(responseBody).isNotNull();
        org.assertj.core.api.Assertions.assertThat(responseBody.getStatus()).isEqualTo(422);
    }
}