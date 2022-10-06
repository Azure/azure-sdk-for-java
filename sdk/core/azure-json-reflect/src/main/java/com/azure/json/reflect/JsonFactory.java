package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface JsonFactory {
    JsonReader getJsonReader(byte[] bytes, JsonOptions options);
    JsonReader getJsonReader(String string, JsonOptions options);
    JsonReader getJsonReader(InputStream stream, JsonOptions options);
    JsonReader getJsonReader(Reader reader, JsonOptions options);
    JsonWriter getJsonWriter(OutputStream stream, JsonOptions options);
    JsonWriter getJsonWriter(Writer writer, JsonOptions options);
    String getLibraryInfo();
}
