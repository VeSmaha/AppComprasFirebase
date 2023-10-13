package com.example.appcompras;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class AddItemActivity extends Activity {

    private DatabaseReference databaseRef;

    //variaveis que armazenarão campos do layout
    private EditText itemEditText;
    private Button addButton;


    //Cria a activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        //chama o layout resonsável pela activity
        setContentView(R.layout.add_item);


        //define os campos pelo seus ids definidos no layout
        itemEditText = findViewById(R.id.itemEditText);
        addButton = findViewById(R.id.addButton);


        //adiciona um ouvinte ao botão de add, agora ele escuta a interações
        addButton.setOnClickListener(new View.OnClickListener() {

            //implementa o metodo quando o botão for clicado
            @Override
            public void onClick(View view) {

                //armazena a String digitada dentro do campo na var newItem
                //resgatando o conteudo inserido, convertendo em string e tirando quaisquer
                //espaços em branco no começo ou no fim da string
                String newItem = itemEditText.getText().toString().trim();

                //se a string não estiver vazia
                if (!newItem.isEmpty()) {
                    //cria uma nova Intent(carrega dados entre activitys)
                    Intent intent = new Intent();
                    //add a string digitada a Intent
                    intent.putExtra("newItem", newItem);
                    //encerra a activity
                    setResult(RESULT_OK, intent);
                    finish();
                    //mostra mensagem de sucesso
                    showToast(getString(R.string.item_adicionado_com_sucesso));
                } else {
                    //se estiver vazio retorna msg de erro
                    showToast(getString(R.string.valido));
                }
            }
        });


        //recupera botão de voltar pelo id
        Button backButton = findViewById(R.id.button);

        //cria um ouvinte nesse botão, botão escuta a novas interações
        backButton.setOnClickListener(new View.OnClickListener() {

            //implementa o metodo quando o botão for clicado
            @Override
            public void onClick(View v) {
                // Cria um Intent(classe para iniciar activitys) para iniciar a ShowItemsActivity
                Intent intent = new Intent(AddItemActivity.this, ShowItemsActivity.class);

                // Inicie a ShowItemsActivity que esta armazenada no intent
                startActivity(intent);

                // Finalize a AddItemActivity(activity atual)
                finish();
            }
        });

    }
    //função que recebe uma string e gera um Toast para ser mostrado ao usuário
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

