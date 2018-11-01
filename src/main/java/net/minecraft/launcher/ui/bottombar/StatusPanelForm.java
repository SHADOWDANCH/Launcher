package net.minecraft.launcher.ui.bottombar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.launcher.Http;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.Launcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class StatusPanelForm extends SidebarGridForm
{
    private static final Logger LOGGER;
    private static final String SERVER_SESSION = "session.minecraft.net";
    private static final String SERVER_LOGIN = "login.minecraft.net";
    private final Launcher minecraftLauncher;
    private final JLabel sessionStatus;
    private final JLabel loginStatus;
    private final Gson gson;
    
    public StatusPanelForm(final Launcher minecraftLauncher) {
        this.sessionStatus = new JLabel("???");
        this.loginStatus = new JLabel("???");
        this.gson = new GsonBuilder().registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).create();
        this.minecraftLauncher = minecraftLauncher;
        this.createInterface();
        this.refreshStatuses();
    }
    
    @Override
    protected void populateGrid(final GridBagConstraints constraints) {
        this.add(new JLabel("Multiplayer:", 2), constraints, 0, 0, 0, 1, 17);
        this.add(this.sessionStatus, constraints, 1, 0, 1, 1);
        this.add(new JLabel("Login:", 2), constraints, 0, 1, 0, 1, 17);
        this.add(this.loginStatus, constraints, 1, 1, 1, 1);
    }
    
    public JLabel getSessionStatus() {
        return this.sessionStatus;
    }
    
    public JLabel getLoginStatus() {
        return this.loginStatus;
    }
    
    public void refreshStatuses() {
        this.minecraftLauncher.getLauncher().getVersionManager().getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final TypeToken<List<Map<String, ServerStatus>>> token = new TypeToken<List<Map<String, ServerStatus>>>() {};
                    final List<Map<String, ServerStatus>> statuses = StatusPanelForm.this.gson.fromJson(Http.performGet(new URL("http://status.mojang.com/check"), StatusPanelForm.this.minecraftLauncher.getLauncher().getProxy()), token.getType());
                    for (final Map<String, ServerStatus> serverStatusInformation : statuses) {
                        if (serverStatusInformation.containsKey(SERVER_LOGIN)) {
                            StatusPanelForm.this.loginStatus.setText(serverStatusInformation.get(SERVER_LOGIN).title);
                        }
                        else {
                            if (!serverStatusInformation.containsKey(SERVER_SESSION)) {
                                continue;
                            }
                            StatusPanelForm.this.sessionStatus.setText(serverStatusInformation.get(SERVER_SESSION).title);
                        }
                    }
                }
                catch (Exception e) {
                    StatusPanelForm.LOGGER.error("Couldn't get server status", e);
                }
            }
        });
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
    
    public enum ServerStatus
    {
        GREEN("Online, no problems detected."), 
        YELLOW("May be experiencing issues."), 
        RED("Offline, experiencing problems.");
        
        private final String title;
        
        ServerStatus(final String title) {
            this.title = title;
        }
    }
}
