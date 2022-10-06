package com.azure.json.reflect.jackson;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;
import com.azure.json.reflect.JsonFactory;

import java.io.*;

public class JacksonJsonFactory implements JsonFactory {
    public JacksonJsonFactory() throws ReflectiveOperationException {
        JacksonJsonReader.initialize();
        JacksonJsonWriter.initialize();
    }

    @Override
    public JsonReader getJsonReader(byte[] bytes, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromBytes(bytes, options);
    }

    @Override
    public JsonReader getJsonReader(String string, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromString(string, options);
    }

    @Override
    public JsonReader getJsonReader(InputStream stream, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromStream(stream, options);
    }

    @Override
    public JsonReader getJsonReader(Reader reader, JsonOptions options) throws IOException {
        return JacksonJsonReader.fromReader(reader, options);
    }

    @Override
    public JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toStream(stream, options);
    }

    @Override
    public JsonWriter getJsonWriter(Writer writer, JsonOptions options) throws IOException {
        return JacksonJsonWriter.toWriter(writer, options);
    }

    @Override
    public String getLibraryInfo() {
        return null;
    }
}
