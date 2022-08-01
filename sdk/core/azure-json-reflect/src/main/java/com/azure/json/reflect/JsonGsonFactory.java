package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.gson.JsonGsonReader;
import com.azure.json.reflect.gson.JsonGsonWriter;

public class JsonGsonFactory implements JsonFactory {
    private Version version;
    private JsonReader jsonReader;
    private JsonWriter jsonWriter;

    protected JsonGsonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        // Check version etc
        this.version = new Version();
        jsonReader = new JsonGsonReader();
        jsonWriter = new JsonGsonWriter();
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public JsonWriter getJsonWriter() {
        return jsonWriter;
    }
}
