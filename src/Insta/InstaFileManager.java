package Insta;

import Windows.Lista;
import Windows.Nodo;
import Windows.SistemadeArchivos;
import Windows.Usuario;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;

public class InstaFileManager {
    public static final String RUTA_INSTA = SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/INSTA_RAIZ";
    public static final String ARCHIVO_USERS_INS = RUTA_INSTA + "/users.ins";
    public static final String RUTA_STICKERS_GLOBALES = RUTA_INSTA + "/stickers_globales";

    public static synchronized void inicializarInsta() {
        File raiz = new File(RUTA_INSTA);
        if (!raiz.exists()) raiz.mkdirs();

        File globStickers = new File(RUTA_STICKERS_GLOBALES);
        if (!globStickers.exists()) globStickers.mkdirs();

        asegurarStickersPorDefecto();

        File userIns = new File(ARCHIVO_USERS_INS);
        if (!userIns.exists()) {
            Lista<Usuario> iniciales = new Lista<>();
            Usuario uNoticias = new Usuario("noticias", "Noticias2026!", false, "Canal Noticias Honduras", 'M', 30, null);
            Usuario uDeportes = new Usuario("deportes", "Deportes2026!", false, "Deportes Extremos HN", 'M', 22, null);
            Usuario uModa = new Usuario("entretenimiento", "Moda2026!", false, "Mundo y Tendencias", 'F', 24, null);

            iniciales.agregar(uNoticias);
            iniciales.agregar(uDeportes);
            iniciales.agregar(uModa);
            guardarUsuariosInsta(iniciales);

            Nodo<Usuario> n = iniciales.getHead();
            while (n != null) {
                crearEspacioUsuarioInsta(n.getDato().getUsername());
                n = n.getSiguiente();
            }

            // Crear imágenes de muestra físicas si no existen
            String imgNoticia = asegurarImagenDemo("noticias", "noticia_demo.jpg", new Color(30, 58, 138), "NOTICIAS HOY");
            String imgDeporte = asegurarImagenDemo("deportes", "deporte_demo.jpg", new Color(4, 120, 87), "CAMPEONATO 2026");
            String imgModa = asegurarImagenDemo("entretenimiento", "moda_demo.jpg", new Color(190, 24, 93), "FASHION & TECH");

            publicarDemo("noticias", "Lanzamiento oficial de la plataforma #Sistemas #Tecnologia", imgNoticia, null, "Noticias", false);
            publicarDemo("deportes", "Gran final de fútbol hoy a las 8PM #Deporte #Campeonato", imgDeporte, null, "Eventos", false);
            publicarDemo("entretenimiento", "Tendencias de moda en tecnología 2026 #Moda", imgModa, null, "General", false);
        }
    }

    // Genera una imagen demo visual limpia si el usuario no tiene ninguna cargada aún
    private static String asegurarImagenDemo(String username, String nombreArchivo, Color colorFondo, String texto) {
        File dirImg = new File(RUTA_INSTA + "/" + username + "/imagenes");
        if (!dirImg.exists()) dirImg.mkdirs();

        File destino = new File(dirImg, nombreArchivo);
        if (!destino.exists()) {
            try {
                BufferedImage img = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colorFondo);
                g2.fillRect(0, 0, 600, 400);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();
                int x = (600 - fm.stringWidth(texto)) / 2;
                int y = (400 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(texto, x, y);
                g2.dispose();

                ImageIO.write(img, "jpg", destino);
            } catch (Exception ignored) {}
        }
        return destino.getAbsolutePath();
    }

    // Resuelve la imagen buscando de forma local si la ruta absoluta pertenecía a otra máquina
    public static File resolverImagenPost(String autor, String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.trim().isEmpty()) return null;

        // 1. Probar ruta directa
        File fDirecto = new File(rutaGuardada);
        if (fDirecto.exists()) return fDirecto;

        // 2. Probar en la carpeta de imágenes del autor
        File fEnUsuario = new File(RUTA_INSTA + "/" + autor + "/imagenes/" + fDirecto.getName());
        if (fEnUsuario.exists()) return fEnUsuario;

        // 3. Probar en la carpeta imágenes del proyecto
        String[] carpetasProyecto = {"Imagenes/", "src/Imagenes/", "imagenes/", "src/imagenes/"};
        for (String c : carpetasProyecto) {
            File fProy = new File(c + fDirecto.getName());
            if (fProy.exists()) return fProy;
        }

        return null;
    }

    private static void asegurarStickersPorDefecto() {
        String[] nombres = {"Feliz", "Triste", "Corazon", "Risa", "Aplauso"};
        Color[] colores = {
            new Color(250, 204, 21),
            new Color(96, 165, 250),
            new Color(244, 63, 94),
            new Color(251, 146, 60),
            new Color(74, 222, 128)
        };
        String[] simbolos = {"^ ‿ ^", "T _ T", "♥", "XD", "👏"};

        for (int i = 0; i < nombres.length; i++) {
            File f = new File(RUTA_STICKERS_GLOBALES, nombres[i] + ".png");
            if (!f.exists()) {
                try {
                    BufferedImage img = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(colores[i]);
                    g2.fillOval(5, 5, 110, 110);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (120 - fm.stringWidth(simbolos[i])) / 2;
                    int ty = (120 + fm.getAscent() - fm.getDescent()) / 2 - 2;
                    g2.drawString(simbolos[i], tx, ty);

                    g2.dispose();
                    ImageIO.write(img, "png", f);
                } catch (Exception ignored) {}
            }
        }
    }

    private static void publicarDemo(String autor, String txt, String rutaImg, String sticker, String carpeta, boolean esHistoria) {
        Publicacion p = new Publicacion(autor, txt, rutaImg, carpeta, sticker, esHistoria, Publicacion.AspectRatio.CUADRADA);
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
            new File(uDir, "folders_personales/Memes").mkdirs();
            new File(uDir, "stickers_personales").mkdirs();

            guardarListaGenerica(new File(uDir, "following.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "followers.ins"), new Lista<String>());
            guardarListaGenerica(new File(uDir, "insta.ins"), new Lista<Publicacion>());
            guardarListaGenerica(new File(uDir, "inbox.ins"), new Lista<MensajeInbox>());

            Lista<Stickers> stks = new Lista<>();
            stks.agregar(new Stickers("Feliz", new File(RUTA_STICKERS_GLOBALES, "Feliz.png").getAbsolutePath(), true));
            stks.agregar(new Stickers("Triste", new File(RUTA_STICKERS_GLOBALES, "Triste.png").getAbsolutePath(), true));
            stks.agregar(new Stickers("Corazon", new File(RUTA_STICKERS_GLOBALES, "Corazon.png").getAbsolutePath(), true));
            stks.agregar(new Stickers("Risa", new File(RUTA_STICKERS_GLOBALES, "Risa.png").getAbsolutePath(), true));
            stks.agregar(new Stickers("Aplauso", new File(RUTA_STICKERS_GLOBALES, "Aplauso.png").getAbsolutePath(), true));
            guardarListaGenerica(new File(uDir, "stickers.ins"), stks);
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
            if (n.getDato().getUsername().equalsIgnoreCase(username)) return n.getDato();
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

    public static synchronized Usuario autenticarInsta(String username, String password) throws CuentaDesactivadaException {
        Lista<Usuario> usuarios = cargarUsuariosInsta();
        Nodo<Usuario> n = usuarios.getHead();
        while (n != null) {
            Usuario u = n.getDato();
            if (u.getUsername().equalsIgnoreCase(username) && u.getPass().equals(password)) {
                if (!u.isActivo()) {
                    throw new CuentaDesactivadaException("Tu cuenta se encuentra desactivada.");
                }
                return u;
            }
            n = n.getSiguiente();
        }
        return null;
    }

    public static synchronized boolean registrarUsuarioInsta(Usuario nuevo) {
        Lista<Usuario> usuarios = cargarUsuariosInsta();
        Nodo<Usuario> n = usuarios.getHead();
        while (n != null) {
            if (n.getDato().getUsername().equalsIgnoreCase(nuevo.getUsername())) return false;
            n = n.getSiguiente();
        }
        usuarios.agregar(nuevo);
        guardarUsuariosInsta(usuarios);
        crearEspacioUsuarioInsta(nuevo.getUsername());
        return true;
    }

    public static Lista<Publicacion> cargarPublicaciones(String username) {
        return cargarListaGenerica(new File(RUTA_INSTA + "/" + username + "/insta.ins"));
    }

    public static void guardarPublicaciones(String username, Lista<Publicacion> posts) {
        guardarListaGenerica(new File(RUTA_INSTA + "/" + username + "/insta.ins"), posts);
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

        boolean yaLoSigue = following.contiene(usuarioDestino.toLowerCase());
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
                if (m.getReceptor().equalsIgnoreCase(u1)) m.setLeido(true);
            }
            n = n.getSiguiente();
        }
        guardarListaGenerica(f, todos);
        return chat;
    }

    
    public static synchronized void marcarConversacionComoLeida(String usuarioActual, String usuarioEmisor) {
        File fInbox = new File(RUTA_INSTA + "/" + usuarioActual + "/inbox.ins");
        if (!fInbox.exists()) return;

        Lista<MensajeInbox> lista = cargarListaGenerica(fInbox);
        boolean huboCambios = false;

        Nodo<MensajeInbox> n = lista.getHead();
        while (n != null) {
            MensajeInbox m = n.getDato();
            if (m.getEmisor().equalsIgnoreCase(usuarioEmisor) && 
                m.getReceptor().equalsIgnoreCase(usuarioActual) && 
                !m.isLeido()) {
                m.setLeido(true);
                huboCambios = true;
            }
            n = n.getSiguiente();
        }
        if (huboCambios) {
            guardarListaGenerica(fInbox, lista);
        }
    }
    
    public static synchronized int contarMensajesNoLeidos(String usuarioActual, String remitente) {
        File f = new File(RUTA_INSTA + "/" + usuarioActual + "/inbox.ins");
        Lista<MensajeInbox> todos = cargarListaGenerica(f);
        Nodo<MensajeInbox> n = todos.getHead();
        int noLeidos = 0;
        while (n != null) {
            MensajeInbox m = n.getDato();
            if (m.getEmisor().equalsIgnoreCase(remitente) && m.getReceptor().equalsIgnoreCase(usuarioActual) && !m.isLeido()) {
                noLeidos++;
            }
            n = n.getSiguiente();
        }
        return noLeidos;
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
            if (!pertenece) filtrados.agregar(m);
            n = n.getSiguiente();
        }
        guardarListaGenerica(f, filtrados);
    }

    public static synchronized boolean agregarStickerPersonal(String username, File archivoOrigen) {
        if (archivoOrigen == null || !archivoOrigen.exists()) return false;

        String nombre = archivoOrigen.getName().toLowerCase();
        if (!nombre.endsWith(".png") && !nombre.endsWith(".jpg") && !nombre.endsWith(".jpeg")) {
            return false;
        }

        File dirPersonal = new File(RUTA_INSTA + "/" + username + "/stickers_personales");
        if (!dirPersonal.exists()) dirPersonal.mkdirs();

        File destino = new File(dirPersonal, archivoOrigen.getName());
        try {
            Files.copy(archivoOrigen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File fStk = new File(RUTA_INSTA + "/" + username + "/stickers.ins");
        Lista<Stickers> lista = cargarStickers(username);

        Stickers nuevoStk = new Stickers(archivoOrigen.getName(), destino.getAbsolutePath(), false);
        if (!lista.contiene(nuevoStk)) {
            lista.agregar(nuevoStk);
            guardarListaGenerica(fStk, lista);
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public static synchronized Lista<Stickers> cargarStickers(String username) {
        asegurarStickersPorDefecto();
        File fStk = new File(RUTA_INSTA + "/" + username + "/stickers.ins");
        Lista<Stickers> resultado = new Lista<>();

        String[] baseStickers = {"Feliz", "Triste", "Corazon", "Risa", "Aplauso"};
        for (String b : baseStickers) {
            File fImg = new File(RUTA_STICKERS_GLOBALES, b + ".png");
            resultado.agregar(new Stickers(b, fImg.exists() ? fImg.getAbsolutePath() : null, true));
        }

        if (fStk.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fStk))) {
                Object obj = ois.readObject();
                if (obj instanceof Lista) {
                    Lista enDisco = (Lista) obj;
                    for (int i = 0; i < enDisco.getSize(); i++) {
                        Object elemento = enDisco.obtener(i);
                        if (elemento instanceof Stickers) {
                            Stickers s = (Stickers) elemento;
                            if (!s.isEsGlobal() && s.getRutaArchivo() != null && new File(s.getRutaArchivo()).exists()) {
                                if (!resultado.contiene(s)) {
                                    resultado.agregar(s);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        File dirPers = new File(RUTA_INSTA + "/" + username + "/stickers_personales");
        if (dirPers.exists() && dirPers.isDirectory()) {
            File[] files = dirPers.listFiles((d, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
            });
            if (files != null) {
                for (File f : files) {
                    Stickers sp = new Stickers(f.getName(), f.getAbsolutePath(), false);
                    if (!resultado.contiene(sp)) {
                        resultado.agregar(sp);
                    }
                }
            }
        }

        guardarListaGenerica(fStk, resultado);
        return resultado;
    }
}