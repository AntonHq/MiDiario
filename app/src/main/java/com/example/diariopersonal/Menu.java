package com.example.diariopersonal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Menu extends AppCompatActivity {

    private Button ajustesBtn, nuevoBtn, todoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ajustesBtn = findViewById(R.id.btnAjustes);
        nuevoBtn = findViewById(R.id.btnNuevo);
        todoBtn = findViewById(R.id.btnTodo);


        // ingresar a vista ajustes
        ajustesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, Settings.class);
                startActivity(intent);
            }
        });

        //ingrensar a vista de pagina nueva
        nuevoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, PagNueva.class);
                startActivity(intent);
            }
        });

        //Ingresar a vista de todas las paginas
        todoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Menu.this, Todo.class);
                startActivity(intent);
            }
        });

    }
}