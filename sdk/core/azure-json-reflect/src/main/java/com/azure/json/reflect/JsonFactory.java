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

    /**
     * Constructs a singleton instance of {@link JsonFactory} prioritizing Jackson over Gson.
     *
     * @return An instance of {@link JsonFactory}.
     * @throws IllegalStateException If no compatible versions of Jackson or Gson are present on the classpath.
     */
    public static JsonFactory getInstance() {
        if (JacksonJsonFactory.INITIALIZED) {
            return getJacksonInstance();
        } else if (GsonJsonFactory.INITIALIZED) {
            return getGsonInstance();
        }

        throw new IllegalStateException("No compatible versions of Jackson or Gson are present on the classpath.");
    }

    /**
     * Constructs a singleton instance of {@link JsonFactory} that uses Jackson.
     *
     * @return An instance of {@link JsonFactory}.
     * @throws IllegalStateException If no compatible version of Jackson is present on the classpath.
     */
    public synchronized static JsonFactory getJacksonInstance() {
        if (!JacksonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Jackson is present on the classpath.");
        }

        if (jacksonJsonFactory == null) {
            jacksonJsonFactory = new JacksonJsonFactory();
        }

        return jacksonJsonFactory;
    }

    /**
     * Constructs a singleton instance of {@link JsonFactory} that uses Gson.
     *
     * @return An instance of {@link JsonFactory}.
     * @throws IllegalStateException If no compatible version of Gson is present on the classpath.
     */
    public synchronized static JsonFactory getGsonInstance() {
        if (!GsonJsonFactory.INITIALIZED) {
            throw new IllegalStateException("No compatible version of Gson is present on the classpath.");
        }

        if (gsonJsonFactory == null) {
            gsonJsonFactory = new GsonJsonFactory();
        }

        return gsonJsonFactory;
    }

    /**
     * Constructs an instance of {@link JsonReader} from a {@code byte[]}.
     *
     * @param bytes JSON {@code byte[]}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If reader initialization fails due to I/O problem
     * @throws NullPointerException If {@code bytes} is null.
     */
    public abstract JsonReader getJsonReader(byte[] bytes, JsonOptions options) throws IOException;

    /**
     * Constructs an instance of {@link JsonReader} from a String.
     *
     * @param string JSON String.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If reader initialization fails due to I/O problem
     * @throws NullPointerException If {@code string} is null.
     */
    public abstract JsonReader getJsonReader(String string, JsonOptions options) throws IOException;

    /**
     * Constructs an instance of {@link JsonReader} from an {@link InputStream}.
     *
     * @param stream JSON {@link InputStream}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link JsonReader}.
     * @throws IOException If reader initialization fails due to I/O problem
     * @throws NullPointerException If {@code stream} is null.
     */
    public abstract JsonReader getJsonReader(InputStream stream, JsonOptions options) throws IOException;

    /**
     * Constructs an instance of {@link GsonJsonReader} from a {@link Reader}.
     *
     * @param reader JSON {@link Reader}.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonReader}.
     * @return An instance of {@link GsonJsonReader}.
     * @throws IOException If reader initialization fails due to I/O problem
     * @throws NullPointerException If {@code reader} is null.
     */
    public abstract JsonReader getJsonReader(Reader reader, JsonOptions options) throws IOException;

    /**
     * Creates a {@link JsonWriter} that writes the given {@link OutputStream}.
     * <p>
     * The passed {@link OutputStream} won't be closed when {@link JsonWriter#close()} is called as the {@link JsonWriter}
     * isn't the owner of the stream.
     *
     * @param stream The {@link OutputStream} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonWriter}.
     * @throws IOException If writer initialization fails due to I/O problem
     * @throws NullPointerException If {@code stream} is null.
     *
     */
    public abstract JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) throws IOException;

    /**
     * Creates a {@link JsonWriter} that writes the given {@link Writer}.
     * <p>
     * The passed {@link Writer} won't be closed when {@link JsonWriter#close()} is called as the {@link JsonWriter}
     * isn't the owner of the stream.
     *
     * @param writer The {@link Writer} that will be written.
     * @param options {@link JsonOptions} to configure the creation of the {@link JsonWriter}.
     * @return An instance of {@link JsonWriter}.
     * @throws IOException If writer initialization fails due to I/O problem
     * @throws NullPointerException If {@code writer} is null.
     */
    public abstract JsonWriter getJsonWriter(Writer writer, JsonOptions options) throws IOException;
}
