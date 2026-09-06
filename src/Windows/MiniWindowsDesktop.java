package Windows;

import Insta.InstaPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import javax.swing.tree.*;

/**
 * @author David Suazo Palao & Ian Suazo Palao
 */
public class MiniWindowsDesktop extends JFrame {
    private JDesktopPane desktopPane;
    private Image backgroundImage;
    private JPanel startMenu;
    private boolean startMenuVisible = false;
    private final Usuario usuarioActual;

    private final Color TASKBAR_COLOR = new Color(14, 28, 54, 240);
    private final Color START_MENU_BG = new Color(18, 36, 68, 245);
    private final Color HOVER_COLOR    = new Color(255, 255, 255, 35);
    private final Color TEXT_WHITE     = new Color(240, 240, 245);

    public MiniWindowsDesktop(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        setTitle("Mini-Windows OS - Sesión: " + usuarioActual.getUsername() + (usuarioActual.isEsAdmin() ? " (Administrador)" : ""));
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
                    g2d.setPaint(new GradientPaint(0, 0, new Color(0, 114, 206), getWidth(), getHeight(), new Color(2, 45, 100)));
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

        crearIconosEscritorio();
        crearMenuInicio();
        crearBarraDeTareas();
    }

    // Método utilitario para cargar iconos redimensionados con soporte de fallback
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

    // ==========================================
    // ICONOS DEL ESCRITORIO
    // ==========================================
    private void crearIconosEscritorio() {
        int x = 20;
        int y = 20;
        int gap = 95; // Separación suficiente para evitar cualquier solapamiento

        desktopPane.add(crearIconoEscritorio("archivos_icono", "Este equipo", x, y, () -> abrirVentana(crearExploradorReal(), "Este equipo (Z:\\)")));
        desktopPane.add(crearIconoEscritorio("archivos_icono", "Mis Documentos", x, y += gap, () -> abrirVentana(crearExploradorReal(), "Z:\\" + usuarioActual.getUsername() + "\\Mis Documentos")));
        desktopPane.add(crearIconoEscritorio("word_icon", "Editor Word", x, y += gap, () -> abrirVentana(crearEditorReal(), "Editor con Formato")));
        desktopPane.add(crearIconoEscritorio("imagenes_icono", "Visor Fotos", x, y += gap, () -> abrirVentana(crearVisorReal(), "Visor de Imágenes")));
        desktopPane.add(crearIconoEscritorio("musica_icono", "Reproductor", x, y += gap, () -> abrirVentana(crearReproductorReal(), "Reproductor MP3")));
        desktopPane.add(crearIconoEscritorio("instagram_icon", "INSTA+", x, y += gap, () -> abrirVentana(new InstaPanel(usuarioActual), "INSTA+")));
    }

   private JPanel crearIconoEscritorio(String nombreIcono, String texto, int x, int y, Runnable accion) {
        final boolean[] isHovered = {false};

        JPanel p = new JPanel(new BorderLayout(0, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isHovered[0]) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Fondo translúcido con bordes redondeados estilo Windows
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        p.setOpaque(false); // Siempre false para evitar el efecto fantasma
        p.setBounds(x, y, 90, 85);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icono = cargarIcono(nombreIcono, 44, 44);
        JLabel lblIcon = new JLabel(icono != null ? icono : new JLabel("📁").getIcon(), SwingConstants.CENTER);

        JLabel lbl = new JLabel("<html><center style='text-shadow: 1px 1px 2px #000;'>" + texto.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(lblIcon, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { accion.run(); }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true;
                p.repaint();
                if (p.getParent() != null) p.getParent().repaint(p.getBounds());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                p.repaint();
                if (p.getParent() != null) p.getParent().repaint(p.getBounds());
            }
        });

        return p;
    }

    // ==========================================
    // MENÚ INICIO
    // ==========================================
    private void crearMenuInicio() {
        startMenu = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(START_MENU_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        startMenu.setOpaque(false);
        startMenu.setLayout(new BorderLayout(10, 10));
        startMenu.setBorder(new EmptyBorder(15, 15, 15, 15));
        startMenu.setSize(390, 440);
        startMenu.setVisible(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);

        JPanel colLeft = new JPanel(new GridLayout(8, 1, 2, 2));
        colLeft.setOpaque(false);

        colLeft.add(crearBotonMenu(null, usuarioActual.getUsername() + (usuarioActual.isEsAdmin() ? " (Admin)" : ""), null, true));
        colLeft.add(crearBotonMenu("archivos_icono", "Explorador", () -> abrirVentana(crearExploradorReal(), "Explorador de Archivos"), false));
        colLeft.add(crearBotonMenu("word_icon", "Editor Word", () -> abrirVentana(crearEditorReal(), "Editor con Formato"), false));
        colLeft.add(crearBotonMenu("imagenes_icono", "Visor Fotos", () -> abrirVentana(crearVisorReal(), "Visor de Imágenes"), false));
        colLeft.add(crearBotonMenu(null, "Consola CMD", () -> abrirVentana(crearCmdReal(), "CMD"), false));
        colLeft.add(crearBotonMenu("musica_icono", "Reproductor", () -> abrirVentana(crearReproductorReal(), "Reproductor MP3"), false));
        colLeft.add(crearBotonMenu("instagram_icon", "INSTA+", () -> abrirVentana(new InstaPanel(usuarioActual), "INSTA+"), false));
        colLeft.add(crearBotonMenu(null, "Cerrar Sesión", () -> cerrarSesion(), false));

        JPanel colRight = new JPanel(new GridLayout(8, 1, 2, 2));
        colRight.setOpaque(false);

        JLabel lblAccesos = new JLabel("Accesos Directos");
        lblAccesos.setForeground(new Color(180, 200, 220));
        lblAccesos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colRight.add(lblAccesos);

        colRight.add(crearBotonMenu("archivos_icono", "Documentos", () -> abrirVentana(crearExploradorReal(), "Mis Documentos"), false));
        colRight.add(crearBotonMenu("imagenes_icono", "Imágenes", () -> abrirVentana(crearVisorReal(), "Mis Imágenes"), false));
        colRight.add(crearBotonMenu("musica_icono", "Música", () -> abrirVentana(crearReproductorReal(), "Música"), false));

        grid.add(colLeft);
        grid.add(colRight);
        startMenu.add(grid, BorderLayout.CENTER);

        desktopPane.add(startMenu, JLayeredPane.POPUP_LAYER);
    }

    private JButton crearBotonMenu(String nombreIcono, String texto, Runnable accion, boolean isHeader) {
        JButton btn = new JButton(texto);
        if (nombreIcono != null) {
            ImageIcon icon = cargarIcono(nombreIcono, 22, 22);
            if (icon != null) btn.setIcon(icon);
        }
        btn.setFont(new Font("Segoe UI", isHeader ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!isHeader) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { btn.setContentAreaFilled(true); btn.setBackground(HOVER_COLOR); }
                @Override
                public void mouseExited(MouseEvent e) { btn.setContentAreaFilled(false); }
            });
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

    // ==========================================
    // BARRA DE TAREAS (TASKBAR)
    // ==========================================
    private void crearBarraDeTareas() {
        JPanel taskBar = new JPanel(new BorderLayout(10, 0));
        taskBar.setBackground(TASKBAR_COLOR);
        taskBar.setPreferredSize(new Dimension(getWidth(), 52));
        taskBar.setBorder(new EmptyBorder(4, 10, 4, 15));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        left.setOpaque(false);

        JButton btnStart = new JButton("🪟");
        btnStart.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btnStart.setBackground(new Color(0, 120, 215));
        btnStart.setForeground(Color.WHITE);
        btnStart.setPreferredSize(new Dimension(42, 38));
        btnStart.setFocusPainted(false);
        btnStart.setBorder(BorderFactory.createEmptyBorder());
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> toggleStartMenu());
        left.add(btnStart);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        center.setOpaque(false);

        center.add(crearBotonBarra("archivos_icono", () -> abrirVentana(crearExploradorReal(), "Explorador de Archivos")));
        center.add(crearBotonBarra("word_icon", () -> abrirVentana(crearEditorReal(), "Editor de Texto")));
        center.add(crearBotonBarra("imagenes_icono", () -> abrirVentana(crearVisorReal(), "Visor de Fotos")));
        center.add(crearBotonBarra("musica_icono", () -> abrirVentana(crearReproductorReal(), "Reproductor MP3")));
        center.add(crearBotonBarra("instagram_icon", () -> abrirVentana(new InstaPanel(usuarioActual), "INSTA+")));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        right.setOpaque(false);

        JLabel lblTimeDate = new JLabel();
        lblTimeDate.setForeground(Color.WHITE);
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        Timer clockTimer = new Timer(1000, e -> {
            String time = new SimpleDateFormat("hh:mm a").format(new Date());
            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            lblTimeDate.setText("<html><center>" + time + "<br><span style='font-size:9px; color:#cccccc;'>" + date + "</span></center></html>");
        });
        clockTimer.start();

        right.add(lblTimeDate);

        taskBar.add(left, BorderLayout.WEST);
        taskBar.add(center, BorderLayout.CENTER);
        taskBar.add(right, BorderLayout.EAST);

        add(taskBar, BorderLayout.SOUTH);
    }

    private JButton crearBotonBarra(String nombreIcono, Runnable accion) {
        JButton btn = new JButton();
        ImageIcon icon = cargarIcono(nombreIcono, 28, 28);
        if (icon != null) btn.setIcon(icon);

        btn.setPreferredSize(new Dimension(42, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setContentAreaFilled(true); btn.setBackground(HOVER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setContentAreaFilled(false); }
        });

        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private void abrirVentana(JComponent content, String titulo) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        frame.setContentPane(content);
        frame.setSize(840, 560);
        frame.setVisible(true);
        desktopPane.add(frame);
        try { frame.setSelected(true); } catch (Exception ignored) {}
    }

    private void cerrarSesion() {
        dispose();
        SwingUtilities.invokeLater(() -> new WindowsLoginFrame().setVisible(true));
    }

    // ==========================================
    // 5 APLICACIONES INTEGRADAS
    // ==========================================

    private JPanel crearExploradorReal() {
        JPanel p = new JPanel(new BorderLayout());
        File raizUsuario = usuarioActual.isEsAdmin() 
                ? new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA)
                : new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername());

        DefaultMutableTreeNode raizNodo = new DefaultMutableTreeNode(raizUsuario.getName());
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raizNodo);
        JTree tree = new JTree(modeloArbol);
        p.add(new JScrollPane(tree), BorderLayout.CENTER);

        Runnable cargarNodos = () -> {
            raizNodo.removeAllChildren();
            poblarNodos(raizUsuario, raizNodo);
            modeloArbol.reload();
        };
        cargarNodos.run();

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnOrganizar = new JButton("⚡ Organizar (Hilo)");
        JButton btnCrear = new JButton("📁 Nueva Carpeta");
        JButton btnEliminar = new JButton("❌ Eliminar");

        btnOrganizar.addActionListener(e -> {
            new Thread(() -> {
                organizarArchivos(raizUsuario);
                SwingUtilities.invokeLater(() -> {
                    cargarNodos.run();
                    JOptionPane.showMessageDialog(this, "Archivos clasificados en Mis Documentos, Música y Mis Imágenes.");
                });
            }).start();
        });

        btnCrear.addActionListener(e -> {
            String nom = JOptionPane.showInputDialog(this, "Nombre de la carpeta:");
            if (nom != null && !nom.trim().isEmpty()) {
                new File(raizUsuario, nom.trim()).mkdirs();
                cargarNodos.run();
            }
        });

        btnEliminar.addActionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selected != null && !selected.isRoot()) {
                File f = new File(raizUsuario, selected.toString());
                if (f.delete()) cargarNodos.run();
            }
        });

        toolbar.add(btnOrganizar);
        toolbar.addSeparator();
        toolbar.add(btnCrear);
        toolbar.add(btnEliminar);
        p.add(toolbar, BorderLayout.NORTH);

        return p;
    }

    private void poblarNodos(File dir, DefaultMutableTreeNode nodo) {
        File[] archivos = dir.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                DefaultMutableTreeNode hijo = new DefaultMutableTreeNode(f.getName());
                nodo.add(hijo);
                if (f.isDirectory()) poblarNodos(f, hijo);
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
                        } else if (name.endsWith(".mp3") || name.endsWith(".wav")) {
                            Files.move(f.toPath(), new File(musDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            Files.move(f.toPath(), new File(docDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private JPanel crearEditorReal() {
        JPanel p = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        p.add(new JScrollPane(textPane), BorderLayout.CENTER);

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> cbFuentes = new JComboBox<>(fuentes);
        cbFuentes.setSelectedItem("Arial");

        Integer[] tamanos = {10, 12, 14, 16, 18, 22, 26, 32};
        JComboBox<Integer> cbTamanos = new JComboBox<>(tamanos);
        cbTamanos.setSelectedItem(14);

        final Color[] colorActual = {Color.BLACK};
        JButton btnColor = new JButton("🎨 Color");
        btnColor.addActionListener(e -> {
            Color nuevo = JColorChooser.showDialog(this, "Color de texto", colorActual[0]);
            if (nuevo != null) colorActual[0] = nuevo;
        });

        JButton btnAplicar = new JButton("Aplicar Formato");
        btnAplicar.addActionListener(e -> {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, (String) cbFuentes.getSelectedItem());
            StyleConstants.setFontSize(attrs, (Integer) cbTamanos.getSelectedItem());
            StyleConstants.setForeground(attrs, colorActual[0]);
            textPane.setCharacterAttributes(attrs, false);
        });

        JButton btnGuardar = new JButton("💾 Guardar (.sop)");
        btnGuardar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arch = fc.getSelectedFile();
                if (!arch.getName().endsWith(".sop")) arch = new File(arch.getAbsolutePath() + ".sop");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arch))) {
                    oos.writeObject(textPane.getStyledDocument());
                    JOptionPane.showMessageDialog(this, "Documento guardado.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        JButton btnAbrir = new JButton("📂 Abrir");
        btnAbrir.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))) {
                    textPane.setStyledDocument((StyledDocument) ois.readObject());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo cargar: " + ex.getMessage());
                }
            }
        });

        tb.add(new JLabel("Fuente: "));
        tb.add(cbFuentes);
        tb.addSeparator();
        tb.add(new JLabel("Tamaño: "));
        tb.add(cbTamanos);
        tb.addSeparator();
        tb.add(btnColor);
        tb.add(btnAplicar);
        tb.addSeparator();
        tb.add(btnAbrir);
        tb.add(btnGuardar);
        p.add(tb, BorderLayout.NORTH);

        return p;
    }

    private JPanel crearCmdReal() {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea areaCmd = new JTextArea();
        areaCmd.setBackground(Color.BLACK);
        areaCmd.setForeground(Color.GREEN);
        areaCmd.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaCmd.setEditable(false);
        p.add(new JScrollPane(areaCmd), BorderLayout.CENTER);

        final File[] dirActual = {new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername())};

        JTextField input = new JTextField();
        input.setBackground(Color.BLACK);
        input.setForeground(Color.WHITE);
        input.setFont(new Font("Consolas", Font.PLAIN, 14));
        input.setCaretColor(Color.WHITE);

        JPanel sur = new JPanel(new BorderLayout());
        JLabel lblPrompt = new JLabel(" Z:\\" + usuarioActual.getUsername() + "> ");
        lblPrompt.setForeground(Color.GREEN);
        lblPrompt.setBackground(Color.BLACK);
        lblPrompt.setOpaque(true);
        sur.add(lblPrompt, BorderLayout.WEST);
        sur.add(input, BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);

        areaCmd.append("Mini-Windows OS Console [Version 2.0]\n(c) UNITEC. Comandos: mkdir, rm, cd, cd.., dir, date, time\n\n");

        input.addActionListener(e -> {
            String cmd = input.getText().trim();
            areaCmd.append(lblPrompt.getText() + cmd + "\n");
            input.setText("");

            String[] partes = cmd.split(" ", 2);
            String comando = partes[0].toLowerCase();
            String arg = partes.length > 1 ? partes[1].trim() : "";

            switch (comando) {
                case "mkdir":
                    if (!arg.isEmpty() && new File(dirActual[0], arg).mkdir()) areaCmd.append("Directorio creado.\n");
                    else areaCmd.append("Error al crear carpeta.\n");
                    break;
                case "rm":
                    if (!arg.isEmpty() && new File(dirActual[0], arg).delete()) areaCmd.append("Elemento eliminado.\n");
                    else areaCmd.append("No se encontró o no se pudo eliminar.\n");
                    break;
                case "cd":
                    File destino = new File(dirActual[0], arg);
                    if (destino.exists() && destino.isDirectory()) dirActual[0] = destino;
                    else areaCmd.append("Ruta no válida.\n");
                    break;
                case "cd..":
                    if (dirActual[0].getParentFile() != null) dirActual[0] = dirActual[0].getParentFile();
                    break;
                case "dir":
                    File[] fList = dirActual[0].listFiles();
                    if (fList != null) {
                        for (File f : fList) areaCmd.append(String.format("%-10s %s\n", (f.isDirectory() ? "<DIR>" : f.length() + "B"), f.getName()));
                    }
                    break;
                case "date":
                    areaCmd.append("Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "\n");
                    break;
                case "time":
                    areaCmd.append("Hora: " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n");
                    break;
                default:
                    areaCmd.append("Comando no reconocido.\n");
            }
            lblPrompt.setText(" " + dirActual[0].getPath().replace(SistemadeArchivos.RUTA_RAIZ_SIMULADA, "Z:") + "> ");
        });

        return p;
    }

    private JPanel crearVisorReal() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lblImg = new JLabel("No hay imágenes cargadas", SwingConstants.CENTER);
        p.add(new JScrollPane(lblImg), BorderLayout.CENTER);

        Lista<File> listaFotos = new Lista<>();
        final int[] index = {0};
        JLabel lblInfo = new JLabel(" 0 / 0 ");

        Runnable mostrar = () -> {
            if (listaFotos.estaVacia()) return;
            File f = listaFotos.obtener(index[0]);
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(Math.min(500, icon.getIconWidth()), Math.min(350, icon.getIconHeight()), Image.SCALE_SMOOTH);
            lblImg.setIcon(new ImageIcon(scaled));
            lblImg.setText("");
            lblInfo.setText(" " + (index[0] + 1) + " / " + listaFotos.getSize() + " (" + f.getName() + ") ");
        };

        File dirFotos = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Imágenes");
        File[] fotos = dirFotos.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg"));
        if (fotos != null) {
            for (File f : fotos) listaFotos.agregar(f);
            mostrar.run();
        }

        JPanel nav = new JPanel(new FlowLayout());
        JButton btnAnt = new JButton("◀ Anterior");
        JButton btnSig = new JButton("Siguiente ▶");

        btnAnt.addActionListener(e -> {
            if (!listaFotos.estaVacia()) {
                index[0] = (index[0] - 1 + listaFotos.getSize()) % listaFotos.getSize();
                mostrar.run();
            }
        });

        btnSig.addActionListener(e -> {
            if (!listaFotos.estaVacia()) {
                index[0] = (index[0] + 1) % listaFotos.getSize();
                mostrar.run();
            }
        });

        nav.add(btnAnt);
        nav.add(lblInfo);
        nav.add(btnSig);
        p.add(nav, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearReproductorReal() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblCover = new JLabel("🎵 [ Sin Carátula ]", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(140, 140));
        lblCover.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(lblCover, BorderLayout.WEST);

        DefaultListModel<File> playlistModel = new DefaultListModel<>();
        JList<File> playlist = new JList<>(playlistModel);
        p.add(new JScrollPane(playlist), BorderLayout.CENTER);

        File dirMusica = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música");
        File[] canciones = dirMusica.listFiles((dir, name) -> name.endsWith(".mp3") || name.endsWith(".wav"));
        if (canciones != null) {
            for (File c : canciones) playlistModel.addElement(c);
        }

        JPanel bottom = new JPanel(new FlowLayout());
        JButton btnPlay = new JButton("▶ Play");
        JButton btnPause = new JButton("⏸ Pause");
        JButton btnStop = new JButton("⏹ Stop");
        JButton btnAdd = new JButton("➕ Cargar Pista");

        final Thread[] hiloMusica = {null};
        final boolean[] reproduciendo = {false};

        btnPlay.addActionListener(e -> {
            File sel = playlist.getSelectedValue();
            if (sel != null) {
                reproduciendo[0] = true;
                lblCover.setText("🎵 " + sel.getName());
                hiloMusica[0] = new Thread(() -> {
                    while (reproduciendo[0]) {
                        try { Thread.sleep(500); } catch (InterruptedException ex) { break; }
                    }
                });
                hiloMusica[0].start();
            }
        });

        btnPause.addActionListener(e -> reproduciendo[0] = false);
        btnStop.addActionListener(e -> {
            reproduciendo[0] = false;
            lblCover.setText("🎵 [ Detenido ]");
        });

        btnAdd.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                playlistModel.addElement(fc.getSelectedFile());
            }
        });

        bottom.add(btnPlay);
        bottom.add(btnPause);
        bottom.add(btnStop);
        bottom.add(btnAdd);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }
}