package com.azure.core.util;

public class MetricsOptions {

    private final static ConfigurationProperty<Boolean> IS_DISABLED_PROPERTY = ConfigurationPropertyBuilder.ofBoolean("metrics.disabled")
        .environmentVariableName(Configuration.PROPERTY_AZURE_METRICS_DISABLED)
        .shared(true)
        .defaultValue(false)
        .build();

    private Object provider;
    private boolean isEnabled;

    public MetricsOptions() {
        isEnabled = !Configuration.getGlobalConfiguration().get(IS_DISABLED_PROPERTY);
    }

    public static MetricsOptions fromConfiguration(Configuration configuration) {
        return new MetricsOptions()
            .enable(!configuration.get(IS_DISABLED_PROPERTY));
    }

    public Object getProvider() {
        return provider;
    }
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public MetricsOptions enable(boolean enabled) {
        this.isEnabled = enabled;
        return this;
    }

    public MetricsOptions setProvider(Object provider) {
        this.provider = provider;
        return this;
    }
}
