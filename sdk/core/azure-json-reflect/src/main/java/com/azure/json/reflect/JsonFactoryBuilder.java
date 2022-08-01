package com.azure.json.reflect;

public class JsonFactoryBuilder {
    public JsonFactory build() {
        try {
            return new JsonJacksonFactory(Class.forName("com.fasterxml.jackson.core.JsonFactory").getPackage());
        } catch (ClassNotFoundException ignored) {
            // Jackson not on classpath
        }

        try {
            return new JsonGsonFactory(Class.forName("com.google.gson.Gson").getPackage());
        } catch (ClassNotFoundException ignored) {
            // Gson not on classpath
        }

        return null;
    }
}
