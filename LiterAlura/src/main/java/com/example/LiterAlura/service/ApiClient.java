package com.example.LiterAlura.service;

import com.example.LiterAlura.exception.ApiRequestException;
import com.example.LiterAlura.model.ResultadoBusca;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public ResultadoBusca buscarLivroPorTitulo(String titulo) throws ApiRequestException {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }

        String tituloFormatado = titulo.trim().replace(" ", "%20");
        String url = BASE_URL + "?search=" + tituloFormatado;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiRequestException("Erro na requisição: código (" + response.statusCode() + ")");
            }
            if (response.body() == null || response.body().isEmpty()) {
                throw new ApiRequestException("Resposta da API vazia.");
            }
            return objectMapper.readValue(response.body(), ResultadoBusca.class);
        } catch (JsonProcessingException e) {
            throw new ApiRequestException("Erro ao processar o JSON da API", e);
        } catch (IOException | InterruptedException e) {
            throw new ApiRequestException("Erro ao realizar a requisição à API", e);
        }
    }
}