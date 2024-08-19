package com.example.diariopersonal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.diariopersonal.Model.Nota;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PagNueva extends AppCompatActivity {

    private TextView fechaLbl, estadoLbl, lblUbicacion;
    private EditText tituloTxt, contenidoTxt;
    private FloatingActionButton guardarBtn, btnCamara, abrirMapa;
    private DocumentReference notaRef;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pag_nueva);

        // Inicializar componentes de la UI
        fechaLbl = findViewById(R.id.lblFecha);
        estadoLbl = findViewById(R.id.lblEstado);
        tituloTxt = findViewById(R.id.txtTItulo);
        contenidoTxt = findViewById(R.id.txtContenido);
        guardarBtn = findViewById(R.id.btnRecuperar);
        btnCamara = findViewById(R.id.btnCamara);
        abrirMapa = findViewById(R.id.abrirMapa);
        lblUbicacion = findViewById(R.id.lblUbicacion);

        // Inicializar el cliente de localización
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Listener para el botón de cámara para obtener la ubicación
        btnCamara.setOnClickListener(v -> checkLocationPermission());

        // Listener para el botón abrirMapa para abrir Google Maps
        abrirMapa.setOnClickListener(v -> openLocationInMap());

        // Recuperar la nota del Intent
        Nota nota = (Nota) getIntent().getSerializableExtra("nota");
        if (nota != null) {
            tituloTxt.setText(nota.getTitulo());
            contenidoTxt.setText(nota.getContenido());
            latitude = nota.getLatitude();
            longitude = nota.getLongitude();
            if (latitude != 0.0 && longitude != 0.0) {
                lblUbicacion.setText("Latitud: " + latitude + ", Longitud: " + longitude);
            }
            notaRef = FirebaseFirestore.getInstance().collection("notas").document(nota.getId());
        }

        fechaLbl.setText(getCurrentDate());

        guardarBtn.setOnClickListener(v -> saveNote());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        lblUbicacion.setText("Latitud: " + latitude + ", Longitud: " + longitude);
                    } else {
                        lblUbicacion.setText("No se pudo obtener la ubicación");
                    }
                });
    }

    private void openLocationInMap() {
        if (latitude != 0.0 && longitude != 0.0) {
            Uri locationUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
            startActivity(mapIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @NonNull
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void saveNote() {
        String titulo = tituloTxt.getText().toString();
        String contenido = contenidoTxt.getText().toString();
        String fecha = getCurrentDate();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (titulo.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(this, "El título y el contenido no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (notaRef == null) {
            notaRef = FirebaseFirestore.getInstance().collection("notas").document();
        }

        Nota nota = new Nota(notaRef.getId(), titulo, contenido, fecha, user != null ? user.getUid() : null);
        nota.setLatitude(latitude);
        nota.setLongitude(longitude);

        notaRef.set(nota)
                .addOnSuccessListener(aVoid -> {
                    estadoLbl.setText("Cambios guardados");
                    Toast.makeText(PagNueva.this, "Nota guardada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PagNueva.this, Todo.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PagNueva.this, "Error al guardar la nota", Toast.LENGTH_SHORT).show();
                });
    }
}
