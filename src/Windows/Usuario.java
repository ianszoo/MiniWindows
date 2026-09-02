/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

import java.util.Date;

/**
 *
 * @author Ian Suazo Palao
 */
public class Usuario {
    private static final long serialVersionUID=1L;
    private String username;
    private String pass;
    private boolean esAdmin;
    private Date fechaCreacion;

    public Usuario(String username, String pass, boolean esAdmin) {
        this.username = username;
        this.pass = pass;
        this.esAdmin = esAdmin;
        this.fechaCreacion=new Date();
    }

    public String getUsername() {
        return username;
    }

    public String getPass() {
        return pass;
    }

    public boolean isEsAdmin() {
        return esAdmin;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Usuario){
            return this.username.equalsIgnoreCase(((Usuario) obj).username);
        }
        return false;
    }
    
    
}
