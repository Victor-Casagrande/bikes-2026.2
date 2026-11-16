package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.BicicletaResponseDto;
import br.edu.ifc.bikes.dto.CartaoDto;
import br.edu.ifc.bikes.dto.DevolucaoDto;
import br.edu.ifc.bikes.dto.LocacaoCreateDto;
import br.edu.ifc.bikes.dto.LocacaoRelatorioDto;
import br.edu.ifc.bikes.dto.LocacaoResponseDto;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//usuários: jose (ADMIN), maria (cliente 10), joao (sem cliente) e ana (cliente 11)
//bicicletas: BK01 (LIVRE), BK02 (ALUGADA) e BK03 (MANUTENCAO)
//locações: 20 (ATIVA, maria/BK02, retirada há 150 min) e 21 (FINALIZADA, ana/BK03, 2 horas)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/locacoes/locacoes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/locacoes/locacoes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LocacaoIT {
    @LocalServerPort
    int port;

    WebTestClient testClient;

    //4111111111111111 é um número de teste válido pelo algoritmo de Luhn
    private static final CartaoDto CARTAO_VALIDO = new CartaoDto("4111111111111111", "Ana Souza", "12/35", "123");

    @BeforeEach
    void setUp(){
        this.testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private LocacaoCreateDto locacaoValida(){
        return new LocacaoCreateDto(LocalDateTime.now().plusHours(2), CARTAO_VALIDO);
    }

    private BicicletaResponseDto buscarBicicleta(String codigo){
        return testClient
                .get()
                .uri("/api/v1/bicicletas/" + codigo)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDto.class)
                .returnResult().getResponseBody();
    }

    // ===== Requisito 6: solicitação de locação

    @Test
    public void solicitarLocacao_ClienteComDadosValidos_RetornaLocacaoCriadaStatus201(){
        LocacaoResponseDto responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocacaoResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isNotNull();
        assertThat(responseBody.nomeCliente()).isEqualTo("Ana Souza");
        assertThat(responseBody.codigoBicicleta()).isEqualTo("BK01"); //a única livre
        assertThat(responseBody.status()).isEqualTo("ATIVA");
        assertThat(responseBody.dataRetirada()).isNotNull();
        assertThat(responseBody.dataPrevistaDevolucao()).isNotNull();
        assertThat(responseBody.dataDevolucao()).isNull();
        assertThat(responseBody.valor()).isNull();
        assertThat(responseBody.cartaoFinal()).isEqualTo("1111");

        //a bicicleta escolhida passa a ALUGADA
        assertThat(buscarBicicleta("BK01").status()).isEqualTo("ALUGADA");
    }

    @Test //BK01 É ALUGADA PELA ANA; EM SEGUIDA NÃO SOBRA BICICLETA PARA A MARIA
    public void solicitarLocacao_SemBicicletaLivre_RetornaErrorMessageStatus409(){
        testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isCreated();

        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void solicitarLocacao_UsuarioSemCadastroDeCliente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "joao@gmail.com", "123456"))
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void solicitarLocacao_UsuarioAdmin_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void solicitarLocacao_SemAutenticacao_RetornaStatus403(){
        testClient
                .post()
                .uri("/api/v1/locacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(locacaoValida())
                .exchange()
                .expectStatus().isForbidden();
    }

    //FORAM REALIZADOS 08 TESTES - SEM DATA, DATA PASSADA, SEM CARTÃO, NÚMERO INVÁLIDO (LUHN),
    //NÚMERO COM LETRAS, VALIDADE MAL FORMATADA, CARTÃO VENCIDO E CVV CURTO
    @Test
    public void solicitarLocacao_DadosInvalidos_RetornaErrorMessageStatus422(){
        LocalDateTime futuro = LocalDateTime.now().plusHours(2);
        List<LocacaoCreateDto> invalidos = List.of(
                new LocacaoCreateDto(null, CARTAO_VALIDO),
                new LocacaoCreateDto(LocalDateTime.now().minusHours(1), CARTAO_VALIDO),
                new LocacaoCreateDto(futuro, null),
                new LocacaoCreateDto(futuro, new CartaoDto("4111111111111112", "Ana Souza", "12/35", "123")),
                new LocacaoCreateDto(futuro, new CartaoDto("4111abcd11111111", "Ana Souza", "12/35", "123")),
                new LocacaoCreateDto(futuro, new CartaoDto("4111111111111111", "Ana Souza", "13/35", "123")),
                new LocacaoCreateDto(futuro, new CartaoDto("4111111111111111", "Ana Souza", "01/20", "123")),
                new LocacaoCreateDto(futuro, new CartaoDto("4111111111111111", "Ana Souza", "12/35", "12"))
        );

        for (LocacaoCreateDto dto : invalidos) {
            ErrorMessage responseBody = testClient
                    .post()
                    .uri("/api/v1/locacoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                    .bodyValue(dto)
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody(ErrorMessage.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.getStatus()).isEqualTo(422);
        }

        //nenhuma locação foi criada: a BK01 continua livre
        assertThat(buscarBicicleta("BK01").status()).isEqualTo("LIVRE");
    }

    // ===== Consultas

    @Test //O DONO E O ADMIN PODEM CONSULTAR
    public void buscarLocacao_PeloDonoOuAdmin_RetornaLocacaoStatus200(){
        for (String username : List.of("maria@gmail.com", "jose@gmail.com")) {
            LocacaoResponseDto responseBody = testClient
                    .get()
                    .uri("/api/v1/locacoes/20")
                    .headers(JwtAuthentication.oAuth(testClient, username, "123456"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(LocacaoResponseDto.class)
                    .returnResult().getResponseBody();

            assertThat(responseBody).isNotNull();
            assertThat(responseBody.id()).isEqualTo(20);
            assertThat(responseBody.nomeCliente()).isEqualTo("Maria da Silva");
            assertThat(responseBody.codigoBicicleta()).isEqualTo("BK02");
            assertThat(responseBody.status()).isEqualTo("ATIVA");
        }
    }

    @Test
    public void buscarLocacao_DeOutroCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/locacoes/20")
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarLocacao_IdInexistente_RetornaErrorMessageStatus404(){
        testClient
                .get()
                .uri("/api/v1/locacoes/99")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void listarMinhasLocacoes_UsuarioCliente_RetornaApenasAsSuasStatus200(){
        List<LocacaoResponseDto> responseBody = testClient
                .get()
                .uri("/api/v1/locacoes/minhas")
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LocacaoResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.getFirst().id()).isEqualTo(21);
        assertThat(responseBody.getFirst().nomeCliente()).isEqualTo("Ana Souza");
    }

    @Test
    public void listarMinhasLocacoes_UsuarioAdmin_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/locacoes/minhas")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }

    // ===== Requisito 7: devolução

    @Test //150 MINUTOS = 3 HORAS INICIADAS x R$ 10,00
    public void devolverLocacao_PeloClienteDono_RetornaLocacaoFinalizadaComValorStatus200(){
        LocacaoResponseDto responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.status()).isEqualTo("FINALIZADA");
        assertThat(responseBody.dataDevolucao()).isNotNull();
        assertThat(responseBody.duracaoMinutos()).isBetween(150L, 151L);
        assertThat(responseBody.valor()).isEqualByComparingTo("30.00");

        //a bicicleta volta a ficar livre
        assertThat(buscarBicicleta("BK02").status()).isEqualTo("LIVRE");
    }

    @Test //O ADMIN REGISTRA A DEVOLUÇÃO INFORMANDO UM PROBLEMA NA BICICLETA
    public void devolverLocacao_ComProblemaNaBicicleta_BicicletaEmManutencaoStatus200(){
        LocacaoResponseDto responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .bodyValue(new DevolucaoDto(true))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.status()).isEqualTo("FINALIZADA");
        assertThat(buscarBicicleta("BK02").status()).isEqualTo("MANUTENCAO");
    }

    @Test
    public void devolverLocacao_JaFinalizada_RetornaErrorMessageStatus409(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/21/devolucao")
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void devolverLocacao_IdInexistente_RetornaErrorMessageStatus404(){
        ErrorMessage responseBody = testClient
                .patch()
                .uri("/api/v1/locacoes/99/devolucao")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test //ANA NÃO PODE DEVOLVER A LOCAÇÃO DA MARIA
    public void devolverLocacao_DeOutroCliente_RetornaStatus403(){
        testClient
                .patch()
                .uri("/api/v1/locacoes/20/devolucao")
                .headers(JwtAuthentication.oAuth(testClient, "ana@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();

        //nada mudou: a locação continua ativa e a bicicleta alugada
        assertThat(buscarBicicleta("BK02").status()).isEqualTo("ALUGADA");
    }

    // ===== Requisito 8: relatório

    @Test
    public void relatorioLocacoes_PeloAdmin_RetornaTodasAsLocacoesStatus200(){
        List<LocacaoRelatorioDto> responseBody = testClient
                .get()
                .uri("/api/v1/locacoes/relatorio")
                .headers(JwtAuthentication.oAuth(testClient, "jose@gmail.com", "123456"))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LocacaoRelatorioDto.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(2);

        //ordenado da retirada mais recente para a mais antiga
        LocacaoRelatorioDto ativa = responseBody.get(0);
        assertThat(ativa.id()).isEqualTo(20);
        assertThat(ativa.nomeCliente()).isEqualTo("Maria da Silva");
        assertThat(ativa.codigoBicicleta()).isEqualTo("BK02");
        assertThat(ativa.dataRetirada()).isNotNull();
        assertThat(ativa.dataDevolucao()).isNull();
        assertThat(ativa.duracaoMinutos()).isBetween(150L, 151L); //ativa: até o momento
        assertThat(ativa.status()).isEqualTo("ATIVA");

        LocacaoRelatorioDto finalizada = responseBody.get(1);
        assertThat(finalizada.id()).isEqualTo(21);
        assertThat(finalizada.nomeCliente()).isEqualTo("Ana Souza");
        assertThat(finalizada.codigoBicicleta()).isEqualTo("BK03");
        assertThat(finalizada.dataDevolucao()).isNotNull();
        assertThat(finalizada.duracaoMinutos()).isEqualTo(120L);
        assertThat(finalizada.status()).isEqualTo("FINALIZADA");
    }

    @Test
    public void relatorioLocacoes_UsuarioCliente_RetornaStatus403(){
        testClient
                .get()
                .uri("/api/v1/locacoes/relatorio")
                .headers(JwtAuthentication.oAuth(testClient, "maria@gmail.com", "123456"))
                .exchange()
                .expectStatus().isForbidden();
    }
}
