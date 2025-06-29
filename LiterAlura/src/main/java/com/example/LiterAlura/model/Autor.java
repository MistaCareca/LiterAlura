package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "autores")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonAlias("name")
    @Column(name = "nome")
    private String nome;

    @JsonAlias("birth_year")
    @Column(name = "nascimento")
    private Integer nascimento;

    @JsonAlias("death_year")
    @Column(name = "morte")
    private Integer morte;

    public Autor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return nome; } // Mantido como getName() para compatibilidade
    public void setName(String nome) { this.nome = nome; }
    public Integer getNascimento() { return nascimento; }
    public void setNascimento(Integer nascimento) { this.nascimento = nascimento; }
    public Integer getMorte() { return morte; }
    public void setMorte(Integer morte) { this.morte = morte; }

    @Override
    public String toString() {
        return "Autor: " + nome + ", Ano de nascimento: " + nascimento + ", Ano de falecimento: " + morte;
    }
}