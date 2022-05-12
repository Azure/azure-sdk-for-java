package com.azure.core.util;

public class MetricsOptions<T> {

    private final T metricProvider;
    private boolean isEnabled;
    private final String instrumentationScope;
    private final String instrumentationVersion;

    public MetricsOptions() {
        this(null);
    }

    public MetricsOptions(T metricProvider) {
        this.metricProvider = metricProvider;
        instrumentationScope = "todo";//getClass().getPackage();
        instrumentationVersion = "0.0.0";
    }

    public static MetricsOptions<?> fromConfiguration(Configuration configuration) {
        // PROPERTY_AZURE_METRICS_DISABLED
        return new MetricsOptions<>(null);
    }

    public void isMetricsEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public T getProviderImplementation() {
        return metricProvider;
    }

    public String getInstrumentationScope() {
        return instrumentationScope;
    }
    public String getInstrumentationVersion() {
        return instrumentationVersion;
    }
}
