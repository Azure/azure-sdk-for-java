// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Implementation of {@link JsonProvider} that creates instances using GSON.
 */
public class GsonJsonProvider implements JsonProvider {
    private static final String JSON_READER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonReader.";

    private static final String JSON_WRITER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonWriter.";

    /**
     * Creates an instance of {@link GsonJsonProvider}.
     */
    public GsonJsonProvider() {
    }

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) {
        validate(json, options, JSON_READER_EXCEPTION);

        return GsonJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) {
        validate(json, options, JSON_READER_EXCEPTION);

        return GsonJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) {
        validate(json, options, JSON_READER_EXCEPTION);

        return GsonJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) {
        validate(json, options, JSON_READER_EXCEPTION);

        return GsonJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return GsonJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return GsonJsonWriter.toWriter(json, options);
    }

    private static void validate(Object json, JsonOptions options, String exceptionMessage) {
        if (json == null || options == null) {
            throw new NullPointerException(exceptionMessage);
        }
    }
}
