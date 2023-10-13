package com.example.appcompras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowItemsActivity extends Activity {
    private CollectionReference itemsCollection;

    //cria lista que sera usada para mostrar a lista de compras, ou seja,
    // cada string adicionada
    private List<String> itemList;

    //cria um adaptador para vincular a lista com uma Listview e exibila na tela
    private ArrayAdapter<String> adapter;

    //referencia a Listview que sera usada no layout para mostrar as strings
    private ListView itemListView;

    //recupera botão de add
    private Button addButton;

    //armazenamento local do android, persiste e mantem dados entre diferentes sessoes
    private SharedPreferences sharedPreferences;


    //cria a activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtém uma referência para a coleção "items"
        itemsCollection = FirebaseFirestore.getInstance().collection("items");

        // Carregue a lista de itens a partir do Firestore
        loadItemList();
        super.onCreate(savedInstanceState);
        //chama o layout responsavel
        setContentView(R.layout.show_items);
        //inicia o sharedpreferences, criando um arquivo para armazenar os dados "MyItemList"
        //e define esse arquivo como privado, apenas o app tem acesso
        sharedPreferences = getSharedPreferences("MyItemList", MODE_PRIVATE);


        //inicializa o adapter e vincula um layout pronto  do android para cada item para mostrar a lista,
        // e a propria lista de string
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, itemList);
        //ecupera a listView pelo id
        itemListView = findViewById(R.id.itemListView);
        //recupera botao de add
        addButton = findViewById(R.id.addButton);

        //define o adapter na list view, para ser vinculada a lista e exibir todos os itens ao usuario
        itemListView.setAdapter(adapter);

        //add um ouvinte ao botão, agora ele escuta a novas interações
        addButton.setOnClickListener(new View.OnClickListener() {
            //implementa o metodo quando o botão for clicado
            @Override
            public void onClick(View view) {
                //cria um Intent para ir da activity atual até a activity de add itens
                Intent addItemIntent = new Intent(ShowItemsActivity.this, AddItemActivity.class);
                //inicia a activity de destino
                //usa 'startActivityForResult' pois queremos armazenar o resultado
                //quando a activity for concluida
                startActivityForResult(addItemIntent, 1);
            }
        });

        //add um ouvinte a cada item da lista, agora ele escuta a novas interações
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //quando um item for clicado...
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //faz o efeito de marcação de um item quando clicado
                itemListView.setItemChecked(position, !itemListView.isItemChecked(position));


            }
        });

        //add um ouvinte ao item da lista, agora ele escuta interações
        itemListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            //implementa o metodo quando um item é pressionado
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final int itemPosition = position;
                final String selectedItem = itemList.get(itemPosition);

                // Cria um AlertDialog para  excluir
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowItemsActivity.this);
                //seta o titulo do alert dialogue
                builder.setTitle(getString(R.string.deseja_excluir));

                //define um botao positivo para a pergunta do alert
                builder.setPositiveButton(getString(R.string.excluir), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //remove o item da lista de strings usando a posicao
                        itemList.remove(itemPosition);
                        //notifica o adaptador que a lista sera atualizada
                        adapter.notifyDataSetChanged();
                        saveItemList(itemList); // Salve a lista atualizada
                        //mostra toast de sucesso
                        showToast(getString(R.string.item_excluido_com_sucesso));

                    }
                });
                //define um botao negativo para cancelar e nao executa nenhuma acao
                builder.setNegativeButton(getString(R.string.cancelar), null);
                //mostra o alert na tela
                builder.show();

                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //se o codigo e o resultado da activity forem esses
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //captura o novo item add
            String newItem = data.getStringExtra("newItem");
            //se ele nao for vazio
            if (newItem != null) {
                //add novo item na lista
                itemList.add(newItem);
                //notifica as alteraçoes ao adapter
                adapter.notifyDataSetChanged();
                saveItemList(itemList); // Salve a lista atualizada
            }
        }
    }

    //funçaõ que recupera a lista de itens salvos
    private void loadItemList() {
        itemsCollection.document("myItemList").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult();
                if (document.exists()) {
                    List<String> itemList = (List<String>) document.get("itemList");
                    if (itemList != null) {
                        updateAdapter(itemList);
                    }
                }
            }
        });
    }

    private void updateAdapter(List<String> itemList) {
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(itemList);
            adapter.notifyDataSetChanged();
        }
    }


            //função para salvar item na lista
    private void saveItemList(List<String> list) {
        Map<String, Object> data = new HashMap<>();
        data.put("itemList", list);
        itemsCollection.document("myItemList").set(data);
    }

    //função que recebe uma string e gera um Toast para ser mostrado ao usuário
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}




