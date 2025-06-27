package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Autor {
    @JsonAlias("name")
    private String nome;
    @JsonAlias("birth_year")
    private String nascimento;
    @JsonAlias("death_year")
    private String morte;

    public Autor() {}

    public String getName() {
        return nome;
    }

    public void setName(String nome) {
        this.nome = nome;
    }

    public String getMorte() {
        return morte;
    }

    public void setMorte(String morte) {
        this.morte = morte;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    @Override
    public String toString() {
        return "Autor: " + nome +
                ", Ano de nascimento: " + nascimento +
                ", Ano de falecimento: " + morte;
    }
}