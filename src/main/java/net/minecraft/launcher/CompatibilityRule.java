package net.minecraft.launcher;

import com.mojang.launcher.OperatingSystem;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatibilityRule
{
    private Action action;
    private OSRestriction os;
    private Map<String, Object> features;
    
    public CompatibilityRule() {
        this.action = Action.ALLOW;
    }
    
    public CompatibilityRule(final CompatibilityRule compatibilityRule) {
        this.action = Action.ALLOW;
        this.action = compatibilityRule.action;
        if (compatibilityRule.os != null) {
            this.os = new OSRestriction(compatibilityRule.os);
        }
        if (compatibilityRule.features != null) {
            this.features = compatibilityRule.features;
        }
    }
    
    public Action getAppliedAction(final FeatureMatcher featureMatcher) {
        if (this.os != null && !this.os.isCurrentOperatingSystem()) {
            return null;
        }
        if (this.features != null) {
            if (featureMatcher == null) {
                return null;
            }
            for (final Map.Entry<String, Object> feature : this.features.entrySet()) {
                if (!featureMatcher.hasFeature(feature.getKey(), feature.getValue())) {
                    return null;
                }
            }
        }
        return this.action;
    }
    
    public Action getAction() {
        return this.action;
    }
    
    public OSRestriction getOs() {
        return this.os;
    }
    
    public Map<String, Object> getFeatures() {
        return this.features;
    }
    
    @Override
    public String toString() {
        return "Rule{action=" + this.action + ", os=" + this.os + ", features=" + this.features + '}';
    }
    
    public enum Action
    {
        ALLOW, 
        DISALLOW
    }
    
    public class OSRestriction
    {
        private OperatingSystem name;
        private String version;
        private String arch;
        
        public OSRestriction() {
        }
        
        public OperatingSystem getName() {
            return this.name;
        }
        
        public String getVersion() {
            return this.version;
        }
        
        public String getArch() {
            return this.arch;
        }
        
        public OSRestriction(final OSRestriction osRestriction) {
            this.name = osRestriction.name;
            this.version = osRestriction.version;
            this.arch = osRestriction.arch;
        }
        
        public boolean isCurrentOperatingSystem() {
            if (this.name != null && this.name != OperatingSystem.getCurrentPlatform()) {
                return false;
            }
            if (this.version != null) {
                try {
                    final Pattern pattern = Pattern.compile(this.version);
                    final Matcher matcher = pattern.matcher(System.getProperty("os.version"));
                    if (!matcher.matches()) {
                        return false;
                    }
                }
                catch (Throwable t) {}
            }
            if (this.arch != null) {
                try {
                    final Pattern pattern = Pattern.compile(this.arch);
                    final Matcher matcher = pattern.matcher(System.getProperty("os.arch"));
                    if (!matcher.matches()) {
                        return false;
                    }
                }
                catch (Throwable t2) {}
            }
            return true;
        }
        
        @Override
        public String toString() {
            return "OSRestriction{name=" + this.name + ", version='" + this.version + '\'' + ", arch='" + this.arch + '\'' + '}';
        }
    }
    
    public interface FeatureMatcher
    {
        boolean hasFeature(final String p0, final Object p1);
    }
}
