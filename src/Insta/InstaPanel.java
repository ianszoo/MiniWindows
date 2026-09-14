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


public class InstaPanel extends JPanel {
    private Usuario usuarioActual;
    private String usuarioPerfilVisitado;

    // Control de Pantallas
    private CardLayout rootCardLayout;
    private JPanel rootContainer;

    private CardLayout authCardLayout;
    private JPanel authContainer;

    private CardLayout screenCardLayout;
    private JPanel screenContainer;

    // Paleta Oficial Instagram Dark Mobile
    public static final Color BG_PHONE        = new Color(0, 0, 0);          // OLED Black
    public static final Color BG_SURFACE      = new Color(18, 18, 18);       // Tarjetas / Headers
    public static final Color BG_INPUT        = new Color(38, 38, 38);       // Inputs
    public static final Color BG_HOVER        = new Color(45, 45, 45);
    public static final Color BORDER_LINE     = new Color(38, 38, 38);       // Líneas divisorias
    public static final Color TEXT_WHITE      = new Color(245, 245, 245);
    public static final Color TEXT_MUTED      = new Color(168, 168, 168);
    public static final Color IG_BLUE         = new Color(0, 149, 246);
    public static final Color IG_RED_HEART    = new Color(255, 48, 64);
    public static final Color IG_GREEN        = new Color(34, 197, 94);

    public static final Color G_ORANGE = new Color(245, 133, 41);
    public static final Color G_PINK   = new Color(221, 42, 123);
    public static final Color G_PURPLE = new Color(129, 52, 175);

    // Vistas Dinámicas
    private JPanel pnlFeedCards;
    private JPanel pnlStoriesBar;
    private JPanel pnlPerfilHeader;
    private JPanel pnlPerfilGrid;
    private JPanel pnlChatStream;
    private JPanel pnlListaConversaciones;
    private String chatUsuarioSeleccionado = "noticias";
    private volatile boolean hiloChatActivo = true;

    private String pantallaActual = "TIMELINE";
    private JPanel bottomNavBar;

    public InstaPanel(Usuario usuario) {
        this.usuarioActual = usuario != null ? usuario : new Usuario("admin", "Admin2026!", true);
        this.usuarioPerfilVisitado = this.usuarioActual.getUsername();

        InstaFileManager.inicializarInsta();
        InstaFileManager.crearEspacioUsuarioInsta(this.usuarioActual.getUsername());

        setLayout(new BorderLayout());
        setBackground(BG_PHONE);

        rootCardLayout = new CardLayout();
        rootContainer = new JPanel(rootCardLayout);
        rootContainer.setOpaque(false);

        // Vista 1: Login / Registro
        rootContainer.add(crearVistaAutenticacionMobile(), "AUTH");

        // Vista 2: Celular Completo con TopBar, Pantalla y Bottom Navigation
        rootContainer.add(crearVistaTelefonoPrincipal(), "APP");

        add(rootContainer, BorderLayout.CENTER);
        rootCardLayout.show(rootContainer, "AUTH");

        iniciarHiloSincronizacionChat();
    }

    // =========================================================================
    // CONTENEDOR MÓVIL (TOP BAR + SCREENS + BOTTOM NAV)
    // =========================================================================
    private JPanel crearVistaTelefonoPrincipal() {
        JPanel phone = new JPanel(new BorderLayout());
        phone.setBackground(BG_PHONE);

        // 1. TOP APP BAR (Logo, Botón Inbox y Configuración)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_SURFACE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LINE));
        topBar.setPreferredSize(new Dimension(getWidth(), 50));
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

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JButton btnDirect = crearBotonIconoNav("💬", () -> cambiarPantalla("INBOX"));
        btnDirect.setToolTipText("Mensajes Directos");

        JButton btnConfig = crearBotonIconoNav("⚙️", () -> cambiarPantalla("EDIT_PROFILE"));
        btnConfig.setToolTipText("Editar Perfil y Configuración");

        topActions.add(btnDirect);
        topActions.add(btnConfig);
        topBar.add(topActions, BorderLayout.EAST);

        phone.add(topBar, BorderLayout.NORTH);

        // 2. CONTENIDO PRINCIPAL (SCREENS)
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

        phone.add(screenContainer, BorderLayout.CENTER);

        // 3. BOTTOM NAVIGATION BAR (BARRA DE ICONOS MÓVIL)
        bottomNavBar = new JPanel(new GridLayout(1, 5, 0, 0));
        bottomNavBar.setBackground(BG_SURFACE);
        bottomNavBar.setPreferredSize(new Dimension(getWidth(), 52));
        bottomNavBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LINE));

        bottomNavBar.add(crearTabBottomNav("🏠", "TIMELINE"));
        bottomNavBar.add(crearTabBottomNav("🔍", "SEARCH"));
        bottomNavBar.add(crearTabBottomNav("➕", "UPLOAD"));
        bottomNavBar.add(crearTabBottomNav("❤️", "NOTIFICACIONES"));
        bottomNavBar.add(crearTabBottomNav("👤", "PERFIL"));

        phone.add(bottomNavBar, BorderLayout.SOUTH);
        return phone;
    }

    private void cambiarPantalla(String nombreCard) {
        pantallaActual = nombreCard;
        if (nombreCard.equals("PERFIL")) {
            usuarioPerfilVisitado = usuarioActual.getUsername();
            recargarPerfil();
        } else if (nombreCard.equals("TIMELINE")) {
            recargarTimeline();
        } else if (nombreCard.equals("INBOX")) {
            recargarChat();
        }
        screenCardLayout.show(screenContainer, nombreCard);
        bottomNavBar.repaint();
    }

    private JButton crearTabBottomNav(String icono, String cardName) {
        JButton btn = new JButton(icono) {
            @Override
            protected void paintComponent(Graphics g) {
                boolean activa = pantallaActual.equals(cardName);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (activa) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(8, 6, getWidth() - 16, getHeight() - 12, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> cambiarPantalla(cardName));
        return btn;
    }

    private JButton crearBotonIconoNav(String icono, Runnable accion) {
        JButton btn = new JButton(icono);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    // =========================================================================
    // 1. TIMELINE (HISTORIAS & FEED LIMPIO)
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
            pnlFeedCards.add(crearTarjetaPostMobile(todosPosts.obtener(i)));
            pnlFeedCards.add(Box.createVerticalStrut(14));
        }

        if (todosPosts.estaVacia()) {
            JLabel lblVacio = new JLabel("No hay publicaciones aún. ¡Comienza a seguir a otros!", SwingConstants.CENTER);
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
                    JOptionPane.showMessageDialog(InstaPanel.this, "Viendo historia activa de @" + username, "Story", JOptionPane.PLAIN_MESSAGE);
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
        int cardH = 210;
        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            cardH = 430;
        }
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.setMaximumSize(new Dimension(cardW, cardH));

        // Header del post
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.add(crearAvatarCircular(p.getAutor(), 34, true, null), BorderLayout.WEST);

        JPanel postInfo = new JPanel(new GridLayout(2, 1, 0, 1));
        postInfo.setOpaque(false);

        JLabel lblAutor = new JLabel(p.getAutor() + " escribió:");
        lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAutor.setForeground(TEXT_WHITE);

        String carpeta = (p.getCarpetaPersonal() != null && !p.getCarpetaPersonal().equals("null")) ? p.getCarpetaPersonal() : "General";
        JLabel lblCarpeta = new JLabel("📁 " + carpeta);
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
        card.add(header, BorderLayout.NORTH);

        // Imagen central (si existe)
        if (p.getRutaImagen() != null && new File(p.getRutaImagen()).exists()) {
            ImageIcon icon = new ImageIcon(p.getRutaImagen());
            Image scaled = icon.getImage().getScaledInstance(366, 210, Image.SCALE_SMOOTH);
            JLabel lblImg = new JLabel(new ImageIcon(scaled));
            lblImg.setBorder(new EmptyBorder(8, 0, 8, 0));
            card.add(lblImg, BorderLayout.CENTER);
        }

        // Footer con acciones y texto
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actionRow.setOpaque(false);

        JButton btnLike = new JButton("🤍");
        btnLike.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
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
        btnComment.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnComment.setContentAreaFilled(false);
        btnComment.setBorderPainted(false);

        actionRow.add(btnLike);
        actionRow.add(btnComment);
        footer.add(actionRow);

        String texto = p.getContenido().replaceAll("(#[\\w]+)", "<span style='color:#0095f6;'>$1</span>");
        texto = texto.replaceAll("(@[\\w]+)", "<span style='color:#ffffff; font-weight:bold;'>$1</span>");

        JLabel lblContenido = new JLabel("<html><body style='width:350px; color:#f1f5f9; font-size:11px; font-family:Segoe UI;'>"
                + "<b>@" + p.getAutor() + "</b> " + texto + "</body></html>");
        lblContenido.setBorder(new EmptyBorder(2, 4, 4, 4));
        footer.add(lblContenido);

        if (p.getSticker() != null && !p.getSticker().isEmpty() && !p.getSticker().equalsIgnoreCase("null")) {
            File fSt = new File(p.getSticker());
            if (fSt.exists()) {
                ImageIcon iconSt = new ImageIcon(new ImageIcon(fSt.getAbsolutePath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
                JLabel lblStImg = new JLabel("  Sticker: ", iconSt, SwingConstants.LEFT);
                lblStImg.setForeground(G_PINK);
                footer.add(lblStImg);
            } else {
                JLabel lblSt = new JLabel("  ✨ Sticker: " + p.getSticker());
                lblSt.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lblSt.setForeground(G_PINK);
                footer.add(lblSt);
            }
        }

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. BUSCADOR MÓVIL (PROFILES & HASHTAGS)
    // =========================================================================
    private JPanel crearVistaBuscar() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_PHONE);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);

        JTextField txtSearch = new JTextField();
        estilizarCampoTexto(txtSearch, "Buscar personas o #hashtag...");

        JButton btnBuscar = crearBotonGradiente("Buscar", 80, 36);
        top.add(txtSearch, BorderLayout.CENTER);
        top.add(btnBuscar, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_SURFACE);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnVer = crearBotonGradiente("Ver Perfil Seleccionado", 200, 36);
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.setOpaque(false);
        bot.add(btnVer);
        p.add(bot, BorderLayout.SOUTH);

        Runnable doSearch = () -> {
            model.clear();
            String q = txtSearch.getText().trim().toLowerCase();

            if (q.startsWith("#")) {
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
                            if (!ids.contiene(pub.getId()) && pub.getHashtags().contiene(q)) {
                                ids.agregar(pub.getId());
                                model.addElement("🏷️ " + q + " por @" + pub.getAutor() + ": " + pub.getContenido());
                            }
                            np = np.getSiguiente();
                        }
                    }
                    nu = nu.getSiguiente();
                }
            } else {
                Lista<Usuario> users = InstaFileManager.cargarUsuariosInsta();
                Nodo<Usuario> n = users.getHead();
                while (n != null) {
                    Usuario u = n.getDato();
                    if (u.isActivo() && (q.isEmpty() || u.getUsername().toLowerCase().contains(q) || u.getNombreCompleto().toLowerCase().contains(q))) {
                        model.addElement("👤 @" + u.getUsername() + " — " + u.getNombreCompleto() + " (" + u.getGenero() + ", " + u.getEdad() + "a)");
                    }
                    n = n.getSiguiente();
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
                cambiarPantalla("PERFIL");
            }
        });

        return p;
    }

    // =========================================================================
    // 3. SUBIR PUBLICACIÓN / IMAGEN
    // =========================================================================
    private JPanel crearVistaUpload() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_PHONE);

        JPanel card = new JPanel(new GridLayout(8, 1, 8, 8));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        card.setPreferredSize(new Dimension(380, 430));

        JLabel lblTit = new JLabel("➕ Crear Publicación", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(TEXT_WHITE);

        JTextField txtDesc = new JTextField();
        estilizarCampoTexto(txtDesc, "Descripción (#tags, @menciones)...");

        final String[] rutaSel = {null};
        JButton btnImg = crearBotonSecundario("📁 Seleccionar Imagen");
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
        if (carpetas == null || carpetas.length == 0) carpetas = new String[]{"General", "Viajes", "Memes"};
        JComboBox<String> cbCarpetas = new JComboBox<>(carpetas);

        final String[] stickerSel = {null};
        JButton btnStk = crearBotonSecundario("✨ Seleccionar Sticker");
        btnStk.addActionListener(e -> {
            mostrarSelectorStickers(st -> {
                stickerSel[0] = st;
                btnStk.setText("✅ Sticker seleccionado");
            });
        });

        JCheckBox chkHistoria = new JCheckBox("Publicar como Historia");
        chkHistoria.setForeground(TEXT_WHITE);
        chkHistoria.setOpaque(false);

        JButton btnPub = crearBotonGradiente("Compartir", 340, 36);
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
            btnImg.setText("📁 Seleccionar Imagen");
            btnStk.setText("✨ Seleccionar Sticker");
            cambiarPantalla("TIMELINE");
        });

        card.add(lblTit);
        card.add(btnImg);
        card.add(txtDesc);
        card.add(cbCarpetas);
        card.add(btnStk);
        card.add(chkHistoria);
        card.add(btnPub);

        root.add(card);
        return root;
    }

    // =========================================================================
    // 4. NOTIFICACIONES / MENCIONES
    // =========================================================================
    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_PHONE);
        p.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel l = new JLabel("❤️ Interacciones y Menciones");
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(TEXT_WHITE);
        p.add(l, BorderLayout.NORTH);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setBackground(BG_SURFACE);
        list.setForeground(TEXT_WHITE);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnRef = crearBotonGradiente("Actualizar", 140, 34);
        btnRef.addActionListener(e -> {
            model.clear();
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
                        if (!ids.contiene(pub.getId()) && pub.getMenciones().contiene(usuarioActual.getUsername().toLowerCase())) {
                            ids.agregar(pub.getId());
                            model.addElement("💬 @" + pub.getAutor() + " te mencionó: \"" + pub.getContenido() + "\"");
                        }
                        np = np.getSiguiente();
                    }
                }
                nu = nu.getSiguiente();
            }
            if (model.isEmpty()) model.addElement("No tienes notificaciones recientes.");
        });

        btnRef.doClick();
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bot.setOpaque(false);
        bot.add(btnRef);
        p.add(bot, BorderLayout.SOUTH);
        return p;
    }

    // =========================================================================
    // 5. PERFIL DE USUARIO COMPLETO (TODOS LOS REQUERIMIENTOS)
    // =========================================================================
    private JPanel crearVistaPerfil() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);

        pnlPerfilHeader = new JPanel(new BorderLayout(14, 0));
        pnlPerfilHeader.setBackground(BG_PHONE);
        pnlPerfilHeader.setBorder(new EmptyBorder(16, 16, 12, 16));

        pnlPerfilGrid = new JPanel(new GridLayout(0, 3, 4, 4));
        pnlPerfilGrid.setBackground(BG_PHONE);
        pnlPerfilGrid.setBorder(new EmptyBorder(10, 16, 20, 16));

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(BG_PHONE);
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

        // Avatar a la izquierda
        pnlPerfilHeader.add(crearAvatarCircular(u.getUsername(), 74, true, null), BorderLayout.WEST);

        // Panel de Información Completa
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        // 1. Username y Estado Activa/Inactiva
        JLabel lblU = new JLabel("@" + u.getUsername() + "  " + (u.isActivo() ? "🟢 Activa" : "🔴 Inactiva"));
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblU.setForeground(TEXT_WHITE);

        // 2. Contadores (Posts, Followers, Following)
        JPanel rowStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        rowStats.setOpaque(false);
        rowStats.add(new JLabel("<html><center><b>" + posts.getSize() + "</b><br><span style='font-size:9px; color:#a8a8a8;'>posts</span></center></html>"));
        rowStats.add(new JLabel("<html><center><b>" + followers.getSize() + "</b><br><span style='font-size:9px; color:#a8a8a8;'>seguidores</span></center></html>"));
        rowStats.add(new JLabel("<html><center><b>" + following.getSize() + "</b><br><span style='font-size:9px; color:#a8a8a8;'>seguidos</span></center></html>"));
        for (Component c : rowStats.getComponents()) c.setForeground(TEXT_WHITE);

        // 3. Nombre Completo, Género, Edad y Fecha de Registro
        String fechaReg = new SimpleDateFormat("dd/MM/yyyy").format(u.getFechaCreacion());
        String generoStr = (u.getGenero() == 'M' || u.getGenero() == 'm') ? "Masculino" : "Femenino";

        JLabel lblNombre = new JLabel("<html><b>" + u.getNombreCompleto() + "</b></html>");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNombre.setForeground(TEXT_WHITE);

        JLabel lblBio = new JLabel("<html><span style='color:#a8a8a8; font-size:10px;'>"
                + "👤 Género: " + generoStr + " • 🎂 Edad: " + u.getEdad() + " años<br>"
                + "📅 Registro: " + fechaReg + "</span></html>");

        info.add(lblU);
        info.add(Box.createVerticalStrut(4));
        info.add(rowStats);
        info.add(Box.createVerticalStrut(4));
        info.add(lblNombre);
        info.add(lblBio);

        // 4. Botón Seguir / Dejar de seguir (con confirmación)
        if (!esPropio) {
            Lista<String> misSeguidos = InstaFileManager.cargarSeguidos(usuarioActual.getUsername());
            boolean loSigo = misSeguidos.contiene(u.getUsername().toLowerCase());

            JButton btnSeguir = new JButton(loSigo ? "Siguiendo" : "Seguir");
            btnSeguir.setBackground(loSigo ? BG_INPUT : IG_BLUE);
            btnSeguir.setForeground(Color.WHITE);
            btnSeguir.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnSeguir.setFocusPainted(false);
            btnSeguir.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnSeguir.addActionListener(e -> {
                if (loSigo) {
                    int resp = JOptionPane.showConfirmDialog(this, "¿Deseas dejar de seguir a @" + u.getUsername() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        InstaFileManager.toggleSeguir(usuarioActual.getUsername(), u.getUsername());
                        recargarPerfil();
                    }
                } else {
                    InstaFileManager.toggleSeguir(usuarioActual.getUsername(), u.getUsername());
                    recargarPerfil();
                }
            });
            info.add(Box.createVerticalStrut(6));
            info.add(btnSeguir);
        }

        pnlPerfilHeader.add(info, BorderLayout.CENTER);

        // Grid de Publicaciones
        for (int i = posts.getSize() - 1; i >= 0; i--) {
            Publicacion pub = posts.obtener(i);
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

        pnlPerfilHeader.revalidate();
        pnlPerfilHeader.repaint();
        pnlPerfilGrid.revalidate();
        pnlPerfilGrid.repaint();
    }

    // =========================================================================
    // 6. INBOX / MENSAJERÍA DIRECTA
    // =========================================================================
    private JPanel crearVistaInbox() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PHONE);

        JPanel topChat = new JPanel(new BorderLayout(8, 0));
        topChat.setBackground(BG_SURFACE);
        topChat.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblUserChat = new JLabel("@" + chatUsuarioSeleccionado + " • 🟢");
        lblUserChat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserChat.setForeground(TEXT_WHITE);
        topChat.add(lblUserChat, BorderLayout.WEST);

        JButton btnEliminar = crearBotonSecundario("🗑");
        btnEliminar.setToolTipText("Eliminar conversación");
        btnEliminar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar chat con @" + chatUsuarioSeleccionado + "?", "Eliminar Chat", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                InstaFileManager.eliminarConversacionCompleta(usuarioActual.getUsername(), chatUsuarioSeleccionado);
                recargarChat();
            }
        });
        topChat.add(btnEliminar, BorderLayout.EAST);
        root.add(topChat, BorderLayout.NORTH);

        pnlChatStream = new JPanel();
        pnlChatStream.setLayout(new BoxLayout(pnlChatStream, BoxLayout.Y_AXIS));
        pnlChatStream.setBackground(BG_PHONE);
        pnlChatStream.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scrollChatStream = new JScrollPane(pnlChatStream);
        scrollChatStream.setBorder(null);
        root.add(scrollChatStream, BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setBackground(BG_SURFACE);
        inputRow.setBorder(new EmptyBorder(8, 10, 8, 10));

        JTextField txtMsg = new JTextField();
        estilizarCampoTexto(txtMsg, "Mensaje...");

        JPanel btnActs = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnActs.setOpaque(false);

        JButton btnStk = crearBotonSecundario("✨");
        JButton btnSend = crearBotonGradiente("➤", 45, 34);

        btnActs.add(btnStk);
        btnActs.add(btnSend);
        inputRow.add(txtMsg, BorderLayout.CENTER);
        inputRow.add(btnActs, BorderLayout.EAST);
        root.add(inputRow, BorderLayout.SOUTH);

        ActionListener enviar = e -> {
            String txt = txtMsg.getText().trim();
            if (!txt.isEmpty()) {
                if (txt.length() > 300) txt = txt.substring(0, 300);
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, txt, MensajeInbox.Tipo.TEXTO);
                txtMsg.setText("");
                recargarChat();
            }
        };
        btnSend.addActionListener(enviar);
        txtMsg.addActionListener(enviar);

        btnStk.addActionListener(e -> {
            mostrarSelectorStickers(stk -> {
                InstaFileManager.enviarMensaje(usuarioActual.getUsername(), chatUsuarioSeleccionado, stk, MensajeInbox.Tipo.STICKER);
                recargarChat();
            });
        });

        recargarChat();
        return root;
    }

    private void mostrarSelectorStickers(java.util.function.Consumer<String> callback) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Stickers", true);
        dlg.setSize(320, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG_SURFACE);

        JPanel grid = new JPanel(new GridLayout(0, 3, 8, 8));
        grid.setBackground(BG_SURFACE);
        grid.setBorder(new EmptyBorder(12, 12, 12, 12));

        Lista<String> stickers = InstaFileManager.cargarStickers(usuarioActual.getUsername());
        Nodo<String> n = stickers.getHead();

        while (n != null) {
            String stk = n.getDato();
            JButton btn = new JButton();
            btn.setBackground(BG_INPUT);
            btn.setBorder(BorderFactory.createLineBorder(BORDER_LINE, 1, true));
            btn.setFocusPainted(false);

            File f = new File(stk);
            if (f.exists()) {
                ImageIcon ic = new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH));
                btn.setIcon(ic);
            } else {
                btn.setText(stk);
                btn.setForeground(TEXT_WHITE);
            }

            btn.addActionListener(e -> {
                callback.accept(stk);
                dlg.dispose();
            });
            grid.add(btn);
            n = n.getSiguiente();
        }

        dlg.add(new JScrollPane(grid), BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private synchronized void recargarChat() {
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

            if (m.getTipo() == MensajeInbox.Tipo.STICKER && new File(m.getTexto()).exists()) {
                ImageIcon ic = new ImageIcon(new ImageIcon(m.getTexto()).getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));
                bubble.add(new JLabel(ic), BorderLayout.CENTER);
            } else {
                JLabel lblMsg = new JLabel("<html><body style='max-width:240px; color:#ffffff; font-size:11px; font-family:Segoe UI;'>"
                        + (m.getTipo() == MensajeInbox.Tipo.STICKER ? "✨ " : "") + m.getTexto() + "</body></html>");
                bubble.add(lblMsg, BorderLayout.CENTER);
            }

            JLabel lblH = new JLabel(hora + (esMio ? (m.isLeido() ? " • Visto" : "") : ""), SwingConstants.RIGHT);
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

    private void iniciarHiloSincronizacionChat() {
        Thread t = new Thread(() -> {
            while (hiloChatActivo) {
                try {
                    Thread.sleep(2500);
                    if (pantallaActual.equals("INBOX")) {
                        SwingUtilities.invokeLater(this::recargarChat);
                    }
                } catch (InterruptedException ignored) { break; }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // =========================================================================
    // 7. EDITAR PERFIL & ACTIVAR / DESACTIVAR CUENTA
    // =========================================================================
    private JPanel crearVistaEditarPerfil() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_PHONE);

        JPanel card = new JPanel(new GridLayout(7, 2, 8, 8));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(380, 360));

        Usuario u = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
        if (u == null) u = usuarioActual;

        JTextField txtNom = new JTextField(u.getNombreCompleto());
        estilizarCampoTexto(txtNom, "");

        JPasswordField txtPass = new JPasswordField(u.getPass());
        estilizarCampoTexto(txtPass, "");

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(u.getEdad(), 1, 120, 1));
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"M", "F"});
        cbGen.setSelectedItem(String.valueOf(u.getGenero()));

        JButton btnGuardar = crearBotonGradiente("Guardar", 140, 34);
        JButton btnDesactivar = crearBotonSecundario(u.isActivo() ? "Desactivar" : "Reactivar");

        btnGuardar.addActionListener(e -> {
            Usuario usr = InstaFileManager.buscarUsuario(usuarioActual.getUsername());
            if (usr != null) {
                usr.setNombreCompleto(txtNom.getText().trim());
                usr.setPass(new String(txtPass.getPassword()));
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
                    int resp = JOptionPane.showConfirmDialog(this, "¿Desactivar tu cuenta?\nDesaparecerás de búsquedas y timeline.", "Confirmar", JOptionPane.YES_NO_OPTION);
                    if (resp == JOptionPane.YES_OPTION) {
                        usr.setActivo(false);
                        InstaFileManager.actualizarUsuario(usr);
                        btnDesactivar.setText("Reactivar");
                        cambiarPantalla("PERFIL");
                    }
                } else {
                    usr.setActivo(true);
                    InstaFileManager.actualizarUsuario(usr);
                    JOptionPane.showMessageDialog(this, "¡Cuenta reactivada!");
                    btnDesactivar.setText("Desactivar");
                    cambiarPantalla("PERFIL");
                }
            }
        });

        card.add(new JLabel("Nombre Completo:")); card.add(txtNom);
        card.add(new JLabel("Contraseña:")); card.add(txtPass);
        card.add(new JLabel("Edad:")); card.add(spinEdad);
        card.add(new JLabel("Género:")); card.add(cbGen);
        card.add(new JLabel("Guardar cambios:")); card.add(btnGuardar);
        card.add(new JLabel("Estado de cuenta:")); card.add(btnDesactivar);

        JButton btnCerrar = new JButton("🚪 Cerrar Sesión");
        btnCerrar.setForeground(new Color(248, 113, 113));
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión de @" + usuarioActual.getUsername() + "?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                rootCardLayout.show(rootContainer, "AUTH");
                authCardLayout.show(authContainer, "LOGIN");
            }
        });
        card.add(new JLabel("Salir:")); card.add(btnCerrar);

        for (Component c : card.getComponents()) if (c instanceof JLabel) ((JLabel) c).setForeground(TEXT_WHITE);

        root.add(card);
        return root;
    }

    // =========================================================================
    // VISTA LOGIN / REGISTRO MOBILE
    // =========================================================================
    private JPanel crearVistaAutenticacionMobile() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_PHONE);

        authCardLayout = new CardLayout();
        authContainer = new JPanel(authCardLayout);
        authContainer.setOpaque(false);
        authContainer.setPreferredSize(new Dimension(380, 520));

        authContainer.add(crearCardLoginInsta(), "LOGIN");
        authContainer.add(crearCardRegistroInsta(), "REGISTRO");

        panel.add(authContainer);
        return panel;
    }

    private JPanel crearCardLoginInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(30, 25, 30, 25)
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
        lblLogo.setFont(new Font("Segoe UI Black", Font.BOLD, 32));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtUser = new JTextField(usuarioActual.getUsername());
        estilizarCampoTexto(txtUser, "Username");

        JPasswordField txtPass = new JPasswordField("Admin2026!");
        estilizarCampoTexto(txtPass, "Contraseña");

        JButton btnLogin = crearBotonGradiente("Iniciar Sesión", 300, 36);
        btnLogin.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            try {
                Usuario userAuth = InstaFileManager.autenticarInsta(u, p);
                if (userAuth != null) {
                    usuarioActual = userAuth;
                    usuarioPerfilVisitado = userAuth.getUsername();
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

        JButton btnIrRegistro = new JButton("¿No tienes cuenta? Regístrate");
        btnIrRegistro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnIrRegistro.setForeground(IG_BLUE);
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setBorderPainted(false);
        btnIrRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIrRegistro.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIrRegistro.addActionListener(e -> authCardLayout.show(authContainer, "REGISTRO"));

        card.add(lblLogo);
        card.add(Box.createVerticalStrut(24));
        card.add(txtUser);
        card.add(Box.createVerticalStrut(10));
        card.add(txtPass);
        card.add(Box.createVerticalStrut(18));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(14));
        card.add(btnIrRegistro);

        return card;
    }

    private JPanel crearCardRegistroInsta() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LINE, 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTit = new JLabel("Crear Cuenta", SwingConstants.CENTER);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTit.setForeground(TEXT_WHITE);
        lblTit.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtNombre = new JTextField();
        estilizarCampoTexto(txtNombre, "Nombre completo");

        JTextField txtUser = new JTextField();
        estilizarCampoTexto(txtUser, "Username único");

        JPasswordField txtPass = new JPasswordField();
        estilizarCampoTexto(txtPass, "Contraseña");

        JPanel rowGenEdad = new JPanel(new GridLayout(1, 2, 8, 0));
        rowGenEdad.setOpaque(false);
        rowGenEdad.setMaximumSize(new Dimension(300, 34));

        JSpinner spinEdad = new JSpinner(new SpinnerNumberModel(20, 13, 100, 1));
        JComboBox<String> cbGen = new JComboBox<>(new String[]{"Género: M", "Género: F"});
        rowGenEdad.add(spinEdad);
        rowGenEdad.add(cbGen);

        final String[] rutaFoto = {null};
        JButton btnFoto = crearBotonSecundario("📷 Foto de Perfil");
        btnFoto.setMaximumSize(new Dimension(300, 32));
        btnFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFoto.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                rutaFoto[0] = fc.getSelectedFile().getAbsolutePath();
                btnFoto.setText("✅ " + fc.getSelectedFile().getName());
            }
        });

        JButton btnRegistrar = crearBotonGradiente("Registrarse", 300, 36);
        btnRegistrar.addActionListener(e -> {
            String nom = txtNombre.getText().trim();
            String usr = txtUser.getText().trim().toLowerCase();
            String pas = new String(txtPass.getPassword());
            char gen = cbGen.getSelectedIndex() == 0 ? 'M' : 'F';
            int edad = (Integer) spinEdad.getValue();

            if (nom.isEmpty() || usr.isEmpty() || pas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Completa todos los campos.");
                return;
            }

            Usuario nuevo = new Usuario(usr, pas, false, nom, gen, edad, rutaFoto[0]);
            if (InstaFileManager.registrarUsuarioInsta(nuevo)) {
                JOptionPane.showMessageDialog(this, "¡Cuenta creada con éxito!");
                usuarioActual = nuevo;
                usuarioPerfilVisitado = nuevo.getUsername();
                rootCardLayout.show(rootContainer, "APP");
                cambiarPantalla("TIMELINE");
            } else {
                JOptionPane.showMessageDialog(this, "Ese username ya está en uso.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnVolver = new JButton("← Volver al login");
        btnVolver.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolver.setForeground(TEXT_MUTED);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.addActionListener(e -> authCardLayout.show(authContainer, "LOGIN"));

        card.add(lblTit);
        card.add(Box.createVerticalStrut(10));
        card.add(txtNombre);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPass);
        card.add(Box.createVerticalStrut(6));
        card.add(rowGenEdad);
        card.add(Box.createVerticalStrut(6));
        card.add(btnFoto);
        card.add(Box.createVerticalStrut(12));
        card.add(btnRegistrar);
        card.add(Box.createVerticalStrut(8));
        card.add(btnVolver);

        return card;
    }

    // =========================================================================
    // AVATARES CIRCULARES CON HISTORIAS Y FOTOS
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

    private void estilizarCampoTexto(JTextField tf, String placeholder) {
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
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_WHITE);
        btn.setBackground(BG_INPUT);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_LINE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
