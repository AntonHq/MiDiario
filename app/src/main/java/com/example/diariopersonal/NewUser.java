package com.example.diariopersonal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText correoTxt, contraseñaTxt, confiContraseñaTxt, usuarioTxt;
    private TextView errorLbl;
    private Button guardarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Conectar las vistas
        correoTxt = findViewById(R.id.txtCorreor);
        contraseñaTxt = findViewById(R.id.txtContraseñar);
        confiContraseñaTxt = findViewById(R.id.txtConfiContraseñar);
        usuarioTxt = findViewById(R.id.txtUsuarior);
        errorLbl = findViewById(R.id.lblErrorr);
        guardarBtn = findViewById(R.id.btnGuardar);

        // Ocultar el mensaje de error inicialmente
        errorLbl.setVisibility(View.GONE);

        // Configurar el listener del botón de registro
        guardarBtn.setOnClickListener(v -> {
            String correo = correoTxt.getText().toString().trim();
            String contraseña = contraseñaTxt.getText().toString().trim();
            String confiContraseña = confiContraseñaTxt.getText().toString().trim();
            String usuario = usuarioTxt.getText().toString().trim();

            if (validarCampos(correo, contraseña, confiContraseña, usuario)) {
                errorLbl.setVisibility(View.GONE);
                crearCuenta(correo, contraseña, usuario);
            }
        });
    }

    private boolean validarCampos(String correo, String contraseña, String confiContraseña, String usuario) {
        if (correo.isEmpty() || contraseña.isEmpty() || confiContraseña.isEmpty() || usuario.isEmpty()) {
            mostrarError("Por favor llene todos los campos");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Correo inválido");
            return false;
        } else if (contraseña.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            return false;
        } else if (!contraseña.equals(confiContraseña)) {
            mostrarError("Las contraseñas no coinciden");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        errorLbl.setText(mensaje);
        errorLbl.setVisibility(View.VISIBLE);
    }

    private void crearCuenta(String correo, String contraseña, String usuario) {
        mAuth.createUserWithEmailAndPassword(correo, contraseña)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            guardarUsuarioEnFirestore(user.getUid(), usuario, correo);
                            actualizarUI(user);
                        }
                    } else {
                        manejarErrorDeRegistro(task.getException());
                    }
                });
    }

    private void guardarUsuarioEnFirestore(String userId, String usuario, String correo) {
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("usuario", usuario);
        usuarioData.put("correo", correo);

        db.collection("usuarios").document(userId)
                .set(usuarioData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NewUser.this, "Usuario registrado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(NewUser.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void manejarErrorDeRegistro(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            mostrarError("El correo ya está registrado");
        } else {
            mostrarError("Error al registrar usuario");
        }
    }

    private void actualizarUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(NewUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}