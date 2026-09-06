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
import Windows.Lista;
import Windows.Nodo;
import Windows.SistemadeArchivos;
import Windows.Usuario;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

public class InstaFileManager {
    public static final String RUTA_INSTA = SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/INSTA_RAIZ";
    public static final String ARCHIVO_USERS_INS = RUTA_INSTA + "/users.ins";

    public static synchronized void inicializarInsta() {
        File raiz = new File(RUTA_INSTA);
        if (!raiz.exists()) raiz.mkdirs();

        File globStickers = new File(RUTA_INSTA + "/stickers_globales");
        if (!globStickers.exists()) globStickers.mkdirs();

        File userIns = new File(ARCHIVO_USERS_INS);
        if (!userIns.exists()) {
            Lista<Usuario> iniciales = new Lista<>();
            Usuario uAdmin = new Usuario("admin", "Admin2026!", true, "Administrador Sistema", 'M', 25, null);
            Usuario uNoticias = new Usuario("noticias", "Noticias2026!", false, "Canal Noticias OS", 'M', 30, null);
            Usuario uDeportes = new Usuario("deportes", "Deportes2026!", false, "Deportes Extremos", 'M', 22, null);
            Usuario uModa = new Usuario("entretenimiento", "Moda2026!", false, "Mundo y Tendencias", 'F', 24, null);

            iniciales.agregar(uAdmin);
            iniciales.agregar(uNoticias);
            iniciales.agregar(uDeportes);
            iniciales.agregar(uModa);
            guardarUsuariosInsta(iniciales);

            Nodo<Usuario> n = iniciales.getHead();
            while (n != null) {
                crearEspacioUsuarioInsta(n.getDato().getUsername());
                n = n.getSiguiente();
            }

            // Publicaciones iniciales de demostración
            publicarDemo("noticias", "Lanzamiento oficial de Mini-Windows v2.0 #Sistemas #Tecnologia @admin", "📢", false);
            publicarDemo("deportes", "Gran final de fútbol hoy a las 8PM @todos #Deporte #Campeonato", "⚽", false);
            publicarDemo("entretenimiento", "Tendencias de moda en tecnología 2026 #Moda @admin", "💖", false);
        }
    }

    private static void publicarDemo(String autor, String txt, String sticker, boolean esHistoria) {
        Publicacion p = new Publicacion(autor, txt, null, sticker, esHistoria);
        Lista<Publicacion> posts = cargarPublicaciones(autor);
        posts.agregar(p);
        guardarPublicaciones(autor, posts);
    }

    public static synchronized void crearEspacioUsuarioInsta(String username) {
        File uDir = new File(RUTA_INSTA + "/" + username);
        if (!uDir.exists()) {
            uDir.mkdirs();
            new File(uDir, "imagenes").mkdirs();
            new File(uDir, "folders_personales").mkdirs();
            new File(uDir, "folders_personales/General").mkdirs();
            new File(uDir, "folders_personales/Viajes").mkdirs();
            new File(uDir, "stickers_personales").mkdirs();

            guardarListaGenerica(new File(uDir, "following.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "followers.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "insta.ins"), new Lista<Publicacion>());
            guardarListaGenerica(new File(uDir, "inbox.ins"), new Lista<MensajeInbox>());

            // 5 Stickers iniciales obligatorios
            Lista<String> stickers = new Lista<>();
            stickers.agregar("😊 Feliz");
            stickers.agregar("😢 Triste");
            stickers.agregar("❤️ Corazón");
            stickers.agregar("😂 Risa");
            stickers.agregar("👏 Aplauso");
            guardarListaGenerica(new File(uDir, "stickers.ins"), stickers);
        }
    }

    // --- PERSISTENCIA BINARIA GENÉRICA ---
    public static synchronized <T> void guardarListaGenerica(File file, Lista<T> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> Lista<T> cargarListaGenerica(File file) {
        if (!file.exists()) return new Lista<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Lista<T>) ois.readObject();
        } catch (Exception e) {
            return new Lista<>();
        }
    }

    // --- GESTIÓN DE USUARIOS ---
    public static synchronized Lista<Usuario> cargarUsuariosInsta() {
        return cargarListaGenerica(new File(ARCHIVO_USERS_INS));
    }

    public static synchronized void guardarUsuariosInsta(Lista<Usuario> usuarios) {
        guardarListaGenerica(new File(ARCHIVO_USERS_INS), usuarios);
    }

    public static synchronized Usuario buscarUsuario(String username) {
        Lista<Usuario> lista = cargarUsuariosInsta();
        Nodo<Usuario> n = lista.getHead();
        while (n != null) {
            if (n.getDato().getUsername().equalsIgnoreCase(username)) {
                return n.getDato();
            }
            n = n.getSiguiente();
        }
        return null;
    }

    public static synchronized void actualizarUsuario(Usuario modificado) {
        Lista<Usuario> lista = cargarUsuariosInsta();
        Nodo<Usuario> n = lista.getHead();
        while (n != null) {
            if (n.getDato().getUsername().equalsIgnoreCase(modificado.getUsername())) {
                n.setDato(modificado);
                break;
            }
            n = n.getSiguiente();
        }
        guardarUsuariosInsta(lista);
    }

    // --- PUBLICACIONES ---
    public static Lista<Publicacion> cargarPublicaciones(String username) {
        File f = new File(RUTA_INSTA + "/" + username + "/insta.ins");
        return cargarListaGenerica(f);
    }

    public static void guardarPublicaciones(String username, Lista<Publicacion> posts) {
        File f = new File(RUTA_INSTA + "/" + username + "/insta.ins");
        guardarListaGenerica(f, posts);
    }

    // --- SEGUIMIENTO ---
    public static Lista<String> cargarSeguidos(String username) {
        return cargarListaGenerica(new File(RUTA_INSTA + "/" + username + "/following.ins"));
    }

    public static Lista<String> cargarSeguidores(String username) {
        return cargarListaGenerica(new File(RUTA_INSTA + "/" + username + "/followers.ins"));
    }

    public static synchronized boolean toggleSeguir(String usuarioActual, String usuarioDestino) {
        if (usuarioActual.equalsIgnoreCase(usuarioDestino)) return false;

        File fFollowing = new File(RUTA_INSTA + "/" + usuarioActual + "/following.ins");
        File fFollowers = new File(RUTA_INSTA + "/" + usuarioDestino + "/followers.ins");

        Lista<String> following = cargarListaGenerica(fFollowing);
        Lista<String> followers = cargarListaGenerica(fFollowers);

        boolean yaLoSigue = false;
        Nodo<String> curr = following.getHead();
        while (curr != null) {
            if (curr.getDato().equalsIgnoreCase(usuarioDestino)) {
                yaLoSigue = true;
                break;
            }
            curr = curr.getSiguiente();
        }

        if (yaLoSigue) {
            following.eliminar(usuarioDestino.toLowerCase());
            followers.eliminar(usuarioActual.toLowerCase());
            guardarListaGenerica(fFollowing, following);
            guardarListaGenerica(fFollowers, followers);
            return false;
        } else {
            following.agregar(usuarioDestino.toLowerCase());
            followers.agregar(usuarioActual.toLowerCase());
            guardarListaGenerica(fFollowing, following);
            guardarListaGenerica(fFollowers, followers);
            return true;
        }
    }

    // --- MENSAJERÍA DIRECTA (INBOX) ---
    public static synchronized void enviarMensaje(String emisor, String receptor, String texto, MensajeInbox.Tipo tipo) {
        MensajeInbox msg = new MensajeInbox(emisor, receptor, texto, tipo);

        File fEmisor = new File(RUTA_INSTA + "/" + emisor + "/inbox.ins");
        Lista<MensajeInbox> inboxEmisor = cargarListaGenerica(fEmisor);
        inboxEmisor.agregar(msg);
        guardarListaGenerica(fEmisor, inboxEmisor);

        File fReceptor = new File(RUTA_INSTA + "/" + receptor + "/inbox.ins");
        Lista<MensajeInbox> inboxReceptor = cargarListaGenerica(fReceptor);
        inboxReceptor.agregar(msg);
        guardarListaGenerica(fReceptor, inboxReceptor);
    }

    public static synchronized Lista<MensajeInbox> obtenerConversacion(String u1, String u2) {
        File f = new File(RUTA_INSTA + "/" + u1 + "/inbox.ins");
        Lista<MensajeInbox> todos = cargarListaGenerica(f);
        Lista<MensajeInbox> chat = new Lista<>();

        Nodo<MensajeInbox> n = todos.getHead();
        while (n != null) {
            MensajeInbox m = n.getDato();
            if ((m.getEmisor().equalsIgnoreCase(u1) && m.getReceptor().equalsIgnoreCase(u2)) ||
                (m.getEmisor().equalsIgnoreCase(u2) && m.getReceptor().equalsIgnoreCase(u1))) {
                chat.agregar(m);
                if (m.getReceptor().equalsIgnoreCase(u1)) {
                    m.setLeido(true);
                }
            }
            n = n.getSiguiente();
        }
        guardarListaGenerica(f, todos);
        return chat;
    }

    public static synchronized void eliminarConversacionCompleta(String u1, String u2) {
        File f = new File(RUTA_INSTA + "/" + u1 + "/inbox.ins");
        Lista<MensajeInbox> todos = cargarListaGenerica(f);
        Lista<MensajeInbox> filtrados = new Lista<>();

        Nodo<MensajeInbox> n = todos.getHead();
        while (n != null) {
            MensajeInbox m = n.getDato();
            boolean pertenece = (m.getEmisor().equalsIgnoreCase(u1) && m.getReceptor().equalsIgnoreCase(u2)) ||
                                (m.getEmisor().equalsIgnoreCase(u2) && m.getReceptor().equalsIgnoreCase(u1));
            if (!pertenece) {
                filtrados.agregar(m);
            }
            n = n.getSiguiente();
        }
        guardarListaGenerica(f, filtrados);
    }

    // --- STICKERS ---
    public static synchronized Lista<String> cargarStickers(String username) {
        Lista<String> listaStickers = new Lista<>();

        // 1. Revisar carpeta de recursos src/Insta/stickers o stickers/
        File[] carpetasPack = {
            new File("src/Insta/stickers"),
            new File("Insta/stickers"),
            new File("stickers"),
            new File(RUTA_INSTA + "/stickers_globales"),
            new File(RUTA_INSTA + "/" + username + "/stickers_personales")
        };

        for (File dir : carpetasPack) {
            if (dir.exists() && dir.isDirectory()) {
                File[] archivos = dir.listFiles((d, name) -> {
                    String n = name.toLowerCase();
                    return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif");
                });
                if (archivos != null) {
                    for (File f : archivos) {
                        // Evitamos duplicar si ya está en la lista
                        if (!listaStickers.contiene(f.getAbsolutePath())) {
                            listaStickers.agregar(f.getAbsolutePath());
                        }
                    }
                }
            }
        }

        // Si la carpeta física está vacía, retornar al menos emojis por defecto
        if (listaStickers.estaVacia()) {
            listaStickers.agregar("😊 Feliz");
            listaStickers.agregar("❤️ Corazón");
            listaStickers.agregar("🔥 Fuego");
            listaStickers.agregar("😂 Risa");
        }

        return listaStickers;
    }


    public static synchronized boolean agregarStickerPersonal(String username, File archivoSticker) {
        String name = archivoSticker.getName().toLowerCase();
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) {
            return false;
        }
        File destino = new File(RUTA_INSTA + "/" + username + "/stickers_personales/" + archivoSticker.getName());
        try {
            Files.copy(archivoSticker.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File f = new File(RUTA_INSTA + "/" + username + "/stickers.ins");
            Lista<String> lista = cargarListaGenerica(f);
            lista.agregar(archivoSticker.getName());
            guardarListaGenerica(f, lista);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static synchronized Usuario autenticarInsta(String username, String password) {
        Lista<Usuario> usuarios = cargarUsuariosInsta();
        Nodo<Usuario> n = usuarios.getHead();
        while (n != null) {
            Usuario u = n.getDato();
            if (u.getUsername().equalsIgnoreCase(username) && u.getPass().equals(password)) {
                return u;
            }
            n = n.getSiguiente();
        }
        return null;
    }

    // Registro maestro en users.ins y creación de estructura (Sección 4.2 b)
    public static synchronized boolean registrarUsuarioInsta(Usuario nuevo) {
        Lista<Usuario> usuarios = cargarUsuariosInsta();
        Nodo<Usuario> n = usuarios.getHead();
        while (n != null) {
            if (n.getDato().getUsername().equalsIgnoreCase(nuevo.getUsername())) {
                return false; // Username duplicado
            }
            n = n.getSiguiente();
        }
        usuarios.agregar(nuevo);
        guardarUsuariosInsta(usuarios);
        crearEspacioUsuarioInsta(nuevo.getUsername());
        return true;
    }
}
