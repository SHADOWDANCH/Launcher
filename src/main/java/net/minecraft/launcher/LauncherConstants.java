package net.minecraft.launcher;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class LauncherConstants
{
    public static final int VERSION_FORMAT = 21;
    public static final int PROFILES_FORMAT = 1;
    public static final URI URL_REGISTER = constantURI("https://account.mojang.com/register");
    public static final String URL_JAR_FALLBACK = "https://s3.amazonaws.com/Minecraft.Download/";
    public static final String URL_RESOURCE_BASE = "http://resources.download.minecraft.net/";
    public static final String URL_LIBRARY_BASE = "https://libraries.minecraft.net/";
    public static final String URL_BLOG = "http://mcupdate.tumblr.com";
    public static final String URL_SUPPORT = "http://help.mojang.com/?ref=launcher";
    public static final String URL_STATUS_CHECKER = "http://status.mojang.com/check";
    public static final int UNVERSIONED_BOOTSTRAP_VERSION = 0;
    public static final int MINIMUM_BOOTSTRAP_SUPPORTED = 4;
    public static final int SUPER_COOL_BOOTSTRAP_VERSION = 100;
    public static final String URL_BOOTSTRAP_DOWNLOAD = "https://mojang.com/2013/06/minecraft-1-6-pre-release/";
    public static final String[] BOOTSTRAP_OUT_OF_DATE_BUTTONS = new String[] { "Go to URL", "Close" };
    public static final String LAUNCHER_OUT_OF_DATE_MESSAGE = "It looks like you've used a newer launcher than this one! If you go back to using this one, we will need to reset your configuration.";
    public static final String[] LAUNCHER_OUT_OF_DATE_BUTTONS = new String[] { "Nevermind, close this launcher", "I'm sure. Reset my settings." };
    public static final String LAUNCHER_NOT_NATIVE_MESSAGE = "This shortcut to the launcher is out of date. Please delete it and remake it to the new launcher, which we will start for you now.";
    public static final String[] CONFIRM_PROFILE_DELETION_OPTIONS = new String[] { "Delete profile", "Cancel" };
    public static final URI URL_FORGOT_USERNAME = constantURI("http://help.mojang.com/customer/portal/articles/1233873?ref=launcher");
    public static final URI URL_FORGOT_PASSWORD_MINECRAFT = constantURI("http://help.mojang.com/customer/portal/articles/329524-change-or-forgot-password?ref=launcher");
    public static final URI URL_FORGOT_MIGRATED_EMAIL = constantURI("http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account?ref=launcher");
    public static final URI URL_DEMO_HELP = constantURI("https://help.mojang.com/customer/portal/articles/1218766-can-only-play-minecraft-demo?ref=launcher");
    public static final URI URL_UPGRADE_WINDOWS = constantURI("https://launcher.mojang.com/download/MinecraftInstaller.msi");
    public static final URI URL_UPGRADE_OSX = constantURI("https://launcher.mojang.com/download/Minecraft.dmg");
    public static final int MAX_NATIVES_LIFE_IN_SECONDS = 3600;
    public static final int MAX_SKIN_LIFE_IN_SECONDS = 604800;
    public static final LauncherProperties PROPERTIES = getProperties();
    
    public static URI constantURI(final String input) {
        try {
            return new URI(input);
        }
        catch (URISyntaxException e) {
            throw new Error(e);
        }
    }
    
    public static URL constantURL(final String input) {
        try {
            return new URL(input);
        }
        catch (MalformedURLException e) {
            throw new Error(e);
        }
    }
    
    public static String getVersionName() {
        return Objects.firstNonNull(LauncherConstants.class.getPackage().getImplementationVersion(), "unknown");
    }
    
    private static LauncherProperties getProperties() {
        final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).create();
        final InputStream stream = LauncherConstants.class.getResourceAsStream("/launcher_properties.json");
        if (stream != null) {
            try {
                return gson.fromJson(IOUtils.toString(stream), LauncherProperties.class);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return new LauncherProperties();
    }

    public enum LauncherEnvironment
    {
        PRODUCTION(""), 
        STAGING(" (STAGING VERSION, NOT FINAL)"), 
        DEV(" (DEV VERSION, NOT FINAL)");
        
        private final String title;
        
        LauncherEnvironment(final String title) {
            this.title = title;
        }
        
        public String getTitle() {
            return this.title;
        }
    }
    
    public static class LauncherProperties
    {
        private final LauncherEnvironment environment;
        private final URL versionManifest;
        
        public LauncherProperties() {
            this.environment = LauncherEnvironment.PRODUCTION;
            this.versionManifest = LauncherConstants.constantURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
        }
        
        public LauncherEnvironment getEnvironment() {
            return this.environment;
        }
        
        public URL getVersionManifest() {
            return this.versionManifest;
        }
    }
}
