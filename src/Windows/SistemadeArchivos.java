package Windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Ian Suazo Palao
 */
public class SistemadeArchivos {
    public static final String RUTA_RAIZ_SIMULADA = "./Disco_Z"; 
    public static final String ARCHIVO_USUARIOS = RUTA_RAIZ_SIMULADA + "/usuarios.sop";
    
    public static void inicializarSistema() {
        File raiz = new File(RUTA_RAIZ_SIMULADA);
        if (!raiz.exists()) {
            raiz.mkdirs();
        }

        File fileUsuarios = new File(ARCHIVO_USUARIOS);
        if (!fileUsuarios.exists()) {
            Lista<Usuario> listaInicial = new Lista<>();
            Usuario adminTest = new Usuario("admin", "Admin2026!", true, "Administrador del Sistema", 'M', 30, null);
            adminTest.setActivo(true);
            listaInicial.agregar(adminTest);
            guardarUsuarios(listaInicial);
            crearEstructuraUsuario("admin");
        } else {
            try {
                Lista<Usuario> list = cargarUsuarios();
                Nodo<Usuario> cur = list.getHead();
                boolean mod = false;
                while (cur != null) {
                    if (cur.getDato().getUsername().equalsIgnoreCase("admin") && !cur.getDato().isActivo()) {
                        cur.getDato().setActivo(true);
                        mod = true;
                    }
                    cur = cur.getSiguiente();
                }
                if (mod) guardarUsuarios(list);
            } catch (Exception ignored) {}
        }
    }
    
    public static synchronized void guardarUsuarios(Lista<Usuario> usuarios) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_USUARIOS))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static synchronized Lista<Usuario> cargarUsuarios() throws CorruptoException {
        File file = new File(ARCHIVO_USUARIOS);
        if (!file.exists()) return new Lista<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Lista<Usuario> list = (Lista<Usuario>) ois.readObject();
            Nodo<Usuario> n = list.getHead();
            while (n != null) {
                if (n.getDato().getUsername().equalsIgnoreCase("admin")) {
                    n.getDato().setActivo(true);
                }
                n = n.getSiguiente();
            }
            return list;
        } catch (Exception e) {
            throw new CorruptoException("Error al leer el archivo binario de usuarios: usuarios.sop");
        }
    }
    
    public static boolean hayAdmin() {
        try {
            Lista<Usuario> list = cargarUsuarios();
            Nodo<Usuario> actual = list.getHead();
            while (actual != null) {
                if (actual.getDato().isEsAdmin()) return true;
                actual = actual.getSiguiente();
            }
        } catch (Exception ignored) {}
        return false;
    }
    
    public static void crearEstructuraUsuario(String username) {
        File userDir = new File(RUTA_RAIZ_SIMULADA + "/" +username);
        if (!userDir.exists()) {
            userDir.mkdirs();
            new File(userDir, "Mis Documentos").mkdirs();
            new File(userDir, "Música").mkdirs();
            new File(userDir, "Mis Imágenes").mkdirs();
        }
    }

    public static Usuario autenticar(String username, String password) throws CorruptoException {
        Lista<Usuario> usuarios = cargarUsuarios();
        Nodo<Usuario> actual = usuarios.getHead();
        while (actual != null) {
            Usuario u = actual.getDato();
            if (u.getUsername().equalsIgnoreCase(username) && u.getPass().equals(password)) {
                return u;
            }
            actual = actual.getSiguiente();
        }
        return null;
    }
    
    public static void registrarUsuario(String username, String password, boolean esAdmin) 
            throws UsernameDuplicadoException, PasswordInvalidEsception, CorruptoException {
        
        Password.validar(password);
        Lista<Usuario> usuarios = cargarUsuarios();
        
        if (!hayAdmin() && !esAdmin) {
            throw new PasswordInvalidEsception("El primer usuario del sistema debe ser creado como Administrador.");
        }
        
        Nodo<Usuario> actual = usuarios.getHead();
        while (actual != null) {
            if (actual.getDato().getUsername().equalsIgnoreCase(username)) {
                throw new UsernameDuplicadoException("El usuario '"+username+"' ya existe en el sistema.");
            }
            actual = actual.getSiguiente();
        }

        Usuario nuevo = new Usuario(username, password, esAdmin);
        nuevo.setActivo(true);
        usuarios.agregar(nuevo);
        guardarUsuarios(usuarios);
        crearEstructuraUsuario(username);
    }
}