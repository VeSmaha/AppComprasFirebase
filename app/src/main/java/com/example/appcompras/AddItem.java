package com.example.appcompras;

import android.app.Activity;
import android.content.Intent;
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

        db = FirebaseFirestore.getInstance();

        itemNameEditText = findViewById(R.id.itemNameEditText);
        saveButton = findViewById(R.id.saveButton);

        Intent intent = getIntent();
        String itemToEdit = intent.getStringExtra("itemToEdit");

        if (itemToEdit != null) {
            itemNameEditText.setText(itemToEdit);
        }


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItemName = itemNameEditText.getText().toString();
                if (itemToEdit != null) {
                    // Esta é uma edição, atualize o item existente no Firestore
                    updateItemInFirestore(itemToEdit, newItemName);
                    Log.d("Firestore", "ID do documento: " + itemToEdit);

                } else {
                    // Isto é uma adição, adicione um novo item ao Firestore
                    addItemToFirestore(newItemName);
                }
            }
        });
    }

    private void addItemToFirestore(String itemName) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("nome", itemName);

        db.collection("items")
                .add(itemData)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            finish();
                        } else {
                            // Trate erros aqui
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
                            itenRef
                                    .update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Item atualizado com sucesso
                                            Toast.makeText(AddItem.this, "Item atualizado com sucesso", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddItem.this, "Item", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddItem.this, "Erro na consulta", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}