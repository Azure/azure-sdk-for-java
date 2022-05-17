package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public interface AzureMeterProvider {
    AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options);

    AzureMeterProvider DEFAULT_PROVIDER = new AzureMeterProvider() {
        private static final AzureMeterProvider.NoopMeter NOOP_METER = new AzureMeterProvider.NoopMeter();
        private static AzureMeterProvider meterProvider;

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
    };

    class NoopMeter implements AzureMeter {

        private static final AzureLongHistogram NOOP_LONG_HISTOGRAM = (value, context) -> {
        };
        private static final AzureLongCounter NOOP_LONG_COUNTER = (value, context) -> {
        };

        private NoopMeter() {
        }

        @Override
        public AzureLongHistogram createLongHistogram(String name, String metricDescription, String unit, Map<String, Object> attributes) {
            return NOOP_LONG_HISTOGRAM;
        }

        @Override
        public AzureLongCounter createLongCounter(String name, String metricDescription, String unit, Map<String, Object> attributes) {
            return NOOP_LONG_COUNTER;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
