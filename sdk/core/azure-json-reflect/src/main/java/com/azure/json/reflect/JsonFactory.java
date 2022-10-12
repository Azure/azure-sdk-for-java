package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.*;

public abstract class JsonFactory {
    private static JsonFactory jacksonJsonFactory = null;
    private static boolean jacksonAttempted = false;

    private static JsonFactory gsonJsonFactory = null;
    private static boolean gsonAttempted = false;

    public static JsonFactory getInstance() {
        JsonFactory jsonFactory = getJacksonInstance();

        if (jsonFactory == null) {
            jsonFactory = getGsonInstance();
        }

        return jsonFactory;
    }

    public synchronized static JsonFactory getJacksonInstance() {
        if (!jacksonAttempted) {
            jacksonAttempted = true;
            try {
                jacksonJsonFactory = new JacksonJsonFactory();
            } catch (ReflectiveOperationException ignored) {
                // Jackson not on classpath
            }
        }

        return jacksonJsonFactory;
    }

    public synchronized static JsonFactory getGsonInstance() {
        if (!gsonAttempted) {
            gsonAttempted = true;
            try {
                gsonJsonFactory = new GsonJsonFactory();
            } catch (ReflectiveOperationException ignored) {
                // Gson not on classpath
            }
        }

        return gsonJsonFactory;
    }

    public abstract JsonReader getJsonReader(byte[] bytes, JsonOptions options) throws IOException;
    public abstract JsonReader getJsonReader(String string, JsonOptions options) throws IOException;
    public abstract JsonReader getJsonReader(InputStream stream, JsonOptions options) throws IOException;
    public abstract JsonReader getJsonReader(Reader reader, JsonOptions options) throws IOException;
    public abstract JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) throws IOException;
    public abstract JsonWriter getJsonWriter(Writer writer, JsonOptions options) throws IOException;
}
