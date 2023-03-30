// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.implementation.DefaultJsonProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Handles loading an instance of {@link JsonProvider} found on the classpath.
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
        Iterator<JsonProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            JSON_PROVIDER = it.next();
        } else {
            JSON_PROVIDER = new DefaultJsonProvider();
        }

        while (it.hasNext()) {
            it.next();
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
