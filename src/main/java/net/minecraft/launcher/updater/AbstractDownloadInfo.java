package net.minecraft.launcher.updater;

import java.net.URL;

public abstract class AbstractDownloadInfo
{
    abstract URL getUrl();
    
    abstract String getSha1();
    
    abstract int getSize();
}
