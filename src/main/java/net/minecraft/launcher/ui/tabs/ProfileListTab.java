package net.minecraft.launcher.ui.tabs;

import com.mojang.launcher.OperatingSystem;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.SwingUserInterface;
import net.minecraft.launcher.profile.AuthenticationDatabase;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.profile.RefreshedProfilesListener;
import net.minecraft.launcher.ui.popups.profile.ProfileEditorPopup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProfileListTab extends JScrollPane implements RefreshedProfilesListener
{
    private static final Logger LOGGER;
    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_VERSION = 1;
    private static final int NUM_COLUMNS = 2;
    private final Launcher minecraftLauncher;
    private final ProfileTableModel dataModel;
    private final JTable table;
    private final JPopupMenu popupMenu;
    private final JMenuItem addProfileButton;
    private final JMenuItem copyProfileButton;
    private final JMenuItem deleteProfileButton;
    private final JMenuItem browseGameFolder;
    
    public ProfileListTab(final Launcher minecraftLauncher) {
        this.dataModel = new ProfileTableModel();
        this.table = new JTable(this.dataModel);
        this.popupMenu = new JPopupMenu();
        this.addProfileButton = new JMenuItem("Add Profile");
        this.copyProfileButton = new JMenuItem("Copy Profile");
        this.deleteProfileButton = new JMenuItem("Delete Profile");
        this.browseGameFolder = new JMenuItem("Open Game Folder");
        this.minecraftLauncher = minecraftLauncher;
        this.setViewportView(this.table);
        this.createInterface();
        minecraftLauncher.getProfileManager().addRefreshedProfilesListener(this);
    }
    
    protected void createInterface() {
        this.popupMenu.add(this.addProfileButton);
        this.popupMenu.add(this.copyProfileButton);
        this.popupMenu.add(this.deleteProfileButton);
        this.popupMenu.add(this.browseGameFolder);
        this.table.setFillsViewportHeight(true);
        this.table.setSelectionMode(0);
        this.popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                final int[] selection = ProfileListTab.this.table.getSelectedRows();
                final boolean hasSelection = selection != null && selection.length > 0;
                ProfileListTab.this.copyProfileButton.setEnabled(hasSelection);
                ProfileListTab.this.deleteProfileButton.setEnabled(hasSelection);
                ProfileListTab.this.browseGameFolder.setEnabled(hasSelection);
            }
            
            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
            }
            
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }
        });
        this.addProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Profile profile = new Profile();
                profile.setName("New Profile");
                while (ProfileListTab.this.minecraftLauncher.getProfileManager().getProfiles().containsKey(profile.getName())) {
                    profile.setName(profile.getName() + "_");
                }
                ProfileEditorPopup.showEditProfileDialog(ProfileListTab.this.getMinecraftLauncher(), profile);
            }
        });
        this.copyProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selection = ProfileListTab.this.table.getSelectedRow();
                if (selection < 0 || selection >= ProfileListTab.this.table.getRowCount()) {
                    return;
                }
                final Profile current = ProfileListTab.this.dataModel.profiles.get(selection);
                final Profile copy = new Profile(current);
                copy.setName("Copy of " + current.getName());
                while (ProfileListTab.this.minecraftLauncher.getProfileManager().getProfiles().containsKey(copy.getName())) {
                    copy.setName(copy.getName() + "_");
                }
                ProfileEditorPopup.showEditProfileDialog(ProfileListTab.this.getMinecraftLauncher(), copy);
            }
        });
        this.browseGameFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selection = ProfileListTab.this.table.getSelectedRow();
                if (selection < 0 || selection >= ProfileListTab.this.table.getRowCount()) {
                    return;
                }
                final Profile profile = ProfileListTab.this.dataModel.profiles.get(selection);
                OperatingSystem.openFolder((profile.getGameDir() == null) ? ProfileListTab.this.minecraftLauncher.getLauncher().getWorkingDirectory() : profile.getGameDir());
            }
        });
        this.deleteProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selection = ProfileListTab.this.table.getSelectedRow();
                if (selection < 0 || selection >= ProfileListTab.this.table.getRowCount()) {
                    return;
                }
                final Profile current = ProfileListTab.this.dataModel.profiles.get(selection);
                final int result = JOptionPane.showOptionDialog(((SwingUserInterface)ProfileListTab.this.minecraftLauncher.getUserInterface()).getFrame(), "Are you sure you want to delete this profile?", "Profile Confirmation", 0, 2, null, LauncherConstants.CONFIRM_PROFILE_DELETION_OPTIONS, LauncherConstants.CONFIRM_PROFILE_DELETION_OPTIONS[0]);
                if (result == 0) {
                    ProfileListTab.this.minecraftLauncher.getProfileManager().getProfiles().remove(current.getName());
                    try {
                        ProfileListTab.this.minecraftLauncher.getProfileManager().saveProfiles();
                        ProfileListTab.this.minecraftLauncher.getProfileManager().fireRefreshEvent();
                    }
                    catch (IOException ex) {
                        ProfileListTab.LOGGER.error("Couldn't save profiles whilst deleting '" + current.getName() + "'", ex);
                    }
                }
            }
        });
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    final int row = ProfileListTab.this.table.getSelectedRow();
                    if (row >= 0 && row < ProfileListTab.this.dataModel.profiles.size()) {
                        ProfileEditorPopup.showEditProfileDialog(ProfileListTab.this.getMinecraftLauncher(), ProfileListTab.this.dataModel.profiles.get(row));
                    }
                }
            }
            
            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    final int r = ProfileListTab.this.table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < ProfileListTab.this.table.getRowCount()) {
                        ProfileListTab.this.table.setRowSelectionInterval(r, r);
                    }
                    else {
                        ProfileListTab.this.table.clearSelection();
                    }
                    ProfileListTab.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    final int r = ProfileListTab.this.table.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < ProfileListTab.this.table.getRowCount()) {
                        ProfileListTab.this.table.setRowSelectionInterval(r, r);
                    }
                    else {
                        ProfileListTab.this.table.clearSelection();
                    }
                    ProfileListTab.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    @Override
    public void onProfilesRefreshed(final ProfileManager manager) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProfileListTab.this.dataModel.setProfiles(manager.getProfiles().values());
            }
        });
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
    
    private class ProfileTableModel extends AbstractTableModel
    {
        private final List<Profile> profiles;
        
        private ProfileTableModel() {
            this.profiles = new ArrayList<Profile>();
        }
        
        @Override
        public int getRowCount() {
            return this.profiles.size();
        }
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public Class<?> getColumnClass(final int columnIndex) {
            return String.class;
        }
        
        @Override
        public String getColumnName(final int column) {
            switch (column) {
                case 1: {
                    return "Version";
                }
                case 0: {
                    return "Version name";
                }
                default: {
                    return super.getColumnName(column);
                }
            }
        }
        
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            final Profile profile = this.profiles.get(rowIndex);
            final AuthenticationDatabase authDatabase = ProfileListTab.this.minecraftLauncher.getProfileManager().getAuthDatabase();
            switch (columnIndex) {
                case 0: {
                    return profile.getName();
                }
                case 1: {
                    if (profile.getLastVersionId() == null) {
                        return "(Latest version)";
                    }
                    return profile.getLastVersionId();
                }
                default: {
                    return null;
                }
            }
        }
        
        public void setProfiles(final Collection<Profile> profiles) {
            this.profiles.clear();
            this.profiles.addAll(profiles);
            Collections.sort(this.profiles);
            this.fireTableDataChanged();
        }
    }
}
