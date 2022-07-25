// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class DefaultMeterProvider implements MeterProvider {
    private static final MeterProvider INSTANCE = new DefaultMeterProvider();
    private static final RuntimeException ERROR;
    private static final ClientLogger LOGGER = new ClientLogger(DefaultMeterProvider.class);
    private static MeterProvider meterProvider;

    private DefaultMeterProvider() {
    }

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Meter classes.
        ServiceLoader<MeterProvider> serviceLoader = ServiceLoader.load(MeterProvider.class, MeterProvider.class.getClassLoader());
        Iterator<MeterProvider> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            meterProvider = iterator.next();

            if (iterator.hasNext()) {
                String allProviders = StreamSupport.stream(serviceLoader.spliterator(), false)
                    .map(p -> p.getClass().getName())
                    .collect(Collectors.joining(", "));

                // TODO (lmolkova) add configuration to allow picking specific provider
                String message = String.format("Expected only one MeterProvider on the classpath, but found multiple providers: %s. "
                         + "Please pick one MeterProvider implementation and remove or exclude packages that bring other implementations", allProviders);

                ERROR = new IllegalStateException(message);
                LOGGER.error(message);
            } else {
                ERROR = null;
                LOGGER.info("Found MeterProvider implementation on the classpath: {}", meterProvider.getClass().getName());
            }
        } else {
            ERROR = null;
        }
    }

    static MeterProvider getInstance() {
        if (ERROR != null) {
            throw LOGGER.logThrowableAsError(ERROR);
        }

        return INSTANCE;
    }

    public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        if (meterProvider != null && (options == null || options.isEnabled())) {
            return meterProvider.createMeter(libraryName, libraryVersion, options);
        }

        return NoopMeter.INSTANCE;
    }
}
