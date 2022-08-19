package com.azure.json.reflect.gson;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.JsonFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class GsonJsonFactory implements JsonFactory {
    public GsonJsonFactory() throws ReflectiveOperationException {
        GsonJsonReader.initialize();
        GsonJsonWriter.initialize();
    }

    @Override
    public JsonReader getJsonReader(byte[] bytes) {
        return GsonJsonReader.fromBytes(bytes);
    }

    @Override
    public JsonReader getJsonReader(String string) {
        return GsonJsonReader.fromString(string);
    }

    @Override
    public JsonReader getJsonReader(InputStream stream) {
        return GsonJsonReader.fromStream(stream);
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream) {
        return GsonJsonWriter.toStream(stream);
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
