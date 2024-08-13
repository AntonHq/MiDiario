package com.example.diariopersonal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PagNueva extends AppCompatActivity {
    private TextView fechaLbl, estadoLbl;
    private EditText tituloTxt;
    private TextInputEditText contenidoTxt;
    private ImageButton guardarBtn, btnCamara;
    private DocumentReference notaRef; //Referencia al documento de la nota en Firestore
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CAMERA = 100;
    private ImageView imgVistaPrevia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pag_nueva);

        //inicializar componentes
        fechaLbl = findViewById(R.id.lblFecha);
        estadoLbl = findViewById(R.id.lblEstado);
        tituloTxt = findViewById(R.id.txtTItulo);
        contenidoTxt = findViewById(R.id.txtContenido);
        guardarBtn = findViewById(R.id.btnGuardar);
        btnCamara = findViewById(R.id.btnCamara);
        imgVistaPrevia = findViewById(R.id.imgVistaPrevia);


        // Listener para el botón de la cámara
        btnCamara.setOnClickListener(view -> {
            // Verificar permisos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_PERMISSION_CAMERA);
            } else {
                // Permiso ya concedido, abrir la cámara
                abrirCamara();
            }
        });


        // Configurar la fecha actual
        fechaLbl.setText(getCurrentDate());

        // Detectar cambios en los campos de texto
        tituloTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                estadoLbl.setText("Cambios no guardados");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        contenidoTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                estadoLbl.setText("Cambios no guardados");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Configurar el listener para guardar la nota
        guardarBtn.setOnClickListener(v -> saveNote());
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Mostrar la imagen en el ImageView
            imgVistaPrevia.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Método para obtener la fecha actual
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Método para guardar la nota en la base de datos firestore
    private void saveNote() {
        String titulo = tituloTxt.getText().toString();
        String contenido = contenidoTxt.getText().toString();
        String fecha = getCurrentDate();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(this, "El título y el contenido no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (notaRef == null) {
            // Crear una nueva nota
            notaRef = db.collection("notas").document();
        }

        // Crear o actualizar la nota en Firestore
        Nota nota = new Nota(titulo, contenido, fecha, user != null ? user.getUid() : null);
        notaRef.set(nota)
                .addOnSuccessListener(aVoid -> {
                    estadoLbl.setText("Cambios guardados");
                    Toast.makeText(PagNueva.this, "Nota guardada/actualizada con éxito", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PagNueva.this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                });
    }

    private void addImage() {

    }
}