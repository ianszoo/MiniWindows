/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import Windows.Lista;
import Windows.Nodo;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author Ian Suazo Palao
 */
public class InstaServer {
    public static final int PUERTO = 8888;
    private static final ConcurrentHashMap<String, PrintWriter> clientesConectados = new ConcurrentHashMap<>();
    private static ServerSocket serverSocket;
    private static boolean activo = false;

    public static synchronized void iniciarServidor() {
        if (activo) return;
        activo = true;

        Thread threadServidor = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PUERTO);
                System.out.println("[SERVIDOR INSTA+] Escuchando en puerto " + PUERTO);

                while (activo) {
                    Socket socketCliente = serverSocket.accept();
                    new Thread(new ManejadorCliente(socketCliente)).start();
                }
            } catch (IOException e) {
                if (activo) e.printStackTrace();
            }
        });
        threadServidor.setDaemon(true);
        threadServidor.start();
    }

    private static class ManejadorCliente implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String usuarioAutenticado = null;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String linea;
                while ((linea = in.readLine()) != null) {
                    procesarComando(linea);
                }
            } catch (IOException ignored) {
            } finally {
                if (usuarioAutenticado != null) {
                    clientesConectados.remove(usuarioAutenticado.toLowerCase());
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void procesarComando(String comandoRaw) {
            String[] tokens = comandoRaw.split("\\|", -1);
            String accion = tokens[0];

            switch (accion) {
                case "CONNECT":
                    // CONNECT|<username>
                    if (tokens.length > 1) {
                        this.usuarioAutenticado = tokens[1].trim();
                        clientesConectados.put(usuarioAutenticado.toLowerCase(), out);
                        out.println("CONNECTED|OK");
                    }
                    break;

                case "CHAT_MSG":
                    // CHAT_MSG|<emisor>|<receptor>|<tipo>|<contenido>
                    if (tokens.length >= 5) {
                        String emisor = tokens[1];
                        String receptor = tokens[2];
                        String tipoStr = tokens[3];
                        String contenido = tokens[4];

                        MensajeInbox.Tipo tipo = tipoStr.equals("STICKER") ? MensajeInbox.Tipo.STICKER : MensajeInbox.Tipo.TEXTO;
                        InstaFileManager.enviarMensaje(emisor, receptor, contenido, tipo);

                        // Si el receptor está conectado, notificarle en vivo por socket
                        PrintWriter outReceptor = clientesConectados.get(receptor.toLowerCase());
                        if (outReceptor != null) {
                            outReceptor.println("NUEVO_MENSAJE|" + emisor + "|" + receptor + "|" + tipoStr + "|" + contenido);
                        }

                        // Eco al emisor para que actualice su pantalla inmediatamente
                        out.println("MENSAJE_ENVIADO|" + emisor + "|" + receptor + "|" + tipoStr + "|" + contenido);
                    }
                    break;

                case "NOTIF_FOLLOW":
                    // NOTIF_FOLLOW|<emisor>|<receptor>
                    if (tokens.length >= 3) {
                        String emisor = tokens[1];
                        String receptor = tokens[2];
                        PrintWriter outReceptor = clientesConectados.get(receptor.toLowerCase());
                        if (outReceptor != null) {
                            outReceptor.println("NUEVO_SEGUIDOR|" + emisor);
                        }
                    }
                    break;
            }
        }
    }
}
