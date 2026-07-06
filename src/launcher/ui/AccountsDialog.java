package launcher.ui;

import launcher.Account;
import launcher.AccountManager;
import launcher.MicrosoftAuth;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.List;

public class AccountsDialog extends JDialog {

    private final AccountManager accountManager;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private List<Account> accounts;
    private final Runnable onChanged;

    public AccountsDialog(Frame owner, AccountManager accountManager, Runnable onChanged) {
        super(owner, "Account Manager", true);
        this.accountManager = accountManager;
        this.onChanged = onChanged;
        setSize(420, 400);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(Theme.BG_DARKEST);

        JLabel title = new JLabel("Accounts");
        title.setForeground(Theme.TEXT);
        title.setFont(Theme.FONT_TITLE);
        root.add(title, BorderLayout.NORTH);

        list.setBackground(Theme.BG_PANEL_LIGHT);
        list.setForeground(Theme.TEXT);
        list.setSelectionBackground(Theme.ACCENT);
        list.addListSelectionListener(e -> {
        });
        root.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel south = new JPanel(new GridLayout(3, 1, 6, 6));
        south.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 6, 0));
        row1.setOpaque(false);
        JButton addMsa = new JButton("Add Microsoft Account");
        JButton addOffline = new JButton("Add Offline Account");
        row1.add(addMsa);
        row1.add(addOffline);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 6, 0));
        row2.setOpaque(false);
        JButton remove = new JButton("Remove Selected");
        GlowButton use = new GlowButton("Use Selected", Theme.ACCENT);
        row2.add(remove);
        row2.add(use);

        south.add(row1);
        south.add(row2);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);

        addOffline.addActionListener(e -> addOfflineAccount());
        addMsa.addActionListener(e -> addMicrosoftAccount());
        remove.addActionListener(e -> removeSelected());
        use.addActionListener(e -> useSelected());

        refresh();
    }

    private void refresh() {
        accounts = accountManager.list();
        model.clear();
        String activeUuid = accountManager.getActive() != null ? accountManager.getActive().uuid : "";
        for (Account a : accounts) {
            String tag = a.type.equals("msa") ? "MSA" : "Offline";
            String prefix = a.uuid.equals(activeUuid) ? "\u25B6 " : "";
            model.addElement(prefix + "[" + tag + "] " + a.name);
        }
    }

    private void addOfflineAccount() {
        String name = JOptionPane.showInputDialog(this, "Enter username:");
        if (name == null || name.isBlank()) return;
        if (accountManager.nameTaken(name.trim())) {
            JOptionPane.showMessageDialog(this, "An account with this username already exists.");
            return;
        }
        accountManager.addOffline(name.trim());
        refresh();
        if (onChanged != null) onChanged.run();
    }

    private JDialog deviceCodeDialog;

    private void addMicrosoftAccount() {
        MicrosoftAuth auth = new MicrosoftAuth();
        new SwingWorker<Account, Void>() {
            String error = null;

            protected Account doInBackground() {
                try {
                    MicrosoftAuth.DeviceCode dc = auth.requestDeviceCode();
                    SwingUtilities.invokeLater(() -> showDeviceCodeDialog(dc));
                    var tokens = auth.pollForToken(dc);
                    return auth.completeLogin(tokens.get("access_token"), tokens.get("refresh_token"));
                } catch (Exception e) {
                    error = e.getMessage();
                    return null;
                }
            }

            protected void done() {
                // Close the "waiting for sign-in" dialog now that polling has
                // finished, whether it succeeded, failed, or was cancelled.
                // Previously this dialog was a blocking JOptionPane, so even
                // after a successful login the account list/label would not
                // update until the user manually clicked OK on it.
                if (deviceCodeDialog != null) {
                    deviceCodeDialog.dispose();
                    deviceCodeDialog = null;
                }

                if (isCancelled()) {
                    return;
                }

                if (error != null) {
                    JOptionPane.showMessageDialog(AccountsDialog.this, "Microsoft login failed: " + error,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    Account acc = get();
                    if (acc != null) {
                        accountManager.addOrUpdateMsa(acc.name, acc.uuid, acc.accessToken, acc.refreshToken);
                        accountManager.setActive(acc.uuid);
                        refresh();
                        if (onChanged != null) onChanged.run();
                        JOptionPane.showMessageDialog(AccountsDialog.this,
                                "Signed in as " + acc.name);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void showDeviceCodeDialog(MicrosoftAuth.DeviceCode dc) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        panel.add(new JLabel("Open this link in your browser:"));
        JTextField urlField = new JTextField(dc.verificationUri);
        urlField.setEditable(false);
        panel.add(urlField);
        panel.add(new JLabel("And enter the code:"));
        JTextField codeField = new JTextField(dc.userCode);
        codeField.setEditable(false);
        codeField.setFont(Theme.FONT_TITLE);
        panel.add(codeField);
        JButton openBrowser = new JButton("Open in Browser");
        openBrowser.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(dc.verificationUri));
            } catch (Exception ignored) {
            }
        });
        panel.add(openBrowser);
        panel.add(new JLabel("Waiting for you to finish signing in..."));

        JButton cancel = new JButton("Cancel");
        panel.add(cancel);

        // Non-modal so it can close itself automatically once login
        // succeeds/fails/times out, instead of blocking the EDT until the
        // user clicks a button.
        deviceCodeDialog = new JDialog(this, "Microsoft Login", false);
        cancel.addActionListener(e -> deviceCodeDialog.dispose());
        deviceCodeDialog.setContentPane(panel);
        deviceCodeDialog.pack();
        deviceCodeDialog.setLocationRelativeTo(this);
        deviceCodeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        deviceCodeDialog.setVisible(true);
    }

    private void removeSelected() {
        int idx = list.getSelectedIndex();
        if (idx < 0) return;
        Account acc = accounts.get(idx);
        accountManager.remove(acc.uuid);
        refresh();
        if (onChanged != null) onChanged.run();
    }

    private void useSelected() {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Please select an account first.");
            return;
        }
        Account acc = accounts.get(idx);
        accountManager.setActive(acc.uuid);
        refresh();
        if (onChanged != null) onChanged.run();
        dispose();
    }
}
