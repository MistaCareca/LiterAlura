package com.example.LiterAlura;

import com.example.LiterAlura.exception.ApiRequestException;
import com.example.LiterAlura.exception.EntradaInvalidaException;
import com.example.LiterAlura.model.Livro;
import com.example.LiterAlura.model.ResultadoBusca;
import com.example.LiterAlura.service.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class LiterAluraApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(LiterAluraApplication.class);
	private static int LIMITE_LIVROS = 5; // Valor padrão

	public static void main(String[] args) {
		SpringApplication.run(LiterAluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Scanner scanner = new Scanner(System.in);
		ApiClient apiClient = new ApiClient();

		while (true) {
			exibirMenu();
			System.out.print("Escolha uma opção: ");
			String entrada = scanner.nextLine().trim();

			if (entrada.equals("0")) {
				System.out.println("Saindo da aplicação...");
				break;
			}

			try {
				int opcao = Integer.parseInt(entrada);
				switch (opcao) {
					case 1:
						System.out.print("Digite o título do livro para buscar: ");
						String titulo = scanner.nextLine().trim();
						if (titulo.isEmpty()) {
							throw new EntradaInvalidaException("O título não pode ser vazio.");
						}
						if (LIMITE_LIVROS == 0) {
							throw new EntradaInvalidaException("O limite de livros não foi configurado. Use a opção 2 primeiro.");
						}
						ResultadoBusca resultado = apiClient.buscarLivroPorTitulo(titulo);
						logger.info("Resultados encontrados: {}", resultado.getCount());
						exibirResultados(resultado);
						break;
					case 2:
						System.out.print("Digite o número máximo de livros a exibir: ");
						try {
							LIMITE_LIVROS = Integer.parseInt(scanner.nextLine().trim());
							if (LIMITE_LIVROS <= 0) {
								throw new EntradaInvalidaException("O limite deve ser maior que zero.");
							}
							System.out.println("Limite configurado para " + LIMITE_LIVROS + " livros.");
						} catch (NumberFormatException e) {
							throw new EntradaInvalidaException("Digite um número válido para o limite.");
						}
						break;
					default:
						throw new EntradaInvalidaException("Opção inválida. Escolha um número entre 0 e 2.");
				}
			} catch (EntradaInvalidaException e) {
				logger.warn("Erro de entrada: {}", e.getMessage());
				System.out.println("Erro: " + e.getMessage());
			} catch (ApiRequestException e) {
				logger.error("Erro na API: {}", e.getMessage(), e);
				System.out.println("Erro: " + e.getMessage());
			}
		}

		scanner.close();
	}

	private void exibirMenu() {
		System.out.println("\n=== LiterAlura ===");
		System.out.println("1 - Buscar livro por título");
		System.out.println("2 - Configurações básicas (definir limite de livros)");
		System.out.println("0 - Sair");
	}

	private void exibirResultados(ResultadoBusca resultado) {
		if (resultado == null || resultado.getResults() == null || resultado.getResults().length == 0) {
			System.out.println("Nenhum livro encontrado.");
			return;
		}

		System.out.println("-".repeat(110));
		System.out.printf("%-5s %-50s %-30s %-20s%n", "ID", "Título", "Autores", "Idiomas");
		System.out.println("-".repeat(110));

		int i = 0;
		for (Livro livro : resultado.getResults()) {
			if (i >= LIMITE_LIVROS) break;

			String autoresStr = livro.getAuthors() != null
					? Arrays.stream(livro.getAuthors())
					.map(autor -> autor.getName() != null ? autor.getName() : "desconhecido")
					.collect(Collectors.joining(", "))
					: "N/A";
			String idiomasStr = livro.getLanguages() != null
					? String.join(", ", livro.getLanguages())
					: "N/A";

			System.out.printf("%-5d %-50s %-30s %-20s%n",
					livro.getId() != null ? livro.getId() : 0,
					limitarString(livro.getTitle() != null ? livro.getTitle() : "desconhecido", 50),
					limitarString(autoresStr, 30),
					limitarString(idiomasStr, 20));
			i++;
		}
		System.out.println("-".repeat(110));
	}

	private String limitarString(String texto, int max) {
		return texto.length() > max ? texto.substring(0, max - 3) + "..." : texto;
	}
}