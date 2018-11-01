package net.minecraft.launcher.ui.tabs.website;

import com.mojang.launcher.OperatingSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;

public class LegacySwingBrowser implements Browser
{
    private static final Logger LOGGER;
    private final JScrollPane scrollPane;
    private final JTextPane browser;
    
    public LegacySwingBrowser() {
        this.scrollPane = new JScrollPane();
        (this.browser = new JTextPane()).setEditable(false);
        this.browser.setMargin(null);
        this.browser.setBackground(Color.DARK_GRAY);
        this.browser.setContentType("text/html");
        this.browser.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Loading page..</h1></center></font></body></html>");
        this.browser.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(final HyperlinkEvent he) {
                if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        OperatingSystem.openLink(he.getURL().toURI());
                    }
                    catch (Exception e) {
                        LegacySwingBrowser.LOGGER.error("Unexpected exception opening link " + he.getURL(), e);
                    }
                }
            }
        });
        this.scrollPane.setViewportView(this.browser);
    }
    
    @Override
    public void loadUrl(final String url) {
        final Thread thread = new Thread("Update website tab") {
            @Override
            public void run() {
                try {
                    LegacySwingBrowser.this.browser.setPage(new URL(url));
                }
                catch (Exception e) {
                    LegacySwingBrowser.LOGGER.error("Unexpected exception loading " + url, e);
                    LegacySwingBrowser.this.browser.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Failed to get page</h1><br>" + e.toString() + "</center></font></body></html>");
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public Component getComponent() {
        return this.scrollPane;
    }
    
    @Override
    public void resize(final Dimension size) {
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
}
