package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.gson.JsonGsonFactory;
import com.azure.json.reflect.jackson.JsonJacksonFactory;

public class JsonFactory {
    private static JsonReader jsonReader;
    private static JsonWriter jsonWriter;

    private JsonFactory() {
        throw new UnsupportedOperationException();
    }

    public static JsonReader getJsonReader() {
        if (jsonReader == null) {
            try {
                Package jacksonPackage = Class.forName("com.fasterxml.jackson.core.JsonFactory").getPackage();
                jsonReader = JsonJacksonFactory.getJsonReader(jacksonPackage);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        if (jsonReader == null) {
            try {
                Package gsonPackage = Class.forName("com.google.code.gson.Gson").getPackage();
                jsonReader = JsonGsonFactory.getJsonReader(gsonPackage);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        return jsonReader;
    }

    public static JsonWriter getJsonWriter() {
        if (jsonWriter == null) {
            try {
                Package jacksonPackage = Class.forName("com.fasterxml.jackson.core.JsonFactory").getPackage();
                jsonWriter = JsonJacksonFactory.getJsonWriter(jacksonPackage);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        if (jsonWriter == null) {
            try {
                Package gsonPackage = Class.forName("com.google.code.gson.Gson").getPackage();
                jsonWriter = JsonGsonFactory.getJsonWriter(gsonPackage);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        return jsonWriter;
    }

}
