package com.example.LiterAlura.repository;

import com.example.LiterAlura.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface livroRepository extends JpaRepository<Livro, Long> {
    List<Livro> findByLanguagesContaining(String idioma);
}