package net.minecraft.launcher.ui.bottombar;

import com.google.common.collect.Lists;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.profile.RefreshedProfilesListener;
import net.minecraft.launcher.ui.popups.profile.ProfileEditorPopup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProfileSelectionPanel extends JPanel implements ActionListener, ItemListener, RefreshedProfilesListener
{
    private static final Logger LOGGER;
    private final JComboBox profileList;
    private final JButton newProfileButton;
    private final JButton editProfileButton;
    private final Launcher minecraftLauncher;
    private boolean skipSelectionUpdate;
    
    public ProfileSelectionPanel(final Launcher minecraftLauncher) {
        this.profileList = new JComboBox();
        this.newProfileButton = new JButton("New Profile");
        this.editProfileButton = new JButton("Edit Profile");
        this.minecraftLauncher = minecraftLauncher;
        this.profileList.setRenderer(new ProfileListRenderer());
        this.profileList.addItemListener(this);
        this.profileList.addItem("Loading profiles...");
        this.newProfileButton.addActionListener(this);
        this.editProfileButton.addActionListener(this);
        this.createInterface();
        minecraftLauncher.getProfileManager().addRefreshedProfilesListener(this);
    }
    
    protected void createInterface() {
        this.setLayout(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.weightx = 0.0;
        constraints.gridy = 0;
        this.add(new JLabel("Profile: "), constraints);
        constraints.gridx = 1;
        this.add(this.profileList, constraints);
        constraints.gridx = 0;
        final GridBagConstraints gridBagConstraints = constraints;
        ++gridBagConstraints.gridy;
        final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        buttonPanel.add(this.newProfileButton);
        buttonPanel.add(this.editProfileButton);
        constraints.gridwidth = 2;
        this.add(buttonPanel, constraints);
        constraints.gridwidth = 1;
        final GridBagConstraints gridBagConstraints2 = constraints;
        ++gridBagConstraints2.gridy;
    }
    
    @Override
    public void onProfilesRefreshed(final ProfileManager manager) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProfileSelectionPanel.this.populateProfiles();
            }
        });
    }
    
    public void populateProfiles() {
        final String previous = this.minecraftLauncher.getProfileManager().getSelectedProfile().getName();
        Profile selected = null;
        final List<Profile> profiles = Lists.newArrayList(this.minecraftLauncher.getProfileManager().getProfiles().values());
        this.profileList.removeAllItems();
        Collections.sort(profiles);
        this.skipSelectionUpdate = true;
        for (final Profile profile : profiles) {
            if (previous.equals(profile.getName())) {
                selected = profile;
            }
            this.profileList.addItem(profile);
        }
        if (selected == null) {
            if (profiles.isEmpty()) {
                selected = this.minecraftLauncher.getProfileManager().getSelectedProfile();
                this.profileList.addItem(selected);
            }
            selected = profiles.iterator().next();
        }
        this.profileList.setSelectedItem(selected);
        this.skipSelectionUpdate = false;
    }
    
    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (e.getStateChange() != 1) {
            return;
        }
        if (!this.skipSelectionUpdate && e.getItem() instanceof Profile) {
            final Profile profile = (Profile)e.getItem();
            this.minecraftLauncher.getProfileManager().setSelectedProfile(profile.getName());
            try {
                this.minecraftLauncher.getProfileManager().saveProfiles();
            }
            catch (IOException e2) {
                ProfileSelectionPanel.LOGGER.error("Couldn't save new selected profile", e2);
            }
            this.minecraftLauncher.ensureLoggedIn();
        }
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.newProfileButton) {
            final Profile profile = new Profile(this.minecraftLauncher.getProfileManager().getSelectedProfile());
            profile.setName("Copy of " + profile.getName());
            while (this.minecraftLauncher.getProfileManager().getProfiles().containsKey(profile.getName())) {
                profile.setName(profile.getName() + "_");
            }
            ProfileEditorPopup.showEditProfileDialog(this.getMinecraftLauncher(), profile);
            this.minecraftLauncher.getProfileManager().setSelectedProfile(profile.getName());
        }
        else if (e.getSource() == this.editProfileButton) {
            final Profile profile = this.minecraftLauncher.getProfileManager().getSelectedProfile();
            ProfileEditorPopup.showEditProfileDialog(this.getMinecraftLauncher(), profile);
        }
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
    
    private static class ProfileListRenderer extends BasicComboBoxRenderer
    {
        @Override
        public Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            if (value instanceof Profile) {
                value = ((Profile)value).getName();
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return this;
        }
    }
}
