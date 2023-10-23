package com.example.appcompras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddItem extends AppCompatActivity {

    private EditText itemNameEditText;
    private Button saveButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        //seta layout

        //inicializa o firestore
        db = FirebaseFirestore.getInstance();


        //resgata campos
        itemNameEditText = findViewById(R.id.itemNameEditText);
        saveButton = findViewById(R.id.saveButton);

        Intent intent = getIntent();
        //obtem o intent que foi enviado de outra acao para resgatar informaçoes
        String itemToEdit = intent.getStringExtra("itemToEdit");
        //tenta obter uma string chamada itemToEdit dos extras da intent,
        //que sao dados adicionais que podem ser passados


        //se essa string for nula quer dizer que o item nao foi add ainda

        //se nao for nula, pega o nome do item a ser editado e seta no campo itemNameEditText
        if (itemToEdit != null) {
            itemNameEditText.setText(itemToEdit);
        }


        //quando clicar no botao de salvar

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pega o valor do campo com a string editada ou uma nova string
                String newItemName = itemNameEditText.getText().toString();
                //se o campo enviado pelo intent nao for vazio
                if (itemToEdit != null) {
                    // Esta é uma edição, atualiza o item existente no Firestore
                    //enviando o nome antigo e o novo nome
                    updateItemInFirestore(itemToEdit, newItemName);

                } else {
                    // Isto é uma adição, adicione um novo item ao Firestore enviando o novo item
                    addItemToFirestore(newItemName);
                }
            }
        });
    }


    //add um novo item no firestore
    private void addItemToFirestore(String itemName) {

        //criado um Map chamado itemData que armazena os dados
        //a serem salvos no firestore
        //par chave e valor, no caso "nome", itemName(veio do parametro)
        Map<String, Object> itemData = new HashMap<>();
        //insere no Map, a chave nome e sua string
        itemData.put("nome", itemName);
        //salva no shared Preferences
        saveItemToSharedPreferences(itemName);

        //O Map é enviado á coleçao items
        db.collection("items")
                .add(itemData)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        //mensagem de sucesso
                        if (task.isSuccessful()) {
                            Message(getString(R.string.item_adicionado_com_sucesso));
                            finish();
                        } else {
                            //captura erro
                            Message(getString(R.string.falhaadd));
                            finish();
                        }
                    }
                });
    }
    //Atualiza item do firestore recebendo nome antigo e o novo nome
    public void updateItemInFirestore(String nomeItemOriginal, String novoNome) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference itemRef = db.collection("items");
//Cria referencia á coleçao items do firestore

        //consulta um item cujo nome é igual ao nomeItemOriginal passado
        itemRef
                .whereEqualTo("nome", nomeItemOriginal).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //percorre os documentos que foram obtidos da consulta
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            //armazena o id do item encontrado em uma var
                            String documentId = document.getId();
                            //cria ref ao item encontrado com o id
                            DocumentReference itenRef = itemRef.document(documentId);
                            //cria um novo Map com as novas atualizaçoes
                            Map<String, Object> updates = new HashMap<>();
                            //coloca os novos nomes no Map
                            updates.put("nome", novoNome);
                            Log.d("Firestore", "ID do documento: " + documentId);
                           //atualiza no sharedPreferences
                            updateItemInSharedPreferences(nomeItemOriginal, novoNome);
                            //atualiza o banco com o novo map updates
                            itenRef
                                    .update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Item atualizado com sucesso
                                            Message(getString(R.string.itemupdate));

                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        //captura erro
                                        public void onFailure(@NonNull Exception e) {
                                            Message(getString(R.string.falhaadd));
                                        }
                                    });
                        }
                    }
                })
                //captura erro
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Message(getString(R.string.falhacoonsulta));
                    }
                });


}
//gera mensgaem de toast
    public void Message(String message){
        Toast.makeText(AddItem.this, message , Toast.LENGTH_SHORT).show();
        finish();
    }
    // Adicionar um item ao SharedPreferences
    private void saveItemToSharedPreferences(String itemName) {
        //Cria instancia do sharedPrefrences
        SharedPreferences sharedPreferences = getSharedPreferences("item_prefs", Context.MODE_PRIVATE);
        //cria um objeto editor onde sera possivel editar o shared
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //armazena a string recebida
        editor.putString(itemName, itemName);
        //aplica alteraçoes
        editor.apply();
    }

    // Atualizar um item no SharedPreferences
    private void updateItemInSharedPreferences(String oldName, String newName) {
        //Cria instancia do sharedPrefrences
        SharedPreferences sharedPreferences = getSharedPreferences("item_prefs", Context.MODE_PRIVATE);
        //cria um objeto editor onde sera possivel editar o shared
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(oldName); // Remove o item antigo
        editor.putString(newName, newName); // Adiciona o item atualizado
        editor.apply();//aplica atualizaçoes
    }
}