/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;
import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;
/**
 *
 * @author Ian Suazo Palao
 */
public class InstaClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread hiloEscucha;
    private boolean conectado = false;
    private MensajeListener listener;

    public interface MensajeListener {
        void onMensajeRecibido(String emisor, String receptor, String contenido, boolean esSticker);
        void onNuevoSeguidor(String seguidor);
    }

    public void setListener(MensajeListener listener) {
        this.listener = listener;
    }

    public boolean conectar(String host, int puerto, String username) {
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            // Handshake inicial
            out.println("CONNECT|" +username);
            conectado = true;

            iniciarHiloEscucha();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void iniciarHiloEscucha() {
        hiloEscucha = new Thread(() -> {
            try {
                String linea;
                while (conectado && (linea = in.readLine()) != null) {
                    final String cmd = linea;
                    SwingUtilities.invokeLater(() -> procesarComandoServidor(cmd));
                }
            } catch (IOException ignored) {
            } finally {
                desconectar();
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    private void procesarComandoServidor(String linea) {
        String[] partes = linea.split("\\|", -1);
        if (partes.length == 0) return;

        String evento = partes[0];
        if (listener != null) {
            if (evento.equals("NUEVO_MENSAJE") || evento.equals("MENSAJE_ENVIADO")) {
                // EVENTO|<emisor>|<receptor>|<tipo>|<contenido>
                String emisor = partes[1];
                String receptor = partes[2];
                boolean esSticker = partes[3].equals("STICKER");
                String contenido = partes[4];
                listener.onMensajeRecibido(emisor, receptor, contenido, esSticker);
            } else if (evento.equals("NUEVO_SEGUIDOR")) {
                String seguidor = partes[1];
                listener.onNuevoSeguidor(seguidor);
            }
        }
    }

    public void enviarMensajeChat(String emisor, String receptor, String contenido, boolean esSticker) {
        if (out != null) {
            out.println("CHAT_MSG|"+emisor+"|"+receptor+"|"+(esSticker ? "STICKER" : "TEXTO")+"|"+contenido);
        }
    }

    public void notificarSeguimiento(String emisor, String receptor) {
        if (out != null) {
            out.println("NOTIF_FOLLOW|"+emisor+"|"+receptor);
        }
    }

    public void desconectar() {
        conectado = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
