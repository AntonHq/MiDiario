package com.example.diariopersonal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diariopersonal.Adapter.NotaAdapter;
import com.example.diariopersonal.Model.Nota;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

public class Todo extends AppCompatActivity {
    private RecyclerView recyclerViewNotas;
    private NotaAdapter notaAdapter;
    private List<Nota> notaList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ListenerRegistration notasListener;
    private FloatingActionButton btnAgregarNota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        btnAgregarNota = findViewById(R.id.btnAgregarNota);
        btnAgregarNota.setOnClickListener(view -> {
            Intent intent = new Intent(Todo.this, PagNueva.class);
            startActivity(intent);
        });

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

        if (user != null) {
            escucharCambiosNotas();
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }



    private void editarNota(Nota nota) {
        Intent intent = new Intent(Todo.this, PagNueva.class);
        intent.putExtra("nota", nota);
        startActivity(intent);
    }

    private void escucharCambiosNotas() {
        notasListener = db.collection("notas")
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(Todo.this, "Error al obtener notas: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        notaList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Nota nota = doc.toObject(Nota.class);
                            notaList.add(nota);
                        }
                        notaAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void eliminarNota(Nota nota, int position) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Nota")
                .setMessage("¿Estás seguro de que deseas eliminar esta nota?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    db.collection("notas")
                            .document(nota.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                // Buscar la posición de la nota usando el ID, en lugar de confiar solo en la posición
                                int posToRemove = -1;
                                for (int i = 0; i < notaList.size(); i++) {
                                    if (notaList.get(i).getId().equals(nota.getId())) {
                                        posToRemove = i;
                                        break;
                                    }
                                }

                                // Si se encuentra la nota en la lista, eliminarla
                                if (posToRemove != -1) {
                                    notaList.remove(posToRemove);
                                    notaAdapter.notifyItemRemoved(posToRemove);
                                }
                                Toast.makeText(Todo.this, "Nota eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(Todo.this, "Error al eliminar nota: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notasListener != null) {
            notasListener.remove();
            notasListener = null;
        }
    }
}