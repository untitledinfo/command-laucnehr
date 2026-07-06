package launcher.ui;

import launcher.Account;
import launcher.AccountManager;
import launcher.Config;
import launcher.FabricSupport;
import launcher.MinecraftInstaller;
import launcher.MinecraftLauncher;
import launcher.ServerPinger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;

public class MainFrame extends JFrame {

    private final Config config;
    private final AccountManager accountManager;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private FadePanel homeFade;
    private FadePanel instancesFade;

    private SidebarButton navHome;
    private SidebarButton navInstances;
    private JLabel accountLabel;

    private JComboBox<String> versionCombo;
    private JTextField serverField;
    private JLabel serverStatusLabel;
    private GlowButton playButton;
    private ShimmerProgressBar progressBar;
    private JLabel progressLabel;

    private JTextArea consoleArea;
    private JPanel consoleWrapper;
    private boolean consoleVisible = false;

    public MainFrame() {
        super("Command Launcher");
        config = Config.load();
        accountManager = new AccountManager(config);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARKEST);

        buildUi();
        loadInstalledVersions();
        updateAccountLabel();
        serverField.setText(config.getString("server", ""));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                config.save();
            }
        });
    }

    // ------------------------------------------------------------------ UI

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARKEST);

        root.add(buildSidebar(), BorderLayout.WEST);

        cards.setOpaque(false);
        homeFade = new FadePanel(buildHomePage());
        instancesFade = new FadePanel(buildInstancesPage());
        cards.add(homeFade, "home");
        cards.add(instancesFade, "instances");
        root.add(cards, BorderLayout.CENTER);

        consoleWrapper = buildConsolePanel();
        consoleWrapper.setVisible(false);
        root.add(consoleWrapper, BorderLayout.SOUTH);

        setContentPane(root);
        switchPage("home", navHome);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(Theme.BG_PANEL);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("\u26A1 COMMAND");
        brand.setForeground(Theme.ACCENT);
        brand.setFont(Theme.FONT_BRAND);
        brand.setBorder(new EmptyBorder(20, 16, 0, 0));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        JLabel sub = new JLabel("LAUNCHER");
        sub.setForeground(Theme.TEXT_DIM);
        sub.setFont(Theme.FONT_SUBTITLE);
        sub.setBorder(new EmptyBorder(0, 16, 20, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sub);

        navHome = new SidebarButton("  \uD83C\uDFE0  Home");
        navInstances = new SidebarButton("  \uD83E\uDDE9  Instances");
        SidebarButton navAccounts = new SidebarButton("  \uD83D\uDC64  Accounts");
        SidebarButton navSettings = new SidebarButton("  \u2699  Settings");
        SidebarButton navConsole = new SidebarButton("  \uD83D\uDCBB  Console");

        ButtonGroup group = new ButtonGroup();
        group.add(navHome);
        group.add(navInstances);

        for (SidebarButton b : new SidebarButton[]{navHome, navInstances, navAccounts, navSettings, navConsole}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            sidebar.add(b);
        }
        navHome.setSelected(true);

        navHome.addActionListener(e -> switchPage("home", navHome));
        navInstances.addActionListener(e -> switchPage("instances", navInstances));
        navAccounts.addActionListener(e -> openAccounts());
        navSettings.addActionListener(e -> openSettings());
        navConsole.addActionListener(e -> toggleConsole());

        sidebar.add(Box.createVerticalGlue());

        accountLabel = new JLabel("<html>No account selected</html>");
        accountLabel.setForeground(Theme.TEXT_DIM);
        accountLabel.setFont(Theme.FONT_SUBTITLE);
        accountLabel.setBorder(new EmptyBorder(0, 16, 20, 10));
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(accountLabel);

        return sidebar;
    }

    private void switchPage(String name, SidebarButton btn) {
        cardLayout.show(cards, name);
        btn.setSelected(true);
        if (name.equals("home")) homeFade.playFadeIn();
        else instancesFade.playFadeIn();
    }

    private JComponent buildHomePage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 36, 20, 36));

        JLabel title = new JLabel("Welcome back");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(title);

        JLabel subtitle = new JLabel("Pick a version, hop on a server, and hit play.");
        subtitle.setForeground(Theme.TEXT_DIM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(subtitle);
        page.add(Box.createVerticalStrut(20));

        JPanel serverCard = card();
        serverCard.setLayout(new BoxLayout(serverCard, BoxLayout.Y_AXIS));
        JLabel serverTitle = new JLabel("Join Server (optional)");
        serverTitle.setForeground(Theme.TEXT);
        serverTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverCard.add(serverTitle);
        serverCard.add(Box.createVerticalStrut(8));

        JPanel serverRow = new JPanel(new BorderLayout(8, 0));
        serverRow.setOpaque(false);
        serverField = new JTextField();
        serverField.setBackground(Theme.BG_PANEL_LIGHT);
        serverField.setForeground(Theme.TEXT);
        serverField.setCaretColor(Theme.TEXT);
        JButton pingButton = new JButton("Ping");
        pingButton.addActionListener(this::onPing);
        serverRow.add(serverField, BorderLayout.CENTER);
        serverRow.add(pingButton, BorderLayout.EAST);
        serverRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverCard.add(serverRow);

        serverStatusLabel = new JLabel(" ");
        serverStatusLabel.setForeground(Theme.TEXT_DIM);
        serverStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverCard.add(Box.createVerticalStrut(6));
        serverCard.add(serverStatusLabel);

        page.add(serverCard);
        page.add(Box.createVerticalStrut(20));

        JPanel playRow = new JPanel(new BorderLayout(14, 0));
        playRow.setOpaque(false);
        playRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        playRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel versionCol = new JPanel(new BorderLayout());
        versionCol.setOpaque(false);
        JLabel versionLabel = new JLabel("Version");
        versionLabel.setForeground(Theme.TEXT);
        versionCombo = new JComboBox<>();
        versionCol.add(versionLabel, BorderLayout.NORTH);
        versionCol.add(versionCombo, BorderLayout.CENTER);
        playRow.add(versionCol, BorderLayout.CENTER);

        playButton = new GlowButton("PLAY", Theme.ACCENT, true, true);
        playButton.setPreferredSize(new Dimension(180, 60));
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        playButton.addActionListener(this::onPlay);
        playRow.add(playButton, BorderLayout.EAST);

        page.add(playRow);
        page.add(Box.createVerticalStrut(14));

        progressLabel = new JLabel(" ");
        progressLabel.setForeground(Theme.TEXT_DIM);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressLabel.setVisible(false);
        page.add(progressLabel);

        progressBar = new ShimmerProgressBar();
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        progressBar.setVisible(false);
        page.add(Box.createVerticalStrut(6));
        page.add(progressBar);

        page.add(Box.createVerticalGlue());
        return page;
    }

    private JComponent buildInstancesPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(30, 36, 20, 36));

        JLabel title = new JLabel("Instances & Mods");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(title);

        JLabel subtitle = new JLabel("Install versions and jump straight to game folders.");
        subtitle.setForeground(Theme.TEXT_DIM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(subtitle);
        page.add(Box.createVerticalStrut(20));

        GlowButton installBtn = new GlowButton("Install New Version", Theme.ACCENT_2);
        installBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        installBtn.setPreferredSize(new Dimension(220, 42));
        installBtn.setMaximumSize(new Dimension(260, 42));
        installBtn.addActionListener(e -> openVersionInstaller());
        page.add(installBtn);
        page.add(Box.createVerticalStrut(10));

        JButton modsFolder = new JButton("Open Mods Folder for Selected Version");
        modsFolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        modsFolder.addActionListener(e -> openProfileFolder("mods"));
        page.add(modsFolder);
        page.add(Box.createVerticalStrut(6));

        JButton resourcePacks = new JButton("Open Resource Packs Folder");
        resourcePacks.setAlignmentX(Component.LEFT_ALIGNMENT);
        resourcePacks.addActionListener(e -> openProfileFolder("resourcepacks"));
        page.add(resourcePacks);
        page.add(Box.createVerticalStrut(6));

        JButton screenshots = new JButton("Open Screenshots Folder");
        screenshots.setAlignmentX(Component.LEFT_ALIGNMENT);
        screenshots.addActionListener(e -> openProfileFolder("screenshots"));
        page.add(screenshots);

        page.add(Box.createVerticalGlue());
        return page;
    }

    private JPanel buildConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(0, 220));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JLabel header = new JLabel("Console");
        header.setForeground(Theme.TEXT);
        header.setBorder(new EmptyBorder(6, 10, 6, 10));
        panel.add(header, BorderLayout.NORTH);

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(Theme.BG_DARK);
        consoleArea.setForeground(Theme.TEXT);
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(consoleArea);
        panel.add(scroll, BorderLayout.CENTER);

        JButton saveLog = new JButton("Save Log");
        saveLog.addActionListener(e -> saveLog());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(saveLog);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        return p;
    }

    // -------------------------------------------------------------- ACTIONS

    private void toggleConsole() {
        consoleVisible = !consoleVisible;
        consoleWrapper.setVisible(consoleVisible);
        revalidate();
    }

    private void openAccounts() {
        AccountsDialog dlg = new AccountsDialog(this, accountManager, this::updateAccountLabel);
        dlg.setVisible(true);
        config.save();
    }

    private void openSettings() {
        SettingsDialog dlg = new SettingsDialog(this, config);
        dlg.setVisible(true);
        if (dlg.wasSaved()) {
            loadInstalledVersions();
            appendConsole("[Launcher] Settings saved.\n");
        }
    }

    private void openVersionInstaller() {
        VersionInstallDialog dlg = new VersionInstallDialog(this, config.getMinecraftDir(), id -> loadInstalledVersions());
        dlg.setVisible(true);
    }

    private void openProfileFolder(String sub) {
        String versionId = (String) versionCombo.getSelectedItem();
        if (versionId == null || versionId.contains("No installed")) {
            JOptionPane.showMessageDialog(this, "Please select a version first.");
            return;
        }
        Path profileDir = config.getMinecraftDir().resolve("profiles").resolve(versionId).resolve(sub);
        try {
            java.nio.file.Files.createDirectories(profileDir);
            Desktop.getDesktop().open(profileDir.toFile());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open folder: " + e.getMessage());
        }
    }

    private void onPing(ActionEvent e) {
        String address = serverField.getText().trim();
        if (address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a server address to ping.");
            return;
        }
        serverStatusLabel.setForeground(Theme.TEXT_DIM);
        serverStatusLabel.setText("Pinging...");

        String host = address;
        int port = 25565;
        if (address.contains(":")) {
            String[] parts = address.split(":", 2);
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        final String fHost = host;
        final int fPort = port;

        new SwingWorker<ServerPinger.PingResult, Void>() {
            protected ServerPinger.PingResult doInBackground() {
                return ServerPinger.ping(fHost, fPort, 4000);
            }

            protected void done() {
                try {
                    ServerPinger.PingResult r = get();
                    if (r.online) {
                        serverStatusLabel.setForeground(Theme.SUCCESS);
                        serverStatusLabel.setText("Online \u2014 " + r.playersOnline + "/" + r.playersMax
                                + " players  |  " + r.versionName);
                    } else {
                        serverStatusLabel.setForeground(Theme.ERROR);
                        serverStatusLabel.setText("Offline or unreachable: " + r.error);
                    }
                } catch (Exception ex) {
                    serverStatusLabel.setForeground(Theme.ERROR);
                    serverStatusLabel.setText("Ping failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onPlay(ActionEvent e) {
        String versionId = (String) versionCombo.getSelectedItem();
        if (versionId == null || versionId.contains("No installed")) {
            JOptionPane.showMessageDialog(this, "No Minecraft version selected!");
            return;
        }
        Account account = accountManager.getActive();
        if (account == null) {
            JOptionPane.showMessageDialog(this, "No account selected. Open Accounts to add one.");
            return;
        }

        config.set("server", serverField.getText().trim());
        config.save();

        setControlsEnabled(false);
        progressLabel.setVisible(true);
        progressBar.setVisible(true);
        progressLabel.setText("Preparing to launch " + versionId + "...");
        appendConsole("[Launcher] Preparing to launch version: " + versionId + "\n");

        String modLoader = config.getString("modLoader", "None");

        new SwingWorker<Process, Void>() {
            String error = null;

            protected Process doInBackground() {
                try {
                    String launchId = versionId;
                    if (modLoader.equals("Fabric")) {
                        SwingUtilities.invokeLater(() -> progressLabel.setText("Checking Fabric loader..."));
                        List<String> loaders = FabricSupport.listLoaderVersions(versionId);
                        if (!loaders.isEmpty()) {
                            launchId = FabricSupport.install(config.getMinecraftDir(), versionId, loaders.get(0),
                                    (text, done, total) -> SwingUtilities.invokeLater(() -> progressLabel.setText(text)));
                        }
                    }
                    MinecraftLauncher launcher = new MinecraftLauncher(config);
                    String server = serverField.getText().trim();
                    return launcher.launch(launchId, account, server.isEmpty() ? null : server,
                            line -> SwingUtilities.invokeLater(() -> appendConsole(line + "\n")));
                } catch (Exception ex) {
                    error = ex.getMessage();
                    return null;
                }
            }

            protected void done() {
                setControlsEnabled(true);
                progressLabel.setVisible(false);
                progressBar.setVisible(false);
                if (error != null) {
                    appendConsole("[ERROR] " + error + "\n");
                    JOptionPane.showMessageDialog(MainFrame.this, error, "Launch Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        playButton.setEnabled(enabled);
        versionCombo.setEnabled(enabled);
    }

    private void loadInstalledVersions() {
        MinecraftInstaller installer = new MinecraftInstaller(config.getMinecraftDir(), null);
        List<String> installed = installer.getInstalledVersions();
        Vector<String> items = new Vector<>();
        if (installed.isEmpty()) {
            items.add("No installed versions found!");
        } else {
            items.addAll(installed);
        }
        versionCombo.setModel(new DefaultComboBoxModel<>(items));
        versionCombo.setEnabled(!installed.isEmpty());
    }

    private void updateAccountLabel() {
        Account acc = accountManager.getActive();
        if (acc != null) {
            String type = acc.type.equals("msa") ? "MSA" : "Offline";
            accountLabel.setText("<html>Logged in as<br><b>" + acc.name + "</b> (" + type + ")</html>");
        } else {
            accountLabel.setText("<html>No account selected.<br>Open Accounts to add one.</html>");
        }
    }

    private void appendConsole(String text) {
        consoleArea.append(text);
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    private void saveLog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("launcher_log.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), consoleArea.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save log: " + e.getMessage());
            }
        }
    }
}
