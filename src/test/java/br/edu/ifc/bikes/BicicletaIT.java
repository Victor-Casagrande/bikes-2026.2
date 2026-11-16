package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaCreateDto;
import br.edu.ifc.bikes.dto.BicicletaResponseDto;
import br.edu.ifc.bikes.dto.BicicletaStatusDto;
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
//todas as ações de bicicleta são restritas ao ADMIN
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

    @Test
    public void criarBicicleta_EmManutencao_RetornaBicicletaCriadaStatus201(){
        BicicletaResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaCreateDto("BK03", "Oggi Hacker", "MANUTENCAO"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BicicletaResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.status()).isEqualTo("MANUTENCAO");
    }

    //FORAM REALIZADOS 05 TESTES - CÓDIGO VAZIO, CÓDIGO CURTO, MODELO VAZIO, STATUS INVÁLIDO E ALUGADA
    //(ALUGADA só é definido por uma locação)
    @Test
    public void criarBicicleta_DadosInvalidos_RetornaErrorMessageStatus422(){
        List<BicicletaCreateDto> invalidos = List.of(
                new BicicletaCreateDto("", "Oggi Hacker", "LIVRE"),
                new BicicletaCreateDto("BK", "Oggi Hacker", "LIVRE"),
                new BicicletaCreateDto("BK03", "", "LIVRE"),
                new BicicletaCreateDto("BK03", "Oggi Hacker", "QUEBRADA"),
                new BicicletaCreateDto("BK03", "Oggi Hacker", "ALUGADA")
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

    @Test
    public void buscarBicicleta_CodigoExistente_RetornaBicicletaStatus200(){
        BicicletaResponseDto responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/BK02")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
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

    @Test //TODAS AS AÇÕES DE BICICLETA SÃO RESTRITAS AO ADMIN
    public void buscarBicicleta_UsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/bicicletas/BK01")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarBicicleta_CodigoInexistente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas/BK99")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
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
    public void listarBicicletas_PeloAdmin_RetornaListaStatus200(){
        List<BicicletaResponseDto> responseBody = testClient
                .get()
                .uri("/api/v1/bicicletas")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BicicletaResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(2);
    }

    @Test
    public void listarBicicletas_UsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/bicicletas")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test //LIVRE -> MANUTENCAO -> LIVRE
    public void alterarStatus_BicicletaLivre_RetornaBicicletaStatus200(){
        for (String status : List.of("MANUTENCAO", "LIVRE")) {
            BicicletaResponseDto responseBody = testClient
                    .patch()
                    .uri("/api/v1/bicicletas/BK01/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                    .bodyValue(new BicicletaStatusDto(status))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BicicletaResponseDto.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.codigo()).isEqualTo("BK01");
            assertThat(responseBody.status()).isEqualTo(status);
        }
    }

    @Test //UMA BICICLETA ALUGADA SÓ MUDA DE STATUS PELA DEVOLUÇÃO
    public void alterarStatus_BicicletaAlugada_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/bicicletas/BK02/status")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaStatusDto("MANUTENCAO"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void alterarStatus_CodigoInexistente_RetornaErrorMessageStatus404(){
        testClient
                .patch()
                .uri("/api/v1/bicicletas/BK99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new BicicletaStatusDto("MANUTENCAO"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test //STATUS VAZIO, INEXISTENTE E ALUGADA
    public void alterarStatus_StatusInvalido_RetornaErrorMessageStatus422(){
        for (String status : List.of("", "QUEBRADA", "ALUGADA")) {
            testClient
                    .patch()
                    .uri("/api/v1/bicicletas/BK01/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                    .bodyValue(new BicicletaStatusDto(status))
                    .exchange()
                    .expectStatus().isEqualTo(422);
        }
    }

    @Test
    public void alterarStatus_UsuarioCliente_RetornaStatus403(){
        testClient
                .patch()
                .uri("/api/v1/bicicletas/BK01/status")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .bodyValue(new BicicletaStatusDto("MANUTENCAO"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
