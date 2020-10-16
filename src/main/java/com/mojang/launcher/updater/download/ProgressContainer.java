package com.mojang.launcher.updater.download;

public class ProgressContainer
{
    private long total;
    private long current;
    private DownloadJob job;
    
    public DownloadJob getJob() {
        return this.job;
    }
    
    public void setJob(final DownloadJob job) {
        this.job = job;
        if (job != null) {
            job.updateProgress();
        }
    }
    
    public long getTotal() {
        return this.total;
    }
    
    public void setTotal(final long total) {
        this.total = total;
        if (this.job != null) {
            this.job.updateProgress();
        }
    }
    
    public long getCurrent() {
        return this.current;
    }
    
    public void setCurrent(final long current) {
        this.current = current;
        if (current > this.total) {
            this.total = current;
        }
        if (this.job != null) {
            this.job.updateProgress();
        }
    }
    
    public void addProgress(final long amount) {
        this.setCurrent(this.getCurrent() + amount);
    }
    
    public float getProgress() {
        if (this.total == 0L) {
            return 0.0f;
        }
        return this.current / (float) this.total;
    }
    
    @Override
    public String toString() {
        return "ProgressContainer{current=" + this.current + ", total=" + this.total + '}';
    }
}
