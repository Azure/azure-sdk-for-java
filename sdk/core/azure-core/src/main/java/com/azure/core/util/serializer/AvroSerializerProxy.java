// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Service Provider Interface (SPI) proxy for {@link AvroSerializerProvider}.
 */
public final class AvroSerializerProxy {
    private static boolean attemptedLoading;
    private static AvroSerializerProvider defaultProvider;
    private static final String CANNOT_FIND_AVRO_SERIALIZER = "Cannot find any AvroSerializerProvider on the classpath "
        + "- unable to create a default Avro ObjectSerializer instance";

    private AvroSerializerProxy() {
        // no-op
    }

    /*
     * Given that not all SDKs will be using an Avro serializer interface the classpath instances are loaded lazily.
     */
    private static synchronized void load() {
        if (attemptedLoading) {
            return;
        }

        attemptedLoading = true;
        Iterator<AvroSerializerProvider> it = ServiceLoader.load(AvroSerializerProvider.class).iterator();
        if (it.hasNext()) {
            defaultProvider = it.next();
        } else {
            throw new IllegalStateException(CANNOT_FIND_AVRO_SERIALIZER);
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
