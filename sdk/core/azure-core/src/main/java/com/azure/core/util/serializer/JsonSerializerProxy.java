// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class is a proxy for using a {@link JsonSerializerProvider} loaded from the classpath.
 */
public final class JsonSerializerProxy {
    private static final String CANNOT_FIND_JSON_SERIALIZER_PROVIDER =
        "Cannot find any JSON serializer provider on the classpath.";

    private static JsonSerializerProvider defaultProvider;
    private static boolean attemptedLoad;

    /**
     * Creates an instance of {@link JsonSerializer} using the first {@link JsonSerializerProvider} found in the
     * classpath.
     *
     * @return A new instance of {@link JsonSerializer}.
     */
    public static JsonSerializer createInstance() {
        if (defaultProvider == null) {
            loadFromClasspath();
        }

        return defaultProvider.createInstance();
    }

    private static synchronized void loadFromClasspath() {
        if (attemptedLoad && defaultProvider != null) {
            return;
        } else if (attemptedLoad) {
            throw new IllegalStateException(CANNOT_FIND_JSON_SERIALIZER_PROVIDER);
        }

        attemptedLoad = true;
        Iterator<JsonSerializerProvider> iterator = ServiceLoader.load(JsonSerializerProvider.class).iterator();
        if (iterator.hasNext()) {
            defaultProvider = iterator.next();
        } else {
            throw new IllegalStateException(CANNOT_FIND_JSON_SERIALIZER_PROVIDER);
        }
    }

    private JsonSerializerProxy() {
        // no-op
    }
}
