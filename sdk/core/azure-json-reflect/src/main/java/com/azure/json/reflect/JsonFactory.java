package com.azure.json.reflect;

import com.azure.json.reflect.gson.JsonGsonFactory;
import com.azure.json.reflect.jackson.JsonJacksonFactory;

public class JsonFactory {
    private static Json json;

    private JsonFactory() {
        throw new UnsupportedOperationException();
    }

    public static Json getJson() {
        Package jacksonPackage = null;
        Package gsonPackage = null;

        if(json == null) {
            try {
                jacksonPackage = Class.forName("com.fasterxml.jackson.core.JsonFactory").getPackage();
            } catch (ClassNotFoundException e) {
                // Ignore
            }

            try {
                gsonPackage = Class.forName("com.google.code.gson.Gson").getPackage();
            } catch (ClassNotFoundException e) {
                // Ignore
            }

            if (jacksonPackage != null) {
                json = JsonJacksonFactory.getJson(jacksonPackage);
            }

            if (gsonPackage != null) {
                json = JsonGsonFactory.getJson(gsonPackage);
            }
        }

        return json;
    }

}
