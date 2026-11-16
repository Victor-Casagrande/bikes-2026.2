package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaCreateDto;
import br.edu.ifc.bikes.dto.BicicletaResponseDto;
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

//usuários: jose (ADMIN) e maria (CLIENTE); bicicletas: BK01 (LIVRE) e BK02 (ALUGADA)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/bicicletas/bicicletas-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/bicicletas/bicicletas-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BicicletaIT {
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
    public void criarBicicleta_AdminComDadosValidos_RetornaBicicletaCriadaStatus201(){
        BicicletaResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaCreateDto("BK03", "Oggi Hacker", "LIVRE"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BicicletaResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isNotNull();
        assertThat(responseBody.codigo()).isEqualTo("BK03");
        assertThat(responseBody.modelo()).isEqualTo("Oggi Hacker");
        assertThat(responseBody.status()).isEqualTo("LIVRE");
    }

    @Test
    public void criarBicicleta_UsuarioCliente_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new BicicletaCreateDto("BK03", "Oggi Hacker", "LIVRE"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarBicicleta_SemAutenticacao_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BicicletaCreateDto("BK03", "Oggi Hacker", "LIVRE"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarBicicleta_CodigoJaCadastrado_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaCreateDto("BK01", "Oggi Hacker", "LIVRE"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test //FORAM REALIZADOS 04 TESTES - CÓDIGO VAZIO, CÓDIGO CURTO, MODELO VAZIO E STATUS INVÁLIDO
    public void criarBicicleta_DadosInvalidos_RetornaErrorMessageStatus422(){
        List<BicicletaCreateDto> invalidos = List.of(
                new BicicletaCreateDto("", "Oggi Hacker", "LIVRE"),
                new BicicletaCreateDto("BK", "Oggi Hacker", "LIVRE"),
                new BicicletaCreateDto("BK03", "", "LIVRE"),
                new BicicletaCreateDto("BK03", "Oggi Hacker", "QUEBRADA")
        );

        for (BicicletaCreateDto dto : invalidos) {
            ErrorMessage responseBody = testClient
                    .post()
                    .uri("/api/v1/bicicletas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                    .bodyValue(dto)
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody(ErrorMessage.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getStatus()).isEqualTo(422);
        }
    }

    @Test //ADMIN E CLIENTE PODEM CONSULTAR
    public void buscarBicicleta_CodigoExistente_RetornaBicicletaStatus200(){
        for (String username : List.of("jose@gmail.com", "maria@gmail.com")) {
            BicicletaResponseDto responseBody = testClient
                    .get()
                    .uri("/api/v1/bicicletas/BK02")
                    .headers(JwtAuthentication.oAuth(testClient, username, "123456"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BicicletaResponseDto.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.id()).isEqualTo(11);
            assertThat(responseBody.codigo()).isEqualTo("BK02");
            assertThat(responseBody.modelo()).isEqualTo("Sense Impact");
            assertThat(responseBody.status()).isEqualTo("ALUGADA");
        }
    }

    @Test
    public void buscarBicicleta_CodigoInexistente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/BK99")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void buscarBicicleta_SemAutenticacao_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/bicicletas/BK01")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void listarBicicletas_UsuarioCliente_RetornaListaStatus200(){
        List<BicicletaResponseDto> responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BicicletaResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(2);
    }
}
