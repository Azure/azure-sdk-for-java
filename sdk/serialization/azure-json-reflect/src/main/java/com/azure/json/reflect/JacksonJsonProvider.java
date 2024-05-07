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
    static final boolean INITIALIZED = JacksonJsonReader.INITIALIZED && JacksonJsonWriter.INITIALIZED;

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toWriter(json, options);
    }
}
