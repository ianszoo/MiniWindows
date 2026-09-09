/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import java.io.Serializable;
import java.util.Date;

/**
 * @author David Suazo Palao
 */
public class MensajeInbox implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tipo { TEXTO, STICKER }

    private String emisor;
    private String receptor;
    private Date fecha;
    private String texto; 
    private Tipo tipo;
    private boolean leido;

    public MensajeInbox(String emisor, String receptor, String texto, Tipo tipo) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.texto = texto;
        this.tipo = tipo;
        this.fecha = new Date();
        this.leido = false;
    }

    public String getEmisor() { return emisor; }
    public String getReceptor() { return receptor; }
    public Date getFecha() { return fecha; }
    public String getTexto() { return texto; }
    public Tipo getTipo() { return tipo; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}