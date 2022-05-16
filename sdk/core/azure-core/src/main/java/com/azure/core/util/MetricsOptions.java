package com.azure.core.util;

public class MetricsOptions {

    private Object implConfiguration;
    private boolean isEnabled = true;

    public MetricsOptions() {
    }

    public Object getImplementationConfiguration() {
        return implConfiguration;
    }
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public MetricsOptions enable(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public MetricsOptions setImplementationConfiguration(Object implConfiguration) {
        this.implConfiguration = implConfiguration;
        return this;
    }
}
