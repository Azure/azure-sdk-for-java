// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

class JacksonJsonFactory extends JsonFactory {
    static final boolean INITIALIZED = JacksonJsonReader.INITIALIZED && JacksonJsonWriter.INITIALIZED;

    @Override
    public JsonReader getJsonReader(byte[] bytes, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromBytes(bytes, options);
    }

    @Override
    public JsonReader getJsonReader(String string, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromString(string, options);
    }

    @Override
    public JsonReader getJsonReader(InputStream stream, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromStream(stream, options);
    }

    @Override
    public JsonReader getJsonReader(Reader reader, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromReader(reader, options);
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toStream(stream, options);
    }

    @Override
    public JsonWriter getJsonWriter(Writer writer, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toWriter(writer, options);
    }
}
