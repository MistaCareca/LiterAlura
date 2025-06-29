package com.example.LiterAlura.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Table(name = "livros")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Livro {
    @Id
    @Column(name = "id")
    private Long id;

    @JsonAlias("title")
    @Column(name = "titulo")
    private String title;

    @JsonAlias("authors")
    @ManyToMany
    @JoinTable(
            name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    private Autor[] authors;

    @JsonAlias("languages")
    @ElementCollection
    @CollectionTable(name = "livro_idiomas", joinColumns = @JoinColumn(name = "livro_id"))
    @Column(name = "idioma")
    private String[] languages;

    @JsonAlias("download_count")
    @Column(name = "download_count")
    private Long count;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Autor[] getAuthors() { return authors; }
    public void setAuthors(Autor[] authors) { this.authors = authors; }
    public String[] getLanguages() { return languages; }
    public void setLanguages(String[] languages) { this.languages = languages; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }

    @Override
    public String toString() {
        String autoresStr = Arrays.stream(authors)
                .map(Autor::getName)
                .collect(Collectors.joining(", "));
        String idiomasStr = String.join(", ", languages);
        return String.format("Livro{id=%d, title='%s', authors=%s, languages=%s, Downloads=%d}",
                id, title, autoresStr, idiomasStr, count);
    }
}