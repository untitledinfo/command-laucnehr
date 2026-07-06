package launcher.ui;

import launcher.Account;
import launcher.AccountManager;
import launcher.Config;
import launcher.FabricSupport;
import launcher.ForgeSupport;
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

    private IconRailButton navHome;
    private IconRailButton navInstances;
    private IconRailButton navAccounts;
    private IconRailButton navSettings;
    private IconRailButton navConsole;
    private JLabel accountLabel;
    private JLabel avatarBubble;
    private DefaultListModel<Account> playersModel;
    private JList<Account> playersList;

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

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildIconRail(), BorderLayout.WEST);
        root.add(buildPlayersPanel(), BorderLayout.EAST);

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

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 56));
        bar.setBackground(Theme.BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        JLabel brand = new JLabel("\u26A1 COMMAND LAUNCHER");
        brand.setForeground(Theme.ACCENT);
        brand.setFont(Theme.FONT_BOLD);
        left.add(brand);

        // Small colored chips, purely decorative flair (mirrors the row of
        // game/community badges in the reference design).
        for (Color c : new Color[]{Theme.CHIP_1, Theme.CHIP_2, Theme.CHIP_3, Theme.CHIP_4}) {
            left.add(chipDot(c));
        }
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(0, 0, 0, 14));

        avatarBubble = new JLabel("?", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.antialias(g2);
                g2.setPaint(Theme.accentGradient(getWidth(), getHeight()));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarBubble.setForeground(Color.WHITE);
        avatarBubble.setFont(Theme.FONT_BOLD);
        avatarBubble.setPreferredSize(new Dimension(30, 30));
        avatarBubble.setOpaque(false);
        right.add(avatarBubble);

        accountLabel = new JLabel("No account selected");
        accountLabel.setForeground(Theme.TEXT);
        accountLabel.setFont(Theme.FONT_SUBTITLE);
        right.add(accountLabel);

        JButton settingsBtn = flatIconButton("\u2699", "Settings");
        settingsBtn.addActionListener(e -> openSettings());
        right.add(settingsBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent chipDot(Color c) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.antialias(g2);
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(16, 16));
        return dot;
    }

    private JButton flatIconButton(String icon, String tooltip) {
        JButton b = new JButton(icon);
        b.setToolTipText(tooltip);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        b.setForeground(Theme.TEXT_DIM);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel buildIconRail() {
        JPanel rail = new JPanel();
        rail.setPreferredSize(new Dimension(64, 0));
        rail.setBackground(Theme.BG_PANEL);
        rail.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.add(Box.createVerticalStrut(16));

        navHome = new IconRailButton("\uD83C\uDFE0", "Home");
        navInstances = new IconRailButton("\uD83E\uDDE9", "Instances");
        navAccounts = new IconRailButton("\uD83D\uDC64", "Accounts");
        navSettings = new IconRailButton("\u2699", "Settings");
        navConsole = new IconRailButton("\uD83D\uDCBB", "Console");

        ButtonGroup group = new ButtonGroup();
        group.add(navHome);
        group.add(navInstances);

        for (IconRailButton b : new IconRailButton[]{navHome, navInstances, navAccounts, navSettings, navConsole}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            rail.add(b);
            rail.add(Box.createVerticalStrut(8));
        }
        navHome.setSelected(true);

        navHome.addActionListener(e -> switchPage("home", navHome));
        navInstances.addActionListener(e -> switchPage("instances", navInstances));
        navAccounts.addActionListener(e -> {
            openAccounts();
            navAccounts.setSelected(false);
        });
        navSettings.addActionListener(e -> {
            openSettings();
            navSettings.setSelected(false);
        });
        navConsole.addActionListener(e -> toggleConsole());

        rail.add(Box.createVerticalGlue());
        return rail;
    }

    private JPanel buildPlayersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(190, 0));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER));

        JLabel header = new JLabel("PLAYERS");
        header.setForeground(Theme.TEXT_DIM);
        header.setFont(Theme.FONT_SUBTITLE);
        header.setBorder(new EmptyBorder(14, 14, 8, 14));
        panel.add(header, BorderLayout.NORTH);

        playersModel = new DefaultListModel<>();
        playersList = new JList<>(playersModel);
        playersList.setOpaque(false);
        playersList.setCellRenderer(new PlayerCellRenderer());
        playersList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Account a = playersList.getSelectedValue();
                    if (a != null) {
                        accountManager.setActive(a.uuid);
                        updateAccountLabel();
                        refreshPlayersPanel();
                    }
                }
            }
        });
        JScrollPane scroll = new JScrollPane(playersList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JButton addAccount = new JButton("+ Add Account");
        addAccount.setBorder(new EmptyBorder(8, 10, 12, 10));
        addAccount.setContentAreaFilled(false);
        addAccount.setForeground(Theme.ACCENT_SOFT);
        addAccount.setFocusPainted(false);
        addAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addAccount.addActionListener(e -> openAccounts());
        panel.add(addAccount, BorderLayout.SOUTH);

        refreshPlayersPanel();
        return panel;
    }

    private void refreshPlayersPanel() {
        if (playersModel == null) return;
        playersModel.clear();
        for (Account a : accountManager.list()) {
            playersModel.addElement(a);
        }
        playersList.repaint();
    }

    /** Renders each account as a small "friend row": avatar dot + name + status. */
    private class PlayerCellRenderer extends JPanel implements ListCellRenderer<Account> {
        private final JLabel dot = new JLabel("\u25CF");
        private final JLabel name = new JLabel();
        private final JLabel status = new JLabel();

        PlayerCellRenderer() {
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(new EmptyBorder(6, 14, 6, 10));
            JPanel textCol = new JPanel();
            textCol.setOpaque(false);
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            name.setForeground(Theme.TEXT);
            name.setFont(Theme.FONT_BODY);
            status.setForeground(Theme.TEXT_DIM);
            status.setFont(Theme.FONT_SUBTITLE);
            textCol.add(name);
            textCol.add(status);
            add(dot, BorderLayout.WEST);
            add(textCol, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Account> list, Account value, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            Account active = accountManager.getActive();
            boolean isActive = active != null && active.uuid.equals(value.uuid);
            dot.setForeground(isActive ? Theme.SUCCESS : Theme.TEXT_DIM);
            name.setText(value.name);
            status.setText(isActive ? "Selected \u00b7 " + (value.type.equals("msa") ? "Microsoft" : "Offline")
                    : (value.type.equals("msa") ? "Microsoft" : "Offline"));
            setBackground(isSelected ? Theme.BG_PANEL_LIGHT : null);
            setOpaque(isSelected);
            return this;
        }
    }

    private void switchPage(String name, IconRailButton btn) {
        cardLayout.show(cards, name);
        btn.setSelected(true);
        if (name.equals("home")) homeFade.playFadeIn();
        else instancesFade.playFadeIn();
    }

    private JComponent buildHomePage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(24, 30, 20, 30));

        page.add(buildHeroPanel());
        page.add(Box.createVerticalStrut(22));

        JLabel newsTitle = new JLabel("LATEST NEWS");
        newsTitle.setForeground(Theme.TEXT_DIM);
        newsTitle.setFont(Theme.FONT_SUBTITLE);
        newsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(newsTitle);
        page.add(Box.createVerticalStrut(10));

        JPanel newsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        newsRow.setOpaque(false);
        newsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        newsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        newsRow.add(new NewsCard("\uD83C\uDD95", "What's New in v3.2",
                "Crash-on-open fix, safer Microsoft sign-in, and more.", Theme.ACCENT, Theme.ACCENT_2));
        newsRow.add(new NewsCard("\uD83D\uDC65", "Join the Community",
                "Add a server address above and hit Ping to check status.", Theme.CHIP_3, Theme.LAUNCH_GREEN_2));
        newsRow.add(new NewsCard("\uD83E\uDDE9", "Manage Instances",
                "Install Fabric/Forge versions from the Instances tab.", Theme.CHIP_4, Theme.ACCENT));
        page.add(newsRow);

        page.add(Box.createVerticalGlue());
        return page;
    }

    /** Dark gradient hero card containing the server-join row and the big LAUNCH pill. */
    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.antialias(g2);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x17, 0x17, 0x24), w, h, new Color(0x0e, 0x0e, 0x16));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                // a couple of soft accent glows in the corners, evoking the
                // starfield/character backdrop from the reference design
                g2.setPaint(new RadialGradientPaint(w * 0.85f, h * 0.2f, Math.max(w, h) * 0.6f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), 40),
                                new Color(0, 0, 0, 0)}));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(22, 26, 26, 26));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel title = new JLabel("Welcome back");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(title);

        JLabel subtitle = new JLabel("Pick a version, hop on a server, and hit launch.");
        subtitle.setForeground(Theme.TEXT_DIM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(subtitle);
        hero.add(Box.createVerticalStrut(16));

        JPanel serverRow = new JPanel(new BorderLayout(8, 0));
        serverRow.setOpaque(false);
        serverRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel serverLabel = new JLabel("Server:");
        serverLabel.setForeground(Theme.TEXT_DIM);
        serverField = new JTextField();
        serverField.setBackground(Theme.BG_PANEL_LIGHT);
        serverField.setForeground(Theme.TEXT);
        serverField.setCaretColor(Theme.TEXT);
        JButton pingButton = new JButton("Ping");
        pingButton.addActionListener(this::onPing);
        serverRow.add(serverLabel, BorderLayout.WEST);
        serverRow.add(serverField, BorderLayout.CENTER);
        serverRow.add(pingButton, BorderLayout.EAST);
        hero.add(serverRow);

        serverStatusLabel = new JLabel(" ");
        serverStatusLabel.setForeground(Theme.TEXT_DIM);
        serverStatusLabel.setFont(Theme.FONT_SUBTITLE);
        serverStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverStatusLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        hero.add(serverStatusLabel);

        hero.add(Box.createVerticalGlue());

        JPanel launchRow = new JPanel(new BorderLayout(16, 0));
        launchRow.setOpaque(false);
        launchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        launchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        versionCombo = new JComboBox<>();
        versionCombo.setMaximumSize(new Dimension(220, 34));
        versionCombo.setPreferredSize(new Dimension(220, 34));
        JPanel versionWrap = new JPanel(new BorderLayout());
        versionWrap.setOpaque(false);
        versionWrap.add(versionCombo, BorderLayout.SOUTH);
        launchRow.add(versionWrap, BorderLayout.WEST);

        // Big rounded "LAUNCH" pill button, matching the reference design's
        // capsule-shaped play button.
        playButton = new GlowButton("\u25B6  LAUNCH", Theme.LAUNCH_GREEN, Theme.LAUNCH_GREEN_2, true, true, 999);
        playButton.setPreferredSize(new Dimension(260, 58));
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        playButton.addActionListener(this::onPlay);
        JPanel launchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        launchWrap.setOpaque(false);
        launchWrap.add(playButton);
        launchRow.add(launchWrap, BorderLayout.EAST);

        hero.add(launchRow);
        hero.add(Box.createVerticalStrut(10));

        progressLabel = new JLabel(" ");
        progressLabel.setForeground(Theme.TEXT_DIM);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressLabel.setVisible(false);
        hero.add(progressLabel);

        progressBar = new ShimmerProgressBar();
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        progressBar.setVisible(false);
        hero.add(Box.createVerticalStrut(6));
        hero.add(progressBar);

        return hero;
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
        page.add(Box.createVerticalStrut(6));

        JButton openRoot = new JButton("Open .minecraft Folder");
        openRoot.setAlignmentX(Component.LEFT_ALIGNMENT);
        openRoot.addActionListener(e -> {
            try {
                java.nio.file.Files.createDirectories(config.getMinecraftDir());
                Desktop.getDesktop().open(config.getMinecraftDir().toFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open folder: " + ex.getMessage());
            }
        });
        page.add(openRoot);
        page.add(Box.createVerticalStrut(16));

        JButton deleteVersion = new JButton("Delete Selected Version");
        deleteVersion.setForeground(Theme.ERROR);
        deleteVersion.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteVersion.addActionListener(e -> deleteSelectedVersion());
        page.add(deleteVersion);

        page.add(Box.createVerticalGlue());
        return page;
    }

    private void deleteSelectedVersion() {
        String versionId = (String) versionCombo.getSelectedItem();
        if (versionId == null || versionId.contains("No installed")) {
            JOptionPane.showMessageDialog(this, "Please select a version first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + versionId + "\" and its profile data? This can't be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            new MinecraftInstaller(config.getMinecraftDir(), null).deleteVersion(versionId);
            loadInstalledVersions();
            appendConsole("[Launcher] Deleted version " + versionId + "\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not delete version: " + ex.getMessage());
        }
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
                    } else if (modLoader.equals("Forge")) {
                        String existing = findExistingForgeVersion(versionId);
                        if (existing != null) {
                            launchId = existing;
                        } else {
                            SwingUtilities.invokeLater(() -> progressLabel.setText("Looking up Forge build..."));
                            String forgeVersion = ForgeSupport.recommendedForgeVersion(versionId);
                            if (forgeVersion == null) {
                                throw new java.io.IOException("No Forge build found for " + versionId);
                            }
                            String javaExe = MinecraftLauncher.resolveJavaExecutable(config.getString("javaPath", ""));
                            launchId = ForgeSupport.install(config.getMinecraftDir(), versionId, forgeVersion, javaExe,
                                    (text, done, total) -> SwingUtilities.invokeLater(() -> {
                                        progressLabel.setText(text);
                                        appendConsole(text + "\n");
                                    }));
                        }
                    }
                    MinecraftLauncher mcLauncher = new MinecraftLauncher(config);
                    String server = serverField.getText().trim();
                    return mcLauncher.launch(launchId, account, server.isEmpty() ? null : server,
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
                loadInstalledVersions();
                if (error != null) {
                    appendConsole("[ERROR] " + error + "\n");
                    JOptionPane.showMessageDialog(MainFrame.this, error, "Launch Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String findExistingForgeVersion(String mcVersion) {
        MinecraftInstaller installer = new MinecraftInstaller(config.getMinecraftDir(), null);
        for (String id : installer.getInstalledVersions()) {
            if (id.startsWith(mcVersion) && id.toLowerCase().contains("forge")) {
                return id;
            }
        }
        return null;
    }

    private void setControlsEnabled(boolean enabled) {
        playButton.setEnabled(enabled);
        versionCombo.setEnabled(enabled);
    }

    private void loadInstalledVersions() {
        String previousSelection = (String) versionCombo.getSelectedItem();
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
        if (previousSelection != null && items.contains(previousSelection)) {
            versionCombo.setSelectedItem(previousSelection);
        }
    }

    private void updateAccountLabel() {
        Account acc = accountManager.getActive();
        if (acc != null) {
            String type = acc.type.equals("msa") ? "MSA" : "Offline";
            accountLabel.setText(acc.name + " (" + type + ")");
            avatarBubble.setText(acc.name.substring(0, 1).toUpperCase());
        } else {
            accountLabel.setText("No account selected");
            avatarBubble.setText("?");
        }
        refreshPlayersPanel();
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
