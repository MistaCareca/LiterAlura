package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultadoBusca{

    private Integer count;
    private Livro[] results;

    public Integer getCount() {
        return count;
    }

    public Livro[] getResults() {
        return results;
    }

}
