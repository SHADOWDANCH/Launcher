package net.minecraft.launcher.ui.popups.login;

import com.google.common.base.Objects;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.launcher.profile.AuthenticationDatabase;
import net.minecraft.launcher.profile.ProfileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ExistingUserListForm extends JPanel implements ActionListener
{
    private static final Logger LOGGER;
    private final LogInPopup popup;
    private final JComboBox userDropdown;
    private final AuthenticationDatabase authDatabase;
    private final JButton playButton;
    private final JButton logOutButton;
    private final ProfileManager profileManager;
    
    public ExistingUserListForm(final LogInPopup popup) {
        this.userDropdown = new JComboBox();
        this.playButton = new JButton("Play");
        this.logOutButton = new JButton("Log Out");
        this.popup = popup;
        this.profileManager = popup.getMinecraftLauncher().getProfileManager();
        this.authDatabase = popup.getMinecraftLauncher().getProfileManager().getAuthDatabase();
        this.fillUsers();
        this.createInterface();
        this.playButton.addActionListener(this);
        this.logOutButton.addActionListener(this);
    }
    
    private void fillUsers() {
        for (final String user : this.authDatabase.getKnownNames()) {
            this.userDropdown.addItem(user);
            if (this.profileManager.getSelectedUser() != null && Objects.equal(this.authDatabase.getByUUID(this.profileManager.getSelectedUser()), this.authDatabase.getByName(user))) {
                this.userDropdown.setSelectedItem(user);
            }
        }
    }
    
    protected void createInterface() {
        this.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        this.add(Box.createGlue());
        final String currentUser = (this.authDatabase.getKnownNames().size() == 1) ? this.authDatabase.getKnownNames().iterator().next() : (this.authDatabase.getKnownNames().size() + " different users");
        final String thisOrThese = (this.authDatabase.getKnownNames().size() == 1) ? "this account" : "one of these accounts";
        this.add(new JLabel("You're already logged in as " + currentUser + "."), constraints);
        this.add(new JLabel("You may use " + thisOrThese + " and skip authentication."), constraints);
        this.add(Box.createVerticalStrut(5), constraints);
        final JLabel usernameLabel = new JLabel("Existing User:");
        final Font labelFont = usernameLabel.getFont().deriveFont(1);
        usernameLabel.setFont(labelFont);
        this.add(usernameLabel, constraints);
        constraints.gridwidth = 1;
        this.add(this.userDropdown, constraints);
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 0.0;
        constraints.insets = new Insets(0, 5, 0, 0);
        this.add(this.playButton, constraints);
        constraints.gridx = 2;
        this.add(this.logOutButton, constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.gridwidth = 2;
        this.add(Box.createVerticalStrut(5), constraints);
        this.add(new JLabel("Alternatively, log in with a new account below:"), constraints);
        this.add(new JPopupMenu.Separator(), constraints);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        final Object selected = this.userDropdown.getSelectedItem();
        final UserAuthentication auth;
        final String uuid;
        if (selected != null && selected instanceof String) {
            auth = this.authDatabase.getByName((String)selected);
            if (auth.getSelectedProfile() == null) {
                uuid = "demo-" + auth.getUserID();
            }
            else {
                uuid = UUIDTypeAdapter.fromUUID(auth.getSelectedProfile().getId());
            }
        }
        else {
            auth = null;
            uuid = null;
        }
        if (e.getSource() == this.playButton) {
            this.popup.setCanLogIn(false);
            this.popup.getMinecraftLauncher().getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    if (auth != null && uuid != null) {
                        try {
                            if (!auth.canPlayOnline()) {
                                auth.logIn();
                            }
                            ExistingUserListForm.this.popup.setLoggedIn(uuid);
                        }
                        catch (AuthenticationException ex) {
                            ExistingUserListForm.this.popup.getErrorForm().displayError(ex, "We couldn't log you back in as " + selected + ".", "Please try to log in again.");
                            ExistingUserListForm.this.removeUser((String)selected, uuid);
                            ExistingUserListForm.this.popup.setCanLogIn(true);
                        }
                    }
                    else {
                        ExistingUserListForm.this.popup.setCanLogIn(true);
                    }
                }
            });
        }
        else if (e.getSource() == this.logOutButton) {
            this.removeUser((String)selected, uuid);
        }
    }
    
    protected void removeUser(final String name, final String uuid) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ExistingUserListForm.this.removeUser(name, uuid);
                }
            });
        }
        else {
            this.userDropdown.removeItem(name);
            this.authDatabase.removeUUID(uuid);
            try {
                this.profileManager.saveProfiles();
            }
            catch (IOException e) {
                ExistingUserListForm.LOGGER.error("Couldn't save profiles whilst removing " + name + " / " + uuid + " from database", e);
            }
            if (this.userDropdown.getItemCount() == 0) {
                this.popup.remove(this);
            }
        }
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
}
