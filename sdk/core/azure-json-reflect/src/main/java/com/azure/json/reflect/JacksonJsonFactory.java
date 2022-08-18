package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.jackson.JacksonJsonWriter;

import java.io.InputStream;

public class JacksonJsonFactory implements JsonFactory {
    private Version version;

    protected JacksonJsonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        // Check version etc
        this.version = new Version(jsonPackage.getImplementationVersion());
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
    public JsonWriter getJsonWriter() {
        return new JacksonJsonWriter();
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
