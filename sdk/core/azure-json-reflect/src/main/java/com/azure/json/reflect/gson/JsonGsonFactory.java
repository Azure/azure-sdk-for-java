package com.azure.json.reflect.gson;

import com.azure.json.reflect.Json;

public class JsonGsonFactory {
    private static Json json;

    private JsonGsonFactory() {
        throw new UnsupportedOperationException();
    }

    public static Json getJson(Package gsonPackage) {
        if (gsonPackage.getName() == "com.google.code.gson") {
            // Check version etc
        }

        return json;
    }
}
