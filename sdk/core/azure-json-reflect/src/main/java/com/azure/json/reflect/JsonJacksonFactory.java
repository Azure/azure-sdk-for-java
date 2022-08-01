package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.jackson.JsonJacksonReader;
import com.azure.json.reflect.jackson.JsonJacksonWriter;

public class JsonJacksonFactory implements JsonFactory {
    private Version version;
    private JsonReader jsonReader;
    private JsonWriter jsonWriter;

    protected JsonJacksonFactory(Package jsonPackage) {
        if (!"com.fasterxml.jackson.core".equals(jsonPackage.getName())) {
            throw new IllegalArgumentException("Incorrect package passed, please pass in com.fasterxml.jackson.core");
        }
        // Check version etc
        this.version = new Version();
        jsonReader = new JsonJacksonReader();
        jsonWriter = new JsonJacksonWriter();
    }

    public JsonReader getJsonReader() {
        return jsonReader;
    }

    public JsonWriter getJsonWriter() {
        return jsonWriter;
    }
}
