package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultadoBusca {
    private Integer count;
    private Livro[] results;
    private String next;
    private String previous;

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

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    @Override
    public String toString() {
        String resultsStr = results != null
                ? Arrays.stream(results)
                .map(Livro::toString)
                .collect(Collectors.joining(", "))
                : "[]";
        return String.format("ResultadoBusca{count=%d, results=[%s], next=%s, previous=%s}",
                count != null ? count : 0, resultsStr, next, previous);
    }
}