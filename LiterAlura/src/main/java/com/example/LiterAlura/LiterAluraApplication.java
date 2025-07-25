package com.example.LiterAlura;

import com.example.LiterAlura.exception.ApiRequestException;
import com.example.LiterAlura.exception.EntradaInvalidaException;
import com.example.LiterAlura.model.Autor;
import com.example.LiterAlura.model.Idioma;
import com.example.LiterAlura.model.Livro;
import com.example.LiterAlura.model.ResultadoBusca;
import com.example.LiterAlura.repository.autorRepository;
import com.example.LiterAlura.repository.livroRepository;
import com.example.LiterAlura.service.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class LiterAluraApplication implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(LiterAluraApplication.class);
	private static int LIMITE_LIVROS = 5;
	private static int MAX_PAGES = 10;

	private final ApiClient apiClient;
	private final livroRepository livroRepository;
	private final autorRepository autorRepository;

	public LiterAluraApplication(ApiClient apiClient, livroRepository livroRepository, autorRepository autorRepository) {
		this.apiClient = apiClient;
		this.livroRepository = livroRepository;
		this.autorRepository = autorRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(LiterAluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Scanner scanner = new Scanner(System.in);

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
						apiClient.exibirLivros(Arrays.asList(resultado.getResults()), LIMITE_LIVROS, logger);
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
						System.out.print("Digite o número máximo de paginas a exibir: ");
						try {
							MAX_PAGES = Integer.parseInt(scanner.nextLine().trim());
							if (MAX_PAGES <= 0) {
								throw new EntradaInvalidaException("O limite deve ser maior que zero.");
							}
							System.out.println("Limite configurado para " + MAX_PAGES + " páginas.");
						} catch (NumberFormatException e) {
							throw new EntradaInvalidaException("Digite um número válido para o limite.");
						}
						break;
					case 3:
						System.out.print("Digite o ID do livro para buscar: ");
						try {
							int id = Integer.parseInt(scanner.nextLine().trim());
							if (id <= 0) {
								throw new EntradaInvalidaException("O ID deve ser um número positivo.");
							}
							if (LIMITE_LIVROS == 0) {
								throw new EntradaInvalidaException("O limite de livros não foi configurado. Use a opção 2 primeiro.");
							}
							Livro livro = apiClient.buscarLivroPorID(id);
							logger.info("Resultado encontrado para ID {}: 1", id);
							apiClient.exibirLivros(Collections.singletonList(livro), 1, logger);
						} catch (NumberFormatException e) {
							throw new EntradaInvalidaException("Digite um número válido para o ID.");
						}
						break;
					case 4:
						System.out.println("Listando todos os livros (limitado a " + LIMITE_LIVROS + " por vez, até " + MAX_PAGES + " páginas)...");
						apiClient.buscarTodosLivros(LIMITE_LIVROS, MAX_PAGES, logger);
						break;
					case 5:
						System.out.print("Digite o ano inicial (ex.: 1800): ");
						int startYear = Integer.parseInt(scanner.nextLine().trim());
						System.out.print("Digite o ano final (ex.: 1899): ");
						int endYear = Integer.parseInt(scanner.nextLine().trim());
						System.out.println("Listando autores vivos entre " + startYear + " e " + endYear + " (limitado a " + LIMITE_LIVROS + " por vez, até " + MAX_PAGES + " páginas)...");
						apiClient.buscarAutoresVivos(startYear, endYear, LIMITE_LIVROS, MAX_PAGES, logger);
						break;
					case 6:
						System.out.println("Idiomas disponíveis: " + listarIdiomasDisponiveis());
						System.out.print("Digite o código do idioma (ex.: en, pt): ");
						String codigoIdiomaContagem = scanner.nextLine().trim().toLowerCase();
						Idioma idiomaContagem = Idioma.fromCodigo(codigoIdiomaContagem);
						long quantidade = livroRepository.findByLanguagesContaining(idiomaContagem.getCodigo()).stream().count();
						System.out.println("Quantidade de livros em " + idiomaContagem.getNome() + ": " + quantidade);
						break;
					case 7:
						System.out.println("Idiomas disponíveis: " + listarIdiomasDisponiveis());
						System.out.print("Digite o código do idioma (ex.: en, pt): ");
						String codigoIdiomaLista = scanner.nextLine().trim().toLowerCase();
						Idioma idiomaLista = Idioma.fromCodigo(codigoIdiomaLista);
						System.out.println("Listando livros no idioma " + idiomaLista.getNome() + " (" + idiomaLista.getCodigo() + ") (limitado a " + LIMITE_LIVROS + " por vez, até " + MAX_PAGES + " páginas)...");
						apiClient.buscarPorIdioma(idiomaLista.getCodigo(), LIMITE_LIVROS, MAX_PAGES, logger);
						break;
					case 8:
						System.out.print("Digite o ano para verificar autores vivos: ");
						try {
							int ano = Integer.parseInt(scanner.nextLine().trim());
							if (ano <= 0) {
								throw new EntradaInvalidaException("O ano deve ser positivo.");
							}
							List<Autor> autoresVivos = autorRepository.findByAnoVivo(ano);
							if (autoresVivos.isEmpty()) {
								System.out.println("Nenhum autor encontrado vivo no ano " + ano + ".");
							} else {
								System.out.println("Autores vivos em " + ano + ":");
								autoresVivos.forEach(System.out::println);
							}
						} catch (NumberFormatException e) {
							throw new EntradaInvalidaException("Digite um ano válido.");
						}
						break;
					default:
						throw new EntradaInvalidaException("Opção inválida. Escolha um número entre 0 e 8.");
				}
			} catch (EntradaInvalidaException e) {
				logger.warn("Erro de entrada: {}", e.getMessage());
				System.out.println("Erro: " + e.getMessage());
			} catch (ApiRequestException e) {
				logger.error("Erro na API: {}", e.getMessage(), e);
				System.out.println("Erro: " + e.getMessage());
			} catch (NumberFormatException e) {
				logger.warn("Entrada inválida: {}", e.getMessage());
				System.out.println("Erro: Digite um número válido.");
			} catch (IllegalArgumentException e) {
				logger.warn("Idioma inválido: {}", e.getMessage());
				System.out.println("Erro: " + e.getMessage());
			}
		}
		scanner.close();
	}

	private String listarIdiomasDisponiveis() {
		return Arrays.stream(Idioma.values())
				.map(idioma -> idioma.getCodigo() + " (" + idioma.getNome() + ")")
				.collect(Collectors.joining(", "));
	}

	private void exibirMenu() {
		System.out.println("\n=== LiterAlura ===");
		System.out.println("1 - Buscar livro por título");
		System.out.println("2 - Configurações básicas");
		System.out.println("3 - Buscar livro por ID");
		System.out.println("4 - Listar todos os livros");
		System.out.println("5 - Listar autores vivos em uma faixa de anos");
		System.out.println("6 - Exibir quantidade de livros por idioma");
		System.out.println("7 - Listar livros por idioma");
		System.out.println("8 - Listar autores vivos em um ano");
		System.out.println("0 - Sair");
	}
}