package com.mojang.launcher.game.process;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.mojang.launcher.OperatingSystem;
import com.mojang.launcher.events.GameOutputLogProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameProcessBuilder
{
    private final String processPath;
    private final List<String> arguments;
    private Predicate<String> sysOutFilter;
    private GameOutputLogProcessor logProcessor;
    private File directory;
    
    public GameProcessBuilder(String processPath) {
        this.arguments = Lists.newArrayList();
        this.sysOutFilter = Predicates.alwaysTrue();
        this.logProcessor = new GameOutputLogProcessor() {
            @Override
            public void onGameOutput(final GameProcess process, final String logLine) {
            }
        };
        if (processPath == null) {
            processPath = OperatingSystem.getCurrentPlatform().getJavaDir();
        }
        this.processPath = processPath;
    }
    
    public List<String> getFullCommands() {
        final List<String> result = new ArrayList<String>(this.arguments);
        result.add(0, this.getProcessPath());
        return result;
    }
    
    public GameProcessBuilder withArguments(final String... commands) {
        this.arguments.addAll(Arrays.asList(commands));
        return this;
    }
    
    public List<String> getArguments() {
        return this.arguments;
    }
    
    public GameProcessBuilder directory(final File directory) {
        this.directory = directory;
        return this;
    }
    
    public File getDirectory() {
        return this.directory;
    }
    
    public GameProcessBuilder withSysOutFilter(final Predicate<String> predicate) {
        this.sysOutFilter = predicate;
        return this;
    }
    
    public GameProcessBuilder withLogProcessor(final GameOutputLogProcessor logProcessor) {
        this.logProcessor = logProcessor;
        return this;
    }
    
    public Predicate<String> getSysOutFilter() {
        return this.sysOutFilter;
    }
    
    protected String getProcessPath() {
        return this.processPath;
    }
    
    public GameOutputLogProcessor getLogProcessor() {
        return this.logProcessor;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("processPath", this.processPath).add("arguments", this.arguments).add("sysOutFilter", this.sysOutFilter).add("directory", this.directory).add("logProcessor", this.logProcessor).toString();
    }
}
