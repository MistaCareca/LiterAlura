package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Livro {
    private Long id;
    private String title;
    private Autor[] authors;
    private String[] languages;

    public String getTitle() {
        return title;
    }

    public Autor[] getAuthors() {
        return authors;
    }

    public String[] getLanguages() {
        return languages;
    }

}