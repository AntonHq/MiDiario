package com.example.diariopersonal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CambiarNombre extends AppCompatActivity {

    private EditText txtNombrec;
    private Button btnGuardarc;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_nombre);

        // Inicializar Firebase y obtener el usuario actual
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Referenciar los elementos del layout
        txtNombrec = findViewById(R.id.txtCorreo);
        btnGuardarc = findViewById(R.id.btnRecuperar);

        // Configurar el botón de guardar
        btnGuardarc.setOnClickListener(view -> {
            String nuevoNombre = txtNombrec.getText().toString().trim();
            if (validarInput(nuevoNombre)) {
                guardarNombreEnFirestore(nuevoNombre);
            }
        });
    }

    private boolean validarInput(String nuevoNombre) {
        if (nuevoNombre.isEmpty()) {
            txtNombrec.setError("El nombre no puede estar vacío");
            return false;
        }else if (nuevoNombre.length() < 3) {
            txtNombrec.setError("El nombre debe tener al menos 3 caracteres");
            return false;
        }else if (nuevoNombre.length() > 20) {
            txtNombrec.setError("El nombre debe tener menos de 15 caracteres");
            return false;
        } else if (!nuevoNombre.matches("[a-zA-Z ]+")) {
            txtNombrec.setError("El nombre solo puede contener letras y espacios");
            return false;
        }
        return true;
    }

    private void guardarNombreEnFirestore(String nuevoNombre) {
        if (user != null) {
            // Actualizar el nombre de usuario en Firestore
            db.collection("usuarios")
                    .document(user.getUid())
                    .update("usuario", nuevoNombre)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CambiarNombre.this, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
                        //dirigir a main activity
                        Intent intent = new Intent(CambiarNombre.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(CambiarNombre.this, "Error al actualizar el nombre: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }
}