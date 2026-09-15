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
public class InstaPanel extends JPanel implements InstaClientSocket.MensajeListener {
    private Usuario usuarioActual;
    private String usuarioPerfilVisitado;

    private CardLayout rootCardLayout;
    private JPanel rootContainer;

    private CardLayout authCardLayout;
    private JPanel authContainer;

    private CardLayout screenCardLayout;
    private JPanel screenContainer;

    private InstaClientSocket socketCliente;

    public static final Color BG_PHONE        = new Color(0, 0, 0);
    public static final Color BG_SURFACE      = new Color(18, 18, 18);
    public static final Color BG_INPUT        = new Color(28, 28, 30);
    public static final Color BG_HOVER        = new Color(44, 44, 46);
    public static final Color BORDER_LINE     = new Color(48, 48, 50);
    public static final Color TEXT_WHITE      = new Color(245, 245, 247);
    public static final Color TEXT_MUTED      = new Color(160, 160, 165);
    public static final Color IG_BLUE         = new Color(0, 149, 246);
    public static final Color IG_RED_HEART    = new Color(255, 48, 64);
    public static final Color G_ORANGE        = new Color(245, 133, 41);
    public static final Color G_PINK          = new Color(221, 42, 123);

    private JPanel pnlFeedCards;
    private JPanel pnlStoriesBar;
    private JPanel pnlPerfilHeader;
    private JPanel pnlPerfilGrid;
    private DefaultListModel<String> modelNotificaciones;

    private CardLayout inboxCardLayout;
    private JPanel inboxContainer;
    private JPanel pnlListaConversaciones;
    private JPanel pnlChatStream;
    private JLabel lblChatHeaderUser;
    private JTextField txtBuscarChats;
    private String chatUsuarioSeleccionado = "noticias";

    private String pantallaActual = "TIMELINE";
    private JPanel bottomNavBar;

    public InstaPanel(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("usuario", "Pass1234!", false);
        this.usuarioPerfilVisitado = this.usuarioActual.getUsername();

        InstaFileManager.inicializarInsta();
        InstaFileManager.crearEspacioUsuarioInsta(this.usuarioActual.getUsername());

        setLayout(new BorderLayout());
        setBackground(BG_PHONE);

        rootCardLayout = new CardLayout();
        rootContainer = new JPanel(rootCardLayout);
        rootContainer.setOpaque(false);

        rootContainer.add(crearVistaAutenticacionMobile(), "AUTH");
        rootContainer.add(crearVistaTelefonoPrincipal(), "APP");

        add(rootContainer, BorderLayout.CENTER);
        rootCardLayout.show(rootContainer, "AUTH");

        iniciarConexionSocket();
    }

    private void iniciarConexionSocket() {
        socketCliente = new InstaClientSocket();
        socketCliente.setListener(this);
        new Thread(() -> {
            socketCliente.conectar("localhost", InstaServer.PUERTO, usuarioActual.getUsername());
        }).start();
    }

    @Override
    public void onMensajeRecibido(String emisor, String receptor, String contenido, boolean esSticker) {
        SwingUtilities.invokeLater(() -> {
            recargarChat();
            recargarNotificacionesEnVivo();
        });
    }

    @Override
    public void onNuevoSeguidor(String seguidor) {
        SwingUtilities.invokeLater(() -> {
            recargarNotificacionesEnVivo();
            if (pantallaActual.equals("PERFIL")) {
                recargarPerfil();
            }
        });
    }

    private JPanel crearVistaTelefonoPrincipal() {
        JPanel phone = new JPanel(new BorderLayout());
        phone.setBackground(BG_PHONE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_SURFACE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LINE));
        topBar.setPreferredSize(new Dimension(getWidth(), 48));
        topBar.setBorder(new EmptyBorder(6, 14, 6, 14));

        JLabel lblLogo = new JLabel("INSTA+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, G_ORANGE, getWidth(), 0, G_PINK));
                g2.setFont(getFont());
                g2.drawString(getText(), 0, g2.getFontMetrics().getAscent());
                g2.dispose();
            }
        };
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 22));
        lblLogo.setPreferredSize(new Dimension(110, 30));
        topBar.add(lblLogo, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        topActions.setOpaque(false);

        JButton btnDirect = crearBotonIconoTop("DM", () -> cambiarPantalla("INBOX"));
        btnDirect.setToolTipText("Mensajes Directos");

        JButton btnConfig = crearBotonIconoTop("CONFIG", () -> cambiarPantalla("CONFIG"));
        btnConfig.setToolTipText("Configuración");

        topActions.add(btnDirect);
        topActions.add(btnConfig);
        topBar.add(topActions, BorderLayout.EAST);
        phone.add(topBar, BorderLayout.NORTH);

        screenCardLayout = new CardLayout();
        screenContainer = new JPanel(screenCardLayout);
        screenContainer.setBackground(BG_PHONE);

        screenContainer.add(crearVistaTimeline(), "TIMELINE");
        screenContainer.add(crearVistaBuscar(), "SEARCH");
        screenContainer.add(crearVistaUpload(), "UPLOAD");
        screenContainer.add(crearVistaMenciones(), "NOTIFICACIONES");
        screenContainer.add(crearVistaPerfil(), "PERFIL");
        screenContainer.add(crearVistaInbox(), "INBOX");
        screenContainer.add(crearVistaEditarPerfil(), "EDIT_PROFILE");
        screenContainer.add(crearVistaConfiguracion(), "CONFIG");

        phone.add(screenContainer, BorderLayout.CENTER);

        bottomNavBar = new JPanel(new GridLayout(1, 5, 0, 0));
        bottomNavBar.setBackground(BG_SURFACE);
        bottomNavBar.setPreferredSize(new Dimension(getWidth(), 50));
        bottomNavBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LINE));

        bottomNavBar.add(crearTabBottomNav("HOME", "TIMELINE"));
        bottomNavBar.add(crearTabBottomNav("SEARCH", "SEARCH"));
        bottomNavBar.add(crearTabBottomNav("ADD", "UPLOAD"));
        bottomNavBar.add(crearTabBottomNav("HEART", "NOTIFICACIONES"));
        bottomNavBar.add(crearTabBottomNav("PROFILE", "PERFIL"));

        phone.add(bottomNavBar, BorderLayout.SOUTH);
        return phone;
    }

    public void cambiarPantalla(String nombreCard) {
        pantallaActual = nombreCard;
        if (nombreCard.equals("PERFIL")) {
            recargarPerfil();
        } else if (nombreCard.equals("TIMELINE")) {
            recargarTimeline();
        } else if (nombreCard.equals("INBOX")) {
            recargarChat();
        }
        screenCardLayout.show(screenContainer, nombreCard);
        bottomNavBar.repaint();
    }

    private JButton crearTabBottomNav(String tipoIcono, String cardName) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                boolean activa = pantallaActual.equals(cardName);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(activa ? Color.WHITE : TEXT_MUTED);
                g2.setStroke(new BasicStroke(activa ? 2.2f : 1.8f));

                if (tipoIcono.equals("HOME")) {
                    int[] xP = {cx, cx - 9, cx - 9, cx + 9, cx + 9};
                    int[] yP = {cy - 9, cy - 1, cy + 8, cy + 8, cy - 1};
                    g2.drawPolygon(xP, yP, 5);
                } else if (tipoIcono.equals("SEARCH")) {
                    g2.drawOval(cx - 8, cy - 8, 12, 12);
                    g2.drawLine(cx + 2, cy + 2, cx + 8, cy + 8);
                } else if (tipoIcono.equals("ADD")) {
                    g2.drawRoundRect(cx - 9, cy - 9, 18, 18, 5, 5);
                    g2.drawLine(cx, cy - 5, cx, cy + 5);
                    g2.drawLine(cx - 5, cy, cx + 5, cy);
                } else if (tipoIcono.equals("HEART")) {
                    int[] xH = {cx, cx - 7, cx - 8, cx - 4, cx, cx + 4, cx + 8, cx + 7};
                    int[] yH = {cy + 7, cy, cy - 5, cy - 8, cy - 4, cy - 8, cy - 5, cy};
                    g2.drawPolygon(xH, yH, 8);
                } else if (tipoIcono.equals("PROFILE")) {
                    g2.drawOval(cx - 5, cy - 8, 10, 10);
                    g2.drawArc(cx - 8, cy + 1, 16, 10, 0, 180);
                }

                if (activa) {
                    g2.setColor(IG_BLUE);
                    g2.fillOval(cx - 2, getHeight() - 5, 4, 4);
                }
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (cardName.equals("PERFIL")) {
                usuarioPerfilVisitado = usuarioActual.getUsername();
            }
            cambiarPantalla(cardName);
        });
        return btn;
    }

    private JButton crearBotonIconoTop(String tipo, Runnable accion) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                g2.setColor(TEXT_WHITE);
                g2.setStroke(new BasicStroke(1.8f));

                if (tipo.equals("DM")) {
                    int[] xP = {cx - 8, cx + 8, cx - 2, cx - 8};
                    int[] yP = {cy - 6, cy - 1, cy + 7, cy + 2};
                    g2.drawPolygon(xP, yP, 4);
                    g2.drawLine(cx - 8, cy - 6, cx - 2, cy + 7);
                } else {
                    g2.drawOval(cx - 7, cy - 7, 14, 14);
                    g2.drawOval(cx - 3, cy - 3, 6, 6);
                }
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(34, 34));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    // =========================================================================
    // 1. TIMELINE
    // =========================================================================
    private JPanel crearVistaTimeline() {
        JPanel feedRoot = new JPanel(new BorderLayout());
        feedRoot.setBackground(BG_PHONE);

        pnlStoriesBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        pnlStoriesBar.setBackground(BG_PHONE);
        pnlStoriesBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LINE));

        JScrollPane scrollStories = new JScrollPane(pnlStoriesBar);
        scrollStories.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollStories.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollStories.setBorder(null);
        scrollStories.setPreferredSize(new Dimension(getWidth(), 105));
        feedRoot.add(scrollStories, BorderLayout.NORTH);

        pnlFeedCards = new JPanel();
        pnlFeedCards.setLayout(new BoxLayout(pnlFeedCards, BoxLayout.Y_AXIS));
        pnlFeedCards.setBackground(BG_PHONE);
        pnlFeedCards.setBorder(new EmptyBorder(10, 0, 20, 0));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setBackground(BG_PHONE);
        wrap.add(pnlFeedCards);

        JScrollPane scrollFeed = new JScrollPane(wrap);
        scrollFeed.setBorder(null);
        scrollFeed.getVerticalScrollBar().setUnitIncrement(16);
        feedRoot.add(scrollFeed, BorderLayout.CENTER);

        recargarTimeline();
        return feedRoot;
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

        Lista<Publicacion> postsOrdenados = new Lista<>();
        Nodo<String> na = autores.getHead();
        while (na != null) {
            String autor = na.getDato();
            Usuario uAutor = InstaFileManager.buscarUsuario(autor);
            if (uAutor != null && uAutor.isActivo()) {
                Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(autor);
                Nodo<Publicacion> np = posts.getHead();
                while (np != null) {
                    if (!np.getDato().isEsHistoria()) {
                        insertarOrdenadoPorFecha(postsOrdenados, np.getDato());
                    }
                    np = np.getSiguiente();
                }
            }
            na = na.getSiguiente();
        }

        Nodo<Publicacion> nodoPub = postsOrdenados.getHead();
        while (nodoPub != null) {
            pnlFeedCards.add(crearTarjetaPostMobile(nodoPub.getDato()));
            pnlFeedCards.add(Box.createVerticalStrut(14));
            nodoPub = nodoPub.getSiguiente();
        }

        if (postsOrdenados.estaVacia()) {
            JLabel lblVacio = new JLabel("No hay publicaciones en tu feed. Sigue a otros usuarios.", SwingConstants.CENTER);
            lblVacio.setForeground(TEXT_MUTED);
            lblVacio.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblVacio.setBorder(new EmptyBorder(40, 20, 20, 20));
            pnlFeedCards.add(lblVacio);
        }

        pnlStoriesBar.revalidate();
        pnlStoriesBar.repaint();
        pnlFeedCards.revalidate();
        pnlFeedCards.repaint();
    }

    private void insertarOrdenadoPorFecha(Lista<Publicacion> lista, Publicacion nueva) {
        if (lista.estaVacia()) {
            lista.agregar(nueva);
            return;
        }

        if (nueva.getFecha().getTime() >= lista.getHead().getDato().getFecha().getTime()) {
            Lista<Publicacion> nuevaLista = new Lista<>();
            nuevaLista.agregar(nueva);
            Nodo<Publicacion> cur = lista.getHead();
            while (cur != null) {
                nuevaLista.agregar(cur.getDato());
                cur = cur.getSiguiente();
            }
            while (!lista.estaVacia()) {
                lista.eliminar(lista.obtener(0));
            }
            Nodo<Publicacion> n = nuevaLista.getHead();
            while (n != null) {
                lista.agregar(n.getDato());
                n = n.getSiguiente();
            }
            return;
        }

        Nodo<Publicacion> actual = lista.getHead();
        int idx = 0;
        while (actual != null && actual.getDato().getFecha().getTime() > nueva.getFecha().getTime()) {
            actual = actual.getSiguiente();
            idx++;
        }

        Lista<Publicacion> temp = new Lista<>();
        Nodo<Publicacion> n = lista.getHead();
        int c = 0;
        while (n != null) {
            if (c == idx) temp.agregar(nueva);
            temp.agregar(n.getDato());
            n = n.getSiguiente();
            c++;
        }
        if (c == idx) temp.agregar(nueva);

        while (!lista.estaVacia()) {
            lista.eliminar(lista.obtener(0));
        }
        Nodo<Publicacion> nTemp = temp.getHead();
        while (nTemp != null) {
            lista.agregar(nTemp.getDato());
            nTemp = nTemp.getSiguiente();
        }
    }

    private JPanel crearBurbujaStory(String label, String username, boolean isMyStory) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        p.add(crearAvatarCircular(username, 54, !isMyStory, isMyStory ? "+" : null), BorderLayout.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isMyStory) {
                    cambiarPantalla("UPLOAD");
                } else {
                    usuarioPerfilVisitado = username;
                    cambiarPantalla("PERFIL");
                }
            }
        });
        return p;
    }

    private JPanel crearTarjetaPostMobile(Publicacion p) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(10, 12, 12, 12)
        ));

        int cardW = 390;
        int cardH = (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) ? 430 : 210;
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.setMaximumSize(new Dimension(cardW, cardH));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));

        header.add(crearAvatarCircular(p.getAutor(), 34, true, null), BorderLayout.WEST);

        JPanel postInfo = new JPanel(new GridLayout(2, 1, 0, 1));
        postInfo.setOpaque(false);

        JLabel lblAutor = new JLabel(p.getAutor() + " escribió:");
        lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAutor.setForeground(TEXT_WHITE);

        String carpeta = (p.getCarpetaPersonal() != null && !p.getCarpetaPersonal().equals("null")) ? p.getCarpetaPersonal() : "General";
        JLabel lblCarpeta = new JLabel("Carpeta: " + carpeta);
        lblCarpeta.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCarpeta.setForeground(TEXT_MUTED);

        postInfo.add(lblAutor);
        postInfo.add(lblCarpeta);
        header.add(postInfo, BorderLayout.CENTER);

        String fechaStr = new SimpleDateFormat("dd/MM/yy hh:mm a").format(p.getFecha());
        JLabel lblFecha = new JLabel(fechaStr);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblFecha.setForeground(TEXT_MUTED);
        header.add(lblFecha, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                usuarioPerfilVisitado = p.getAutor();
                cambiarPantalla("PERFIL");
            }
        });
        card.add(header, BorderLayout.NORTH);

        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image scaled = icon.getImage().getScaledInstance(366, 210, Image.SCALE_SMOOTH);
            JLabel lblImg = new JLabel(new ImageIcon(scaled));
            lblImg.setBorder(new EmptyBorder(8, 0, 8, 0));
            card.add(lblImg, BorderLayout.CENTER);
        }

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        String texto = p.getContenido().replaceAll("(#[\\w]+)", "<span style='color:#0095f6;'>$1</span>");
        texto = texto.replaceAll("(@[\\w]+)", "<span style='color:#ffffff; font-weight:bold;'>$1</span>");

        JLabel lblContenido = new JLabel("<html><body style='width:350px; color:#f1f5f9; font-size:11px; font-family:Segoe UI;'>"
                + "<b>@" + p.getAutor() + "</b> " + texto + "</body></html>");
        lblContenido.setBorder(new EmptyBorder(4, 4, 4, 4));
        footer.add(lblContenido);

        if (p.getSticker() != null && !p.getSticker().isEmpty() && !p.getSticker().equalsIgnoreCase("null")) {
            File fStk = new File(p.getSticker());
            if (fStk.exists()) {
                ImageIcon ic = new ImageIcon(new ImageIcon(fStk.getAbsolutePath()).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
                JLabel lblStkImg = new JLabel(ic);
                lblStkImg.setBorder(new EmptyBorder(4, 4, 4, 4));
                footer.add(lblStkImg);
            } else {
                JLabel lblSt = new JLabel(" Sticker: " + p.getSticker());
                lblSt.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblSt.setForeground(G_PINK);
                footer.add(lblSt);
            }
        }

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. BUSCADOR
    // =========================================================================
    private JPanel crearVistaBuscar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_PHONE);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JTextField txtSearch = new JTextField();
        estilizarCampoTexto(txtSearch);
        top.add(txtSearch);
        top.add(Box.createVerticalStrut(8));

        JPanel tabs = new JPanel(new GridLayout(1, 2, 8, 0));
        tabs.setOpaque(false);

        final boolean[] modoUsuarios = {true};

        JButton tabUsers = new JButton("Usuarios") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(modoUsuarios[0] ? BG_HOVER : BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(modoUsuarios[0] ? IG_BLUE : BORDER_LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tabUsers.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabUsers.setForeground(TEXT_WHITE);
        tabUsers.setContentAreaFilled(false);
        tabUsers.setBorderPainted(false);
        tabUsers.setFocusPainted(false);
        tabUsers.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton tabTags = new JButton("Hashtags") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!modoUsuarios[0] ? BG_HOVER : BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(!modoUsuarios[0] ? IG_BLUE : BORDER_LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tabTags.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabTags.setForeground(TEXT_WHITE);
        tabTags.setContentAreaFilled(false);
        tabTags.setBorderPainted(false);
        tabTags.setFocusPainted(false);
        tabTags.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabs.add(tabUsers);
        tabs.add(tabTags);
        top.add(tabs);

        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_SURFACE);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        list.setSelectionBackground(IG_BLUE);
        list.setSelectionForeground(Color.WHITE);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnVer = crearBotonGradiente("Ver Perfil Seleccionado", 220, 36);
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.setOpaque(false);
        bot.add(btnVer);
        p.add(bot, BorderLayout.SOUTH);

        Runnable ejecutarBusqueda = () -> {
            model.clear();
            String q = txtSearch.getText().trim().toLowerCase();

            if (modoUsuarios[0]) {
                Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
                Nodo<Usuario> n = users.getHead();
                while (n != null) {
                    Usuario u = n.getDato();
                    if (u.isActivo() && (q.isEmpty() || u.getUsername().toLowerCase().contains(q) || u.getNombreCompleto().toLowerCase().contains(q))) {
                        model.addElement("@" + u.getUsername() + " — " + u.getNombreCompleto() + " (" + u.getGenero() + ", " + u.getEdad() + "a)");
                    }
                    n = n.getSiguiente();
                }
            } else {
                Lista<String> ids = new Lista<>();
                Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
                Nodo<Usuario> nu = todos.getHead();
                while (nu != null) {
                    Usuario u = nu.getDato();
                    if (u.isActivo()) {
                        Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                        Nodo<Publicacion> np = posts.getHead();
                        while (np != null) {
                            Publicacion pub = np.getDato();
                            if (!ids.contiene(pub.getId())) {
                                boolean coincide = q.isEmpty();
                                if (!coincide) {
                                    Nodo<String> ntag = pub.getHashtags().getHead();
                                    while (ntag != null) {
                                        if (ntag.getDato().contains(q.replace("#", ""))) { coincide = true; break; }
                                        ntag = ntag.getSiguiente();
                                    }
                                }
                                if (coincide) {
                                    ids.agregar(pub.getId());
                                    model.addElement("# @" + pub.getAutor() + ": " + pub.getContenido());
                                }
                            }
                            np = np.getSiguiente();
                        }
                    }
                    nu = nu.getSiguiente();
                }
            }
        };

        tabUsers.addActionListener(e -> {
            modoUsuarios[0] = true;
            tabUsers.repaint();
            tabTags.repaint();
            btnVer.setVisible(true);
            ejecutarBusqueda.run();
        });

        tabTags.addActionListener(e -> {
            modoUsuarios[0] = false;
            tabUsers.repaint();
            tabTags.repaint();
            btnVer.setVisible(false);
            ejecutarBusqueda.run();
        });

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { ejecutarBusqueda.run(); }
        });

        btnVer.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null && sel.contains("@")) {
                int at = sel.indexOf("@");
                String u = sel.substring(at + 1).split("[ —\\s\\(]")[0].trim();
                usuarioPerfilVisitado = u;
                cambiarPantalla("PERFIL");
            }
        });

        ejecutarBusqueda.run();
        return p;
    }

    // =========================================================================
    // 3. CARGAR IMÁGENES / PUBLICAR
    // =========================================================================
    private JPanel crearVistaUpload() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG_SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTit = new JLabel("Crear Publicación", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        final String[] rutaSel = {null};
        JButton btnImg = crearBotonSecundario("Seleccionar Imagen desde equipo");
        btnImg.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnImg.setMaximumSize(new Dimension(340, 36));

        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File sel = fc.getSelectedFile();
                File destino = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/imagenes/" + sel.getName());
                try {
                    Files.copy(sel.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    rutaSel[0] = destino.getAbsolutePath();
                    btnImg.setText("Imagen: " + sel.getName());
                } catch (Exception ex) {
                    rutaSel[0] = sel.getAbsolutePath();
                }
            }
        });

        JLabel lblDesc = crearEtiquetaCampo("Descripción (#hashtags, @menciones):");
        JTextField txtDesc = new JTextField();
        estilizarCampoTexto(txtDesc);

        JLabel lblCarpeta = crearEtiquetaCampo("Carpeta de destino:");
        File uFolders = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/folders_personales");
        String[] carpetas = uFolders.list((dir, name) -> new File(dir, name).isDirectory());
        if (carpetas == null || carpetas.length == 0) carpetas = new String[]{"General", "Viajes", "Memes"};
        JComboBox<String> cbCarpetas = new JComboBox<>(carpetas);
        cbCarpetas.setBackground(BG_INPUT);
        cbCarpetas.setForeground(TEXT_WHITE);
        cbCarpetas.setMaximumSize(new Dimension(340, 34));

        final String[] stickerSel = {null};
        JButton btnStk = crearBotonSecundario("Adjuntar Sticker");
        btnStk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStk.setMaximumSize(new Dimension(340, 34));

        btnStk.addActionListener(e -> {
            mostrarSelectorStickers(st -> {
                stickerSel[0] = st;
                File f = new File(st);
                btnStk.setText("Sticker: " + (f.exists() ? f.getName() : st));
            });
        });

        JCheckBox chkHistoria = new JCheckBox("Publicar como Historia (Story)");
        chkHistoria.setForeground(TEXT_WHITE);
        chkHistoria.setOpaque(false);
        chkHistoria.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnPub = crearBotonGradiente("Compartir", 340, 38);
        btnPub.addActionListener(e -> {
            String txt = txtDesc.getText().trim();
            Publicacion p = new Publicacion(
                    usuarioActual.getUsername(),
                    txt,
                    rutaSel[0],
                    (String) cbCarpetas.getSelectedItem(),
                    stickerSel[0],
                    chkHistoria.isSelected(),
                    Publicacion.AspectRatio.CUADRADA
            );

            Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(usuarioActual.getUsername());
            posts.agregar(p);
            InstaFileManager.guardarPublicaciones(usuarioActual.getUsername(), posts);

            JOptionPane.showMessageDialog(this, "¡Publicación compartida con éxito!");
            txtDesc.setText("");
            rutaSel[0] = null;
            stickerSel[0] = null;
            btnImg.setText("Seleccionar Imagen desde equipo");
            btnStk.setText("Adjuntar Sticker");
            cambiarPantalla("TIMELINE");
        });

        form.add(lblTit);
        form.add(Box.createVerticalStrut(12));
        form.add(btnImg);
        form.add(Box.createVerticalStrut(8));
        form.add(lblDesc);
        form.add(txtDesc);
        form.add(Box.createVerticalStrut(8));
        form.add(lblCarpeta);
        form.add(cbCarpetas);
        form.add(Box.createVerticalStrut(10));
        form.add(btnStk);
        form.add(Box.createVerticalStrut(8));
        form.add(chkHistoria);
        form.add(Box.createVerticalStrut(14));
        form.add(btnPub);

        root.add(form, BorderLayout.NORTH);
        return root;
    }

    // =========================================================================
    // 4. NOTIFICACIONES
    // =========================================================================
    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(BG_PHONE);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel l = new JLabel("Notificaciones");
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(TEXT_WHITE);
        p.add(l, BorderLayout.NORTH);

        modelNotificaciones = new DefaultListModel<>();
        JList<String> list = new JList<>(modelNotificaciones);
        list.setBackground(BG_SURFACE);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        recargarNotificacionesEnVivo();
        return p;
    }

    private void recargarNotificacionesEnVivo() {
        if (modelNotificaciones == null) return;
        modelNotificaciones.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

        Lista<String> misSeguidores = InstaFileManager.cargarSeguidores(usuarioActual.getUsername());
        Nodo<String> nSeg = misSeguidores.getHead();
        while (nSeg != null) {
            String seguidor = nSeg.getDato();
            modelNotificaciones.addElement("👤 @" + seguidor + " comenzó a seguirte.");
            nSeg = nSeg.getSiguiente();
        }

        File fInbox = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/inbox.ins");
        Lista<MensajeInbox> inbox = InstaFileManager.cargarListaGenerica(fInbox);
        Nodo<MensajeInbox> nMsg = inbox.getHead();
        while (nMsg != null) {
            MensajeInbox m = nMsg.getDato();
            if (m.getReceptor().equalsIgnoreCase(usuarioActual.getUsername())) {
                String preview = m.getTexto();
                if (m.getTipo() == MensajeInbox.Tipo.STICKER) preview = "[Sticker]";
                else if (preview.length() > 25) preview = preview.substring(0, 25) + "...";
                
                String hora = sdf.format(m.getFecha());
                String estado = m.isLeido() ? "" : " 🔵 [Nuevo]";
                modelNotificaciones.addElement("💬 @" + m.getEmisor() + " te envió un mensaje" + estado + ": \"" + preview + "\" (" + hora + ")");
            }
            nMsg = nMsg.getSiguiente();
        }

        Lista<String> idsProcesados = new Lista<>();
        Lista<Usuario> todos = InstaFileManager.cargarUsuariosInsta();
        Nodo<Usuario> nu = todos.getHead();
        while (nu != null) {
            Usuario u = nu.getDato();
            if (u.isActivo()) {
                Lista<Publicacion> posts = InstaFileManager.cargarPublicaciones(u.getUsername());
                Nodo<Publicacion> np = posts.getHead();
                while (np != null) {
                    Publicacion pub = np.getDato();
                    if (!idsProcesados.contiene(pub.getId()) && pub.getMenciones().contiene(usuarioActual.getUsername().toLowerCase())) {
                        idsProcesados.agregar(pub.getId());
                        modelNotificaciones.addElement("🏷️ @" + pub.getAutor() + " te mencionó: \"" + pub.getContenido() + "\"");
                    }
                    np = np.getSiguiente();
                }
            }
            nu = nu.getSiguiente();
        }

        if (modelNotificaciones.isEmpty()) {
            modelNotificaciones.addElement("No tienes notificaciones pendientes.");
        }
    }

    // =========================================================================
    // 5. PERFIL DE USUARIO
    // =========================================================================
    private JPanel crearVistaPerfil() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);

        pnlPerfilHeader = new JPanel();
        pnlPerfilHeader.setLayout(new BoxLayout(pnlPerfilHeader, BoxLayout.Y_AXIS));
        pnlPerfilHeader.setBackground(BG_PHONE);
        pnlPerfilHeader.setBorder(new EmptyBorder(14, 16, 8, 16));

        pnlPerfilGrid = new JPanel(new GridLayout(0, 3, 4, 4));
        pnlPerfilGrid.setBackground(BG_PHONE);
        pnlPerfilGrid.setBorder(new EmptyBorder(8, 16, 20, 16));

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BG_PHONE);
        contentWrapper.add(pnlPerfilHeader, BorderLayout.NORTH);
        contentWrapper.add(pnlPerfilGrid, BorderLayout.CENTER);

        JPanel scrollAnchor = new JPanel(new BorderLayout());
        scrollAnchor.setBackground(BG_PHONE);
        scrollAnchor.add(contentWrapper, BorderLayout.NORTH);

        JScrollPane sc = new JScrollPane(scrollAnchor);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
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

        JPanel topInfoRow = new JPanel(new BorderLayout(12, 0));
        topInfoRow.setOpaque(false);
        topInfoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        topInfoRow.add(crearAvatarCircular(u.getUsername(), 74, true, null), BorderLayout.WEST);

        JPanel rightTextPanel = new JPanel();
        rightTextPanel.setLayout(new BoxLayout(rightTextPanel, BoxLayout.Y_AXIS));
        rightTextPanel.setOpaque(false);

        JLabel lblNombre = new JLabel(u.getNombreCompleto());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNombre.setForeground(TEXT_WHITE);

        JLabel lblUser = new JLabel("@" + u.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(TEXT_MUTED);

        JLabel lblEstado = new JLabel(u.isActivo() ? "• Cuenta activa" : "• Cuenta inactiva");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEstado.setForeground(u.isActivo() ? new Color(74, 222, 128) : new Color(248, 113, 113));

        rightTextPanel.add(lblNombre);
        rightTextPanel.add(Box.createVerticalStrut(2));
        rightTextPanel.add(lblUser);
        rightTextPanel.add(Box.createVerticalStrut(3));
        rightTextPanel.add(lblEstado);
        topInfoRow.add(rightTextPanel, BorderLayout.CENTER);

        if (!esPropio) {
            JButton btnTopDM = new JButton("DM") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? BG_HOVER : BG_INPUT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(IG_BLUE);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnTopDM.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnTopDM.setForeground(Color.WHITE);
            btnTopDM.setPreferredSize(new Dimension(65, 34));
            btnTopDM.setContentAreaFilled(false);
            btnTopDM.setBorderPainted(false);
            btnTopDM.setFocusPainted(false);
            btnTopDM.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnTopDM.setToolTipText("Enviar mensaje directo");

            btnTopDM.addActionListener(e -> {
                cambiarPantalla("INBOX");
                abrirChatConUsuario(u.getUsername());
            });

            JPanel pnlBtnDM = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
            pnlBtnDM.setOpaque(false);
            pnlBtnDM.add(btnTopDM);
            topInfoRow.add(pnlBtnDM, BorderLayout.EAST);
        }

        pnlPerfilHeader.add(topInfoRow);
        pnlPerfilHeader.add(Box.createVerticalStrut(12));

        JPanel rowStats = new JPanel(new GridLayout(1, 3, 8, 0));
        rowStats.setOpaque(false);
        rowStats.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowStats.setMaximumSize(new Dimension(380, 42));

        rowStats.add(crearCajaEstadistica(posts.getSize() + "", "Publicaciones"));
        rowStats.add(crearCajaEstadistica(followers.getSize() + "", "Seguidores"));
        rowStats.add(crearCajaEstadistica(following.getSize() + "", "Siguiendo"));

        pnlPerfilHeader.add(rowStats);
        pnlPerfilHeader.add(Box.createVerticalStrut(10));

        JPanel bioPanel = new JPanel();
        bioPanel.setLayout(new BoxLayout(bioPanel, BoxLayout.Y_AXIS));
        bioPanel.setOpaque(false);
        bioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String fechaReg = new SimpleDateFormat("dd/MM/yyyy").format(u.getFechaCreacion());
        String generoStr = (u.getGenero() == 'M' || u.getGenero() == 'm') ? "Masculino" : "Femenino";

        JLabel lblBio1 = new JLabel("Género: " + generoStr + "  •  Edad: " + u.getEdad() + " años");
        lblBio1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBio1.setForeground(TEXT_MUTED);

        JLabel lblBio2 = new JLabel("Miembro desde: " + fechaReg);
        lblBio2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBio2.setForeground(TEXT_MUTED);

        bioPanel.add(lblBio1);
        bioPanel.add(lblBio2);
        pnlPerfilHeader.add(bioPanel);
        pnlPerfilHeader.add(Box.createVerticalStrut(10));

        if (!esPropio) {
            Lista<String> misSeguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
            boolean loSigo = misSeguidos.contiene(u.getUsername().toLowerCase());

            JButton btnSeguir = new JButton(loSigo ? "Siguiendo" : "Seguir") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(loSigo ? BG_INPUT : IG_BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btnSeguir.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnSeguir.setForeground(Color.WHITE);
            btnSeguir.setContentAreaFilled(false);
            btnSeguir.setBorderPainted(false);
            btnSeguir.setFocusPainted(false);
            btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnSeguir.setMaximumSize(new Dimension(380, 36));
            btnSeguir.setPreferredSize(new Dimension(380, 36));
            btnSeguir.setAlignmentX(Component.LEFT_ALIGNMENT);

            btnSeguir.addActionListener(e -> {
                if (loSigo) {
                    int resp = JOptionPane.showConfirmDialog(this, "¿Deseas dejar de seguir a @" + u.getUsername() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        InstaFileManager.toggleSeguir(usuarioActual.getUsername(), u.getUsername());
                        if (socketCliente != null) {
                            socketCliente.notificarSeguimiento(usuarioActual.getUsername(), u.getUsername());
                        }
                        recargarPerfil();
                    }
                } else {
                    InstaFileManager.toggleSeguir(usuarioActual.getUsername(), u.getUsername());
                    if (socketCliente != null) {
                        socketCliente.notificarSeguimiento(usuarioActual.getUsername(), u.getUsername());
                    }
                    recargarPerfil();
                }
            });
            pnlPerfilHeader.add(btnSeguir);
        } else {
            JButton btnEdit = crearBotonSecundario("Editar perfil");
            btnEdit.setMaximumSize(new Dimension(380, 36));
            btnEdit.setPreferredSize(new Dimension(380, 36));
            btnEdit.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnEdit.addActionListener(e -> cambiarPantalla("EDIT_PROFILE"));
            pnlPerfilHeader.add(btnEdit);
        }

        pnlPerfilHeader.add(Box.createVerticalStrut(12));

        JPanel divPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        divPanel.setOpaque(false);
        divPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LINE));
        divPanel.setMaximumSize(new Dimension(380, 24));
        divPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGridTab = new JLabel("PUBLICACIONES");
        lblGridTab.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblGridTab.setForeground(TEXT_MUTED);
        divPanel.add(lblGridTab);
        pnlPerfilHeader.add(divPanel);

        for (int i = posts.getSize() - 1; i >= 0; i--) {
            Publicacion pub = posts.obtener(i);
            if (!pub.isEsHistoria()) {
                JPanel gItem = new JPanel(new BorderLayout());
                gItem.setBackground(BG_SURFACE);
                gItem.setPreferredSize(new Dimension(115, 115));
                gItem.setBorder(BorderFactory.createLineBorder(BORDER_LINE));

                if (pub.getRutaImagen() != null && new File(pub.getRutaImagen()).exists()) {
                    ImageIcon ic = new ImageIcon(new ImageIcon(pub.getRutaImagen()).getImage().getScaledInstance(115, 115, Image.SCALE_SMOOTH));
                    gItem.add(new JLabel(ic), BorderLayout.CENTER);
                } else {
                    JLabel lbl = new JLabel("<html><center style='color:#cbd5e1; font-size:9px; padding:6px;'>" + pub.getContenido() + "</center></html>", SwingConstants.CENTER);
                    gItem.add(lbl, BorderLayout.CENTER);
                }
                pnlPerfilGrid.add(gItem);
            }
        }

        pnlPerfilHeader.revalidate();
        pnlPerfilHeader.repaint();
        pnlPerfilGrid.revalidate();
        pnlPerfilGrid.repaint();
    }

    private JPanel crearCajaEstadistica(String numero, String label) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(BORDER_LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblNum = new JLabel(numero, SwingConstants.CENTER);
        lblNum.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNum.setForeground(TEXT_WHITE);

        JLabel lblTxt = new JLabel(label, SwingConstants.CENTER);
        lblTxt.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTxt.setForeground(TEXT_MUTED);

        p.add(lblNum);
        p.add(lblTxt);
        return p;
    }

    // =========================================================================
    // 6. INBOX (BANDEJA DE ENTRADA CON MARCAR LEÍDOS + CHAT)
    // =========================================================================
    private JPanel crearVistaInbox() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);

        inboxCardLayout = new CardLayout();
        inboxContainer = new JPanel(inboxCardLayout);
        inboxContainer.setOpaque(false);

        inboxContainer.add(crearVistaBandejaConversaciones(), "LISTA_CHATS");
        inboxContainer.add(crearVistaChatPrivado(), "SALA_CHAT");

        root.add(inboxContainer, BorderLayout.CENTER);
        return root;
    }

    private JPanel crearVistaBandejaConversaciones() {
        JPanel pnlBandeja = new JPanel(new BorderLayout());
        pnlBandeja.setBackground(BG_PHONE);

        JPanel topBandeja = new JPanel(new BorderLayout(0, 8));
        topBandeja.setBackground(BG_SURFACE);
        topBandeja.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel lblTit = new JLabel("Mensajes Directos");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTit.setForeground(TEXT_WHITE);

        txtBuscarChats = new JTextField();
        estilizarCampoTexto(txtBuscarChats);
        txtBuscarChats.setToolTipText("Buscar chats...");

        txtBuscarChats.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { recargarBandejaChats(); }
        });

        topBandeja.add(lblTit, BorderLayout.NORTH);
        topBandeja.add(txtBuscarChats, BorderLayout.SOUTH);
        pnlBandeja.add(topBandeja, BorderLayout.NORTH);

        pnlListaConversaciones = new JPanel();
        pnlListaConversaciones.setLayout(new BoxLayout(pnlListaConversaciones, BoxLayout.Y_AXIS));
        pnlListaConversaciones.setBackground(BG_PHONE);

        JScrollPane scrollBandeja = new JScrollPane(pnlListaConversaciones);
        scrollBandeja.setBorder(null);
        scrollBandeja.getVerticalScrollBar().setUnitIncrement(14);
        pnlBandeja.add(scrollBandeja, BorderLayout.CENTER);

        return pnlBandeja;
    }

    private JPanel crearVistaChatPrivado() {
        JPanel pnlChat = new JPanel(new BorderLayout());
        pnlChat.setBackground(BG_PHONE);

        JPanel topChat = new JPanel(new BorderLayout(8, 0));
        topChat.setBackground(BG_SURFACE);
        topChat.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel leftChatInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftChatInfo.setOpaque(false);

        JButton btnVolverBandeja = new JButton("←") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BG_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnVolverBandeja.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnVolverBandeja.setForeground(TEXT_WHITE);
        btnVolverBandeja.setContentAreaFilled(false);
        btnVolverBandeja.setBorderPainted(false);
        btnVolverBandeja.setFocusPainted(false);
        btnVolverBandeja.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolverBandeja.addActionListener(e -> {
            inboxCardLayout.show(inboxContainer, "LISTA_CHATS");
            recargarBandejaChats();
        });

        lblChatHeaderUser = new JLabel("@" + chatUsuarioSeleccionado);
        lblChatHeaderUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblChatHeaderUser.setForeground(TEXT_WHITE);

        leftChatInfo.add(btnVolverBandeja);
        leftChatInfo.add(lblChatHeaderUser);
        topChat.add(leftChatInfo, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightActions.setOpaque(false);

        // BOTÓN: MARCAR MENSAJES COMO LEÍDOS
        JButton btnMarcarLeido = crearBotonSecundario("✓ Marcar leído");
        btnMarcarLeido.setToolTipText("Marcar todos los mensajes como leídos");
        btnMarcarLeido.addActionListener(e -> {
            InstaFileManager.marcarConversacionComoLeida(usuarioActual.getUsername(), chatUsuarioSeleccionado);
            recargarMensajesChat();
            JOptionPane.showMessageDialog(this, "Mensajes con @" + chatUsuarioSeleccionado + " marcados como leídos.");
        });

        JButton btnEliminar = crearBotonSecundario("Eliminar");
        btnEliminar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar todos los mensajes con @" + chatUsuarioSeleccionado + "?", "Eliminar Chat", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                InstaFileManager.eliminarConversacionCompleta(usuarioActual.getUsername(), chatUsuarioSeleccionado);
                inboxCardLayout.show(inboxContainer, "LISTA_CHATS");
                recargarBandejaChats();
            }
        });

        rightActions.add(btnMarcarLeido);
        rightActions.add(btnEliminar);
        topChat.add(rightActions, BorderLayout.EAST);
        pnlChat.add(topChat, BorderLayout.NORTH);

        pnlChatStream = new JPanel();
        pnlChatStream.setLayout(new BoxLayout(pnlChatStream, BoxLayout.Y_AXIS));
        pnlChatStream.setBackground(BG_PHONE);
        pnlChatStream.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scrollChat = new JScrollPane(pnlChatStream);
        scrollChat.setBorder(null);
        scrollChat.getVerticalScrollBar().setUnitIncrement(14);
        pnlChat.add(scrollChat, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setBackground(BG_SURFACE);
        inputRow.setBorder(new EmptyBorder(8, 10, 8, 10));

        JTextField txtMsg = new JTextField();
        estilizarCampoTexto(txtMsg);

        JPanel btnActs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnActs.setOpaque(false);

        JButton btnStk = crearBotonSecundario("Sticker");
        JButton btnSend = crearBotonGradiente("Enviar", 75, 34);

        btnActs.add(btnStk);
        btnActs.add(btnSend);
        inputRow.add(txtMsg, BorderLayout.CENTER);
        inputRow.add(btnActs, BorderLayout.EAST);
        pnlChat.add(inputRow, BorderLayout.SOUTH);

        ActionListener enviar = e -> {
            String txt = txtMsg.getText().trim();
            if (!txt.isEmpty()) {
                if (txt.length() > 300) txt = txt.substring(0, 300);
                if (socketCliente != null) {
                    socketCliente.enviarMensajeChat(usuarioActual.getUsername(), chatUsuarioSeleccionado, txt, false);
                } else {
                    InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, txt, MensajeInbox.Tipo.TEXTO);
                }
                txtMsg.setText("");
                recargarMensajesChat();
            }
        };
        btnSend.addActionListener(enviar);
        txtMsg.addActionListener(enviar);

        btnStk.addActionListener(e -> {
            mostrarSelectorStickers(stk -> {
                if (socketCliente != null) {
                    socketCliente.enviarMensajeChat(usuarioActual.getUsername(), chatUsuarioSeleccionado, stk, true);
                } else {
                    InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, stk, MensajeInbox.Tipo.STICKER);
                }
                recargarMensajesChat();
            });
        });

        return pnlChat;
    }

    public synchronized void recargarChat() {
        recargarBandejaChats();
        recargarMensajesChat();
    }

    private synchronized void recargarBandejaChats() {
        if (pnlListaConversaciones == null) return;
        pnlListaConversaciones.removeAll();

        String q = (txtBuscarChats != null) ? txtBuscarChats.getText().trim().toLowerCase() : "";
        Lista<String> contactosValidos = new Lista<>();

        File fInbox = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/inbox.ins");
        Lista<MensajeInbox> todosMensajes = InstaFileManager.cargarListaGenerica(fInbox);
        Nodo<MensajeInbox> nm = todosMensajes.getHead();
        while (nm != null) {
            MensajeInbox m = nm.getDato();
            String otro = m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername()) ? m.getReceptor() : m.getEmisor();
            if (!contactosValidos.contiene(otro.toLowerCase()) && !otro.equalsIgnoreCase(usuarioActual.getUsername())) {
                contactosValidos.agregar(otro.toLowerCase());
            }
            nm = nm.getSiguiente();
        }

        Lista<String> seguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
        Nodo<String> ns = seguidos.getHead();
        while (ns != null) {
            String s = ns.getDato().toLowerCase();
            if (!contactosValidos.contiene(s) && !s.equalsIgnoreCase(usuarioActual.getUsername())) {
                contactosValidos.agregar(s);
            }
            ns = ns.getSiguiente();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        int totalMostrados = 0;

        Nodo<String> nc = contactosValidos.getHead();
        while (nc != null) {
            String targetUser = nc.getDato();
            Usuario uObj = InstaFileManager.buscarUsuario(targetUser);

            if (uObj != null && uObj.isActivo()) {
                if (q.isEmpty() || targetUser.contains(q) || uObj.getNombreCompleto().toLowerCase().contains(q)) {
                    totalMostrados++;

                    int noLeidos = InstaFileManager.contarMensajesNoLeidos(usuarioActual.getUsername(), uObj.getUsername());

                    JPanel rowConv = new JPanel(new BorderLayout(10, 0));
                    rowConv.setBackground(BG_SURFACE);
                    rowConv.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LINE),
                            new EmptyBorder(10, 14, 10, 14)
                    ));
                    rowConv.setCursor(new Cursor(Cursor.HAND_CURSOR));

                    rowConv.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            rowConv.setBackground(BG_HOVER);
                        }
                        @Override
                        public void mouseExited(MouseEvent e) {
                            rowConv.setBackground(BG_SURFACE);
                        }
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            abrirChatConUsuario(uObj.getUsername());
                        }
                    });

                    rowConv.add(crearAvatarCircular(uObj.getUsername(), 44, false, null), BorderLayout.WEST);

                    File fChat = new File(InstaFileManager.RUTA_INSTA + "/" + usuarioActual.getUsername() + "/inbox.ins");
                    Lista<MensajeInbox> todos = InstaFileManager.cargarListaGenerica(fChat);
                    Lista<MensajeInbox> chatHist = new Lista<>();
                    Nodo<MensajeInbox> nmsg = todos.getHead();
                    while (nmsg != null) {
                        MensajeInbox m = nmsg.getDato();
                        if ((m.getEmisor().equalsIgnoreCase(usuarioActual.getUsername()) && m.getReceptor().equalsIgnoreCase(uObj.getUsername())) ||
                            (m.getEmisor().equalsIgnoreCase(uObj.getUsername()) && m.getReceptor().equalsIgnoreCase(usuarioActual.getUsername()))) {
                            chatHist.agregar(m);
                        }
                        nmsg = nmsg.getSiguiente();
                    }

                    String ultimoTxt = "Inicia una conversación...";
                    String horaTxt = "";
                    if (!chatHist.estaVacia()) {
                        MensajeInbox lastM = chatHist.obtener(chatHist.getSize() - 1);
                        ultimoTxt = lastM.getTipo() == MensajeInbox.Tipo.STICKER ? "[Sticker]" : lastM.getTexto();
                        if (ultimoTxt.length() > 24) ultimoTxt = ultimoTxt.substring(0, 24) + "...";
                        horaTxt = sdf.format(lastM.getFecha());
                    }

                    JPanel centerText = new JPanel(new GridLayout(2, 1, 0, 2));
                    centerText.setOpaque(false);

                    JLabel lblName = new JLabel(uObj.getNombreCompleto());
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lblName.setForeground(TEXT_WHITE);

                    JLabel lblSub = new JLabel("@" + uObj.getUsername() + " • " + ultimoTxt);
                    lblSub.setFont(new Font("Segoe UI", noLeidos > 0 ? Font.BOLD : Font.PLAIN, 11));
                    lblSub.setForeground(noLeidos > 0 ? Color.WHITE : TEXT_MUTED);

                    centerText.add(lblName);
                    centerText.add(lblSub);
                    rowConv.add(centerText, BorderLayout.CENTER);

                    JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 2));
                    rightPanel.setOpaque(false);

                    if (!horaTxt.isEmpty()) {
                        JLabel lblHora = new JLabel(horaTxt, SwingConstants.RIGHT);
                        lblHora.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                        lblHora.setForeground(TEXT_MUTED);
                        rightPanel.add(lblHora);
                    }

                    if (noLeidos > 0) {
                        JLabel lblBadge = new JLabel("🔵 " + noLeidos + " nuevo" + (noLeidos > 1 ? "s" : ""), SwingConstants.RIGHT);
                        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        lblBadge.setForeground(IG_BLUE);
                        rightPanel.add(lblBadge);
                    }

                    rowConv.add(rightPanel, BorderLayout.EAST);
                    pnlListaConversaciones.add(rowConv);
                }
            }
            nc = nc.getSiguiente();
        }

        if (totalMostrados == 0) {
            JLabel lblVacio = new JLabel("<html><center style='color:#a0a0a5; font-size:12px; padding:30px;'>"
                    + "No tienes conversaciones activas.<br>Envía un DM desde el perfil de un usuario.</center></html>", SwingConstants.CENTER);
            pnlListaConversaciones.add(lblVacio);
        }

        pnlListaConversaciones.revalidate();
        pnlListaConversaciones.repaint();
    }

    public void abrirChatConUsuario(String username) {
        this.chatUsuarioSeleccionado = username;
        if (lblChatHeaderUser != null) {
            lblChatHeaderUser.setText("@" + chatUsuarioSeleccionado);
        }
        if (inboxCardLayout != null && inboxContainer != null) {
            inboxCardLayout.show(inboxContainer, "SALA_CHAT");
        }
        recargarMensajesChat();
    }

    private synchronized void recargarMensajesChat() {
        if (pnlChatStream == null) return;
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

            JPanel bubble = new JPanel(new BorderLayout(0, 2));
            bubble.setBackground(esMio ? IG_BLUE : BG_INPUT);
            bubble.setBorder(new EmptyBorder(6, 10, 6, 10));

            if (m.getTipo() == MensajeInbox.Tipo.STICKER) {
                File f = new File(m.getTexto());
                if (f.exists()) {
                    ImageIcon ic = new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(85, 85, Image.SCALE_SMOOTH));
                    bubble.add(new JLabel(ic), BorderLayout.CENTER);
                } else {
                    JLabel lblStkText = new JLabel("[" + m.getTexto() + "]");
                    lblStkText.setForeground(Color.WHITE);
                    lblStkText.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    bubble.add(lblStkText, BorderLayout.CENTER);
                }
            } else {
                JLabel lblMsg = new JLabel("<html><body style='max-width:240px; color:#ffffff; font-size:11px; font-family:Segoe UI;'>"
                        + m.getTexto() + "</body></html>");
                bubble.add(lblMsg, BorderLayout.CENTER);
            }

            JLabel lblH = new JLabel(hora + (esMio ? (m.isLeido() ? " • Visto" : "") : (m.isLeido() ? " • Leído" : "")), SwingConstants.RIGHT);
            lblH.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            lblH.setForeground(new Color(200, 200, 200));
            bubble.add(lblH, BorderLayout.SOUTH);

            row.add(bubble);
            pnlChatStream.add(row);
            pnlChatStream.add(Box.createVerticalStrut(6));
            n = n.getSiguiente();
        }

        pnlChatStream.revalidate();
        pnlChatStream.repaint();
    }

    // =========================================================================
    // MODAL DE STICKERS
    // =========================================================================
    private void mostrarSelectorStickers(java.util.function.Consumer<String> callback) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dlg;
        if (parentWindow instanceof Frame) {
            dlg = new JDialog((Frame) parentWindow, "Stickers Pack", true);
        } else if (parentWindow instanceof Dialog) {
            dlg = new JDialog((Dialog) parentWindow, "Stickers Pack", true);
        } else {
            dlg = new JDialog((Frame) null, "Stickers Pack", true);
        }

        dlg.setSize(400, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 8));
        dlg.getContentPane().setBackground(BG_SURFACE);

        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(12, 16, 4, 16));

        JLabel lblTit = new JLabel("Galería de Stickers");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTit.setForeground(TEXT_WHITE);
        topPanel.add(lblTit, BorderLayout.WEST);

        JButton btnImportar = crearBotonGradiente("+ Importar Sticker", 150, 32);
        topPanel.add(btnImportar, BorderLayout.EAST);
        dlg.add(topPanel, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
        grid.setBackground(BG_SURFACE);
        grid.setBorder(new EmptyBorder(10, 16, 16, 16));

        Runnable cargarGrid = () -> {
            grid.removeAll();
            Lista<Stickers> stickers = InstaFileManager.cargarStickers(usuarioActual.getUsername());

            for (int i = 0; i < stickers.getSize(); i++) {
                Stickers stkObj = stickers.obtener(i);

                JButton btnStkItem = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getModel().isRollover() ? BG_HOVER : BG_INPUT);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        g2.setColor(BORDER_LINE);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btnStkItem.setContentAreaFilled(false);
                btnStkItem.setBorderPainted(false);
                btnStkItem.setFocusPainted(false);
                btnStkItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnStkItem.setPreferredSize(new Dimension(95, 95));

                if (stkObj.getRutaArchivo() != null && new File(stkObj.getRutaArchivo()).exists()) {
                    ImageIcon raw = new ImageIcon(stkObj.getRutaArchivo());
                    Image img = raw.getImage().getScaledInstance(68, 68, Image.SCALE_SMOOTH);
                    btnStkItem.setIcon(new ImageIcon(img));
                    btnStkItem.setToolTipText(stkObj.getNombre());
                } else {
                    btnStkItem.setText(stkObj.getNombre());
                    btnStkItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btnStkItem.setForeground(TEXT_WHITE);
                }

                btnStkItem.addActionListener(e -> {
                    String val = (stkObj.getRutaArchivo() != null && new File(stkObj.getRutaArchivo()).exists())
                            ? stkObj.getRutaArchivo()
                            : stkObj.getNombre();
                    callback.accept(val);
                    dlg.dispose();
                });
                grid.add(btnStkItem);
            }
            grid.revalidate();
            grid.repaint();
        };

        btnImportar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Importar Sticker a tu cuenta (.png o .jpg)");
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                File archivoSel = fc.getSelectedFile();
                boolean exito = InstaFileManager.agregarStickerPersonal(usuarioActual.getUsername(), archivoSel);
                if (exito) {
                    JOptionPane.showMessageDialog(dlg, "¡Sticker agregado a tus stickers personales!");
                    cargarGrid.run();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Error: El archivo debe ser una imagen con formato .png o .jpg.", "Formato Inválido", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cargarGrid.run();

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(sc, BorderLayout.CENTER);

        dlg.setVisible(true);
    }

    // =========================================================================
    // 7. EDITAR PERFIL (CON CONFIRMAR CONTRASEÑA AGREGADO)
    // =========================================================================
    private JPanel crearVistaEditarPerfil() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);
        root.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        JLabel lblTit = new JLabel("Editar Perfil", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNom = crearEtiquetaCampo("Nombre completo:");
        JTextField txtNom = new JTextField(u.getNombreCompleto());
        estilizarCampoTexto(txtNom);

        JLabel lblPass = crearEtiquetaCampo("Nueva Contraseña:");
        JPasswordField txtPass = new JPasswordField(u.getPass());
        JPanel passRow = crearCampoPasswordConOjo(txtPass);

        JLabel lblPassConfirm = crearEtiquetaCampo("Confirmar Contraseña:");
        JPasswordField txtPassConfirm = new JPasswordField(u.getPass());
        JPanel passConfirmRow = crearCampoPasswordConOjo(txtPassConfirm);

        JLabel lblEdad = crearEtiquetaCampo("Edad:");
        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(u.getEdad(), 1, 120, 1));
        spinEdad.setMaximumSize(new Dimension(340, 32));

        JLabel lblGen = crearEtiquetaCampo("Género:");
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
        cbGen.setSelectedItem(String.valueOf(u.getGenero()));
        cbGen.setMaximumSize(new Dimension(340, 32));

        JButton btnGuardar = crearBotonGradiente("Guardar Cambios", 340, 36);
        JButton btnDesactivar = crearBotonSecundario(u.isActivo() ? "Desactivar Cuenta" : "Reactivar Cuenta");
        btnDesactivar.setMaximumSize(new Dimension(340, 34));
        btnDesactivar.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnGuardar.addActionListener(e -> {
            Usuario usr = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
            if (usr != null) {
                String nuevaPass = new String(txtPass.getPassword());
                String nuevaPassConf = new String(txtPassConfirm.getPassword());

                if (!nuevaPass.equals(nuevaPassConf)) {
                    JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden. Por favor verifícalas.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!esPasswordValido(nuevaPass)) {
                    JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 8 caracteres y contener un número o símbolo.", "Contraseña Insegura", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                usr.setNombreCompleto(txtNom.getText().trim());
                usr.setPass(nuevaPass);
                usr.setEdad((Integer) spinEdad.getValue());
                usr.setGenero(((String) cbGen.getSelectedItem()).charAt(0));
                InstaFileManager.actualizarUsuario(usr);
                JOptionPane.showMessageDialog(this, "Perfil actualizado con éxito.");
                cambiarPantalla("PERFIL");
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
                        cambiarPantalla("PERFIL");
                    }
                } else {
                    usr.setActivo(true);
                    InstaFileManager.actualizarUsuario(usr);
                    JOptionPane.showMessageDialog(this, "¡Cuenta reactivada!");
                    btnDesactivar.setText("Desactivar Cuenta");
                    cambiarPantalla("PERFIL");
                }
            }
        });

        card.add(lblTit);
        card.add(Box.createVerticalStrut(10));
        card.add(lblNom);
        card.add(txtNom);
        card.add(Box.createVerticalStrut(6));
        card.add(lblPass);
        card.add(passRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblPassConfirm);
        card.add(passConfirmRow);
        card.add(Box.createVerticalStrut(6));
        card.add(lblEdad);
        card.add(spinEdad);
        card.add(Box.createVerticalStrut(6));
        card.add(lblGen);
        card.add(cbGen);
        card.add(Box.createVerticalStrut(12));
        card.add(btnGuardar);
        card.add(Box.createVerticalStrut(8));
        card.add(btnDesactivar);

        root.add(card, BorderLayout.NORTH);
        return root;
    }

    // =========================================================================
    // 8. CONFIGURACIÓN Y CERRAR SESIÓN
    // =========================================================================
    private JPanel crearVistaConfiguracion() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(BG_SURFACE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTit = new JLabel("Configuración", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnItemCuenta = crearBotonSecundario("Cuenta y Privacidad");
        btnItemCuenta.setMaximumSize(new Dimension(320, 38));
        btnItemCuenta.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnItemCuenta.addActionListener(e -> cambiarPantalla("EDIT_PROFILE"));

        JButton btnItemNotif = crearBotonSecundario("Notificaciones");
        btnItemNotif.setMaximumSize(new Dimension(320, 38));
        btnItemNotif.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnItemNotif.addActionListener(e -> cambiarPantalla("NOTIFICACIONES"));

        JButton btnItemLogout = new JButton("Cerrar Sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 20, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(150, 40, 40));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnItemLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnItemLogout.setForeground(new Color(248, 113, 113));
        btnItemLogout.setContentAreaFilled(false);
        btnItemLogout.setBorderPainted(false);
        btnItemLogout.setMaximumSize(new Dimension(320, 40));
        btnItemLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnItemLogout.setFocusPainted(false);
        btnItemLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnItemLogout.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que quieres cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                if (socketCliente != null) socketCliente.desconectar();
                rootCardLayout.show(rootContainer, "AUTH");
                authCardLayout.show(authContainer, "LANDING");
            }
        });

        pnl.add(lblTit);
        pnl.add(Box.createVerticalStrut(20));
        pnl.add(btnItemCuenta);
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(btnItemNotif);
        pnl.add(Box.createVerticalStrut(25));
        pnl.add(btnItemLogout);

        root.add(pnl, BorderLayout.NORTH);
        return root;
    }

    // =========================================================================
    // VISTA DE AUTENTICACIÓN
    // =========================================================================
    private JPanel crearVistaAutenticacionMobile() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PHONE);

        authCardLayout = new CardLayout();
        authContainer = new JPanel(authCardLayout);
        authContainer.setOpaque(false);
        authContainer.setPreferredSize(new Dimension(380, 560));

        authContainer.add(crearCardLandingOpciones(), "LANDING");
        authContainer.add(crearCardLoginInsta(), "LOGIN");
        authContainer.add(crearCardRegistroInsta(), "REGISTRO");

        panel.add(authContainer);
        authCardLayout.show(authContainer, "LANDING");
        return panel;
    }

    private JPanel crearCardLandingOpciones() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(40, 25, 40, 25)
        ));

        JLabel lblLogo = new JLabel("INSTA+", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, G_ORANGE, getWidth(), 0, G_PINK));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, fm.getAscent());
                g2.dispose();
            }
        };
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 36));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSlogan = new JLabel("Comparte momentos con amigos", SwingConstants.CENTER);
        lblSlogan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSlogan.setForeground(TEXT_MUTED);
        lblSlogan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnIrLogin = crearBotonGradiente("Iniciar Sesión", 300, 40);
        btnIrLogin.addActionListener(e -> authCardLayout.show(authContainer, "LOGIN"));

        JButton btnIrReg = crearBotonSecundario("Crear Cuenta Nueva");
        btnIrReg.setPreferredSize(new Dimension(300, 40));
        btnIrReg.setMaximumSize(new Dimension(300, 40));
        btnIrReg.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIrReg.addActionListener(e -> authCardLayout.show(authContainer, "REGISTRO"));

        card.add(Box.createVerticalStrut(20));
        card.add(lblLogo);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSlogan);
        card.add(Box.createVerticalStrut(50));
        card.add(btnIrLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(btnIrReg);

        return card;
    }

    private JPanel crearCardLoginInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(30, 25, 30, 25)
        ));

        JLabel lblLogo = new JLabel("INSTA+", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 28));
        lblLogo.setForeground(TEXT_WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = crearEtiquetaCampo("Usuario (Username):");
        JTextField txtUser = new JTextField();
        estilizarCampoTexto(txtUser);

        JLabel lblPass = crearEtiquetaCampo("Contraseña:");
        JPasswordField txtPass = new JPasswordField();
        JPanel passRow = crearCampoPasswordConOjo(txtPass);

        JButton btnLogin = crearBotonGradiente("Iniciar Sesión", 300, 38);
        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            try {
                Usuario userAuth = InstaFileManager.autenticarInsta(u, p);
                if (userAuth != null) {
                    usuarioActual = userAuth;
                    usuarioPerfilVisitado = userAuth.getUsername();
                    iniciarConexionSocket();
                    rootCardLayout.show(rootContainer, "APP");
                    cambiarPantalla("TIMELINE");
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (CuentaDesactivadaException ex) {
                int resp = JOptionPane.showConfirmDialog(this, ex.getMessage() + "\n¿Deseas reactivar tu cuenta?", "Cuenta Desactivada", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    Usuario inact = InstaFileManager.buscarUsuario(u);
                    if (inact != null) {
                        inact.setActivo(true);
                        InstaFileManager.actualizarUsuario(inact);
                        JOptionPane.showMessageDialog(this, "¡Cuenta reactivada! Ya puedes entrar.");
                    }
                }
            }
        });

        JButton btnVolver = new JButton("Volver a inicio");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(TEXT_MUTED);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> authCardLayout.show(authContainer, "LANDING"));

        card.add(lblLogo);
        card.add(Box.createVerticalStrut(18));
        card.add(lblUser);
        card.add(txtUser);
        card.add(Box.createVerticalStrut(8));
        card.add(lblPass);
        card.add(passRow);
        card.add(Box.createVerticalStrut(18));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(btnVolver);

        return card;
    }

    private JPanel crearCardRegistroInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(16, 25, 16, 25)
        ));

        JLabel lblTit = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNom = crearEtiquetaCampo("Nombre completo:");
        JTextField txtNombre = new JTextField();
        estilizarCampoTexto(txtNombre);

        JLabel lblUsr = crearEtiquetaCampo("Username único:");
        JTextField txtUser = new JTextField();
        estilizarCampoTexto(txtUser);

        JLabel lblPass = crearEtiquetaCampo("Contraseña (mín. 8 caracteres, 1 número o símbolo):");
        JPasswordField txtPass = new JPasswordField();
        JPanel passRow = crearCampoPasswordConOjo(txtPass);

        JLabel lblPassConfirm = crearEtiquetaCampo("Confirmar contraseña:");
        JPasswordField txtPassConfirm = new JPasswordField();
        JPanel passConfirmRow = crearCampoPasswordConOjo(txtPassConfirm);

        JPanel rowGenEdad = new JPanel(new GridLayout(1, 2, 8, 0));
        rowGenEdad.setOpaque(false);
        rowGenEdad.setMaximumSize(new Dimension(300, 34));

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(20, 13, 100, 1));
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"Género: M", "Género: F"});
        rowGenEdad.add(spinEdad);
        rowGenEdad.add(cbGen);

        final String[] rutaFoto = {null};
        JButton btnFoto = crearBotonSecundario("Seleccionar Foto de Perfil");
        btnFoto.setMaximumSize(new Dimension(300, 32));
        btnFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto[0] = fc.getSelectedFile().getAbsolutePath();
                btnFoto.setText("Foto: " + fc.getSelectedFile().getName());
            }
        });

        JButton btnRegistrar = crearBotonGradiente("Registrarse", 300, 36);
        btnRegistrar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String usr = txtUser.getText().trim().toLowerCase();
            String pas = new String(txtPass.getPassword());
            String pasConf = new String(txtPassConfirm.getPassword());
            char gen = cbGen.getSelectedIndex() == 0 ? 'M' : 'F';
            int edad = (Integer) spinEdad.getValue();

            if (nom.isEmpty() || usr.isEmpty() || pas.isEmpty() || pasConf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor completa todos los campos requeridos.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pas.equals(pasConf)) {
                JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden. Por favor verifícalas.", "Error de Contraseña", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!esPasswordValido(pas)) {
                JOptionPane.showMessageDialog(this, 
                    "La contraseña no es válida:\n• Debe tener al menos 8 caracteres.\n• Debe incluir al menos un número (0-9) o un carácter especial (!@#$%...).", 
                    "Contraseña Insegura", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Usuario nuevo = new Usuario(usr, pas, false, nom, gen, edad, rutaFoto[0]);
            if (InstaFileManager.registrarUsuarioInsta(nuevo)) {
                JOptionPane.showMessageDialog(this, "¡Cuenta @" + usr + " creada con éxito!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
                usuarioActual = nuevo;
                usuarioPerfilVisitado = nuevo.getUsername();
                iniciarConexionSocket();
                rootCardLayout.show(rootContainer, "APP");
                cambiarPantalla("TIMELINE");
            } else {
                JOptionPane.showMessageDialog(this, "Ese username @" + usr + " ya está en uso. Elige otro.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnVolver = new JButton("Volver a inicio");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(TEXT_MUTED);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> authCardLayout.show(authContainer, "LANDING"));

        card.add(lblTit);
        card.add(Box.createVerticalStrut(6));
        card.add(lblNom);
        card.add(txtNombre);
        card.add(Box.createVerticalStrut(4));
        card.add(lblUsr);
        card.add(txtUser);
        card.add(Box.createVerticalStrut(4));
        card.add(lblPass);
        card.add(passRow);
        card.add(Box.createVerticalStrut(4));
        card.add(lblPassConfirm);
        card.add(passConfirmRow);
        card.add(Box.createVerticalStrut(6));
        card.add(rowGenEdad);
        card.add(Box.createVerticalStrut(6));
        card.add(btnFoto);
        card.add(Box.createVerticalStrut(10));
        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(6));
        card.add(btnVolver);

        return card;
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================
    private JPanel crearCampoPasswordConOjo(JPasswordField pf) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setMaximumSize(new Dimension(300, 36));
        wrapper.setPreferredSize(new Dimension(300, 36));
        wrapper.setBackground(BG_INPUT);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(2, 6, 2, 4)
        ));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        pf.setBackground(BG_INPUT);
        pf.setForeground(TEXT_WHITE);
        pf.setCaretColor(Color.WHITE);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(null);

        JButton btnEye = new JButton("Ver") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEye.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEye.setForeground(TEXT_MUTED);
        btnEye.setContentAreaFilled(false);
        btnEye.setBorderPainted(false);
        btnEye.setFocusPainted(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char defaultEcho = pf.getEchoChar();
        btnEye.addActionListener(e -> {
            if (pf.getEchoChar() == (char) 0) {
                pf.setEchoChar(defaultEcho);
                btnEye.setText("Ver");
            } else {
                pf.setEchoChar((char) 0);
                btnEye.setText("Ocultar");
            }
        });

        wrapper.add(pf, BorderLayout.CENTER);
        wrapper.add(btnEye, BorderLayout.EAST);
        return wrapper;
    }

    private JLabel crearEtiquetaCampo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }
    
    public static boolean esPasswordValido(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return password.matches(".*[0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
    
    public static JComponent crearAvatarCircular(String username, int diametro, boolean tieneStoryRing, String badgeOverlay) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int offset = tieneStoryRing ? 3 : 0;
                int size = diametro - (offset * 2);

                if (tieneStoryRing) {
                    GradientPaint gp = new GradientPaint(0, 0, G_ORANGE, diametro, diametro, G_PINK);
                    g2.setPaint(gp);
                    g2.setStroke(new BasicStroke(2.0f));
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
                        new Color(59, 130, 246), new Color(236, 72, 153), new Color(139, 92, 246), new Color(16, 185, 129), new Color(245, 158, 11)
                    };
                    g2.setColor(avatarColors[hash % avatarColors.length]);
                    g2.fillOval(offset, offset, size, size);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, diametro / 3 + 2));
                    String letter = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "U";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(letter, offset + (size - fm.stringWidth(letter)) / 2, offset + (size + fm.getAscent() - fm.getDescent()) / 2);
                }

                if (badgeOverlay != null) {
                    g2.setColor(IG_BLUE);
                    g2.fillOval(diametro - 15, diametro - 15, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.drawString("+", diametro - 12, diametro - 4);
                }
                g2.dispose();
            }
        };
        comp.setPreferredSize(new Dimension(diametro, diametro));
        return comp;
    }

    private void estilizarCampoTexto(JTextField tf) {
        tf.setMaximumSize(new Dimension(300, 36));
        tf.setPreferredSize(new Dimension(300, 36));
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JButton crearBotonGradiente(String texto, int w, int h) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, G_ORANGE, getWidth(), 0, G_PINK));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

    private JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_HOVER : BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}