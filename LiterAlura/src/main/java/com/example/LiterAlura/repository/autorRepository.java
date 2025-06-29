package com.example.LiterAlura.repository;

import com.example.LiterAlura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface autorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a WHERE a.nascimento <= :ano AND (a.morte IS NULL OR a.morte >= :ano)")
    List<Autor> findByAnoVivo(Integer ano);
}