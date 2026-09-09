/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Insta;

import Windows.Lista;
import Windows.Nodo;
import Windows.Usuario;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * @author David Suazo Palao & Ian Suazo Palao
 */
public class InstaPanel extends JPanel {
    private Usuario usuarioActual;
    private CardLayout rootCardLayout;
    private JPanel rootContainer;

    private CardLayout authCardLayout;
    private JPanel authContainer;

    private CardLayout appCardLayout;
    private JPanel appMainContent;

    // Paleta de colores Instagram Dark Moderno
    public static final Color BG_MAIN          = new Color(8, 10, 16);
    public static final Color BG_SIDEBAR       = new Color(11, 15, 25);
    public static final Color BG_CARD          = new Color(16, 21, 34);
    public static final Color BG_INPUT         = new Color(22, 31, 48);
    public static final Color BG_HOVER         = new Color(27, 37, 56);
    public static final Color BORDER_COLOR     = new Color(31, 43, 66);
    public static final Color TEXT_WHITE       = new Color(248, 250, 252);
    public static final Color TEXT_MUTED       = new Color(148, 163, 184);
    public static final Color IG_BLUE          = new Color(0, 149, 246);
    public static final Color IG_RED_HEART     = new Color(255, 48, 64);
    public static final Color ONLINE_GREEN     = new Color(34, 197, 94);

    // Degradados
    public static final Color G_ORANGE  = new Color(245, 133, 41);
    public static final Color G_PINK    = new Color(221, 42, 123);
    public static final Color G_PURPLE  = new Color(129, 52, 175);
    public static final Color BTN_BLUE_1 = new Color(56, 88, 246);
    public static final Color BTN_BLUE_2 = new Color(139, 63, 251);

    // Chat / Inbox
    private JPanel pnlChatStream;
    private JScrollPane scrollChatStream;
    private String chatUsuarioSeleccionado = "noticias";
    private JPanel pnlListaConversaciones;
    private volatile boolean hiloChatActivo = true;
    private String usuarioPerfilVisitado = null;
    private JTextField txtBuscarChats;

    private final String[] activeCard = {"TIMELINE"};

    public InstaPanel(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        this.usuarioPerfilVisitado = this.usuarioActual.getUsername();

        InstaFileManager.inicializarInsta();
        InstaFileManager.crearEspacioUsuarioInsta(usuarioActual.getUsername());

        setLayout(new BorderLayout());
        setBackground(BG_MAIN);

        rootCardLayout = new CardLayout();
        rootContainer = new JPanel(rootCardLayout);
        rootContainer.setBackground(BG_MAIN);

        // Vista 1: Login / Registro (Sin sidebar)
        rootContainer.add(crearVistaAutenticacion(), "AUTH");

        // Vista 2: Aplicación Principal (Sidebar con iconos + Pantallas)
        rootContainer.add(crearVistaAppPrincipal(), "APP");

        add(rootContainer, BorderLayout.CENTER);

        rootCardLayout.show(rootContainer, "AUTH");
        iniciarHiloSincronizacionChat();
    }

    // =========================================================================
    // CARGADOR DE ICONOS PERSONALIZADOS (ImagenesInsta)
    // =========================================================================
    public static ImageIcon cargarIconoInsta(String nombreBase, int ancho, int alto) {
        String[] extensiones = {".png", ".jpg", ".jpeg", ""};
        String[] rutas = {
            "/Insta/ImagenesInsta/" + nombreBase,
            "src/Insta/ImagenesInsta/" + nombreBase,
            "Insta/ImagenesInsta/" + nombreBase,
            "/ImagenesInsta/" + nombreBase,
            "src/ImagenesInsta/" + nombreBase,
            "ImagenesInsta/" + nombreBase
        };

        for (String r : rutas) {
            for (String ext : extensiones) {
                String fullPath = r + ext;
                if (fullPath.startsWith("/")) {
                    java.net.URL url = InstaPanel.class.getResource(fullPath);
                    if (url != null) {
                        Image img = new ImageIcon(url).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                } else {
                    File f = new File(fullPath);
                    if (f.exists()) {
                        Image img = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    }
                }
            }
        }
        return null;
    }

    // =========================================================================
    // UTILIDADES DE ESTILIZADO DARK MODE
    // =========================================================================
    private void estilizarCampoTexto(JTextField tf, String placeholder) {
        tf.setMaximumSize(new Dimension(340, 38));
        tf.setPreferredSize(new Dimension(340, 38));
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void estilizarSpinner(JSpinner spin) {
        spin.setBackground(BG_INPUT);
        spin.setForeground(TEXT_WHITE);
        spin.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        JComponent editor = spin.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_WHITE);
            tf.setCaretColor(Color.WHITE);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setBorder(new EmptyBorder(4, 8, 4, 8));
        }
    }

    private <T> void estilizarComboBox(JComboBox<T> cb) {
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_WHITE);
        cb.setFocusable(false);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? BG_HOVER : BG_INPUT);
                lbl.setForeground(TEXT_WHITE);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
    }

    private JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_HOVER : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonGradiente(String texto, int w, int h) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BTN_BLUE_1, getWidth(), getHeight(), BTN_BLUE_2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setMaximumSize(new Dimension(w, h));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    // =========================================================================
    // PANTALLA DE INICIO (LOGIN / REGISTRO)
    // =========================================================================
    private JPanel crearVistaAutenticacion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        authCardLayout = new CardLayout();
        authContainer = new JPanel(authCardLayout);
        authContainer.setOpaque(false);
        authContainer.setPreferredSize(new Dimension(410, 570));

        authContainer.add(crearCardLoginInsta(), "LOGIN");
        authContainer.add(crearCardRegistroInsta(), "REGISTRO");

        panel.add(authContainer);
        return panel;
    }

    private JPanel crearCardLoginInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(35, 35, 30, 35)
        ));

        JLabel lblLogo = new JLabel("INSTA+", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, G_ORANGE, getWidth(), 0, G_PURPLE);
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                g2.drawString(getText(), x, fm.getAscent());
                g2.dispose();
            }
        };
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 34));
        lblLogo.setPreferredSize(new Dimension(340, 44));
        lblLogo.setMaximumSize(new Dimension(340, 44));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Inicia sesión en tu cuenta", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtUser = new JTextField(usuarioActual.getUsername());
        estilizarCampoTexto(txtUser, "Usuario o username");

        JPasswordField txtPass = new JPasswordField("Admin2026!");
        estilizarCampoTexto(txtPass, "Contraseña");

        JButton btnLogin = crearBotonGradiente("Iniciar Sesión", 340, 38);
        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());

            Usuario userAuth = InstaFileManager.autenticarInsta(u, p);
            if (userAuth != null) {
                usuarioActual = userAuth;
                usuarioPerfilVisitado = userAuth.getUsername();
                rootCardLayout.show(rootContainer, "APP");
                recargarTimeline();
            } else {
                int resp = JOptionPane.showOptionDialog(this,
                        "Usuario o contraseña incorrectos.\n¿Deseas intentar de nuevo o registrarte?",
                        "Error de Autenticación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        new String[]{"Reintentar Login", "Crear Cuenta Nueva"},
                        "Reintentar Login");

                if (resp == JOptionPane.NO_OPTION) {
                    authCardLayout.show(authContainer, "REGISTRO");
                }
            }
        });

        JButton btnIrRegistro = new JButton("¿No tienes una cuenta? Regístrate");
        btnIrRegistro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnIrRegistro.setForeground(IG_BLUE);
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setBorderPainted(false);
        btnIrRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIrRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIrRegistro.addActionListener(e -> authCardLayout.show(authContainer, "REGISTRO"));

        card.add(lblLogo);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));
        card.add(txtUser);
        card.add(Box.createVerticalStrut(12));
        card.add(txtPass);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(btnIrRegistro);

        return card;
    }

    private JPanel crearCardRegistroInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(25, 35, 25, 35)
        ));

        JLabel lblTit = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtNombre = new JTextField();
        estilizarCampoTexto(txtNombre, "Nombre completo");

        JTextField txtUser = new JTextField();
        estilizarCampoTexto(txtUser, "Username único");

        JPasswordField txtPass = new JPasswordField();
        estilizarCampoTexto(txtPass, "Contraseña");

        JPanel rowGenEdad = new JPanel(new GridLayout(1, 2, 10, 0));
        rowGenEdad.setOpaque(false);
        rowGenEdad.setMaximumSize(new Dimension(340, 36));
        rowGenEdad.setPreferredSize(new Dimension(340, 36));

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(20, 13, 100, 1));
        estilizarSpinner(spinEdad);

        JComboBox<String> cbGen = new JComboBox<>(new String[]{"Género: M", "Género: F"});
        estilizarComboBox(cbGen);

        rowGenEdad.add(spinEdad);
        rowGenEdad.add(cbGen);

        final String[] rutaFoto = {null};
        JButton btnFoto = crearBotonSecundario("📷 Seleccionar Foto de Perfil");
        btnFoto.setMaximumSize(new Dimension(340, 34));
        btnFoto.setPreferredSize(new Dimension(340, 34));
        btnFoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto[0] = fc.getSelectedFile().getAbsolutePath();
                btnFoto.setText("✅ " + fc.getSelectedFile().getName());
            }
        });

        JButton btnRegistrar = crearBotonGradiente("Crear Cuenta (users.ins)", 340, 38);
        btnRegistrar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String usr = txtUser.getText().trim().toLowerCase();
            String pas = new String(txtPass.getPassword());
            char gen = cbGen.getSelectedIndex() == 0 ? 'M' : 'F';
            int edad = (Integer) spinEdad.getValue();

            if (nom.isEmpty() || usr.isEmpty() || pas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor completa todos los campos requeridos.");
                return;
            }

            Usuario nuevo = new Usuario(usr, pas, false, nom, gen, edad, rutaFoto[0]);
            boolean exito = InstaFileManager.registrarUsuarioInsta(nuevo);
            if (exito) {
                JOptionPane.showMessageDialog(this, "¡Cuenta @" + usr + " creada con éxito!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                usuarioActual = nuevo;
                usuarioPerfilVisitado = nuevo.getUsername();
                rootCardLayout.show(rootContainer, "APP");
                recargarTimeline();
            } else {
                JOptionPane.showMessageDialog(this, "El username @" + usr + " ya existe. Elige otro.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnVolver = new JButton("← Volver al inicio de sesión");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(TEXT_MUTED);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> authCardLayout.show(authContainer, "LOGIN"));

        card.add(lblTit);
        card.add(Box.createVerticalStrut(14));
        card.add(txtNombre);
        card.add(Box.createVerticalStrut(8));
        card.add(txtUser);
        card.add(Box.createVerticalStrut(8));
        card.add(txtPass);
        card.add(Box.createVerticalStrut(8));
        card.add(rowGenEdad);
        card.add(Box.createVerticalStrut(8));
        card.add(btnFoto);
        card.add(Box.createVerticalStrut(14));
        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(10));
        card.add(btnVolver);

        return card;
    }

    // =========================================================================
    // APLICACIÓN PRINCIPAL (SIDEBAR CON ICONOS)
    // =========================================================================
    private JPanel crearVistaAppPrincipal() {
        JPanel appPanel = new JPanel(new BorderLayout());
        appPanel.setBackground(BG_MAIN);

        JPanel sidebar = crearSidebarIzquierda();
        appPanel.add(sidebar, BorderLayout.WEST);

        appCardLayout = new CardLayout();
        appMainContent = new JPanel(appCardLayout);
        appMainContent.setBackground(BG_MAIN);

        appMainContent.add(crearVistaTimeline(), "TIMELINE");
        appMainContent.add(crearVistaBuscarGeneral(), "SEARCH");
        appMainContent.add(crearVistaUpload(), "UPLOAD");
        appMainContent.add(crearVistaInbox(), "INBOX");
        appMainContent.add(crearVistaMenciones(), "NOTIFICACIONES");
        appMainContent.add(crearVistaPerfil(), "PERFIL");
        appMainContent.add(crearVistaEditarPerfil(), "EDIT_PROFILE");

        appPanel.add(appMainContent, BorderLayout.CENTER);
        return appPanel;
    }

    private JPanel crearSidebarIzquierda() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(225, getHeight()));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 22));
        logoPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("INSTA+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, G_ORANGE, getWidth(), 0, G_PURPLE);
                g2.setPaint(gp);
                g2.setFont(getFont());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblLogo.setPreferredSize(new Dimension(140, 28));

        logoPanel.add(lblLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menú con tus iconos personalizados de ImagenesInsta
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(8, 12, 10, 12));

        menuPanel.add(crearItemMenuSidebar("Inicio", "TIMELINE", "icono_inicio_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Buscar", "SEARCH", "icono_buscar_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Crear", "UPLOAD", "icono_crear_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Mensajes", "INBOX", "icono_mensajes_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Notificaciones", "NOTIFICACIONES", "icono_notificaciones_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Perfil", "PERFIL", "icono_perfil_insta"));
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(crearItemMenuSidebar("Configuración", "EDIT_PROFILE", "icono_ajustes_insta"));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Sección Inferior: Usuario Logueado + Cerrar Sesión
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 12, 16, 12));

        JPanel userBadge = new JPanel(new BorderLayout(10, 0));
        userBadge.setOpaque(false);
        userBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userBadge.setBorder(new EmptyBorder(8, 8, 8, 8));

        userBadge.add(crearAvatarCircular(usuarioActual.getUsername(), 36, false, null), BorderLayout.WEST);

        JPanel userText = new JPanel(new GridLayout(2, 1, 0, 2));
        userText.setOpaque(false);
        JLabel lblUser = new JLabel("@" + usuarioActual.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(TEXT_WHITE);

        String rol = usuarioActual.isEsAdmin() ? "Administrador" : "En línea";
        JLabel lblRol = new JLabel(rol);
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRol.setForeground(TEXT_MUTED);

        userText.add(lblUser);
        userText.add(lblRol);
        userBadge.add(userText, BorderLayout.CENTER);

        userBadge.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activeCard[0] = "PERFIL";
                usuarioPerfilVisitado = usuarioActual.getUsername();
                appCardLayout.show(appMainContent, "PERFIL");
                recargarPerfil();
                sidebar.repaint();
            }
        });

        JButton btnCerrarSesion = new JButton("🚪  Cerrar sesión");
        btnCerrarSesion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCerrarSesion.setForeground(new Color(248, 113, 113));
        btnCerrarSesion.setContentAreaFilled(false);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCerrarSesion.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión de @" + usuarioActual.getUsername() + "?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                rootCardLayout.show(rootContainer, "AUTH");
                authCardLayout.show(authContainer, "LOGIN");
            }
        });

        bottomPanel.add(userBadge);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(btnCerrarSesion);

        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton crearItemMenuSidebar(String texto, String cardName, String nombreIcono) {
        ImageIcon iconImg = cargarIconoInsta(nombreIcono, 22, 22);

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = activeCard[0].equals(cardName);

                if (isActive) {
                    g2.setColor(BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(IG_BLUE);
                    g2.fillRoundRect(0, 8, 4, getHeight() - 16, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 10));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                int xText = 16;
                if (iconImg != null) {
                    g2.drawImage(iconImg.getImage(), 14, (getHeight() - 22) / 2, null);
                    xText = 46;
                }

                g2.setColor(isActive ? TEXT_WHITE : TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
                g2.drawString(texto, xText, (getHeight() + g2.getFontMetrics().getAscent() - 6) / 2);
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(200, 42));
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            activeCard[0] = cardName;
            if (cardName.equals("PERFIL")) usuarioPerfilVisitado = usuarioActual.getUsername();
            appCardLayout.show(appMainContent, cardName);
            if (cardName.equals("TIMELINE")) recargarTimeline();
            if (cardName.equals("PERFIL")) recargarPerfil();
            if (cardName.equals("INBOX")) recargarChat();
            repaint();
        });
        return btn;
    }

    // =========================================================================
    // 1. TIMELINE & HISTORIAS
    // =========================================================================
    private JPanel pnlStoriesBar;
    private JPanel pnlFeedCards;

    private JPanel crearVistaTimeline() {
        JPanel centerFeed = new JPanel(new BorderLayout());
        centerFeed.setBackground(BG_MAIN);

        pnlStoriesBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        pnlStoriesBar.setBackground(BG_MAIN);
        pnlStoriesBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JScrollPane scrollStories = new JScrollPane(pnlStoriesBar);
        scrollStories.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStories.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollStories.setBorder(null);
        scrollStories.setPreferredSize(new Dimension(getWidth(), 115));
        scrollStories.getHorizontalScrollBar().setUnitIncrement(14);
        centerFeed.add(scrollStories, BorderLayout.NORTH);

        pnlFeedCards = new JPanel();
        pnlFeedCards.setLayout(new BoxLayout(pnlFeedCards, BoxLayout.Y_AXIS));
        pnlFeedCards.setBackground(BG_MAIN);
        pnlFeedCards.setBorder(new EmptyBorder(20, 0, 40, 0));

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setBackground(BG_MAIN);
        wrapper.add(pnlFeedCards);

        JScrollPane scrollFeed = new JScrollPane(wrapper);
        scrollFeed.setBorder(null);
        scrollFeed.getVerticalScrollBar().setUnitIncrement(18);
        centerFeed.add(scrollFeed, BorderLayout.CENTER);

        recargarTimeline();
        return centerFeed;
    }

    private void recargarTimeline() {
        if (pnlStoriesBar == null || pnlFeedCards == null) return;
        pnlStoriesBar.removeAll();
        pnlFeedCards.removeAll();

        pnlStoriesBar.add(crearBurbujaStory("Tu historia", usuarioActual.getUsername(), true));

        Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
        Lista<String> autores = new Lista<>();
        autores.agregar(usuarioActual.getUsername());

        Nodo<String> ns = seguidos.getHead();
        while (ns != null) {
            autores.agregar(ns.getDato());
            pnlStoriesBar.add(crearBurbujaStory(ns.getDato(), ns.getDato(), false));
            ns = ns.getSiguiente();
        }

        String[] demos = {"noticias", "deportes", "entretenimiento"};
        for (String demo : demos) {
            if (!demo.equalsIgnoreCase(usuarioActual.getUsername()) && !seguidos.contiene(demo)) {
                pnlStoriesBar.add(crearBurbujaStory(demo, demo, false));
                autores.agregar(demo);
            }
        }

        Lista<Publicacion> todosPosts = new Lista<>();
        Nodo<String> na = autores.getHead();
        while (na != null) {
            String autor = na.getDato();
            Usuario uAutor = InstaFileManager.buscarUsuario(autor);
            if (uAutor != null && uAutor.isActivo()) {
                Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(autor);
                Nodo<Publicacion> np = posts.getHead();
                while (np != null) {
                    if (!np.getDato().isEsHistoria()) {
                        todosPosts.agregar(np.getDato());
                    }
                    np = np.getSiguiente();
                }
            }
            na = na.getSiguiente();
        }

        for (int i = todosPosts.getSize() - 1; i >= 0; i--) {
            pnlFeedCards.add(crearTarjetaPostEspecificacion(todosPosts.obtener(i)));
            pnlFeedCards.add(Box.createVerticalStrut(24));
        }

        if (todosPosts.estaVacia()) {
            JLabel lblVacio = new JLabel("No hay publicaciones en tu feed. ¡Sigue a otros usuarios!", SwingConstants.CENTER);
            lblVacio.setForeground(TEXT_MUTED);
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblVacio.setBorder(new EmptyBorder(50, 20, 20, 20));
            pnlFeedCards.add(lblVacio);
        }

        pnlStoriesBar.revalidate();
        pnlStoriesBar.repaint();
        pnlFeedCards.revalidate();
        pnlFeedCards.repaint();
    }

    private JPanel crearBurbujaStory(String label, String username, boolean isMyStory) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.add(crearAvatarCircular(username, 60, !isMyStory, isMyStory ? "+" : null), BorderLayout.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isMyStory) {
                    activeCard[0] = "UPLOAD";
                    appCardLayout.show(appMainContent, "UPLOAD");
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(InstaPanel.this, "Viendo historia de @" + username + " 📖", "Instagram Story", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        return p;
    }

    private JPanel crearTarjetaPostEspecificacion(Publicacion p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 16, 16)
        ));
        card.setPreferredSize(new Dimension(500, p.getRutaImagen() != null ? 530 : 260));

        // Cabecera del post
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.add(crearAvatarCircular(p.getAutor(), 40, true, null), BorderLayout.WEST);

        JPanel postInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        postInfo.setOpaque(false);

        JLabel lblAutor = new JLabel(p.getAutor());
        lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAutor.setForeground(TEXT_WHITE);

        String ubicacion = p.getAutor().equalsIgnoreCase("noticias") ? "📍 San Pedro Sula" : "📍 Tegucigalpa";
        JLabel lblUbic = new JLabel(ubicacion);
        lblUbic.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUbic.setForeground(TEXT_MUTED);

        postInfo.add(lblAutor);
        postInfo.add(lblUbic);
        header.add(postInfo, BorderLayout.CENTER);

        String fechaStr = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(p.getFecha());
        JLabel lblFecha = new JLabel(fechaStr);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFecha.setForeground(TEXT_MUTED);
        header.add(lblFecha, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Imagen principal del post (si existe)
        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image scaled = icon.getImage().getScaledInstance(468, 270, Image.SCALE_SMOOTH);
            JLabel lblImg = new JLabel(new ImageIcon(scaled));
            lblImg.setBorder(new EmptyBorder(10, 0, 10, 0));
            card.add(lblImg, BorderLayout.CENTER);
        }

        // Pie de publicación (Interacciones, Texto, Hashtags y Sticker)
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        actionRow.setOpaque(false);

        JButton btnLike = new JButton("🤍");
        btnLike.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnLike.setForeground(Color.WHITE);
        btnLike.setContentAreaFilled(false);
        btnLike.setBorderPainted(false);
        btnLike.setFocusPainted(false);
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final boolean[] liked = {false};
        btnLike.addActionListener(e -> {
            liked[0] = !liked[0];
            btnLike.setText(liked[0] ? "❤️" : "🤍");
            btnLike.setForeground(liked[0] ? IG_RED_HEART : Color.WHITE);
        });

        JButton btnComment = new JButton("💬");
        btnComment.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnComment.setContentAreaFilled(false);
        btnComment.setBorderPainted(false);
        btnComment.setFocusPainted(false);
        btnComment.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnShare = new JButton("↗️");
        btnShare.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnShare.setContentAreaFilled(false);
        btnShare.setBorderPainted(false);
        btnShare.setFocusPainted(false);
        btnShare.setCursor(new Cursor(Cursor.HAND_CURSOR));

        actionRow.add(btnLike);
        actionRow.add(btnComment);
        actionRow.add(btnShare);
        footer.add(actionRow);

        JLabel lblLikes = new JLabel("  ❤️ Le gusta a varias personas");
        lblLikes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLikes.setForeground(TEXT_WHITE);
        footer.add(lblLikes);
        footer.add(Box.createVerticalStrut(4));

        String texto = p.getContenido().replaceAll("(#[\\w]+)", "<span style='color:#0095f6;'>$1</span>");
        texto = texto.replaceAll("(@[\\w]+)", "<span style='color:#ffffff; font-weight:bold;'>$1</span>");

        JLabel lblContenido = new JLabel("<html><body style='width:450px; color:#ffffff; font-size:12px; font-family:Segoe UI;'>"
                + "<b>@" + p.getAutor() + "</b> " + texto + "</body></html>");
        lblContenido.setBorder(new EmptyBorder(2, 6, 2, 6));
        footer.add(lblContenido);

        // Renderizado del Sticker adjunto (PNG de sticker pack o texto)
        if (p.getSticker() != null && !p.getSticker().isEmpty()) {
            File fSt = new File(p.getSticker());
            if (fSt.exists()) {
                ImageIcon iconSt = new ImageIcon(fSt.getAbsolutePath());
                Image imgSt = iconSt.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                JLabel lblStImg = new JLabel("  Sticker: ", new ImageIcon(imgSt), SwingConstants.LEFT);
                lblStImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblStImg.setForeground(G_PINK);
                footer.add(Box.createVerticalStrut(6));
                footer.add(lblStImg);
            } else {
                JLabel lblSt = new JLabel("  🌟 Sticker: " + p.getSticker());
                lblSt.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lblSt.setForeground(G_PINK);
                footer.add(Box.createVerticalStrut(4));
                footer.add(lblSt);
            }
        }

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. BUSCADOR GENERAL (USUARIOS & HASHTAGS)
    // =========================================================================
    private JPanel crearVistaBuscarGeneral() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 35, 25, 35));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JTextField txtSearch = new JTextField();
        estilizarCampoTexto(txtSearch, "");
        txtSearch.setPreferredSize(new Dimension(380, 40));

        JButton btnBuscar = crearBotonGradiente("🔍 Buscar", 110, 40);
        top.add(txtSearch, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bot.setOpaque(false);
        JButton btnVer = crearBotonGradiente("Ver Perfil Seleccionado", 220, 38);
        bot.add(btnVer);
        p.add(bot, BorderLayout.SOUTH);

        Runnable doSearch = () -> {
            model.clear();
            String q = txtSearch.getText().trim().toLowerCase();

            Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
            Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
            Nodo<Usuario> n = users.getHead();
            while (n != null) {
                Usuario u = n.getDato();
                if (u.isActivo() && (q.isEmpty() || u.getUsername().toLowerCase().contains(q) || u.getNombreCompleto().toLowerCase().contains(q))) {
                    boolean sigue = seguidos.contiene(u.getUsername().toLowerCase());
                    model.addElement("👤 @" + u.getUsername() + " — " + u.getNombreCompleto() + " (" + (sigue ? "Siguiendo" : "No sigues") + ")");
                }
                n = n.getSiguiente();
            }

            if (!q.isEmpty() && q.startsWith("#")) {
                Nodo<Usuario> nu = users.getHead();
                while (nu != null) {
                    Usuario u = nu.getDato();
                    if (u.isActivo()) {
                        Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                        Nodo<Publicacion> np = posts.getHead();
                        while (np != null) {
                            if (np.getDato().getHashtags().contiene(q)) {
                                model.addElement("🏷️ " + q + " por @" + np.getDato().getAutor() + ": " + np.getDato().getContenido());
                            }
                            np = np.getSiguiente();
                        }
                    }
                    nu = nu.getSiguiente();
                }
            }
        };

        btnBuscar.addActionListener(e -> doSearch.run());
        doSearch.run();

        btnVer.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null && sel.contains("@")) {
                String target = sel.substring(sel.indexOf("@") + 1).split(" ")[0].trim();
                usuarioPerfilVisitado = target;
                activeCard[0] = "PERFIL";
                appCardLayout.show(appMainContent, "PERFIL");
                recargarPerfil();
            }
        });

        return p;
    }

    // =========================================================================
    // 3. INBOX / DIRECT CONVERSIONES (CON IMÁGENES DE STICKERS)
    // =========================================================================
    private JPanel crearVistaInbox() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        // Panel Izquierdo: Lista de Contactos
        JPanel leftList = new JPanel(new BorderLayout());
        leftList.setBackground(BG_SIDEBAR);
        leftList.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        leftList.setPreferredSize(new Dimension(300, getHeight()));

        JPanel headLeft = new JPanel(new BorderLayout(0, 10));
        headLeft.setOpaque(false);
        headLeft.setBorder(new EmptyBorder(16, 16, 10, 16));

        JLabel lblTit = new JLabel("Mensajes");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(TEXT_WHITE);

        txtBuscarChats = new JTextField();
        estilizarCampoTexto(txtBuscarChats, "");
        txtBuscarChats.setPreferredSize(new Dimension(260, 32));

        headLeft.add(lblTit, BorderLayout.NORTH);
        headLeft.add(txtBuscarChats, BorderLayout.SOUTH);
        leftList.add(headLeft, BorderLayout.NORTH);

        pnlListaConversaciones = new JPanel();
        pnlListaConversaciones.setLayout(new BoxLayout(pnlListaConversaciones, BoxLayout.Y_AXIS));
        pnlListaConversaciones.setBackground(BG_SIDEBAR);

        JScrollPane scrollConv = new JScrollPane(pnlListaConversaciones);
        scrollConv.setBorder(null);
        leftList.add(scrollConv, BorderLayout.CENTER);
        root.add(leftList, BorderLayout.WEST);

        // Panel Derecho: Chat Stream
        JPanel rightChat = new JPanel(new BorderLayout());
        rightChat.setBackground(BG_MAIN);

        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(BG_SIDEBAR);
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        chatHeader.setPreferredSize(new Dimension(getWidth(), 60));
        chatHeader.setBorder(new EmptyBorder(10, 18, 10, 18));

        JPanel chatUserBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        chatUserBadge.setOpaque(false);

        JLabel lblHeaderAvatar = new JLabel();
        JLabel lblHeaderName = new JLabel("@" + chatUsuarioSeleccionado);
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderName.setForeground(TEXT_WHITE);

        JLabel lblHeaderStatus = new JLabel(" • 🟢 En línea");
        lblHeaderStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHeaderStatus.setForeground(ONLINE_GREEN);

        chatUserBadge.add(lblHeaderAvatar);
        chatUserBadge.add(lblHeaderName);
        chatUserBadge.add(lblHeaderStatus);
        chatHeader.add(chatUserBadge, BorderLayout.WEST);

        JButton btnEliminarChat = crearBotonSecundario("🗑 Eliminar Chat");
        btnEliminarChat.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar todos los mensajes con @" + chatUsuarioSeleccionado + "?", "Eliminar Chat", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                InstaFileManager.eliminarConversacionCompleta(usuarioActual.getUsername(), chatUsuarioSeleccionado);
                recargarChat();
            }
        });
        chatHeader.add(btnEliminarChat, BorderLayout.EAST);
        rightChat.add(chatHeader, BorderLayout.NORTH);

        pnlChatStream = new JPanel();
        pnlChatStream.setLayout(new BoxLayout(pnlChatStream, BoxLayout.Y_AXIS));
        pnlChatStream.setBackground(BG_MAIN);
        pnlChatStream.setBorder(new EmptyBorder(20, 24, 20, 24));

        scrollChatStream = new JScrollPane(pnlChatStream);
        scrollChatStream.setBorder(null);
        rightChat.add(scrollChatStream, BorderLayout.CENTER);

        JPanel inputBottom = new JPanel(new BorderLayout(10, 0));
        inputBottom.setBackground(BG_SIDEBAR);
        inputBottom.setBorder(new EmptyBorder(12, 18, 14, 18));

        JTextField txtInput = new JTextField();
        estilizarCampoTexto(txtInput, "");

        JPanel btnActs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnActs.setOpaque(false);

        JButton btnSticker = crearBotonSecundario("😊 Stickers");
        JButton btnHeart = crearBotonSecundario("❤️");
        JButton btnSend = crearBotonGradiente("➤ Enviar", 90, 36);

        btnActs.add(btnSticker);
        btnActs.add(btnHeart);
        btnActs.add(btnSend);

        inputBottom.add(txtInput, BorderLayout.CENTER);
        inputBottom.add(btnActs, BorderLayout.EAST);
        rightChat.add(inputBottom, BorderLayout.SOUTH);

        root.add(rightChat, BorderLayout.CENTER);

        ActionListener enviarMsg = e -> {
            String txt = txtInput.getText().trim();
            if (!txt.isEmpty()) {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, txt, MensajeInbox.Tipo.TEXTO);
                txtInput.setText("");
                recargarChat();
            }
        };
        btnSend.addActionListener(enviarMsg);
        txtInput.addActionListener(enviarMsg);

        btnHeart.addActionListener(e -> {
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, "❤️", MensajeInbox.Tipo.TEXTO);
            recargarChat();
        });

        // Selector visual en cuadrícula de Stickers
        btnSticker.addActionListener(e -> {
            mostrarSelectorStickers(stickerSeleccionado -> {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, stickerSeleccionado, MensajeInbox.Tipo.STICKER);
                recargarChat();
            });
        });

        recargarChat();
        return root;
    }

    private void mostrarSelectorStickers(java.util.function.Consumer<String> alSeleccionarSticker) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sticker Pack", true);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_CARD);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        header.setOpaque(false);
        JLabel lbl = new JLabel("✨ Elige un Sticker");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_WHITE);
        header.add(lbl);
        dlg.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setBackground(BG_CARD);
        grid.setBorder(new EmptyBorder(10, 15, 15, 15));

        Lista<String> stickers = InstaFileManager.cargarStickers(usuarioActual.getUsername());
        Nodo<String> n = stickers.getHead();

        while (n != null) {
            String rutaOTexto = n.getDato();
            JButton btn = new JButton();
            btn.setBackground(BG_INPUT);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(95, 95));

            File f = new File(rutaOTexto);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            } else {
                btn.setText(rutaOTexto);
                btn.setForeground(TEXT_WHITE);
            }

            btn.addActionListener(e -> {
                alSeleccionarSticker.accept(rutaOTexto);
                dlg.dispose();
            });

            grid.add(btn);
            n = n.getSiguiente();
        }

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(14);
        dlg.add(sc, BorderLayout.CENTER);

        dlg.setVisible(true);
    }

    private synchronized void recargarChat() {
        if (pnlChatStream == null || pnlListaConversaciones == null) return;
        pnlListaConversaciones.removeAll();

        String query = (txtBuscarChats != null) ? txtBuscarChats.getText().trim().toLowerCase() : "";

        Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
        Nodo<Usuario> nu = todos.getHead();
        while (nu != null) {
            Usuario uObj = nu.getDato();
            String u = uObj.getUsername();
            if (!u.equalsIgnoreCase(usuarioActual.getUsername())) {
                if (query.isEmpty() || u.toLowerCase().contains(query) || uObj.getNombreCompleto().toLowerCase().contains(query)) {
                    boolean isSelected = u.equalsIgnoreCase(chatUsuarioSeleccionado);
                    JPanel item = new JPanel(new BorderLayout(10, 0));
                    item.setOpaque(true);
                    item.setBackground(isSelected ? BG_HOVER : BG_SIDEBAR);
                    item.setBorder(new EmptyBorder(10, 14, 10, 14));
                    item.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    item.add(crearAvatarCircular(u, 40, false, null), BorderLayout.WEST);

                    JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
                    textPanel.setOpaque(false);

                    JLabel lblName = new JLabel(uObj.getNombreCompleto());
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lblName.setForeground(TEXT_WHITE);

                    Lista<MensajeInbox> ultimos = InstaFileManager.obtenerConversacion(usuarioActual.getUsername(), u);
                    String preview = "@" + u;
                    if (!ultimos.estaVacia()) {
                        MensajeInbox ultimoMsg = ultimos.obtener(ultimos.getSize() - 1);
                        if (ultimoMsg.getTipo() == MensajeInbox.Tipo.STICKER) {
                            preview = "🌟 [Sticker]";
                        } else {
                            preview = ultimoMsg.getTexto();
                        }
                    }
                    if (preview.length() > 22) preview = preview.substring(0, 22) + "...";

                    JLabel lblSub = new JLabel(preview);
                    lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lblSub.setForeground(TEXT_MUTED);

                    textPanel.add(lblName);
                    textPanel.add(lblSub);
                    item.add(textPanel, BorderLayout.CENTER);

                    item.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            chatUsuarioSeleccionado = u;
                            recargarChat();
                        }
                    });
                    pnlListaConversaciones.add(item);
                    pnlListaConversaciones.add(Box.createVerticalStrut(2));
                }
            }
            nu = nu.getSiguiente();
        }

        pnlChatStream.removeAll();
        Lista<MensajeInbox> conversacion = InstaFileManager.obtenerConversacion(usuarioActual.getUsername(), chatUsuarioSeleccionado);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

        Nodo<MensajeInbox> n = conversacion.getHead();
        while (n != null) {
            MensajeInbox m = n.getDato();
            boolean esMio = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername());
            String hora = sdf.format(m.getFecha());

            JPanel row = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            row.setOpaque(false);

            boolean esStickerArchivo = (m.getTipo() == MensajeInbox.Tipo.STICKER) && new File(m.getTexto()).exists();

            JPanel bubble = new JPanel(new BorderLayout(0, 4)) {
                @Override
                protected void paintComponent(Graphics g) {
                    if (!esStickerArchivo) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        if (esMio) {
                            GradientPaint gp = new GradientPaint(0, 0, BTN_BLUE_1, getWidth(), getHeight(), BTN_BLUE_2);
                            g2.setPaint(gp);
                        } else {
                            g2.setColor(BG_INPUT);
                        }
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            bubble.setOpaque(false);
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

            if (esStickerArchivo) {
                File fSticker = new File(m.getTexto());
                ImageIcon ic = new ImageIcon(fSticker.getAbsolutePath());
                Image img = ic.getImage().getScaledInstance(95, 95, Image.SCALE_SMOOTH);
                JLabel lblStickerImg = new JLabel(new ImageIcon(img));

                JLabel lblHora = new JLabel(hora, esMio ? SwingConstants.RIGHT : SwingConstants.LEFT);
                lblHora.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                lblHora.setForeground(TEXT_MUTED);

                bubble.add(lblStickerImg, BorderLayout.CENTER);
                bubble.add(lblHora, BorderLayout.SOUTH);
            } else if (m.getTipo() == MensajeInbox.Tipo.STICKER) {
                JLabel lblMsg = new JLabel("<html><body style='color:#ffffff; font-size:13px; font-family:Segoe UI;'>"
                        + "🌟 <b>[Sticker]</b> " + m.getTexto() + "<br><span style='font-size:9px; color:#cbd5e1;'>" + hora + "</span></body></html>");
                bubble.add(lblMsg, BorderLayout.CENTER);
            } else {
                JLabel lblMsg = new JLabel("<html><body style='max-width:320px; color:#ffffff; font-size:12px; font-family:Segoe UI;'>"
                        + m.getTexto() + "<br><span style='font-size:9px; color:#cbd5e1;'>" + hora + "</span></body></html>");
                bubble.add(lblMsg, BorderLayout.CENTER);
            }

            row.add(bubble);
            pnlChatStream.add(row);
            pnlChatStream.add(Box.createVerticalStrut(8));
            n = n.getSiguiente();
        }

        pnlListaConversaciones.revalidate();
        pnlListaConversaciones.repaint();
        pnlChatStream.revalidate();
        pnlChatStream.repaint();
    }

    private void iniciarHiloSincronizacionChat() {
        Thread t = new Thread(() -> {
            while (hiloChatActivo) {
                try {
                    Thread.sleep(2000);
                    SwingUtilities.invokeLater(this::recargarChat);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // =========================================================================
    // 4. SUBIR PUBLICACIÓN / IMAGEN
    // =========================================================================
    private JPanel crearVistaUpload() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_MAIN);

        JPanel card = new JPanel(new GridLayout(9, 1, 8, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(22, 28, 22, 28)
        ));
        card.setPreferredSize(new Dimension(480, 440));

        JLabel lblTit = new JLabel("➕ Crear Nueva Publicación", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTit.setForeground(TEXT_WHITE);

        JTextField txtDesc = new JTextField("Escribe una descripción (#tags, @menciones)...");
        estilizarCampoTexto(txtDesc, "");

        final String[] rutaSel = {null};
        JButton btnImg = crearBotonSecundario("📁 Seleccionar imagen (.jpg / .png)");

        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                File destino = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/imagenes/" + sel.getName());
                try {
                    Files.copy(sel.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    rutaSel[0] = destino.getAbsolutePath();
                    btnImg.setText("✅ " + sel.getName());
                } catch (Exception ex) {
                    rutaSel[0] = sel.getAbsolutePath();
                }
            }
        });

        File uFolders = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/folders_personales");
        String[] carpetas = uFolders.list((dir, name) -> new File(dir, name).isDirectory());
        if (carpetas == null || carpetas.length == 0) carpetas = new String[]{"General"};
        JComboBox<String> cbCarpetas = new JComboBox<>(carpetas);
        estilizarComboBox(cbCarpetas);

        final String[] stickerSeleccionado = {null};
        JButton btnElegirSticker = crearBotonSecundario("🌟 Seleccionar Sticker (Pack)");
        btnElegirSticker.addActionListener(e -> {
            mostrarSelectorStickers(st -> {
                stickerSeleccionado[0] = st;
                File f = new File(st);
                btnElegirSticker.setText("✅ Sticker: " + (f.exists() ? f.getName() : st));
            });
        });

        JCheckBox chkHistoria = new JCheckBox("Subir como Historia (Story 24h)");
        chkHistoria.setForeground(TEXT_WHITE);
        chkHistoria.setOpaque(false);

        JButton btnPub = crearBotonGradiente("Publicar en Instagram", 400, 38);
        btnPub.addActionListener(e -> {
            String txt = txtDesc.getText().trim();
            if (txt.length() > 220) {
                JOptionPane.showMessageDialog(this, "La descripción no puede exceder 220 caracteres.");
                return;
            }
            Publicacion p = new Publicacion(usuarioActual.getUsername(), txt, rutaSel[0], stickerSeleccionado[0], chkHistoria.isSelected());
            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(usuarioActual.getUsername());
            posts.agregar(p);
            InstaFileManager.guardarPublicaciones(usuarioActual.getUsername(), posts);

            JOptionPane.showMessageDialog(this, "¡Publicación subida con éxito!");
            txtDesc.setText("");
            rutaSel[0] = null;
            stickerSeleccionado[0] = null;
            btnImg.setText("📁 Seleccionar imagen (.jpg / .png)");
            btnElegirSticker.setText("🌟 Seleccionar Sticker (Pack)");
            activeCard[0] = "TIMELINE";
            appCardLayout.show(appMainContent, "TIMELINE");
            recargarTimeline();
        });

        JLabel lblCarpetaDest = new JLabel("Carpeta personal de destino:");
        lblCarpetaDest.setForeground(TEXT_MUTED);

        card.add(lblTit);
        card.add(btnImg);
        card.add(txtDesc);
        card.add(lblCarpetaDest);
        card.add(cbCarpetas);
        card.add(btnElegirSticker);
        card.add(chkHistoria);
        card.add(btnPub);

        root.add(card);
        return root;
    }

    // =========================================================================
    // 5. NOTIFICACIONES / MENCIONES
    // =========================================================================
    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel l = new JLabel("🔔 Notificaciones e Interacciones");
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(TEXT_WHITE);
        p.add(l, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnRef = crearBotonGradiente("Actualizar Notificaciones", 220, 38);
        btnRef.addActionListener(e -> {
            model.clear();
            Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
            Nodo<Usuario> nu = todos.getHead();
            while (nu != null) {
                Usuario u = nu.getDato();
                if (u.isActivo()) {
                    Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                    Nodo<Publicacion> np = posts.getHead();
                    while (np != null) {
                        Publicacion pub = np.getDato();
                        if (pub.getMenciones().contiene(usuarioActual.getUsername().toLowerCase())) {
                            model.addElement("💬 @" + pub.getAutor() + " te mencionó: \"" + pub.getContenido() + "\"");
                        }
                        np = np.getSiguiente();
                    }
                }
                nu = nu.getSiguiente();
            }
            if (model.isEmpty()) model.addElement("No tienes notificaciones pendientes.");
        });

        btnRef.doClick();
        p.add(btnRef, BorderLayout.SOUTH);
        return p;
    }

    // =========================================================================
    // 6. PERFIL DEL USUARIO
    // =========================================================================
    private JPanel pnlPerfilHeader;
    private JPanel pnlPerfilGrid;

    private JPanel crearVistaPerfil() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        pnlPerfilHeader = new JPanel(new BorderLayout(25, 0));
        pnlPerfilHeader.setBackground(BG_MAIN);
        pnlPerfilHeader.setBorder(new EmptyBorder(30, 40, 20, 40));

        pnlPerfilGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        pnlPerfilGrid.setBackground(BG_MAIN);
        pnlPerfilGrid.setBorder(new EmptyBorder(20, 40, 40, 40));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(BG_MAIN);
        wrap.add(pnlPerfilHeader);
        wrap.add(pnlPerfilGrid);

        JScrollPane sc = new JScrollPane(wrap);
        sc.setBorder(null);
        root.add(sc, BorderLayout.CENTER);
        recargarPerfil();
        return root;
    }

    private void recargarPerfil() {
        if (pnlPerfilHeader == null || pnlPerfilGrid == null) return;
        pnlPerfilHeader.removeAll();
        pnlPerfilGrid.removeAll();

        Usuario usuarioTemp = InstaFileManager.buscarUsuario(usuarioPerfilVisitado);
        final Usuario u = (usuarioTemp != null) ? usuarioTemp : usuarioActual;

        boolean esPropio = u.getUsername().equalsIgnoreCase(usuarioActual.getUsername());
        Lista<String> followers = InstaFileManager.cargarSeguidores(u.getUsername());
        Lista<String> following = InstaFileManager.cargarSeguidos(u.getUsername());
        Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());

        pnlPerfilHeader.add(crearAvatarCircular(u.getUsername(), 86, true, null), BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 6));
        info.setOpaque(false);
        JLabel lblU = new JLabel("@" + u.getUsername() + "   (" + (u.isActivo() ? "🟢 Activo" : "🔴 Inactivo") + ")");
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblU.setForeground(TEXT_WHITE);

        JPanel rowStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        rowStats.setOpaque(false);
        rowStats.add(new JLabel("<html><b>" + posts.getSize() + "</b> publicaciones</html>"));
        rowStats.add(new JLabel("<html><b>" + followers.getSize() + "</b> seguidores</html>"));
        rowStats.add(new JLabel("<html><b>" + following.getSize() + "</b> seguidos</html>"));
        for (Component c : rowStats.getComponents()) c.setForeground(TEXT_WHITE);

        JLabel lblBio = new JLabel("<html><b>" + u.getNombreCompleto() + "</b> • Edad: " + u.getEdad() + " | Género: " + u.getGenero() + "</html>");
        lblBio.setForeground(TEXT_MUTED);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        if (!esPropio) {
            Lista<String> misSeguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
            final boolean loSigo = misSeguidos.contiene(u.getUsername().toLowerCase());

            JButton btnSeguir = new JButton(loSigo ? "Siguiendo" : "Seguir");
            btnSeguir.setBackground(loSigo ? BG_INPUT : IG_BLUE);
            btnSeguir.setForeground(Color.WHITE);
            btnSeguir.addActionListener(e -> {
                InstaFileManager.toggleSeguir(usuarioActual.getUsername(), u.getUsername());
                recargarPerfil();
            });
            btnRow.add(btnSeguir);
        }

        info.add(lblU);
        info.add(rowStats);
        info.add(lblBio);
        info.add(btnRow);
        pnlPerfilHeader.add(info, BorderLayout.CENTER);

        Nodo<Publicacion> np = posts.getHead();
        while (np != null) {
            Publicacion pub = np.getDato();
            JPanel gItem = new JPanel(new BorderLayout());
            gItem.setBackground(BG_CARD);
            gItem.setPreferredSize(new Dimension(140, 140));
            gItem.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

            if (pub.getRutaImagen() != null && new File(pub.getRutaImagen()).exists()) {
                ImageIcon ic = new ImageIcon(pub.getRutaImagen());
                Image sc = ic.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                gItem.add(new JLabel(new ImageIcon(sc)), BorderLayout.CENTER);
            } else {
                JLabel lbl = new JLabel("<html><center style='color:#cbd5e1; font-size:10px; padding:10px;'>"
                        + pub.getContenido() + "</center></html>", SwingConstants.CENTER);
                gItem.add(lbl, BorderLayout.CENTER);
            }
            pnlPerfilGrid.add(gItem);
            np = np.getSiguiente();
        }

        pnlPerfilHeader.revalidate();
        pnlPerfilHeader.repaint();
        pnlPerfilGrid.revalidate();
        pnlPerfilGrid.repaint();
    }

    // =========================================================================
    // 7. CONFIGURACIÓN Y EDITAR PERFIL
    // =========================================================================
    private JPanel crearVistaEditarPerfil() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_MAIN);

        JPanel card = new JPanel(new GridLayout(7, 2, 10, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setPreferredSize(new Dimension(480, 340));

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        JTextField txtNom = new JTextField(u.getNombreCompleto());
        estilizarCampoTexto(txtNom, "");

        JPasswordField txtPass = new JPasswordField(u.getPass());
        estilizarCampoTexto(txtPass, "");

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(u.getEdad(), 1, 120, 1));
        estilizarSpinner(spinEdad);

        JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
        estilizarComboBox(cbGen);
        cbGen.setSelectedItem(String.valueOf(u.getGenero()));

        JButton btnGuardar = crearBotonGradiente("Guardar Cambios", 180, 36);
        JButton btnDesactivar = crearBotonSecundario(u.isActivo() ? "Desactivar Cuenta" : "Reactivar Cuenta");

        btnGuardar.addActionListener(e -> {
            Usuario usr = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
            if (usr != null) {
                usr.setNombreCompleto(txtNom.getText().trim());
                usr.setPass(new String(txtPass.getPassword()));
                usr.setEdad((Integer) spinEdad.getValue());
                usr.setGenero(((String) cbGen.getSelectedItem()).charAt(0));
                InstaFileManager.actualizarUsuario(usr);
                JOptionPane.showMessageDialog(this, "Perfil actualizado con éxito.");
                recargarPerfil();
            }
        });

        btnDesactivar.addActionListener(e -> {
            Usuario usr = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
            if (usr != null) {
                if (usr.isActivo()) {
                    int resp = JOptionPane.showConfirmDialog(this, "¿Desactivar tu cuenta?\nNo aparecerás en búsquedas ni feed.", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        usr.setActivo(false);
                        InstaFileManager.actualizarUsuario(usr);
                        btnDesactivar.setText("Reactivar Cuenta");
                        recargarPerfil();
                    }
                } else {
                    usr.setActivo(true);
                    InstaFileManager.actualizarUsuario(usr);
                    JOptionPane.showMessageDialog(this, "¡Cuenta reactivada exitosamente!");
                    btnDesactivar.setText("Desactivar Cuenta");
                    recargarPerfil();
                }
            }
        });

        JLabel lblL1 = new JLabel("Nombre Completo:"); lblL1.setForeground(TEXT_WHITE);
        JLabel lblL2 = new JLabel("Contraseña:"); lblL2.setForeground(TEXT_WHITE);
        JLabel lblL3 = new JLabel("Edad:"); lblL3.setForeground(TEXT_WHITE);
        JLabel lblL4 = new JLabel("Género:"); lblL4.setForeground(TEXT_WHITE);
        JLabel lblL5 = new JLabel("Acciones:"); lblL5.setForeground(TEXT_WHITE);
        JLabel lblL6 = new JLabel("Estado:"); lblL6.setForeground(TEXT_WHITE);

        card.add(lblL1);
        card.add(txtNom);
        card.add(lblL2);
        card.add(txtPass);
        card.add(lblL3);
        card.add(spinEdad);
        card.add(lblL4);
        card.add(cbGen);
        card.add(lblL5);
        card.add(btnGuardar);
        card.add(lblL6);
        card.add(btnDesactivar);

        root.add(card);
        return root;
    }

    // =========================================================================
    // GENERADOR DE AVATARES CIRCULARES CON FOTO O DEGRADADO
    // =========================================================================
    public static JComponent crearAvatarCircular(String username, int diametro, boolean tieneStoryRing, String badgeOverlay) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int offset = tieneStoryRing ? 3 : 0;
                int size = diametro - (offset * 2);

                if (tieneStoryRing) {
                    GradientPaint gp = new GradientPaint(0, 0, G_ORANGE, diametro, diametro, G_PURPLE);
                    g2.setPaint(gp);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawOval(1, 1, diametro - 3, diametro - 3);
                }

                Usuario u = InstaFileManager.buscarUsuario(username);
                boolean fotoPintada = false;

                if (u != null && u.getFotoPerfil() != null) {
                    File f = new File(u.getFotoPerfil());
                    if (f.exists()) {
                        try {
                            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                            Image img = icon.getImage();
                            Shape clipAnterior = g2.getClip();
                            g2.setClip(new java.awt.geom.Ellipse2D.Float(offset, offset, size, size));
                            g2.drawImage(img, offset, offset, size, size, null);
                            g2.setClip(clipAnterior);
                            fotoPintada = true;
                        } catch (Exception ignored) {}
                    }
                }

                if (!fotoPintada) {
                    int hash = Math.abs((username != null ? username : "user").hashCode());
                    Color[] avatarColors = {
                        new Color(59, 130, 246),
                        new Color(236, 72, 153),
                        new Color(139, 92, 246),
                        new Color(16, 185, 129),
                        new Color(245, 158, 11)
                    };
                    Color userColor = avatarColors[hash % avatarColors.length];

                    g2.setColor(userColor);
                    g2.fillOval(offset, offset, size, size);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, diametro / 3 + 2));
                    String letter = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "U";
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = offset + (size - fm.stringWidth(letter)) / 2;
                    int ty = offset + (size + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(letter, tx, ty);
                }

                if (badgeOverlay != null) {
                    g2.setColor(IG_BLUE);
                    g2.fillOval(diametro - 16, diametro - 16, 15, 15);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString("+", diametro - 13, diametro - 4);
                }
                g2.dispose();
            }
        };
        comp.setPreferredSize(new Dimension(diametro, diametro));
        return comp;
    }
}