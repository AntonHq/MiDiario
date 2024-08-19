package com.example.diariopersonal.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diariopersonal.Model.Nota;
import com.example.diariopersonal.R;

import java.util.List;

public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {

    private List<Nota> notaList;
    private OnNotaClickListener listener;

    public NotaAdapter(List<Nota> notaList, OnNotaClickListener listener) {
        this.notaList = notaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nota, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        Nota nota = notaList.get(position);
        holder.txtTituloNota.setText(nota.getTitulo());
        holder.txtFechaNota.setText(nota.getFecha());
        // Mostrar solo una pequeña línea del contenido
        holder.txtContenidoNota.setText(nota.getContenido().length() > 50
                ? nota.getContenido().substring(0, 10) + "..."
                : nota.getContenido());

        holder.btnEditarNota.setOnClickListener(v -> listener.onEditNotaClick(nota, position));
        holder.btnEliminarNota.setOnClickListener(v -> listener.onDeleteNotaClick(nota, position));

        // Listener para detectar click en la nota completa (si quieres agregar esta funcionalidad)
        holder.itemView.setOnClickListener(v -> listener.onEditNotaClick(nota, position));
    }

    @Override
    public int getItemCount() {
        return notaList.size();
    }

    public static class NotaViewHolder extends RecyclerView.ViewHolder {

        TextView txtTituloNota, txtFechaNota, txtContenidoNota;
        ImageButton btnEditarNota, btnEliminarNota;


        public NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloNota = itemView.findViewById(R.id.txtTituloNota);
            txtFechaNota = itemView.findViewById(R.id.txtFechaNota);
            txtContenidoNota = itemView.findViewById(R.id.txtContenidoNota);
            btnEditarNota = itemView.findViewById(R.id.btnEditarNota);
            btnEliminarNota = itemView.findViewById(R.id.btnEliminarNota);
        }
    }

    public interface OnNotaClickListener {
        void onEditNotaClick(Nota nota, int position);
        void onDeleteNotaClick(Nota nota, int position);
    }
}