package Windows;

import Insta.InstaPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
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

    // Registro de Ventanas Abiertas
    private final Map<String, JInternalFrame> ventanasAbiertas = new HashMap<>();

    // Paleta de Colores Windows Dark Modern
    private final Color TASKBAR_COLOR   = new Color(15, 23, 42, 245);
    private final Color START_MENU_BG   = new Color(30, 41, 59, 250);
    private final Color ACCENT_BLUE     = new Color(0, 120, 215);
    private final Color HOVER_COLOR     = new Color(255, 255, 255, 30);
    private final Color TEXT_WHITE      = new Color(241, 245, 249);
    private final Color TEXT_MUTED      = new Color(148, 163, 184);

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

        crearIconosEscritorio();
        crearMenuInicio();
        crearBarraDeTareas();
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

    // GESTIÓN DE VENTANAS CON INSTANCIA ÚNICA Y REAPERTURA
    private void gestionarVentana(String appId, String titulo, JComponent content, int ancho, int alto) {
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

        int posX = Math.max(30, (desktopPane.getWidth() - ancho) / 2 + (ventanasAbiertas.size() * 20));
        int posY = Math.max(30, (desktopPane.getHeight() - alto) / 2 + (ventanasAbiertas.size() * 20));
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

    // ICONOS DEL ESCRITORIO
    private void crearIconosEscritorio() {
        int x = 20;
        int y = 20;
        int gap = 95;

        desktopPane.add(crearIconoEscritorio("archivos_icono", "Este equipo", x, y, () -> abrirExplorador()));
        desktopPane.add(crearIconoEscritorio("word_icon", "Editor Word", x, y += gap, () -> abrirEditor()));
        desktopPane.add(crearIconoEscritorio("imagenes_icono", "Visor Fotos", x, y += gap, () -> abrirVisor()));
        desktopPane.add(crearIconoEscritorio("musica_icono", "Reproductor", x, y += gap, () -> abrirReproductor()));
        desktopPane.add(crearIconoEscritorio("cmd_icono", "Consola CMD", x, y += gap, () -> abrirCMD()));
        desktopPane.add(crearIconoEscritorio("instagram_icon", "INSTA+", x, y += gap, () -> abrirInsta()));
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
        p.setBounds(x, y, 90, 85);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icono = cargarIcono(nombreIcono, 44, 44);
        JLabel lblIcon = new JLabel(icono != null ? icono : new ImageIcon(), SwingConstants.CENTER);

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

    // MENÚ INICIO
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
        startMenu.setSize(390, 440);
        startMenu.setVisible(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);

        JPanel colLeft = new JPanel(new GridLayout(8, 1, 2, 2));
        colLeft.setOpaque(false);

        colLeft.add(crearBotonMenu(null, usuarioActual.getUsername() + (usuarioActual.isEsAdmin() ? " (Admin)" : ""), null, true));
        colLeft.add(crearBotonMenu("archivos_icono", "Explorador", () -> abrirExplorador(), false));
        colLeft.add(crearBotonMenu("word_icon", "Editor Word", () -> abrirEditor(), false));
        colLeft.add(crearBotonMenu("imagenes_icono", "Visor Fotos", () -> abrirVisor(), false));
        colLeft.add(crearBotonMenu("cmd_icono", "Consola CMD", () -> abrirCMD(), false));
        colLeft.add(crearBotonMenu("musica_icono", "Reproductor", () -> abrirReproductor(), false));
        colLeft.add(crearBotonMenu("instagram_icon", "INSTA+", () -> abrirInsta(), false));
        colLeft.add(crearBotonMenu(null, "Cerrar Sesión", () -> cerrarSesion(), false));

        JPanel colRight = new JPanel(new GridLayout(8, 1, 2, 2));
        colRight.setOpaque(false);

        JLabel lblAccesos = new JLabel("Accesos Directos");
        lblAccesos.setForeground(TEXT_MUTED);
        lblAccesos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colRight.add(lblAccesos);

        colRight.add(crearBotonMenu("archivos_icono", "Documentos", () -> abrirExplorador(), false));
        colRight.add(crearBotonMenu("imagenes_icono", "Imágenes", () -> abrirVisor(), false));
        colRight.add(crearBotonMenu("musica_icono", "Música", () -> abrirReproductor(), false));

        grid.add(colLeft);
        grid.add(colRight);
        startMenu.add(grid, BorderLayout.CENTER);

        desktopPane.add(startMenu, JLayeredPane.POPUP_LAYER);
    }

    private JButton crearBotonMenu(String nombreIcono, String texto, Runnable accion, boolean isHeader) {
        JButton btn = new JButton(texto);
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

    // BARRA DE TAREAS
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

        center.add(crearBotonBarra("archivos_icono", () -> abrirExplorador(), "Explorador de Archivos"));
        center.add(crearBotonBarra("word_icon", () -> abrirEditor(), "Editor de Texto"));
        center.add(crearBotonBarra("imagenes_icono", () -> abrirVisor(), "Visor de Fotos"));
        center.add(crearBotonBarra("musica_icono", () -> abrirReproductor(), "Reproductor de Música"));
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
        dispose();
        SwingUtilities.invokeLater(() -> new WindowsLoginFrame().setVisible(true));
    }

    private void abrirExplorador() { gestionarVentana("EXPLORADOR", "Explorador de Archivos (Z:\\)", crearExploradorReal(), 880, 560); }
    private void abrirEditor() { gestionarVentana("EDITOR", "Editor de Documentos con Formato", crearEditorReal(), 820, 540); }
    private void abrirVisor() { gestionarVentana("VISOR", "Visor de Imágenes", crearVisorReal(), 760, 520); }
    private void abrirCMD() { gestionarVentana("CMD", "Símbolo del Sistema (CMD)", crearCmdReal(), 720, 440); }
    private void abrirReproductor() { gestionarVentana("REPRODUCTOR", "Reproductor de Música", crearReproductorReal(), 920, 580); }
    private void abrirInsta() { gestionarVentana("INSTA", "INSTA+ - Red Social Integrada", new InstaPanel(usuarioActual), 920, 620); }

    // =========================================================================
    // 1. EXPLORADOR DE ARCHIVOS
    // =========================================================================
    private JPanel crearExploradorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(248, 250, 252));

        File raizUsuario = usuarioActual.isEsAdmin() 
                ? new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA)
                : new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername());

        DefaultMutableTreeNode raizNodo = new DefaultMutableTreeNode(raizUsuario.getName());
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raizNodo);
        JTree tree = new JTree(modeloArbol);
        tree.setBackground(new Color(241, 245, 249));
        JScrollPane scrollTree = new JScrollPane(tree);
        scrollTree.setPreferredSize(new Dimension(220, 0));
        scrollTree.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(226, 232, 240)));

        String[] columnas = {"Nombre", "Tipo", "Tamaño", "Fecha de Modificación"};
        DefaultTableModel modelTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tableArchivos = new JTable(modelTabla);
        tableArchivos.setRowHeight(24);
        tableArchivos.setShowGrid(false);
        tableArchivos.setSelectionBackground(new Color(219, 234, 254));
        tableArchivos.setSelectionForeground(Color.BLACK);
        JScrollPane scrollTabla = new JScrollPane(tableArchivos);

        final File[] carpetaActual = {raizUsuario};

        Runnable cargarContenido = () -> {
            raizNodo.removeAllChildren();
            poblarNodos(raizUsuario, raizNodo);
            modeloArbol.reload();

            modelTabla.setRowCount(0);
            File[] files = carpetaActual[0].listFiles();
            if (files != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                for (File f : files) {
                    String tipo = f.isDirectory() ? "Carpeta de archivos" : "Archivo " + obtenerExtension(f.getName());
                    String tam = f.isDirectory() ? "--" : (f.length() / 1024) + " KB";
                    modelTabla.addRow(new Object[]{f.getName(), tipo, tam, sdf.format(new Date(f.lastModified()))});
                }
            }
        };
        cargarContenido.run();

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JButton btnOrganizar = new JButton("⚡ Organizar (Hilo)");
        JButton btnCrear = new JButton("➕ Nueva Carpeta");
        JButton btnEliminar = new JButton("❌ Eliminar");

        btnOrganizar.setBackground(new Color(238, 242, 255));
        btnOrganizar.setForeground(new Color(79, 70, 229));

        btnOrganizar.addActionListener(e -> {
            new Thread(() -> {
                organizarArchivos(carpetaActual[0]);
                SwingUtilities.invokeLater(() -> {
                    cargarContenido.run();
                    JOptionPane.showMessageDialog(this, "¡Archivos clasificados en Mis Documentos, Música y Mis Imágenes!", "Organizador", JOptionPane.INFORMATION_MESSAGE);
                });
            }).start();
        });

        btnCrear.addActionListener(e -> {
            String nom = JOptionPane.showInputDialog(this, "Nombre de la nueva carpeta:");
            if (nom != null && !nom.trim().isEmpty()) {
                new File(carpetaActual[0], nom.trim()).mkdirs();
                cargarContenido.run();
            }
        });

        btnEliminar.addActionListener(e -> {
            int row = tableArchivos.getSelectedRow();
            if (row != -1) {
                String nombre = (String) modelTabla.getValueAt(row, 0);
                File aEliminar = new File(carpetaActual[0], nombre);
                int resp = JOptionPane.showConfirmDialog(this, "¿Desea eliminar '" + nombre + "'?", "Eliminar", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    aEliminar.delete();
                    cargarContenido.run();
                }
            }
        });

        toolbar.add(btnOrganizar);
        toolbar.addSeparator();
        toolbar.add(btnCrear);
        toolbar.add(btnEliminar);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, scrollTabla);
        split.setDividerLocation(220);
        split.setBorder(null);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private String obtenerExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx != -1 ? name.substring(idx).toUpperCase() : "";
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
                        } else if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a")) {
                            Files.move(f.toPath(), new File(musDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } else if (!name.endsWith(".sop")) {
                            Files.move(f.toPath(), new File(docDir, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    // =========================================================================
    // 2. EDITOR DE TEXTO CON FORMATO (JToolBar Corregido con addSeparator)
    // =========================================================================
    private JPanel crearEditorReal() {
        JPanel p = new JPanel(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setFont(new Font("Calibri", Font.PLAIN, 15));
        textPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        // JToolBar permite usar addSeparator() nativamente sin errores
        JToolBar ribbon = new JToolBar();
        ribbon.setFloatable(false);
        ribbon.setBackground(new Color(241, 245, 249));
        ribbon.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> cbFuentes = new JComboBox<>(fuentes);
        cbFuentes.setSelectedItem("Arial");
        cbFuentes.setMaximumSize(new Dimension(150, 28));

        Integer[] tamanos = {10, 12, 14, 16, 18, 20, 24, 28, 32, 40};
        JComboBox<Integer> cbTamanos = new JComboBox<>(tamanos);
        cbTamanos.setSelectedItem(14);
        cbTamanos.setMaximumSize(new Dimension(60, 28));

        final Color[] colorActual = {Color.BLACK};
        JButton btnColor = new JButton("Color");
        btnColor.addActionListener(e -> {
            Color nuevo = JColorChooser.showDialog(this, "Selecciona Color", colorActual[0]);
            if (nuevo != null) colorActual[0] = nuevo;
        });

        JButton btnAplicar = new JButton("Aplicar");
        btnAplicar.setBackground(ACCENT_BLUE);
        btnAplicar.setForeground(Color.WHITE);
        btnAplicar.addActionListener(e -> {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setFontFamily(attrs, (String) cbFuentes.getSelectedItem());
            StyleConstants.setFontSize(attrs, (Integer) cbTamanos.getSelectedItem());
            StyleConstants.setForeground(attrs, colorActual[0]);
            textPane.setCharacterAttributes(attrs, false);
        });

        JButton btnGuardar = new JButton("Guardar (.sop)");
        btnGuardar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arch = fc.getSelectedFile();
                if (!arch.getName().endsWith(".sop")) arch = new File(arch.getAbsolutePath() + ".sop");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(arch))) {
                    oos.writeObject(textPane.getStyledDocument());
                    JOptionPane.showMessageDialog(this, "Documento guardado con su formato.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
                }
            }
        });

        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fc.getSelectedFile()))) {
                    textPane.setStyledDocument((StyledDocument) ois.readObject());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al abrir: " + ex.getMessage());
                }
            }
        });

        ribbon.add(new JLabel(" Fuente: "));
        ribbon.add(cbFuentes);
        ribbon.add(new JLabel(" Tamaño: "));
        ribbon.add(cbTamanos);
        ribbon.add(btnColor);
        ribbon.add(btnAplicar);
        ribbon.addSeparator(); // Compila perfectamente
        ribbon.add(btnAbrir);
        ribbon.add(btnGuardar);

        p.add(ribbon, BorderLayout.NORTH);
        p.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    // 3. CONSOLA CMD
    // =========================================================================
    private JPanel crearCmdReal() {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea areaCmd = new JTextArea();
        areaCmd.setBackground(new Color(12, 12, 12));
        areaCmd.setForeground(new Color(0, 255, 100));
        areaCmd.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaCmd.setEditable(false);
        areaCmd.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JScrollPane(areaCmd), BorderLayout.CENTER);

        final File[] dirActual = {new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername())};

        JTextField input = new JTextField();
        input.setBackground(new Color(12, 12, 12));
        input.setForeground(Color.WHITE);
        input.setFont(new Font("Consolas", Font.BOLD, 14));
        input.setCaretColor(Color.WHITE);
        input.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel sur = new JPanel(new BorderLayout());
        JLabel lblPrompt = new JLabel(" Z:\\" + usuarioActual.getUsername() + "> ");
        lblPrompt.setForeground(new Color(0, 255, 100));
        lblPrompt.setBackground(new Color(12, 12, 12));
        lblPrompt.setOpaque(true);
        sur.add(lblPrompt, BorderLayout.WEST);
        sur.add(input, BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);

        areaCmd.append("Microsoft Windows [Versión Simulada 2.0]\n(c) UNITEC Programación II. Todos los derechos reservados.\n\n");

        input.addActionListener(e -> {
            String cmd = input.getText().trim();
            areaCmd.append(lblPrompt.getText() + cmd + "\n");
            input.setText("");

            String[] partes = cmd.split(" ", 2);
            String comando = partes[0].toLowerCase();
            String arg = partes.length > 1 ? partes[1].trim() : "";

            switch (comando) {
                case "mkdir":
                    if (!arg.isEmpty() && new File(dirActual[0], arg).mkdir()) areaCmd.append("Directorio creado exitosamente.\n");
                    else areaCmd.append("Error al crear carpeta.\n");
                    break;
                case "rm":
                    if (!arg.isEmpty() && new File(dirActual[0], arg).delete()) areaCmd.append("Elemento eliminado.\n");
                    else areaCmd.append("No se encontró el elemento.\n");
                    break;
                case "cd":
                    File destino = new File(dirActual[0], arg);
                    if (destino.exists() && destino.isDirectory()) {
                        if (!usuarioActual.isEsAdmin() && !destino.getAbsolutePath().contains(usuarioActual.getUsername())) {
                            areaCmd.append("Acceso denegado: Fuera del directorio personal.\n");
                        } else {
                            dirActual[0] = destino;
                        }
                    } else areaCmd.append("Ruta no válida.\n");
                    break;
                case "cd..":
                    File padre = dirActual[0].getParentFile();
                    if (padre != null && (usuarioActual.isEsAdmin() || padre.getAbsolutePath().contains(usuarioActual.getUsername()))) {
                        dirActual[0] = padre;
                    }
                    break;
                case "dir":
                    File[] fList = dirActual[0].listFiles();
                    if (fList != null) {
                        for (File f : fList) areaCmd.append(String.format("%-10s %s\n", (f.isDirectory() ? "<DIR>" : f.length() + "B"), f.getName()));
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
                    areaCmd.append("'" + comando + "' no se reconoce como un comando interno.\n");
            }
            lblPrompt.setText(" " + dirActual[0].getPath().replace(SistemadeArchivos.RUTA_RAIZ_SIMULADA, "Z:") + "> ");
        });

        return p;
    }

    // =========================================================================
    // 4. VISOR DE FOTOS
    // =========================================================================
    private JPanel crearVisorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 20, 20));

        JLabel lblImg = new JLabel("No hay imágenes en Mis Imágenes", SwingConstants.CENTER);
        lblImg.setForeground(Color.LIGHT_GRAY);
        p.add(lblImg, BorderLayout.CENTER);

        Lista<File> listaFotos = new Lista<>();
        final int[] index = {0};
        JLabel lblInfo = new JLabel(" 0 / 0 ");
        lblInfo.setForeground(Color.WHITE);

        Runnable mostrar = () -> {
            if (listaFotos.estaVacia()) return;
            File f = listaFotos.obtener(index[0]);
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(Math.min(620, icon.getIconWidth()), Math.min(420, icon.getIconHeight()), Image.SCALE_SMOOTH);
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

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        nav.setBackground(new Color(30, 30, 30));

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

    // =========================================================================
    // 5. MOTOR DE AUDIO MULTIFORMATO (.MP3 NATIVO CON PAUSA/REANUDACIÓN REAL)
    // =========================================================================
    
    private static class MotorAudioPlayer {
        private Process processWMP = null;
        private BufferedWriter wmpWriter = null;
        private Clip clipWav = null;
        private FloatControl gainControl = null;
        private boolean isWav = false;
        private boolean isPaused = false;
        private boolean isPlaying = false;
        private int segundosTranscurridos = 0;
        private int duracionEstimadaSegundos = 180;
        private File archivoActual = null;
        private long microsegundosWavPausa = 0;

        private synchronized void iniciarWorkerWMP() {
            if (processWMP == null || !processWMP.isAlive()) {
                try {
                    // Script puente persistente con el reproductor nativo de Windows (Soporta MP3, M4A, etc.)
                    String psCmd = "$w = New-Object -ComObject WMPlayer.OCX; "
                            + "$w.settings.autoStart = $true; "
                            + "while($l = [Console]::In.ReadLine()){ "
                            + "  if(!$l){break} "
                            + "  $idx = $l.IndexOf(' '); "
                            + "  if($idx -gt 0){ $c=$l.Substring(0,$idx); $a=$l.Substring($idx+1) } else { $c=$l; $a='' } "
                            + "  if($c -eq 'LOAD'){ $w.URL = $a } "
                            + "  elseif($c -eq 'PLAY'){ $w.controls.play() } "
                            + "  elseif($c -eq 'PAUSE'){ $w.controls.pause() } "
                            + "  elseif($c -eq 'STOP'){ $w.controls.stop() } "
                            + "  elseif($c -eq 'VOL'){ $w.settings.volume = [int]$a } "
                            + "  elseif($c -eq 'EXIT'){ break } "
                            + "}";
                    ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", psCmd);
                    processWMP = pb.start();
                    wmpWriter = new BufferedWriter(new OutputStreamWriter(processWMP.getOutputStream()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private synchronized void enviarComandoWMP(String cmd) {
            try {
                if (wmpWriter != null && processWMP != null && processWMP.isAlive()) {
                    wmpWriter.write(cmd);
                    wmpWriter.newLine();
                    wmpWriter.flush();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void reproducir(File file, int volInicial) {
            detener();
            this.archivoActual = file;
            this.isPaused = false;
            this.isPlaying = true;
            this.segundosTranscurridos = 0;

            String name = file.getName().toLowerCase();

            // Si es WAV nativo, usar JavaSound
            if (name.endsWith(".wav") || name.endsWith(".au") || name.endsWith(".aiff")) {
                isWav = true;
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    clipWav = AudioSystem.getClip();
                    clipWav.open(ais);
                    if (clipWav.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        gainControl = (FloatControl) clipWav.getControl(FloatControl.Type.MASTER_GAIN);
                    }
                    duracionEstimadaSegundos = (int) (clipWav.getMicrosecondLength() / 1_000_000);
                    setVolumen(volInicial);
                    clipWav.start();
                    return;
                } catch (Exception ignored) {
                    isWav = false;
                }
            }

            // Para .MP3 y otros formatos: Reproducción real por altavoz vía subsistema de Windows
            isWav = false;
            iniciarWorkerWMP();
            enviarComandoWMP("LOAD " + file.getAbsolutePath());
            enviarComandoWMP("VOL " + volInicial);

            // Estimar duración en segundos basada en el tamaño del archivo MP3
            long bytes = file.length();
            duracionEstimadaSegundos = Math.max(30, (int) (bytes / (192 * 1024 / 8)));
        }

        public void pausar() {
            if (!isPlaying || isPaused) return;
            isPaused = true;
            isPlaying = false;

            if (isWav && clipWav != null && clipWav.isOpen()) {
                microsegundosWavPausa = clipWav.getMicrosecondPosition();
                clipWav.stop();
            } else {
                enviarComandoWMP("PAUSE");
            }
        }

        public void reanudar() {
            if (!isPaused) return;
            isPaused = false;
            isPlaying = true;

            if (isWav && clipWav != null && clipWav.isOpen()) {
                clipWav.setMicrosecondPosition(microsegundosWavPausa);
                clipWav.start();
            } else {
                enviarComandoWMP("PLAY");
            }
        }

        public void detener() {
            isPlaying = false;
            isPaused = false;
            segundosTranscurridos = 0;

            if (isWav && clipWav != null) {
                try {
                    clipWav.stop();
                    clipWav.close();
                } catch (Exception ignored) {}
                clipWav = null;
            } else {
                enviarComandoWMP("STOP");
            }
        }

        public void setVolumen(int vol) { // 0 a 100
            if (isWav && gainControl != null) {
                float val = vol / 100.0f;
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                if (val <= 0.01f) {
                    gainControl.setValue(min);
                } else {
                    float dB = (float) (Math.log10(val) * 20.0);
                    gainControl.setValue(Math.max(min, Math.min(max, dB)));
                }
            } else {
                enviarComandoWMP("VOL " + vol);
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

        public int getDuracionTotalSegundos() {
            if (isWav && clipWav != null && clipWav.isOpen()) {
                return (int) (clipWav.getMicrosecondLength() / 1_000_000);
            }
            return Math.max(duracionEstimadaSegundos, 60);
        }

        public boolean estaReproduciendo() { return isPlaying; }
        public boolean estaPausado() { return isPaused; }
        public File getArchivoActual() { return archivoActual; }
    }

    private final MotorAudioPlayer motorAudio = new MotorAudioPlayer();

    private JPanel crearReproductorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(32, 33, 36));

        // 1. SIDEBAR IZQUIERDO (Estilo oscuro, cero cuadros blancos)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(24, 25, 28));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 46, 50)));

        JPanel topSidebar = new JPanel(new BorderLayout(5, 8));
        topSidebar.setOpaque(false);
        topSidebar.setBorder(new EmptyBorder(14, 12, 10, 12));

        JLabel lblLogo = new JLabel("  Media Player");
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLogo.setIcon(cargarIcono("musica_icono", 20, 20));
        topSidebar.add(lblLogo, BorderLayout.NORTH);

        JTextField searchBar = new JTextField();
        searchBar.setPreferredSize(new Dimension(170, 28));
        searchBar.setBackground(new Color(36, 37, 42));
        searchBar.setForeground(Color.WHITE);
        searchBar.setCaretColor(Color.WHITE);
        searchBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 62, 68), 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        topSidebar.add(searchBar, BorderLayout.SOUTH);
        sidebar.add(topSidebar, BorderLayout.NORTH);

        JPanel navList = new JPanel(new GridLayout(3, 1, 4, 6));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 8, 10, 8));

        JButton btnBiblioteca = crearBotonSidebarItem("Mi Biblioteca", true);
        JButton btnAgregarAudio = crearBotonSidebarItem("Agregar MP3 / Audio...", false);
        JButton btnAbrirCarpeta = crearBotonSidebarItem("Carpeta Música", false);

        navList.add(btnBiblioteca);
        navList.add(btnAgregarAudio);
        navList.add(btnAbrirCarpeta);

        sidebar.add(navList, BorderLayout.CENTER);
        p.add(sidebar, BorderLayout.WEST);

        // 2. PANEL CENTRAL: LISTA DE CANCIONES, CARÁTULA Y DESCRIPCIÓN
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(new Color(32, 33, 36));
        centerPanel.setBorder(new EmptyBorder(18, 20, 10, 20));

        JLabel lblHeader = new JLabel("Música");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        centerPanel.add(lblHeader, BorderLayout.NORTH);

        DefaultListModel<File> playlistModel = new DefaultListModel<>();
        JList<File> playlist = new JList<>(playlistModel);
        playlist.setBackground(new Color(24, 25, 28));
        playlist.setForeground(Color.WHITE);
        playlist.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        playlist.setSelectionBackground(new Color(234, 88, 12));
        playlist.setSelectionForeground(Color.WHITE);
        playlist.setFixedCellHeight(34);

        playlist.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
                if (value instanceof File) {
                    lbl.setText("  " + ((File) value).getName());
                }
                return lbl;
            }
        });

        JScrollPane scrollPlaylist = new JScrollPane(playlist);
        scrollPlaylist.setBorder(BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true));

        // Panel de Detalles (Carátula y Descripción según Sección 3.7 PDF)
        JPanel detailsPanel = new JPanel(new BorderLayout(12, 12));
        detailsPanel.setPreferredSize(new Dimension(280, 0));
        detailsPanel.setBackground(new Color(24, 25, 28));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblCaratula = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
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
        };
        lblCaratula.setPreferredSize(new Dimension(180, 160));
        detailsPanel.add(lblCaratula, BorderLayout.NORTH);

        JTextArea txtDescripcion = new JTextArea("Seleccione una canción de la lista para reproducir.");
        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBackground(new Color(24, 25, 28));
        txtDescripcion.setForeground(new Color(203, 213, 225));
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsPanel.add(new JScrollPane(txtDescripcion), BorderLayout.CENTER);

        JSplitPane splitCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPlaylist, detailsPanel);
        splitCenter.setDividerLocation(380);
        splitCenter.setOpaque(false);
        splitCenter.setBorder(null);

        centerPanel.add(splitCenter, BorderLayout.CENTER);
        p.add(centerPanel, BorderLayout.CENTER);

        // 3. BARRA INFERIOR DE REPRODUCCIÓN (VECTORIAL Y VINCULADA AL AUDIO REAL)
        JPanel bottomBar = new JPanel(new BorderLayout(10, 4));
        bottomBar.setBackground(new Color(18, 19, 21));
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
        lblTrackTitle.setPreferredSize(new Dimension(220, 36));
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

        // Control de Volumen
        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightTools.setOpaque(false);
        JLabel lblVol = new JLabel("VOL");
        lblVol.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblVol.setForeground(TEXT_MUTED);
        JSlider volSlider = new JSlider(0, 100, 80);
        volSlider.setPreferredSize(new Dimension(80, 20));
        volSlider.setOpaque(false);

        volSlider.addChangeListener(e -> {
            motorAudio.setVolumen(volSlider.getValue());
        });

        rightTools.add(lblVol);
        rightTools.add(volSlider);

        controlRow.add(rightTools, BorderLayout.EAST);
        bottomBar.add(controlRow, BorderLayout.CENTER);
        p.add(bottomBar, BorderLayout.SOUTH);

        // Carga de Archivos de la carpeta Música
        File dirMusica = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música");
        Runnable recargarMusica = () -> {
            playlistModel.clear();
            File[] canciones = dirMusica.listFiles((dir, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".m4a") || n.endsWith(".wma") || n.endsWith(".au");
            });
            if (canciones != null) {
                for (File c : canciones) playlistModel.addElement(c);
            }
        };
        recargarMusica.run();

        playlist.addListSelectionListener(e -> {
            File sel = playlist.getSelectedValue();
            if (sel != null) {
                lblTrackTitle.setText(sel.getName());
                txtDescripcion.setText("Título: " + sel.getName() + "\n\n"
                        + "Formato: Audio MP3 / Nativo\n"
                        + "Tamaño: " + (sel.length() / 1024) + " KB\n"
                        + "Ruta: " + sel.getAbsolutePath());
            }
        });

        btnAgregarAudio.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File sel = fc.getSelectedFile();
                    Files.copy(sel.toPath(), new File(dirMusica, sel.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    recargarMusica.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al importar audio: " + ex.getMessage());
                }
            }
        });

        btnAbrirCarpeta.addActionListener(e -> abrirExplorador());

        // HILO CONCURRENTE PARA LA SINCRONIZACIÓN DEL TIEMPO Y PROGRESO
        Thread hiloProgreso = new Thread(() -> {
            while (true) {
                if (motorAudio.estaReproduciendo() && !motorAudio.estaPausado()) {
                    motorAudio.tickSegundo();
                    int curSec = motorAudio.getSegundosTranscurridos();
                    int totSec = motorAudio.getDuracionTotalSegundos();
                    int prog = totSec > 0 ? (int) ((curSec * 100.0) / totSec) : 0;

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(Math.min(100, prog));
                        lblTimeCur.setText(String.format("%02d:%02d", curSec / 60, curSec % 60));
                        lblTimeTotal.setText(String.format("%02d:%02d", totSec / 60, totSec % 60));
                    });
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        });
        hiloProgreso.setDaemon(true);
        hiloProgreso.start();

        // CONTROL DE PLAY / PAUSA EXACTO (REANUDA SIN REINICIAR)
        btnPlay.addActionListener(e -> {
            File sel = playlist.getSelectedValue();
            if (sel == null && playlistModel.getSize() > 0) {
                playlist.setSelectedIndex(0);
                sel = playlist.getSelectedValue();
            }

            if (sel == null) {
                JOptionPane.showMessageDialog(this, "Agregue o seleccione un archivo de audio primero.");
                return;
            }

            File archivoFinal = sel;

            // 1. SI ESTÁ SONANDO -> PAUSAR
            if (motorAudio.estaReproduciendo()) {
                motorAudio.pausar();
                btnPlay.setText("PLAY");
                btnPlay.repaint();
                return;
            }

            // 2. SI ESTABA PAUSADO EN LA MISMA PISTA -> REANUDAR EN EL MISMO SEGUNDO
            if (motorAudio.estaPausado() && archivoFinal.equals(motorAudio.getArchivoActual())) {
                motorAudio.reanudar();
                btnPlay.setText("PAUSE");
                btnPlay.repaint();
                return;
            }

            // 3. REPRODUCIR NUEVA CANCIÓN DESDE EL INICIO
            new Thread(() -> {
                motorAudio.reproducir(archivoFinal, volSlider.getValue());
                SwingUtilities.invokeLater(() -> {
                    btnPlay.setText("PAUSE");
                    btnPlay.repaint();
                    lblTrackTitle.setText(archivoFinal.getName());
                    int tot = motorAudio.getDuracionTotalSegundos();
                    lblTimeTotal.setText(String.format("%02d:%02d", tot / 60, tot % 60));
                });
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
            int idx = playlist.getSelectedIndex();
            if (idx > 0) {
                playlist.setSelectedIndex(idx - 1);
                btnPlay.doClick();
            }
        });

        btnNext.addActionListener(e -> {
            int idx = playlist.getSelectedIndex();
            if (idx != -1 && idx < playlistModel.getSize() - 1) {
                playlist.setSelectedIndex(idx + 1);
                btnPlay.doClick();
            }
        });

        return p;
    }

    // Botones del Sidebar (100% Oscuros con Acento Naranja, Cero Fondo Blanco)
    private JButton crearBotonSidebarItem(String texto, boolean isSelected) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(new Color(42, 45, 52));
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

    // Botones Vectoriales (1 = Prev, 2 = Next, 3 = Stop)
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

                if (tipo == 1) { // Prev
                    g2.fillRect(10, 10, 2, 12);
                    int[] x = {22, 22, 13};
                    int[] y = {9, 23, 16};
                    g2.fillPolygon(x, y, 3);
                } else if (tipo == 2) { // Next
                    int[] x = {10, 10, 19};
                    int[] y = {9, 23, 16};
                    g2.fillPolygon(x, y, 3);
                    g2.fillRect(20, 10, 2, 12);
                } else if (tipo == 3) { // Stop
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