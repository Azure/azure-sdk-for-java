// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.implementation;

import com.typespec.json.JsonOptions;
import com.typespec.json.JsonProvider;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Default {@link JsonProvider} implementation.
 */
public final class DefaultJsonProvider implements JsonProvider {
    private static final String JSON_READER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonReader.";

    private static final String JSON_WRITER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonWriter.";

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return DefaultJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return DefaultJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return DefaultJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return DefaultJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return DefaultJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return DefaultJsonWriter.toWriter(json, options);
    }

    private static void validate(Object json, JsonOptions options, String exceptionMessage) {
        if (json == null || options == null) {
            throw new NullPointerException(exceptionMessage);
        }
    }
}
