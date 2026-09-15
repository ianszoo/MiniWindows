/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import java.io.Serializable;

/**
 *
 * @author Ian Suazo Palao
 */
public class Stickers implements Serializable{
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String rutaArchivo;
    private boolean esGlobal;

    public Stickers(String nombre, String rutaArchivo, boolean esGlobal) {
        this.nombre = nombre;
        this.rutaArchivo = rutaArchivo;
        this.esGlobal = esGlobal;
    }

    public String getNombre() { return nombre; }
    public String getRutaArchivo() { return rutaArchivo; }
    public boolean isEsGlobal() { return esGlobal; }
}
