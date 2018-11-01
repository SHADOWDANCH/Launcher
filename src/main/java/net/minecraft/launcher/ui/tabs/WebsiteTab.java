package net.minecraft.launcher.ui.tabs;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.ui.tabs.website.Browser;
import net.minecraft.launcher.ui.tabs.website.JFXBrowser;
import net.minecraft.launcher.ui.tabs.website.LegacySwingBrowser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.IntrospectionException;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class WebsiteTab extends JPanel
{
    private static final Logger LOGGER;
    private final Browser browser;
    private final Launcher minecraftLauncher;
    
    public WebsiteTab(final Launcher minecraftLauncher) {
        this.browser = this.selectBrowser();
        this.minecraftLauncher = minecraftLauncher;
        this.setLayout(new BorderLayout());
        this.add(this.browser.getComponent(), "Center");
        this.browser.resize(this.getSize());
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                WebsiteTab.this.browser.resize(e.getComponent().getSize());
            }
        });
    }
    
    private Browser selectBrowser() {
        if (this.hasJFX()) {
            WebsiteTab.LOGGER.info("JFX is already initialized");
            return new JFXBrowser();
        }
        final File jfxrt = new File(System.getProperty("java.home"), "lib/jfxrt.jar");
        if (jfxrt.isFile()) {
            WebsiteTab.LOGGER.debug("Attempting to load {}...", jfxrt);
            try {
                addToSystemClassLoader(jfxrt);
                WebsiteTab.LOGGER.info("JFX has been detected & successfully loaded");
                return new JFXBrowser();
            }
            catch (Throwable e) {
                WebsiteTab.LOGGER.debug("JFX has been detected but unsuccessfully loaded", e);
                return new LegacySwingBrowser();
            }
        }
        WebsiteTab.LOGGER.debug("JFX was not found at {}", jfxrt);
        return new LegacySwingBrowser();
    }
    
    public void setPage(final String url) {
        this.browser.loadUrl(url);
    }
    
    public Launcher getMinecraftLauncher() {
        return this.minecraftLauncher;
    }
    
    public static void addToSystemClassLoader(final File file) throws IntrospectionException {
        if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
            final URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            try {
                final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.toURI().toURL());
            }
            catch (Throwable t) {
                WebsiteTab.LOGGER.warn("Couldn't add " + file + " to system classloader", t);
            }
        }
    }
    
    public boolean hasJFX() {
        try {
            this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
            return true;
        }
        catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
}
