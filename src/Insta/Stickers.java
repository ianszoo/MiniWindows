/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import java.io.Serializable;

/**
 * @author Ian Suazo Palao & David Suazo Palao
 */
public class Stickers implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String rutaArchivo;
    private boolean esGlobal;

    public Stickers(String nombre, String rutaArchivo, boolean esGlobal) {
        this.nombre = nombre;
        this.rutaArchivo = rutaArchivo;
        this.esGlobal = esGlobal;
    }

    public String getNombre() {
        return nombre; 
    }
    public void setNombre(String nombre) {
        this.nombre = nombre; 
    }

    public String getRutaArchivo() {
        return rutaArchivo; 
    }
    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo; 
    }

    public boolean isEsGlobal() {
        return esGlobal; 
    }
    public void setEsGlobal(boolean esGlobal) {
        this.esGlobal = esGlobal; 
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stickers) {
            Stickers otro = (Stickers) obj;
            if (this.rutaArchivo != null && otro.rutaArchivo != null) {
                return this.rutaArchivo.equalsIgnoreCase(otro.rutaArchivo);
            }
            if (this.nombre != null && otro.nombre != null) {
                return this.nombre.equalsIgnoreCase(otro.nombre);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
