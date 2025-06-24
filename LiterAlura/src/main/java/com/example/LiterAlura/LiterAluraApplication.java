package com.example.LiterAlura;

import com.example.LiterAlura.model.Autor;
import com.example.LiterAlura.model.Livro;
import com.example.LiterAlura.model.ResultadoBusca;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import services.ApiClient;

import java.io.IOException;
import java.util.Scanner;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LiterAluraApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiterAluraApplication.class, args);

		Scanner scanner = new Scanner(System.in);
		ApiClient apiClient = new ApiClient();

		System.out.println("=== LiterAlura ===");
		System.out.print("Digite o título do livro para buscar: ");
		String titulo = scanner.nextLine();

		try {
			ResultadoBusca resultado = apiClient.buscarLivroPorTitulo(titulo);
			System.out.println("Resultados encontrados: " + resultado.getCount());
			for (Livro livro : resultado.getResults()) {
				System.out.println("Título: " + livro.getTitle());
				for (Autor autor : livro.getAuthors()) {
					System.out.println("- Autor: " + autor.getName());
					}
				System.out.println("Idiomas: " + String.join(", ", livro.getLanguages()));
				System.out.println("---");
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Erro ao buscar o livro: " + e.getMessage());
		}

		scanner.close();
	}
}