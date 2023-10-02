// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.json.JsonOptions;
import com.typespec.json.JsonReader;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;

import java.io.IOException;
import java.util.Objects;

// Copied from azure-core-serializer-json-jackson, with minor edits.
/**
 * Utility methods for working with {@code com.azure.json}.
 */
final class AzureJsonUtils {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param json The JSON bytes being parsed.
     * @param options The reader options.
     * @return The {@link JacksonJsonReader} that will parse the JSON bytes.
     * @throws IOException If an instance fails to be created.
     */
    static JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
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
    static JsonReader createReader(String json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "JSON source cannot be null when creating a JsonReader.");
        return new JacksonJsonReader(configureParser(FACTORY.createParser(json), options), null, json, true, options);
    }

    private static JsonParser configureParser(JsonParser parser, JsonOptions options) {
        boolean nonNumericSupported = options == null || options.isNonNumericNumbersSupported();

        return parser.configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), nonNumericSupported);
    }

    /**
     * Creates an instance of {@link JacksonJsonReader}.
     *
     * @param parser The {@link JsonParser} parsing JSON.
     * @return A {@link JacksonJsonReader} wrapping the {@link JsonParser}.
     */
    static JsonReader createReader(JsonParser parser) {
        return new JacksonJsonReader(parser, null, null, false, null);
    }

    private AzureJsonUtils() {
    }
}
