package launcher.ui;

import launcher.MinecraftInstaller;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VersionInstallDialog extends JDialog {

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> versionList = new JList<>(listModel);
    private final JCheckBox releases = new JCheckBox("Releases", true);
    private final JCheckBox snapshots = new JCheckBox("Snapshots");
    private final JCheckBox betas = new JCheckBox("Beta");
    private final JCheckBox alphas = new JCheckBox("Alpha");
    private final JTextField search = new JTextField();
    private final ShimmerProgressBar progressBar = new ShimmerProgressBar();
    private final JLabel statusLabel = new JLabel(" ");
    private final GlowButton installButton = new GlowButton("Install Selected Version", Theme.ACCENT);

    private List<Map<String, Object>> allVersions;
    private final Path mcDir;
    private final Consumer<String> onInstalled;

    public VersionInstallDialog(Frame owner, Path mcDir, Consumer<String> onInstalled) {
        super(owner, "Install Minecraft Version", true);
        this.mcDir = mcDir;
        this.onInstalled = onInstalled;
        setSize(420, 580);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Theme.BG_DARKEST);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(Theme.BG_DARKEST);

        JLabel title = new JLabel("Install a Version");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        search.setBackground(Theme.BG_PANEL_LIGHT);
        search.setForeground(Theme.TEXT);
        search.setCaretColor(Theme.TEXT);
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setToolTipText("Search versions...");
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
        });
        top.add(search);
        top.add(Box.createVerticalStrut(6));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JCheckBox cb : new JCheckBox[]{releases, snapshots, betas, alphas}) {
            cb.setForeground(Theme.TEXT);
            cb.setOpaque(false);
            cb.addActionListener(e -> refreshList());
            filters.add(cb);
        }
        top.add(filters);
        center.add(top, BorderLayout.NORTH);

        versionList.setBackground(Theme.BG_PANEL_LIGHT);
        versionList.setForeground(Theme.TEXT);
        versionList.setSelectionBackground(Theme.ACCENT);
        JScrollPane scroll = new JScrollPane(versionList);
        center.add(scroll, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        statusLabel.setForeground(Theme.TEXT_DIM);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setPreferredSize(new Dimension(100, 22));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        installButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        installButton.setPreferredSize(new Dimension(100, 42));
        installButton.addActionListener(e -> installSelected());

        bottom.add(statusLabel);
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(progressBar);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(installButton);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        new SwingWorker<List<Map<String, Object>>, Void>() {
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return MinecraftInstaller.fetchVersionList();
            }

            protected void done() {
                try {
                    allVersions = get();
                    refreshList();
                } catch (Exception e) {
                    statusLabel.setText("Failed to load version list: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void refreshList() {
        if (allVersions == null) return;
        listModel.clear();
        String query = search.getText().trim().toLowerCase();
        for (Map<String, Object> v : allVersions) {
            String type = String.valueOf(v.get("type"));
            String id = String.valueOf(v.get("id"));
            if (type.equals("release") && !releases.isSelected()) continue;
            if (type.equals("snapshot") && !snapshots.isSelected()) continue;
            if (type.equals("old_beta") && !betas.isSelected()) continue;
            if (type.equals("old_alpha") && !alphas.isSelected()) continue;
            if (!query.isEmpty() && !id.toLowerCase().contains(query)) continue;
            listModel.addElement(id);
        }
    }

    private void installSelected() {
        String versionId = versionList.getSelectedValue();
        if (versionId == null) {
            JOptionPane.showMessageDialog(this, "Please select a version first.");
            return;
        }
        installButton.setEnabled(false);
        progressBar.setIndeterminate(false);

        MinecraftInstaller installer = new MinecraftInstaller(mcDir, (text, done, total) -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(text);
                if (total > 0) {
                    progressBar.setMaximum((int) Math.min(total, Integer.MAX_VALUE));
                    progressBar.setValue((int) Math.min(done, Integer.MAX_VALUE));
                }
            });
        });

        new SwingWorker<Void, Void>() {
            String error = null;

            protected Void doInBackground() {
                try {
                    installer.installVersion(versionId);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            protected void done() {
                installButton.setEnabled(true);
                if (error != null) {
                    JOptionPane.showMessageDialog(VersionInstallDialog.this,
                            "Install failed: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(VersionInstallDialog.this,
                            "Version " + versionId + " installed!");
                    if (onInstalled != null) onInstalled.accept(versionId);
                }
            }
        }.execute();
    }
}
