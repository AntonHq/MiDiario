package com.example.diariopersonal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class newUser extends AppCompatActivity {

    // Inicializar la base de datos firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private EditText correorTxt, contraseñarTxt, confiContraseñarTxt, usuariorTxt;
    private TextView errorrLbl;
    private Button guardarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Inicializar la instancia de Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Conectar las vistas
        correorTxt = findViewById(R.id.txtCorreor);
        contraseñarTxt = findViewById(R.id.txtContraseñar);
        guardarBtn = findViewById(R.id.btnGuardar);
        errorrLbl = findViewById(R.id.lblErrorr);
        usuariorTxt = findViewById(R.id.txtUsuarior);
        confiContraseñarTxt = findViewById(R.id.txtConfiContraseñar);

        // ocultar el mensaje de error
        errorrLbl.setVisibility(View.GONE);

        // Configurar el listener del botón de registro
       guardarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correorTxt.getText().toString();
                String contraseña = contraseñarTxt.getText().toString();
                String confiContraseña = confiContraseñarTxt.getText().toString();
                String usuario = usuariorTxt.getText().toString();

                if (correo.isEmpty() || contraseña.isEmpty() || confiContraseña.isEmpty() || usuario.isEmpty()) {
                    errorrLbl.setText("Por favor llene todos los campos");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (!correo.contains("@") || !correo.contains(".")) {
                    errorrLbl.setText("Correo inválido");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (contraseña.length() < 6) {
                    errorrLbl.setText("La contraseña debe tener al menos 6 caracteres");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else if (!contraseña.equals(confiContraseña)) {
                    errorrLbl.setText("Las contraseñas no coinciden");
                    errorrLbl.setVisibility(View.VISIBLE);
                } else {
                    // Crear cuenta
                    createAccount(correo, contraseña, usuario);
                    errorrLbl.setVisibility(View.GONE);
                }
            }
        });
}

    // Método para crear una cuenta
    private void createAccount(String email, String password, String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Verificar si la tarea fue exitosa
                        if (task.isSuccessful()) {
                            // Obtener el usuario actual
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Crear un nuevo usuario en la base de datos
                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("usuario", username);
                            usuario.put("correo", email);

                            // Añadir el usuario a la base de datos
                            db.collection("usuarios").document(user.getUid())
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(newUser.this, "Usuario registrado",
                                                Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(newUser.this, "Error al registrar usuario",
                                                Toast.LENGTH_SHORT).show();
                                    });
                            updateUI(user);
                        }
                        else {
                            // Verificar si el correo ya está registrado
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorrLbl.setText("El correo ya está registrado");
                                errorrLbl.setVisibility(View.VISIBLE);
                            } else {
                                errorrLbl.setText("Error al registrar usuario");
                                errorrLbl.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(newUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}