// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Utility methods for working with {@code com.azure.json}.
 */
public final class AzureJsonUtils {
    /**
     * Creates an instance of {@link GsonJsonReader}.
     *
     * @param json The JSON bytes being parsed.
     * @param options The reader options.
     * @return The {@link GsonJsonReader} that will parse the JSON bytes.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new GsonJsonReader(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), json,
            null, true, options);
    }

    /**
     * Creates an instance of {@link GsonJsonReader}.
     *
     * @param json The JSON string being parsed.
     * @param options The reader options.
     * @return The {@link GsonJsonReader} that will parse the JSON string.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(String json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new GsonJsonReader(new StringReader(json), null, json, true, options);
    }

    /**
     * Creates an instance of {@link GsonJsonReader}.
     *
     * @param json The JSON stream being parsed.
     * @param options The reader options.
     * @return The {@link GsonJsonReader} that will parse the JSON stream.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new GsonJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8), null, null, false, options);
    }

    /**
     * Creates an instance of {@link GsonJsonReader}.
     *
     * @param json The JSON reader being parsed.
     * @param options The reader options.
     * @return The {@link GsonJsonReader} that will parse the JSON reader.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new GsonJsonReader(json, null, null, false, options);
    }

    /**
     * Creates an instance of {@link GsonJsonReader}.
     *
     * @param reader The {@link com.google.gson.stream.JsonReader} reading JSON.
     * @param options The options used to create the reader.
     * @return A {@link GsonJsonReader} wrapping the {@link com.google.gson.stream.JsonReader}.
     */
    public static JsonReader createReader(com.google.gson.stream.JsonReader reader, JsonOptions options) {
        return new GsonJsonReader(reader, options, false);
    }

    /**
     * Creates an instance of {@link GsonJsonWriter}.
     *
     * @param json The JSON stream being written to.
     * @param options The writer options.
     * @return The {@link GsonJsonWriter} that will write to the JSON stream.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON target cannot be null when creating a JsonWriter.");
        return new GsonJsonWriter(createGsonWriter(new OutputStreamWriter(json, StandardCharsets.UTF_8), options));
    }

    /**
     * Creates an instance of {@link GsonJsonWriter}.
     *
     * @param json The JSON writer being written to.
     * @param options The writer options.
     * @return The {@link GsonJsonWriter} that will write to the JSON writer.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON target cannot be null when creating a JsonWriter.");
        return new GsonJsonWriter(createGsonWriter(json, options));
    }

    private static com.google.gson.stream.JsonWriter createGsonWriter(Writer writer, JsonOptions options) {
        boolean lenient = options == null || options.isNonNumericNumbersSupported();

        com.google.gson.stream.JsonWriter gsonWriter = new com.google.gson.stream.JsonWriter(writer);
        gsonWriter.setLenient(lenient);

        return gsonWriter;
    }

    /**
     * Creates an instance of {@link GsonJsonWriter}.
     *
     * @param writer The {@link com.google.gson.stream.JsonWriter} writing JSON.
     * @return A {@link GsonJsonWriter} wrapping the {@link com.google.gson.stream.JsonWriter}.
     */
    public static JsonWriter createWriter(com.google.gson.stream.JsonWriter writer) {
        return new GsonJsonWriter(writer);
    }

    private AzureJsonUtils() {
    }
}
