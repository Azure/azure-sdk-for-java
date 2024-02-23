// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

class GsonJsonProvider implements JsonProvider {
    static final boolean INITIALIZED = GsonJsonReader.INITIALIZED && GsonJsonWriter.INITIALIZED;

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) {
        return GsonJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) {
        return GsonJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) {
        return GsonJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) {
        return GsonJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) {
        return GsonJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) {
        return GsonJsonWriter.toWriter(json, options);
    }
}
