package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


public abstract class JsonFactory {
    private static JsonFactory jacksonJsonFactory = null;
    private static JsonFactory gsonJsonFactory = null;

    public static JsonFactory getInstance() {
        if (JacksonJsonFactory.INITIALIZED) {
            return getJacksonInstance();
        } else if (GsonJsonFactory.INITIALIZED) {
            return getGsonInstance();
        }

        throw new IllegalStateException("No compatible versions of Jackson or Gson are present on the classpath.");
    }

    public synchronized static JsonFactory getJacksonInstance() {
        if (!JacksonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Jackson is present on the classpath.");
        }

        if (jacksonJsonFactory == null) {
            jacksonJsonFactory = new JacksonJsonFactory();
        }

        return jacksonJsonFactory;
    }

    public synchronized static JsonFactory getGsonInstance() {
        if (!GsonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Gson is present on the classpath.");
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
