package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Autor {
    @JsonAlias("name")
    private String nome;

    public Autor() {}

    public String getName() {
        return nome;
    }

    public void setName(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return String.format("Autor{nome='%s'}", nome != null ? nome : "desconhecido");
    }
}