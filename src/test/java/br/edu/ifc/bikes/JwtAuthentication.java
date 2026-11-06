package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioLoginDto;
import br.edu.ifc.bikes.jwt.JwtToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

//faz o login em /api/v1/auth e devolve o cabeçalho Authorization com o token, para usar em .headers(...)
public class JwtAuthentication {

    public static Consumer<HttpHeaders> oAuth(WebTestClient client, String username, String password){
        String token = client
                .post()
                .uri("/api/v1/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioLoginDto(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(JwtToken.class)
                .returnResult().getResponseBody().getToken();

        return headers -> headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
