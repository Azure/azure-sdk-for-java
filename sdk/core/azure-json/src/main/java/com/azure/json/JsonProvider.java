// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * An interface to be implemented by any azure-json plugin that wishes to provide an alternate {@link JsonReader} or
 * {@link JsonWriter} implementation.
 */
public interface JsonProvider {
    /**
     * Creates an instance of {@link JsonReader} that reads a {@code byte[]}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonReader createReader(byte[] json) {
        return createReader(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@code byte[]}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonReader createReader(byte[] json, JsonOptions options);

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link String}.
     *
     * @param json The JSON represented as a {@link String}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonReader createReader(String json) {
        return createReader(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link String}.
     *
     * @param json The JSON represented as a {@link String}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonReader createReader(String json, JsonOptions options);

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link InputStream}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonReader createReader(InputStream json) {
        return createReader(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link InputStream}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonReader createReader(InputStream json, JsonOptions options);

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link Reader}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonReader createReader(Reader json) {
        return createReader(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link Reader}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonReader createReader(Reader json, JsonOptions options);

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link OutputStream}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonWriter createWriter(OutputStream json) {
        return createWriter(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link OutputStream}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonWriter createWriter(OutputStream json, JsonOptions options);

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link Writer}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     */
    default JsonWriter createWriter(Writer json) {
        return createWriter(json, JsonOptions.DEFAULT_OPTIONS);
    }

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link Writer}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} is null.
     */
    JsonWriter createWriter(Writer json, JsonOptions options);
}
