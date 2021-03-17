// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.implementation.serializer.JacksonSerializerProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class is a proxy for using a {@link JsonSerializerProvider} loaded from the classpath.
 */
public final class JsonSerializerProviders {
    private static final String CANNOT_FIND_JSON_SERIALIZER_PROVIDER = "A request was made to load the default JSON "
        + "serializer provider but one could not be found on the classpath. If you are using a dependency manager, "
        + "consider including a dependency on azure-core-serializer-json-jackson or azure-core-serializer-json-gson. "
        + "Depending on your existing dependencies, you have the choice of Jackson or GSON implementations. "
        + "Additionally, refer to https://aka.ms/azsdk/java/docs/custom-jsonserializer to learn about writing your own "
        + "implementation.";

    private static JsonSerializerProvider jsonSerializerProvider;
    private static boolean attemptedLoad;

    /**
     * Creates an instance of {@link JsonSerializer} using the first {@link JsonSerializerProvider} found in the
     * classpath.
     *
     * @return A new instance of {@link JsonSerializer}.
     * @throws IllegalStateException if a {@link JsonSerializerProvider} is not found in the classpath.
     */
    public static JsonSerializer createInstance() {
        return createInstance(false);
    }

    /**
     * Creates an instance of {@link JsonSerializer} using the first {@link JsonSerializerProvider} found in the
     * classpath. If no provider is found in classpath, a default provider will be included if {@code useDefaultIfAbsent}
     * is set to true.
     *
     * @param useDefaultIfAbsent If no provider is found in classpath, a default provider will be used.
     * if {@code useDefaultIfAbsent} is set to true.
     * @return A new instance of {@link JsonSerializer}.
     * @throws IllegalStateException if a {@link JsonSerializerProvider} is not found in the classpath and
     * {@code useDefaultIfAbsent} is set to false.
     */
    public static JsonSerializer createInstance(boolean useDefaultIfAbsent) {
        if (jsonSerializerProvider == null) {
            loadDefaultSerializer(useDefaultIfAbsent);
        }

        return jsonSerializerProvider.createInstance();
    }

    private static synchronized void loadDefaultSerializer(boolean useDefaultIfAbsent) {
        if (attemptedLoad && jsonSerializerProvider != null) {
            return;
        } else if (attemptedLoad) {
            throw new IllegalStateException(CANNOT_FIND_JSON_SERIALIZER_PROVIDER);
        }

        attemptedLoad = true;
        Iterator<JsonSerializerProvider> iterator = ServiceLoader.load(JsonSerializerProvider.class).iterator();
        if (iterator.hasNext()) {
            jsonSerializerProvider = iterator.next();
            return;
        }

        if (useDefaultIfAbsent) {
            jsonSerializerProvider = new JacksonSerializerProvider();
        } else {
            throw new IllegalStateException(CANNOT_FIND_JSON_SERIALIZER_PROVIDER);
        }
    }

    private JsonSerializerProviders() {
        // no-op
    }
}
