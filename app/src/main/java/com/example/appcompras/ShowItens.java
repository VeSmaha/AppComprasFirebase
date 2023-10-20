package com.example.appcompras;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
//implementa a interface ItemDeleteListener que esta dentro do Adapter

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;


    //metodo implementado da interface que chama o metodo de exclusao
    //do firebase passando o nome do Item
    @Override
    public void onDeleteItem(String nomeDoItem) {

        excluirItem(nomeDoItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //seta o layout
        setContentView(R.layout.show_items);
        //cria instancia do firestore
        db = FirebaseFirestore.getInstance();

        
    //inicializa a lista de itens e o Adapter com essa mesma lista
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this);

//inicializa a reci=ycler view
        //seta o layot manager
        //seta o adaptador
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemAdapter);

        //carrega itens da lista do firestore
        carregarItens();

        //resgata botao
        Button addButton = findViewById(R.id.addButton);

        //add ouvinte ao clicar botao de add
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //inicia nova atividade
                startActivity(new Intent(ShowItens.this, AddItem.class));
            }
        });


        // Observar as mudanças na coleção "items" do Firestore

        //consultar campo nome em ascendente da coleçao items do firebase
        db.collection("items")
                .orderBy("nome", Query.Direction.ASCENDING)
                // quando ouver mudanças na colecao a funçao anonima é chamada
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {

                    //percorre cada mudança feita nos docs da colecao
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        //verifica o tipo da mudança
                        switch (dc.getType()) {
                            //se for added, add novo item
                            case ADDED:
                                //converte o doc alterado para um objeto item
                                Item item = dc.getDocument().toObject(Item.class);
                                //add a lista que esta sendo exibida
                                itemList.add(item);
                                //notifica as mudanças ao adapter assim ele altera a interface
                                itemAdapter.notifyDataSetChanged();
                                break;

                        }
                    }
                });



        //executa a lógica necessária para que a atividade se torne visível na tela
        // e interativa. Isso inclui a atualização da interface do usuário e a restauração do estado da atividade.
    }@Override
    protected void onResume() {
        super.onResume();
        carregarItens();
    }

    private void carregarItens() {
        // Consulta o Firebase Firestore para obter todos os itens da lista de itens
        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //se for bem sucedida
                        if (task.isSuccessful()) {
                            //limpa a lista para que carregue a nova
                            itemList.clear();
                            //percorre cada doc armazenado na coleçao e converte em um objeto Item
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Item item = document.toObject(Item.class);
                                //add a nova lista de itens
                                itemList.add(item);
                            }
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            //caso encontre erro
                            Message(getString(R.string.falhacarregar));
                        }
                    }
                });
    }
    //função para gerar Toasts
    public void Message(String message){
        Toast.makeText(ShowItens.this, message , Toast.LENGTH_SHORT).show();
        finish();
    }

    private void excluirItem(String nomeDoItem) {

        // Consulte o Firebase Firestore para encontrar o doc com base no nome
        db.collection("items")
                .whereEqualTo("nome", nomeDoItem)
                .get()
                //quando a consulta for encerrada e bem sucedida
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //percorre cada doc ate encontrar um com o mesmo nome passado
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Encontrou um documento com o nome correspondente
                                //guarda o id do doc em uma var
                                String itemId = document.getId();
                                // Agora, exclui o documento usando o ID
                                db.collection("items")
                                        .document(itemId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //mostra mensagem de sucesso e exclui o item localmente
                                                Message(getString(R.string.item_excluido_com_sucesso));
                                                deleteItemFromSharedPreferences(nomeDoItem);
                                                //carrega de novo a lista com as alteraçoes feitas
                                                carregarItens();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //caso encontre erro
                                                Message(getString(R.string.falhaexcluir));
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            //caso a busca falhe
                            Message(getString(R.string.falhacoonsulta));
                            finish();
                        }
                    }
                    //deleta do SharedPreferences
                    private void deleteItemFromSharedPreferences(String itemName) {
                        //cria instancia do shared e inicializa o armazenamento
                        SharedPreferences sharedPreferences = getSharedPreferences("item_prefs", Context.MODE_PRIVATE);
                        //cria um objeto do SharedPreferences editor
                        //onde sera possivel alterar a SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        //usa o objeto para remover o item enviando seu nome
                        editor.remove(itemName);
                        //aplica mudanças
                        editor.apply();
                    }
                });
    }

}
