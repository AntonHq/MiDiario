package com.example.diariopersonal;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatDelegate;

public class Settings extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public TextView usuarioLbl, correoLbl;
    public Button cerrarBtn, eliminarBtn, cambiarNombreBtn;
    public SwitchCompat swtTema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Configurar el tema oscuro
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        usuarioLbl = findViewById(R.id.lblUsuario);
        correoLbl = findViewById(R.id.lblCorreo);
        cerrarBtn = findViewById(R.id.btnCerrar);
        eliminarBtn = findViewById(R.id.btnEliminar);
        cambiarNombreBtn = findViewById(R.id.btnCambiarNombre);
        swtTema = findViewById(R.id.swtTema);

        // Colocar el estado del switch en base a la preferencia
        swtTema.setChecked(isDarkMode);

        swtTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            // Guardar el estado del switch en las preferencias
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("usuarios").document(user.getUid());

            // Intentar cargar el nombre de usuario desde Firestore
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usuarioLbl.setText(documentSnapshot.getString("usuario"));
                } else {
                    // Si no existe en Firestore, usar el displayName de FirebaseAuth
                    if (user.getDisplayName() != null) {
                        usuarioLbl.setText(user.getDisplayName());
                    } else {
                        Toast.makeText(Settings.this, "No se encontró un nombre de usuario.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
                // En caso de error al obtener los datos de Firestore
                if (user.getDisplayName() != null) {
                    usuarioLbl.setText(user.getDisplayName());
                } else {
                    Toast.makeText(Settings.this, "Error al obtener los datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Mostrar el correo
            correoLbl.setText(user.getEmail());
        }

        // Manejar el clic en el botón de cambiar nombre
        cambiarNombreBtn.setOnClickListener(v -> {
            if (user != null) {
                boolean esProveedorCorreo = false;

                // Verificar el proveedor de autenticación
                for (UserInfo profile : user.getProviderData()) {
                    if (profile.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                        esProveedorCorreo = true;
                        break;
                    }
                }

                // Si el usuario se autenticó con correo y contraseña
                if (esProveedorCorreo) {
                    Intent intent = new Intent(Settings.this, CambiarNombre.class);
                    startActivity(intent);
                } else {
                    // Si el usuario se autenticó con Google u otro proveedor
                    Toast.makeText(Settings.this, "No se puede cambiar el nombre de usuario si iniciaste sesión con Google.", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Manejar el clic en el botón de cerrar sesión
        cerrarBtn.setOnClickListener(v -> mostrarDialogoConfirmacion("Cerrar sesión", "¿Estás seguro que deseas cerrar sesión?", this::cerrarSesion));

        // Manejar el clic en el botón de eliminar cuenta
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