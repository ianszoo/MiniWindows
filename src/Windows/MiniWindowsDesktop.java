/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

/**
 *
 * @author David Suazo Palao
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MiniWindowsDesktop extends JFrame {
    private JDesktopPane desktopPane;
    private Image backgroundImage;
    private JPanel startMenu;
    private boolean startMenuVisible = false;

    private final Color TASKBAR_COLOR = new Color(14, 28, 54, 240);
    private final Color START_MENU_BG = new Color(18, 36, 68, 245);
    private final Color SEARCH_BAR_BG = new Color(28, 50, 88, 200);
    private final Color HOVER_COLOR    = new Color(255, 255, 255, 35);
    private final Color TEXT_WHITE     = new Color(240, 240, 245);

    public MiniWindowsDesktop() {
        setTitle("Mini-Windows OS - Unidad Z:\\");
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

    private void cargarFondo() {
        String[] posiblesRutas = {
            "imagenes/windows_background.jpg",
            "imagenes/windows_background.png",
            "imagenes/windows_background.jpeg",
            "src/imagenes/windows_background.jpg",
            "src/imagenes/windows_background.png",
            "windows_background/windows_background.jpg",
            "windows_background/windows_background.png",
            "windows_background.jpg",
            "windows_background.png"
        };

        // 1. Intentar cargar desde el sistema de archivos
        for (String ruta : posiblesRutas) {
            File f = new File(ruta);
            if (f.exists()) {
                backgroundImage = new ImageIcon(f.getAbsolutePath()).getImage();
                return;
            }
        }

        // 2. Intentar cargar desde los recursos internos del JAR/Classpath
        String[] posiblesRecursos = {
            "/imagenes/windows_background.jpg",
            "/imagenes/windows_background.png",
            "/Windows/windows_background.jpg",
            "/Windows/windows_background.png",
            "/windows_background.jpg"
        };

        for (String recurso : posiblesRecursos) {
            java.net.URL url = getClass().getResource(recurso);
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
                return;
            }
        }
    }

    private void crearIconosEscritorio() {
        int x = 15;
        int y = 15;
        int gap = 82;

        desktopPane.add(crearIconoEscritorio("🖥️", "Este equipo", x, y, () -> abrirVentana(crearExplorador(), "Este equipo (Z:\\)")));
        desktopPane.add(crearIconoEscritorio("📁", "Documentos", x, y += gap, () -> abrirVentana(crearExplorador(), "Z:\\admin\\Mis Documentos")));
        desktopPane.add(crearIconoEscritorio("🗑️", "Papelera de\nreciclaje", x, y += gap, () -> JOptionPane.showMessageDialog(this, "La papelera está vacía.")));
        desktopPane.add(crearIconoEscritorio("⚙️", "Configuración", x, y += gap, () -> abrirVentana(new InstaPanel(), "INSTA+ / Configuración")));
        desktopPane.add(crearIconoEscritorio("❓", "Ayuda", x, y += gap, () -> JOptionPane.showMessageDialog(this, "Mini-Windows OS v1.0\nDesarrollado para Programación II")));
    }

    private JPanel crearIconoEscritorio(String emoji, String texto, int x, int y, Runnable accion) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBounds(x, y, 85, 75);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel lbl = new JLabel("<html><center>" + texto.replace("\n", "<br>") + "</center></html>", SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(icon, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { accion.run(); }
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setOpaque(true);
                p.setBackground(new Color(255, 255, 255, 45));
                p.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                p.setOpaque(false);
                p.repaint();
            }
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
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        startMenu.setOpaque(false);
        startMenu.setLayout(new BorderLayout(10, 10));
        startMenu.setBorder(new EmptyBorder(15, 15, 15, 15));
        startMenu.setSize(380, 430);
        startMenu.setVisible(false);

        JPanel grid = new JPanel(new GridLayout(1, 2, 10, 0));
        grid.setOpaque(false);

        JPanel colLeft = new JPanel(new GridLayout(8, 1, 2, 2));
        colLeft.setOpaque(false);

        colLeft.add(crearBotonMenu("👤", "Usuario (Admin)", null, true));
        colLeft.add(crearBotonMenu("📁", "Explorador", () -> abrirVentana(crearExplorador(), "Explorador (Z:\\)"), false));
        colLeft.add(crearBotonMenu("📝", "Editor TXT", () -> abrirVentana(crearEditor(), "Editor TXT"), false));
        colLeft.add(crearBotonMenu("🖼️", "Visor Fotos", () -> abrirVentana(crearVisor(), "Visor Fotos"), false));
        colLeft.add(crearBotonMenu("💻", "Consola CMD", () -> abrirVentana(crearCmd(), "CMD"), false));
        colLeft.add(crearBotonMenu("🎵", "Reproductor", () -> abrirVentana(crearReproductor(), "Reproductor MP3"), false));
        colLeft.add(crearBotonMenu("📸", "INSTA+", () -> abrirVentana(new InstaPanel(), "INSTA+"), false));
        colLeft.add(crearBotonMenu("⭕", "Apagar", () -> System.exit(0), false));

        JPanel colRight = new JPanel(new GridLayout(8, 1, 2, 2));
        colRight.setOpaque(false);

        JLabel lblAccesos = new JLabel("Accesos");
        lblAccesos.setForeground(new Color(180, 200, 220));
        lblAccesos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        colRight.add(lblAccesos);

        colRight.add(crearBotonMenu("📁", "Documentos", () -> abrirVentana(crearExplorador(), "Z:\\admin\\Mis Documentos"), false));
        colRight.add(crearBotonMenu("🖼️", "Imágenes", () -> abrirVentana(crearVisor(), "Z:\\admin\\Mis Imágenes"), false));
        colRight.add(crearBotonMenu("🎵", "Música", () -> abrirVentana(crearReproductor(), "Z:\\admin\\Música"), false));
        colRight.add(crearBotonMenu("📥", "Descargas", () -> abrirVentana(crearExplorador(), "Z:\\admin\\Descargas"), false));

        grid.add(colLeft);
        grid.add(colRight);
        startMenu.add(grid, BorderLayout.CENTER);

        desktopPane.add(startMenu, JLayeredPane.POPUP_LAYER);
    }

    private JButton crearBotonMenu(String emoji, String texto, Runnable accion, boolean isHeader) {
        JButton btn = new JButton(emoji + "  " + texto);
        btn.setFont(new Font("Segoe UI", isHeader ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
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
        btnStart.setPreferredSize(new Dimension(42, 36));
        btnStart.setFocusPainted(false);
        btnStart.setBorder(BorderFactory.createEmptyBorder());
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.addActionListener(e -> toggleStartMenu());

        JTextField searchBar = new JTextField("  🔍 Buscar...");
        searchBar.setPreferredSize(new Dimension(190, 34));
        searchBar.setBackground(SEARCH_BAR_BG);
        searchBar.setForeground(new Color(200, 220, 240));
        searchBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchBar.setCaretColor(Color.WHITE);
        searchBar.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 30), 1, true));

        left.add(btnStart);
        left.add(searchBar);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        center.setOpaque(false);

        center.add(crearBotonBarra("📁", () -> abrirVentana(crearExplorador(), "Explorador de Archivos")));
        center.add(crearBotonBarra("📝", () -> abrirVentana(crearEditor(), "Editor de Texto")));
        center.add(crearBotonBarra("🖼️", () -> abrirVentana(crearVisor(), "Visor de Fotos")));
        center.add(crearBotonBarra("💻", () -> abrirVentana(crearCmd(), "Consola CMD")));
        center.add(crearBotonBarra("▶️", () -> abrirVentana(crearReproductor(), "Reproductor MP3")));
        center.add(crearBotonBarra("📸", () -> abrirVentana(new InstaPanel(), "INSTA+")));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        right.setOpaque(false);

        JLabel lblIcons = new JLabel("📶   🔊   🔋");
        lblIcons.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        lblIcons.setForeground(Color.WHITE);

        JLabel lblTimeDate = new JLabel("<html><center>11:30 AM<br><small>31/05/2026</small></center></html>");
        lblTimeDate.setForeground(Color.WHITE);
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        Timer clockTimer = new Timer(1000, e -> {
            String time = new SimpleDateFormat("hh:mm a").format(new Date());
            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            lblTimeDate.setText("<html><center>" + time + "<br><span style='font-size:9px; color:#cccccc;'>" + date + "</span></center></html>");
        });
        clockTimer.start();

        right.add(lblIcons);
        right.add(lblTimeDate);

        taskBar.add(left, BorderLayout.WEST);
        taskBar.add(center, BorderLayout.CENTER);
        taskBar.add(right, BorderLayout.EAST);

        add(taskBar, BorderLayout.SOUTH);
    }

    private JButton crearBotonBarra(String emoji, Runnable accion) {
        JButton btn = new JButton(emoji);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(40, 36));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(HOVER_COLOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });

        btn.addActionListener(e -> accion.run());
        return btn;
    }

    private void abrirVentana(JComponent content, String titulo) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        frame.setContentPane(content);
        frame.setSize(780, 500);
        frame.setVisible(true);
        desktopPane.add(frame);
        try { frame.setSelected(true); } catch (Exception ignored) {}
    }


    private JPanel crearExplorador() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Z:\\ (Unidad Virtual)");
        DefaultMutableTreeNode user = new DefaultMutableTreeNode("admin");
        user.add(new DefaultMutableTreeNode("Mis Documentos"));
        user.add(new DefaultMutableTreeNode("Música"));
        user.add(new DefaultMutableTreeNode("Mis Imágenes"));
        root.add(user);

        JTree tree = new JTree(new DefaultTreeModel(root));
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(220, 0));
        p.add(treeScroll, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JButton("Organizar"));
        toolbar.add(new JButton("Nuevo Archivo"));
        toolbar.add(new JButton("Nueva Carpeta"));
        toolbar.add(new JButton("Eliminar"));
        p.add(toolbar, BorderLayout.NORTH);

        JList<String> fileList = new JList<>(new String[]{
            "📄 notas.txt (12 KB)", "🎵 cancion.mp3 (3.4 MB)", "🖼️ foto.png (540 KB)"
        });
        p.add(new JScrollPane(fileList), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearEditor() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Fuente:"));
        toolbar.add(new JComboBox<>(new String[]{"Arial", "Consolas", "Segoe UI"}));
        toolbar.add(new JLabel("Tamaño:"));
        toolbar.add(new JComboBox<>(new String[]{"12", "16", "20", "24"}));
        toolbar.add(new JButton("🎨 Color"));
        toolbar.add(new JButton("💾 Guardar"));
        p.add(toolbar, BorderLayout.NORTH);

        JTextPane textPane = new JTextPane();
        textPane.setText("Bienvenido al editor de texto con formato de Mini-Windows.");
        p.add(new JScrollPane(textPane), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearCmd() {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea txtCmd = new JTextArea(
            "Microsoft Mini-Windows [Versión 1.0]\n" +
            "(c) 2026 Mini-Windows. Todos los derechos reservados.\n\n" +
            "Z:\\admin> dir\n" +
            " <DIR>          Mis Documentos\n" +
            " <DIR>          Música\n" +
            " <DIR>          Mis Imágenes\n\n" +
            "Z:\\admin> "
        );
        txtCmd.setBackground(Color.BLACK);
        txtCmd.setForeground(Color.GREEN);
        txtCmd.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtCmd.setCaretColor(Color.WHITE);
        p.add(new JScrollPane(txtCmd), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearVisor() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lblImg = new JLabel("🖼️ [ Vista previa: Z:\\admin\\Mis Imágenes\\wallpaper.jpg ]", SwingConstants.CENTER);
        lblImg.setFont(new Font("Segoe UI", Font.ITALIC, 14));

        JPanel nav = new JPanel(new FlowLayout());
        nav.add(new JButton("◀ Anterior"));
        nav.add(new JLabel(" 1 / 3 "));
        nav.add(new JButton("Siguiente ▶"));

        p.add(lblImg, BorderLayout.CENTER);
        p.add(nav, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearReproductor() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblCover = new JLabel("🎵 [ Carátula ]", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(140, 140));
        lblCover.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(lblCover, BorderLayout.WEST);

        JPanel details = new JPanel(new GridLayout(3, 1));
        details.add(new JLabel("Título: Pista 01"));
        details.add(new JLabel("Artista: Artista Desconocido"));
        details.add(new JLabel("Ruta: Z:\\admin\\Música\\audio.mp3"));
        p.add(details, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JSlider(0, 100, 25), BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout());
        controls.add(new JButton("▶ Play"));
        controls.add(new JButton("⏸ Pause"));
        controls.add(new JButton("⏹ Stop"));
        bottom.add(controls, BorderLayout.SOUTH);

        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    //*(public static void main(String[] args) {
      //  try {
        //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       // } catch (Exception ignored) {}

      //  SwingUtilities.invokeLater(() -> new MiniWindowsDesktop().setVisible(true));
  //  }
}
