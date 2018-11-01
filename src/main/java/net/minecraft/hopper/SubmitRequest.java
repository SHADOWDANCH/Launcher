package net.minecraft.hopper;

import java.util.Map;

public class SubmitRequest
{
    private String report;
    private String version;
    private String product;
    private Map<String, String> environment;
    
    public SubmitRequest(final String report, final String product, final String version, final Map<String, String> environment) {
        this.report = report;
        this.version = version;
        this.product = product;
        this.environment = environment;
    }
}
