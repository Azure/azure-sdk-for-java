package com.azure.core.serializer;

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

    public static ObjectSerializer createInstance(String schema) {
        if (defaultProvider == null) {
            load();
        }

        return defaultProvider.createInstance(schema);
    }
}
