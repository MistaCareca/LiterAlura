package services;

import com.example.LiterAlura.model.ResultadoBusca;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://gutendex.com/books/";

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ResultadoBusca buscarLivroPorTitulo(String titulo) throws IOException, InterruptedException {
        String tituloFormatado = titulo.trim().replace(" ", "%20");
        String url = BASE_URL + "?search=" + tituloFormatado;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), ResultadoBusca.class);
        } else {
            throw new IOException("Erro na requisição: código (" + response.statusCode() + ")");
        }
    }
}