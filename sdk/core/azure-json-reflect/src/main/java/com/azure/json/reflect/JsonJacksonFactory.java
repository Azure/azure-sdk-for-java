package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.jackson.JacksonJsonReader;
import com.azure.json.reflect.jackson.JacksonJsonWriter;

import java.io.Reader;

public class JsonJacksonFactory implements JsonFactory {
    private Version version;

    protected JsonJacksonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        // Check version etc
        this.version = new Version(jsonPackage.getImplementationVersion());
    }

    public JsonReader getJsonReader(Reader reader) {
        return new JacksonJsonReader(reader);
    }

    public JsonWriter getJsonWriter() {
        return new JacksonJsonWriter();
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
