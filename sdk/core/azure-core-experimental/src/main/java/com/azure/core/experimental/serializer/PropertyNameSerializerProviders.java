// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class is a proxy for using a {@link PropertyNameSerializerProvider} loaded from the classpath.
 */
public final class PropertyNameSerializerProviders {
    private static final String CANNOT_FIND_JSON_SERIALIZER_PROVIDER =
        "Cannot find any seriailized name serializer provider on the classpath.";

    private static PropertyNameSerializerProvider defaultProvider;
    private static boolean attemptedLoad;

    /**
     * Creates an instance of {@link PropertyNameSerializer} using the first {@link PropertyNameSerializer} found in the
     * classpath.
     *
     * @return A new instance of {@link PropertyNameSerializer}.
     */
    public static PropertyNameSerializer createInstance() {
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
        Iterator<PropertyNameSerializerProvider> iterator =
            ServiceLoader.load(PropertyNameSerializerProvider.class).iterator();
        if (iterator.hasNext()) {
            defaultProvider = iterator.next();
        } else {
            throw new IllegalStateException(CANNOT_FIND_JSON_SERIALIZER_PROVIDER);
        }
    }

    private PropertyNameSerializerProviders() {
        // no-op
    }
}
