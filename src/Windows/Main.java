package Windows;

import Insta.InstaServer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Ian Suazo Palao
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        SistemadeArchivos.inicializarSistema();
        InstaServer.iniciarServidor();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new WindowsLoginFrame().setVisible(true);
        });
    }
}