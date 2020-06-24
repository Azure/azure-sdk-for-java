// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Service Provider Interface (SPI) proxy for {@link AvroSerializerProvider}.
 */
public final class AvroSerializerProviders {
    private static boolean loadAttempted;
    private static AvroSerializerProvider defaultProvider;

    private static final IllegalStateException NO_AVRO_SERIALIZER_EXCEPTION =
        new IllegalStateException("No AvroSerializerProvicer found on the classpath.");

    private AvroSerializerProviders() {
        // no-op
    }

    /*
     * Given that not all SDKs will be using an Avro serializer interface the classpath instances are loaded lazily.
     */
    private static synchronized void load() {
        if (loadAttempted) {
            if (defaultProvider == null) {
                throw NO_AVRO_SERIALIZER_EXCEPTION;
            }

            return;
        }

        loadAttempted = true;
        Iterator<AvroSerializerProvider> it = ServiceLoader.load(AvroSerializerProvider.class).iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        } else {
            throw NO_AVRO_SERIALIZER_EXCEPTION;
        }
    }

    /**
     * Creates an Avro serializer instance based using the first {@link AvroSerializerProvider} found on the classpath.
     *
     * @param schema Schema tied to the Avro serializer for its lifetime.
     * @return A new Avro serializer instance tied to the passed schema.
     */
    public static ObjectSerializer createInstance(String schema) {
        if (defaultProvider == null) {
            load();
        }

        return defaultProvider.createInstance(schema);
    }
}
