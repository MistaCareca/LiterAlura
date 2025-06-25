package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Livro {
    @JsonAlias("id")
    private Long id;

    @JsonAlias("title")
    private String titulo;

    @JsonAlias("authors")
    private Autor[] autores;

    @JsonAlias("languages")
    private String[] idiomas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return titulo;
    }

    public void setTitle(String titulo) {
        this.titulo = titulo;
    }

    public Autor[] getAuthors() {
        return autores;
    }

    public void setAuthors(Autor[] autores) {
        this.autores = autores;
    }

    public String[] getLanguages() {
        return idiomas;
    }

    public void setLanguages(String[] idiomas) {
        this.idiomas = idiomas;
    }

    @Override
    public String toString() {
        String autoresStr = Arrays.stream(autores)
                .map(Autor::getName)
                .collect(Collectors.joining(", "));
        String idiomasStr = String.join(", ", idiomas);
        return String.format("Livro{id=%d, titulo='%s', autores=%s, idiomas=%s}",
                id, titulo, autoresStr, idiomasStr);
    }
}