package com.example.appcompras;

public class Item {
    private String nome;

    public Item() {
        // Construtor vazio necessário para Firestore
    }

    public Item(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
