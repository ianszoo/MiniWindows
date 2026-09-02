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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
public class InstaPanel extends JPanel  {
     private CardLayout cardLayout;
    private JPanel mainContent;

    public InstaPanel() {
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new GridLayout(9, 1, 4, 4));
        sidebar.setBackground(new Color(245, 247, 250));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(190, getHeight()));

        JButton btnFeed          = new JButton("🏠 Timeline");
        JButton btnPerfil        = new JButton("👤 Mi Perfil");
        JButton btnUpload        = new JButton("➕ Cargar Imagen");
        JButton btnMentions      = new JButton("🔔 Interacciones");
        JButton btnSearchProfile = new JButton("🔍 Buscar Perfil");
        JButton btnSearchTag     = new JButton("# Buscar Tag");
        JButton btnInbox         = new JButton("✉️ Inbox");
        JButton btnEditProfile   = new JButton("⚙️ Editar Perfil");
        JButton btnLogout        = new JButton("🚪 Cerrar Sesión");

        sidebar.add(btnFeed);
        sidebar.add(btnPerfil);
        sidebar.add(btnUpload);
        sidebar.add(btnMentions);
        sidebar.add(btnSearchProfile);
        sidebar.add(btnSearchTag);
        sidebar.add(btnInbox);
        sidebar.add(btnEditProfile);
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        mainContent.add(crearVistaTimeline(), "TIMELINE");
        mainContent.add(crearVistaPerfil(), "PERFIL");
        mainContent.add(crearVistaUpload(), "UPLOAD");
        mainContent.add(crearVistaMenciones(), "MENCIONES");
        mainContent.add(crearVistaBuscarPerfil(), "SEARCH_PROFILE");
        mainContent.add(crearVistaBuscarTag(), "SEARCH_TAG");
        mainContent.add(crearVistaInbox(), "INBOX");
        mainContent.add(crearVistaEditarPerfil(), "EDIT_PROFILE");

        add(mainContent, BorderLayout.CENTER);

        btnFeed.addActionListener(e -> cardLayout.show(mainContent, "TIMELINE"));
        btnPerfil.addActionListener(e -> cardLayout.show(mainContent, "PERFIL"));
        btnUpload.addActionListener(e -> cardLayout.show(mainContent, "UPLOAD"));
        btnMentions.addActionListener(e -> cardLayout.show(mainContent, "MENCIONES"));
        btnSearchProfile.addActionListener(e -> cardLayout.show(mainContent, "SEARCH_PROFILE"));
        btnSearchTag.addActionListener(e -> cardLayout.show(mainContent, "SEARCH_TAG"));
        btnInbox.addActionListener(e -> cardLayout.show(mainContent, "INBOX"));
        btnEditProfile.addActionListener(e -> cardLayout.show(mainContent, "EDIT_PROFILE"));
        btnLogout.addActionListener(e -> JOptionPane.showMessageDialog(this, "Sesión cerrada."));
    }

    private JPanel crearVistaTimeline() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("📸 TIMELINE DE PUBLICACIONES", SwingConstants.CENTER), BorderLayout.NORTH);
        JPanel postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
        postsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        postsPanel.add(new JLabel("<html><b>@noticias escribió:</b><br>'Lanzamiento oficial de Mini-Windows #Sistemas' — <i>Hoy</i></html>"));
        postsPanel.add(new JSeparator());
        postsPanel.add(new JLabel("<html><b>@admin escribió:</b><br>'Probando el timeline y menciones @todos' — <i>Ayer</i></html>"));
        p.add(new JScrollPane(postsPanel), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearVistaPerfil() {
        JPanel p = new JPanel(new GridLayout(9, 1, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("👤 PERFIL DE USUARIO"));
        p.add(new JLabel("Nombre: Administrador"));
        p.add(new JLabel("Username: @admin"));
        p.add(new JLabel("Edad: 21 | Género: M"));
        p.add(new JLabel("Fecha de Registro: 2026-03-02"));
        p.add(new JLabel("Seguidores: 12 | Seguidos: 3"));
        p.add(new JLabel("Publicaciones: 5"));
        p.add(new JLabel("Estado: ACTIVA"));
        p.add(new JButton("Seguir / Dejar de seguir"));
        return p;
    }

    private JPanel crearVistaUpload() {
        JPanel p = new JPanel(new GridLayout(6, 1, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("➕ Subir publicación (Modo Responsive 1080x1080)"));
        p.add(new JButton("📁 Seleccionar Imagen (.png / .jpg)"));
        p.add(new JTextField("Descripción (máx 220 caracteres, #tags, @menciones)..."));
        p.add(new JComboBox<>(new String[]{"Carpeta: /folders_personales/Viajes", "Carpeta: /folders_personales/Familia"}));
        p.add(new JComboBox<>(new String[]{"Sticker: Ninguno", "Sticker: Feliz", "Sticker: Corazón", "Sticker: Aplauso"}));
        p.add(new JButton("Publicar en Feed"));
        return p;
    }

    private JPanel crearVistaMenciones() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("🔔 MENCIONES (@admin)", SwingConstants.CENTER), BorderLayout.NORTH);
        p.add(new JScrollPane(new JList<>(new String[]{"@deportes: 'Hola @admin'", "@moda: 'Sigan a @admin'"})), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearVistaBuscarPerfil() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JTextField("Buscar username..."), BorderLayout.CENTER);
        top.add(new JButton("Buscar"), BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JList<>(new String[]{"@noticias — Lo sigues", "@deportes — No lo sigues", "@entretenimiento — No lo sigues"})), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearVistaBuscarTag() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JTextField("# Ingrese hashtag..."), BorderLayout.CENTER);
        top.add(new JButton("Buscar"), BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(new JList<>(new String[]{"#tecnologia — @noticias", "#programacion — @admin"})), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearVistaInbox() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("✉️ DIRECT MESSAGES (INBOX)", SwingConstants.CENTER), BorderLayout.NORTH);
        JTextArea chat = new JTextArea("[10:30 AM] @noticias: Hola @admin\n[10:32 AM] @admin: ¡Listo el proyecto!\n[10:33 AM] @noticias envió un sticker [Aplauso.png]");
        chat.setEditable(false);
        p.add(new JScrollPane(chat), BorderLayout.CENTER);
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.add(new JTextField(), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout());
        btns.add(new JButton("Enviar"));
        btns.add(new JButton("Sticker"));
        sendPanel.add(btns, BorderLayout.EAST);
        p.add(sendPanel, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearVistaEditarPerfil() {
        JPanel p = new JPanel(new GridLayout(6, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("Nombre:"));
        p.add(new JTextField("Administrador"));
        p.add(new JLabel("Contraseña:"));
        p.add(new JPasswordField("admin123"));
        p.add(new JLabel("Foto de perfil:"));
        p.add(new JButton("Cambiar Foto..."));
        p.add(new JLabel("Estado de cuenta:"));
        p.add(new JCheckBox("Cuenta Activa", true));
        p.add(new JButton("Guardar Cambios"));
        p.add(new JButton("Desactivar Cuenta"));
        return p;
    }
}
