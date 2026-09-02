/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Ian Suazo Palao
 */
public class SistemadeArchivos {
    public static final String RUTA_RAIZ_SIMULADA="./Disco_Z"; 
    public static final String ARCHIVO_USUARIOS=RUTA_RAIZ_SIMULADA+"/usuarios.sop";
    
    public static void inicializarSistema(){
        File raiz = new File(RUTA_RAIZ_SIMULADA);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }

        // Crear usuario admin por defecto si no existe el archivo binario
        File fileUsuarios = new File(ARCHIVO_USUARIOS);
        if (!fileUsuarios.exists()) {
            Lista<Usuario> listaInicial = new Lista<>();
            Usuario admin=new Usuario("admin", "Admin2026!", true);
            listaInicial.agregar(admin);
            guardarUsuarios(listaInicial);
            crearEstructuraUsuario("admin");
        }
    }
    
    public static synchronized void guardarUsuarios(Lista<Usuario> usuarios){
        try(
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_USUARIOS))){
            oos.writeObject(usuarios);
        } 
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
  
    public static synchronized Lista<Usuario> cargarUsuarios() throws CorruptoException{
        File file = new File(ARCHIVO_USUARIOS);
        if (!file.exists()) return new Lista<>();

        try (
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
            return ((Lista<Usuario>)ois.readObject());
        } 
        catch (Exception e){
            throw new CorruptoException(
                    "Error al leer el archivo binario de usuarios: usuarios.sop");
        }
    }
    
    public static void crearEstructuraUsuario(String username) {
        File userDir = new File(RUTA_RAIZ_SIMULADA + "/" + username);
        if (!userDir.exists()) {
            userDir.mkdirs();
            new File(userDir, "Mis Documentos").mkdirs();
            new File(userDir, "Música").mkdirs();
            new File(userDir, "Mis Imágenes").mkdirs();
        }
    }

    public static Usuario autenticar(String username, String password) throws CorruptoException {
        Lista<Usuario> usuarios=cargarUsuarios();
        Nodo<Usuario> actual=usuarios.getHead();
        while (actual!=null){
            Usuario u =actual.getDato();
            if (u.getUsername().equalsIgnoreCase(username) && u.getPass().equals(password)){
                return u;
            }
            actual=actual.getSiguiente();
        }
        return null;
    }
    
    public static void registrarUsuario(String username, String password, boolean esAdmin) throws UsernameDuplicadoException,PasswordInvalidEsception,CorruptoException {
        
        Password.validar(password);
        Lista<Usuario> usuarios = cargarUsuarios();
        
        Nodo<Usuario> actual = usuarios.getHead();
        while (actual != null){
            if (actual.getDato().getUsername().equalsIgnoreCase(username)){
                throw new UsernameDuplicadoException("El usuario '"+username+"' ya existe en el sistema.");
            }
            actual = actual.getSiguiente();
        }

        Usuario nuevo = new Usuario(username,password,esAdmin);
        usuarios.agregar(nuevo);
        guardarUsuarios(usuarios);
        crearEstructuraUsuario(username);
    }
}
