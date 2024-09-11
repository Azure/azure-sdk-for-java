// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 * Utility methods for working with {@code com.azure.json}.
 */
public final class AzureJsonUtils {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param json The JSON bytes being parsed.
     * @param options The reader options.
     * @return The {@link JacksonJsonReader} that will parse the JSON bytes.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new JacksonJsonReader(configureParser(FACTORY.createParser(json), options), json, null, true, options);
    }

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param json The JSON string being parsed.
     * @param options The reader options.
     * @return The {@link JacksonJsonReader} that will parse the JSON string.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(String json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new JacksonJsonReader(configureParser(FACTORY.createParser(json), options), null, json, true, options);
    }

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param json The JSON stream being parsed.
     * @param options The reader options.
     * @return The {@link JacksonJsonReader} that will parse the JSON stream.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new JacksonJsonReader(configureParser(FACTORY.createParser(json), options), null, null, false, options);
    }

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param json The JSON reader being parsed.
     * @param options The reader options.
     * @return The {@link JacksonJsonReader} that will parse the JSON reader.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new JacksonJsonReader(configureParser(FACTORY.createParser(json), options), null, null, false, options);
    }

    private static JsonParser configureParser(JsonParser parser, JsonOptions options) {
        boolean nonNumericSupported = options == null || options.isNonNumericNumbersSupported();
        boolean commentsSupported = options != null && options.isJsoncSupported();

        return parser
            .configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), nonNumericSupported)
            .configure(JsonParser.Feature.ALLOW_COMMENTS, commentsSupported);
    }

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param parser The {@link JsonParser} parsing JSON.
     * @return A {@link JacksonJsonReader} wrapping the {@link JsonParser}.
     */
    public static JsonReader createReader(JsonParser parser) {
        return new JacksonJsonReader(parser, null, null, false, null);
    }

    /**
     * Creates an instance of {@link JacksonJsonWriter}.
     *
     * @param json The JSON stream being written to.
     * @param options The writer options.
     * @return The {@link JacksonJsonWriter} that will write to the JSON stream.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON target cannot be null when creating a JsonWriter.");
        return new JacksonJsonWriter(configureGenerator(FACTORY.createGenerator(json), options));
    }

    /**
     * Creates an instance of {@link JacksonJsonWriter}.
     *
     * @param json The JSON writer being written to.
     * @param options The writer options.
     * @return The {@link JacksonJsonWriter} that will write to the JSON writer.
     * @throws IOException If an instance fails to be created.
     */
    public static JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON target cannot be null when creating a JsonWriter.");
        return new JacksonJsonWriter(configureGenerator(FACTORY.createGenerator(json), options));
    }

    private static JsonGenerator configureGenerator(JsonGenerator generator, JsonOptions options) {
        boolean nonNumericSupported = options == null || options.isNonNumericNumbersSupported();

        return generator.configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS.mappedFeature(), nonNumericSupported);
    }

    /**
     * Creates an instance of {@link JacksonJsonWriter}.
     *
     * @param generator The {@link JsonGenerator} writing JSON.
     * @return A {@link JacksonJsonWriter} wrapping the {@link JsonGenerator}.
     */
    public static JsonWriter createWriter(JsonGenerator generator) {
        return new JacksonJsonWriter(generator);
    }

    private AzureJsonUtils() {
    }
}
