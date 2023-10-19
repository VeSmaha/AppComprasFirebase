package com.example.appcompras;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowItens extends AppCompatActivity implements ItemAdapter.ItemDeleteListener{
    // Resto da classe ShowItens

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;
    @Override
    public void onDeleteItem(String nomeDoItem) {
        excluirItem(nomeDoItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_items);

        db = FirebaseFirestore.getInstance();



        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);

        carregarItens();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowItens.this, AddItem.class));
            }
        });


        // Observar as mudanças na coleção "items" do Firestore
        db.collection("items")
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {


                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Item item = dc.getDocument().toObject(Item.class);
                                itemList.add(item);
                                itemAdapter.notifyDataSetChanged();
                                break;
                            // Lidar com outras ações (REMOVED, MODIFIED) conforme necessário
                        }
                    }
                });


    }@Override
    protected void onResume() {
        super.onResume();
        carregarItens();
    }

    private void carregarItens() {
        // Consulte o Firebase Firestore para obter a lista de itens
        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            itemList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Item item = document.toObject(Item.class);
                                itemList.add(item);
                            }
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            // Trate erros aqui
                        }
                    }
                });
    }

    private void excluirItem(String nomeDoItem) {
        Log.d("Firestore", "nome do documento: " + nomeDoItem);
        // Consulte o Firebase Firestore para encontrar o ID do documento com base no nome
        db.collection("items")
                .whereEqualTo("nome", nomeDoItem)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Encontrou um documento com o nome correspondente
                                String itemId = document.getId();
                                Log.d("Firestore", "ID do documento: " + itemId);
                                // Agora, exclua o documento usando o ID
                                db.collection("items")
                                        .document(itemId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Item excluído com sucesso
                                                // Você pode atualizar a lista de itens se necessário
                                                carregarItens();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Trate erros aqui
                                            }
                                        });
                            }
                        } else {
                            // Trate erros aqui
                        }
                    }
                });
    }

}
