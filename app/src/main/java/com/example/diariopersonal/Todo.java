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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Todo extends AppCompatActivity {
    private RecyclerView recyclerViewNotas;
    private NotaAdapter notaAdapter;
    private List<Nota> notaList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        recyclerViewNotas = findViewById(R.id.recyclerViewNotas);
        recyclerViewNotas.setLayoutManager(new LinearLayoutManager(this));

        notaAdapter = new NotaAdapter(notaList, new NotaAdapter.OnNotaClickListener() {
            @Override
            public void onEditNotaClick(Nota nota, int position) {
                editNota(nota);
            }

            @Override
            public void onDeleteNotaClick(Nota nota, int position) {
                deleteNota(nota, position);
            }
        });

        recyclerViewNotas.setAdapter(notaAdapter);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadNotas();
    }

    private void loadNotas() {
        if (user != null) {
            db.collection("notas")
                    .whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            notaList.clear();
                            notaList.addAll(queryDocumentSnapshots.toObjects(Nota.class));
                            notaAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(Todo.this, "Error al cargar notas", Toast.LENGTH_SHORT).show());
        }
    }

    private void editNota(Nota nota) {
        Intent intent = new Intent(Todo.this, PagNueva.class);
        intent.putExtra("nota", Nota.class);
        startActivity(intent);
    }

    private void deleteNota(Nota nota, int position) {
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