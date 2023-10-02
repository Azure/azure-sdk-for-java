// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * An interface to be implemented by any azure-json plugin that wishes to provide an alternate {@link JsonReader} or
 * {@link JsonWriter} implementation.
 *
 * @see com.typespec.json
 * @see JsonReader
 * @see JsonWriter
 */
public interface JsonProvider {

    /**
     * Creates an instance of {@link JsonReader} that reads a {@code byte[]}.
     *
     * @param json The JSON represented as a {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonReader createReader(byte[] json, JsonOptions options) throws IOException;

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link String}.
     *
     * @param json The JSON represented as a {@link String}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonReader createReader(String json, JsonOptions options) throws IOException;

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link InputStream}.
     *
     * @param json The JSON represented as a {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonReader createReader(InputStream json, JsonOptions options) throws IOException;

    /**
     * Creates an instance of {@link JsonReader} that reads a {@link Reader}.
     *
     * @param json The JSON represented as a {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return A new instance of {@link JsonReader}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonReader createReader(Reader json, JsonOptions options) throws IOException;

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link OutputStream}.
     *
     * @param json The JSON represented as an {@link OutputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException;

    /**
     * Creates an instance of {@link JsonWriter} that writes to an {@link Writer}.
     *
     * @param json The JSON represented as an {@link Writer}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return A new instance of {@link JsonWriter}.
     * @throws NullPointerException If {@code json} or {@code options} is null.
     * @throws IOException If a {@link JsonReader} cannot be instantiated.
     */
    JsonWriter createWriter(Writer json, JsonOptions options) throws IOException;
}
