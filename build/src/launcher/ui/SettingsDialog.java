package launcher.ui;

import launcher.Config;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsDialog extends JDialog {

    private final Config config;
    private final JTextField mcDirField;
    private final JTextField javaPathField;
    private final JSpinner ramSpinner;
    private final JComboBox<String> loaderCombo;
    private final JTextField jvmArgsField;
    private boolean saved = false;

    public SettingsDialog(Frame owner, Config config) {
        super(owner, "Settings", true);
        this.config = config;
        setSize(520, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(Theme.BG_DARKEST);

        JLabel title = new JLabel("Settings");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(12));

        mcDirField = new JTextField(config.getString("minecraftDirectory", ""));
        root.add(labeledRow("Minecraft Folder:", mcDirField, true));
        root.add(Box.createVerticalStrut(8));

        javaPathField = new JTextField(config.getString("javaPath", ""));
        root.add(labeledRow("Custom Java Path (optional):", javaPathField, true));
        root.add(Box.createVerticalStrut(8));

        JPanel optionsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        optionsRow.setOpaque(false);
        optionsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ramCol = new JPanel(new BorderLayout());
        ramCol.setOpaque(false);
        JLabel ramLabel = new JLabel("RAM (MB):");
        ramLabel.setForeground(Theme.TEXT);
        ramSpinner = new JSpinner(new SpinnerNumberModel(config.getInt("ram", 4096), 1024, 32768, 512));
        ramCol.add(ramLabel, BorderLayout.NORTH);
        ramCol.add(ramSpinner, BorderLayout.CENTER);
        optionsRow.add(ramCol);

        JPanel loaderCol = new JPanel(new BorderLayout());
        loaderCol.setOpaque(false);
        JLabel loaderLabel = new JLabel("Mod Loader:");
        loaderLabel.setForeground(Theme.TEXT);
        loaderCombo = new JComboBox<>(new String[]{"None", "Fabric", "Forge"});
        loaderCombo.setSelectedItem(config.getString("modLoader", "None"));
        loaderCol.add(loaderLabel, BorderLayout.NORTH);
        loaderCol.add(loaderCombo, BorderLayout.CENTER);
        optionsRow.add(loaderCol);

        root.add(optionsRow);
        root.add(Box.createVerticalStrut(8));

        jvmArgsField = new JTextField(config.getString("extraJvmArgs", ""));
        root.add(labeledRow("Extra JVM Arguments:", jvmArgsField, false));
        root.add(Box.createVerticalStrut(14));

        JPanel utilRow = new JPanel(new GridLayout(1, 2, 8, 0));
        utilRow.setOpaque(false);
        utilRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton clearCache = new JButton("Clear Launcher Cache");
        clearCache.addActionListener(e -> clearCache());
        utilRow.add(clearCache);
        root.add(utilRow);
        root.add(Box.createVerticalStrut(14));

        GlowButton save = new GlowButton("Save and Close", Theme.ACCENT);
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setPreferredSize(new Dimension(100, 42));
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        save.addActionListener(e -> {
            saveValues();
            saved = true;
            dispose();
        });
        root.add(save);

        setContentPane(root);
    }

    private JPanel labeledRow(String labelText, JTextField field, boolean withBrowse) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);

        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(field, BorderLayout.CENTER);
        if (withBrowse) {
            JButton browse = new JButton("Browse");
            browse.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    field.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            });
            row.add(browse, BorderLayout.EAST);
        }
        panel.add(row);
        return panel;
    }

    private void clearCache() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This deletes downloaded assets/versions so they re-download next launch. Continue?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            Path mcDir = Path.of(mcDirField.getText());
            deleteRecursive(mcDir.resolve("assets").resolve("indexes"));
            deleteRecursive(mcDir.resolve("assets").resolve("objects"));
            deleteRecursive(mcDir.resolve("versions"));
            JOptionPane.showMessageDialog(this, "Cache cleared.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not clear cache: " + e.getMessage());
        }
    }

    private void deleteRecursive(Path path) throws Exception {
        if (!Files.exists(path)) return;
        try (var walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private void saveValues() {
        config.set("minecraftDirectory", mcDirField.getText());
        config.set("javaPath", javaPathField.getText());
        config.set("ram", ((Number) ramSpinner.getValue()).doubleValue());
        config.set("modLoader", (String) loaderCombo.getSelectedItem());
        config.set("extraJvmArgs", jvmArgsField.getText());
        config.save();
    }

    public boolean wasSaved() {
        return saved;
    }
}
