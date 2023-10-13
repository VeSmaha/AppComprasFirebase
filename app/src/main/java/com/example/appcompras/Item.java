package com.example.appcompras;

public class Item {
    private String nome;

    public Item() {
        // Requer um construtor vazio para Firestore
    }

    public Item(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}


