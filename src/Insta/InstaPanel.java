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
import Windows.SistemadeArchivos;
import Windows.Usuario;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
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

    // Paleta exacta de las imágenes (Deep Dark Navy)
    public static final Color BG_MAIN       = new Color(8, 11, 16);
    public static final Color BG_SIDEBAR    = new Color(11, 14, 20);
    public static final Color BG_CARD       = new Color(15, 19, 28);
    public static final Color BG_INPUT      = new Color(18, 24, 36);
    public static final Color BORDER_COLOR  = new Color(28, 36, 52);
    public static final Color TEXT_WHITE    = new Color(245, 247, 250);
    public static final Color TEXT_MUTED    = new Color(140, 152, 175);
    public static final Color IG_BLUE       = new Color(0, 149, 246);
    public static final Color IG_RED_HEART  = new Color(255, 48, 64);
    public static final Color BADGE_RED     = new Color(237, 73, 86);

    // Gradientes oficiales de Instagram
    public static final Color GRAD_ORANGE   = new Color(245, 133, 41);
    public static final Color GRAD_PINK     = new Color(221, 42, 123);
    public static final Color GRAD_PURPLE   = new Color(129, 52, 175);
    public static final Color PILL_PURPLE   = new Color(112, 0, 255);
    public static final Color PILL_BLUE     = new Color(46, 99, 246);

    // Componentes para sincronización de Chat (Imagen 2)
    private JPanel pnlChatStream;
    private JScrollPane scrollChatStream;
    private String chatUsuarioSeleccionado = "valenxzz";
    private JPanel pnlListaConversaciones;
    private volatile boolean hiloChatActivo = true;

    // Cuentas temáticas sugeridas
    private final String[][] CUENTAS_SUGERIDAS = {
        {"valenxzz", "Valentina", "Te sigue"},
        {"sofiiaa.jpg", "Sofía", "Te sigue"},
        {"alejandro_7", "Alejandro", "Te sigue"},
        {"dani.castro", "Daniela", "Te sigue"},
        {"mateo.lx", "Mateo", "Te sigue"},
        {"sarita.88", "Sara", "Cerca de ti"}
    };

    public InstaPanel(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        InstaFileManager.inicializarInsta();
        InstaFileManager.crearEspacioUsuarioInsta(usuarioActual.getUsername());
        asegurarMensajesInicialesDemo();

        setLayout(new BorderLayout());
        setBackground(BG_MAIN);

        crearSidebarIzquierda();
        crearVistasPrincipales();
        iniciarHiloSincronizacionChat();
    }

    private void asegurarMensajesInicialesDemo() {
        File fInbox = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/inbox.ins");
        Lista<MensajeInbox> inbox = InstaFileManager.cargarListaGenerica(fInbox);
        if (inbox.estaVacia()) {
            InstaFileManager.enviarMensaje("valenxzz", usuarioActual.getUsername(), "Oyee, ya viste la tarea de historia?", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), "valenxzz", "Sii, ya la terminé ¿y tú?", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje("valenxzz", usuarioActual.getUsername(), "Aún no jaja, me falta el resumen pero lo termino en la tarde 😭", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), "valenxzz", "Okok, cuando lo termines me la mandas pls ❤️", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje("valenxzz", usuarioActual.getUsername(), "Dalee, y nos vemos en la uni mañana?", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), "valenxzz", "Siii, a la hora de siempre?", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje("valenxzz", usuarioActual.getUsername(), "Sii, 7:00 😁", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), "valenxzz", "Ya llegaste? :o", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje("valenxzz", usuarioActual.getUsername(), "Sii, estoy en la entrada 😊", MensajeInbox.Tipo.TEXTO);
            InstaFileManager.enviarMensaje(usuarioActual.getUsername(), "valenxzz", "Dale, te veo en un rato 👍", MensajeInbox.Tipo.TEXTO);
        }
    }

    // =========================================================================
    // BARRA LATERAL IZQUIERDA (Exacta a la imagen)
    // =========================================================================
    private final String[] activeCard = {"TIMELINE"};

    private void crearSidebarIzquierda() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(210, getHeight()));

        // Logo Instagram
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 22));
        logoPanel.setOpaque(false);
        JLabel lblLogo = new JLabel(" Instagram");
        lblLogo.setFont(new Font("Segoe Script", Font.BOLD, 23));
        lblLogo.setForeground(TEXT_WHITE);
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menú de navegación con badges
        JPanel menuPanel = new JPanel(new GridLayout(9, 1, 0, 4));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        menuPanel.add(crearBotonMenu("Inicio", "TIMELINE", "🏠", 0));
        menuPanel.add(crearBotonMenu("Buscar", "SEARCH_PROFILE", "🔍", 0));
        menuPanel.add(crearBotonMenu("Explorar", "SEARCH_TAG", "🧭", 0));
        menuPanel.add(crearBotonMenu("Reels", "TIMELINE", "🎬", 0));
        menuPanel.add(crearBotonMenu("Mensajes", "INBOX", "✉", 3));
        menuPanel.add(crearBotonMenu("Notificaciones", "MENCIONES", "♡", 5));
        menuPanel.add(crearBotonMenu("Crear", "UPLOAD", "➕", 0));
        menuPanel.add(crearBotonMenu("Perfil", "PERFIL", "👤", 0));
        menuPanel.add(crearBotonMenu("Ajustes", "EDIT_PROFILE", "⚙", 0));

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Perfil abajo con mini avatar
        JPanel userBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        userBottom.setOpaque(false);
        userBottom.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userBottom.add(crearAvatarCircular(usuarioActual.getUsername(), 30, false, null));
        JLabel lblUser = new JLabel("@" + usuarioActual.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(TEXT_WHITE);
        userBottom.add(lblUser);

        userBottom.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainContent, "PERFIL");
                recargarPerfil();
            }
        });

        sidebar.add(userBottom, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);
    }

    private JButton crearBotonMenu(String texto, String cardName, String icon, int badgeCount) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = activeCard[0].equals(cardName);

                if (isActive) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(40, 50, 75), getWidth(), getHeight(), new Color(25, 32, 50));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }

                // Dibujar texto e icono
                g2.setColor(isActive ? TEXT_WHITE : TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", isActive ? Font.BOLD : Font.PLAIN, 13));
                g2.drawString(icon + "   " + texto, 14, 25);

                // Dibujar Badge Rojo de Notificaciones
                if (badgeCount > 0) {
                    g2.setColor(BADGE_RED);
                    g2.fillOval(getWidth() - 28, 10, 18, 18);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(String.valueOf(badgeCount), getWidth() - 23, 23);
                }
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(190, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            activeCard[0] = cardName;
            cardLayout.show(mainContent, cardName);
            if (cardName.equals("TIMELINE")) recargarTimeline();
            if (cardName.equals("PERFIL")) recargarPerfil();
            if (cardName.equals("INBOX")) recargarChat();
            repaint();
        });
        return btn;
    }

    private void crearVistasPrincipales() {
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BG_MAIN);

        mainContent.add(crearVistaTimelinePrincipal(), "TIMELINE");
        mainContent.add(crearVistaInboxDirect(), "INBOX");
        mainContent.add(crearVistaPerfil(), "PERFIL");
        mainContent.add(crearVistaUpload(), "UPLOAD");
        mainContent.add(crearVistaMenciones(), "MENCIONES");
        mainContent.add(crearVistaBuscarPerfil(), "SEARCH_PROFILE");
        mainContent.add(crearVistaBuscarTag(), "SEARCH_TAG");
        mainContent.add(crearVistaEditarPerfil(), "EDIT_PROFILE");

        add(mainContent, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. TIMELINE PRINCIPAL (FEED + COLUMNA DERECHA) - EXACTO A IMAGEN 1
    // =========================================================================
    private JPanel pnlStoriesBar;
    private JPanel pnlFeedCards;

    private JPanel crearVistaTimelinePrincipal() {
        JPanel splitContainer = new JPanel(new BorderLayout());
        splitContainer.setBackground(BG_MAIN);

        // --- COLUMNA CENTRAL (STORIES + POSTS) ---
        JPanel centerFeedContainer = new JPanel(new BorderLayout());
        centerFeedContainer.setBackground(BG_MAIN);

        // Barra de historias superior
        pnlStoriesBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        pnlStoriesBar.setBackground(BG_MAIN);
        pnlStoriesBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JScrollPane scrollStories = new JScrollPane(pnlStoriesBar);
        scrollStories.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStories.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollStories.setBorder(null);
        scrollStories.setPreferredSize(new Dimension(getWidth(), 110));
        scrollStories.getHorizontalScrollBar().setUnitIncrement(12);
        centerFeedContainer.add(scrollStories, BorderLayout.NORTH);

        // Feed de publicaciones
        pnlFeedCards = new JPanel();
        pnlFeedCards.setLayout(new BoxLayout(pnlFeedCards, BoxLayout.Y_AXIS));
        pnlFeedCards.setBackground(BG_MAIN);
        pnlFeedCards.setBorder(new EmptyBorder(15, 0, 30, 0));

        JPanel wrapperCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperCenter.setBackground(BG_MAIN);
        wrapperCenter.add(pnlFeedCards);

        JScrollPane scrollFeed = new JScrollPane(wrapperCenter);
        scrollFeed.setBorder(null);
        scrollFeed.getVerticalScrollBar().setUnitIncrement(18);
        centerFeedContainer.add(scrollFeed, BorderLayout.CENTER);

        splitContainer.add(centerFeedContainer, BorderLayout.CENTER);

        // --- COLUMNA DERECHA (SUGERENCIAS Y MENSAJES RÁPIDOS) ---
        splitContainer.add(crearColumnaDerechaSugerencias(), BorderLayout.EAST);

        recargarTimeline();
        return splitContainer;
    }

    private JPanel crearColumnaDerechaSugerencias() {
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setBackground(BG_MAIN);
        rightCol.setBorder(new EmptyBorder(22, 18, 20, 24));
        rightCol.setPreferredSize(new Dimension(280, getHeight()));

        // 1. Cabecera con Perfil Actual
        JPanel myProfileRow = new JPanel(new BorderLayout(12, 0));
        myProfileRow.setOpaque(false);
        myProfileRow.add(crearAvatarCircular(usuarioActual.getUsername(), 46, false, null), BorderLayout.WEST);

        JPanel names = new JPanel(new GridLayout(2, 1, 0, 2));
        names.setOpaque(false);
        JLabel lblU = new JLabel(usuarioActual.getUsername());
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblU.setForeground(TEXT_WHITE);
        JLabel lblN = new JLabel(usuarioActual.getNombreCompleto());
        lblN.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblN.setForeground(TEXT_MUTED);
        names.add(lblU);
        names.add(lblN);
        myProfileRow.add(names, BorderLayout.CENTER);

        JLabel lblCog = new JLabel("⚙");
        lblCog.setForeground(TEXT_MUTED);
        lblCog.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblCog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainContent, "EDIT_PROFILE");
            }
        });
        myProfileRow.add(lblCog, BorderLayout.EAST);
        rightCol.add(myProfileRow);
        rightCol.add(Box.createVerticalStrut(20));

        // 2. Sección "Sugerencias para ti"
        JPanel headerSug = new JPanel(new BorderLayout());
        headerSug.setOpaque(false);
        JLabel lblTitSug = new JLabel("Sugerencias para ti");
        lblTitSug.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitSug.setForeground(TEXT_MUTED);
        JLabel lblVerTodo = new JLabel("Ver todo");
        lblVerTodo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVerTodo.setForeground(IG_BLUE);
        lblVerTodo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerSug.add(lblTitSug, BorderLayout.WEST);
        headerSug.add(lblVerTodo, BorderLayout.EAST);
        rightCol.add(headerSug);
        rightCol.add(Box.createVerticalStrut(12));

        for (String[] cuenta : CUENTAS_SUGERIDAS) {
            rightCol.add(crearFilaSugerencia(cuenta[0], cuenta[2]));
            rightCol.add(Box.createVerticalStrut(8));
        }

        rightCol.add(Box.createVerticalStrut(15));

        // 3. Sección "Mensajes rápidos"
        JPanel headerMsg = new JPanel(new BorderLayout());
        headerMsg.setOpaque(false);
        JLabel lblTitMsg = new JLabel("Mensajes");
        lblTitMsg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitMsg.setForeground(TEXT_MUTED);
        JLabel lblVerTodoMsg = new JLabel("Ver todo");
        lblVerTodoMsg.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVerTodoMsg.setForeground(IG_BLUE);
        headerMsg.add(lblTitMsg, BorderLayout.WEST);
        headerMsg.add(lblVerTodoMsg, BorderLayout.EAST);
        rightCol.add(headerMsg);
        rightCol.add(Box.createVerticalStrut(12));

        rightCol.add(crearFilaMensajeRapido("sofiiaa.jpg", "Jajaja siiii 😂", "2 min", true));
        rightCol.add(Box.createVerticalStrut(8));
        rightCol.add(crearFilaMensajeRapido("valenxzz", "Ya llegaste?", "10 min", true));
        rightCol.add(Box.createVerticalStrut(8));
        rightCol.add(crearFilaMensajeRapido("dani.castro", "Nos vemos luego!", "25 min", false));
        rightCol.add(Box.createVerticalStrut(8));
        rightCol.add(crearFilaMensajeRapido("mateo.lx", "Está bien", "1 h", false));

        return rightCol;
    }

    private JPanel crearFilaSugerencia(String username, String subtitulo) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(crearAvatarCircular(username, 34, false, null), BorderLayout.WEST);

        JPanel names = new JPanel(new GridLayout(2, 1, 0, 1));
        names.setOpaque(false);
        JLabel lblU = new JLabel(username);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblU.setForeground(TEXT_WHITE);
        JLabel lblS = new JLabel(subtitulo);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblS.setForeground(TEXT_MUTED);
        names.add(lblU);
        names.add(lblS);
        row.add(names, BorderLayout.CENTER);

        JButton btnSeguir = new JButton("Seguir") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean siguiendo = getText().equals("Siguiendo");
                g2.setColor(siguiendo ? new Color(40, 48, 65) : IG_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSeguir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSeguir.setForeground(Color.WHITE);
        btnSeguir.setPreferredSize(new Dimension(68, 26));
        btnSeguir.setContentAreaFilled(false);
        btnSeguir.setBorderPainted(false);
        btnSeguir.setFocusPainted(false);
        btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSeguir.addActionListener(e -> {
            boolean ahoraSigue = InstaFileManager.toggleSeguir(usuarioActual.getUsername(), username);
            btnSeguir.setText(ahoraSigue ? "Siguiendo" : "Seguir");
            recargarTimeline();
        });

        row.add(btnSeguir, BorderLayout.EAST);
        return row;
    }

    private JPanel crearFilaMensajeRapido(String username, String preview, String tiempo, boolean unread) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.add(crearAvatarCircular(username, 34, false, null), BorderLayout.WEST);

        JPanel names = new JPanel(new GridLayout(2, 1, 0, 1));
        names.setOpaque(false);
        JLabel lblU = new JLabel(username);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblU.setForeground(TEXT_WHITE);
        JLabel lblP = new JLabel(preview);
        lblP.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblP.setForeground(TEXT_MUTED);
        names.add(lblU);
        names.add(lblP);
        row.add(names, BorderLayout.CENTER);

        JLabel lblTime = new JLabel("<html>" + tiempo + (unread ? " <span style='color:#0095f6;'>●</span>" : "") + "</html>");
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTime.setForeground(TEXT_MUTED);
        row.add(lblTime, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chatUsuarioSeleccionado = username;
                activeCard[0] = "INBOX";
                cardLayout.show(mainContent, "INBOX");
                recargarChat();
            }
        });
        return row;
    }

    private void recargarTimeline() {
        if (pnlStoriesBar == null || pnlFeedCards == null) return;
        pnlStoriesBar.removeAll();
        pnlFeedCards.removeAll();

        // 1. Stories
        pnlStoriesBar.add(crearBurbujaStory("Tu historia", usuarioActual.getUsername(), true));
        for (String[] cuenta : CUENTAS_SUGERIDAS) {
            pnlStoriesBar.add(crearBurbujaStory(cuenta[0], cuenta[0], false));
        }

        // 2. Posts en el Feed
        Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
        Lista<String> autores = new Lista<>();
        autores.agregar(usuarioActual.getUsername());
        autores.agregar("_david.suazo");
        for (String[] c : CUENTAS_SUGERIDAS) autores.agregar(c[0]);

        Nodo<String> na = autores.getHead();
        while (na != null) {
            String autor = na.getDato();
            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(autor);
            Nodo<Publicacion> np = posts.getHead();
            while (np != null) {
                Publicacion p = np.getDato();
                if (!p.isEsHistoria()) {
                    pnlFeedCards.add(crearTarjetaPostVisual(p));
                    pnlFeedCards.add(Box.createVerticalStrut(20));
                }
                np = np.getSiguiente();
            }
            na = na.getSiguiente();
        }

        // Si no hay posts subidos, mostrar post demo idéntico a la imagen 1
        if (pnlFeedCards.getComponentCount() == 0) {
            Publicacion pDemo = new Publicacion("_david.suazo", "Otro atardecer, otra razón para seguir. 🌅", null, null, false);
            pnlFeedCards.add(crearTarjetaPostVisual(pDemo));
        }

        pnlStoriesBar.revalidate();
        pnlStoriesBar.repaint();
        pnlFeedCards.revalidate();
        pnlFeedCards.repaint();
    }

    private JPanel crearBurbujaStory(String label, String username, boolean isMyStory) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.add(crearAvatarCircular(username, 58, !isMyStory, isMyStory ? "+" : null), BorderLayout.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isMyStory) {
                    cardLayout.show(mainContent, "UPLOAD");
                } else {
                    JOptionPane.showMessageDialog(InstaPanel.this, "Viendo historia de @" + username + " 📖", "Instagram Stories", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        return p;
    }

    private JPanel crearTarjetaPostVisual(Publicacion p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 14, 14)
        ));
        card.setPreferredSize(new Dimension(480, 540));

        // Cabecera: Avatar + Nombre + Ubicación + Menú
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.add(crearAvatarCircular(p.getAutor(), 36, true, null), BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel lblUser = new JLabel(p.getAutor());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(TEXT_WHITE);
        JLabel lblLoc = new JLabel("San Pedro Sula, Honduras");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLoc.setForeground(TEXT_MUTED);
        titlePanel.add(lblUser);
        titlePanel.add(lblLoc);
        header.add(titlePanel, BorderLayout.CENTER);

        JLabel lblDots = new JLabel("•••");
        lblDots.setForeground(TEXT_MUTED);
        lblDots.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(lblDots, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Imagen central (Carga imagen real o dibuja atardecer simulado idéntico)
        JPanel photoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (p.getRutaImagen() == null || !new File(p.getRutaImagen()).exists()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    // Pintar atardecer en degradado idéntico a la foto
                    GradientPaint sky = new GradientPaint(0, 0, new Color(40, 20, 60), 0, getHeight(), new Color(245, 120, 40));
                    g2.setPaint(sky);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    // Silueta de montañas
                    g2.setColor(new Color(15, 10, 25));
                    int[] xM = {0, 100, 200, 320, 480, 480, 0};
                    int[] yM = {getHeight(), getHeight() - 90, getHeight() - 130, getHeight() - 70, getHeight() - 110, getHeight(), getHeight()};
                    g2.fillPolygon(xM, yM, 7);
                    g2.dispose();
                }
            }
        };
        photoPanel.setPreferredSize(new Dimension(460, 290));
        photoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image scaled = icon.getImage().getScaledInstance(460, 290, Image.SCALE_SMOOTH);
            photoPanel.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);
        }
        card.add(photoPanel, BorderLayout.CENTER);

        // Pie de tarjeta con Likes, descripción y comentarios
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        // Barra de acciones (Corazón, Comentario, Compartir, Guardar)
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        JPanel leftActs = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftActs.setOpaque(false);

        JButton btnLike = new JButton("❤");
        btnLike.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btnLike.setForeground(TEXT_WHITE);
        btnLike.setContentAreaFilled(false);
        btnLike.setBorderPainted(false);
        btnLike.setFocusPainted(false);
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblLikesCount = new JLabel("1,248 Me gusta");
        lblLikesCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLikesCount.setForeground(TEXT_WHITE);

        btnLike.addActionListener(e -> {
            boolean liked = btnLike.getForeground().equals(IG_RED_HEART);
            btnLike.setForeground(liked ? TEXT_WHITE : IG_RED_HEART);
            lblLikesCount.setText(liked ? "1,248 Me gusta" : "1,249 Me gusta");
        });

        JLabel btnComment = new JLabel("💬");
        btnComment.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnComment.setForeground(TEXT_WHITE);

        JLabel btnShare = new JLabel("➤");
        btnShare.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnShare.setForeground(TEXT_WHITE);

        leftActs.add(btnLike);
        leftActs.add(btnComment);
        leftActs.add(btnShare);
        actions.add(leftActs, BorderLayout.WEST);

        JLabel btnBookmark = new JLabel("🔖");
        btnBookmark.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnBookmark.setForeground(TEXT_WHITE);
        actions.add(btnBookmark, BorderLayout.EAST);
        footer.add(actions);
        footer.add(Box.createVerticalStrut(4));

        footer.add(lblLikesCount);
        footer.add(Box.createVerticalStrut(4));

        JLabel lblCaption = new JLabel("<html><body style='width:440px; color:#ffffff; font-size:11px; font-family:Segoe UI;'>"
                + "<b>" + p.getAutor() + "</b> " + p.getContenido() + "</body></html>");
        footer.add(lblCaption);
        footer.add(Box.createVerticalStrut(4));

        JLabel lblVerComs = new JLabel("Ver los 32 comentarios");
        lblVerComs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVerComs.setForeground(TEXT_MUTED);
        footer.add(lblVerComs);

        JLabel lblCom1 = new JLabel("<html><b style='color:#fff;'>alejandro_7</b> <span style='color:#ddd;'>Qué buena foto bro! 🔥</span></html>");
        lblCom1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(lblCom1);

        JLabel lblCom2 = new JLabel("<html><b style='color:#fff;'>valenxzz</b> <span style='color:#ddd;'>San Pedro nunca se ve tan bonito 😍</span></html>");
        lblCom2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.add(lblCom2);

        JLabel lblTime = new JLabel("HACE 2 HORAS • Ver traducción");
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTime.setForeground(TEXT_MUTED);
        footer.add(Box.createVerticalStrut(2));
        footer.add(lblTime);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. INBOX: DIRECT MESSAGES (EXACTO A IMAGEN 2)
    // =========================================================================
    private JPanel crearVistaInboxDirect() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        // --- PANEL IZQUIERDO: LISTA DE CHATS ---
        JPanel leftList = new JPanel(new BorderLayout());
        leftList.setBackground(BG_SIDEBAR);
        leftList.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        leftList.setPreferredSize(new Dimension(280, getHeight()));

        // Cabecera Mensajes
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 18, 12, 18));
        JLabel lblTit = new JLabel("Mensajes");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(TEXT_WHITE);
        JLabel lblIcons = new JLabel("📝  •••");
        lblIcons.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblIcons.setForeground(TEXT_WHITE);
        header.add(lblTit, BorderLayout.WEST);
        header.add(lblIcons, BorderLayout.EAST);
        leftList.add(header, BorderLayout.NORTH);

        // Buscador y Pestañas
        JPanel topSub = new JPanel();
        topSub.setLayout(new BoxLayout(topSub, BoxLayout.Y_AXIS));
        topSub.setOpaque(false);
        topSub.setBorder(new EmptyBorder(0, 18, 10, 18));

        JTextField txtSearch = new JTextField("🔍 Buscar en mensajes...");
        txtSearch.setBackground(BG_INPUT);
        txtSearch.setForeground(TEXT_MUTED);
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        topSub.add(txtSearch);
        topSub.add(Box.createVerticalStrut(10));

        // Pestañas (Principal, General, Solicitudes)
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabs.setOpaque(false);
        tabs.add(crearPildoraTab("Principal", true));
        tabs.add(crearPildoraTab("General", false));
        tabs.add(crearPildoraTab("Solicitudes (3)", false));
        topSub.add(tabs);
        leftList.add(topSub, BorderLayout.CENTER);

        // Lista scrolleable de conversaciones
        pnlListaConversaciones = new JPanel();
        pnlListaConversaciones.setLayout(new BoxLayout(pnlListaConversaciones, BoxLayout.Y_AXIS));
        pnlListaConversaciones.setBackground(BG_SIDEBAR);

        JScrollPane scrollConv = new JScrollPane(pnlListaConversaciones);
        scrollConv.setBorder(null);
        scrollConv.getVerticalScrollBar().setUnitIncrement(12);

        JPanel centerListWrap = new JPanel(new BorderLayout());
        centerListWrap.setOpaque(false);
        centerListWrap.add(topSub, BorderLayout.NORTH);
        centerListWrap.add(scrollConv, BorderLayout.CENTER);
        leftList.add(centerListWrap, BorderLayout.CENTER);

        root.add(leftList, BorderLayout.WEST);

        // --- PANEL DERECHO: CONVERSACIÓN ABIERTA ---
        JPanel rightChat = new JPanel(new BorderLayout());
        rightChat.setBackground(BG_MAIN);

        // Cabecera del chat activo
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(BG_MAIN);
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        chatHeader.setPreferredSize(new Dimension(getWidth(), 60));
        chatHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel userActiveInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userActiveInfo.setOpaque(false);
        userActiveInfo.add(crearAvatarCircular(chatUsuarioSeleccionado, 40, false, null));

        JPanel userTxts = new JPanel(new GridLayout(2, 1));
        userTxts.setOpaque(false);
        JLabel lblChatUser = new JLabel(chatUsuarioSeleccionado);
        lblChatUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChatUser.setForeground(TEXT_WHITE);
        JLabel lblStatus = new JLabel("🟢 Activo(a) ahora");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(TEXT_MUTED);
        userTxts.add(lblChatUser);
        userTxts.add(lblStatus);
        userActiveInfo.add(userTxts);
        chatHeader.add(userActiveInfo, BorderLayout.WEST);

        JLabel lblCallIcons = new JLabel("📹   📞   ⓘ  ");
        lblCallIcons.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblCallIcons.setForeground(TEXT_WHITE);
        chatHeader.add(lblCallIcons, BorderLayout.EAST);
        rightChat.add(chatHeader, BorderLayout.NORTH);

        // Área de Mensajes (Burbujas)
        pnlChatStream = new JPanel();
        pnlChatStream.setLayout(new BoxLayout(pnlChatStream, BoxLayout.Y_AXIS));
        pnlChatStream.setBackground(BG_MAIN);
        pnlChatStream.setBorder(new EmptyBorder(15, 25, 15, 25));

        scrollChatStream = new JScrollPane(pnlChatStream);
        scrollChatStream.setBorder(null);
        scrollChatStream.getVerticalScrollBar().setUnitIncrement(14);
        rightChat.add(scrollChatStream, BorderLayout.CENTER);

        // Barra inferior de envío (Píldora idéntica)
        JPanel inputBottom = new JPanel(new BorderLayout(10, 0));
        inputBottom.setBackground(BG_MAIN);
        inputBottom.setBorder(new EmptyBorder(12, 25, 16, 25));

        JPanel pillInput = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        pillInput.setOpaque(false);
        pillInput.setPreferredSize(new Dimension(getWidth(), 44));
        pillInput.setBorder(new EmptyBorder(4, 14, 4, 14));

        JLabel lblEmoji = new JLabel("😊");
        lblEmoji.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblEmoji.setForeground(TEXT_MUTED);
        lblEmoji.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JTextField txtInput = new JTextField();
        txtInput.setOpaque(false);
        txtInput.setForeground(TEXT_WHITE);
        txtInput.setCaretColor(Color.WHITE);
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtInput.setBorder(null);

        JPanel rightActs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightActs.setOpaque(false);
        JLabel lblImgAct = new JLabel("🖼");
        lblImgAct.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblImgAct.setForeground(TEXT_MUTED);
        JLabel lblHeartAct = new JLabel("♡");
        lblHeartAct.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeartAct.setForeground(TEXT_MUTED);
        lblHeartAct.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightActs.add(lblImgAct);
        rightActs.add(lblHeartAct);

        pillInput.add(lblEmoji, BorderLayout.WEST);
        pillInput.add(txtInput, BorderLayout.CENTER);
        pillInput.add(rightActs, BorderLayout.EAST);
        inputBottom.add(pillInput, BorderLayout.CENTER);
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
        txtInput.addActionListener(enviarMsg);
        lblHeartAct.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, "❤️", MensajeInbox.Tipo.STICKER);
                recargarChat();
            }
        });

        recargarChat();
        return root;
    }

    private JLabel crearPildoraTab(String texto, boolean active) {
        JLabel l = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, PILL_PURPLE, getWidth(), getHeight(), PILL_BLUE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(active ? Color.WHITE : TEXT_MUTED);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        l.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return l;
    }

    private synchronized void recargarChat() {
        if (pnlChatStream == null || pnlListaConversaciones == null) return;
        pnlListaConversaciones.removeAll();

        // 1. Cargar lista de conversaciones a la izquierda (con "true" y "false" en texto)
        String[][] chats = {
            {"valenxzz", "Ya llegaste? :o", "10 min", "true"},
            {"sofiiaa.jpg", "Jajaja siiii 😂", "12 min", "true"},
            {"alejandro_7", "Nos vemos luego!", "25 min", "true"},
            {"dani.castro", "Estás bien?", "1 h", "false"},
            {"mateo.lx", "Qué onda bro", "2 h", "false"},
            {"sarita.88", "Te veo en la tarde?", "3 h", "false"},
            {"tu historia", "Respondiste a su historia", "4 h", "false"},
            {"alex_fer", "Jajajajaja", "5 h", "false"},
            {"luna.av", "Gracias! 💙", "6 h", "false"},
            {"cristianx", "Dale bro, te escribo después", "8 h", "false"}
        };

        for (String[] c : chats) {
            boolean isSelected = c[0].equalsIgnoreCase(chatUsuarioSeleccionado);
            pnlListaConversaciones.add(crearFilaChatConversacion(c[0], c[1], c[2], isSelected, c[3].equals("true")));
            pnlListaConversaciones.add(Box.createVerticalStrut(4));
        }

        // 2. Cargar burbujas de conversación
        pnlChatStream.removeAll();
        Lista<MensajeInbox> conversacion = InstaFileManager.obtenerConversacion(usuarioActual.getUsername(), chatUsuarioSeleccionado);

        // Tiempos separadores
        pnlChatStream.add(crearSeparadorTiempo("Hoy 9:32 a. m."));
        pnlChatStream.add(Box.createVerticalStrut(10));

        Nodo<MensajeInbox> n = conversacion.getHead();
        int count = 0;
        while (n != null) {
            MensajeInbox m = n.getDato();
            boolean esMio = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername());
            if (count == 6) {
                pnlChatStream.add(crearSeparadorTiempo("Hoy 9:45 a. m."));
                pnlChatStream.add(Box.createVerticalStrut(10));
            } else if (count == 8) {
                pnlChatStream.add(crearSeparadorTiempo("Hoy 10:03 a. m."));
                pnlChatStream.add(Box.createVerticalStrut(10));
            }

            pnlChatStream.add(crearBurbujaChatReal(m.getTexto(), esMio));
            pnlChatStream.add(Box.createVerticalStrut(8));
            count++;
            n = n.getSiguiente();
        }

        // Visto al final
        JLabel lblVisto = new JLabel("Visto ✔✔", SwingConstants.RIGHT);
        lblVisto.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVisto.setForeground(TEXT_MUTED);
        pnlChatStream.add(lblVisto);

        pnlListaConversaciones.revalidate();
        pnlListaConversaciones.repaint();
        pnlChatStream.revalidate();
        pnlChatStream.repaint();
    }

    private JPanel crearFilaChatConversacion(String username, String snippet, String tiempo, boolean isSelected, boolean unread) {
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                if (isSelected) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(38, 25, 55));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 12, 8, 12));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.add(crearAvatarCircular(username, 42, isSelected, null), BorderLayout.WEST);

        JPanel names = new JPanel(new GridLayout(2, 1, 0, 2));
        names.setOpaque(false);
        JLabel lblU = new JLabel(username);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblU.setForeground(TEXT_WHITE);
        JLabel lblS = new JLabel(snippet);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblS.setForeground(TEXT_MUTED);
        names.add(lblU);
        names.add(lblS);
        p.add(names, BorderLayout.CENTER);

        JLabel lblT = new JLabel("<html>" + tiempo + (unread ? " <span style='color:#0095f6;'>●</span>" : "") + "</html>");
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblT.setForeground(TEXT_MUTED);
        p.add(lblT, BorderLayout.EAST);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chatUsuarioSeleccionado = username;
                recargarChat();
            }
        });
        return p;
    }

    private JLabel crearSeparadorTiempo(String tiempo) {
        JLabel l = new JLabel(tiempo, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JPanel crearBurbujaChatReal(String texto, boolean esMio) {
        JPanel row = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        if (!esMio) {
            row.add(crearAvatarCircular(chatUsuarioSeleccionado, 28, false, null));
            row.add(Box.createHorizontalStrut(8));
        }

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (esMio) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(46, 99, 246), getWidth(), getHeight(), new Color(112, 0, 255));
                    g2.setPaint(gp);
                } else {
                    g2.setColor(new Color(24, 30, 42));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel lbl = new JLabel("<html><body style='max-width:320px; color:#ffffff; font-size:12px; font-family:Segoe UI;'>"
                + texto + "</body></html>");
        bubble.add(lbl, BorderLayout.CENTER);
        row.add(bubble);
        return row;
    }

    // =========================================================================
    // UTILITARIOS DE DISEÑO (AVATAR CIRCULAR CON ANILLO DE GRADIENTE)
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
                    GradientPaint gp = new GradientPaint(0, 0, GRAD_ORANGE, diametro, diametro, GRAD_PURPLE);
                    g2.setPaint(gp);
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawOval(1, 1, diametro - 3, diametro - 3);
                }

                // Círculo base de avatar
                g2.setColor(new Color(32, 40, 56));
                g2.fillOval(offset, offset, size, size);

                // Inicial del nombre
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, diametro / 3 + 2));
                String letter = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "U";
                FontMetrics fm = g2.getFontMetrics();
                int tx = offset + (size - fm.stringWidth(letter)) / 2;
                int ty = offset + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, tx, ty);

                // Badge de "+" para Tu historia
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
    // VISTAS RESTANTES: PERFIL, CREAR, BUSCADORES
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

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        Lista<String> followers = InstaFileManager.cargarSeguidores(u.getUsername());
        Lista<String> following = InstaFileManager.cargarSeguidos(u.getUsername());
        Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());

        pnlPerfilHeader.add(crearAvatarCircular(u.getUsername(), 86, true, null), BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(3, 1, 0, 6));
        info.setOpaque(false);
        JLabel lblU = new JLabel(u.getUsername() + "   " + (u.isActivo() ? "🟢" : "🔴"));
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblU.setForeground(TEXT_WHITE);

        JPanel rowStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        rowStats.setOpaque(false);
        rowStats.add(new JLabel("<html><b>" + posts.getSize() + "</b> publicaciones</html>"));
        rowStats.add(new JLabel("<html><b>" + followers.getSize() + "</b> seguidores</html>"));
        rowStats.add(new JLabel("<html><b>" + following.getSize() + "</b> seguidos</html>"));
        for (Component c : rowStats.getComponents()) c.setForeground(TEXT_WHITE);

        JLabel lblBio = new JLabel("<html><b>" + u.getNombreCompleto() + "</b> • " + u.getEdad() + " años</html>");
        lblBio.setForeground(TEXT_MUTED);

        info.add(lblU);
        info.add(rowStats);
        info.add(lblBio);
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
                JLabel lbl = new JLabel("<html><center style='color:#bbb; font-size:10px; padding:10px;'>"
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

    private JPanel crearVistaUpload() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_MAIN);

        JPanel card = new JPanel(new GridLayout(8, 1, 10, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setPreferredSize(new Dimension(480, 420));

        JLabel lblTit = new JLabel("Crear nueva publicación", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTit.setForeground(TEXT_WHITE);

        JTextField txtDesc = new JTextField("Escribe algo... (#tags, @menciones)");
        txtDesc.setBackground(BG_INPUT);
        txtDesc.setForeground(TEXT_WHITE);
        txtDesc.setCaretColor(Color.WHITE);

        final String[] rutaSel = {null};
        JButton btnImg = new JButton("📁 Seleccionar imagen (.jpg, .png)");
        btnImg.setBackground(BG_INPUT);
        btnImg.setForeground(TEXT_WHITE);

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

        JComboBox<String> cbSticker = new JComboBox<>(new String[]{"Sin Sticker", "😊 Feliz", "😢 Triste", "❤️ Corazón", "😂 Risa", "👏 Aplauso", "🔥 Fuego"});
        cbSticker.setBackground(BG_INPUT);
        cbSticker.setForeground(TEXT_WHITE);

        JCheckBox chkHistoria = new JCheckBox("Subir a tus Historias (Stories)");
        chkHistoria.setForeground(TEXT_WHITE);
        chkHistoria.setOpaque(false);

        JButton btnPub = new JButton("Compartir");
        btnPub.setBackground(IG_BLUE);
        btnPub.setForeground(Color.WHITE);
        btnPub.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnPub.addActionListener(e -> {
            String txt = txtDesc.getText().trim();
            String st = cbSticker.getSelectedIndex() > 0 ? (String) cbSticker.getSelectedItem() : null;
            Publicacion p = new Publicacion(usuarioActual.getUsername(), txt, rutaSel[0], st, chkHistoria.isSelected());
            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(usuarioActual.getUsername());
            posts.agregar(p);
            InstaFileManager.guardarPublicaciones(usuarioActual.getUsername(), posts);

            JOptionPane.showMessageDialog(this, "¡Publicado exitosamente!");
            txtDesc.setText("");
            rutaSel[0] = null;
            btnImg.setText("📁 Seleccionar imagen (.jpg, .png)");
            cardLayout.show(mainContent, "TIMELINE");
            recargarTimeline();
        });

        card.add(lblTit);
        card.add(btnImg);
        card.add(txtDesc);
        card.add(new JLabel("Sticker Opcional:"));
        card.add(cbSticker);
        card.add(chkHistoria);
        card.add(btnPub);

        root.add(card);
        return root;
    }

    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel l = new JLabel("Notificaciones y Menciones (@" + usuarioActual.getUsername() + ")");
        l.setFont(new Font("Segoe UI", Font.BOLD, 17));
        l.setForeground(TEXT_WHITE);
        p.add(l, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_WHITE);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnRef = new JButton("Actualizar");
        btnRef.setBackground(BG_INPUT);
        btnRef.setForeground(TEXT_WHITE);
        btnRef.addActionListener(e -> {
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

        btnRef.doClick();
        p.add(btnRef, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearVistaBuscarPerfil() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JTextField txt = new JTextField();
        txt.setBackground(BG_INPUT);
        txt.setForeground(TEXT_WHITE);
        JButton btn = new JButton("Buscar");
        btn.setBackground(IG_BLUE);
        btn.setForeground(Color.WHITE);
        top.add(txt, BorderLayout.CENTER);
        top.add(btn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_WHITE);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnFollow = new JButton("Seguir / Dejar de seguir seleccionado");
        btnFollow.setBackground(IG_BLUE);
        btnFollow.setForeground(Color.WHITE);
        p.add(btnFollow, BorderLayout.SOUTH);

        Runnable buscar = () -> {
            model.clear();
            String q = txt.getText().trim().toLowerCase();
            Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
            Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
            Nodo<Usuario> n = users.getHead();
            while (n != null) {
                Usuario u = n.getDato();
                if (u.isActivo() && u.getUsername().toLowerCase().contains(q)) {
                    boolean sigue = seguidos.contiene(u.getUsername().toLowerCase());
                    model.addElement(u.getUsername() + "  —  (" + u.getNombreCompleto() + ") — " + (sigue ? "✅ Siguiendo" : "➕ Seguir"));
                }
                n = n.getSiguiente();
            }
        };

        btn.addActionListener(e -> buscar.run());
        buscar.run();

        btnFollow.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) {
                String target = sel.split(" ")[0].trim();
                boolean ahora = InstaFileManager.toggleSeguir(usuarioActual.getUsername(), target);
                JOptionPane.showMessageDialog(this, ahora ? "¡Ahora sigues a @" + target + "!" : "Dejaste de seguir a @" + target);
                buscar.run();
                recargarTimeline();
            }
        });
        return p;
    }

    private JPanel crearVistaBuscarTag() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JTextField txt = new JTextField("#");
        txt.setBackground(BG_INPUT);
        txt.setForeground(TEXT_WHITE);
        JButton btn = new JButton("Explorar Hashtag");
        btn.setBackground(IG_BLUE);
        btn.setForeground(Color.WHITE);
        top.add(txt, BorderLayout.CENTER);
        top.add(btn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_CARD);
        list.setForeground(TEXT_WHITE);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        btn.addActionListener(e -> {
            model.clear();
            String tag = txt.getText().trim().toLowerCase();
            if (!tag.startsWith("#")) tag = "#" + tag;
            Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
            Nodo<Usuario> nu = users.getHead();
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
        txtNom.setBackground(BG_INPUT);
        txtNom.setForeground(TEXT_WHITE);

        JPasswordField txtPass = new JPasswordField(u.getPass());
        txtPass.setBackground(BG_INPUT);
        txtPass.setForeground(TEXT_WHITE);

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(u.getEdad(), 1, 120, 1));
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
        cbGen.setBackground(BG_INPUT);
        cbGen.setForeground(TEXT_WHITE);
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
