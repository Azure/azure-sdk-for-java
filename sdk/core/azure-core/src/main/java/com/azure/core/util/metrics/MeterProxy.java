package com.azure.core.util.metrics;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class MeterProxy {

    private static MeterProvider meterProvider;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Tracer classes.
        ServiceLoader<MeterProvider> serviceLoader = ServiceLoader.load(MeterProvider.class, MeterProxy.class.getClassLoader());
        Iterator<?> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            meterProvider = serviceLoader.iterator().next();
        }
    }

    public static <T> DoubleHistogram getDoubleHistogram(String name, String description, String units, MetricsOptions<T> options) {
        if (meterProvider == null) {
            return NoopDoubleHistogram.INSTANCE;
        }

        return meterProvider.getDoubleHistogram(name, description, units, options);
    }

    private static class NoopDoubleHistogram implements DoubleHistogram {

        public static final NoopDoubleHistogram INSTANCE = new NoopDoubleHistogram();

        private NoopDoubleHistogram() {

        }

        @Override
        public void record(double value, Map<String, Object> attributes, Context context) {

        }
    }
}
