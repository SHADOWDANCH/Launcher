package net.minecraft.launcher.updater;

import com.google.gson.*;
import com.mojang.launcher.game.process.GameProcessBuilder;
import net.minecraft.launcher.CompatibilityRule;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Argument
{
    private final String[] value;
    private final List<CompatibilityRule> compatibilityRules;
    
    public Argument(final String[] values, final List<CompatibilityRule> compatibilityRules) {
        this.value = values;
        this.compatibilityRules = compatibilityRules;
    }
    
    public void apply(final GameProcessBuilder output, final CompatibilityRule.FeatureMatcher featureMatcher, final StrSubstitutor substitutor) {
        if (this.appliesToCurrentEnvironment(featureMatcher)) {
            for (int i = 0; i < this.value.length; ++i) {
                output.withArguments(substitutor.replace(this.value[i]));
            }
        }
    }
    
    public boolean appliesToCurrentEnvironment(final CompatibilityRule.FeatureMatcher featureMatcher) {
        if (this.compatibilityRules == null) {
            return true;
        }
        CompatibilityRule.Action lastAction = CompatibilityRule.Action.DISALLOW;
        for (final CompatibilityRule compatibilityRule : this.compatibilityRules) {
            final CompatibilityRule.Action action = compatibilityRule.getAppliedAction(featureMatcher);
            if (action != null) {
                lastAction = action;
            }
        }
        return lastAction == CompatibilityRule.Action.ALLOW;
    }
    
    public static class Serializer implements JsonDeserializer<Argument>
    {
        @Override
        public Argument deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Argument(new String[] { json.getAsString() }, null);
            }
            if (json.isJsonObject()) {
                final JsonObject obj = json.getAsJsonObject();
                final JsonElement value = obj.get("value");
                String[] values;
                if (value.isJsonPrimitive()) {
                    values = new String[] { value.getAsString() };
                }
                else {
                    final JsonArray array = value.getAsJsonArray();
                    values = new String[array.size()];
                    for (int i = 0; i < array.size(); ++i) {
                        values[i] = array.get(i).getAsString();
                    }
                }
                final List<CompatibilityRule> rules = new ArrayList<CompatibilityRule>();
                if (obj.has("rules")) {
                    final JsonArray array2 = obj.getAsJsonArray("rules");
                    for (final JsonElement element : array2) {
                        rules.add((CompatibilityRule) context.deserialize(element, CompatibilityRule.class));
                    }
                }
                return new Argument(values, rules);
            }
            throw new JsonParseException("Invalid argument, must be object or string");
        }
    }
}
