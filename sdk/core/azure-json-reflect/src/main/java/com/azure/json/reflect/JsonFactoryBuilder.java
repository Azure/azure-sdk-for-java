package com.azure.json.reflect;

import com.azure.json.reflect.gson.GsonJsonFactory;
import com.azure.json.reflect.jackson.JacksonJsonFactory;

public class JsonFactoryBuilder {
    public JsonFactory build() {
        try {
            return new JacksonJsonFactory();
        } catch (ReflectiveOperationException ignored) {
            // Jackson not on classpath
        }

        try {
            return new GsonJsonFactory();
        } catch (ReflectiveOperationException ignored) {
            // GSON not on classpath
        }

        return null;
    }
}
