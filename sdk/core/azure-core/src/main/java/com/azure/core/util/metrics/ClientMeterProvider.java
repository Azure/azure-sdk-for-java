package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class ClientMeterProvider {
    private static final NoopMeter NOOP_METER = new NoopMeter();
    private static ClientMeterProvider meterProvider;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Tracer classes.
        ServiceLoader<ClientMeterProvider> serviceLoader = ServiceLoader.load(ClientMeterProvider.class, ClientMeterProvider.class.getClassLoader());
        Iterator<?> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            meterProvider = serviceLoader.iterator().next();
        }
    }

    public static ClientMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options)  {
        if (meterProvider != null && options.isEnabled()) {
            return meterProvider.getMeter(libraryName, libraryVersion, options);
        }

        return NOOP_METER;
    }

    public abstract ClientMeter getMeter(String libraryName, String libraryVersion, MetricsOptions options);


    private static class NoopMeter implements ClientMeter {

        private static final ClientLongHistogram NOOP_LONG_HISTOGRAM = (value, context) -> { };
        private static final ClientLongCounter NOOP_LONG_COUNTER = (value, context) -> { };

        private NoopMeter() {
        }

        @Override
        public ClientLongHistogram getLongHistogram(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
            return NOOP_LONG_HISTOGRAM;
        }

        @Override
        public ClientLongCounter getLongCounter(String metricName, String metricDescription, String unit, Map<String, Object> attributes) {
            return NOOP_LONG_COUNTER;
        }
    }
}
