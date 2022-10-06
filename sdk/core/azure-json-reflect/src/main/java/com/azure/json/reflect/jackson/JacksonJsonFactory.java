package com.azure.json.reflect.jackson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.JsonFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class JacksonJsonFactory implements JsonFactory {
    public JacksonJsonFactory() throws ReflectiveOperationException {
    }

    @Override
    public JsonReader getJsonReader(byte[] bytes, JsonOptions options) {
        return null;
    }

    @Override
    public JsonReader getJsonReader(String string, JsonOptions options) {
        return null;
    }

    @Override
    public JsonReader getJsonReader(InputStream stream, JsonOptions options) {
        return null;
    }

    @Override
    public JsonReader getJsonReader(Reader reader, JsonOptions options) {
        return null;
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) {
        return null;
    }

    @Override
    public JsonWriter getJsonWriter(Writer writer, JsonOptions options) {
        return null;
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
