package com.example.diariopersonal;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CambiarDatos extends AppCompatActivity {

    private EditText txtNombrec, txtContraseñac;
    private Button btnGuardarc;
    private TextView errorLbl;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_datos);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Conectar las vistas
        txtNombrec = findViewById(R.id.txtNombrec);
        txtContraseñac = findViewById(R.id.txtContraseñac);
        btnGuardarc = findViewById(R.id.btnGuardarc);
        errorLbl = new TextView(this); // Crear un TextView para mostrar mensajes de error

        // Cargar el nombre actual del usuario en el campo de nombre
        if (currentUser != null) {
            DocumentReference docRef = db.collection("usuarios").document(currentUser.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nombre = documentSnapshot.getString("usuario");
                    txtNombrec.setText(nombre);
                }
            });
        }

        // Configurar el listener del botón para guardar los cambios
        btnGuardarc.setOnClickListener(v -> {
            String nuevoNombre = txtNombrec.getText().toString();
            String nuevaContraseña = txtContraseñac.getText().toString();

            if (nuevoNombre.isEmpty() || nuevaContraseña.isEmpty()) {
                mostrarError("Por favor, completa todos los campos");
            } else {
                actualizarDatosUsuario(nuevoNombre, nuevaContraseña);
            }
        });
    }

    private void actualizarDatosUsuario(String nuevoNombre, String nuevaContraseña) {
        if (currentUser != null) {
            // Actualizar el nombre en Firestore
            DocumentReference userRef = db.collection("usuarios").document(currentUser.getUid());
            userRef.update("usuario", nuevoNombre)
                    .addOnSuccessListener(aVoid -> {
                        // Nombre actualizado exitosamente
                        Toast.makeText(CambiarDatos.this, "Nombre actualizado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> mostrarError("Error al actualizar el nombre"));

            // Actualizar la contraseña en Firebase Auth
            currentUser.updatePassword(nuevaContraseña)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(CambiarDatos.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                            // Redirigir a la página de configuración o cerrar la actividad
                            finish();
                        } else {
                            mostrarError("Error al actualizar la contraseña");
                        }
                    });
        }
    }

    private void mostrarError(String mensaje) {
        errorLbl.setText(mensaje);
        errorLbl.setTextColor(Color.RED);
        errorLbl.setVisibility(View.VISIBLE);
    }
}
