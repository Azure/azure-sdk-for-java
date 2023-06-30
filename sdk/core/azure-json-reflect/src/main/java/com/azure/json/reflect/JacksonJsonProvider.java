// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


class JacksonJsonProvider implements JsonProvider {
    private static final String JSON_READER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonReader.";

    private static final String JSON_WRITER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to "
        + "create an instance of JsonWriter.";

    static final boolean INITIALIZED = JacksonJsonReader.INITIALIZED && JacksonJsonWriter.INITIALIZED;

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return JacksonJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return JacksonJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return JacksonJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        validate(json, options, JSON_READER_EXCEPTION);

        return JacksonJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return JacksonJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        validate(json, options, JSON_WRITER_EXCEPTION);

        return JacksonJsonWriter.toWriter(json, options);
    }

    private static void validate(Object json, JsonOptions options, String exceptionMessage) {
        if (json == null || options == null) {
            throw new NullPointerException(exceptionMessage);
        }
    }
}
