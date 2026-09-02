package Windows;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Ian Suazo Palao
 */
public class Main {
    public static void main(String[] args) {
        // Inicializar persistencia de Z:\ y admin por defecto
        SistemadeArchivos.inicializarSistema();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Abrir la pantalla de acceso
        SwingUtilities.invokeLater(() -> {
            new WindowsLoginFrame().setVisible(true);
        });
    }
}