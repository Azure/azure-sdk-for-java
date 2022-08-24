// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.gson;

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
    @Override
    public JsonReader createReader(byte[] json) {
        return GsonJsonReader.fromBytes(json);
    }

    @Override
    public JsonReader createReader(String json) {
        return GsonJsonReader.fromString(json);
    }

    @Override
    public JsonReader createReader(InputStream json) {
        return GsonJsonReader.fromStream(json);
    }

    @Override
    public JsonReader createReader(Reader json) {
        return GsonJsonReader.fromReader(json);
    }

    @Override
    public JsonWriter createWriter(OutputStream json) {
        return GsonJsonWriter.toStream(json);
    }

    @Override
    public JsonWriter createWriter(Writer json) {
        return GsonJsonWriter.toWriter(json);
    }
}
