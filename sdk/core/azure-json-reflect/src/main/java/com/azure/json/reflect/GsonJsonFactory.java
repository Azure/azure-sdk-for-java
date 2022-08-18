package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.gson.GsonJsonReader;

import java.io.InputStream;

public class GsonJsonFactory implements JsonFactory {
    private Version version;

    protected GsonJsonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        this.version = new Version(jsonPackage.getImplementationVersion());
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
    public JsonWriter getJsonWriter() {
        return null;
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
