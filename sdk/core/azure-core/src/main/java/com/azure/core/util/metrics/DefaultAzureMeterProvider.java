// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.MetricsOptions;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

final class DefaultAzureMeterProvider implements AzureMeterProvider {
    public static final AzureMeterProvider INSTANCE = new DefaultAzureMeterProvider();

    private static final AzureAttributeBuilder NOOP_ATTRIBUTES = new AzureAttributeBuilder() {
        @Override
        public AzureAttributeBuilder add(String key, String value) {
            return this;
        }

        @Override
        public AzureAttributeBuilder add(String key, long value) {
            return this;
        }

        @Override
        public AzureAttributeBuilder add(String key, double value) {
            return this;
        }

        @Override
        public AzureAttributeBuilder add(String key, boolean value) {
            return this;
        }
    };

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
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        if (meterProvider != null && (options == null || options.isEnabled())) {
            return meterProvider.createMeter(libraryName, libraryVersion, options);
        }

        return NoopMeter.INSTANCE;
    }

    @Override
    public AzureAttributeBuilder createAttributeBuilder() {
        if (meterProvider != null) {
            return meterProvider.createAttributeBuilder();
        }

        return NOOP_ATTRIBUTES;
    }
}
