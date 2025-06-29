package com.example.LiterAlura.service;

import com.example.LiterAlura.exception.ApiRequestException;
import com.example.LiterAlura.model.Autor;
import com.example.LiterAlura.model.Livro;
import com.example.LiterAlura.model.ResultadoBusca;
import com.example.LiterAlura.repository.autorRepository;
import com.example.LiterAlura.repository.livroRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://gutendex.com/books/";
    private final livroRepository livroRepository;
    private final autorRepository autorRepository;

    @Autowired
    public ApiClient(livroRepository livroRepository, autorRepository autorRepository) {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        if (autorRepository == null || livroRepository == null) {
            throw new IllegalStateException("Repositórios não foram injetados corretamente!");
        }
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
            ResultadoBusca resultado = objectMapper.readValue(response.body(), ResultadoBusca.class);
            for (Livro livro : resultado.getResults()) {
                if (livro.getAuthors() != null && livro.getAuthors().length > 0) {
                    Autor autor = livro.getAuthors()[0];
                    autorRepository.save(autor);
                    livro.setAuthors(new Autor[]{autor});
                }
                if (livro.getLanguages() != null && livro.getLanguages().length > 0) {
                    livro.setLanguages(new String[]{livro.getLanguages()[0]});
                }
                livroRepository.save(livro);
            }
            logger.info("Livros salvos com sucesso: {}", resultado.getCount());
            return resultado;
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
            Livro livro = objectMapper.readValue(response.body(), Livro.class);
            if (livro.getAuthors() != null && livro.getAuthors().length > 0) {
                Autor autor = livro.getAuthors()[0];
                autorRepository.save(autor);
                livro.setAuthors(new Autor[]{autor});
            }
            if (livro.getLanguages() != null && livro.getLanguages().length > 0) {
                livro.setLanguages(new String[]{livro.getLanguages()[0]});
            }
            livroRepository.save(livro);
            return livro;
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
                    for (Livro livro : resultado.getResults()) {
                        if (livro.getAuthors() != null && livro.getAuthors().length > 0) {
                            Autor autor = livro.getAuthors()[0];
                            autorRepository.save(autor);
                            livro.setAuthors(new Autor[]{autor});
                        }
                        if (livro.getLanguages() != null && livro.getLanguages().length > 0) {
                            livro.setLanguages(new String[]{livro.getLanguages()[0]});
                        }
                        livroRepository.save(livro);
                    }
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

            String autoresStr = livro.getAuthors() != null && livro.getAuthors().length > 0
                    ? livro.getAuthors()[0].getName() != null ? livro.getAuthors()[0].getName() : "desconhecido"
                    : "N/A";
            String idiomasStr = livro.getLanguages() != null && livro.getLanguages().length > 0
                    ? livro.getLanguages()[0]
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
                    for (Livro livro : resultado.getResults()) {
                        if (livro.getAuthors() != null && livro.getAuthors().length > 0) {
                            Autor autor = livro.getAuthors()[0];
                            autorRepository.save(autor);
                            livro.setAuthors(new Autor[]{autor});
                        }
                        if (livro.getLanguages() != null && livro.getLanguages().length > 0) {
                            livro.setLanguages(new String[]{livro.getLanguages()[0]});
                        }
                        livroRepository.save(livro);
                    }
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
                    for (Livro livro : resultado.getResults()) {
                        if (livro.getAuthors() != null && livro.getAuthors().length > 0) {
                            Autor autor = livro.getAuthors()[0];
                            autorRepository.save(autor);
                            livro.setAuthors(new Autor[]{autor});
                        }
                        if (livro.getLanguages() != null && livro.getLanguages().length > 0) {
                            livro.setLanguages(new String[]{livro.getLanguages()[0]});
                        }
                        livroRepository.save(livro);
                    }
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