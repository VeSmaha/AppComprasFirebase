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

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;



    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item, parent, false);
        return new ItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemNameTextView.setText(item.getNome());


        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir a atividade de adição, passando o nome do item para edição
                Intent intent = new Intent(v.getContext(), AddItem.class);
                intent.putExtra("itemToEdit", item.getNome());
                v.getContext().startActivity(intent);
            }
        });

        // ... Outras lógicas de ViewHolder
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }



    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView;
        Button editButton;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }


}
