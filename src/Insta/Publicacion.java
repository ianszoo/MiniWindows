/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

/**
 *
 * @author David Suazo Palao
 */
import java.io.Serializable;
import java.util.Date;
import Windows.Lista;
public class Publicacion implements Serializable{
    private static final long serialVersionUID = 1L;

    private String id;
    private String autor;
    private Date fecha;
    private String contenido;
    private String rutaImagen;
    private String sticker;
    private Lista<String> hashtags;
    private Lista<String> menciones;
    private boolean esHistoria;

    public Publicacion(String autor, String contenido, String rutaImagen, String sticker, boolean esHistoria) {
        this.id = autor + "_" + System.currentTimeMillis();
        this.autor = autor;
        this.contenido = contenido != null ? contenido : "";
        this.rutaImagen = rutaImagen;
        this.sticker = sticker;
        this.esHistoria = esHistoria;
        this.fecha = new Date();
        this.hashtags = new Lista<>();
        this.menciones = new Lista<>();
        extraerTagsYMenciones();
    }

    private void extraerTagsYMenciones() {
        if (contenido == null) return;
        String[] palabras = contenido.split("\\s+");
        for (String p : palabras) {
            if (p.startsWith("#") && p.length() > 1) {
                hashtags.agregar(p.toLowerCase());
            } else if (p.startsWith("@") && p.length() > 1) {
                menciones.agregar(p.substring(1).toLowerCase());
            }
        }
    }

    public String getId() { return id; }
    public String getAutor() { return autor; }
    public Date getFecha() { return fecha; }
    public String getContenido() { return contenido; }
    public String getRutaImagen() { return rutaImagen; }
    public String getSticker() { return sticker; }
    public Lista<String> getHashtags() { return hashtags; }
    public Lista<String> getMenciones() { return menciones; }
    public boolean isEsHistoria() { return esHistoria; }
}
