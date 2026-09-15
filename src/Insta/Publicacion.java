/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import java.io.Serializable;
import java.util.Date;
import Windows.Lista;

/**
 * @author David Suazo Palao
 */
public class Publicacion implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AspectRatio { CUADRADA, VERTICAL, HORIZONTAL }

    private String id;
    private String autor;
    private Date fecha;
    private String contenido; 
    private String rutaImagen;
    private String carpetaPersonal;
    private String sticker;
    private boolean esHistoria;
    private AspectRatio formatoAspecto;
    private Lista<String> hashtags;
    private Lista<String> menciones;

    public Publicacion(String autor, String contenido, String rutaImagen, String carpetaPersonal, String sticker, boolean esHistoria, AspectRatio formato) {
        this.id = autor + "_" + System.currentTimeMillis();
        this.autor = autor;
        this.contenido = contenido != null ? (contenido.length() > 220 ? contenido.substring(0, 220) : contenido) : "";
        this.rutaImagen = rutaImagen;
        this.carpetaPersonal = carpetaPersonal != null ? carpetaPersonal : "General";
        this.sticker = sticker;
        this.esHistoria = esHistoria;
        this.formatoAspecto = formato != null ? formato : AspectRatio.CUADRADA;
        this.fecha = new Date();
        this.hashtags = new Lista<>();
        this.menciones = new Lista<>();
        extraerTagsYMenciones();
    }

    private void extraerTagsYMenciones() {
        if (contenido == null || contenido.isEmpty()) return;
        String[] palabras = contenido.split("\\s+");
        for (String p : palabras) {
            if (p.startsWith("#") && p.length() > 1) {
                String tag = p.toLowerCase().replaceAll("[^a-záéíóúñ0-9_#]", "");
                if (!hashtags.contiene(tag)) hashtags.agregar(tag);
            } else if (p.startsWith("@") && p.length() > 1) {
                String user = p.substring(1).toLowerCase().replaceAll("[^a-záéíóúñ0-9_]", "");
                if (!menciones.contiene(user)) menciones.agregar(user);
            }
        }
    }

    public String getId() {
        return id; 
    }
    public String getAutor() {
        return autor; 
    }
    public Date getFecha() {
        return fecha; 
    }
    public String getContenido() {
        return contenido; 
    }
    public String getRutaImagen() {
        return rutaImagen; 
    }
    public String getCarpetaPersonal() {
        return carpetaPersonal; 
    }
    public String getSticker() {
        return sticker; 
    }
    public boolean isEsHistoria() {
        return esHistoria; 
    }
    public AspectRatio getFormatoAspecto() {
        return formatoAspecto; 
    }
    public Lista<String> getHashtags() {
        return hashtags; 
    }
    public Lista<String> getMenciones() {
        return menciones; 
    }
}