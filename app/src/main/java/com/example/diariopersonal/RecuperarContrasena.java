package com.example.diariopersonal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RecuperarContrasena extends AppCompatActivity {

    private EditText txtCorreo;
    private Button btnRecuperar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasena);

        txtCorreo = findViewById(R.id.txtCorreo);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        mAuth = FirebaseAuth.getInstance();

        btnRecuperar.setOnClickListener(view -> {
            String email = txtCorreo.getText().toString().trim();

            // Validación de correo vacío
            if (email.isEmpty()) {
                txtCorreo.setError("El correo no puede estar vacío");
                return;
            }

            // validar que correo exista en la base de datos
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RecuperarContrasena.this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RecuperarContrasena.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RecuperarContrasena.this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}