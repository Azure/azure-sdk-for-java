package com.azure.json.reflect.jackson;

import com.azure.json.reflect.Json;

public class JsonJacksonFactory {
    private static Json json;

    private JsonJacksonFactory() {
        throw new UnsupportedOperationException();
    }

    public static Json getJson(Package jacksonPackage) {
        if (jacksonPackage.getName() == "com.fasterxml.jackson.core") {
            // Check version etc
        }

        return json;
    }
}
