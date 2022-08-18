package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.gson.GsonJsonReader;
import com.azure.json.reflect.gson.GsonJsonWriter;

import java.io.Reader;

public class JsonGsonFactory implements JsonFactory {
    private Version version;

    protected JsonGsonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        this.version = new Version(jsonPackage.getImplementationVersion());
    }

    public JsonReader getJsonReader(Reader reader) {
        return new GsonJsonReader(reader);
    }

    public JsonWriter getJsonWriter() {
        return null;
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
