package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.ClienteCreateDto;
import br.edu.ifc.bikes.dto.ClienteResponseDto;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//usuários: jose (ADMIN, id 100), maria (CLIENTE, id 101, já tem o cliente id 10) e joao (CLIENTE, id 102, sem cliente)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/clientes/clientes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clientes/clientes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ClienteIT {
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
    public void criarCliente_UsuarioClienteComDadosValidos_RetornaClienteCriadoStatus201(){
        ClienteResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteCreateDto("Joao Pereira", "11144477735"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isNotNull();
        assertThat(responseBody.nome()).isEqualTo("Joao Pereira");
        assertThat(responseBody.cpf()).isEqualTo("11144477735");
    }

    @Test
    public void criarCliente_SemAutenticacao_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteCreateDto("Joao Pereira", "11144477735"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarCliente_UsuarioAdmin_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new ClienteCreateDto("Jose Souza", "93541134780"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarCliente_CpfJaCadastrado_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "joao@gmail.com", "123456"))
                .bodyValue(new ClienteCreateDto("Joao Pereira", "52998224725"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void criarCliente_UsuarioJaPossuiCliente_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new ClienteCreateDto("Maria da Silva", "93541134780"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //FORAM REALIZADOS 04 TESTES - NOME VAZIO, NOME CURTO, CPF COM MENOS DE 11 DÍGITOS E CPF INVÁLIDO
    public void criarCliente_DadosInvalidos_RetornaErrorMessageStatus422(){
        List<ClienteCreateDto> invalidos = List.of(
                new ClienteCreateDto("", "11144477735"),
                new ClienteCreateDto("Jo", "11144477735"),
                new ClienteCreateDto("Joao Pereira", "1114447773"),
                new ClienteCreateDto("Joao Pereira", "11144477700")
        );

        for (ClienteCreateDto dto : invalidos) {
            ErrorMessage responseBody = testClient
                    .post()
                    .uri("/api/v1/clientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(JwtAuthentication.oAuth(testClient, "joao@gmail.com", "123456"))
                    .bodyValue(dto)
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody(ErrorMessage.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getStatus()).isEqualTo(422);
        }
    }

    @Test
    public void buscarCliente_IdExistentePeloAdmin_RetornaClienteStatus200(){
        ClienteResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/clientes/10")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(10);
        assertThat(responseBody.nome()).isEqualTo("Maria da Silva");
        assertThat(responseBody.cpf()).isEqualTo("52998224725");
    }

    @Test
    public void buscarCliente_IdInexistentePeloAdmin_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/99")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void buscarCliente_PeloIdComUsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/clientes/10")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void listarClientes_PeloAdmin_RetornaListaStatus200(){
        List<ClienteResponseDto> responseBody = testClient
                .get()
                .uri("/api/v1/clientes")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(1);
    }

    @Test
    public void listarClientes_ComUsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/clientes")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarDetalhes_UsuarioCliente_RetornaSeusPropriosDadosStatus200(){
        ClienteResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(10);
        assertThat(responseBody.cpf()).isEqualTo("52998224725");
    }

    @Test
    public void buscarDetalhes_UsuarioSemCliente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/detalhes")
                .headers(JwtAuthentication.oAuth(testClient, "joao@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }
}
