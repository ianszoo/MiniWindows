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
 * @author David Suazo Palao & Ian Suazo Palao
 * Pantalla de Inicio de Sesión Oficial Windows 10/11 con Reloj y Confirmación
 */
public class WindowsLoginFrame extends JFrame {
    private Image backgroundImage;
    private Usuario usuarioSeleccionado;
    private Lista<Usuario> listaUsuarios;

    // Componentes Centrales
    private JComponent avatarComp;
    private JLabel lblNombreCompleto;
    private JLabel lblUsername;
    private JPasswordField txtPass;
    private JPasswordField txtConfirmPass;
    private JLabel lblError;
    private JPanel panelListaUsuarios;
    private JLabel lblReloj;

    // Colores Windows 10/11
    private final Color ACCENT_PINK   = new Color(192, 38, 211); // Magenta / Rosa del avatar
    private final Color TEXT_WHITE    = new Color(255, 255, 255);
    private final Color TEXT_MUTED    = new Color(203, 213, 225);
    private final Color INPUT_BG      = new Color(241, 245, 249, 240); // Blanco suave
    private final Color INPUT_TEXT    = new Color(15, 23, 42);

    public WindowsLoginFrame() {
        SistemadeArchivos.inicializarSistema();

        setTitle("Windows - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        cargarFondo();
        cargarUsuariosSistema();

        // Panel de Fondo con capa acrílica
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

        // 1. CENTRO: Avatar, Nombre, Etiquetas y Cajas de Contraseña
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(crearPanelLoginCentral());
        backgroundPanel.add(centerWrapper, BorderLayout.CENTER);

        // 2. INFERIOR: Reloj en vivo, Cuentas y Botón Apagar
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

    private void cargarUsuariosSistema() {
        try {
            listaUsuarios = SistemadeArchivos.cargarUsuarios();
        } catch (Exception e) {
            listaUsuarios = new Lista<>();
        }

        usuarioSeleccionado = null;
        Nodo<Usuario> n = listaUsuarios.getHead();
        while (n != null) {
            if (n.getDato().getUsername().equalsIgnoreCase("admin")) {
                usuarioSeleccionado = n.getDato();
                break;
            }
            n = n.getSiguiente();
        }

        if (usuarioSeleccionado == null) {
            if (!listaUsuarios.estaVacia()) {
                usuarioSeleccionado = listaUsuarios.obtener(0);
            } else {
                usuarioSeleccionado = new Usuario("admin", "Admin2026!", true, "Administrador del sistema", 'M', 25, null);
            }
        }
    }

    // =========================================================================
    // PANEL CENTRAL (AVATAR + ETIQUETA "Contraseña" + ETIQUETA "Confirmar contraseña")
    // =========================================================================
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
                if (usuarioSeleccionado != null && usuarioSeleccionado.getFotoPerfil() != null) {
                    File fFoto = new File(usuarioSeleccionado.getFotoPerfil());
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
                    String iniciales = obtenerIniciales(usuarioSeleccionado);
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
        lblNombreCompleto = new JLabel(usuarioSeleccionado != null ? usuarioSeleccionado.getNombreCompleto() : "Administrador del sistema", SwingConstants.CENTER);
        lblNombreCompleto.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNombreCompleto.setForeground(TEXT_WHITE);
        lblNombreCompleto.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Subtítulo Username
        lblUsername = new JLabel(usuarioSeleccionado != null ? usuarioSeleccionado.getUsername() : "admin", SwingConstants.CENTER);
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUsername.setForeground(TEXT_MUTED);
        lblUsername.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. ETIQUETA: "Contraseña"
        JLabel lblEtiquetaPass = new JLabel("Contraseña:", SwingConstants.LEFT);
        lblEtiquetaPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEtiquetaPass.setForeground(TEXT_WHITE);
        lblEtiquetaPass.setMaximumSize(new Dimension(280, 18));
        lblEtiquetaPass.setPreferredSize(new Dimension(280, 18));
        lblEtiquetaPass.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPass = new JPasswordField();
        JPanel boxPass = crearCajaInputWindows(txtPass, true);

        // 5. ETIQUETA: "Confirmar contraseña"
        JLabel lblEtiquetaConfirm = new JLabel("Confirmar contraseña:", SwingConstants.LEFT);
        lblEtiquetaConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEtiquetaConfirm.setForeground(TEXT_WHITE);
        lblEtiquetaConfirm.setMaximumSize(new Dimension(280, 18));
        lblEtiquetaConfirm.setPreferredSize(new Dimension(280, 18));
        lblEtiquetaConfirm.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtConfirmPass = new JPasswordField();
        JPanel boxConfirm = crearCajaInputWindows(txtConfirmPass, false);

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

        // Añadir Etiqueta 1 + Caja 1
        center.add(lblEtiquetaPass);
        center.add(Box.createVerticalStrut(4));
        center.add(boxPass);
        center.add(Box.createVerticalStrut(10));

        // Añadir Etiqueta 2 + Caja 2
        center.add(lblEtiquetaConfirm);
        center.add(Box.createVerticalStrut(4));
        center.add(boxConfirm);
        center.add(Box.createVerticalStrut(8));
        center.add(lblError);

        return center;
    }

    private JPanel crearCajaInputWindows(JPasswordField pf, boolean incluirBotonEntrar) {
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

        // Botón Ojo Vectorial
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
        btnOjo.setToolTipText("Mostrar/Ocultar contraseña");

        char echoDefault = pf.getEchoChar();
        btnOjo.addActionListener(e -> {
            if (pf.getEchoChar() == (char) 0) {
                pf.setEchoChar(echoDefault);
            } else {
                pf.setEchoChar((char) 0);
            }
            btnOjo.repaint();
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnOjo);

        if (incluirBotonEntrar) {
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

            ActionListener accion = e -> procesarLogin();
            btnSubmit.addActionListener(accion);
            pf.addActionListener(accion);

            rightActions.add(btnSubmit);
        } else {
            pf.addActionListener(e -> procesarLogin());
        }

        box.add(pf, BorderLayout.CENTER);
        box.add(rightActions, BorderLayout.EAST);
        return box;
    }

    private void procesarLogin() {
        String pass = new String(txtPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());

        if (pass.isEmpty()) {
            lblError.setText("Escribe tu contraseña.");
            return;
        }

        if (!confirm.isEmpty() && !pass.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden.");
            txtConfirmPass.setText("");
            txtConfirmPass.requestFocus();
            return;
        }

        try {
            Usuario u = SistemadeArchivos.autenticar(usuarioSeleccionado.getUsername(), pass);
            if (u != null) {
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    MiniWindowsDesktop desktop = new MiniWindowsDesktop(u);
                    desktop.setVisible(true);
                });
            } else {
                lblError.setText("La contraseña es incorrecta. (Prueba con: Admin2026!)");
                txtPass.setText("");
                txtConfirmPass.setText("");
                txtPass.requestFocus();
            }
        } catch (CorruptoException ex) {
            lblError.setText("Error: Archivo de usuarios corrupto.");
        }
    }

    private String obtenerIniciales(Usuario u) {
        if (u == null) return "AD";
        String nom = u.getNombreCompleto();
        if (nom == null || nom.trim().isEmpty()) {
            return u.getUsername().substring(0, Math.min(2, u.getUsername().length())).toUpperCase();
        }
        String[] partes = nom.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return ("" + nom.charAt(0) + (nom.length() > 1 ? nom.charAt(1) : "")).toUpperCase();
    }

    // =========================================================================
    // BARRA INFERIOR (RELOJ + USUARIOS + BOTÓN APAGAR SIN CARACTERES ROTOS)
    // =========================================================================
    private JPanel crearBarraInferiorWindows() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 24, 18, 24));

        panelListaUsuarios = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelListaUsuarios.setOpaque(false);

        Nodo<Usuario> n = listaUsuarios.getHead();
        while (n != null) {
            Usuario u = n.getDato();
            JButton btnUser = new JButton(u.getNombreCompleto()) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (getModel().isRollover()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(new Color(255, 255, 255, 25));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            btnUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnUser.setForeground(TEXT_MUTED);
            btnUser.setContentAreaFilled(false);
            btnUser.setBorderPainted(false);
            btnUser.setFocusPainted(false);
            btnUser.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnUser.addActionListener(e -> {
                usuarioSeleccionado = u;
                lblNombreCompleto.setText(u.getNombreCompleto());
                lblUsername.setText(u.getUsername());
                lblError.setText(" ");
                txtPass.setText("");
                txtConfirmPass.setText("");
                avatarComp.repaint();
                txtPass.requestFocus();
            });

            panelListaUsuarios.add(btnUser);
            n = n.getSiguiente();
        }

        lblReloj = new JLabel();
        lblReloj.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblReloj.setForeground(TEXT_WHITE);
        lblReloj.setHorizontalAlignment(SwingConstants.CENTER);

        Timer timer = new Timer(1000, e -> actualizarReloj());
        timer.start();
        actualizarReloj();

        // Botón Apagar con icono vectorial (sin emojis rotos)
        JButton btnPower = new JButton("Apagar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }

                // Dibuja el símbolo de encendido/apagado en blanco
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
        btnPower.setBorder(new EmptyBorder(4, 26, 4, 8)); // Espacio para el icono dibujado
        btnPower.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPower.addActionListener(e -> System.exit(0));

        JPanel leftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftWrapper.setOpaque(false);
        leftWrapper.add(lblReloj);
        leftWrapper.add(panelListaUsuarios);

        bottom.add(leftWrapper, BorderLayout.WEST);
        bottom.add(btnPower, BorderLayout.EAST);
        return bottom;
    }

    private void actualizarReloj() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a   |   EEEE, d 'de' MMMM");
        lblReloj.setText(sdf.format(new Date()));
    }
}