// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

/**
 * The main factory class of Azure Json Reflect package, used to
 * construct {@link JsonProvider} instances.
 * <p>
 * Returned {@link JsonProvider} instances create instances of
 * {@link JsonReader} and {@link JsonWriter} that reflectively
 * search for either Jackson or Gson on the class path and
 * provide abstractions of these libraries without having any
 * hard dependencies on them.
 *
 * @author Jack Sandford
 */
public final class JsonProviderFactory {
    private static JsonProvider jacksonJsonProvider = null;
    private static JsonProvider gsonJsonProvider = null;

    private JsonProviderFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Constructs a singleton instance of {@link JsonProvider} prioritizing Jackson over Gson.
     *
     * @return An instance of {@link JsonProvider}.
     * @throws IllegalStateException If no compatible versions of Jackson or Gson are present on the classpath.
     */
    public static JsonProvider getJsonProvider() {
        if (JacksonJsonProvider.INITIALIZED) {
            return getJacksonJsonProvider();
        } else if (GsonJsonProvider.INITIALIZED) {
            return getGsonJsonProvider();
        }

        throw new IllegalStateException("No compatible versions of Jackson or Gson are present on the classpath.");
    }

    /**
     * Constructs a singleton instance of {@link JsonProvider} that uses Jackson.
     *
     * @return An instance of {@link JsonProvider}.
     * @throws IllegalStateException If no compatible version of Jackson is present on the classpath.
     */
    public static synchronized JsonProvider getJacksonJsonProvider() {
        if (!JacksonJsonProvider.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Jackson is present on the classpath.");
        }

        if (jacksonJsonProvider == null) {
            jacksonJsonProvider = new JacksonJsonProvider();
        }

        return jacksonJsonProvider;
    }

    /**
     * Constructs a singleton instance of {@link JsonProvider} that uses Gson.
     *
     * @return An instance of {@link JsonProvider}.
     * @throws IllegalStateException If no compatible version of Gson is present on the classpath.
     */
    public static synchronized JsonProvider getGsonJsonProvider() {
        if (!GsonJsonProvider.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Gson is present on the classpath.");
        }

        if (gsonJsonProvider == null) {
            gsonJsonProvider = new GsonJsonProvider();
        }

        return gsonJsonProvider;
    }
}
