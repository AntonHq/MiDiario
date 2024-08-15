package com.example.diariopersonal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diariopersonal.Adapter.NotaAdapter;
import com.example.diariopersonal.Model.Nota;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class Todo extends AppCompatActivity {
    private RecyclerView recyclerViewNotas;
    private NotaAdapter notaAdapter;
    private List<Nota> notaList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ListenerRegistration notasListener; // Para manejar el listener de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        recyclerViewNotas = findViewById(R.id.recyclerViewNotas);
        recyclerViewNotas.setLayoutManager(new LinearLayoutManager(this));

        notaAdapter = new NotaAdapter(notaList, new NotaAdapter.OnNotaClickListener() {
            @Override
            public void onEditNotaClick(Nota nota, int position) {
                editarNota(nota);
            }

            @Override
            public void onDeleteNotaClick(Nota nota, int position) {
                eliminarNota(nota, position);
            }
        });

        recyclerViewNotas.setAdapter(notaAdapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        escucharCambiosNotas();
    }

    private void escucharCambiosNotas() {
        if (user != null) {
            notasListener = db.collection("notas")
                    .whereEqualTo("userId", user.getUid())
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(Todo.this, "Error al escuchar cambios", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            notaList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Nota nota = document.toObject(Nota.class);
                                nota.setId(document.getId()); // Asignar el ID del documento
                                notaList.add(nota);
                            }
                            notaAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detener el listener cuando la actividad se destruya para evitar fugas de memoria
        if (notasListener != null) {
            notasListener.remove();
        }
    }

    private void editarNota(Nota nota) {
        Intent intent = new Intent(Todo.this, PagNueva.class);
        intent.putExtra("nota", nota); // Pasar la nota como un extra
        startActivity(intent);
    }

    private void eliminarNota(Nota nota, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Nota")
                .setMessage("¿Estás seguro de que deseas eliminar esta nota?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Proceder con la eliminación
                    db.collection("notas").document(nota.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                notaList.remove(position);
                                notaAdapter.notifyItemRemoved(position);
                                Toast.makeText(Todo.this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(Todo.this, "Error al eliminar nota", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
