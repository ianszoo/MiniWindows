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

            publicarDemo("noticias", "¡Lanzamiento oficial de Mini-Windows v2.0! #Sistemas #Tecnologia @admin", "📢", false);
            publicarDemo("noticias", "Historia del día: Actualización completada", "✨", true);
            publicarDemo("deportes", "Gran final de fútbol hoy a las 8PM @todos #Deporte #Campeonato", "⚽", false);
            publicarDemo("deportes", "Entrenando temprano", "🔥", true);
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
            new File(uDir, "stickers_personales").mkdirs();

            guardarListaGenerica(new File(uDir, "following.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "followers.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "insta.ins"), new Lista<Publicacion>());
            guardarListaGenerica(new File(uDir, "inbox.ins"), new Lista<MensajeInbox>());

            Lista<String> stickers = new Lista<>();
            stickers.agregar("😊 Feliz");
            stickers.agregar("😢 Triste");
            stickers.agregar("❤️ Corazón");
            stickers.agregar("😂 Risa");
            stickers.agregar("👏 Aplauso");
            guardarListaGenerica(new File(uDir, "stickers.ins"), stickers);
        }
    }

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

    public static Lista<Publicacion> cargarPublicaciones(String username) {
        File f = new File(RUTA_INSTA + "/" + username + "/insta.ins");
        return cargarListaGenerica(f);
    }

    public static void guardarPublicaciones(String username, Lista<Publicacion> posts) {
        File f = new File(RUTA_INSTA + "/" + username + "/insta.ins");
        guardarListaGenerica(f, posts);
    }

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
}
