package com.example.appcompras;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


//O RecyclerView é usado para exibir uma lista de itens roláveis, e o
// adaptador é responsável por fornecer os dados a serem exibidos
// na lista, bem como manipular as interações do usuário com esses itens.


//Cria um Adaptador para o REcycler View que controlara a exibiçao de itens na tela
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    //lista de objetos a serem exibidos
    private List<Item> itemList;

    //instancia da interface que notifica quando um item é excluido
    private ItemDeleteListener deleteListener;

    //construtor inicializa o adapter recebendo uma lista de itens
//do recycler view e inicializa o ouvinte de exclusao
    public ItemAdapter(List<Item> itemList,ItemDeleteListener deleteListener) {
        this.itemList = itemList;
        this.deleteListener = deleteListener;

    }
    //interface que tem o metodo que é chamado para excluir um item
    public interface ItemDeleteListener {
        void onDeleteItem(String nomeDoItem);
    }
    //chamado quando o recycler view necessita criar uma nova visulaizaçao
    //do item
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infla o layout do item para exibilo na lista
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item, parent, false);

        return new ItemViewHolder(itemView);
    }


    //quando um item é exibido
//recebe o item Nome e a posiçao do item
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemNameTextView.setText(item.getNome());

        //quando clicado no botao editar do item
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir a atividade de adição, passando o nome do item para edição
                //v -> ref á view no caso button
                Intent intent = new Intent(v.getContext(), AddItem.class);
                intent.putExtra("itemToEdit", item.getNome());
                v.getContext().startActivity(intent);
            }

        });
        //quando clicar no botao excluir do item
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //captura o nome do iten
                String nomeDoItem = item.getNome();
                if (deleteListener != null) {
                    //se o delete listener for passado no construtor
                    //chama o metodo da interface passando o nome do item
                    deleteListener.onDeleteItem(nomeDoItem);
                }
            }
        });

        }

    @Override
    public int getItemCount() {
        //determina quantos itens estao na lista para dimensionar o tamanho da lista
        return itemList.size();
    }


//classe interna que retorna ItemViewHolder, visu do item na lista
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        Button editButton;

        Button deleteButton;
    //resgata campos e botoes do item_layout
        public ItemViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.excluiButton);
        }
    }


}
