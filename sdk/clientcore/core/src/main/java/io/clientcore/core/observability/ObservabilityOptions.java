package io.clientcore.core.observability;

public class ObservabilityOptions<T> {
    private boolean isTracingEnabled = true;
    private T provider = null;

    public ObservabilityOptions<T> setTracingEnabled(boolean isTracingEnabled) {
        this.isTracingEnabled = isTracingEnabled;
        return this;
    }

    public ObservabilityOptions<T> setProvider(T provider) {
        this.provider = provider;
        return this;
    }

    public boolean isTracingEnabled() {
        return isTracingEnabled;
    }

    public T getProvider() {
        return provider;
    }

    public ObservabilityOptions() {
    }

}
