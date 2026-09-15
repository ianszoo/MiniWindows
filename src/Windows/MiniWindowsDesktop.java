package Windows;

import Insta.InstaPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import javax.swing.tree.*;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

/**
 * @author David Suazo Palao & Ian Suazo Palao
 */
public class MiniWindowsDesktop extends JFrame {

    static {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    private JDesktopPane desktopPane;
    private Image backgroundImage;
    private JPanel startMenu;
    private boolean startMenuVisible = false;
    private Usuario usuarioActual;
    private final Map<String, JInternalFrame> ventanasAbiertas = new HashMap<>();

    // Portapapeles del Explorador de Archivos
    private File archivoPortapapeles = null;
    private boolean esOperacionCortar = false;

    // Paleta de Colores Windows Dark Modern
    private final Color TASKBAR_COLOR   = new Color(15, 23, 42, 245);
    private final Color START_MENU_BG   = new Color(30, 41, 59, 250);
    private final Color ACCENT_BLUE     = new Color(0, 120, 215);
    private final Color HOVER_COLOR     = new Color(255, 255, 255, 28);
    private final Color TEXT_WHITE      = new Color(241, 245, 249);
    private final Color TEXT_MUTED      = new Color(148, 163, 184);

    public MiniWindowsDesktop(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        this.usuarioActual.setActivo(true);
        actualizarTituloVentana();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        cargarFondo();

        desktopPane = new JDesktopPane() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(new GradientPaint(0, 0, new Color(15, 32, 67), getWidth(), getHeight(), new Color(2, 10, 25)));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        desktopPane.setLayout(null);

        desktopPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (startMenuVisible) {
                    toggleStartMenu();
                }
            }
        });

        add(desktopPane, BorderLayout.CENTER);

        refrescarEscritorioCompleto();
        crearBarraDeTareas();
    }

    private void actualizarTituloVentana() {
        setTitle("Mini-Windows OS - Sesión: " + usuarioActual.getUsername() + (usuarioActual.isEsAdmin() ? " (Administrador)" : " (Usuario Estándar)"));
    }

    public void cambiarUsuarioEnCaliente(Usuario nuevoUsuario) {
        motorAudio.detener();
        for (JInternalFrame f : desktopPane.getAllFrames()) {
            f.dispose();
        }
        ventanasAbiertas.clear();
        archivoPortapapeles = null;

        this.usuarioActual = nuevoUsuario;
        this.usuarioActual.setActivo(true);
        actualizarTituloVentana();

        if (startMenuVisible) {
            toggleStartMenu();
        }

        refrescarEscritorioCompleto();
        JOptionPane.showMessageDialog(this, "Sesión cambiada a @" + nuevoUsuario.getUsername() + "\nDirectorio activo: Z:\\" + nuevoUsuario.getUsername(), "Cambio de Cuenta", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refrescarEscritorioCompleto() {
        desktopPane.removeAll();
        crearIconosEscritorio();
        crearMenuInicio();
        desktopPane.revalidate();
        desktopPane.repaint();
    }

    public static ImageIcon cargarIcono(String nombreBase, int ancho, int alto) {
        String[] extensiones = {".png", ".jpg", ".jpeg"};
        String[] carpetas = {"/Imagenes/", "src/Imagenes/", "Imagenes/"};

        for (String cap : carpetas) {
            for (String ext : extensiones) {
                String ruta = cap + nombreBase + ext;
                if (cap.startsWith("/")) {
                    java.net.URL url = MiniWindowsDesktop.class.getResource(ruta);
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } else {
                    File f = new File(ruta);
                    if (f.exists()) {
                        Image img = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                }
            }
        }
        return null;
    }

    private void cargarFondo() {
        ImageIcon bg = cargarIcono("windows_background", 1920, 1080);
        if (bg != null) {
            backgroundImage = bg.getImage();
        }
    }

    private void gestionarVentana(String appId, String titulo, JComponent content, int ancho, int alto, boolean darkTheme) {
        JInternalFrame frame = ventanasAbiertas.get(appId);

        if (frame != null && (frame.isClosed() || frame.getParent() == null || !frame.isDisplayable())) {
            ventanasAbiertas.remove(appId);
            frame = null;
        }

        if (frame != null) {
            try {
                if (frame.isIcon()) {
                    frame.setIcon(false);
                    frame.toFront();
                    frame.setSelected(true);
                } else if (frame.isSelected()) {
                    frame.setIcon(true);
                } else {
                    frame.toFront();
                    frame.setSelected(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        JInternalFrame newFrame = new JInternalFrame(titulo, true, true, true, true);
        newFrame.setContentPane(content);
        newFrame.setSize(ancho, alto);
        newFrame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);

        newFrame.setBorder(BorderFactory.createLineBorder(darkTheme ? new Color(55, 55, 60) : new Color(210, 215, 225), 1));
        if (newFrame.getUI() instanceof BasicInternalFrameUI) {
            BasicInternalFrameUI ui = (BasicInternalFrameUI) newFrame.getUI();
            JComponent titlePane = (JComponent) ui.getNorthPane();
            if (titlePane != null) {
                titlePane.setBackground(darkTheme ? new Color(28, 28, 32) : new Color(245, 246, 248));
                titlePane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, darkTheme ? new Color(45, 45, 50) : new Color(225, 228, 235)));
            }
        }

        int posX = Math.max(25, (desktopPane.getWidth() - ancho) / 2 + (ventanasAbiertas.size() * 18));
        int posY = Math.max(25, (desktopPane.getHeight() - alto) / 2 + (ventanasAbiertas.size() * 18));
        newFrame.setLocation(posX, posY);

        newFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                ventanasAbiertas.remove(appId);
            }
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                ventanasAbiertas.remove(appId);
            }
        });

        desktopPane.add(newFrame);
        ventanasAbiertas.put(appId, newFrame);
        newFrame.setVisible(true);
        try {
            newFrame.setSelected(true);
        } catch (Exception ignored) {}
    }

    private void crearIconosEscritorio() {
        int x = 20;
        int y = 20;
        int gap = 95;

        desktopPane.add(crearIconoEscritorio("archivos_icono", "Explorador de\narchivos", x, y, () -> abrirExplorador(null)));
        desktopPane.add(crearIconoEscritorio("word_icon", "Editor de texto", x, y += gap, () -> abrirEditor(null)));
        desktopPane.add(crearIconoEscritorio("imagenes_icono", "Visor de\nimágenes", x, y += gap, () -> abrirVisor(null)));
        desktopPane.add(crearIconoEscritorio("musica_icono", "Reproductor de\nmúsica", x, y += gap, () -> abrirReproductor(null)));
        desktopPane.add(crearIconoEscritorio("cmd_icono", "Consola de\ncomandos", x, y += gap, () -> abrirCMD()));
        desktopPane.add(crearIconoEscritorio("instagram_icon", "INSTA+", x, y += gap, () -> abrirInsta()));

        if (usuarioActual.isEsAdmin()) {
            desktopPane.add(crearIconoEscritorio(null, "Cuentas de\nusuario", x, y += gap, () -> abrirCuentasUsuario()));
        }
    }

    private JPanel crearIconoEscritorio(String nombreIcono, String texto, int x, int y, Runnable accion) {
        final boolean[] isHovered = {false};

        JPanel p = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isHovered[0]) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        p.setOpaque(false);
        p.setBounds(x, y, 95, 85);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon;
        if (nombreIcono != null) {
            ImageIcon icono = cargarIcono(nombreIcono, 42, 42);
            lblIcon = new JLabel(icono != null ? icono : new ImageIcon(), SwingConstants.CENTER);
        } else {
            lblIcon = new JLabel("🛡", SwingConstants.CENTER);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            lblIcon.setForeground(Color.WHITE);
        }

        JLabel lbl = new JLabel("<html><center style='text-shadow: 1px 1px 3px #000;'>" + texto.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(lblIcon, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { accion.run(); }
            @Override
            public void mouseEntered(MouseEvent e) { isHovered[0] = true; p.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { isHovered[0] = false; p.repaint(); }
        });

        return p;
    }

    private void crearMenuInicio() {
        startMenu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(START_MENU_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        startMenu.setOpaque(false);
        startMenu.setLayout(new BorderLayout(10, 10));
        startMenu.setBorder(new EmptyBorder(15, 15, 15, 15));
        startMenu.setSize(400, 480);
        startMenu.setVisible(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);

        JPanel colLeft = new JPanel(new GridLayout(10, 1, 2, 2));
        colLeft.setOpaque(false);

        colLeft.add(crearBotonMenu(null, usuarioActual.getUsername() + (usuarioActual.isEsAdmin() ? " (Admin)" : ""), null, true));
        colLeft.add(crearBotonMenu("archivos_icono", "Explorador", () -> abrirExplorador(null), false));
        colLeft.add(crearBotonMenu("word_icon", "Editor Word", () -> abrirEditor(null), false));
        colLeft.add(crearBotonMenu("imagenes_icono", "Visor Fotos", () -> abrirVisor(null), false));
        colLeft.add(crearBotonMenu("cmd_icono", "Consola CMD", () -> abrirCMD(), false));
        colLeft.add(crearBotonMenu("musica_icono", "Reproductor", () -> abrirReproductor(null), false));
        colLeft.add(crearBotonMenu("instagram_icon", "INSTA+", () -> abrirInsta(), false));
        if (usuarioActual.isEsAdmin()) {
            colLeft.add(crearBotonMenu(null, "Cuentas de Usuario", () -> abrirCuentasUsuario(), false));
        }
        colLeft.add(crearBotonMenu(null, "Cambiar Cuenta", () -> solicitarCambioDeCuenta(), false));
        colLeft.add(crearBotonMenu(null, "Cerrar Sesión (Admin)", () -> cerrarSesion(), false));

        JPanel colRight = new JPanel(new GridLayout(8, 1, 2, 2));
        colRight.setOpaque(false);

        JLabel lblAccesos = new JLabel("Accesos Directos");
        lblAccesos.setForeground(TEXT_MUTED);
        lblAccesos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colRight.add(lblAccesos);

        colRight.add(crearBotonMenu("archivos_icono", "Documentos", () -> abrirExplorador(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos")), false));
        colRight.add(crearBotonMenu("imagenes_icono", "Imágenes", () -> abrirVisor(null), false));
        colRight.add(crearBotonMenu("musica_icono", "Música", () -> abrirExplorador(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música")), false));

        grid.add(colLeft);
        grid.add(colRight);
        startMenu.add(grid, BorderLayout.CENTER);

        desktopPane.add(startMenu, JLayeredPane.POPUP_LAYER);
    }

    private JButton crearBotonMenu(String nombreIcono, String texto, Runnable accion, boolean isHeader) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isHeader && getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(HOVER_COLOR); 
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        if (nombreIcono != null) {
            ImageIcon icon = cargarIcono(nombreIcono, 20, 20);
            if (icon != null) btn.setIcon(icon);
        }
        btn.setFont(new Font("Segoe UI", isHeader ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(isHeader ? ACCENT_BLUE : TEXT_WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));

        if (!isHeader) {
            btn.addActionListener(e -> {
                toggleStartMenu();
                if (accion != null) accion.run();
            });
        }
        return btn;
    }

    private void toggleStartMenu() {
        startMenuVisible = !startMenuVisible;
        if (startMenuVisible) {
            startMenu.setLocation(10, desktopPane.getHeight() - startMenu.getHeight() - 10);
            startMenu.setVisible(true);
            startMenu.requestFocus();
        } else {
            startMenu.setVisible(false);
        }
    }

    private void crearBarraDeTareas() {
        JPanel taskBar = new JPanel(new BorderLayout(10, 0));
        taskBar.setBackground(TASKBAR_COLOR);
        taskBar.setPreferredSize(new Dimension(getWidth(), 52));
        taskBar.setBorder(new EmptyBorder(4, 10, 4, 15));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.setOpaque(false);

        JButton btnStart = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.setColor(new Color(0, 164, 239));
                int pad = 12;
                int size = 7;
                g2.fillRect(pad, pad, size, size);
                g2.fillRect(pad + size + 2, pad, size, size);
                g2.fillRect(pad, pad + size + 2, size, size);
                g2.fillRect(pad + size + 2, pad + size + 2, size, size);
                g2.dispose();
            }
        };
        btnStart.setPreferredSize(new Dimension(42, 38));
        btnStart.setContentAreaFilled(false);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(e -> toggleStartMenu());
        left.add(btnStart);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        center.setOpaque(false);

        center.add(crearBotonBarra("archivos_icono", () -> abrirExplorador(null), "Explorador de Archivos"));
        center.add(crearBotonBarra("word_icon", () -> abrirEditor(null), "Editor de Texto"));
        center.add(crearBotonBarra("imagenes_icono", () -> abrirVisor(null), "Visor de Fotos"));
        center.add(crearBotonBarra("musica_icono", () -> abrirReproductor(null), "Reproductor de Música"));
        center.add(crearBotonBarra("cmd_icono", () -> abrirCMD(), "Consola CMD"));
        center.add(crearBotonBarra("instagram_icon", () -> abrirInsta(), "INSTA+"));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        right.setOpaque(false);

        JLabel lblTimeDate = new JLabel();
        lblTimeDate.setForeground(Color.WHITE);
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        Timer clockTimer = new Timer(1000, e -> {
            String time = new SimpleDateFormat("hh:mm a").format(new Date());
            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            lblTimeDate.setText("<html><center style='line-height:90%'>" + time + "<br><span style='font-size:9px; color:#94a3b8;'>" + date + "</span></center></html>");
        });
        clockTimer.start();

        right.add(lblTimeDate);

        taskBar.add(left, BorderLayout.WEST);
        taskBar.add(center, BorderLayout.CENTER);
        taskBar.add(right, BorderLayout.EAST);

        add(taskBar, BorderLayout.SOUTH);
    }

    private JButton crearBotonBarra(String nombreIcono, Runnable accion, String tooltip) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(HOVER_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        ImageIcon icon = cargarIcono(nombreIcono, 26, 26);
        if (icon != null) btn.setIcon(icon);

        btn.setPreferredSize(new Dimension(44, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private void cerrarSesion() {
        motorAudio.detener();
        dispose();
        SwingUtilities.invokeLater(() -> new WindowsLoginFrame().setVisible(true));
    }

    private void solicitarCambioDeCuenta() {
        try {
            Lista<Usuario> lista = SistemadeArchivos.cargarUsuarios();
            String[] usuarios = new String[lista.getSize()];
            for (int i = 0; i < lista.getSize(); i++) {
                usuarios[i] = lista.obtener(i).getUsername();
            }

            String sel = (String) JOptionPane.showInputDialog(this, "Selecciona la cuenta a la que deseas ingresar:", "Cambiar de Cuenta", JOptionPane.QUESTION_MESSAGE, null, usuarios, usuarios[0]);
            if (sel != null) {
                pedirPasswordEIngresar(sel);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener usuarios: " + ex.getMessage());
        }
    }

    private void pedirPasswordEIngresar(String username) {
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(this, pf, "Ingresa la contraseña para @" + username + ":", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            String pss = new String(pf.getPassword());
            try {
                Usuario u = SistemadeArchivos.autenticar(username, pss);
                if (u != null) {
                    if (!u.isActivo() && !u.getUsername().equalsIgnoreCase("admin")) {
                        JOptionPane.showMessageDialog(this, "La cuenta @" + username + " se encuentra desactivada.", "Cuenta Inactiva", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    cambiarUsuarioEnCaliente(u);
                } else {
                    JOptionPane.showMessageDialog(this, "Contraseña incorrecta para @" + username, "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al autenticar: " + ex.getMessage());
            }
        }
    }

    private void abrirExplorador(File carpetaInicial) { 
        gestionarVentana("EXPLORADOR", "Explorador de archivos (" + usuarioActual.getUsername() + ")", crearExploradorReal(carpetaInicial), 940, 610, false); 
    }
    private void abrirEditor(File archivoParaAbrir) { 
        gestionarVentana("EDITOR", "Bloc de Notas - Editor con Formato", crearEditorReal(archivoParaAbrir), 860, 560, true); 
    }
    private void abrirVisor(File fotoInicial) { 
        gestionarVentana("VISOR", "Visor de Imágenes", crearVisorReal(fotoInicial), 880, 600, true); 
    }
    private void abrirCMD() { 
        gestionarVentana("CMD", "Símbolo del Sistema (CMD) - " + usuarioActual.getUsername(), crearCmdReal(), 720, 440, true); 
    }
    private void abrirReproductor(File cancionParaTocar) { 
        gestionarVentana("REPRODUCTOR", "Media Player", crearReproductorReal(cancionParaTocar), 960, 600, true); 
    }
    
    private void abrirInsta() { 
        String instanciaId = "INSTA_" + System.currentTimeMillis();
        gestionarVentana(instanciaId, "INSTA+ - Red Social Móvil (" + usuarioActual.getUsername() + ")", new InstaPanel(usuarioActual), 460, 750, true); 
    }

    private void abrirCuentasUsuario() {
        if (!usuarioActual.isEsAdmin()) {
            JOptionPane.showMessageDialog(this, "Acceso denegado: Solo el Administrador puede gestionar las cuentas.", "Seguridad", JOptionPane.WARNING_MESSAGE);
            return;
        }
        gestionarVentana("CUENTAS_USUARIOS", "Cuentas de usuario", crearPanelCuentasUsuario(), 880, 480, false);
    }

    private JButton crearBotonPersonalizado(String texto, Color bgBase, Color bgHover) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgBase.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgHover);
                } else {
                    g2.setColor(bgBase);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private static String truncarTexto(String texto, int maxLen) {
        if (texto == null) return "";
        if (texto.length() <= maxLen) return texto;
        return texto.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    private static File resolverArchivoCaratula(File dirMusica, String caratulaRutaONombre) {
        if (caratulaRutaONombre == null || caratulaRutaONombre.trim().isEmpty()) return null;
        File fRelativo = new File(dirMusica, new File(caratulaRutaONombre).getName());
        if (fRelativo.exists()) return fRelativo;
        File fDirecto = new File(caratulaRutaONombre);
        if (fDirecto.exists()) return fDirecto;
        return null;
    }

    // PANEL "CUENTAS DE USUARIO"
    private JPanel crearPanelCuentasUsuario() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(new Color(248, 249, 251));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 235)));

        JButton btnIngresar = new JButton("▶ Ingresar a cuenta");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnIngresar.setBackground(new Color(219, 234, 254));
        btnIngresar.setForeground(ACCENT_BLUE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toolbar.add(btnIngresar);

        JButton btnNuevo = new JButton("Nuevo usuario");
        JButton btnRol = new JButton("Cambiar rol");
        JButton btnEstado = new JButton("Activar / Desactivar");
        JButton btnPass = new JButton("Restablecer contraseña");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        JButton[] btns = {btnNuevo, btnRol, btnEstado, btnPass, btnEliminar, btnRefrescar};
        for (JButton b : btns) {
            b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            b.setBackground(Color.WHITE);
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            toolbar.add(b);
        }
        p.add(toolbar, BorderLayout.NORTH);

        String[] columnas = {"Usuario", "Nombre completo", "Rol", "Edad", "Género", "Registro", "Estado"};
        DefaultTableModel modelUsuarios = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tableUsuarios = new JTable(modelUsuarios);
        tableUsuarios.setRowHeight(26);
        tableUsuarios.setShowGrid(false);
        tableUsuarios.setSelectionBackground(new Color(219, 234, 254));
        tableUsuarios.setSelectionForeground(Color.BLACK);
        tableUsuarios.getTableHeader().setBackground(new Color(245, 246, 248));
        tableUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollUsuarios = new JScrollPane(tableUsuarios);
        scrollUsuarios.setBorder(BorderFactory.createLineBorder(new Color(230, 232, 238), 1));
        p.add(scrollUsuarios, BorderLayout.CENTER);

        JLabel lblStatus = new JLabel("0 cuenta(s) registradas");
        lblStatus.setForeground(new Color(120, 125, 135));
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        p.add(lblStatus, BorderLayout.SOUTH);

        Runnable recargarTabla = () -> {
            modelUsuarios.setRowCount(0);
            try {
                Lista<Usuario> list = SistemadeArchivos.cargarUsuarios();
                Nodo<Usuario> cur = list.getHead();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                int count = 0;
                while (cur != null) {
                    Usuario u = cur.getDato();
                    boolean activoVal = u.getUsername().equalsIgnoreCase("admin") ? true : u.isActivo();
                    modelUsuarios.addRow(new Object[]{
                        u.getUsername(),
                        u.getNombreCompleto(),
                        u.isEsAdmin() ? "Administrador" : "Estándar",
                        u.getEdad(),
                        String.valueOf(u.getGenero()),
                        sdf.format(u.getFechaCreacion()),
                        activoVal ? "Activa" : "Inactiva"
                    });
                    count++;
                    cur = cur.getSiguiente();
                }
                lblStatus.setText(count + " cuenta(s) registradas");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        recargarTabla.run();

        btnIngresar.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla para ingresar a su cuenta.");
                return;
            }
            String usr = (String) modelUsuarios.getValueAt(row, 0);
            pedirPasswordEIngresar(usr);
        });

        tableUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableUsuarios.getSelectedRow();
                    if (row != -1) {
                        String usr = (String) modelUsuarios.getValueAt(row, 0);
                        pedirPasswordEIngresar(usr);
                    }
                }
            }
        });

        btnNuevo.addActionListener(e -> {
            JDialog dlg = new JDialog(this, "Nuevo usuario", true);
            dlg.setSize(400, 410);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
            form.setBorder(new EmptyBorder(15, 20, 15, 20));

            JTextField txtNom = new JTextField();
            JTextField txtUsr = new JTextField();
            JPasswordField txtPwd = new JPasswordField();
            JPasswordField txtPwdConfirm = new JPasswordField();
            JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
            JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
            JComboBox<String> cbRol = new JComboBox<>(new String[]{"Estándar", "Administrador"});

            form.add(new JLabel("Nombre completo:")); form.add(txtNom);
            form.add(new JLabel("Username:")); form.add(txtUsr);
            form.add(new JLabel("Contraseña:")); form.add(txtPwd);
            form.add(new JLabel("Confirmar contraseña:")); form.add(txtPwdConfirm);
            form.add(new JLabel("Edad:")); form.add(spinEdad);
            form.add(new JLabel("Género:")); form.add(cbGen);
            form.add(new JLabel("Rol:")); form.add(cbRol);

            JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton btnOk = new JButton("OK");
            JButton btnCancel = new JButton("Cancel");
            botPanel.add(btnOk);
            botPanel.add(btnCancel);

            btnCancel.addActionListener(ev -> dlg.dispose());
            btnOk.addActionListener(ev -> {
                String u = txtUsr.getText().trim();
                String nom = txtNom.getText().trim();
                String pss = new String(txtPwd.getPassword());
                String pssConf = new String(txtPwdConfirm.getPassword());
                boolean adm = cbRol.getSelectedItem().equals("Administrador");

                if (u.isEmpty() || nom.isEmpty() || pss.isEmpty() || pssConf.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Debe completar todos los campos.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!pss.equals(pssConf)) {
                    JOptionPane.showMessageDialog(dlg, "Las contraseñas no coinciden.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    SistemadeArchivos.registrarUsuario(u, pss, adm);
                    Lista<Usuario> users = SistemadeArchivos.cargarUsuarios();
                    Nodo<Usuario> node = users.getHead();
                    while (node != null) {
                        if (node.getDato().getUsername().equalsIgnoreCase(u)) {
                            node.getDato().setNombreCompleto(nom);
                            node.getDato().setEdad((Integer) spinEdad.getValue());
                            node.getDato().setGenero(cbGen.getSelectedItem().toString().charAt(0));
                            node.getDato().setActivo(true);
                            break;
                        }
                        node = node.getSiguiente();
                    }
                    SistemadeArchivos.guardarUsuarios(users);

                    dlg.dispose();
                    recargarTabla.run();
                    JOptionPane.showMessageDialog(this, "Usuario '" + u + "' creado exitosamente. Puedes seleccionarlo y dar clic en 'Ingresar a cuenta'.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            dlg.add(form, BorderLayout.CENTER);
            dlg.add(botPanel, BorderLayout.SOUTH);
            dlg.setVisible(true);
        });

        btnRol.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row != -1) {
                String usr = (String) modelUsuarios.getValueAt(row, 0);
                if (usr.equalsIgnoreCase("admin")) {
                    JOptionPane.showMessageDialog(this, "No se puede modificar el rol del Administrador principal.");
                    return;
                }
                try {
                    Lista<Usuario> users = SistemadeArchivos.cargarUsuarios();
                    Nodo<Usuario> n = users.getHead();
                    while (n != null) {
                        if (n.getDato().getUsername().equalsIgnoreCase(usr)) {
                            n.getDato().setEsAdmin(!n.getDato().isEsAdmin());
                            break;
                        }
                        n = n.getSiguiente();
                    }
                    SistemadeArchivos.guardarUsuarios(users);
                    recargarTabla.run();
                } catch (Exception ignored) {}
            }
        });

        btnEstado.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row != -1) {
                String usr = (String) modelUsuarios.getValueAt(row, 0);
                if (usr.equalsIgnoreCase("admin")) {
                    JOptionPane.showMessageDialog(this, "La cuenta principal de Administrador siempre permanece activa.");
                    return;
                }
                try {
                    Lista<Usuario> users = SistemadeArchivos.cargarUsuarios();
                    Nodo<Usuario> n = users.getHead();
                    while (n != null) {
                        if (n.getDato().getUsername().equalsIgnoreCase(usr)) {
                            n.getDato().setActivo(!n.getDato().isActivo());
                            break;
                        }
                        n = n.getSiguiente();
                    }
                    SistemadeArchivos.guardarUsuarios(users);
                    recargarTabla.run();
                } catch (Exception ignored) {}
            }
        });

        btnPass.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row != -1) {
                String usr = (String) modelUsuarios.getValueAt(row, 0);
                
                JPanel passPanel = new JPanel(new GridLayout(2, 2, 6, 6));
                JPasswordField pf1 = new JPasswordField();
                JPasswordField pf2 = new JPasswordField();
                passPanel.add(new JLabel("Nueva contraseña:"));
                passPanel.add(pf1);
                passPanel.add(new JLabel("Confirmar contraseña:"));
                passPanel.add(pf2);

                int resp = JOptionPane.showConfirmDialog(this, passPanel, "Restablecer contraseña para @" + usr, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    String p1 = new String(pf1.getPassword());
                    String p2 = new String(pf2.getPassword());

                    if (p1.isEmpty() || p2.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Debe ingresar ambas contraseñas.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (!p1.equals(p2)) {
                        JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        Password.validar(p1);
                        Lista<Usuario> users = SistemadeArchivos.cargarUsuarios();
                        Nodo<Usuario> n = users.getHead();
                        while (n != null) {
                            if (n.getDato().getUsername().equalsIgnoreCase(usr)) {
                                n.getDato().setPass(p1);
                                break;
                            }
                            n = n.getSiguiente();
                        }
                        SistemadeArchivos.guardarUsuarios(users);
                        JOptionPane.showMessageDialog(this, "Contraseña actualizada exitosamente.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Contraseña", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            int row = tableUsuarios.getSelectedRow();
            if (row != -1) {
                String usr = (String) modelUsuarios.getValueAt(row, 0);
                if (usr.equalsIgnoreCase("admin")) {
                    JOptionPane.showMessageDialog(this, "No se puede eliminar la cuenta principal de Administrador.");
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this, "¿Desea eliminar al usuario '" + usr + "' y su espacio de archivos?", "Eliminar", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        Lista<Usuario> users = SistemadeArchivos.cargarUsuarios();
                        users.eliminar(new Usuario(usr, "", false));
                        SistemadeArchivos.guardarUsuarios(users);
                        
                        File uDir = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usr);
                        if (uDir.exists()) {
                            eliminarDirectorioRecursivo(uDir);
                        }
                        recargarTabla.run();
                    } catch (Exception ignored) {}
                }
            }
        });

        btnRefrescar.addActionListener(e -> recargarTabla.run());

        return p;
    }

    // EXPLORADOR DE ARCHIVOS
    private JPanel crearExploradorReal(File carpetaInicial) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(Color.WHITE);

        File raizPermitida = usuarioActual.isEsAdmin() 
                ? new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA) 
                : new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername());

        final File[] carpetaActual = {(carpetaInicial != null && carpetaInicial.exists()) ? carpetaInicial : raizPermitida};
        final String[] criterioOrden = {"Nombre"};

        Stack<File> historialAtras = new Stack<>();
        Stack<File> historialAdelante = new Stack<>();

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(248, 249, 251));
        topContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 235)));

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        navBar.setOpaque(false);

        JButton btnAtras = new JButton("<");
        JButton btnAdelante = new JButton(">");
        JButton btnSubir = new JButton("↑");
        JButton btnRefrescar = new JButton("↻");
        JTextField txtBuscar = new JTextField(" Buscar en esta carpeta");
        txtBuscar.setPreferredSize(new Dimension(180, 26));
        txtBuscar.setForeground(new Color(150, 150, 150));

        navBar.add(btnAtras);
        navBar.add(btnAdelante);
        navBar.add(btnSubir);
        navBar.add(btnRefrescar);
        navBar.add(txtBuscar);

        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        actionsBar.setOpaque(false);

        JButton btnNuevaCarpeta = new JButton("Nueva carpeta");
        JButton btnNuevoDoc = new JButton("Nuevo documento");
        JButton btnOrganizar = new JButton("⚡ Organizar");
        JButton btnImportar = new JButton("Importar");
        JButton btnMoverA = new JButton("Mover a...");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnCopiar = new JButton("Copiar");
        JButton btnCortar = new JButton("Cortar");
        JButton btnPegar = new JButton("Pegar");
        JButton btnEliminar = new JButton("Eliminar");

        JComboBox<String> cbOrdenar = new JComboBox<>(new String[]{"Nombre", "Tipo", "Modificado", "Tamaño"});

        actionsBar.add(btnNuevaCarpeta);
        actionsBar.add(btnNuevoDoc);
        actionsBar.add(btnOrganizar);
        actionsBar.add(btnImportar);
        actionsBar.add(btnMoverA);
        actionsBar.add(btnRenombrar);
        actionsBar.add(btnCopiar);
        actionsBar.add(btnCortar);
        actionsBar.add(btnPegar);
        actionsBar.add(btnEliminar);
        actionsBar.add(new JLabel(" Ordenar por "));
        actionsBar.add(cbOrdenar);

        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(actionsBar, BorderLayout.SOUTH);
        p.add(topContainer, BorderLayout.NORTH);

        DefaultMutableTreeNode raizNodo = new DefaultMutableTreeNode(raizPermitida.getName());
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raizNodo);
        JTree tree = new JTree(modeloArbol);
        tree.setBackground(new Color(250, 251, 253));

        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                String name = String.valueOf(value).toLowerCase();
                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    lbl.setIcon(cargarIcono("imagenes_icono", 16, 16));
                } else if (name.endsWith(".mp3") || name.endsWith(".wav")) {
                    lbl.setIcon(cargarIcono("musica_icono", 16, 16));
                } else if (name.endsWith(".sop") || name.endsWith(".txt")) {
                    lbl.setIcon(cargarIcono("word_icon", 16, 16));
                } else {
                    lbl.setIcon(cargarIcono("archivos_icono", 16, 16));
                }
                return lbl;
            }
        });

        JScrollPane scrollTree = new JScrollPane(tree);
        scrollTree.setPreferredSize(new Dimension(210, 0));
        scrollTree.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 230, 235)));

        String[] columnas = {"Nombre", "Tipo", "Modificado", "Tamaño"};
        DefaultTableModel modelTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tableArchivos = new JTable(modelTabla);
        tableArchivos.setRowHeight(26);
        tableArchivos.setShowGrid(false);
        tableArchivos.setSelectionBackground(new Color(219, 234, 254));
        tableArchivos.setSelectionForeground(Color.BLACK);
        tableArchivos.getTableHeader().setBackground(new Color(240, 242, 245));
        tableArchivos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableArchivos.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(new EmptyBorder(0, 6, 0, 6));
                String name = String.valueOf(value).toLowerCase();

                if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    lbl.setIcon(cargarIcono("imagenes_icono", 16, 16));
                } else if (name.endsWith(".mp3") || name.endsWith(".wav")) {
                    lbl.setIcon(cargarIcono("musica_icono", 16, 16));
                } else if (name.endsWith(".sop") || name.endsWith(".txt")) {
                    lbl.setIcon(cargarIcono("word_icon", 16, 16));
                } else {
                    lbl.setIcon(cargarIcono("archivos_icono", 16, 16));
                }
                return lbl;
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tableArchivos);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(null);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        JLabel lblRutaActual = new JLabel("  📁 Z:\\");
        lblRutaActual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRutaActual.setBorder(new EmptyBorder(6, 8, 6, 8));
        centerPanel.add(lblRutaActual, BorderLayout.NORTH);
        centerPanel.add(scrollTabla, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, centerPanel);
        split.setDividerLocation(210);
        split.setBorder(null);
        p.add(split, BorderLayout.CENTER);

        JLabel lblStatus = new JLabel(" 0 elemento(s)");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(130, 135, 145));
        lblStatus.setBorder(new EmptyBorder(4, 10, 4, 10));
        p.add(lblStatus, BorderLayout.SOUTH);

        Runnable cargarContenido = () -> {
            raizNodo.removeAllChildren();
            poblarNodos(raizPermitida, raizNodo);
            modeloArbol.reload();

            modelTabla.setRowCount(0);
            String rutaDisplay = carpetaActual[0].getAbsolutePath().replace(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA).getAbsolutePath(), "Z:");
            lblRutaActual.setText("  📁 " + rutaDisplay);

            String filtro = txtBuscar.getText().trim().toLowerCase();
            if (filtro.equals("buscar en esta carpeta")) filtro = "";

            File[] files = carpetaActual[0].listFiles();
            if (files != null) {
                switch (criterioOrden[0]) {
                    case "Tipo":
                        Arrays.sort(files, (a, b) -> {
                            if (a.isDirectory() && !b.isDirectory()) return -1;
                            if (!a.isDirectory() && b.isDirectory()) return 1;
                            return a.getName().compareToIgnoreCase(b.getName());
                        });
                        break;
                    case "Modificado":
                        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                        break;
                    case "Tamaño":
                        Arrays.sort(files, (a, b) -> Long.compare(b.length(), a.length()));
                        break;
                    case "Nombre":
                    default:
                        Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase()));
                        break;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                int count = 0;
                for (File f : files) {
                    if (filtro.isEmpty() || f.getName().toLowerCase().contains(filtro)) {
                        String tipo = f.isDirectory() ? "Carpeta" : (f.getName().endsWith(".txt") || f.getName().endsWith(".sop") ? "Documento de texto" : "Archivo " + obtenerExtension(f.getName()));
                        String tam = f.isDirectory() ? "" : (f.length() + " B");
                        modelTabla.addRow(new Object[]{f.getName(), tipo, sdf.format(new Date(f.lastModified())), tam});
                        count++;
                    }
                }
                lblStatus.setText(" " + count + " elemento(s) en " + rutaDisplay);
            }
        };
        cargarContenido.run();

        btnOrganizar.addActionListener(e -> {
            File carpetaAOrganizar = carpetaActual[0];
            btnOrganizar.setEnabled(false);
            btnOrganizar.setText("Organizando...");

            Thread hiloOrganizador = new Thread(() -> {
                organizarArchivos(carpetaAOrganizar);
                SwingUtilities.invokeLater(() -> {
                    btnOrganizar.setEnabled(true);
                    btnOrganizar.setText("⚡ Organizar");
                    cargarContenido.run();
                    JOptionPane.showMessageDialog(this, "¡Archivos clasificados por tipo exitosamente!", "Organizador", JOptionPane.INFORMATION_MESSAGE);
                });
            });
            hiloOrganizador.setDaemon(true);
            hiloOrganizador.start();
        });

        tableArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableArchivos.getSelectedRow();
                    if (row != -1) {
                        String nombre = (String) modelTabla.getValueAt(row, 0);
                        File destino = new File(carpetaActual[0], nombre);
                        if (destino.isDirectory()) {
                            historialAtras.push(carpetaActual[0]);
                            carpetaActual[0] = destino;
                            cargarContenido.run();
                        } else {
                            abrirArchivoSegunExtension(destino);
                        }
                    }
                }
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    Object[] nodes = path.getPath();
                    File res = raizPermitida;
                    for (int i = 1; i < nodes.length; i++) {
                        res = new File(res, nodes[i].toString());
                    }

                    if (res.exists()) {
                        if (res.isDirectory()) {
                            carpetaActual[0] = res;
                            modelTabla.setRowCount(0);
                            String rutaDisplay = carpetaActual[0].getAbsolutePath().replace(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA).getAbsolutePath(), "Z:");
                            lblRutaActual.setText("  📁 " + rutaDisplay);
                            File[] files = carpetaActual[0].listFiles();
                            if (files != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                                for (File f : files) {
                                    String tipo = f.isDirectory() ? "Carpeta" : "Archivo " + obtenerExtension(f.getName());
                                    modelTabla.addRow(new Object[]{f.getName(), tipo, sdf.format(new Date(f.lastModified())), f.isDirectory() ? "" : (f.length() + " B")});
                                }
                            }
                        } else if (e.getClickCount() == 2) {
                            abrirArchivoSegunExtension(res);
                        }
                    }
                }
            }
        });

        btnAtras.addActionListener(e -> {
            if (!historialAtras.isEmpty()) {
                historialAdelante.push(carpetaActual[0]);
                carpetaActual[0] = historialAtras.pop();
                cargarContenido.run();
            }
        });

        btnAdelante.addActionListener(e -> {
            if (!historialAdelante.isEmpty()) {
                historialAtras.push(carpetaActual[0]);
                carpetaActual[0] = historialAdelante.pop();
                cargarContenido.run();
            }
        });

        btnSubir.addActionListener(e -> {
            File padre = carpetaActual[0].getParentFile();
            if (padre != null && padre.getAbsolutePath().startsWith(raizPermitida.getAbsolutePath())) {
                historialAtras.push(carpetaActual[0]);
                carpetaActual[0] = padre;
                cargarContenido.run();
            }
        });

        btnRefrescar.addActionListener(e -> cargarContenido.run());

        txtBuscar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtBuscar.getText().equals(" Buscar en esta carpeta")) {
                    txtBuscar.setText("");
                    txtBuscar.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtBuscar.getText().trim().isEmpty()) {
                    txtBuscar.setText(" Buscar en esta carpeta");
                    txtBuscar.setForeground(new Color(150, 150, 150));
                    cargarContenido.run();
                }
            }
        });

        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cargarContenido.run();
            }
        });

        btnNuevaCarpeta.addActionListener(e -> {
            String nom = JOptionPane.showInputDialog(this, "Nombre de la carpeta:", "Nueva carpeta", JOptionPane.PLAIN_MESSAGE);
            if (nom != null && !nom.trim().isEmpty()) {
                new File(carpetaActual[0], nom.trim()).mkdirs();
                cargarContenido.run();
            }
        });

        btnNuevoDoc.addActionListener(e -> {
            String nom = JOptionPane.showInputDialog(this, "Nombre del documento:", "nuevo_documento.sop");
            if (nom != null && !nom.trim().isEmpty()) {
                if (!nom.endsWith(".sop") && !nom.endsWith(".txt")) {
                    nom += ".sop";
                }
                File nuevo = new File(carpetaActual[0], nom.trim());
                try {
                    if (nuevo.getName().endsWith(".sop")) {
                        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nuevo))) {
                            DefaultStyledDocument doc = new DefaultStyledDocument();
                            doc.insertString(0, "Nuevo documento con formato persistente.", null);
                            oos.writeObject(doc);
                        }
                    } else {
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nuevo))) {
                            bw.write("Nuevo documento de texto.");
                        }
                    }
                    cargarContenido.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al crear archivo: " + ex.getMessage());
                }
            }
        });

        btnImportar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fSel = fc.getSelectedFile();
                try {
                    Files.copy(fSel.toPath(), new File(carpetaActual[0], fSel.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    cargarContenido.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al importar: " + ex.getMessage());
                }
            }
        });

        btnMoverA.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo o carpeta en la tabla para mover.");
                return;
            }
            String n = (String) modelTabla.getValueAt(row, 0);
            File origen = new File(carpetaActual[0], n);

            File[] subcarpetas = carpetaActual[0].listFiles(File::isDirectory);
            if (subcarpetas == null || subcarpetas.length == 0) {
                JOptionPane.showMessageDialog(this, "No hay subcarpetas dentro de esta ubicación para mover el archivo.");
                return;
            }

            String[] opciones = Arrays.stream(subcarpetas).map(File::getName).toArray(String[]::new);
            String seleccion = (String) JOptionPane.showInputDialog(this, "Seleccione la carpeta de destino:", "Mover archivo", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

            if (seleccion != null) {
                File destinoCarpeta = new File(carpetaActual[0], seleccion);
                try {
                    Files.move(origen.toPath(), new File(destinoCarpeta, origen.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    cargarContenido.run();
                    JOptionPane.showMessageDialog(this, "¡Elemento movido a " + seleccion + "!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al mover: " + ex.getMessage());
                }
            }
        });

        btnCopiar.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String n = (String) modelTabla.getValueAt(row, 0);
                archivoPortapapeles = new File(carpetaActual[0], n);
                esOperacionCortar = false;
                JOptionPane.showMessageDialog(this, "Copiado al portapapeles: " + n);
            }
        });

        btnCortar.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String n = (String) modelTabla.getValueAt(row, 0);
                archivoPortapapeles = new File(carpetaActual[0], n);
                esOperacionCortar = true;
                JOptionPane.showMessageDialog(this, "Cortado al portapapeles: " + n);
            }
        });

        btnPegar.addActionListener(e -> {
            if (archivoPortapapeles == null || !archivoPortapapeles.exists()) {
                JOptionPane.showMessageDialog(this, "El portapapeles está vacío.");
                return;
            }

            File carpetaDestinoFinal = carpetaActual[0];
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String nomSel = (String) modelTabla.getValueAt(row, 0);
                File fSel = new File(carpetaActual[0], nomSel);
                if (fSel.isDirectory()) {
                    carpetaDestinoFinal = fSel;
                }
            }

            File dest = new File(carpetaDestinoFinal, archivoPortapapeles.getName());
            try {
                if (esOperacionCortar) {
                    Files.move(archivoPortapapeles.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    archivoPortapapeles = null;
                } else {
                    Files.copy(archivoPortapapeles.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                cargarContenido.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al pegar: " + ex.getMessage());
            }
        });

        btnRenombrar.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String n = (String) modelTabla.getValueAt(row, 0);
                File actual = new File(carpetaActual[0], n);
                String nuevo = JOptionPane.showInputDialog(this, "Nuevo nombre:", n);
                if (nuevo != null && !nuevo.trim().isEmpty()) {
                    actual.renameTo(new File(carpetaActual[0], nuevo.trim()));
                    cargarContenido.run();
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String nombre = (String) modelTabla.getValueAt(row, 0);
                File aEliminar = new File(carpetaActual[0], nombre);
                int resp = JOptionPane.showConfirmDialog(this, "¿Desea eliminar '" + nombre + "'?", "Eliminar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    eliminarDirectorioRecursivo(aEliminar);
                    cargarContenido.run();
                }
            }
        });

        cbOrdenar.addActionListener(e -> {
            criterioOrden[0] = (String) cbOrdenar.getSelectedItem();
            cargarContenido.run();
        });

        return p;
    }

    private void abrirArchivoSegunExtension(File f) {
        String n = f.getName().toLowerCase();
        if (f.getName().equalsIgnoreCase("usuarios.sop")) {
            if (usuarioActual.isEsAdmin()) {
                abrirCuentasUsuario();
            } else {
                abrirEditor(f);
            }
        } else if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
            abrirVisor(f);
        } else if (n.endsWith(".sop") || n.endsWith(".txt")) {
            abrirEditor(f);
        } else if (n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".m4a")) {
            abrirReproductor(f);
        } else {
            abrirEditor(f);
        }
    }

    private String obtenerExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx != -1 ? name.substring(idx).toUpperCase() : "";
    }

    private void poblarNodos(File dir, DefaultMutableTreeNode nodo) {
        File[] archivos = dir.listFiles();
        if (archivos != null) {
            Arrays.sort(archivos, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for (File f : archivos) {
                DefaultMutableTreeNode hijo = new DefaultMutableTreeNode(f.getName());
                nodo.add(hijo);
                if (f.isDirectory()) {
                    poblarNodos(f, hijo);
                }
            }
        }
    }

    private void organizarArchivos(File carpeta) {
        File imgDir = new File(carpeta, "Mis Imágenes");
        File docDir = new File(carpeta, "Mis Documentos");
        File musDir = new File(carpeta, "Música");
        imgDir.mkdirs(); docDir.mkdirs(); musDir.mkdirs();

        File[] archivos = carpeta.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                if (f.isFile()) {
                    String name = f.getName().toLowerCase();
                    try {
                        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                            Files.move(f.toPath(), new File(imgDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
                            Files.move(f.toPath(), new File(musDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else if (!name.endsWith(".sop") || !f.getName().equalsIgnoreCase("usuarios.sop")) {
                            Files.move(f.toPath(), new File(docDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private boolean eliminarDirectorioRecursivo(File elemento) {
        if (elemento.isDirectory()) {
            File[] hijos = elemento.listFiles();
            if (hijos != null) {
                for (File h : hijos) {
                    eliminarDirectorioRecursivo(h);
                }
            }
        }
        return elemento.delete();
    }

    // EDITOR DE TEXTO
    private JPanel crearEditorReal(File archivoParaAbrir) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(32, 32, 32));

        final File[] archivoActualEnEditor = {archivoParaAbrir};

        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 15));
        textPane.setBackground(new Color(30, 30, 30));
        textPane.setForeground(new Color(240, 240, 240));
        textPane.setCaretColor(Color.WHITE);
        textPane.setSelectionColor(new Color(0, 120, 215));
        textPane.setSelectedTextColor(Color.WHITE);
        textPane.setBorder(new EmptyBorder(15, 20, 15, 20));

        SimpleAttributeSet defaultAttrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(defaultAttrs, "Consolas");
        StyleConstants.setFontSize(defaultAttrs, 15);
        StyleConstants.setForeground(defaultAttrs, new Color(240, 240, 240));
        ((MutableAttributeSet) textPane.getInputAttributes()).addAttributes(defaultAttrs);
        textPane.setCharacterAttributes(defaultAttrs, false);

        if (archivoParaAbrir != null && archivoParaAbrir.exists()) {
            cargarArchivoEnEditor(textPane, archivoParaAbrir);
        }

        JScrollPane scrollEditor = new JScrollPane(textPane);
        scrollEditor.setBorder(null);
        scrollEditor.setBackground(new Color(30, 30, 30));

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(38, 38, 38));

        JToolBar ribbon = new JToolBar();
        ribbon.setFloatable(false);
        ribbon.setBackground(new Color(38, 38, 38));
        ribbon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 55)),
                new EmptyBorder(6, 10, 6, 10)
        ));

        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> cbFuentes = new JComboBox<>(fuentes);
        cbFuentes.setSelectedItem("Consolas");
        cbFuentes.setMaximumSize(new Dimension(160, 28));
        estilizarComboBoxOscuro(cbFuentes);

        Integer[] tamanos = {10, 12, 14, 16, 18, 20, 24, 28, 32, 40};
        JComboBox<Integer> cbTamanos = new JComboBox<>(tamanos);
        cbTamanos.setSelectedItem(15);
        cbTamanos.setMaximumSize(new Dimension(65, 28));
        estilizarComboBoxOscuro(cbTamanos);

        final Color[] colorActual = {new Color(240, 240, 240)};
        final boolean[] isBold = {false};
        final boolean[] isItalic = {false};
        final boolean[] isUnderline = {false};

        Runnable actualizarFormato = () -> {
            String f = (String) cbFuentes.getSelectedItem();
            Integer t = (Integer) cbTamanos.getSelectedItem();
            if (f != null && t != null) {
                aplicarFormatoTexto(textPane, f, t, colorActual[0], isBold[0], isItalic[0], isUnderline[0]);
            }
        };

        JButton btnColor = crearBotonPersonalizado("Color", new Color(48, 48, 54), new Color(68, 70, 80));
        btnColor.addActionListener(e -> {
            Color nuevo = JColorChooser.showDialog(this, "Selecciona Color de Texto", colorActual[0]);
            if (nuevo != null) {
                colorActual[0] = nuevo;
                btnColor.setForeground(colorActual[0]);
                actualizarFormato.run();
            }
        });

        JButton btnBold = crearBotonPersonalizado("B", new Color(48, 48, 54), new Color(68, 70, 80));
        btnBold.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBold.setPreferredSize(new Dimension(34, 28));

        JButton btnItalic = crearBotonPersonalizado("I", new Color(48, 48, 54), new Color(68, 70, 80));
        btnItalic.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        btnItalic.setPreferredSize(new Dimension(34, 28));

        JButton btnUnderline = crearBotonPersonalizado("U", new Color(48, 48, 54), new Color(68, 70, 80));
        btnUnderline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnUnderline.setPreferredSize(new Dimension(34, 28));

        btnBold.addActionListener(e -> {
            isBold[0] = !isBold[0];
            btnBold.setBackground(isBold[0] ? new Color(75, 75, 85) : new Color(48, 48, 54));
            actualizarFormato.run();
        });

        btnItalic.addActionListener(e -> {
            isItalic[0] = !isItalic[0];
            btnItalic.setBackground(isItalic[0] ? new Color(75, 75, 85) : new Color(48, 48, 54));
            actualizarFormato.run();
        });

        btnUnderline.addActionListener(e -> {
            isUnderline[0] = !isUnderline[0];
            btnUnderline.setBackground(isUnderline[0] ? new Color(75, 75, 85) : new Color(48, 48, 54));
            actualizarFormato.run();
        });

        cbFuentes.addActionListener(e -> actualizarFormato.run());
        cbTamanos.addActionListener(e -> actualizarFormato.run());

        JButton btnAplicar = crearBotonPersonalizado("Aplicar", ACCENT_BLUE, new Color(25, 145, 255));
        btnAplicar.addActionListener(e -> actualizarFormato.run());

        JButton btnGuardar = crearBotonPersonalizado("Guardar", ACCENT_BLUE, new Color(25, 145, 255));
        btnGuardar.addActionListener(e -> {
            if (archivoActualEnEditor[0] != null) {
                guardarArchivoDesdeEditor(textPane, archivoActualEnEditor[0]);
            } else {
                JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File arch = fc.getSelectedFile();
                    if (!arch.getName().endsWith(".sop") && !arch.getName().endsWith(".txt")) {
                        arch = new File(arch.getAbsolutePath() + ".sop");
                    }
                    archivoActualEnEditor[0] = arch;
                    guardarArchivoDesdeEditor(textPane, arch);
                }
            }
        });

        JButton btnGuardarComo = crearBotonPersonalizado("Guardar como...", new Color(48, 48, 54), new Color(68, 70, 80));
        btnGuardarComo.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arch = fc.getSelectedFile();
                if (!arch.getName().endsWith(".sop") && !arch.getName().endsWith(".txt")) {
                    arch = new File(arch.getAbsolutePath() + ".sop");
                }
                archivoActualEnEditor[0] = arch;
                guardarArchivoDesdeEditor(textPane, arch);
            }
        });

        JButton btnAbrir = crearBotonPersonalizado("Abrir", new Color(48, 48, 54), new Color(68, 70, 80));
        btnAbrir.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                archivoActualEnEditor[0] = fc.getSelectedFile();
                cargarArchivoEnEditor(textPane, fc.getSelectedFile());
            }
        });

        JLabel lblFuente = new JLabel("  Fuente: ");
        lblFuente.setForeground(new Color(200, 200, 200));
        ribbon.add(lblFuente);
        ribbon.add(cbFuentes);

        JLabel lblTam = new JLabel("  Tamaño: ");
        lblTam.setForeground(new Color(200, 200, 200));
        ribbon.add(lblTam);
        ribbon.add(cbTamanos);

        ribbon.add(Box.createHorizontalStrut(8));
        ribbon.add(btnColor);
        ribbon.add(Box.createHorizontalStrut(4));
        ribbon.add(btnBold);
        ribbon.add(btnItalic);
        ribbon.add(btnUnderline);
        ribbon.add(Box.createHorizontalStrut(6));
        ribbon.add(btnAplicar);
        ribbon.addSeparator();
        ribbon.add(btnAbrir);
        ribbon.add(btnGuardar);
        ribbon.add(btnGuardarComo);

        topContainer.add(ribbon, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(24, 24, 24));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)),
                new EmptyBorder(4, 15, 4, 15)
        ));

        JLabel lblStatusLeft = new JLabel("Ln 1, Col 1   |   0 caracteres");
        lblStatusLeft.setForeground(new Color(150, 150, 150));
        lblStatusLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lblStatusRight = new JLabel("Formato: Binario (.sop)   |   100%   |   Windows (CRLF)   |   UTF-8");
        lblStatusRight.setForeground(new Color(150, 150, 150));
        lblStatusRight.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        statusBar.add(lblStatusLeft, BorderLayout.WEST);
        statusBar.add(lblStatusRight, BorderLayout.EAST);

        textPane.addCaretListener(e -> {
            try {
                int pos = textPane.getCaretPosition();
                Element root = textPane.getDocument().getDefaultRootElement();
                int line = root.getElementIndex(pos) + 1;
                int col = pos - root.getElement(line - 1).getStartOffset() + 1;
                int totalChars = textPane.getDocument().getLength();
                lblStatusLeft.setText("Ln " + line + ", Col " + col + "   |   " + totalChars + " caracteres");
            } catch (Exception ignored) {}
        });

        p.add(topContainer, BorderLayout.NORTH);
        p.add(scrollEditor, BorderLayout.CENTER);
        p.add(statusBar, BorderLayout.SOUTH);

        return p;
    }

    private void cargarArchivoEnEditor(JTextPane textPane, File arch) {
        if (arch == null || !arch.exists()) return;
        try {
            if (arch.getName().equalsIgnoreCase("usuarios.sop")) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arch))) {
                    Object obj = ois.readObject();
                    if (obj instanceof Lista) {
                        @SuppressWarnings("unchecked")
                        Lista<Usuario> lista = (Lista<Usuario>) obj;
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== REGISTRO BINARIO DEL SISTEMA OPERATIVO (usuarios.sop) ===\n\n");
                        Nodo<Usuario> cur = lista.getHead();
                        int i = 1;
                        while (cur != null) {
                            Usuario u = cur.getDato();
                            boolean act = u.getUsername().equalsIgnoreCase("admin") ? true : u.isActivo();
                            sb.append(String.format("[%d] Usuario: %-15s | Nombre: %-20s | Rol: %-12s | Estado: %s\n",
                                    i++, u.getUsername(), u.getNombreCompleto(), (u.isEsAdmin() ? "Admin" : "Estándar"), (act ? "Activa" : "Inactiva")));
                            cur = cur.getSiguiente();
                        }
                        textPane.setText(sb.toString());
                        aplicarFormatoTexto(textPane, "Consolas", 14, Color.WHITE, false, false, false);
                        return;
                    }
                } catch (Exception ignored) {}
            }

            if (arch.getName().toLowerCase().endsWith(".sop")) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arch))) {
                    Object obj = ois.readObject();
                    if (obj instanceof StyledDocument) {
                        textPane.setStyledDocument((StyledDocument) obj);
                        textPane.setCaretColor(Color.WHITE);
                        return;
                    }
                } catch (Exception ignored) {}
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(arch), "UTF-8"))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    sb.append(linea).append("\n");
                }
            }
            textPane.setText(sb.toString());
            aplicarFormatoTexto(textPane, "Consolas", 14, Color.WHITE, false, false, false);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al abrir archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarArchivoDesdeEditor(JTextPane textPane, File arch) {
        try {
            if (arch.getName().toLowerCase().endsWith(".sop")) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arch))) {
                    oos.writeObject(textPane.getStyledDocument());
                }
            } else {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(arch), "UTF-8"))) {
                    bw.write(textPane.getText());
                }
            }
            JOptionPane.showMessageDialog(this, "Documento guardado exitosamente en: " + arch.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void estilizarComboBoxOscuro(JComboBox<?> cb) {
        cb.setBackground(new Color(38, 38, 42));
        cb.setForeground(Color.WHITE);
        cb.setOpaque(false);
        cb.setBorder(BorderFactory.createLineBorder(new Color(65, 65, 72), 1, true));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▼") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(new Color(38, 38, 42));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(200, 200, 200));
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString("▼", (getWidth() - fm.stringWidth("▼")) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
                        g2.dispose();
                    }
                };
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(38, 38, 42));
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? ACCENT_BLUE : new Color(30, 30, 34));
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });
    }

    private void aplicarFormatoTexto(JTextPane pane, String fuente, int tamano, Color color, boolean bold, boolean italic, boolean underline) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        if (fuente != null) StyleConstants.setFontFamily(attrs, fuente);
        if (tamano > 0) StyleConstants.setFontSize(attrs, tamano);
        if (color != null) StyleConstants.setForeground(attrs, color);
        StyleConstants.setBold(attrs, bold);
        StyleConstants.setItalic(attrs, italic);
        StyleConstants.setUnderline(attrs, underline);

        int start = pane.getSelectionStart();
        int end = pane.getSelectionEnd();
        StyledDocument doc = pane.getStyledDocument();

        if (start != end) {
            doc.setCharacterAttributes(start, end - start, attrs, false);
        } else {
            MutableAttributeSet inputAttrs = (MutableAttributeSet) pane.getInputAttributes();
            inputAttrs.addAttributes(attrs);
            pane.setCharacterAttributes(attrs, false);
        }
    }

    // CONSOLA CMD
    private JPanel crearCmdReal() {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea areaCmd = new JTextArea();
        areaCmd.setBackground(new Color(12, 12, 12));
        areaCmd.setForeground(new Color(0, 255, 100));
        areaCmd.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaCmd.setEditable(false);
        areaCmd.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(areaCmd), BorderLayout.CENTER);

        File dirInicial = usuarioActual.isEsAdmin()
                ? new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA)
                : new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername());
        
        final File[] dirActual = {dirInicial};

        JTextField input = new JTextField();
        input.setBackground(new Color(12, 12, 12));
        input.setForeground(Color.WHITE);
        input.setFont(new Font("Consolas", Font.BOLD, 14));
        input.setCaretColor(Color.WHITE);
        input.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel sur = new JPanel(new BorderLayout());
        String promptInicial = " " + dirActual[0].getPath().replace(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA).getAbsolutePath(), "Z:") + "> ";
        JLabel lblPrompt = new JLabel(promptInicial);
        lblPrompt.setForeground(new Color(0, 255, 100));
        lblPrompt.setBackground(new Color(12, 12, 12));
        lblPrompt.setOpaque(true);
        sur.add(lblPrompt, BorderLayout.WEST);
        sur.add(input, BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);

        areaCmd.append("Microsoft Windows [Versión Simulada 2.0]\n(c) UNITEC Programación II. Todos los derechos reservados.\n\n");
        areaCmd.append("Escribe 'help' para ver la lista de comandos disponibles.\n\n");

        input.addActionListener(e -> {
            String cmd = input.getText().trim();
            areaCmd.append(lblPrompt.getText() + cmd + "\n");
            input.setText("");

            String[] partes = cmd.split(" ", 2);
            String comando = partes[0].toLowerCase();
            String arg = partes.length > 1 ? partes[1].trim() : "";

            switch (comando) {
                case "help":
                    areaCmd.append("Comandos disponibles en Mini-Windows CMD:\n");
                    areaCmd.append(String.format("  %-16s %s\n", "mkdir <nombre>", "Crea una nueva carpeta en la ruta actual."));
                    areaCmd.append(String.format("  %-16s %s\n", "rm <nombre>", "Elimina un archivo o carpeta."));
                    areaCmd.append(String.format("  %-16s %s\n", "cd <carpeta>", "Cambia al directorio indicado."));
                    areaCmd.append(String.format("  %-16s %s\n", "cd..", "Regresa a la carpeta anterior."));
                    areaCmd.append(String.format("  %-16s %s\n", "dir", "Lista todas las carpetas y archivos."));
                    areaCmd.append(String.format("  %-16s %s\n", "date", "Muestra la fecha actual del sistema."));
                    areaCmd.append(String.format("  %-16s %s\n", "time", "Muestra la hora actual del sistema."));
                    areaCmd.append(String.format("  %-16s %s\n", "cls", "Limpia la pantalla de la consola."));
                    areaCmd.append(String.format("  %-16s %s\n", "help", "Muestra esta lista de comandos."));
                    break;

                case "mkdir":
                    if (!arg.isEmpty()) {
                        File nueva = new File(dirActual[0], arg);
                        if (nueva.mkdir()) {
                            areaCmd.append("Directorio creado exitosamente.\n");
                        } else {
                            areaCmd.append("Error al crear carpeta o ya existe.\n");
                        }
                    } else {
                        areaCmd.append("Uso: mkdir <nombre>\n");
                    }
                    break;

                case "rm":
                    if (arg.isEmpty()) {
                        areaCmd.append("Uso: rm <nombre>\n");
                    } else {
                        File aBorrar = new File(dirActual[0], arg);
                        if (!aBorrar.exists()) {
                            areaCmd.append("No se encontró el archivo o directorio: " + arg + "\n");
                        } else {
                            File raizSimulada = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA);
                            File raizPersonal = new File(raizSimulada, usuarioActual.getUsername());

                            boolean tienePermiso;
                            if (usuarioActual.isEsAdmin()) {
                                tienePermiso = !aBorrar.getAbsolutePath().equals(raizSimulada.getAbsolutePath());
                            } else {
                                tienePermiso = aBorrar.getAbsolutePath().startsWith(raizPersonal.getAbsolutePath()) 
                                        && !aBorrar.getAbsolutePath().equals(raizPersonal.getAbsolutePath());
                            }

                            if (!tienePermiso) {
                                areaCmd.append("Acceso denegado: No tiene permisos para eliminar este elemento.\n");
                            } else {
                                if (eliminarDirectorioRecursivo(aBorrar)) {
                                    areaCmd.append("Elemento '" + arg + "' eliminado exitosamente.\n");
                                } else {
                                    areaCmd.append("Error al eliminar el elemento.\n");
                                }
                            }
                        }
                    }
                    break;

                case "cd":
                    File destino = new File(dirActual[0], arg);
                    if (destino.exists() && destino.isDirectory()) {
                        File raizPersonal = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername());
                        if (!usuarioActual.isEsAdmin() && !destino.getAbsolutePath().startsWith(raizPersonal.getAbsolutePath())) {
                            areaCmd.append("Acceso denegado: Fuera del directorio personal de " + usuarioActual.getUsername() + ".\n");
                        } else {
                            dirActual[0] = destino;
                        }
                    } else {
                        areaCmd.append("Ruta no válida o el directorio no existe.\n");
                    }
                    break;

                case "cd..":
                    File padre = dirActual[0].getParentFile();
                    File raizSimulada = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA);
                    File raizPersonal = new File(raizSimulada, usuarioActual.getUsername());

                    if (padre != null) {
                        if (usuarioActual.isEsAdmin()) {
                            if (padre.getAbsolutePath().startsWith(raizSimulada.getAbsolutePath())) {
                                dirActual[0] = padre;
                            }
                        } else {
                            if (padre.getAbsolutePath().startsWith(raizPersonal.getAbsolutePath())) {
                                dirActual[0] = padre;
                            }
                        }
                    }
                    break;

                case "dir":
                    File[] fList = dirActual[0].listFiles();
                    if (fList != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  hh:mm a");
                        for (File f : fList) {
                            String fecha = sdf.format(new Date(f.lastModified()));
                            String tipo = f.isDirectory() ? "<DIR>          " : String.format("%15s", f.length() + " B");
                            areaCmd.append(fecha + "    " + tipo + " " + f.getName() + "\n");
                        }
                    }
                    break;

                case "date":
                    areaCmd.append("Fecha actual: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "\n");
                    break;

                case "time":
                    areaCmd.append("Hora actual: " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n");
                    break;

                case "cls":
                    areaCmd.setText("");
                    break;

                default:
                    areaCmd.append("'" + comando + "' no se reconoce como un comando interno. Escribe 'help' para ayuda.\n");
            }
            
            String promptActual = dirActual[0].getAbsolutePath().replace(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA).getAbsolutePath(), "Z:");
            lblPrompt.setText(" " + promptActual + "> ");
        });

        return p;
    }

    // VISOR DE FOTOS
    private JPanel crearVisorReal(File fotoInicial) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(24, 24, 27));

        Lista<File> listaFotos = new Lista<>();
        final int[] indexActual = {0};

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(32, 32, 36));
        topBar.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel lblTituloFoto = new JLabel("Sin imagen", SwingConstants.CENTER);
        lblTituloFoto.setForeground(Color.WHITE);
        lblTituloFoto.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panelBotonesTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotonesTop.setOpaque(false);

        JButton btnAgregarFoto = crearBotonPersonalizado("Agregar Imagen...", new Color(45, 46, 52), new Color(65, 68, 78));
        JButton btnEliminarFoto = crearBotonPersonalizado("Eliminar", new Color(60, 30, 35), new Color(180, 40, 50));

        panelBotonesTop.add(btnAgregarFoto);
        panelBotonesTop.add(btnEliminarFoto);

        topBar.add(lblTituloFoto, BorderLayout.CENTER);
        topBar.add(panelBotonesTop, BorderLayout.EAST);
        p.add(topBar, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(new Color(20, 20, 22));

        JLabel lblImgPrincipal = new JLabel("", SwingConstants.CENTER);
        centerContainer.add(lblImgPrincipal, BorderLayout.CENTER);

        JButton btnAnt = crearBotonVectorial(1);
        btnAnt.setPreferredSize(new Dimension(42, 42));
        JButton btnSig = crearBotonVectorial(2);
        btnSig.setPreferredSize(new Dimension(42, 42));

        JPanel panelLeft = new JPanel(new GridBagLayout());
        panelLeft.setOpaque(false);
        panelLeft.setBorder(new EmptyBorder(0, 10, 0, 0));
        panelLeft.add(btnAnt);

        JPanel panelRight = new JPanel(new GridBagLayout());
        panelRight.setOpaque(false);
        panelRight.setBorder(new EmptyBorder(0, 0, 0, 10));
        panelRight.add(btnSig);

        centerContainer.add(panelLeft, BorderLayout.WEST);
        centerContainer.add(panelRight, BorderLayout.EAST);
        p.add(centerContainer, BorderLayout.CENTER);

        JPanel stripThumbnails = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        stripThumbnails.setBackground(new Color(16, 16, 18));

        JScrollPane scrollStrip = new JScrollPane(stripThumbnails);
        scrollStrip.setPreferredSize(new Dimension(0, 85));
        scrollStrip.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 45)));
        scrollStrip.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStrip.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p.add(scrollStrip, BorderLayout.SOUTH);

        Runnable mostrarFotoActual = () -> {
            if (listaFotos.estaVacia()) {
                lblImgPrincipal.setIcon(null);
                lblImgPrincipal.setText("No hay imágenes en la carpeta.");
                lblTituloFoto.setText("Sin imágenes");
                stripThumbnails.removeAll();
                stripThumbnails.revalidate();
                stripThumbnails.repaint();
                return;
            }

            File imgFile = listaFotos.obtener(indexActual[0]);
            lblTituloFoto.setText(imgFile.getName() + " (" + (indexActual[0] + 1) + " de " + listaFotos.getSize() + ")");

            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
            int maxW = Math.max(300, centerContainer.getWidth() - 120);
            int maxH = Math.max(200, centerContainer.getHeight() - 20);
            Image scaled = icon.getImage().getScaledInstance(Math.min(maxW, icon.getIconWidth()), Math.min(maxH, icon.getIconHeight()), Image.SCALE_SMOOTH);
            lblImgPrincipal.setIcon(new ImageIcon(scaled));
            lblImgPrincipal.setText("");

            for (int i = 0; i < stripThumbnails.getComponentCount(); i++) {
                Component comp = stripThumbnails.getComponent(i);
                if (comp instanceof JLabel) {
                    JLabel thumb = (JLabel) comp;
                    if (i == indexActual[0]) {
                        thumb.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2, true));
                    } else {
                        thumb.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 1, true));
                    }
                }
            }
        };

        File dirFotos = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Imágenes");
        Runnable recargarFotos = () -> {
            while (!listaFotos.estaVacia()) {
                listaFotos.eliminar(listaFotos.obtener(0));
            }
            stripThumbnails.removeAll();

            File[] fotos = dirFotos.listFiles((dir, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".jpeg");
            });

            if (fotos != null && fotos.length > 0) {
                for (int i = 0; i < fotos.length; i++) {
                    File f = fotos[i];
                    listaFotos.agregar(f);

                    final int idx = i;
                    ImageIcon rawIcon = new ImageIcon(f.getAbsolutePath());
                    Image thumbImg = rawIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    JLabel thumbLabel = new JLabel(new ImageIcon(thumbImg));
                    thumbLabel.setPreferredSize(new Dimension(60, 60));
                    thumbLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    thumbLabel.setToolTipText(f.getName());

                    thumbLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            indexActual[0] = idx;
                            mostrarFotoActual.run();
                        }
                    });

                    stripThumbnails.add(thumbLabel);
                }
            }

            if (fotoInicial != null && fotoInicial.exists()) {
                for (int i = 0; i < listaFotos.getSize(); i++) {
                    if (listaFotos.obtener(i).getName().equals(fotoInicial.getName())) {
                        indexActual[0] = i;
                        break;
                    }
                }
            } else if (indexActual[0] >= listaFotos.getSize()) {
                indexActual[0] = Math.max(0, listaFotos.getSize() - 1);
            }

            stripThumbnails.revalidate();
            stripThumbnails.repaint();
            mostrarFotoActual.run();
        };

        recargarFotos.run();

        btnAgregarFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                String n = sel.getName().toLowerCase();
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                    try {
                        File dest = new File(dirFotos, sel.getName());
                        Files.copy(sel.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        recargarFotos.run();
                        for (int i = 0; i < listaFotos.getSize(); i++) {
                            if (listaFotos.obtener(i).getName().equals(dest.getName())) {
                                indexActual[0] = i;
                                break;
                            }
                        }
                        mostrarFotoActual.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error al importar imagen: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor seleccione una imagen válida (.png o .jpg)");
                }
            }
        });

        btnAnt.addActionListener(e -> {
            if (!listaFotos.estaVacia()) {
                indexActual[0] = (indexActual[0] - 1 + listaFotos.getSize()) % listaFotos.getSize();
                mostrarFotoActual.run();
            }
        });

        btnSig.addActionListener(e -> {
            if (!listaFotos.estaVacia()) {
                indexActual[0] = (indexActual[0] + 1) % listaFotos.getSize();
                mostrarFotoActual.run();
            }
        });

        btnEliminarFoto.addActionListener(e -> {
            if (listaFotos.estaVacia()) return;
            File f = listaFotos.obtener(indexActual[0]);
            int resp = JOptionPane.showConfirmDialog(this, "¿Deseas eliminar permanentemente '" + f.getName() + "'?", "Eliminar Foto", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                f.delete();
                recargarFotos.run();
            }
        });

        return p;
    }

    private static class MotorAudioPlayer {
        private Player playerJLayer = null;
        private FileInputStream fis = null;
        private BufferedInputStream bis = null;
        private Clip clipWav = null;
        private boolean isWav = false;
        private Thread hiloReproductor = null;
        private File archivoActual = null;
        private long totalBytes = 0;
        private long bytesPausados = 0;
        private long microsegundosWavPausa = 0;
        private boolean isPaused = false;
        private boolean isPlaying = false;
        private int segundosTranscurridos = 0;
        private int duracionTotalSegundos = 180;

        public synchronized void reproducir(File file) throws Exception {
            detener();
            this.archivoActual = file;
            this.totalBytes = file.length();
            this.bytesPausados = 0;
            this.segundosTranscurridos = 0;

            String name = file.getName().toLowerCase();

            if (name.endsWith(".wav") || name.endsWith(".au") || name.endsWith(".aiff")) {
                isWav = true;
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                clipWav = AudioSystem.getClip();
                clipWav.open(ais);
                duracionTotalSegundos = (int) (clipWav.getMicrosecondLength() / 1_000_000);
                isPlaying = true;
                isPaused = false;
                clipWav.start();
                return;
            }

            isWav = false;
            duracionTotalSegundos = calcularDuracionRealMP3(file);
            iniciarStreamDesdeOffset(0);
        }

        private int calcularDuracionRealMP3(File file) {
            try (FileInputStream f = new FileInputStream(file)) {
                Bitstream bs = new Bitstream(f);
                Header h = bs.readFrame();
                if (h != null) {
                    int ms = (int) h.total_ms((int) file.length());
                    bs.close();
                    return Math.max(1, ms / 1000);
                }
            } catch (Exception ignored) {}
            return Math.max(30, (int) (file.length() / (192 * 1024 / 8)));
        }

        private synchronized void iniciarStreamDesdeOffset(long offset) throws Exception {
            if (archivoActual == null) return;
            fis = new FileInputStream(archivoActual);
            if (offset > 0) {
                fis.skip(offset);
            }
            bis = new BufferedInputStream(fis);
            playerJLayer = new Player(bis);
            isPlaying = true;
            isPaused = false;

            hiloReproductor = new Thread(() -> {
                try {
                    playerJLayer.play();
                } catch (Exception ex) {
                    isPlaying = false;
                }
            });
            hiloReproductor.setDaemon(true);
            hiloReproductor.start();
        }

        public synchronized void buscarPosicion(double porcentaje) {
            if (archivoActual == null) return;
            porcentaje = Math.max(0.0, Math.min(1.0, porcentaje));
            
            if (isWav && clipWav != null && clipWav.isOpen()) {
                long targetMicros = (long) (porcentaje * clipWav.getMicrosecondLength());
                clipWav.setMicrosecondPosition(targetMicros);
                if (isPaused) microsegundosWavPausa = targetMicros;
            } else {
                long targetBytes = (long) (porcentaje * totalBytes);
                this.bytesPausados = targetBytes;
                this.segundosTranscurridos = (int) (porcentaje * duracionTotalSegundos);

                if (isPlaying && !isPaused) {
                    try {
                        if (playerJLayer != null) playerJLayer.close();
                        if (bis != null) bis.close();
                        if (fis != null) fis.close();
                        iniciarStreamDesdeOffset(targetBytes);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        public synchronized void pausar() {
            if (!isPlaying || isPaused) return;
            isPaused = true;
            isPlaying = false;

            if (isWav && clipWav != null && clipWav.isOpen()) {
                microsegundosWavPausa = clipWav.getMicrosecondPosition();
                clipWav.stop();
            } else {
                try {
                    if (fis != null) {
                        bytesPausados = totalBytes - fis.available();
                    }
                    if (playerJLayer != null) {
                        playerJLayer.close();
                    }
                } catch (Exception ignored) {}
            }
        }

        public synchronized void reanudar() {
            if (!isPaused || archivoActual == null) return;
            if (isWav && clipWav != null && clipWav.isOpen()) {
                clipWav.setMicrosecondPosition(microsegundosWavPausa);
                clipWav.start();
                isPlaying = true;
                isPaused = false;
            } else {
                try {
                    iniciarStreamDesdeOffset(bytesPausados);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public synchronized void detener() {
            isPlaying = false;
            isPaused = false;
            bytesPausados = 0;
            segundosTranscurridos = 0;
            microsegundosWavPausa = 0;

            if (isWav && clipWav != null) {
                try {
                    clipWav.stop();
                    clipWav.close();
                } catch (Exception ignored) {}
                clipWav = null;
            }

            if (playerJLayer != null) {
                try {
                    playerJLayer.close();
                } catch (Exception ignored) {}
                playerJLayer = null;
            }
            if (bis != null) {
                try { bis.close(); } catch (Exception ignored) {}
            }
            if (fis != null) {
                try { fis.close(); } catch (Exception ignored) {}
            }
        }

        public void tickSegundo() {
            if (isPlaying && !isPaused) {
                segundosTranscurridos++;
            }
        }

        public int getSegundosTranscurridos() {
            if (isWav && clipWav != null && clipWav.isOpen()) {
                return (int) (clipWav.getMicrosecondPosition() / 1_000_000);
            }
            return segundosTranscurridos;
        }

        public int getDuracionTotalSegundos() { return duracionTotalSegundos; }
        public boolean estaReproduciendo() { return isPlaying; }
        public boolean estaPausado() { return isPaused; }
        public File getArchivoActual() { return archivoActual; }
    }

    private final MotorAudioPlayer motorAudio = new MotorAudioPlayer();

    private static class MetadataCancion implements Serializable {
        private static final long serialVersionUID = 3L;
        String autor;
        String album;
        String descripcion;
        String caratulaNombre;

        public MetadataCancion(String autor, String album, String descripcion, String caratulaNombre) {
            this.autor = autor;
            this.album = album;
            this.descripcion = descripcion;
            this.caratulaNombre = caratulaNombre;
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, MetadataCancion> cargarMetadatosMusica(File dirMusica) {
        File metaFile = new File(dirMusica, "metadatos_musica.sop");
        if (metaFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metaFile))) {
                return (HashMap<String, MetadataCancion>) ois.readObject();
            } catch (Exception ignored) {}
        }
        return new HashMap<>();
    }

    private void guardarMetadatosMusica(File dirMusica, HashMap<String, MetadataCancion> map) {
        File metaFile = new File(dirMusica, "metadatos_musica.sop");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metaFile))) {
            oos.writeObject(map);
        } catch (Exception ignored) {}
    }

    private String formatearSegundos(int segs) {
        int m = segs / 60;
        int s = segs % 60;
        return String.format("%d:%02d", m, s);
    }

    private JPanel crearReproductorReal(File cancionParaTocar) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(18, 18, 18));

        File dirMusica = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música");
        HashMap<String, MetadataCancion> mapaMetadatos = cargarMetadatosMusica(dirMusica);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(18, 18, 20));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(38, 38, 42)));

        JPanel topSidebar = new JPanel(new BorderLayout(5, 8));
        topSidebar.setOpaque(false);
        topSidebar.setBorder(new EmptyBorder(14, 12, 10, 12));

        JLabel lblLogo = new JLabel("  Media Player");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogo.setIcon(cargarIcono("musica_icono", 20, 20));
        topSidebar.add(lblLogo, BorderLayout.NORTH);

        JTextField searchBar = new JTextField("Buscar canción, artista...");
        searchBar.setPreferredSize(new Dimension(170, 28));
        searchBar.setBackground(new Color(30, 30, 34));
        searchBar.setForeground(new Color(150, 150, 150));
        searchBar.setCaretColor(Color.WHITE);
        searchBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        topSidebar.add(searchBar, BorderLayout.SOUTH);
        sidebar.add(topSidebar, BorderLayout.NORTH);

        JPanel navList = new JPanel(new GridLayout(3, 1, 4, 6));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 8, 10, 8));

        JButton btnBiblioteca = crearBotonSidebarItem("Mi Biblioteca", true);
        JButton btnAgregarAudio = crearBotonSidebarItem("Agregar Canción...", false);
        JButton btnAbrirCarpeta = crearBotonSidebarItem("Carpeta Música", false);

        navList.add(btnBiblioteca);
        navList.add(btnAgregarAudio);
        navList.add(btnAbrirCarpeta);

        sidebar.add(navList, BorderLayout.CENTER);
        p.add(sidebar, BorderLayout.WEST);

        CardLayout cardsCenter = new CardLayout();
        JPanel centerCards = new JPanel(cardsCenter);
        centerCards.setOpaque(false);

        JPanel vistaBiblioteca = new JPanel(new BorderLayout(15, 15));
        vistaBiblioteca.setBackground(new Color(18, 18, 18));
        vistaBiblioteca.setBorder(new EmptyBorder(16, 20, 10, 20));

        JLabel lblHeader = new JLabel("Música");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        vistaBiblioteca.add(lblHeader, BorderLayout.NORTH);

        String[] columnas = {"#", "Título", "Álbum", "Duración"};
        DefaultTableModel modelTablaSpotify = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tableSpotify = new JTable(modelTablaSpotify);
        tableSpotify.setBackground(new Color(18, 18, 18));
        tableSpotify.setForeground(Color.WHITE);
        tableSpotify.setRowHeight(48);
        tableSpotify.setShowGrid(false);
        tableSpotify.setIntercellSpacing(new Dimension(0, 0));
        tableSpotify.setSelectionBackground(new Color(40, 40, 44));
        tableSpotify.setSelectionForeground(Color.WHITE);

        tableSpotify.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(new Color(24, 24, 27));
                lbl.setForeground(new Color(180, 180, 180));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 50)),
                        new EmptyBorder(6, 8, 6, 8)
                ));
                return lbl;
            }
        });

        tableSpotify.getColumnModel().getColumn(0).setMaxWidth(35);
        tableSpotify.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableSpotify.getColumnModel().getColumn(3).setMaxWidth(75);

        tableSpotify.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
            private final JPanel cell = new JPanel(new BorderLayout(8, 0));
            private final JLabel lblThumb = new JLabel();
            private final JLabel lblTrack = new JLabel();
            private final JLabel lblArtist = new JLabel();
            private final JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));

            {
                cell.setOpaque(true);
                lblThumb.setPreferredSize(new Dimension(36, 36));
                lblTrack.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblArtist.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                textPanel.setOpaque(false);
                textPanel.add(lblTrack);
                textPanel.add(lblArtist);
                cell.add(lblThumb, BorderLayout.WEST);
                cell.add(textPanel, BorderLayout.CENTER);
                cell.setBorder(new EmptyBorder(4, 6, 4, 6));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof File) {
                    File f = (File) value;
                    MetadataCancion meta = mapaMetadatos.get(f.getName());
                    String tit = f.getName().replaceAll("(?i)\\.(mp3|wav|m4a|au)$", "");
                    String art = (meta != null && !meta.autor.isEmpty()) ? meta.autor : "Desconocido";

                    lblTrack.setText(truncarTexto(tit, 26));
                    lblArtist.setText(truncarTexto(art, 22));
                    lblTrack.setForeground(Color.WHITE);
                    lblArtist.setForeground(new Color(160, 160, 160));

                    File imgCaratula = (meta != null) ? resolverArchivoCaratula(dirMusica, meta.caratulaNombre) : null;
                    if (imgCaratula != null) {
                        ImageIcon raw = new ImageIcon(imgCaratula.getAbsolutePath());
                        lblThumb.setIcon(new ImageIcon(raw.getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH)));
                    } else {
                        lblThumb.setIcon(cargarIcono("musica_icono", 28, 28));
                    }
                }
                cell.setBackground(isSelected ? new Color(42, 42, 46) : new Color(18, 18, 18));
                return cell;
            }
        });

        DefaultTableCellRenderer renderSimple = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(isSelected ? new Color(42, 42, 46) : new Color(18, 18, 18));
                lbl.setForeground(new Color(179, 179, 179));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        };
        tableSpotify.getColumnModel().getColumn(0).setCellRenderer(renderSimple);
        tableSpotify.getColumnModel().getColumn(2).setCellRenderer(renderSimple);
        tableSpotify.getColumnModel().getColumn(3).setCellRenderer(renderSimple);

        JScrollPane scrollSpotify = new JScrollPane(tableSpotify);
        scrollSpotify.setBorder(BorderFactory.createLineBorder(new Color(38, 38, 42), 1, true));
        scrollSpotify.getViewport().setBackground(new Color(18, 18, 18));

        JPanel detailsPanel = new JPanel(new BorderLayout(10, 8));
        detailsPanel.setPreferredSize(new Dimension(280, 0));
        detailsPanel.setBackground(new Color(24, 24, 27));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 40, 45), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblCaratula = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getIcon() == null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(new GradientPaint(0, 0, new Color(45, 48, 56), getWidth(), getHeight(), new Color(20, 22, 26)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.setColor(new Color(234, 88, 12));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.fillOval(cx - 20, cy + 5, 14, 10);
                    g2.fillOval(cx + 6, cy - 2, 14, 10);
                    g2.fillRect(cx - 8, cy - 22, 3, 30);
                    g2.fillRect(cx + 18, cy - 28, 3, 30);
                    g2.fillRect(cx - 8, cy - 25, 29, 6);
                    g2.dispose();
                }
            }
        };
        lblCaratula.setPreferredSize(new Dimension(140, 125));
        detailsPanel.add(lblCaratula, BorderLayout.NORTH);

        JPanel infoCard = new JPanel(new BorderLayout(0, 6));
        infoCard.setOpaque(false);

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTextPanel.setOpaque(false);
        headerTextPanel.setPreferredSize(new Dimension(240, 44));

        JLabel lblSongTitle = new JLabel("Selecciona una canción");
        lblSongTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSongTitle.setForeground(Color.WHITE);

        JLabel lblSongSubtitle = new JLabel("--");
        lblSongSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSongSubtitle.setForeground(new Color(160, 160, 160));

        headerTextPanel.add(lblSongTitle);
        headerTextPanel.add(lblSongSubtitle);
        infoCard.add(headerTextPanel, BorderLayout.NORTH);

        JTextArea txtDescripcion = new JTextArea("Aquí aparecerá la descripción personalizada.");
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBackground(new Color(18, 18, 20));
        txtDescripcion.setForeground(new Color(225, 230, 240));
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDescripcion.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollDescPanel = new JScrollPane(txtDescripcion);
        scrollDescPanel.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 50), 1, true));
        scrollDescPanel.getViewport().setBackground(new Color(18, 18, 20));
        infoCard.add(scrollDescPanel, BorderLayout.CENTER);

        detailsPanel.add(infoCard, BorderLayout.CENTER);

        JSplitPane splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollSpotify, detailsPanel);
        splitCenter.setDividerLocation(450);
        splitCenter.setOpaque(false);
        splitCenter.setBorder(null);

        vistaBiblioteca.add(splitCenter, BorderLayout.CENTER);

        JPanel vistaAgregar = new JPanel(new BorderLayout(15, 15));
        vistaAgregar.setBackground(new Color(24, 24, 27));
        vistaAgregar.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel lblHeaderAdd = new JLabel("Agregar Nueva Canción a la Biblioteca");
        lblHeaderAdd.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderAdd.setForeground(Color.WHITE);
        vistaAgregar.add(lblHeaderAdd, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridLayout(1, 2, 20, 0));
        formContent.setBackground(new Color(30, 30, 34));
        formContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel colCaratula = new JPanel(new BorderLayout(10, 10));
        colCaratula.setOpaque(false);

        JLabel lblTitCaratula = new JLabel("Vista Previa de Carátula:", SwingConstants.CENTER);
        lblTitCaratula.setForeground(Color.WHITE);
        lblTitCaratula.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblPreviewCaratula = new JLabel("Sin Carátula", SwingConstants.CENTER);
        lblPreviewCaratula.setPreferredSize(new Dimension(180, 180));
        lblPreviewCaratula.setOpaque(true);
        lblPreviewCaratula.setBackground(new Color(20, 20, 22));
        lblPreviewCaratula.setForeground(TEXT_MUTED);
        lblPreviewCaratula.setBorder(BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true));

        final File[] archivoCaratulaSeleccionada = {null};
        final File[] archivoAudioSeleccionado = {null};

        JButton btnBuscarCaratula = crearBotonPersonalizado("Seleccionar Carátula (.jpg / .png)", new Color(45, 46, 52), new Color(65, 68, 78));

        btnBuscarCaratula.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File img = fc.getSelectedFile();
                String n = img.getName().toLowerCase();
                if (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                    archivoCaratulaSeleccionada[0] = img;
                    ImageIcon raw = new ImageIcon(img.getAbsolutePath());
                    Image scaled = raw.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                    lblPreviewCaratula.setIcon(new ImageIcon(scaled));
                    lblPreviewCaratula.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor seleccione una imagen válida (.png o .jpg)");
                }
            }
        });

        colCaratula.add(lblTitCaratula, BorderLayout.NORTH);
        colCaratula.add(lblPreviewCaratula, BorderLayout.CENTER);
        colCaratula.add(btnBuscarCaratula, BorderLayout.SOUTH);

        JPanel colDatos = new JPanel(new GridLayout(9, 1, 4, 3));
        colDatos.setOpaque(false);

        JLabel lblAudioSel = new JLabel("Ningún archivo de audio seleccionado");
        lblAudioSel.setForeground(TEXT_MUTED);
        lblAudioSel.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        JButton btnBuscarAudio = crearBotonPersonalizado("Elegir Archivo MP3 / WAV", new Color(45, 46, 52), new Color(65, 68, 78));

        btnBuscarAudio.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File audio = fc.getSelectedFile();
                String n = audio.getName().toLowerCase();
                if (n.endsWith(".mp3") || n.endsWith(".wav")) {
                    archivoAudioSeleccionado[0] = audio;
                    lblAudioSel.setText("Seleccionado: " + audio.getName());
                    lblAudioSel.setForeground(new Color(74, 222, 128));
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor seleccione un archivo .mp3 o .wav");
                }
            }
        });

        JLabel lblTitAutor = new JLabel("Autor / Artista:");
        lblTitAutor.setForeground(Color.WHITE);
        lblTitAutor.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtAutorInput = new JTextField();
        txtAutorInput.setBackground(new Color(20, 20, 22));
        txtAutorInput.setForeground(Color.WHITE);
        txtAutorInput.setCaretColor(Color.WHITE);
        txtAutorInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel lblTitAlbum = new JLabel("Álbum:");
        lblTitAlbum.setForeground(Color.WHITE);
        lblTitAlbum.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtAlbumInput = new JTextField();
        txtAlbumInput.setBackground(new Color(20, 20, 22));
        txtAlbumInput.setForeground(Color.WHITE);
        txtAlbumInput.setCaretColor(Color.WHITE);
        txtAlbumInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel lblTitDesc = new JLabel("Descripción breve (máximo 50 palabras):");
        lblTitDesc.setForeground(Color.WHITE);
        lblTitDesc.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextArea txtDescInput = new JTextArea(2, 20);
        txtDescInput.setLineWrap(true);
        txtDescInput.setWrapStyleWord(true);
        txtDescInput.setBackground(new Color(20, 20, 22));
        txtDescInput.setForeground(Color.WHITE);
        txtDescInput.setCaretColor(Color.WHITE);

        JScrollPane scrollDesc = new JScrollPane(txtDescInput);
        scrollDesc.setBorder(BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true));
        scrollDesc.setBackground(new Color(20, 20, 22));
        scrollDesc.getViewport().setBackground(new Color(20, 20, 22));

        JLabel lblConteoPalabras = new JLabel("Palabras: 0 / 50");
        lblConteoPalabras.setForeground(TEXT_MUTED);
        lblConteoPalabras.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        txtDescInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String t = txtDescInput.getText().trim();
                int cant = t.isEmpty() ? 0 : t.split("\\s+").length;
                lblConteoPalabras.setText("Palabras: " + cant + " / 50");
                if (cant > 50) lblConteoPalabras.setForeground(new Color(248, 113, 113));
                else lblConteoPalabras.setForeground(TEXT_MUTED);
            }
        });

        colDatos.add(btnBuscarAudio);
        colDatos.add(lblAudioSel);
        colDatos.add(lblTitAutor);
        colDatos.add(txtAutorInput);
        colDatos.add(lblTitAlbum);
        colDatos.add(txtAlbumInput);
        colDatos.add(lblTitDesc);
        colDatos.add(scrollDesc);
        colDatos.add(lblConteoPalabras);

        formContent.add(colCaratula);
        formContent.add(colDatos);
        vistaAgregar.add(formContent, BorderLayout.CENTER);

        JPanel formBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        formBotones.setOpaque(false);

        JButton btnCancelarForm = crearBotonPersonalizado("Cancelar", new Color(50, 52, 58), new Color(70, 72, 80));
        JButton btnGuardarForm = crearBotonPersonalizado("Guardar y Añadir a Biblioteca", new Color(234, 88, 12), new Color(249, 115, 22));

        formBotones.add(btnCancelarForm);
        formBotones.add(btnGuardarForm);
        vistaAgregar.add(formBotones, BorderLayout.SOUTH);

        centerCards.add(vistaBiblioteca, "BIBLIOTECA");
        centerCards.add(vistaAgregar, "AGREGAR");
        p.add(centerCards, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 4));
        bottomBar.setBackground(new Color(18, 18, 20));
        bottomBar.setBorder(new EmptyBorder(6, 18, 8, 18));

        JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
        progressPanel.setOpaque(false);

        JLabel lblTimeCur = new JLabel("00:00");
        lblTimeCur.setForeground(TEXT_MUTED);
        lblTimeCur.setFont(new Font("Consolas", Font.PLAIN, 11));

        JSlider progressBar = new JSlider(0, 100, 0);
        progressBar.setOpaque(false);

        JLabel lblTimeTotal = new JLabel("00:00");
        lblTimeTotal.setForeground(TEXT_MUTED);
        lblTimeTotal.setFont(new Font("Consolas", Font.PLAIN, 11));

        progressPanel.add(lblTimeCur, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(lblTimeTotal, BorderLayout.EAST);
        bottomBar.add(progressPanel, BorderLayout.NORTH);

        JPanel controlRow = new JPanel(new BorderLayout());
        controlRow.setOpaque(false);

        JLabel lblTrackTitle = new JLabel("Sin pista seleccionada");
        lblTrackTitle.setForeground(Color.WHITE);
        lblTrackTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTrackTitle.setPreferredSize(new Dimension(240, 36));
        controlRow.add(lblTrackTitle, BorderLayout.WEST);

        JPanel centerBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerBtns.setOpaque(false);

        JButton btnPrev = crearBotonVectorial(1);
        JButton btnPlay = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(234, 88, 12));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                if (getText().equals("PAUSE")) {
                    g2.fillRect(14, 12, 4, 16);
                    g2.fillRect(22, 12, 4, 16);
                } else {
                    int[] xPoints = {16, 16, 28};
                    int[] yPoints = {12, 28, 20};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }
                g2.dispose();
            }
        };
        btnPlay.setText("PLAY");
        btnPlay.setPreferredSize(new Dimension(40, 40));
        btnPlay.setContentAreaFilled(false);
        btnPlay.setBorderPainted(false);
        btnPlay.setFocusPainted(false);
        btnPlay.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnStop = crearBotonVectorial(3);
        JButton btnNext = crearBotonVectorial(2);

        centerBtns.add(btnPrev);
        centerBtns.add(btnPlay);
        centerBtns.add(btnStop);
        centerBtns.add(btnNext);
        controlRow.add(centerBtns, BorderLayout.CENTER);

        bottomBar.add(controlRow, BorderLayout.CENTER);
        p.add(bottomBar, BorderLayout.SOUTH);

        Runnable filtrarMusica = () -> {
            String rawQuery = searchBar.getText().trim().toLowerCase();
            String q = rawQuery.equals("buscar canción, artista...") ? "" : rawQuery;

            modelTablaSpotify.setRowCount(0);
            File[] canciones = dirMusica.listFiles((dir, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".mp3") || n.endsWith(".wav");
            });

            if (canciones != null) {
                int rowNum = 1;
                for (File f : canciones) {
                    MetadataCancion meta = mapaMetadatos.get(f.getName());
                    String autor = (meta != null && !meta.autor.isEmpty()) ? meta.autor : "Desconocido";
                    String alb = (meta != null && meta.album != null && !meta.album.isEmpty()) ? meta.album : "Sencillo";
                    String titLimpio = f.getName().replaceAll("(?i)\\.(mp3|wav|m4a|au)$", "");

                    boolean coincide = q.isEmpty()
                            || titLimpio.toLowerCase().contains(q)
                            || autor.toLowerCase().contains(q)
                            || alb.toLowerCase().contains(q)
                            || f.getName().toLowerCase().contains(q);

                    if (coincide) {
                        int durSeg = 0;
                        if (f.getName().toLowerCase().endsWith(".mp3")) {
                            try (FileInputStream fis = new FileInputStream(f)) {
                                Bitstream bs = new Bitstream(fis);
                                Header h = bs.readFrame();
                                if (h != null) durSeg = (int) (h.total_ms((int) f.length()) / 1000);
                                bs.close();
                            } catch (Exception ignored) {}
                        }
                        if (durSeg <= 0) durSeg = Math.max(30, (int) (f.length() / (192 * 1024 / 8)));

                        modelTablaSpotify.addRow(new Object[]{
                            String.valueOf(rowNum++),
                            f,
                            alb,
                            formatearSegundos(durSeg)
                        });
                    }
                }
            }
        };

        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filtrarMusica.run();
            }
        });

        searchBar.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchBar.getText().equals("Buscar canción, artista...")) {
                    searchBar.setText("");
                    searchBar.setForeground(Color.WHITE);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchBar.getText().trim().isEmpty()) {
                    searchBar.setText("Buscar canción, artista...");
                    searchBar.setForeground(new Color(150, 150, 150));
                    filtrarMusica.run();
                }
            }
        });

        filtrarMusica.run();

        tableSpotify.getSelectionModel().addListSelectionListener(e -> {
            int row = tableSpotify.getSelectedRow();
            if (row != -1 && row < modelTablaSpotify.getRowCount()) {
                File sel = (File) modelTablaSpotify.getValueAt(row, 1);
                lblTrackTitle.setText(sel.getName());
                MetadataCancion meta = mapaMetadatos.get(sel.getName());
                String autor = (meta != null && !meta.autor.isEmpty()) ? meta.autor : "Desconocido";
                String album = (meta != null && meta.album != null && !meta.album.isEmpty()) ? meta.album : "Sencillo";
                String desc = (meta != null && !meta.descripcion.isEmpty()) ? meta.descripcion : "Sin descripción personalizada disponible.";

                String titCompleto = sel.getName().replaceAll("(?i)\\.(mp3|wav|m4a|au)$", "");
                
                lblSongTitle.setText(truncarTexto(titCompleto, 20));
                lblSongSubtitle.setText(truncarTexto(autor, 14) + " • " + truncarTexto(album, 14));
                lblSongTitle.setToolTipText(titCompleto);
                lblSongSubtitle.setToolTipText(autor + " (" + album + ")");

                txtDescripcion.setText("[ DESCRIPCIÓN ]\n" + desc + "\n\n"
                        + "[ DETALLES ]\n"
                        + "• Formato: " + obtenerExtension(sel.getName()) + "\n"
                        + "• Tamaño: " + (sel.length() / 1024) + " KB\n"
                        + "• Ruta: " + sel.getAbsolutePath());

                File imgCaratula = (meta != null) ? resolverArchivoCaratula(dirMusica, meta.caratulaNombre) : null;
                if (imgCaratula != null) {
                    ImageIcon img = new ImageIcon(imgCaratula.getAbsolutePath());
                    Image scaled = img.getImage().getScaledInstance(140, 125, Image.SCALE_SMOOTH);
                    lblCaratula.setIcon(new ImageIcon(scaled));
                } else {
                    lblCaratula.setIcon(null);
                }
            }
        });

        tableSpotify.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableSpotify.getSelectedRow();
                    if (row != -1) {
                        File sel = (File) modelTablaSpotify.getValueAt(row, 1);
                        new Thread(() -> {
                            try {
                                motorAudio.reproducir(sel);
                                SwingUtilities.invokeLater(() -> {
                                    btnPlay.setText("PAUSE");
                                    btnPlay.repaint();
                                    lblTrackTitle.setText(sel.getName());
                                    lblTimeTotal.setText(formatearSegundos(motorAudio.getDuracionTotalSegundos()));
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    }
                }
            }
        });

        if (cancionParaTocar != null && cancionParaTocar.exists()) {
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < modelTablaSpotify.getRowCount(); i++) {
                    File f = (File) modelTablaSpotify.getValueAt(i, 1);
                    if (f.getName().equals(cancionParaTocar.getName())) {
                        tableSpotify.setRowSelectionInterval(i, i);
                        break;
                    }
                }
                new Thread(() -> {
                    try {
                        motorAudio.reproducir(cancionParaTocar);
                        SwingUtilities.invokeLater(() -> {
                            btnPlay.setText("PAUSE");
                            btnPlay.repaint();
                            lblTrackTitle.setText(cancionParaTocar.getName());
                            lblTimeTotal.setText(formatearSegundos(motorAudio.getDuracionTotalSegundos()));
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            });
        }

        btnBiblioteca.addActionListener(e -> cardsCenter.show(centerCards, "BIBLIOTECA"));

        btnAgregarAudio.addActionListener(e -> {
            archivoAudioSeleccionado[0] = null;
            archivoCaratulaSeleccionada[0] = null;
            lblAudioSel.setText("Ningún archivo de audio seleccionado");
            lblAudioSel.setForeground(TEXT_MUTED);
            lblPreviewCaratula.setIcon(null);
            lblPreviewCaratula.setText("Sin Carátula");
            txtAutorInput.setText("");
            txtAlbumInput.setText("");
            txtDescInput.setText("");
            lblConteoPalabras.setText("Palabras: 0 / 50");
            cardsCenter.show(centerCards, "AGREGAR");
        });

        btnCancelarForm.addActionListener(e -> cardsCenter.show(centerCards, "BIBLIOTECA"));

        btnGuardarForm.addActionListener(e -> {
            if (archivoAudioSeleccionado[0] == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un archivo de audio (.mp3 o .wav)", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String desc = txtDescInput.getText().trim();
            String[] pals = desc.isEmpty() ? new String[0] : desc.split("\\s+");
            if (pals.length > 50) {
                JOptionPane.showMessageDialog(this, "La descripción no puede superar las 50 palabras.", "Límite Excedido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                File audioDest = new File(dirMusica, archivoAudioSeleccionado[0].getName());
                Files.copy(archivoAudioSeleccionado[0].toPath(), audioDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String nombreArchivoCaratula = null;
                if (archivoCaratulaSeleccionada[0] != null) {
                    String extImg = obtenerExtension(archivoCaratulaSeleccionada[0].getName()).toLowerCase();
                    String nombreImg = audioDest.getName() + "_cover." + extImg;
                    File caratulaDest = new File(dirMusica, nombreImg);
                    Files.copy(archivoCaratulaSeleccionada[0].toPath(), caratulaDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    nombreArchivoCaratula = nombreImg;
                }

                String autor = txtAutorInput.getText().trim();
                String album = txtAlbumInput.getText().trim();
                mapaMetadatos.put(audioDest.getName(), new MetadataCancion(autor, album, desc, nombreArchivoCaratula));
                guardarMetadatosMusica(dirMusica, mapaMetadatos);

                filtrarMusica.run();
                cardsCenter.show(centerCards, "BIBLIOTECA");
                JOptionPane.showMessageDialog(this, "¡Canción agregada con éxito a la biblioteca!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar canción: " + ex.getMessage());
            }
        });

        btnAbrirCarpeta.addActionListener(e -> abrirExplorador(dirMusica));

        final boolean[] isSeeking = {false};

        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isSeeking[0] = true;
                actualizarScrub(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                actualizarScrub(e);
                double pct = progressBar.getValue() / 100.0;
                motorAudio.buscarPosicion(pct);
                isSeeking[0] = false;
            }

            private void actualizarScrub(MouseEvent e) {
                int mouseX = e.getX();
                int width = progressBar.getWidth();
                if (width > 0) {
                    double pct = Math.max(0.0, Math.min(1.0, (double) mouseX / width));
                    progressBar.setValue((int) (pct * 100));
                    int curSec = (int) (pct * motorAudio.getDuracionTotalSegundos());
                    lblTimeCur.setText(formatearSegundos(curSec));
                }
            }
        });

        Thread hiloProgreso = new Thread(() -> {
            while (true) {
                if (!isSeeking[0] && motorAudio.estaReproduciendo() && !motorAudio.estaPausado()) {
                    motorAudio.tickSegundo();
                    int curSec = motorAudio.getSegundosTranscurridos();
                    int totSec = motorAudio.getDuracionTotalSegundos();
                    int prog = totSec > 0 ? (int) ((curSec * 100.0) / totSec) : 0;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(Math.min(100, prog));
                        lblTimeCur.setText(formatearSegundos(curSec));
                        lblTimeTotal.setText(formatearSegundos(totSec));
                    });
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        });
        hiloProgreso.setDaemon(true);
        hiloProgreso.start();

        btnPlay.addActionListener(e -> {
            int row = tableSpotify.getSelectedRow();
            if (row == -1 && modelTablaSpotify.getRowCount() > 0) {
                tableSpotify.setRowSelectionInterval(0, 0);
                row = 0;
            }

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Agregue o seleccione un archivo de audio primero.");
                return;
            }

            File archivoFinal = (File) modelTablaSpotify.getValueAt(row, 1);

            if (motorAudio.estaReproduciendo()) {
                motorAudio.pausar();
                btnPlay.setText("PLAY");
                btnPlay.repaint();
                return;
            }

            if (motorAudio.estaPausado() && archivoFinal.equals(motorAudio.getArchivoActual())) {
                motorAudio.reanudar();
                btnPlay.setText("PAUSE");
                btnPlay.repaint();
                return;
            }

            new Thread(() -> {
                try {
                    motorAudio.reproducir(archivoFinal);
                    SwingUtilities.invokeLater(() -> {
                        btnPlay.setText("PAUSE");
                        btnPlay.repaint();
                        lblTrackTitle.setText(archivoFinal.getName());
                        lblTimeTotal.setText(formatearSegundos(motorAudio.getDuracionTotalSegundos()));
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Error al reproducir: " + ex.getMessage() + "\nAsegúrate de tener 'jl1.0.1.jar' en las librerías del proyecto.");
                    });
                }
            }).start();
        });

        btnStop.addActionListener(e -> {
            motorAudio.detener();
            btnPlay.setText("PLAY");
            btnPlay.repaint();
            progressBar.setValue(0);
            lblTimeCur.setText("00:00");
        });

        btnPrev.addActionListener(e -> {
            int total = modelTablaSpotify.getRowCount();
            if (total > 0) {
                int row = tableSpotify.getSelectedRow();
                int anterior = (row - 1 + total) % total;
                tableSpotify.setRowSelectionInterval(anterior, anterior);
                if (motorAudio.estaReproduciendo()) {
                    motorAudio.detener();
                }
                btnPlay.doClick();
            }
        });

        btnNext.addActionListener(e -> {
            int total = modelTablaSpotify.getRowCount();
            if (total > 0) {
                int row = tableSpotify.getSelectedRow();
                int siguiente = (row + 1) % total;
                tableSpotify.setRowSelectionInterval(siguiente, siguiente);
                if (motorAudio.estaReproduciendo()) {
                    motorAudio.detener();
                }
                btnPlay.doClick();
            }
        });

        return p;
    }

    private JButton crearBotonSidebarItem(String texto, boolean isSelected) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(new Color(36, 36, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(234, 88, 12));
                    g2.fillRect(0, 4, 3, getHeight() - 8);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(isSelected ? Color.WHITE : new Color(203, 213, 225));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private JButton crearBotonVectorial(int tipo) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.setColor(Color.WHITE);

                if (tipo == 1) {
                    g2.fillRect(10, 10, 2, 12);
                    int[] x = {22, 22, 13};
                    int[] y = {9, 23, 16};
                    g2.fillPolygon(x, y, 3);
                } else if (tipo == 2) {
                    int[] x = {10, 10, 19};
                    int[] y = {9, 23, 16};
                    g2.fillPolygon(x, y, 3);
                    g2.fillRect(20, 10, 2, 12);
                } else if (tipo == 3) {
                    g2.fillRect(11, 11, 10, 10);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}