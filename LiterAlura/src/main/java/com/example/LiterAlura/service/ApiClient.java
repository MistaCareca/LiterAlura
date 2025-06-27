package com.example.LiterAlura.service;

import com.example.LiterAlura.exception.ApiRequestException;
import com.example.LiterAlura.model.Livro;
import com.example.LiterAlura.model.ResultadoBusca;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://gutendex.com/books/";

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10)) // Timeout de conexão
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public HttpClient getClient() {
        return client;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
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

    public Livro buscarLivroPorID(Integer id) throws ApiRequestException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID deve ser um número positivo.");
        }

        String url = BASE_URL + id + "/";
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
            return objectMapper.readValue(response.body(), Livro.class);
        } catch (JsonProcessingException e) {
            throw new ApiRequestException("Erro ao processar o JSON da API", e);
        } catch (IOException | InterruptedException e) {
            throw new ApiRequestException("Erro ao realizar a requisição à API", e);
        }
    }

    public void buscarTodosLivros(int limiteLivros, int maxPages, Logger logger) throws ApiRequestException {
        String nextUrl = BASE_URL;
        int pageCount = 0;

        try {
            while (nextUrl != null && pageCount < maxPages) {
                long startTime = System.currentTimeMillis();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nextUrl))
                        .GET()
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long endTime = System.currentTimeMillis();
                logger.info("Requisição para {} levou {} ms", nextUrl, endTime - startTime);

                if (response.statusCode() != 200) {
                    throw new ApiRequestException("Erro na requisição: código (" + response.statusCode() + ")");
                }
                if (response.body() == null || response.body().isEmpty()) {
                    throw new ApiRequestException("Resposta da API vazia.");
                }

                ResultadoBusca resultado = objectMapper.readValue(response.body(), ResultadoBusca.class);
                if (resultado.getResults() != null) {
                    exibirLivros(Arrays.asList(resultado.getResults()), limiteLivros, logger);
                }
                nextUrl = resultado.getNext();
                pageCount++;
                logger.info("Página {} processada", pageCount);
            }
            if (pageCount >= maxPages) {
                System.out.println("Limite de " + maxPages + " páginas atingido. Use um limite maior se necessário.");
            }
        } catch (JsonProcessingException e) {
            throw new ApiRequestException("Erro ao processar o JSON da API: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            throw new ApiRequestException("Erro ao realizar a requisição à API: " + e.getMessage(), e);
        }
    }

    public void exibirLivros(Iterable<Livro> livros, int limite, Logger logger) {
        Livro[] livroArray = StreamSupport.stream(livros.spliterator(), false)
                .filter(Objects::nonNull)
                .toArray(Livro[]::new);
        if (livroArray.length == 0) {
            System.out.println("Nenhum livro encontrado.");
            return;
        }

        System.out.println("-".repeat(110));
        System.out.printf("%-5s %-50s %-30s %-20s %-15s%n", "ID", "Título", "Autores", "Idiomas", "Downloads");
        System.out.println("-".repeat(110));

        int i = 0;
        for (Livro livro : livroArray) {
            if (i >= limite) break;

            String autoresStr = livro.getAuthors() != null
                    ? Arrays.stream(livro.getAuthors())
                    .map(autor -> autor.getName() != null ? autor.getName() : "desconhecido")
                    .collect(Collectors.joining(", "))
                    : "N/A";
            String idiomasStr = livro.getLanguages() != null
                    ? String.join(", ", livro.getLanguages())
                    : "N/A";
            String downloadStr = livro.getCount() != null ? String.valueOf(livro.getCount()) : "N/A";

            System.out.printf("%-5d %-50s %-30s %-20s %-15s%n",
                    livro.getId() != null ? livro.getId() : 0,
                    limitarString(livro.getTitle() != null ? livro.getTitle() : "desconhecido", 50),
                    limitarString(autoresStr, 60),
                    limitarString(idiomasStr, 20),
                    limitarString(downloadStr, 15));
            i++;
        }
        System.out.println("-".repeat(110));
    }

    public void buscarAutoresVivos(int startYear, int endYear, int limiteLivros, int maxPages, Logger logger) throws ApiRequestException {
        if (startYear >= endYear) {
            throw new IllegalArgumentException("O ano inicial deve ser menor que o ano final.");
        }
        if (startYear <= 0 || endYear <= 0) {
            throw new IllegalArgumentException("Os anos devem ser positivos.");
        }

        String nextUrl = BASE_URL + "?author_year_start=" + startYear + "&author_year_end=" + endYear;
        int pageCount = 0;

        try {
            while (nextUrl != null && pageCount < maxPages) {
                long startTime = System.currentTimeMillis();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nextUrl))
                        .GET()
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long endTime = System.currentTimeMillis();
                logger.info("Requisição para {} levou {} ms", nextUrl, endTime - startTime);

                if (response.statusCode() != 200) {
                    throw new ApiRequestException("Erro na requisição: código (" + response.statusCode() + ")");
                }
                if (response.body() == null || response.body().isEmpty()) {
                    throw new ApiRequestException("Resposta da API vazia.");
                }

                ResultadoBusca resultado = objectMapper.readValue(response.body(), ResultadoBusca.class);
                if (resultado.getResults() != null) {
                    exibirLivros(Arrays.asList(resultado.getResults()), limiteLivros, logger);
                }

                nextUrl = resultado.getNext();
                pageCount++;
                logger.info("Página {} processada", pageCount);
            }
            if (pageCount >= maxPages) {
                System.out.println("Limite de " + maxPages + " páginas atingido. Use um limite maior se necessário.");
            }
        } catch (JsonProcessingException e) {
            throw new ApiRequestException("Erro ao processar o JSON da API: " + e.getMessage(), e);
        } catch (IOException | InterruptedException e) {
            throw new ApiRequestException("Erro ao realizar a requisição à API: " + e.getMessage(), e);
        }
    }

    public void buscarPorIdioma(String idioma, int limiteLivros, int maxPages, Logger logger) throws ApiRequestException {
        if (idioma == null || idioma.trim().isEmpty()) {
            throw new IllegalArgumentException("O idioma não pode ser vazio.");
        }

        String nextUrl = BASE_URL + "?languages=" + idioma.trim();
        int pageCount = 0;

        try {
            while (nextUrl != null && pageCount < maxPages) {
                long startTime = System.currentTimeMillis();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(nextUrl))
                        .GET()
                        .header("Accept", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long endTime = System.currentTimeMillis();
                logger.info("Requisição para {} levou {} ms", nextUrl, endTime - startTime);

                if (response.statusCode() != 200) {
                    throw new ApiRequestException("Erro na requisição: código (" + response.statusCode() + ")");
                }
                if (response.body() == null || response.body().isEmpty()) {
                    throw new ApiRequestException("Resposta da API vazia.");
                }

                ResultadoBusca resultado = objectMapper.readValue(response.body(), ResultadoBusca.class);
                if (resultado.getResults() != null) {
                    exibirLivros(Arrays.asList(resultado.getResults()), limiteLivros, logger);
                }

                nextUrl = resultado.getNext();
                pageCount++;
                logger.info("Página {} processada", pageCount);
            }
            if (pageCount >= maxPages) {
                System.out.println("Limite de " + maxPages + " páginas atingido. Use um limite maior se necessário.");
            }
        } catch (IOException | InterruptedException e) {
            throw new ApiRequestException("Erro ao realizar a requisição à API: " + e.getMessage(), e);
        }
    }

    private String limitarString(String texto, int max) {
        return texto.length() > max ? texto.substring(0, max - 3) + "..." : texto;
    }
}