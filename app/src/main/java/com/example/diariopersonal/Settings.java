package com.example.diariopersonal;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatDelegate;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public TextView usuarioLbl, correoLbl;
    public Button cerrarBtn, eliminarBtn, cambiarDatosBtn;
    private Switch swtTema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        usuarioLbl = findViewById(R.id.lblUsuario);
        correoLbl = findViewById(R.id.lblCorreo);
        cerrarBtn = findViewById(R.id.btnCerrar);
        eliminarBtn = findViewById(R.id.btnEliminar);
        cambiarDatosBtn = findViewById(R.id.btnCambiarDatos);
        swtTema = findViewById(R.id.swtTema);

        // colocar el estado del switch en base a la preferencia
        swtTema.setChecked(isDarkMode);

        swtTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // guardar el estado del switch en las preferencias
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection("usuarios").document(user.getUid());

        // Mostrar el nombre de usuario y correo en la vista
        if (user != null) {
            if (user.getDisplayName() != null) {
                usuarioLbl.setText(user.getDisplayName());
            } else {
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        usuarioLbl.setText(documentSnapshot.getString("usuario"));
                    } else {
                        Toast.makeText(Settings.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            correoLbl.setText(user.getEmail());
        }

        cerrarBtn.setOnClickListener(v -> mostrarDialogoConfirmacion("Cerrar sesión", "¿Estás seguro que deseas cerrar sesión?", this::cerrarSesion));
        eliminarBtn.setOnClickListener(v -> mostrarDialogoConfirmacion("Eliminar cuenta", "¿Estás seguro que deseas eliminar tu cuenta?", this::eliminarCuenta));
    }

    private void mostrarDialogoConfirmacion(String titulo, String mensaje, Runnable accionConfirmar) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> accionConfirmar.run())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(Settings.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void eliminarCuenta() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("usuarios").document(user.getUid()).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLoggedIn", false);
                            editor.apply();

                            Intent intent = new Intent(Settings.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Settings.this, "Error al eliminar la cuenta: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Settings.this, "Error al eliminar los datos del usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}