// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Service Provider Interface (SPI) proxy for {@link AvroSerializerProvider}.
 */
public final class AvroSerializerProviders {
    private static boolean loadAttempted;
    private static AvroSerializerProvider defaultProvider;

    private static final IllegalStateException NO_AVRO_SERIALIZER_EXCEPTION =
        new IllegalStateException("No AvroSerializerProvider found on the classpath.");

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
     * @return A new {@link AvroSerializer} instance tied to the passed schema.
     */
    public static AvroSerializer createInstance(String schema) {
        if (defaultProvider == null) {
            load();
        }

        return defaultProvider.createInstance(schema);
    }

    /**
     * Returns the Avro schema for specified object.
     *
     * @param object The object having its Avro schema retrieved.
     * @return The Avro schema for the object.
     * @throws IllegalArgumentException If the object is an unsupported type.
     */
    public static String getSchema(Object object) {
        if (defaultProvider == null) {
            load();
        }

        return defaultProvider.getSchema(object);
    }

    /**
     * Returns the Avro schema for specified object.
     *
     * @param object The object having its Avro schema name retrieved.
     * @return The Avro schema name for the object.
     * @throws IllegalArgumentException If the object is an unsupported type.
     */
    public static String getSchemaName(Object object) {
        if (defaultProvider == null) {
            load();
        }

        return defaultProvider.getSchemaName(object);
    }
}
