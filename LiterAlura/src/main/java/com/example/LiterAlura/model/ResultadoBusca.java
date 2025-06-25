package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultadoBusca {
    private Integer count;
    private Livro[] results;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Livro[] getResults() {
        return results;
    }

    public void setResults(Livro[] results) {
        this.results = results;
    }

    @Override
    public String toString() {
        String resultsStr = results != null
                ? Arrays.stream(results)
                .map(Livro::toString)
                .collect(Collectors.joining(", "))
                : "[]";
        return String.format("ResultadoBusca{count=%d, results=[%s]}",
                count != null ? count : 0,
                resultsStr);
    }
}