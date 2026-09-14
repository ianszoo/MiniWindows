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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.tree.*;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import javazoom.jl.player.Player;

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

        desktopPane.add(crearIconoEscritorio("archivos_icono", "Este equipo", x, y, () -> abrirExplorador(null)));
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
        colLeft.add(crearBotonMenu("archivos_icono", "Explorador", () -> abrirExplorador(null), false));
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

        colRight.add(crearBotonMenu("archivos_icono", "Documentos", () -> abrirExplorador(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Mis Documentos")), false));
        colRight.add(crearBotonMenu("imagenes_icono", "Imágenes", () -> abrirVisor(), false));
        colRight.add(crearBotonMenu("musica_icono", "Música", () -> abrirExplorador(new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música")), false));

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

        center.add(crearBotonBarra("archivos_icono", () -> abrirExplorador(null), "Explorador de Archivos"));
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

    private void abrirExplorador(File carpetaInicial) { 
        gestionarVentana("EXPLORADOR", "Explorador de Archivos (Z:\\)", crearExploradorReal(carpetaInicial), 880, 560); 
    }
    private void abrirEditor() { gestionarVentana("EDITOR", "Bloc de Notas - Editor con Formato", crearEditorReal(), 860, 560); }
    private void abrirVisor() { gestionarVentana("VISOR", "Visor de Imágenes", crearVisorReal(), 880, 600); }
    private void abrirCMD() { gestionarVentana("CMD", "Símbolo del Sistema (CMD)", crearCmdReal(), 720, 440); }
    private void abrirReproductor() { gestionarVentana("REPRODUCTOR", "Media Player", crearReproductorReal(), 920, 580); }
    private void abrirInsta() { gestionarVentana("INSTA", "INSTA+ Mobile", new InstaPanel(usuarioActual), 460, 750); 
}

    // Utilidad: Crea botones con estilo oscuro nativo sin fondos blancos en hover
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

    // =========================================================================
    // 1. EXPLORADOR DE ARCHIVOS
    // =========================================================================
    private JPanel crearExploradorReal(File carpetaInicial) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(248, 250, 252));

        File raizUsuario = (carpetaInicial != null && carpetaInicial.exists()) 
                ? carpetaInicial 
                : (usuarioActual.isEsAdmin() ? new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA) : new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername()));

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

        JButton btnOrganizar = new JButton("Organizar (Hilo)");
        JButton btnCrear = new JButton("Nueva Carpeta");
        JButton btnEliminar = new JButton("Eliminar");

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
    // 2. EDITOR DE TEXTO CON FORMATO (COMBOS OSCUROS CON ALTO CONTRASTE)
    // =========================================================================
    private JPanel crearEditorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(32, 32, 32));

        // 1. LIENZO DEL EDITOR
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

        JScrollPane scrollEditor = new JScrollPane(textPane);
        scrollEditor.setBorder(null);
        scrollEditor.setBackground(new Color(30, 30, 30));

        // 2. BARRA DE HERRAMIENTAS MODERNA
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(38, 38, 38));

        JToolBar ribbon = new JToolBar();
        ribbon.setFloatable(false);
        ribbon.setBackground(new Color(38, 38, 38));
        ribbon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(55, 55, 55)),
                new EmptyBorder(6, 10, 6, 10)
        ));

        // Selector de Fuentes del Sistema (Fondo Gris Oscuro y Texto Blanco)
        String[] fuentes = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> cbFuentes = new JComboBox<>(fuentes);
        cbFuentes.setSelectedItem("Consolas");
        cbFuentes.setMaximumSize(new Dimension(160, 28));
        estilizarComboBoxOscuro(cbFuentes);

        // Selector de Tamaños (Fondo Gris Oscuro y Texto Blanco)
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

        JButton btnGuardar = crearBotonPersonalizado("Guardar (.sop)", new Color(48, 48, 54), new Color(68, 70, 80));
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

        JButton btnAbrir = crearBotonPersonalizado("Abrir", new Color(48, 48, 54), new Color(68, 70, 80));
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

        topContainer.add(ribbon, BorderLayout.CENTER);

        // 3. BARRA DE ESTADO INFERIOR
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

    // Estiliza un JComboBox para forzar fondo oscuro y texto blanco (Sin cuadro blanco)
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
                    areaCmd.append(String.format("  %-16s %s\n", "dir", "Lista todos los archivos y carpetas."));
                    areaCmd.append(String.format("  %-16s %s\n", "date", "Muestra la fecha actual del sistema."));
                    areaCmd.append(String.format("  %-16s %s\n", "time", "Muestra la hora actual del sistema."));
                    areaCmd.append(String.format("  %-16s %s\n", "cls", "Limpia la pantalla de la consola."));
                    areaCmd.append(String.format("  %-16s %s\n", "help", "Muestra esta ayuda con todos los comandos."));
                    break;

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
                    areaCmd.append("'" + comando + "' no se reconoce como un comando interno. Escribe 'help' para ayuda.\n");
            }
            lblPrompt.setText(" " + dirActual[0].getPath().replace(SistemadeArchivos.RUTA_RAIZ_SIMULADA, "Z:") + "> ");
        });

        return p;
    }

    // =========================================================================
    // 4. VISOR DE FOTOS (CON BOTÓN AGREGAR IMAGEN Y ELIMINAR SIN EMOJIS)
    // =========================================================================
    private JPanel crearVisorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(24, 24, 27));

        Lista<File> listaFotos = new Lista<>();
        final int[] indexActual = {0};

        // 1. Barra Superior con Título y Botones
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

        // 2. Lienzo Central
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

        // 3. Carrete de Miniaturas Inferior
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

            if (indexActual[0] >= listaFotos.getSize()) {
                indexActual[0] = Math.max(0, listaFotos.getSize() - 1);
            }
            stripThumbnails.revalidate();
            stripThumbnails.repaint();
            mostrarFotoActual.run();
        };

        recargarFotos.run();

        // Botón Agregar Imagen
        btnAgregarFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
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
                    JOptionPane.showMessageDialog(this, "Por favor seleccione una imagen válida (.png, .jpg, .jpeg)");
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

    // =========================================================================
    // 5. REPRODUCTOR DE MÚSICA CON FORMULARIO INTEGRADO Y CARÁTULA
    // =========================================================================
    
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

    // Metadatos con Carátula Asociada
    private static class MetadataCancion implements Serializable {
        private static final long serialVersionUID = 1L;
        String autor;
        String descripcion;
        String caratulaRuta;

        public MetadataCancion(String autor, String descripcion, String caratulaRuta) {
            this.autor = autor;
            this.descripcion = descripcion;
            this.caratulaRuta = caratulaRuta;
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

    private JPanel crearReproductorReal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(32, 33, 36));

        File dirMusica = new File(SistemadeArchivos.RUTA_RAIZ_SIMULADA + "/" + usuarioActual.getUsername() + "/Música");
        HashMap<String, MetadataCancion> mapaMetadatos = cargarMetadatosMusica(dirMusica);

        // 1. SIDEBAR IZQUIERDO
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
        JButton btnAgregarAudio = crearBotonSidebarItem("Agregar Canción...", false);
        JButton btnAbrirCarpeta = crearBotonSidebarItem("Carpeta Música", false);

        navList.add(btnBiblioteca);
        navList.add(btnAgregarAudio);
        navList.add(btnAbrirCarpeta);

        sidebar.add(navList, BorderLayout.CENTER);
        p.add(sidebar, BorderLayout.WEST);

        // 2. CONTENEDOR CENTRAL CON CARDLAYOUT
        CardLayout cardsCenter = new CardLayout();
        JPanel centerCards = new JPanel(cardsCenter);
        centerCards.setOpaque(false);

        // --- TARJETA 1: BIBLIOTECA REGULAR ---
        JPanel vistaBiblioteca = new JPanel(new BorderLayout(15, 15));
        vistaBiblioteca.setBackground(new Color(32, 33, 36));
        vistaBiblioteca.setBorder(new EmptyBorder(18, 20, 10, 20));

        JLabel lblHeader = new JLabel("Música");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);
        vistaBiblioteca.add(lblHeader, BorderLayout.NORTH);

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

        // Panel de Detalles
        JPanel detailsPanel = new JPanel(new BorderLayout(12, 12));
        detailsPanel.setPreferredSize(new Dimension(280, 0));
        detailsPanel.setBackground(new Color(24, 25, 28));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(15, 15, 15, 15)
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

        vistaBiblioteca.add(splitCenter, BorderLayout.CENTER);

        // --- TARJETA 2: FORMULARIO INTEGRADO PARA AGREGAR CANCIÓN ---
        JPanel vistaAgregar = new JPanel(new BorderLayout(15, 15));
        vistaAgregar.setBackground(new Color(32, 33, 36));
        vistaAgregar.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel lblHeaderAdd = new JLabel("Agregar Nueva Canción a la Biblioteca");
        lblHeaderAdd.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderAdd.setForeground(Color.WHITE);
        vistaAgregar.add(lblHeaderAdd, BorderLayout.NORTH);

        JPanel formContent = new JPanel(new GridLayout(1, 2, 20, 0));
        formContent.setBackground(new Color(24, 25, 28));
        formContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 52, 58), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Columna Izquierda: Carátula
        JPanel colCaratula = new JPanel(new BorderLayout(10, 10));
        colCaratula.setOpaque(false);

        JLabel lblTitCaratula = new JLabel("Vista Previa de Carátula:", SwingConstants.CENTER);
        lblTitCaratula.setForeground(Color.WHITE);
        lblTitCaratula.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JLabel lblPreviewCaratula = new JLabel("Sin Carátula", SwingConstants.CENTER);
        lblPreviewCaratula.setPreferredSize(new Dimension(180, 180));
        lblPreviewCaratula.setOpaque(true);
        lblPreviewCaratula.setBackground(new Color(35, 36, 40));
        lblPreviewCaratula.setForeground(TEXT_MUTED);
        lblPreviewCaratula.setBorder(BorderFactory.createLineBorder(new Color(60, 62, 68), 1, true));

        final File[] archivoCaratulaSeleccionada = {null};
        final File[] archivoAudioSeleccionado = {null};

        JButton btnBuscarCaratula = crearBotonPersonalizado("Seleccionar Carátula (.jpg / .png)", new Color(45, 46, 52), new Color(65, 68, 78));

        btnBuscarCaratula.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
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

        // Columna Derecha: Audio, Autor y Descripción
        JPanel colDatos = new JPanel(new GridLayout(7, 1, 4, 4));
        colDatos.setOpaque(false);

        JLabel lblAudioSel = new JLabel("Ningún archivo de audio seleccionado");
        lblAudioSel.setForeground(TEXT_MUTED);
        lblAudioSel.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        JButton btnBuscarAudio = crearBotonPersonalizado("Elegir Archivo MP3 / WAV", new Color(45, 46, 52), new Color(65, 68, 78));

        btnBuscarAudio.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
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
        txtAutorInput.setBackground(new Color(36, 37, 42));
        txtAutorInput.setForeground(Color.WHITE);
        txtAutorInput.setCaretColor(Color.WHITE);
        txtAutorInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 62, 68), 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel lblTitDesc = new JLabel("Descripción breve:");
        lblTitDesc.setForeground(Color.WHITE);
        lblTitDesc.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextArea txtDescInput = new JTextArea(2, 20);
        txtDescInput.setLineWrap(true);
        txtDescInput.setWrapStyleWord(true);
        txtDescInput.setBackground(new Color(36, 37, 42));
        txtDescInput.setForeground(Color.WHITE);
        txtDescInput.setCaretColor(Color.WHITE);

        JScrollPane scrollDesc = new JScrollPane(txtDescInput);
        scrollDesc.setBorder(BorderFactory.createLineBorder(new Color(60, 62, 68), 1, true));
        scrollDesc.setBackground(new Color(36, 37, 42));
        scrollDesc.getViewport().setBackground(new Color(36, 37, 42));

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
        colDatos.add(lblTitDesc);
        colDatos.add(scrollDesc);
        colDatos.add(lblConteoPalabras);

        formContent.add(colCaratula);
        formContent.add(colDatos);
        vistaAgregar.add(formContent, BorderLayout.CENTER);

        // Botones Inferiores del Formulario
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

        // 3. BARRA INFERIOR DE REPRODUCCIÓN
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

        bottomBar.add(controlRow, BorderLayout.CENTER);
        p.add(bottomBar, BorderLayout.SOUTH);

        // Recargar Playlist y mostrar carátula
        Runnable recargarMusica = () -> {
            playlistModel.clear();
            File[] canciones = dirMusica.listFiles((dir, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".mp3") || n.endsWith(".wav");
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
                MetadataCancion meta = mapaMetadatos.get(sel.getName());
                String autor = (meta != null && !meta.autor.isEmpty()) ? meta.autor : "Desconocido";
                String desc = (meta != null && !meta.descripcion.isEmpty()) ? meta.descripcion : "Sin descripción personalizada.";

                if (meta != null && meta.caratulaRuta != null && new File(meta.caratulaRuta).exists()) {
                    ImageIcon img = new ImageIcon(meta.caratulaRuta);
                    Image scaled = img.getImage().getScaledInstance(180, 160, Image.SCALE_SMOOTH);
                    lblCaratula.setIcon(new ImageIcon(scaled));
                } else {
                    lblCaratula.setIcon(null);
                }

                txtDescripcion.setText("Título: " + sel.getName() + "\n"
                        + "Autor/Artista: " + autor + "\n\n"
                        + "Descripción:\n" + desc + "\n\n"
                        + "Formato: Audio " + obtenerExtension(sel.getName()) + "\n"
                        + "Tamaño: " + (sel.length() / 1024) + " KB\n"
                        + "Ruta: " + sel.getAbsolutePath());
            }
        });

        // Eventos del Sidebar
        btnBiblioteca.addActionListener(e -> cardsCenter.show(centerCards, "BIBLIOTECA"));

        btnAgregarAudio.addActionListener(e -> {
            archivoAudioSeleccionado[0] = null;
            archivoCaratulaSeleccionada[0] = null;
            lblAudioSel.setText("Ningún archivo de audio seleccionado");
            lblAudioSel.setForeground(TEXT_MUTED);
            lblPreviewCaratula.setIcon(null);
            lblPreviewCaratula.setText("Sin Carátula");
            txtAutorInput.setText("");
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

                String rutaCaratula = null;
                if (archivoCaratulaSeleccionada[0] != null) {
                    String extImg = obtenerExtension(archivoCaratulaSeleccionada[0].getName()).toLowerCase();
                    File caratulaDest = new File(dirMusica, archivoAudioSeleccionado[0].getName() + "_cover." + extImg);
                    Files.copy(archivoCaratulaSeleccionada[0].toPath(), caratulaDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    rutaCaratula = caratulaDest.getAbsolutePath();
                }

                String autor = txtAutorInput.getText().trim();
                mapaMetadatos.put(audioDest.getName(), new MetadataCancion(autor, desc, rutaCaratula));
                guardarMetadatosMusica(dirMusica, mapaMetadatos);

                recargarMusica.run();
                cardsCenter.show(centerCards, "BIBLIOTECA");
                JOptionPane.showMessageDialog(this, "¡Canción agregada con éxito a la biblioteca!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar canción: " + ex.getMessage());
            }
        });

        btnAbrirCarpeta.addActionListener(e -> abrirExplorador(dirMusica));

        // Hilo de Tiempo
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
                        int tot = motorAudio.getDuracionTotalSegundos();
                        lblTimeTotal.setText(String.format("%02d:%02d", tot / 60, tot % 60));
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
            int total = playlistModel.getSize();
            if (total > 0) {
                int idx = playlist.getSelectedIndex();
                int anterior = (idx - 1 + total) % total;
                playlist.setSelectedIndex(anterior);
                if (motorAudio.estaReproduciendo()) {
                    motorAudio.detener();
                }
                btnPlay.doClick();
            }
        });

        btnNext.addActionListener(e -> {
            int total = playlistModel.getSize();
            if (total > 0) {
                int idx = playlist.getSelectedIndex();
                int siguiente = (idx + 1) % total;
                playlist.setSelectedIndex(siguiente);
                if (motorAudio.estaReproduciendo()) {
                    motorAudio.detener();
                }
                btnPlay.doClick();
            }
        });

        return p;
    }

    // Botones del Sidebar
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

    // Botones Vectoriales
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