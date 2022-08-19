package com.azure.json.reflect.jackson;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.JsonFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class JacksonJsonFactory implements JsonFactory {
    public JacksonJsonFactory() throws ReflectiveOperationException {
    }

    @Override
    public JsonReader getJsonReader(byte[] bytes) {
        return null;
    }

    @Override
    public JsonReader getJsonReader(String string) {
        return null;
    }

    @Override
    public JsonReader getJsonReader(InputStream stream) {
        return null;
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream) {
        return null;
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
