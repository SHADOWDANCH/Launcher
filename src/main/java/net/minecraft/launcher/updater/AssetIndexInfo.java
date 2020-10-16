package net.minecraft.launcher.updater;

import net.minecraft.launcher.LauncherConstants;

public class AssetIndexInfo extends DownloadInfo
{
    protected long totalSize;
    protected String id;
    protected boolean known;
    
    public AssetIndexInfo() {
        this.known = true;
    }
    
    public AssetIndexInfo(final String id) {
        this.known = true;
        this.id = id;
        this.url = LauncherConstants.constantURL(LauncherConstants.URL_JAR_FALLBACK + "indexes/" + id + ".json");
        this.known = false;
    }
    
    public long getTotalSize() {
        return this.totalSize;
    }
    
    public String getId() {
        return this.id;
    }
    
    public boolean sizeAndHashKnown() {
        return this.known;
    }
}
