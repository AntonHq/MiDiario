package com.example.diariopersonal;

public class Nota {
    private String titulo;
    private String contenido;
    private String fecha;
    private String userId;

    // Constructor vacío
    public Nota() {
    }

    public Nota(String titulo, String contenido, String fecha, String userId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = fecha;
        this.userId = userId;
    }

    // Getters y Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
