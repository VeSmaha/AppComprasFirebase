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
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("nome", itemName);
        saveItemToSharedPreferences(itemName);

        db.collection("items")
                .add(itemData)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Message(getString(R.string.item_adicionado_com_sucesso));
                            finish();
                        } else {
                            Message(getString(R.string.falhaadd));
                            finish();
                        }
                    }
                });
    }

    public void updateItemInFirestore(String nomeItemOriginal, String novoNome) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference itemRef = db.collection("items");


        itemRef
                .whereEqualTo("nome", nomeItemOriginal).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String documentId = document.getId();
                            DocumentReference itenRef = itemRef.document(documentId);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("nome", novoNome);
                            Log.d("Firestore", "ID do documento: " + documentId);
                            updateItemInSharedPreferences(nomeItemOriginal, novoNome);
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
                                        public void onFailure(@NonNull Exception e) {
                                            Message(getString(R.string.falhaadd));
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Message(getString(R.string.falhacoonsulta));
                    }
                });


}
    public void Message(String message){
        Toast.makeText(AddItem.this, message , Toast.LENGTH_SHORT).show();
        finish();
    }
    // Adicionar um item ao SharedPreferences
    private void saveItemToSharedPreferences(String itemName) {
        SharedPreferences sharedPreferences = getSharedPreferences("item_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(itemName, itemName);
        editor.apply();
    }

    // Atualizar um item no SharedPreferences
    private void updateItemInSharedPreferences(String oldName, String newName) {
        SharedPreferences sharedPreferences = getSharedPreferences("item_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(oldName); // Remove o item antigo
        editor.putString(newName, newName); // Adiciona o item atualizado
        editor.apply();
    }
}