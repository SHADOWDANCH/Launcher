package net.minecraft.launcher.updater;

import java.net.MalformedURLException;
import java.net.URL;

public class LocalDownloadInfo extends AbstractDownloadInfo
{
    protected String url;
    protected String sha1;
    protected int size;
    
    public LocalDownloadInfo() {
    }
    
    public LocalDownloadInfo(final LocalDownloadInfo other) {
        this.url = other.url;
        this.sha1 = other.sha1;
        this.size = other.size;
    }
    
    public URL getUrl() {
        try {
            return new URL(this.url);
        }
        catch (MalformedURLException e) {
            return null;
        }
    }
    
    public String getSha1() {
        return this.sha1;
    }
    
    public int getSize() {
        return this.size;
    }
}
