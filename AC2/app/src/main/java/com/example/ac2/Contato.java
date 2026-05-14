package com.example.ac2;

// ============================================================
// CONTATO.JAVA - Classe Modelo (Model)
// ============================================================
// No SQLite, os dados ficavam numa TABELA (DatabaseHelper).
// No Firebase Firestore, cada contato vira um DOCUMENTO dentro
// da coleção "contatos". Essa classe mapeia os campos.
//
// REGRA OBRIGATÓRIA DO FIRESTORE:
//   - Precisa ter construtor VAZIO (sem parâmetros)
//   - Precisa ter GETTERS e SETTERS para todos os campos
//   - O Firestore usa isso para serializar/desserializar
//     automaticamente com doc.toObject(Contato.class)
// ============================================================

public class Contato {

    // O id agora é String (ex: "6pJiPFTDCUGEK9HxUL24")
    // No SQLite era INTEGER AUTOINCREMENT
    // No Firestore é gerado automaticamente pelo .add()
    private String id;

    private String nome;
    private String telefone;
    private String email;
    private String categoria;
    private String cidade;

    // No SQLite era INTEGER (0 ou 1) por causa da limitação do banco
    // No Firestore pode ser boolean mesmo
    private boolean favorito;

    // ============================================================
    // CONSTRUTOR VAZIO - OBRIGATÓRIO PARA O FIRESTORE!
    // O Firestore precisa desse construtor para reconstruir o objeto
    // automaticamente quando você chama doc.toObject(Contato.class)
    // ============================================================
    public Contato() {
        // Deixar vazio - o Firestore preenche os campos pelos setters
    }

    // Construtor completo - usado no código quando você quer criar um Contato
    public Contato(String id, String nome, String telefone, String email,
                   String categoria, String cidade, boolean favorito) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.categoria = categoria;
        this.cidade = cidade;
        this.favorito = favorito;
    }

    // ============================================================
    // GETTERS E SETTERS
    // O Firestore usa os getters para salvar os dados
    // e os setters para carregar de volta do banco
    // ============================================================

    // O campo "id" não é salvo no documento (é o ID do documento em si)
    // Por isso não precisa estar no construtor vazio, mas precisamos do getter/setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public boolean isFavorito() { return favorito; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
}
