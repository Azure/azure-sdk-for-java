// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class DefaultTracerProvider implements TracerProvider {
    private static final TracerProvider INSTANCE = new DefaultTracerProvider();
    private static final RuntimeException ERROR;
    private static final ClientLogger LOGGER = new ClientLogger(DefaultTracerProvider.class);
    private static TracerProvider tracerProvider;

    private DefaultTracerProvider() {
    }

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't/ depend on the
        // System classloader to load Meter classes.
        ServiceLoader<TracerProvider> serviceLoader = ServiceLoader.load(TracerProvider.class, TracerProvider.class.getClassLoader());
        Iterator<TracerProvider> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            tracerProvider = iterator.next();

            if (iterator.hasNext()) {
                String allProviders = StreamSupport.stream(serviceLoader.spliterator(), false)
                    .map(p -> p.getClass().getName())
                    .collect(Collectors.joining(", "));

                // TODO (lmolkova) add configuration to allow picking specific provider
                String message = String.format("Expected only one TracerProvider on the classpath, but found multiple providers: %s. "
                         + "Please pick one TracerProvider implementation and remove or exclude packages that bring other implementations", allProviders);

                ERROR = new IllegalStateException(message);
                LOGGER.error(message);
            } else {
                ERROR = null;
                LOGGER.info("Found TracerProvider implementation on the classpath: {}", tracerProvider.getClass().getName());
            }
        } else {
            ERROR = null;
        }
    }

    static TracerProvider getInstance() {
        if (ERROR != null) {
            throw LOGGER.logThrowableAsError(ERROR);
        }

        return INSTANCE;
    }

    public Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        if (tracerProvider != null && (options == null || options.isEnabled())) {
            return tracerProvider.createTracer(libraryName, libraryVersion, azNamespace, options);
        }

        return NoopTracer.INSTANCE;
    }
}
