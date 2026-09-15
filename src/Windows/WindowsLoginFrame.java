package Windows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Pantalla de Inicio de Sesión Oficial exclusiva para el Administrador del Sistema.
 */
public class WindowsLoginFrame extends JFrame {
    private Image backgroundImage;
    private Usuario adminUsuario;

    // Componentes Centrales
    private JComponent avatarComp;
    private JLabel lblNombreCompleto;
    private JLabel lblUsername;
    private JPasswordField txtPass;
    private JPasswordField txtPassConfirm;
    private JLabel lblError;
    private JLabel lblReloj;

    // Colores Windows 10/11
    private final Color ACCENT_PINK   = new Color(192, 38, 211);
    private final Color TEXT_WHITE    = new Color(255, 255, 255);
    private final Color TEXT_MUTED    = new Color(203, 213, 225);
    private final Color INPUT_BG      = new Color(241, 245, 249, 240);
    private final Color INPUT_TEXT    = new Color(15, 23, 42);

    public WindowsLoginFrame() {
        SistemadeArchivos.inicializarSistema();

        setTitle("Mini-Windows OS - Acceso de Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        cargarFondo();
        cargarUsuarioAdmin();

        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, new Color(15, 32, 67), getWidth(), getHeight(), new Color(2, 10, 25)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.setColor(new Color(10, 25, 47, 160));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(crearPanelLoginCentral());
        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        backgroundPanel.add(crearBarraInferiorWindows(), BorderLayout.SOUTH);
        add(backgroundPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> txtPass.requestFocusInWindow());
    }

    private void cargarFondo() {
        String[] posiblesRutas = {
            "imagenes/windows_background.jpg", "imagenes/windows_background.png",
            "src/imagenes/windows_background.jpg", "windows_background.jpg", "wallpaper.jpg"
        };
        for (String ruta : posiblesRutas) {
            File f = new File(ruta);
            if (f.exists()) {
                backgroundImage = new ImageIcon(f.getAbsolutePath()).getImage();
                return;
            }
        }
    }

    private void cargarUsuarioAdmin() {
        try {
            Lista<Usuario> lista = SistemadeArchivos.cargarUsuarios();
            Nodo<Usuario> n = lista.getHead();
            while (n != null) {
                if (n.getDato().isEsAdmin() || n.getDato().getUsername().equalsIgnoreCase("admin")) {
                    adminUsuario = n.getDato();
                    break;
                }
                n = n.getSiguiente();
            }
        } catch (Exception ignored) {}

        if (adminUsuario == null) {
            adminUsuario = new Usuario("admin", "Admin2026!", true, "Administrador del Sistema", 'M', 30, null);
        }
    }

    private JPanel crearPanelLoginCentral() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setPreferredSize(new Dimension(380, 480));

        // 1. Avatar Circular Grande (110px)
        avatarComp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 110;
                int x = (getWidth() - size) / 2;
                int y = 0;

                boolean fotoDibujada = false;
                if (adminUsuario != null && adminUsuario.getFotoPerfil() != null) {
                    File fFoto = new File(adminUsuario.getFotoPerfil());
                    if (fFoto.exists()) {
                        try {
                            Image img = new ImageIcon(fFoto.getAbsolutePath()).getImage();
                            Shape clipAnterior = g2.getClip();
                            g2.setClip(new Ellipse2D.Float(x, y, size, size));
                            g2.drawImage(img, x, y, size, size, null);
                            g2.setClip(clipAnterior);
                            fotoDibujada = true;
                        } catch (Exception ignored) {}
                    }
                }

                if (!fotoDibujada) {
                    g2.setColor(ACCENT_PINK);
                    g2.fillOval(x, y, size, size);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                    String iniciales = "AD";
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (size - fm.stringWidth(iniciales)) / 2;
                    int ty = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(iniciales, tx, ty);
                }
                g2.dispose();
            }
        };
        avatarComp.setPreferredSize(new Dimension(380, 115));
        avatarComp.setMaximumSize(new Dimension(380, 115));
        avatarComp.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Nombre Completo
        lblNombreCompleto = new JLabel(adminUsuario.getNombreCompleto(), SwingConstants.CENTER);
        lblNombreCompleto.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNombreCompleto.setForeground(TEXT_WHITE);
        lblNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Subtítulo Rol
        lblUsername = new JLabel("Cuenta de Administrador Principal", SwingConstants.CENTER);
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUsername.setForeground(TEXT_MUTED);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Etiqueta Contraseña
        JLabel lblEtiquetaPass = new JLabel("Contraseña de Administrador:", SwingConstants.LEFT);
        lblEtiquetaPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEtiquetaPass.setForeground(TEXT_WHITE);
        lblEtiquetaPass.setMaximumSize(new Dimension(280, 18));
        lblEtiquetaPass.setPreferredSize(new Dimension(280, 18));
        lblEtiquetaPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPass = new JPasswordField();
        JPanel boxPass = crearCajaInputWindows(txtPass, false);

        // 5. Etiqueta Confirmar Contraseña
        JLabel lblEtiquetaPassConf = new JLabel("Confirmar Contraseña:", SwingConstants.LEFT);
        lblEtiquetaPassConf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEtiquetaPassConf.setForeground(TEXT_WHITE);
        lblEtiquetaPassConf.setMaximumSize(new Dimension(280, 18));
        lblEtiquetaPassConf.setPreferredSize(new Dimension(280, 18));
        lblEtiquetaPassConf.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassConfirm = new JPasswordField();
        JPanel boxPassConf = crearCajaInputWindows(txtPassConfirm, true);

        // 6. Mensaje de Error
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(252, 165, 165));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(avatarComp);
        center.add(Box.createVerticalStrut(12));
        center.add(lblNombreCompleto);
        center.add(Box.createVerticalStrut(2));
        center.add(lblUsername);
        center.add(Box.createVerticalStrut(18));

        center.add(lblEtiquetaPass);
        center.add(Box.createVerticalStrut(4));
        center.add(boxPass);
        center.add(Box.createVerticalStrut(8));

        center.add(lblEtiquetaPassConf);
        center.add(Box.createVerticalStrut(4));
        center.add(boxPassConf);
        center.add(Box.createVerticalStrut(8));

        center.add(lblError);

        return center;
    }

    private JPanel crearCajaInputWindows(JPasswordField pf, boolean incluirBotonIngresar) {
        JPanel box = new JPanel(new BorderLayout(4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setMaximumSize(new Dimension(280, 36));
        box.setPreferredSize(new Dimension(280, 36));
        box.setBorder(new EmptyBorder(2, 10, 2, 4));
        box.setAlignmentX(Component.CENTER_ALIGNMENT);

        pf.setOpaque(false);
        pf.setBorder(null);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setForeground(INPUT_TEXT);
        pf.setCaretColor(INPUT_TEXT);

        JButton btnOjo = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                }
                g2.setColor(new Color(100, 116, 139));
                g2.setStroke(new BasicStroke(1.4f));

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.drawArc(cx - 7, cy - 5, 14, 10, 0, 180);
                g2.drawArc(cx - 7, cy - 5, 14, 10, 180, 180);
                g2.fillOval(cx - 2, cy - 2, 5, 5);

                if (pf.getEchoChar() != (char) 0) {
                    g2.drawLine(cx - 6, cy + 5, cx + 6, cy - 5);
                }
                g2.dispose();
            }
        };
        btnOjo.setPreferredSize(new Dimension(28, 28));
        btnOjo.setContentAreaFilled(false);
        btnOjo.setBorderPainted(false);
        btnOjo.setFocusPainted(false);
        btnOjo.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char echoDefault = pf.getEchoChar();
        btnOjo.addActionListener(e -> {
            if (pf.getEchoChar() == (char) 0) {
                pf.setEchoChar(echoDefault);
            } else {
                pf.setEchoChar((char) 0);
            }
            btnOjo.repaint();
        });

        ActionListener accion = e -> procesarLoginAdmin();
        pf.addActionListener(accion);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnOjo);

        if (incluirBotonIngresar) {
            JButton btnSubmit = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(0, 0, 0, 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    }
                    g2.setColor(new Color(71, 85, 105));
                    g2.setStroke(new BasicStroke(2.0f));

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    g2.drawLine(cx - 5, cy, cx + 5, cy);
                    g2.drawLine(cx + 1, cy - 4, cx + 5, cy);
                    g2.drawLine(cx + 1, cy + 4, cx + 5, cy);
                    g2.dispose();
                }
            };
            btnSubmit.setPreferredSize(new Dimension(30, 28));
            btnSubmit.setContentAreaFilled(false);
            btnSubmit.setBorderPainted(false);
            btnSubmit.setFocusPainted(false);
            btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSubmit.setToolTipText("Iniciar Sesión");
            btnSubmit.addActionListener(accion);
            rightActions.add(btnSubmit);
        }

        box.add(pf, BorderLayout.CENTER);
        box.add(rightActions, BorderLayout.EAST);
        return box;
    }

    private void procesarLoginAdmin() {
        String pass = new String(txtPass.getPassword());
        String passConf = new String(txtPassConfirm.getPassword());

        if (pass.isEmpty() || passConf.isEmpty()) {
            lblError.setText("Escribe y confirma la contraseña de administrador.");
            return;
        }

        if (!pass.equals(passConf)) {
            lblError.setText("Las contraseñas no coinciden.");
            txtPassConfirm.setText("");
            txtPassConfirm.requestFocus();
            return;
        }

        try {
            Usuario u = SistemadeArchivos.autenticar(adminUsuario.getUsername(), pass);
            if (u != null && u.isEsAdmin()) {
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    MiniWindowsDesktop desktop = new MiniWindowsDesktop(u);
                    desktop.setVisible(true);
                });
            } else {
                lblError.setText("Contraseña incorrecta. (Por defecto: Admin2026!)");
                txtPass.setText("");
                txtPassConfirm.setText("");
                txtPass.requestFocus();
            }
        } catch (CorruptoException ex) {
            lblError.setText("Error: Archivo de usuarios corrupto.");
        }
    }

    private JPanel crearBarraInferiorWindows() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 24, 18, 24));

        lblReloj = new JLabel();
        lblReloj.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblReloj.setForeground(TEXT_WHITE);

        Timer timer = new Timer(1000, e -> actualizarReloj());
        timer.start();
        actualizarReloj();

        JButton btnPower = new JButton("Apagar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }

                g2.setColor(TEXT_WHITE);
                g2.setStroke(new BasicStroke(1.8f));
                int cy = getHeight() / 2;
                g2.drawArc(8, cy - 6, 12, 12, 140, 260);
                g2.drawLine(14, cy - 7, 14, cy - 1);
                g2.dispose();

                super.paintComponent(g);
            }
        };
        btnPower.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPower.setForeground(TEXT_WHITE);
        btnPower.setContentAreaFilled(false);
        btnPower.setBorderPainted(false);
        btnPower.setFocusPainted(false);
        btnPower.setBorder(new EmptyBorder(4, 26, 4, 8));
        btnPower.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPower.addActionListener(e -> System.exit(0));

        bottom.add(lblReloj, BorderLayout.WEST);
        bottom.add(btnPower, BorderLayout.EAST);
        return bottom;
    }

    private void actualizarReloj() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a   |   EEEE, d 'de' MMMM");
        lblReloj.setText(sdf.format(new Date()));
    }
}