/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Insta;

/**
 *
 * @author David Suazo Palao
 */
import Windows.Lista;
import Windows.Nodo;
import Windows.Usuario;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
public class InstaPanel extends JPanel  {
    private final Usuario usuarioActual;
    private CardLayout cardLayout;
    private JPanel mainContent;

    // Paleta oficial de Instagram Dark Mode
    public static final Color IG_BLACK      = new Color(0, 0, 0);
    public static final Color IG_SURFACE    = new Color(18, 18, 18);
    public static final Color IG_CARD_BG    = new Color(26, 26, 26);
    public static final Color IG_BORDER     = new Color(38, 38, 38);
    public static final Color IG_HOVER      = new Color(32, 32, 32);
    public static final Color IG_BLUE       = new Color(0, 149, 246);
    public static final Color IG_PINK_RED   = new Color(255, 48, 64);
    public static final Color IG_TEXT_WHITE = new Color(245, 245, 245);
    public static final Color IG_TEXT_MUTED = new Color(168, 168, 168);

    // Gradiente oficial de Stories
    private static final Color GRAD_ORANGE = new Color(245, 133, 41);
    private static final Color GRAD_PINK   = new Color(221, 42, 123);
    private static final Color GRAD_PURPLE = new Color(129, 52, 175);

    // Componentes para chat en vivo
    private JPanel pnlMensajesStream;
    private JScrollPane scrollChatStream;
    private JComboBox<String> cbDestinatarioChat;
    private volatile boolean hiloChatActivo = true;

    public InstaPanel(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        InstaFileManager.inicializarInsta();
        InstaFileManager.crearEspacioUsuarioInsta(usuarioActual.getUsername());

        setLayout(new BorderLayout());
        setBackground(IG_BLACK);

        crearSidebarModerna();
        crearContenedorCentral();
        iniciarHiloSincronizacionChat();
    }

    // =========================================================================
    // BARRA LATERAL IZQUIERDA (Estilo Instagram Web / iPad)
    // =========================================================================
    private void crearSidebarModerna() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(IG_BLACK);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, IG_BORDER));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));

        // Logo Instagram
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setOpaque(false);

        ImageIcon igIcon = MiniWindowsDesktop.cargarIcono("instagram_icon", 28, 28);
        JLabel lblLogoIcon = new JLabel(igIcon != null ? igIcon : new JLabel("📸").getIcon());

        JLabel lblLogo = new JLabel(" Instagram");
        lblLogo.setFont(new Font("Segoe Script", Font.BOLD, 22));
        lblLogo.setForeground(IG_TEXT_WHITE);

        logoPanel.add(lblLogoIcon);
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);
        

        // Menú de navegación
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 0, 4));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        menuPanel.add(crearBotonNav("Inicio", "TIMELINE", "🏠"));
        menuPanel.add(crearBotonNav("Buscar", "SEARCH_PROFILE", "🔍"));
        menuPanel.add(crearBotonNav("Hashtags", "SEARCH_TAG", "#"));
        menuPanel.add(crearBotonNav("Mensajes", "INBOX", "✉"));
        menuPanel.add(crearBotonNav("Menciones", "MENCIONES", "🔔"));
        menuPanel.add(crearBotonNav("Crear", "UPLOAD", "➕"));
        menuPanel.add(crearBotonNav("Perfil", "PERFIL", "👤"));
        menuPanel.add(crearBotonNav("Ajustes", "EDIT_PROFILE", "⚙"));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Pie de barra con info de usuario
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        footer.setOpaque(false);
        JLabel lblUser = new JLabel("@" + usuarioActual.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(IG_TEXT_MUTED);
        footer.add(lblUser);
        sidebar.add(footer, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    private JButton crearBotonNav(String texto, String cardName, String iconSymbol) {
        JButton btn = new JButton("   " + iconSymbol + "   " + texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(IG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(IG_TEXT_WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(196, 44));

        btn.addActionListener(e -> {
            cardLayout.show(mainContent, cardName);
            if (cardName.equals("TIMELINE")) recargarTimeline();
            if (cardName.equals("PERFIL")) recargarPerfil();
            if (cardName.equals("INBOX")) recargarChat();
        });
        return btn;
    }

    private void crearContenedorCentral() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(IG_BLACK);

        mainContent.add(crearVistaTimeline(), "TIMELINE");
        mainContent.add(crearVistaPerfil(), "PERFIL");
        mainContent.add(crearVistaUpload(), "UPLOAD");
        mainContent.add(crearVistaMenciones(), "MENCIONES");
        mainContent.add(crearVistaBuscarPerfil(), "SEARCH_PROFILE");
        mainContent.add(crearVistaBuscarTag(), "SEARCH_TAG");
        mainContent.add(crearVistaInbox(), "INBOX");
        mainContent.add(crearVistaEditarPerfil(), "EDIT_PROFILE");

        add(mainContent, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. TIMELINE + HISTORIAS REALES CON GRADIENTE
    // =========================================================================
    private JPanel panelPostsStream;
    private JPanel panelStoriesScroll;

    private JPanel crearVistaTimeline() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(IG_BLACK);

        // Barra superior de Stories
        panelStoriesScroll = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        panelStoriesScroll.setBackground(IG_BLACK);
        panelStoriesScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, IG_BORDER));

        JScrollPane scrollStories = new JScrollPane(panelStoriesScroll);
        scrollStories.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStories.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollStories.setBorder(null);
        scrollStories.setPreferredSize(new Dimension(getWidth(), 115));
        scrollStories.getHorizontalScrollBar().setUnitIncrement(12);

        contenedor.add(scrollStories, BorderLayout.NORTH);

        // Feed de publicaciones centrado
        panelPostsStream = new JPanel();
        panelPostsStream.setLayout(new BoxLayout(panelPostsStream, BoxLayout.Y_AXIS));
        panelPostsStream.setBackground(IG_BLACK);
        panelPostsStream.setBorder(new EmptyBorder(15, 0, 30, 0));

        JPanel wrapperCentrado = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperCentrado.setBackground(IG_BLACK);
        wrapperCentrado.add(panelPostsStream);

        JScrollPane scrollFeed = new JScrollPane(wrapperCentrado);
        scrollFeed.setBorder(null);
        scrollFeed.getVerticalScrollBar().setUnitIncrement(18);

        contenedor.add(scrollFeed, BorderLayout.CENTER);
        recargarTimeline();
        return contenedor;
    }

    private void recargarTimeline() {
        if (panelPostsStream == null || panelStoriesScroll == null) return;
        panelPostsStream.removeAll();
        panelStoriesScroll.removeAll();

        // 1. Agregar botón para crear historia propia
        panelStoriesScroll.add(crearBotonAgregarHistoria());

        Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
        Lista<String> autores = new Lista<>();
        autores.agregar(usuarioActual.getUsername());

        Nodo<String> ns = seguidos.getHead();
        while (ns != null) {
            autores.agregar(ns.getDato());
            ns = ns.getSiguiente();
        }

        // 2. Renderizar Historias de autores
        Nodo<String> na = autores.getHead();
        while (na != null) {
            String autor = na.getDato();
            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(autor);
            Nodo<Publicacion> np = posts.getHead();
            while (np != null) {
                Publicacion p = np.getDato();
                if (p.isEsHistoria()) {
                    panelStoriesScroll.add(crearBurbujaHistoriaInstagram(p));
                }
                np = np.getSiguiente();
            }
            na = na.getSiguiente();
        }

        // 3. Renderizar Posts de Feed
        na = autores.getHead();
        int totalPosts = 0;
        while (na != null) {
            String autor = na.getDato();
            Usuario u = InstaFileManager.buscarUsuario(autor);
            if (u != null && u.isActivo()) {
                Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(autor);
                Nodo<Publicacion> np = posts.getHead();
                while (np != null) {
                    Publicacion p = np.getDato();
                    if (!p.isEsHistoria()) {
                        panelPostsStream.add(crearTarjetaPostInstagram(p));
                        panelPostsStream.add(Box.createVerticalStrut(20));
                        totalPosts++;
                    }
                    np = np.getSiguiente();
                }
            }
            na = na.getSiguiente();
        }

        if (totalPosts == 0) {
            JPanel pnlEmpty = new JPanel(new GridLayout(2, 1));
            pnlEmpty.setOpaque(false);
            pnlEmpty.setBorder(new EmptyBorder(50, 20, 20, 20));
            JLabel l1 = new JLabel("No hay publicaciones en tu feed", SwingConstants.CENTER);
            l1.setFont(new Font("Segoe UI", Font.BOLD, 16));
            l1.setForeground(IG_TEXT_WHITE);
            JLabel l2 = new JLabel("Sigue a otras personas desde el buscador para ver sus fotos.", SwingConstants.CENTER);
            l2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l2.setForeground(IG_TEXT_MUTED);
            pnlEmpty.add(l1);
            pnlEmpty.add(l2);
            panelPostsStream.add(pnlEmpty);
        }

        panelStoriesScroll.revalidate();
        panelStoriesScroll.repaint();
        panelPostsStream.revalidate();
        panelPostsStream.repaint();
    }

    private JPanel crearBotonAgregarHistoria() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JComponent avatarCircle = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(IG_CARD_BG);
                g2.fillOval(4, 4, 56, 56);
                g2.setColor(IG_BLUE);
                g2.fillOval(42, 42, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2.drawString("+", 47, 56);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString(usuarioActual.getUsername().substring(0, 1).toUpperCase(), 25, 39);
                g2.dispose();
            }
        };
        avatarCircle.setPreferredSize(new Dimension(64, 64));

        JLabel lbl = new JLabel("Tu historia", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(IG_TEXT_WHITE);

        panel.add(avatarCircle, BorderLayout.CENTER);
        panel.add(lbl, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainContent, "UPLOAD");
            }
        });
        return panel;
    }

    private JPanel crearBurbujaHistoriaInstagram(Publicacion story) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JComponent ringAvatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Anillo de gradiente oficial
                GradientPaint gp = new GradientPaint(0, 0, GRAD_ORANGE, 64, 64, GRAD_PURPLE);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2.6f));
                g2.drawOval(2, 2, 60, 60);

                // Círculo interior
                g2.setColor(IG_BLACK);
                g2.fillOval(5, 5, 54, 54);

                g2.setColor(new Color(50, 50, 55));
                g2.fillOval(8, 8, 48, 48);

                // Inicial del autor
                g2.setColor(IG_TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                String letra = story.getAutor().substring(0, 1).toUpperCase();
                g2.drawString(letra, 26, 39);
                g2.dispose();
            }
        };
        ringAvatar.setPreferredSize(new Dimension(64, 64));

        JLabel lbl = new JLabel(story.getAutor(), SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(IG_TEXT_WHITE);

        panel.add(ringAvatar, BorderLayout.CENTER);
        panel.add(lbl, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirVisorHistoria(story);
            }
        });
        return panel;
    }

    private void abrirVisorHistoria(Publicacion story) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Historia de @" + story.getAutor(), true);
        dlg.setSize(380, 560);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(IG_BLACK);
        dlg.setLayout(new BorderLayout());

        // Barra de progreso superior
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(100);
        progress.setForeground(Color.WHITE);
        progress.setBackground(new Color(70, 70, 70));
        progress.setPreferredSize(new Dimension(380, 4));
        progress.setBorder(null);
        dlg.add(progress, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(IG_BLACK);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblUser = new JLabel("<html><b>@" + story.getAutor() + "</b> <span style='color:#888;'>• hace un momento</span></html>");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(IG_TEXT_WHITE);
        center.add(lblUser, BorderLayout.NORTH);

        JLabel lblTxt = new JLabel("<html><body style='width:280px; text-align:center; color:#ffffff; font-size:16px;'>"
                + story.getContenido() + "</body></html>", SwingConstants.CENTER);
        center.add(lblTxt, BorderLayout.CENTER);

        if (story.getSticker() != null) {
            JLabel lblSticker = new JLabel(story.getSticker(), SwingConstants.CENTER);
            lblSticker.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            center.add(lblSticker, BorderLayout.SOUTH);
        }

        dlg.add(center, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private JPanel crearTarjetaPostInstagram(Publicacion p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(IG_BLACK);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IG_BORDER, 1),
                new EmptyBorder(12, 14, 14, 14)
        ));
        card.setPreferredSize(new Dimension(470, p.getRutaImagen() != null ? 530 : 260));

        // 1. Header (Avatar, Username, Timestamp)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblUser = new JLabel(" @" + p.getAutor());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(IG_TEXT_WHITE);

        String timeAgo = new SimpleDateFormat("dd MMM • hh:mm a").format(p.getFecha());
        JLabel lblTime = new JLabel(timeAgo);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(IG_TEXT_MUTED);

        header.add(lblUser, BorderLayout.WEST);
        header.add(lblTime, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // 2. Body (Foto o contenedor gráfico)
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 10, 0));

        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image img = icon.getImage().getScaledInstance(440, 290, Image.SCALE_SMOOTH);
            JLabel lblImg = new JLabel(new ImageIcon(img));
            body.add(lblImg, BorderLayout.CENTER);
        }

        card.add(body, BorderLayout.CENTER);

        // 3. Footer (Botones Like/Comment + Pie de foto)
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        // Barra de acciones
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsBar.setOpaque(false);

        JButton btnLike = new JButton("❤");
        btnLike.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLike.setForeground(IG_TEXT_WHITE);
        btnLike.setContentAreaFilled(false);
        btnLike.setBorderPainted(false);
        btnLike.setFocusPainted(false);
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLike.addActionListener(e -> {
            if (btnLike.getForeground().equals(IG_PINK_RED)) {
                btnLike.setForeground(IG_TEXT_WHITE);
            } else {
                btnLike.setForeground(IG_PINK_RED);
            }
        });

        JLabel lblComment = new JLabel("💬");
        lblComment.setForeground(IG_TEXT_WHITE);
        lblComment.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        actionsBar.add(btnLike);
        actionsBar.add(lblComment);
        footer.add(actionsBar);
        footer.add(Box.createVerticalStrut(6));

        // Descripción con hashtags coloreados
        String texto = p.getContenido().replaceAll("(#[\\w]+)", "<span style='color:#0095f6;'>$1</span>");
        texto = texto.replaceAll("(@[\\w]+)", "<span style='color:#e0e0e0; font-weight:bold;'>$1</span>");

        JLabel lblCaption = new JLabel("<html><body style='width:430px; color:#f0f0f0; font-size:12px; font-family:Segoe UI;'>"
                + "<b>@" + p.getAutor() + "</b> " + texto + "</body></html>");
        footer.add(lblCaption);

        if (p.getSticker() != null) {
            JLabel lblSt = new JLabel("Sticker: " + p.getSticker());
            lblSt.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblSt.setForeground(GRAD_PINK);
            footer.add(Box.createVerticalStrut(4));
            footer.add(lblSt);
        }

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. PERFIL DE USUARIO ESTILO INSTAGRAM
    // =========================================================================
    private JPanel pnlPerfilInfo;
    private JPanel pnlPerfilGrid;

    private JPanel crearVistaPerfil() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(IG_BLACK);

        pnlPerfilInfo = new JPanel(new BorderLayout());
        pnlPerfilInfo.setBackground(IG_BLACK);
        pnlPerfilInfo.setBorder(new EmptyBorder(30, 40, 20, 40));

        pnlPerfilGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        pnlPerfilGrid.setBackground(IG_BLACK);
        pnlPerfilGrid.setBorder(new EmptyBorder(20, 40, 40, 40));

        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(IG_BLACK);
        contenedor.add(pnlPerfilInfo);
        contenedor.add(pnlPerfilGrid);

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        recargarPerfil();
        return panel;
    }

    private void recargarPerfil() {
        if (pnlPerfilInfo == null || pnlPerfilGrid == null) return;
        pnlPerfilInfo.removeAll();
        pnlPerfilGrid.removeAll();

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        Lista<String> followers = InstaFileManager.cargarSeguidores(u.getUsername());
        Lista<String> following = InstaFileManager.cargarSeguidos(u.getUsername());
        Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());

        // Cabecera del perfil: Avatar a la izquierda, Estadísticas a la derecha
        JPanel topHeader = new JPanel(new BorderLayout(30, 0));
        topHeader.setOpaque(false);

        // Avatar gigante
        JComponent bigAvatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(IG_CARD_BG);
                g2.fillOval(0, 0, 90, 90);
                g2.setColor(IG_TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
                g2.drawString(usuarioActual.getUsername().substring(0, 1).toUpperCase(), 32, 58);
                g2.dispose();
            }
        };
        bigAvatar.setPreferredSize(new Dimension(90, 90));
        topHeader.add(bigAvatar, BorderLayout.WEST);

        // Datos y contadores
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        statsPanel.setOpaque(false);

        JLabel lblUsername = new JLabel("@" + u.getUsername() + "   " + (u.isActivo() ? "🟢" : "🔴"));
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUsername.setForeground(IG_TEXT_WHITE);

        JPanel countsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        countsRow.setOpaque(false);
        countsRow.add(crearContadorLabel(posts.getSize() + "", "publicaciones"));
        countsRow.add(crearContadorLabel(followers.getSize() + "", "seguidores"));
        countsRow.add(crearContadorLabel(following.getSize() + "", "seguidos"));

        JLabel lblBio = new JLabel("<html><b>" + u.getNombreCompleto() + "</b><br><span style='color:#8e8e8e;'>"
                + u.getEdad() + " años • Género: " + u.getGenero() + "</span></html>");
        lblBio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBio.setForeground(IG_TEXT_WHITE);

        statsPanel.add(lblUsername);
        statsPanel.add(countsRow);
        statsPanel.add(lblBio);

        topHeader.add(statsPanel, BorderLayout.CENTER);
        pnlPerfilInfo.add(topHeader, BorderLayout.CENTER);

        // Cuadrícula de fotos (Grid)
        Nodo<Publicacion> n = posts.getHead();
        while (n != null) {
            Publicacion pub = n.getDato();
            JPanel gridItem = new JPanel(new BorderLayout());
            gridItem.setBackground(IG_CARD_BG);
            gridItem.setPreferredSize(new Dimension(140, 140));
            gridItem.setBorder(BorderFactory.createLineBorder(IG_BORDER));

            if (pub.getRutaImagen() != null && new File(pub.getRutaImagen()).exists()) {
                ImageIcon icon = new ImageIcon(pub.getRutaImagen());
                Image scaled = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                gridItem.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
            } else {
                JLabel lbl = new JLabel("<html><center style='color:#aaa; font-size:10px; padding:10px;'>"
                        + pub.getContenido() + "</center></html>", SwingConstants.CENTER);
                gridItem.add(lbl, BorderLayout.CENTER);
            }
            pnlPerfilGrid.add(gridItem);
            n = n.getSiguiente();
        }

        pnlPerfilInfo.revalidate();
        pnlPerfilInfo.repaint();
        pnlPerfilGrid.revalidate();
        pnlPerfilGrid.repaint();
    }

    private JLabel crearContadorLabel(String numero, String texto) {
        JLabel l = new JLabel("<html><b>" + numero + "</b> <span style='color:#a8a8a8;'>" + texto + "</span></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(IG_TEXT_WHITE);
        return l;
    }

    // =========================================================================
    // 3. INBOX: DIRECT MESSAGES EN TIEMPO REAL
    // =========================================================================
    private JPanel crearVistaInbox() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(IG_BLACK);

        // Panel Izquierdo: Lista de Chats
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(IG_BLACK);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, IG_BORDER));
        leftPanel.setPreferredSize(new Dimension(240, getHeight()));

        JPanel topUser = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topUser.setOpaque(false);
        JLabel lblMyUser = new JLabel("💬 " + usuarioActual.getUsername());
        lblMyUser.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMyUser.setForeground(IG_TEXT_WHITE);
        topUser.add(lblMyUser);
        leftPanel.add(topUser, BorderLayout.NORTH);

        cbDestinatarioChat = new JComboBox<>();
        cbDestinatarioChat.setBackground(IG_CARD_BG);
        cbDestinatarioChat.setForeground(IG_TEXT_WHITE);
        cbDestinatarioChat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        actualizarComboUsuarios();
        leftPanel.add(cbDestinatarioChat, BorderLayout.CENTER);

        root.add(leftPanel, BorderLayout.WEST);

        // Panel Derecho: Conversación y Burbujas
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(IG_BLACK);

        pnlMensajesStream = new JPanel();
        pnlMensajesStream.setLayout(new BoxLayout(pnlMensajesStream, BoxLayout.Y_AXIS));
        pnlMensajesStream.setBackground(IG_BLACK);
        pnlMensajesStream.setBorder(new EmptyBorder(15, 20, 15, 20));

        scrollChatStream = new JScrollPane(pnlMensajesStream);
        scrollChatStream.setBorder(null);
        scrollChatStream.getVerticalScrollBar().setUnitIncrement(14);
        chatPanel.add(scrollChatStream, BorderLayout.CENTER);

        // Barra de Envío inferior (Estilo píldora)
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(IG_BLACK);
        inputBar.setBorder(new EmptyBorder(12, 20, 15, 20));

        JTextField txtMsg = new JTextField();
        txtMsg.setBackground(IG_CARD_BG);
        txtMsg.setForeground(IG_TEXT_WHITE);
        txtMsg.setCaretColor(Color.WHITE);
        txtMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMsg.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IG_BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);

        JButton btnSticker = new JButton("🌟");
        btnSticker.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnSticker.setForeground(IG_TEXT_WHITE);
        btnSticker.setContentAreaFilled(false);
        btnSticker.setBorderPainted(false);
        btnSticker.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnSend = new JButton("Enviar");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setForeground(IG_BLUE);
        btnSend.setContentAreaFilled(false);
        btnSend.setBorderPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btns.add(btnSticker);
        btns.add(btnSend);

        inputBar.add(txtMsg, BorderLayout.CENTER);
        inputBar.add(btns, BorderLayout.EAST);
        chatPanel.add(inputBar, BorderLayout.SOUTH);

        root.add(chatPanel, BorderLayout.CENTER);

        cbDestinatarioChat.addActionListener(e -> recargarChat());

        ActionListener accionEnviar = e -> {
            String destino = (String) cbDestinatarioChat.getSelectedItem();
            String txt = txtMsg.getText().trim();
            if (destino != null && !txt.isEmpty()) {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), destino, txt, MensajeInbox.Tipo.TEXTO);
                txtMsg.setText("");
                recargarChat();
            }
        };
        btnSend.addActionListener(accionEnviar);
        txtMsg.addActionListener(accionEnviar);

        btnSticker.addActionListener(e -> {
            String destino = (String) cbDestinatarioChat.getSelectedItem();
            if (destino == null) return;
            String[] stickers = {"😊 Feliz", "😢 Triste", "❤️ Corazón", "😂 Risa", "👏 Aplauso", "🔥 Fuego", "✨ Estrella"};
            String sel = (String) JOptionPane.showInputDialog(this, "Selecciona un Sticker:", "Enviar Sticker",
                    JOptionPane.PLAIN_MESSAGE, null, stickers, stickers[0]);
            if (sel != null) {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), destino, sel, MensajeInbox.Tipo.STICKER);
                recargarChat();
            }
        });

        return root;
    }

    private void actualizarComboUsuarios() {
        if (cbDestinatarioChat == null) return;
        cbDestinatarioChat.removeAllItems();
        Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
        Nodo<Usuario> n = todos.getHead();
        while (n != null) {
            String u = n.getDato().getUsername();
            if (!u.equalsIgnoreCase(usuarioActual.getUsername())) {
                cbDestinatarioChat.addItem(u);
            }
            n = n.getSiguiente();
        }
    }

    private synchronized void recargarChat() {
        if (pnlMensajesStream == null || cbDestinatarioChat == null) return;
        String destino = (String) cbDestinatarioChat.getSelectedItem();
        if (destino == null) return;

        pnlMensajesStream.removeAll();
        Lista<MensajeInbox> chat = InstaFileManager.obtenerConversacion(usuarioActual.getUsername(), destino);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

        Nodo<MensajeInbox> n = chat.getHead();
        while (n != null) {
            MensajeInbox m = n.getDato();
            boolean esMio = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername());
            pnlMensajesStream.add(crearBurbujaChat(m.getTexto(), sdf.format(m.getFecha()), esMio, m.getTipo() == MensajeInbox.Tipo.STICKER));
            pnlMensajesStream.add(Box.createVerticalStrut(8));
            n = n.getSiguiente();
        }

        pnlMensajesStream.revalidate();
        pnlMensajesStream.repaint();
    }

    private JPanel crearBurbujaChat(String texto, String hora, boolean esMio, boolean esSticker) {
        JPanel wrapper = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout(6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (esMio) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(55, 151, 239), getWidth(), getHeight(), new Color(112, 0, 255));
                    g2.setPaint(gp);
                } else {
                    g2.setColor(IG_CARD_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel lblMsg = new JLabel("<html><body style='width: 220px; color:#ffffff; font-size:12px; font-family:Segoe UI;'>"
                + (esSticker ? "🌟 " : "") + texto + "</body></html>");
        JLabel lblTime = new JLabel(hora, SwingConstants.RIGHT);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTime.setForeground(new Color(220, 220, 220));

        bubble.add(lblMsg, BorderLayout.CENTER);
        bubble.add(lblTime, BorderLayout.SOUTH);
        wrapper.add(bubble);
        return wrapper;
    }

    private void iniciarHiloSincronizacionChat() {
        Thread t = new Thread(() -> {
            while (hiloChatActivo) {
                try {
                    Thread.sleep(1500);
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
    // 4. CREAR PUBLICACIÓN / CARGAR FOTO / HISTORIA
    // =========================================================================
    private JPanel crearVistaUpload() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(IG_BLACK);

        JPanel card = new JPanel(new GridLayout(8, 1, 10, 10));
        card.setBackground(IG_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IG_BORDER, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setPreferredSize(new Dimension(480, 430));

        JLabel lblTit = new JLabel("Crear nueva publicación", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTit.setForeground(IG_TEXT_WHITE);

        JTextField txtDesc = new JTextField();
        txtDesc.setBackground(IG_SURFACE);
        txtDesc.setForeground(IG_TEXT_WHITE);
        txtDesc.setCaretColor(Color.WHITE);
        txtDesc.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(IG_BORDER), "Pie de foto (#tags, @menciones)", 0, 0, new Font("Segoe UI", Font.PLAIN, 11), IG_TEXT_MUTED));

        final String[] rutaImgSel = {null};
        JButton btnImg = new JButton("📁 Seleccionar imagen de la computadora");
        btnImg.setBackground(IG_HOVER);
        btnImg.setForeground(IG_TEXT_WHITE);
        btnImg.setFocusPainted(false);

        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                File destino = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/imagenes/" + sel.getName());
                try {
                    Files.copy(sel.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    rutaImgSel[0] = destino.getAbsolutePath();
                    btnImg.setText("✅ " + sel.getName());
                } catch (Exception ex) {
                    rutaImgSel[0] = sel.getAbsolutePath();
                }
            }
        });

        JComboBox<String> cbSticker = new JComboBox<>(new String[]{"Sin Sticker", "😊 Feliz", "😢 Triste", "❤️ Corazón", "😂 Risa", "👏 Aplauso", "🔥 Fuego"});
        cbSticker.setBackground(IG_SURFACE);
        cbSticker.setForeground(IG_TEXT_WHITE);

        JCheckBox chkHistoria = new JCheckBox("Subir a tus Historias (Stories)");
        chkHistoria.setForeground(IG_TEXT_WHITE);
        chkHistoria.setOpaque(false);

        JButton btnPublicar = new JButton("Compartir");
        btnPublicar.setBackground(IG_BLUE);
        btnPublicar.setForeground(Color.WHITE);
        btnPublicar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPublicar.setFocusPainted(false);
        btnPublicar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPublicar.addActionListener(e -> {
            String texto = txtDesc.getText().trim();
            if (texto.length() > 220) {
                JOptionPane.showMessageDialog(this, "El texto no puede exceder 220 caracteres.");
                return;
            }
            String st = cbSticker.getSelectedIndex() > 0 ? (String) cbSticker.getSelectedItem() : null;
            Publicacion pub = new Publicacion(usuarioActual.getUsername(), texto, rutaImgSel[0], st, chkHistoria.isSelected());

            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(usuarioActual.getUsername());
            posts.agregar(pub);
            InstaFileManager.guardarPublicaciones(usuarioActual.getUsername(), posts);

            JOptionPane.showMessageDialog(this, "¡Publicado correctamente!");
            txtDesc.setText("");
            btnImg.setText("📁 Seleccionar imagen");
            rutaImgSel[0] = null;
            recargarTimeline();
            cardLayout.show(mainContent, "TIMELINE");
        });

        card.add(lblTit);
        card.add(btnImg);
        card.add(txtDesc);
        card.add(new JLabel("Sticker:", SwingConstants.LEFT));
        card.add(cbSticker);
        card.add(chkHistoria);
        card.add(btnPublicar);

        root.add(card);
        return root;
    }

    // =========================================================================
    // 5. MENCIONES, BUSCADOR Y AJUSTES
    // =========================================================================
    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(IG_BLACK);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTit = new JLabel("Notificaciones y Menciones (@" + usuarioActual.getUsername() + ")");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(IG_TEXT_WHITE);
        p.add(lblTit, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(IG_CARD_BG);
        list.setForeground(IG_TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar Menciones");
        btnRefrescar.setBackground(IG_HOVER);
        btnRefrescar.setForeground(IG_TEXT_WHITE);
        btnRefrescar.addActionListener(e -> {
            model.clear();
            Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
            Nodo<Usuario> nu = todos.getHead();
            while (nu != null) {
                Usuario u = nu.getDato();
                Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                Nodo<Publicacion> np = posts.getHead();
                while (np != null) {
                    Publicacion pub = np.getDato();
                    if (pub.getMenciones().contiene(usuarioActual.getUsername().toLowerCase())) {
                        model.addElement("🔔 @" + pub.getAutor() + " te mencionó: \"" + pub.getContenido() + "\"");
                    }
                    np = np.getSiguiente();
                }
                nu = nu.getSiguiente();
            }
            if (model.isEmpty()) model.addElement("No tienes menciones nuevas.");
        });

        btnRefrescar.doClick();
        p.add(btnRefrescar, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearVistaBuscarPerfil() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(IG_BLACK);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JTextField txtBuscar = new JTextField();
        txtBuscar.setBackground(IG_CARD_BG);
        txtBuscar.setForeground(IG_TEXT_WHITE);
        txtBuscar.setCaretColor(Color.WHITE);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IG_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(IG_BLUE);
        btnBuscar.setForeground(Color.WHITE);

        top.add(txtBuscar, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(IG_CARD_BG);
        list.setForeground(IG_TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnToggleSeguir = new JButton("Seguir / Dejar de seguir");
        btnToggleSeguir.setBackground(IG_BLUE);
        btnToggleSeguir.setForeground(Color.WHITE);
        p.add(btnToggleSeguir, BorderLayout.SOUTH);

        Runnable ejecutarBusqueda = () -> {
            model.clear();
            String query = txtBuscar.getText().trim().toLowerCase();
            Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
            Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());

            Nodo<Usuario> n = users.getHead();
            while (n != null) {
                Usuario u = n.getDato();
                if (u.isActivo() && u.getUsername().toLowerCase().contains(query)) {
                    boolean loSigue = seguidos.contiene(u.getUsername().toLowerCase());
                    model.addElement(u.getUsername() + "  —  (" + u.getNombreCompleto() + ") — " + (loSigue ? "✅ Siguiendo" : "➕ Seguir"));
                }
                n = n.getSiguiente();
            }
        };

        btnBuscar.addActionListener(e -> ejecutarBusqueda.run());
        ejecutarBusqueda.run();

        btnToggleSeguir.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) {
                String target = sel.split(" ")[0].trim();
                boolean ahoraSigue = InstaFileManager.toggleSeguir(usuarioActual.getUsername(), target);
                JOptionPane.showMessageDialog(this, ahoraSigue ? "¡Ahora sigues a @" + target + "!" : "Has dejado de seguir a @" + target);
                ejecutarBusqueda.run();
                recargarTimeline();
            }
        });

        return p;
    }

    private JPanel crearVistaBuscarTag() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(IG_BLACK);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JTextField txtTag = new JTextField("#");
        txtTag.setBackground(IG_CARD_BG);
        txtTag.setForeground(IG_TEXT_WHITE);
        txtTag.setCaretColor(Color.WHITE);

        JButton btnTag = new JButton("Explorar Hashtag");
        btnTag.setBackground(IG_BLUE);
        btnTag.setForeground(Color.WHITE);

        top.add(txtTag, BorderLayout.CENTER);
        top.add(btnTag, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(IG_CARD_BG);
        list.setForeground(IG_TEXT_WHITE);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        btnTag.addActionListener(e -> {
            model.clear();
            String tag = txtTag.getText().trim().toLowerCase();
            if (!tag.startsWith("#")) tag = "#" + tag;

            Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
            Nodo<Usuario> nu = todos.getHead();
            while (nu != null) {
                Usuario u = nu.getDato();
                if (u.isActivo()) {
                    Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                    Nodo<Publicacion> np = posts.getHead();
                    while (np != null) {
                        Publicacion pub = np.getDato();
                        if (pub.getHashtags().contiene(tag)) {
                            model.addElement("@" + pub.getAutor() + ": \"" + pub.getContenido() + "\"");
                        }
                        np = np.getSiguiente();
                    }
                }
                nu = nu.getSiguiente();
            }
            if (model.isEmpty()) model.addElement("No se encontraron publicaciones con " + tag);
        });

        return p;
    }

    private JPanel crearVistaEditarPerfil() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(IG_BLACK);

        JPanel card = new JPanel(new GridLayout(7, 2, 12, 12));
        card.setBackground(IG_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IG_BORDER, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setPreferredSize(new Dimension(500, 360));

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        JTextField txtNom = new JTextField(u.getNombreCompleto());
        txtNom.setBackground(IG_SURFACE);
        txtNom.setForeground(IG_TEXT_WHITE);

        JPasswordField txtPass = new JPasswordField(u.getPass());
        txtPass.setBackground(IG_SURFACE);
        txtPass.setForeground(IG_TEXT_WHITE);

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(u.getEdad(), 1, 120, 1));
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
        cbGen.setBackground(IG_SURFACE);
        cbGen.setForeground(IG_TEXT_WHITE);
        cbGen.setSelectedItem(String.valueOf(u.getGenero()));

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setBackground(IG_BLUE);
        btnGuardar.setForeground(Color.WHITE);

        JButton btnDesactivar = new JButton(u.isActivo() ? "Desactivar Cuenta" : "Reactivar Cuenta");
        btnDesactivar.setBackground(u.isActivo() ? new Color(180, 40, 40) : new Color(40, 160, 60));
        btnDesactivar.setForeground(Color.WHITE);

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
                    int resp = JOptionPane.showConfirmDialog(this, "¿Estás seguro de desactivar tu cuenta?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        usr.setActivo(false);
                        InstaFileManager.actualizarUsuario(usr);
                        btnDesactivar.setText("Reactivar Cuenta");
                        btnDesactivar.setBackground(new Color(40, 160, 60));
                        recargarPerfil();
                    }
                } else {
                    usr.setActivo(true);
                    InstaFileManager.actualizarUsuario(usr);
                    JOptionPane.showMessageDialog(this, "¡Cuenta reactivada exitosamente!");
                    btnDesactivar.setText("Desactivar Cuenta");
                    btnDesactivar.setBackground(new Color(180, 40, 40));
                    recargarPerfil();
                }
            }
        });

        card.add(new JLabel("Nombre Completo:"));
        card.add(txtNom);
        card.add(new JLabel("Contraseña:"));
        card.add(txtPass);
        card.add(new JLabel("Edad:"));
        card.add(spinEdad);
        card.add(new JLabel("Género:"));
        card.add(cbGen);
        card.add(new JLabel("Acciones:"));
        card.add(btnGuardar);
        card.add(new JLabel("Estado:"));
        card.add(btnDesactivar);

        root.add(card);
        return root;
    }
}
