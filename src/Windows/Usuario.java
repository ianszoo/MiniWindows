/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Ian Suazo Palao
 */
public class Usuario implements Serializable{
    private static final long serialVersionUID = 1L;
    private String username;
    private String pass;
    private boolean esAdmin;
    private Date fechaCreacion;

    // Campos solicitados para INSTA+
    private String nombreCompleto;
    private char genero; // 'M' o 'F'
    private int edad;
    private boolean activo;
    private String fotoPerfil;

    public Usuario(String username, String pass, boolean esAdmin) {
        this(username, pass, esAdmin, username, 'M', 20, null);
    }

    public Usuario(String username, String pass, boolean esAdmin, String nombreCompleto, char genero, int edad, String fotoPerfil) {
        this.username = username;
        this.pass = pass;
        this.esAdmin = esAdmin;
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto : username;
        this.genero = genero;
        this.edad = edad > 0 ? edad : 18;
        this.activo = true;
        this.fotoPerfil = fotoPerfil;
        this.fechaCreacion = new Date();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }

    public Date getFechaCreacion() { return fechaCreacion; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public char getGenero() { return genero; }
    public void setGenero(char genero) { this.genero = genero; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Usuario) {
            return this.username.equalsIgnoreCase(((Usuario) obj).username);
        }
        return false;
    }
    
    
}
