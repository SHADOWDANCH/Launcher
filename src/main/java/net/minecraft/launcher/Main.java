package net.minecraft.launcher;

import com.mojang.launcher.OperatingSystem;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

public class Main
{
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void main(final String[] args) {
        Main.LOGGER.debug("main() called!");
        startLauncher(args);
    }
    
    private static void startLauncher(final String[] args) {
        final OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("winTen");
        final OptionSpec<String> proxyHostOption = parser.accepts("proxyHost").withRequiredArg();
        final OptionSpec<Integer> proxyPortOption = parser.accepts("proxyPort")
                .withRequiredArg()
                .defaultsTo("8080")
                .ofType(Integer.class);
        final OptionSpec<File> workDirOption = parser.accepts("workDir")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(getWorkingDirectory());
        final OptionSpec<String> nonOption = parser.nonOptions();
        final OptionSet optionSet = parser.parse(args);
        final List<String> leftoverArgs = optionSet.valuesOf(nonOption);
        final String hostName = optionSet.valueOf(proxyHostOption);
        Proxy proxy = Proxy.NO_PROXY;
        if (hostName != null) {
            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(hostName, optionSet.valueOf(proxyPortOption)));
            }
            catch (Exception ignored) { }
        }
        final File workingDirectory = optionSet.valueOf(workDirOption);
        workingDirectory.mkdirs();
        Main.LOGGER.debug("About to create JFrame.");
        final Proxy finalProxy = proxy;
        final JFrame frame = new JFrame();
        frame.setTitle("Minecraft Launcher " + LauncherConstants.getVersionName() + LauncherConstants.PROPERTIES.getEnvironment().getTitle());
        frame.setPreferredSize(new Dimension(900, 580));
        try {
            final InputStream in = Launcher.class.getResourceAsStream("/favicon.png");
            if (in != null) {
                frame.setIconImage(ImageIO.read(in));
            }
        }
        catch (IOException ignored) { }
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        if (optionSet.has("winTen")) {
            System.setProperty("os.name", "Windows 10");
            System.setProperty("os.version", "10.0");
        }
        Main.LOGGER.debug("Starting up launcher.");
        final Launcher launcher = new Launcher(frame, workingDirectory, finalProxy, null, leftoverArgs.toArray(new String[leftoverArgs.size()]), LauncherConstants.SUPER_COOL_BOOTSTRAP_VERSION);
        if (optionSet.has("winTen")) {
            launcher.setWinTenHack();
        }
        frame.setLocationRelativeTo(null);
        Main.LOGGER.debug("End of main.");
    }
    
    public static File getWorkingDirectory() {
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (OperatingSystem.getCurrentPlatform()) {
            case LINUX: {
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            }
            case WINDOWS: {
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, ".minecraft/");
                break;
            }
            case OSX: {
                workingDirectory = new File(userHome, "Library/Application Support/minecraft");
                break;
            }
            default: {
                workingDirectory = new File(userHome, "minecraft/");
                break;
            }
        }
        return workingDirectory;
    }
}
