package com.azure.json.reflect.gson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.JsonFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class GsonJsonFactory implements JsonFactory {
    public GsonJsonFactory() throws ReflectiveOperationException {
        GsonJsonReader.initialize();
        GsonJsonWriter.initialize();
    }

    @Override
    public JsonReader getJsonReader(byte[] bytes, JsonOptions options) {
        return GsonJsonReader.fromBytes(bytes, options);
    }

    @Override
    public JsonReader getJsonReader(String string, JsonOptions options) {
        return GsonJsonReader.fromString(string, options);
    }

    @Override
    public JsonReader getJsonReader(InputStream stream, JsonOptions options) {
        return GsonJsonReader.fromStream(stream, options);
    }

    @Override
    public JsonReader getJsonReader(Reader reader, JsonOptions options) {
        return GsonJsonReader.fromReader(reader, options);
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) {
        return GsonJsonWriter.toStream(stream, options);
    }

    @Override
    public JsonWriter getJsonWriter(Writer writer, JsonOptions options) {
        return GsonJsonWriter.toWriter(writer, options);
    }
}
