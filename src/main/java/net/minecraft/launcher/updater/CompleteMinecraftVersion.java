package net.minecraft.launcher.updater;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.UserAuthentication;
import com.mojang.launcher.OperatingSystem;
import com.mojang.launcher.game.process.GameProcessBuilder;
import com.mojang.launcher.updater.VersionSyncInfo;
import com.mojang.launcher.updater.download.Downloadable;
import com.mojang.launcher.versions.CompleteVersion;
import com.mojang.launcher.versions.ReleaseType;
import net.minecraft.launcher.CompatibilityRule;
import net.minecraft.launcher.CurrentLaunchFeatureMatcher;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.profile.ProfileManager;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.*;

public class CompleteMinecraftVersion implements CompleteVersion
{
    private static final Logger LOGGER;
    private String inheritsFrom;
    private String id;
    private Date time;
    private Date releaseTime;
    private ReleaseType type;
    private String minecraftArguments;
    private List<Library> libraries;
    private String mainClass;
    private int minimumLauncherVersion;
    private String incompatibilityReason;
    private String assets;
    private List<CompatibilityRule> compatibilityRules;
    private String jar;
    private CompleteMinecraftVersion savableVersion;
    private transient boolean synced;
    private Map<DownloadType, DownloadInfo> downloads;
    private AssetIndexInfo assetIndex;
    private Map<ArgumentType, List<Argument>> arguments;
    
    public CompleteMinecraftVersion() {
        this.synced = false;
        this.downloads = Maps.newEnumMap(DownloadType.class);
    }
    
    public CompleteMinecraftVersion(final CompleteMinecraftVersion version) {
        this.synced = false;
        this.downloads = Maps.newEnumMap(DownloadType.class);
        this.inheritsFrom = version.inheritsFrom;
        this.id = version.id;
        this.time = version.time;
        this.releaseTime = version.releaseTime;
        this.type = version.type;
        this.minecraftArguments = version.minecraftArguments;
        this.mainClass = version.mainClass;
        this.minimumLauncherVersion = version.minimumLauncherVersion;
        this.incompatibilityReason = version.incompatibilityReason;
        this.assets = version.assets;
        this.jar = version.jar;
        this.downloads = version.downloads;
        if (version.libraries != null) {
            this.libraries = Lists.newArrayList();
            for (final Library library : version.getLibraries()) {
                this.libraries.add(new Library(library));
            }
        }
        if (version.arguments != null) {
            this.arguments = Maps.newEnumMap(ArgumentType.class);
            for (final Map.Entry<ArgumentType, List<Argument>> entry : version.arguments.entrySet()) {
                this.arguments.put(entry.getKey(), new ArrayList<Argument>(entry.getValue()));
            }
        }
        if (version.compatibilityRules != null) {
            this.compatibilityRules = Lists.newArrayList();
            for (final CompatibilityRule compatibilityRule : version.compatibilityRules) {
                this.compatibilityRules.add(new CompatibilityRule(compatibilityRule));
            }
        }
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    
    @Override
    public ReleaseType getType() {
        return this.type;
    }
    
    @Override
    public Date getUpdatedTime() {
        return this.time;
    }
    
    @Override
    public Date getReleaseTime() {
        return this.releaseTime;
    }
    
    public List<Library> getLibraries() {
        return this.libraries;
    }
    
    public String getMainClass() {
        return this.mainClass;
    }
    
    public String getJar() {
        return (this.jar == null) ? this.id : this.jar;
    }
    
    public void setType(final ReleaseType type) {
        if (type == null) {
            throw new IllegalArgumentException("Release type cannot be null");
        }
        this.type = type;
    }
    
    public Collection<Library> getRelevantLibraries(final CompatibilityRule.FeatureMatcher featureMatcher) {
        final List<Library> result = new ArrayList<Library>();
        for (final Library library : this.libraries) {
            if (library.appliesToCurrentEnvironment(featureMatcher)) {
                result.add(library);
            }
        }
        return result;
    }
    
    public Collection<File> getClassPath(final OperatingSystem os, final File base, final CompatibilityRule.FeatureMatcher featureMatcher) {
        final Collection<Library> libraries = this.getRelevantLibraries(featureMatcher);
        final Collection<File> result = new ArrayList<File>();
        for (final Library library : libraries) {
            if (library.getNatives() == null) {
                result.add(new File(base, "libraries/" + library.getArtifactPath()));
            }
        }
        result.add(new File(base, "versions/" + this.getJar() + "/" + this.getJar() + ".jar"));
        return result;
    }
    
    public Set<String> getRequiredFiles(final OperatingSystem os) {
        final Set<String> neededFiles = new HashSet<String>();
        for (final Library library : this.getRelevantLibraries(this.createFeatureMatcher())) {
            if (library.getNatives() != null) {
                final String natives = library.getNatives().get(os);
                if (natives == null) {
                    continue;
                }
                neededFiles.add("libraries/" + library.getArtifactPath(natives));
            }
            else {
                neededFiles.add("libraries/" + library.getArtifactPath());
            }
        }
        return neededFiles;
    }
    
    public Set<Downloadable> getRequiredDownloadables(final OperatingSystem os, final Proxy proxy, final File targetDirectory, final boolean ignoreLocalFiles) throws MalformedURLException {
        final Set<Downloadable> neededFiles = new HashSet<Downloadable>();
        for (final Library library : this.getRelevantLibraries(this.createFeatureMatcher())) {
            String file = null;
            String classifier = null;
            if (library.getNatives() != null) {
                classifier = library.getNatives().get(os);
                if (classifier != null) {
                    file = library.getArtifactPath(classifier);
                }
            }
            else {
                file = library.getArtifactPath();
            }
            if (file != null) {
                final File local = new File(targetDirectory, "libraries/" + file);
                final Downloadable download = library.createDownload(proxy, file, local, ignoreLocalFiles, classifier);
                if (download == null) {
                    continue;
                }
                neededFiles.add(download);
            }
        }
        return neededFiles;
    }
    
    @Override
    public String toString() {
        return "CompleteVersion{id='" + this.id + '\'' + ", updatedTime=" + this.time + ", releasedTime=" + this.time + ", type=" + this.type + ", libraries=" + this.libraries + ", mainClass='" + this.mainClass + '\'' + ", jar='" + this.jar + '\'' + ", minimumLauncherVersion=" + this.minimumLauncherVersion + '}';
    }
    
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }
    
    @Override
    public int getMinimumLauncherVersion() {
        return this.minimumLauncherVersion;
    }
    
    @Override
    public boolean appliesToCurrentEnvironment() {
        if (this.compatibilityRules == null) {
            return true;
        }
        CompatibilityRule.Action lastAction = CompatibilityRule.Action.DISALLOW;
        for (final CompatibilityRule compatibilityRule : this.compatibilityRules) {
            final ProfileManager profileManager = Launcher.getCurrentInstance().getProfileManager();
            final UserAuthentication auth = profileManager.getAuthDatabase().getByUUID(profileManager.getSelectedUser());
            final CompatibilityRule.Action action = compatibilityRule.getAppliedAction(new CurrentLaunchFeatureMatcher(profileManager.getSelectedProfile(), this, auth));
            if (action != null) {
                lastAction = action;
            }
        }
        return lastAction == CompatibilityRule.Action.ALLOW;
    }
    
    @Override
    public String getIncompatibilityReason() {
        return this.incompatibilityReason;
    }
    
    @Override
    public boolean isSynced() {
        return this.synced;
    }
    
    @Override
    public void setSynced(final boolean synced) {
        this.synced = synced;
    }
    
    public String getInheritsFrom() {
        return this.inheritsFrom;
    }
    
    public CompleteMinecraftVersion resolve(final MinecraftVersionManager versionManager) throws IOException {
        return this.resolve(versionManager, new HashSet<String>());
    }
    
    protected CompleteMinecraftVersion resolve(final MinecraftVersionManager versionManager, final Set<String> resolvedSoFar) throws IOException {
        if (this.inheritsFrom == null) {
            return this;
        }
        if (!resolvedSoFar.add(this.id)) {
            throw new IllegalStateException("Circular dependency detected");
        }
        final VersionSyncInfo parentSync = versionManager.getVersionSyncInfo(this.inheritsFrom);
        final CompleteMinecraftVersion parent = versionManager.getLatestCompleteVersion(parentSync).resolve(versionManager, resolvedSoFar);
        final CompleteMinecraftVersion result = new CompleteMinecraftVersion(parent);
        if (!parentSync.isInstalled() || !parentSync.isUpToDate() || parentSync.getLatestSource() != VersionSyncInfo.VersionSource.LOCAL) {
            versionManager.installVersion(parent);
        }
        result.savableVersion = this;
        result.inheritsFrom = null;
        result.id = this.id;
        result.time = this.time;
        result.releaseTime = this.releaseTime;
        result.type = this.type;
        if (this.minecraftArguments != null) {
            result.minecraftArguments = this.minecraftArguments;
        }
        if (this.mainClass != null) {
            result.mainClass = this.mainClass;
        }
        if (this.incompatibilityReason != null) {
            result.incompatibilityReason = this.incompatibilityReason;
        }
        if (this.assets != null) {
            result.assets = this.assets;
        }
        if (this.jar != null) {
            result.jar = this.jar;
        }
        if (this.libraries != null) {
            final List<Library> newLibraries = Lists.newArrayList();
            for (final Library library : this.libraries) {
                newLibraries.add(new Library(library));
            }
            for (final Library library : result.libraries) {
                newLibraries.add(library);
            }
            result.libraries = newLibraries;
        }
        if (this.arguments != null) {
            if (result.arguments == null) {
                result.arguments = new EnumMap<ArgumentType, List<Argument>>(ArgumentType.class);
            }
            for (final Map.Entry<ArgumentType, List<Argument>> entry : this.arguments.entrySet()) {
                List<Argument> arguments = result.arguments.get(entry.getKey());
                if (arguments == null) {
                    arguments = new ArrayList<Argument>();
                    result.arguments.put(entry.getKey(), arguments);
                }
                arguments.addAll(entry.getValue());
            }
        }
        if (this.compatibilityRules != null) {
            for (final CompatibilityRule compatibilityRule : this.compatibilityRules) {
                result.compatibilityRules.add(new CompatibilityRule(compatibilityRule));
            }
        }
        return result;
    }
    
    public CompleteMinecraftVersion getSavableVersion() {
        return Objects.firstNonNull(this.savableVersion, this);
    }
    
    public DownloadInfo getDownloadURL(final DownloadType type) {
        return this.downloads.get(type);
    }
    
    public AssetIndexInfo getAssetIndex() {
        if (this.assetIndex == null) {
            this.assetIndex = new AssetIndexInfo(Objects.firstNonNull(this.assets, "legacy"));
        }
        return this.assetIndex;
    }
    
    public CompatibilityRule.FeatureMatcher createFeatureMatcher() {
        final ProfileManager profileManager = Launcher.getCurrentInstance().getProfileManager();
        final UserAuthentication auth = profileManager.getAuthDatabase().getByUUID(profileManager.getSelectedUser());
        return new CurrentLaunchFeatureMatcher(profileManager.getSelectedProfile(), this, auth);
    }
    
    public void addArguments(final ArgumentType type, final CompatibilityRule.FeatureMatcher featureMatcher, final GameProcessBuilder builder, final StrSubstitutor substitutor) {
        if (this.arguments != null) {
            final List<Argument> args = this.arguments.get(type);
            if (args != null) {
                for (final Argument argument : args) {
                    argument.apply(builder, featureMatcher, substitutor);
                }
            }
        }
        else if (this.minecraftArguments != null) {
            if (type == ArgumentType.GAME) {
                for (final String arg : this.minecraftArguments.split(" ")) {
                    builder.withArguments(substitutor.replace(arg));
                }
                if (featureMatcher.hasFeature("is_demo_user", true)) {
                    builder.withArguments("--demo");
                }
                if (featureMatcher.hasFeature("has_custom_resolution", true)) {
                    builder.withArguments("--width", substitutor.replace("${resolution_width}"), "--height", substitutor.replace("${resolution_height}"));
                }
            }
            else if (type == ArgumentType.JVM) {
                if (OperatingSystem.getCurrentPlatform() == OperatingSystem.WINDOWS) {
                    builder.withArguments("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
                    if (Launcher.getCurrentInstance().usesWinTenHack()) {
                        builder.withArguments("-Dos.name=Windows 10", "-Dos.version=10.0");
                    }
                }
                else if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) {
                    builder.withArguments(substitutor.replace("-Xdock:icon=${asset=icons/minecraft.icns}"), "-Xdock:name=Minecraft");
                }
                builder.withArguments(substitutor.replace("-Djava.library.path=${natives_directory}"));
                builder.withArguments(substitutor.replace("-Dminecraft.launcher.brand=${launcher_name}"));
                builder.withArguments(substitutor.replace("-Dminecraft.launcher.version=${launcher_version}"));
                builder.withArguments(substitutor.replace("-Dminecraft.client.jar=${primary_jar}"));
                builder.withArguments("-cp", substitutor.replace("${classpath}"));
            }
        }
    }
    
    static {
        LOGGER = LogManager.getLogger();
    }
}
