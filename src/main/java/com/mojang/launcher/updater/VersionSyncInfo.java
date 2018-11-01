package com.mojang.launcher.updater;

import com.mojang.launcher.versions.Version;

public class VersionSyncInfo
{
    private final Version localVersion;
    private final Version remoteVersion;
    private final boolean isInstalled;
    private final boolean isUpToDate;
    
    public VersionSyncInfo(final Version localVersion, final Version remoteVersion, final boolean installed, final boolean upToDate) {
        this.localVersion = localVersion;
        this.remoteVersion = remoteVersion;
        this.isInstalled = installed;
        this.isUpToDate = upToDate;
    }
    
    public Version getLocalVersion() {
        return this.localVersion;
    }
    
    public Version getRemoteVersion() {
        return this.remoteVersion;
    }
    
    public Version getLatestVersion() {
        if (this.getLatestSource() == VersionSource.REMOTE) {
            return this.remoteVersion;
        }
        return this.localVersion;
    }
    
    public VersionSource getLatestSource() {
        if (this.getLocalVersion() == null) {
            return VersionSource.REMOTE;
        }
        if (this.getRemoteVersion() == null) {
            return VersionSource.LOCAL;
        }
        if (this.getRemoteVersion().getUpdatedTime().after(this.getLocalVersion().getUpdatedTime())) {
            return VersionSource.REMOTE;
        }
        return VersionSource.LOCAL;
    }
    
    public boolean isInstalled() {
        return this.isInstalled;
    }
    
    public boolean isOnRemote() {
        return this.remoteVersion != null;
    }
    
    public boolean isUpToDate() {
        return this.isUpToDate;
    }
    
    @Override
    public String toString() {
        return "VersionSyncInfo{localVersion=" + this.localVersion + ", remoteVersion=" + this.remoteVersion + ", isInstalled=" + this.isInstalled + ", isUpToDate=" + this.isUpToDate + '}';
    }
    
    public enum VersionSource
    {
        REMOTE, 
        LOCAL
    }
}
