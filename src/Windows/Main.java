package Windows;

import Insta.InstaServer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Ian Suazo Palao
 */
public class Main {
    public static void main(String[] args) {
        // 1. Inicializar persistencia binaria de Z:\
        SistemadeArchivos.inicializarSistema();

        // 2. Iniciar Servidor de Sockets para sincronización en tiempo real
        InstaServer.iniciarServidor();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 3. Lanzar interfaz gráfica
        SwingUtilities.invokeLater(() -> {
            new WindowsLoginFrame().setVisible(true);
        });
    }
}