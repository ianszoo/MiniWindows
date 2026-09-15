/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ian Suazo Palao & David Suazo Palao
 */
public class InstaServer {
    public static final int PUERTO = 8888;
    private static final ConcurrentHashMap<String, PrintWriter> clientesConectados = new ConcurrentHashMap<>();
    private static ServerSocket serverSocket;
    private static volatile boolean activo = false;

    public static synchronized void iniciarServidor() {
        if (activo) return;

        Thread threadServidor = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(PUERTO));
                activo = true;
                System.out.println("[SERVIDOR INSTA+] Escuchando activamente en puerto " + PUERTO);

                while (activo && !serverSocket.isClosed()) {
                    try {
                        Socket socketCliente = serverSocket.accept();
                        new Thread(new ManejadorCliente(socketCliente)).start();
                    } catch (SocketException se) {
                        break;
                    }
                }
            } catch (BindException be) {
                System.out.println("[SERVIDOR INSTA+] El puerto " + PUERTO + " ya está en uso. Conectando a servidor existente.");
                activo = true;
            } catch (IOException e) {
                System.err.println("[SERVIDOR INSTA+] Error en el servidor: " + e.getMessage());
            }
        });
        threadServidor.setDaemon(true);
        threadServidor.start();
    }

    public static synchronized void detenerServidor() {
        activo = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}
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
            if (tokens.length == 0) return;
            String accion = tokens[0];

            switch (accion) {
                case "CONNECT":
                    if (tokens.length > 1) {
                        this.usuarioAutenticado = tokens[1].trim();
                        clientesConectados.put(usuarioAutenticado.toLowerCase(), out);
                        out.println("CONNECTED|OK");
                    }
                    break;

                case "CHAT_MSG":
                    if (tokens.length >= 5) {
                        String emisor = tokens[1];
                        String receptor = tokens[2];
                        String tipoStr = tokens[3];
                        String contenido = tokens[4];

                        MensajeInbox.Tipo tipo = tipoStr.equals("STICKER") ? MensajeInbox.Tipo.STICKER : MensajeInbox.Tipo.TEXTO;
                        InstaFileManager.enviarMensaje(emisor, receptor, contenido, tipo);

                        PrintWriter outReceptor = clientesConectados.get(receptor.toLowerCase());
                        if (outReceptor != null) {
                            outReceptor.println("NUEVO_MENSAJE|" + emisor + "|" + receptor + "|" + tipoStr + "|" + contenido);
                        }

                        out.println("MENSAJE_ENVIADO|" + emisor + "|" + receptor + "|" + tipoStr + "|" + contenido);
                    }
                    break;

                case "NOTIF_FOLLOW":
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