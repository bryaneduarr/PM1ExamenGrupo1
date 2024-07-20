package com.example.pm1examengrupo1.Models;

public class Personas {
  private int id;
  private String foto;
  private String nombre;
  private int telefono;
  private int latitud;
  private int longitud;

  public Personas(int id, String foto, String nombre, int telefono, int latitud, int longitud) {
    this.id = id;
    this.foto = foto;
    this.nombre = nombre;
    this.telefono = telefono;
    this.latitud = latitud;
    this.longitud = longitud;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFoto() {
    return foto;
  }

  public void setFoto(String foto) {
    this.foto = foto;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public int getTelefono() {
    return telefono;
  }

  public void setTelefono(int telefono) {
    this.telefono = telefono;
  }

  public int getLatitud() {
    return latitud;
  }

  public void setLatitud(int latitud) {
    this.latitud = latitud;
  }

  public int getLongitud() {
    return longitud;
  }

  public void setLongitud(int longitud) {
    this.longitud = longitud;
  }
}