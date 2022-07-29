package com.azure.json.reflect.jackson;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

public class JsonJacksonFactory {
    private static JsonReader jsonReader;
    private static JsonWriter jsonWriter;

    private JsonJacksonFactory() {
        throw new UnsupportedOperationException();
    }

    public static JsonReader getJsonReader(Package jacksonPackage) {
        if (jacksonPackage.getName() == "com.fasterxml.jackson.core") {
            // Check version etc
        }

        return jsonReader;
    }

    public static JsonWriter getJsonWriter(Package jacksonPackage) {
        if (jacksonPackage.getName() == "com.fasterxml.jackson.core") {
            // Check version etc
        }

        return jsonWriter;
    }
}
