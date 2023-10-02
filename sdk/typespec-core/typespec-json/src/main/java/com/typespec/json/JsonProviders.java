// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import com.typespec.json.implementation.DefaultJsonProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Utility class for {@link JsonProvider} that will use the implementation of {@link JsonProvider} found on the
 * classpath to create instances of {@link JsonReader} or {@link JsonWriter}.
 * <p>
 * If no implementation of {@link JsonProvider} is found on the classpath a default implementation provided by this
 * library will be used.
 * <p>
 * At this time, additional implementations of {@link JsonProvider} found on the classpath after the first will cause
 * an {@link IllegalStateException} to be thrown. Ensure the implementation that should be used is the only one listed
 * in {@code META-INF/services/com.azure.json.JsonProvider} of your JAR.
 *
 * @see com.typespec.json
 * @see JsonProvider
 * @see JsonReader
 * @see JsonWriter
 */
public final class JsonProviders {
    private static final JsonOptions DEFAULT_OPTIONS = new JsonOptions();
    private static final JsonProvider JSON_PROVIDER;

    static {
        // Use as classloader to load provider-configuration files and provider classes the classloader
        // that loaded this class. In most cases this will be the System classloader.
        // But this choice here provides additional flexibility in managed environments that control
        // classloading differently (OSGi, Spring and others) and don't depend on the
        // System classloader to load HttpClientProvider classes.
        ServiceLoader<JsonProvider> serviceLoader = ServiceLoader.load(JsonProvider.class,
            JsonProvider.class.getClassLoader());
        // Use the first provider found in the service loader iterator.
        List<String> implementationNames = new ArrayList<>();
        Iterator<JsonProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            JsonProvider implementation = it.next();
            implementationNames.add(implementation.getClass().getName());
            JSON_PROVIDER = implementation;
        } else {
            JSON_PROVIDER = new DefaultJsonProvider();
        }

        while (it.hasNext()) {
            // For now, ignore other implementations found.
            JsonProvider implementation = it.next();
            implementationNames.add(implementation.getClass().getName());
        }

        if (implementationNames.size() > 1) {
            throw new IllegalStateException("More than one implementation of 'com.azure.json.JsonProvider' was found "
                + "on the classpath. At this time 'azure-json' only supports one implementation being on the "
                + "classpath. Remove all implementations, except the one that should be used during runtime, from "
                + "'META-INF/services/com.azure.json.JsonProvider'. Found implementations were: "
                + String.join(", ", implementationNames));
        }
    }

    private JsonProviders() {
        // no-op
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@code byte[]}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(byte[], JsonOptions) createReader(json, new JsonOptions())}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(byte[] json) throws IOException {
        return createReader(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@code byte[]}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link String}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(String, JsonOptions) createReader(json, new JsonOptions())}.
     *
     * @param json The JSON represented as a {@link String}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(String json) throws IOException {
        return createReader(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link String}.
     *
     * @param json The JSON represented as a {@link String}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(String json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link InputStream}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to
     * {@link #createReader(InputStream, JsonOptions) createReader(json, new JsonOptions())}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(InputStream json) throws IOException {
        return createReader(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link InputStream}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link Reader}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createReader(Reader, JsonOptions) createReader(json, new JsonOptions())}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(Reader json) throws IOException {
        return createReader(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link Reader}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    public static JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link OutputStream}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createWriter(OutputStream, JsonOptions) createWriter(json, new JsonOptions())}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonWriter} cannot be instantiated.
     */
    public static JsonWriter createWriter(OutputStream json) throws IOException {
        return createWriter(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link OutputStream}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonWriter} cannot be instantiated.
     */
    public static JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createWriter(json, options);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link Writer}.
     * <p>
     * If a provider could not be found on the classpath this will use the default implementation, effectively the
     * equivalent to {@link #createWriter(Writer, JsonOptions) createWriter(json, new JsonOptions())}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     * @throws IOException If a {@link JsonWriter} cannot be instantiated.
     */
    public static JsonWriter createWriter(Writer json) throws IOException {
        return createWriter(json, DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link Writer}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonWriter} cannot be instantiated.
     */
    public static JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createWriter(json, options);
    }
}
