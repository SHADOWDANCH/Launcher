package net.minecraft.launcher.ui;

import com.mojang.launcher.OperatingSystem;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class LauncherPanel extends JPanel
{
    public static final String CARD_DIRT_BACKGROUND = "loading";
    public static final String CARD_LOGIN = "login";
    public static final String CARD_LAUNCHER = "launcher";
    private final CardLayout cardLayout;
    private final LauncherTabPanel tabPanel;
    private final BottomBarPanel bottomBar;
    private final JProgressBar progressBar;
    private final Launcher minecraftLauncher;
    private final JPanel loginPanel;
    private JLabel warningLabel;
    
    public LauncherPanel(final Launcher minecraftLauncher) {
        this.minecraftLauncher = minecraftLauncher;
        this.setLayout(this.cardLayout = new CardLayout());
        this.progressBar = new JProgressBar();
        this.bottomBar = new BottomBarPanel(minecraftLauncher);
        this.tabPanel = new LauncherTabPanel(minecraftLauncher);
        this.loginPanel = new TexturedPanel("/dirt.png");
        this.createInterface();
    }
    
    protected void createInterface() {
        this.add(this.createLauncherInterface(), CARD_LAUNCHER);
        this.add(this.createDirtInterface(), CARD_DIRT_BACKGROUND);
        this.add(this.createLoginInterface(), CARD_LOGIN);
    }
    
    protected JPanel createLauncherInterface() {
        final JPanel result = new JPanel(new BorderLayout());
        this.tabPanel.getBlog().setPage(LauncherConstants.URL_BLOG);
        final boolean javaBootstrap = this.getMinecraftLauncher().getBootstrapVersion() < 100;
        boolean upgradableOS = OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS;
        if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) {
            final String ver = SystemUtils.OS_VERSION;
            if (ver != null && !ver.isEmpty()) {
                final String[] split = ver.split("\\.", 3);
                if (split.length >= 2) {
                    try {
                        final int major = Integer.parseInt(split[0]);
                        final int minor = Integer.parseInt(split[1]);
                        if (major == 10) {
                            upgradableOS = (minor >= 8);
                        }
                        else if (major > 10) {
                            upgradableOS = true;
                        }
                    }
                    catch (NumberFormatException ignored) { }
                }
            }
        }
        if (javaBootstrap && upgradableOS) {
            (this.warningLabel = new JLabel()).setForeground(Color.RED);
            this.warningLabel.setHorizontalAlignment(0);
            final URI url;
            if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
                url = LauncherConstants.URL_UPGRADE_WINDOWS;
            }
            else {
                url = LauncherConstants.URL_UPGRADE_OSX;
            }
            if (SystemUtils.IS_JAVA_1_8) {
                if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
                    this.warningLabel.setText("<html><p style='font-size: 1.1em'>You are running an old version of the launcher. Please consider <a href='" + url + "'>using the new launcher</a> which will improve the performance of both launcher and game.</p></html>");
                }
                else {
                    this.warningLabel.setText("<html><p style='font-size: 1em'>You are running an old version of the launcher. Please consider <a href='" + url + "'>using the new launcher</a> which will improve the performance of both launcher and game.</p></html>");
                }
            }
            else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
                this.warningLabel.setText("<html><p style='font-size: 1.1em'>You are running on an old version of Java. Please consider <a href='" + url + "'>using the new launcher</a> which doesn't require Java, as it will make your game faster.</p></html>");
            }
            else {
                this.warningLabel.setText("<html><p style='font-size: 1em'>You are running on an old version of Java. Please consider <a href='" + url + "'>using the new launcher</a> which doesn't require Java, as it will make your game faster.</p></html>");
            }
            result.add(this.warningLabel, "North");
            result.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    OperatingSystem.openLink(url);
                }
            });
        }
        final JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.add(this.tabPanel, "Center");
        center.add(this.progressBar, "South");
        this.progressBar.setVisible(false);
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(100);
        this.progressBar.setStringPainted(true);
        result.add(center, "Center");
        result.add(this.bottomBar, "South");
        return result;
    }
    
    protected JPanel createDirtInterface() {
        return new TexturedPanel("/dirt.png");
    }
    
    protected JPanel createLoginInterface() {
        this.loginPanel.setLayout(new GridBagLayout());
        return this.loginPanel;
    }
    
    public LauncherTabPanel getTabPanel() {
        return this.tabPanel;
    }
    
    public BottomBarPanel getBottomBar() {
        return this.bottomBar;
    }
    
    public JProgressBar getProgressBar() {
        return this.progressBar;
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    public void setCard(final String card, final JPanel additional) {
        if (card.equals(CARD_LOGIN)) {
            this.loginPanel.removeAll();
            this.loginPanel.add(additional);
        }
        this.cardLayout.show(this, card);
    }
}
