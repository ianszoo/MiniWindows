package Windows;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author David Suazo Palao & Ian Suazo Palao
 */
public class WindowsLoginFrame extends JFrame {
    private Image backgroundImage;
    private JPanel cardContainer;
    private CardLayout cardLayout;

    // Campos de Login
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;
    private JLabel lblLoginError;

    // Campos de Registro
    private JTextField txtRegUser;
    private JTextField txtRegNombre;
    private JSpinner spinRegEdad;
    private JComboBox<String> cbRegGenero;
    private JPasswordField txtRegPass;
    private JPasswordField txtRegPassConfirm;
    private JCheckBox chkRegAdmin;
    private JLabel lblRegError;

    public WindowsLoginFrame() {
        setTitle("Mini-Windows OS - Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        cargarFondo();

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0, 114, 206), getWidth(), getHeight(), new Color(2, 45, 100)));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(new Color(10, 25, 45, 175));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);
        cardContainer.setPreferredSize(new Dimension(440, 600));

        cardContainer.add(crearPanelLogin(), "LOGIN");
        cardContainer.add(crearPanelRegistro(), "REGISTRO");

        backgroundPanel.add(cardContainer);
        crearBarraBloqueo();
    }

    private void cargarFondo() {
        File imgFile = new File("wallpaper.jpg");
        if (imgFile.exists()) {
            backgroundImage = new ImageIcon("wallpaper.jpg").getImage();
        } else {
            java.net.URL url = getClass().getResource("/Windows/wallpaper.jpg");
            if (url != null) backgroundImage = new ImageIcon(url).getImage();
        }
    }

    private JPanel crearPanelLogin() {
        JPanel card = crearTarjetaGlass();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER);
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtLoginUser = new JTextField("admin");
        estilizarTextField(txtLoginUser);

        txtLoginPass = new JPasswordField("Admin2026!");
        JPanel passPanel = crearCampoPasswordConOjo(txtLoginPass);

        lblLoginError = new JLabel(" ");
        lblLoginError.setForeground(new Color(255, 120, 120));
        lblLoginError.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLoginError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = crearBotonPrimario("Ingresar  ➔");
        btnLogin.addActionListener(e -> procesarLogin());

        JButton btnIrRegistro = new JButton("¿No tienes cuenta? Crear usuario");
        estilizarBotonLink(btnIrRegistro);
        btnIrRegistro.addActionListener(e -> {
            lblLoginError.setText(" ");
            cardLayout.show(cardContainer, "REGISTRO");
        });

        card.add(Box.createVerticalStrut(10));
        card.add(lblAvatar);
        card.add(Box.createVerticalStrut(10));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(25));
        card.add(txtLoginUser);
        card.add(Box.createVerticalStrut(15));
        card.add(passPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(lblLoginError);
        card.add(Box.createVerticalStrut(15));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));
        card.add(btnIrRegistro);

        return card;
    }

    private JPanel crearPanelRegistro() {
        JPanel card = crearTarjetaGlass();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Crear Nuevo Usuario");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtRegUser = new JTextField();
        estilizarTextField(txtRegUser);

        txtRegNombre = new JTextField();
        estilizarTextField(txtRegNombre);

        JPanel rowExtra = new JPanel(new GridLayout(1, 2, 10, 0));
        rowExtra.setOpaque(false);
        rowExtra.setMaximumSize(new Dimension(320, 36));

        spinRegEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));
        cbRegGenero = new JComboBox<>(new String[]{"Género: M", "Género: F"});
        cbRegGenero.setBackground(new Color(22, 38, 65));
        cbRegGenero.setForeground(Color.WHITE);

        rowExtra.add(spinRegEdad);
        rowExtra.add(cbRegGenero);

        txtRegPass = new JPasswordField();
        JPanel regPassPanel = crearCampoPasswordConOjo(txtRegPass);

        txtRegPassConfirm = new JPasswordField();
        JPanel regPassConfirmPanel = crearCampoPasswordConOjo(txtRegPassConfirm);

        chkRegAdmin = new JCheckBox("Permisos de Administrador");
        chkRegAdmin.setOpaque(false);
        chkRegAdmin.setForeground(new Color(200, 220, 245));
        chkRegAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkRegAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblRegError = new JLabel("<html><center style='color:#ffd2d2; font-size:10px;'>"
                + "Req: Mín. 8 caract, 1 mayúscula, 1 número y 1 símbolo</center></html>");
        lblRegError.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnRegistrar = crearBotonPrimario("Crear Cuenta y Espacio Z:\\");
        btnRegistrar.addActionListener(e -> procesarRegistro());

        JButton btnVolver = new JButton("⬅ Volver al inicio de sesión");
        estilizarBotonLink(btnVolver);
        btnVolver.addActionListener(e -> cardLayout.show(cardContainer, "LOGIN"));

        card.add(Box.createVerticalStrut(5));
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));
        card.add(crearEtiquetaCampo("Usuario (username):"));
        card.add(txtRegUser);
        card.add(Box.createVerticalStrut(6));
        card.add(crearEtiquetaCampo("Nombre Completo:"));
        card.add(txtRegNombre);
        card.add(Box.createVerticalStrut(6));
        card.add(rowExtra);
        card.add(Box.createVerticalStrut(6));
        card.add(crearEtiquetaCampo("Contraseña:"));
        card.add(regPassPanel);
        card.add(Box.createVerticalStrut(6));
        card.add(crearEtiquetaCampo("Confirmar Contraseña:"));
        card.add(regPassConfirmPanel);
        card.add(Box.createVerticalStrut(6));
        card.add(chkRegAdmin);
        card.add(Box.createVerticalStrut(6));
        card.add(lblRegError);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(8));
        card.add(btnVolver);

        return card;
    }

    private JPanel crearCampoPasswordConOjo(JPasswordField pf) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setMaximumSize(new Dimension(320, 38));
        wrapper.setPreferredSize(new Dimension(320, 38));
        wrapper.setBackground(new Color(22, 38, 65));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 110, 170), 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 4)
        ));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        pf.setBackground(new Color(22, 38, 65));
        pf.setForeground(Color.WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setSelectionColor(new Color(0, 120, 215));
        pf.setSelectedTextColor(Color.WHITE);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JButton btnEye = new JButton("👁️");
        btnEye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnEye.setForeground(Color.WHITE);
        btnEye.setContentAreaFilled(false);
        btnEye.setBorderPainted(false);
        btnEye.setFocusPainted(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char defaultEcho = pf.getEchoChar();

        btnEye.addActionListener(e -> {
            if (pf.getEchoChar() == (char) 0) {
                pf.setEchoChar(defaultEcho);
                btnEye.setText("👁️");
            } else {
                pf.setEchoChar((char) 0);
                btnEye.setText("🔒");
            }
        });

        wrapper.add(pf, BorderLayout.CENTER);
        wrapper.add(btnEye, BorderLayout.EAST);
        return wrapper;
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(new Color(200, 220, 245));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // --- AUTENTICACIÓN Y PERSISTENCIA REAL ---

    private void procesarLogin() {
        String user = txtLoginUser.getText().trim();
        String pass = new String(txtLoginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblLoginError.setText("Complete todos los campos.");
            return;
        }

        try {
            Usuario u = SistemadeArchivos.autenticar(user, pass);
            if (u != null) {
                abrirEscritorio(u);
            } else {
                lblLoginError.setText("Usuario o contraseña incorrectos.");
            }
        } catch (CorruptoException ex) {
            lblLoginError.setText("Error: Archivo usuarios.sop corrupto.");
        }
    }

    private void procesarRegistro() {
        String user = txtRegUser.getText().trim();
        String pass = new String(txtRegPass.getPassword());
        String confirm = new String(txtRegPassConfirm.getPassword());

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblRegError.setText("<html><center style='color:#ff6666;'>Llene todos los campos.</center></html>");
            return;
        }

        if (!pass.equals(confirm)) {
            lblRegError.setText("<html><center style='color:#ff6666;'>Las contraseñas no coinciden.</center></html>");
            return;
        }

        try {
            SistemadeArchivos.registrarUsuario(user, pass, chkRegAdmin.isSelected());
            JOptionPane.showMessageDialog(this,
                    "¡Usuario '" + user + "' registrado con éxito!\nSe ha generado el espacio virtual: Z:\\" + user + "\\",
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);

            txtLoginUser.setText(user);
            txtLoginPass.setText("");
            cardLayout.show(cardContainer, "LOGIN");
        } catch (PasswordInvalidEsception ex) {
            lblRegError.setText("<html><center style='color:#ff6666; font-size:10px;'>" + ex.getMessage() + "</center></html>");
        } catch (UsernameDuplicadoException ex) {
            lblRegError.setText("<html><center style='color:#ff6666;'>" + ex.getMessage() + "</center></html>");
        } catch (CorruptoException ex) {
            lblRegError.setText("<html><center style='color:#ff6666;'>Error en archivo usuarios.sop</center></html>");
        }
    }

    private void abrirEscritorio(Usuario usuario) {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            MiniWindowsDesktop desktop = new MiniWindowsDesktop(usuario);
            desktop.setVisible(true);
        });
    }

    private JPanel crearTarjetaGlass() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 30, 55, 230));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(60, 100, 160));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 30, 20, 30));
        return card;
    }

    private void estilizarTextField(JTextField tf) {
        tf.setMaximumSize(new Dimension(320, 38));
        tf.setPreferredSize(new Dimension(320, 38));
        tf.setBackground(new Color(22, 38, 65));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setSelectionColor(new Color(0, 120, 215));
        tf.setSelectedTextColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 110, 170), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JButton crearBotonPrimario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 90, 170));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(25, 145, 255));
                } else {
                    g2.setColor(new Color(0, 120, 215));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(320, 40));
        btn.setPreferredSize(new Dimension(320, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void estilizarBotonLink(JButton btn) {
        btn.setForeground(new Color(160, 210, 255));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void crearBarraBloqueo() {
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(10, 20, 35, 220));
        bottomBar.setBorder(new EmptyBorder(10, 25, 12, 25));

        JLabel lblTime = new JLabel();
        lblTime.setForeground(Color.WHITE);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Timer timer = new Timer(1000, e -> {
            lblTime.setText(new SimpleDateFormat("hh:mm a   |   EEEE, d 'de' MMMM").format(new Date()));
        });
        timer.start();

        JButton btnPower = new JButton("⏻ Apagar");
        btnPower.setForeground(new Color(255, 100, 100));
        btnPower.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPower.setContentAreaFilled(false);
        btnPower.setBorderPainted(false);
        btnPower.setFocusPainted(false);
        btnPower.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPower.addActionListener(e -> System.exit(0));

        bottomBar.add(lblTime, BorderLayout.WEST);
        bottomBar.add(btnPower, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);
    }
}