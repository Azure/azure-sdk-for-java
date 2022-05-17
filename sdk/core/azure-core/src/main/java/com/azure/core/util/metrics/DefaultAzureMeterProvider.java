package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

import java.util.Iterator;
import java.util.ServiceLoader;

class DefaultAzureMeterProvider implements AzureMeterProvider {
    public final static AzureMeterProvider INSTANCE = new DefaultAzureMeterProvider();
    private static final NoopMeter NOOP_METER = new NoopMeter();
    private static AzureMeterProvider meterProvider;
    private DefaultAzureMeterProvider() {

    }

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Meter classes.
        ServiceLoader<AzureMeterProvider> serviceLoader = ServiceLoader.load(AzureMeterProvider.class, AzureMeterProvider.class.getClassLoader());
        Iterator<?> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            meterProvider = serviceLoader.iterator().next();
        }
    }

    public AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        if (meterProvider != null && options.isEnabled()) {
            return meterProvider.createMeter(libraryName, libraryVersion, options);
        }

        return NOOP_METER;
    }
}
