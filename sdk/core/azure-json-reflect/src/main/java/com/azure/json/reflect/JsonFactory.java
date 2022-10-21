package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.*;

public abstract class JsonFactory {
    private static JsonFactory jacksonJsonFactory = null;
    private static JsonFactory gsonJsonFactory = null;

    public static JsonFactory getInstance() {
        JsonFactory jsonFactory = getJacksonInstance();

        if (jsonFactory == null) {
            jsonFactory = getGsonInstance();
        }

        return jsonFactory;
    }

    public synchronized static JsonFactory getJacksonInstance() {
        if (!JacksonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("Jackson is not present or an incorrect version is present.");
        }

        if(jacksonJsonFactory == null) {
            jacksonJsonFactory = new JacksonJsonFactory();
        }

        return jacksonJsonFactory;
    }

    public synchronized static JsonFactory getGsonInstance() {
        if (!GsonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("Gson is not present or an incorrect version is present.");
        }

        if (gsonJsonFactory == null) {
            gsonJsonFactory = new GsonJsonFactory();
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
