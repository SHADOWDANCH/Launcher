package net.minecraft.launcher;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.mojang.authlib.UserAuthentication;
import com.mojang.launcher.OperatingSystem;
import com.mojang.launcher.events.GameOutputLogProcessor;
import com.mojang.launcher.updater.DownloadProgress;
import com.mojang.launcher.versions.CompleteVersion;
import net.minecraft.launcher.game.MinecraftGameRunner;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.LauncherPanel;
import net.minecraft.launcher.ui.popups.login.LogInPopup;
import net.minecraft.launcher.ui.tabs.CrashReportTab;
import net.minecraft.launcher.ui.tabs.GameOutputTab;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class SwingUserInterface implements MinecraftUserInterface
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final long MAX_SHUTDOWN_TIME = 10000L;
    private final Launcher minecraftLauncher;
    private LauncherPanel launcherPanel;
    private final JFrame frame;
    
    public SwingUserInterface(final Launcher minecraftLauncher, final JFrame frame) {
        this.minecraftLauncher = minecraftLauncher;
        this.frame = frame;
        setLookAndFeel();
    }
    
    private static void setLookAndFeel() {
        final JFrame frame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Throwable ignored) {
            try {
                SwingUserInterface.LOGGER.error("Your java failed to provide normal look and feel, trying the old fallback now");
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch (Throwable t) {
                SwingUserInterface.LOGGER.error("Unexpected exception setting look and feel", t);
            }
        }
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("test"));
        frame.add(panel);
        try {
            frame.pack();
        }
        catch (Throwable ignored2) {
            SwingUserInterface.LOGGER.error("Custom (broken) theme detected, falling back onto x-platform theme");
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            catch (Throwable ex) {
                SwingUserInterface.LOGGER.error("Unexpected exception setting look and feel", ex);
            }
        }
        frame.dispose();
    }
    
    public void showLoginPrompt(final Launcher minecraftLauncher, final LogInPopup.Callback callback) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final LogInPopup popup = new LogInPopup(minecraftLauncher, callback);
                SwingUserInterface.this.launcherPanel.setCard("login", popup);
            }
        });
    }
    
    public void initializeFrame() {
        this.frame.getContentPane().removeAll();
        this.frame.setTitle("Minecraft Launcher " + LauncherConstants.getVersionName() + LauncherConstants.PROPERTIES.getEnvironment().getTitle());
        this.frame.setPreferredSize(new Dimension(900, 580));
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                SwingUserInterface.LOGGER.info("Window closed, shutting down.");
                SwingUserInterface.this.frame.setVisible(false);
                SwingUserInterface.this.frame.dispose();
                SwingUserInterface.LOGGER.info("Halting executors");
                SwingUserInterface.this.minecraftLauncher.getLauncher().getVersionManager().getExecutorService().shutdown();
                SwingUserInterface.LOGGER.info("Awaiting termination.");
                try {
                    SwingUserInterface.this.minecraftLauncher.getLauncher().getVersionManager().getExecutorService().awaitTermination(10L, TimeUnit.SECONDS);
                }
                catch (InterruptedException e2) {
                    SwingUserInterface.LOGGER.info("Termination took too long.");
                }
                SwingUserInterface.LOGGER.info("Goodbye.");
                SwingUserInterface.this.forcefullyShutdown();
            }
        });
        try {
            final InputStream in = Launcher.class.getResourceAsStream("/favicon.png");
            if (in != null) {
                this.frame.setIconImage(ImageIO.read(in));
            }
        }
        catch (IOException ignored) { }
        this.launcherPanel = new LauncherPanel(this.minecraftLauncher);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.frame.add(SwingUserInterface.this.launcherPanel);
                SwingUserInterface.this.frame.pack();
                SwingUserInterface.this.frame.setVisible(true);
                SwingUserInterface.this.frame.setAlwaysOnTop(true);
                SwingUserInterface.this.frame.setAlwaysOnTop(false);
            }
        });
    }
    
    private void forcefullyShutdown() {
        try {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Runtime.getRuntime().halt(0);
                }
            }, MAX_SHUTDOWN_TIME);
            System.exit(0);
        }
        catch (Throwable ignored) {
            Runtime.getRuntime().halt(0);
        }
    }
    
    @Override
    public void showOutdatedNotice() {
        final String error = "Sorry, but your launcher is outdated! Please redownload it at https://mojang.com/2013/06/minecraft-1-6-pre-release/";
        this.frame.getContentPane().removeAll();
        final int result = JOptionPane.showOptionDialog(this.frame, error, "Outdated launcher", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, LauncherConstants.BOOTSTRAP_OUT_OF_DATE_BUTTONS, LauncherConstants.BOOTSTRAP_OUT_OF_DATE_BUTTONS[0]);
        if (result == 0) {
            try {
                OperatingSystem.openLink(new URI(LauncherConstants.URL_BOOTSTRAP_DOWNLOAD));
            }
            catch (URISyntaxException e) {
                SwingUserInterface.LOGGER.error("Couldn't open bootstrap download link. Please visit https://mojang.com/2013/06/minecraft-1-6-pre-release/ manually.", e);
            }
        }
        this.minecraftLauncher.getLauncher().shutdownLauncher();
    }
    
    @Override
    public void showLoginPrompt() {
        final ProfileManager profileManager = this.minecraftLauncher.getProfileManager();
        try {
            profileManager.saveProfiles();
        }
        catch (IOException e) {
            SwingUserInterface.LOGGER.error("Couldn't save profiles before logging in!", e);
        }
        final Profile selectedProfile = profileManager.getSelectedProfile();
        this.showLoginPrompt(this.minecraftLauncher, new LogInPopup.Callback() {
            @Override
            public void onLogIn(final String uuid) {
                final UserAuthentication auth = profileManager.getAuthDatabase().getByUUID(uuid);
                profileManager.setSelectedUser(uuid);
                if (selectedProfile.getName().equals("(Default)") && auth.getSelectedProfile() != null) {
                    final String playerName = auth.getSelectedProfile().getName();
                    String profileName = auth.getSelectedProfile().getName();
                    for (int count = 1; profileManager.getProfiles().containsKey(profileName); profileName = playerName + " " + ++count) {}
                    final Profile newProfile = new Profile(selectedProfile);
                    newProfile.setName(profileName);
                    profileManager.getProfiles().put(profileName, newProfile);
                    profileManager.getProfiles().remove("(Default)");
                    profileManager.setSelectedProfile(profileName);
                }
                try {
                    profileManager.saveProfiles();
                }
                catch (IOException e) {
                    SwingUserInterface.LOGGER.error("Couldn't save profiles after logging in!", e);
                }
                if (uuid == null) {
                    SwingUserInterface.this.minecraftLauncher.getLauncher().shutdownLauncher();
                }
                else {
                    profileManager.fireRefreshEvent();
                }
                SwingUserInterface.this.launcherPanel.setCard("launcher", null);
            }
        });
    }
    
    @Override
    public void setVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.frame.setVisible(visible);
            }
        });
    }
    
    @Override
    public void shutdownLauncher() {
        if (SwingUtilities.isEventDispatchThread()) {
            SwingUserInterface.LOGGER.info("Requesting window close");
            this.frame.dispatchEvent(new WindowEvent(this.frame, 201));
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    SwingUserInterface.this.shutdownLauncher();
                }
            });
        }
    }
    
    @Override
    public void setDownloadProgress(final DownloadProgress downloadProgress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.launcherPanel.getProgressBar().setVisible(true);
                SwingUserInterface.this.launcherPanel.getProgressBar().setValue((int)(downloadProgress.getPercent() * 100.0f));
                SwingUserInterface.this.launcherPanel.getProgressBar().setString(downloadProgress.getStatus());
            }
        });
    }
    
    @Override
    public void hideDownloadProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.launcherPanel.getProgressBar().setVisible(false);
            }
        });
    }
    
    @Override
    public void showCrashReport(final CompleteVersion version, final File crashReportFile, final String crashReport) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.launcherPanel.getTabPanel().setCrashReport(new CrashReportTab(SwingUserInterface.this.minecraftLauncher, version, crashReportFile, crashReport));
            }
        });
    }
    
    @Override
    public void gameLaunchFailure(final String reason) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SwingUserInterface.this.frame, reason, "Cannot play game", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    @Override
    public void updatePlayState() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUserInterface.this.launcherPanel.getBottomBar().getPlayButtonPanel().checkState();
            }
        });
    }
    
    @Override
    public GameOutputLogProcessor showGameOutputTab(final MinecraftGameRunner gameRunner) {
        final SettableFuture<GameOutputLogProcessor> future = SettableFuture.create();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final GameOutputTab tab = new GameOutputTab(SwingUserInterface.this.minecraftLauncher);
                future.set(tab);
                final UserAuthentication auth = gameRunner.getAuth();
                final String name = (auth.getSelectedProfile() == null) ? "Demo" : auth.getSelectedProfile().getName();
                SwingUserInterface.this.launcherPanel.getTabPanel().removeTab("Game Output (" + name + ")");
                SwingUserInterface.this.launcherPanel.getTabPanel().addTab("Game Output (" + name + ")", tab);
                SwingUserInterface.this.launcherPanel.getTabPanel().setSelectedComponent(tab);
            }
        });
        return Futures.getUnchecked(future);
    }
    
    @Override
    public boolean shouldDowngradeProfiles() {
        final int result = JOptionPane.showOptionDialog(
                this.frame,
                LauncherConstants.LAUNCHER_OUT_OF_DATE_MESSAGE,
                "Outdated launcher",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                LauncherConstants.LAUNCHER_OUT_OF_DATE_BUTTONS,
                LauncherConstants.LAUNCHER_OUT_OF_DATE_BUTTONS[0]
        );
        return result == 1;
    }
    
    @Override
    public String getTitle() {
        return "Minecraft Launcher " + LauncherConstants.getVersionName();
    }
    
    public JFrame getFrame() {
        return this.frame;
    }
}
