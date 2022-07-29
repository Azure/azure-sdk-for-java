package com.azure.json.reflect.gson;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

public class JsonGsonFactory {
    private static JsonReader jsonReader;
    private static JsonWriter jsonWriter;

    private JsonGsonFactory() {
        throw new UnsupportedOperationException();
    }

    public static JsonReader getJsonReader(Package gsonPackage) {
        if (gsonPackage.getName() == "com.google.code.gson") {
            // Check version etc
        }

        return jsonReader;
    }

    public static JsonWriter getJsonWriter(Package gsonPackage) {
        if (gsonPackage.getName() == "com.google.code.gson") {
            // Check version etc
        }

        return jsonWriter;
    }
}
