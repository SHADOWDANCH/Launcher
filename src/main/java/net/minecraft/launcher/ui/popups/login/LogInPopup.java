package net.minecraft.launcher.ui.popups.login;

import com.mojang.launcher.OperatingSystem;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LogInPopup extends JPanel implements ActionListener
{
    private final Launcher minecraftLauncher;
    private final Callback callback;
    private final AuthErrorForm errorForm;
    private final ExistingUserListForm existingUserListForm;
    private final LogInForm logInForm;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JProgressBar progressBar;
    
    public LogInPopup(final Launcher minecraftLauncher, final Callback callback) {
        super(true);
        this.loginButton = new JButton("Log In");
        this.registerButton = new JButton("Register");
        this.progressBar = new JProgressBar();
        this.minecraftLauncher = minecraftLauncher;
        this.callback = callback;
        this.errorForm = new AuthErrorForm(this);
        this.existingUserListForm = new ExistingUserListForm(this);
        this.logInForm = new LogInForm(this);
        this.createInterface();
        this.loginButton.addActionListener(this);
        this.registerButton.addActionListener(this);
    }
    
    protected void createInterface() {
        this.setLayout(new BoxLayout(this, 1));
        this.setBorder(new EmptyBorder(5, 15, 5, 15));
        try {
            final InputStream stream = LogInPopup.class.getResourceAsStream("/minecraft_logo.png");
            if (stream != null) {
                final BufferedImage image = ImageIO.read(stream);
                final JLabel label = new JLabel(new ImageIcon(image));
                final JPanel imagePanel = new JPanel();
                imagePanel.add(label);
                this.add(imagePanel);
                this.add(Box.createVerticalStrut(10));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (!this.minecraftLauncher.getProfileManager().getAuthDatabase().getKnownNames().isEmpty()) {
            this.add(this.existingUserListForm);
        }
        this.add(this.errorForm);
        this.add(this.logInForm);
        this.add(Box.createVerticalStrut(15));
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(this.registerButton);
        buttonPanel.add(this.loginButton);
        this.add(buttonPanel);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setVisible(false);
        this.add(this.progressBar);
    }
    
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.loginButton) {
            this.logInForm.tryLogIn();
        }
        else if (e.getSource() == this.registerButton) {
            OperatingSystem.openLink(LauncherConstants.URL_REGISTER);
        }
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    public void setCanLogIn(final boolean enabled) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.loginButton.setEnabled(enabled);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setIndeterminate(true);
            this.progressBar.setVisible(!enabled);
            this.repack();
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    LogInPopup.this.setCanLogIn(enabled);
                }
            });
        }
    }
    
    public LogInForm getLogInForm() {
        return this.logInForm;
    }
    
    public AuthErrorForm getErrorForm() {
        return this.errorForm;
    }
    
    public ExistingUserListForm getExistingUserListForm() {
        return this.existingUserListForm;
    }
    
    public void setLoggedIn(final String uuid) {
        this.callback.onLogIn(uuid);
    }
    
    public void repack() {
        final Window window = SwingUtilities.windowForComponent(this);
        if (window != null) {
            window.pack();
        }
    }
    
    public interface Callback
    {
        void onLogIn(final String p0);
    }
}
