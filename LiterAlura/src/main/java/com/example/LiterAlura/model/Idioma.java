package com.example.LiterAlura.model;

public enum Idioma {
    EN("en", "Inglês"),
    PT("pt", "Português"),
    ES("es", "Espanhol"),
    FR("fr", "Francês"),
    DE("de", "Alemão"),
    IT("it", "Italiano"),
    RU("ru", "Russo"),
    ZH("zh", "Chinês"),
    JA("ja", "Japonês"),
    AR("ar", "Árabe");

    private final String codigo;
    private final String nome;

    Idioma(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public static Idioma fromCodigo(String codigo) {
        for (Idioma idioma : Idioma.values()) {
            if (idioma.getCodigo().equalsIgnoreCase(codigo)) {
                return idioma;
            }
        }
        throw new IllegalArgumentException("Idioma inválido: " + codigo);
    }
}