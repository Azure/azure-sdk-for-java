package com.azure.json.reflect;

import com.azure.json.JsonOptions;
import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

import java.io.*;

public interface JsonFactory {
    JsonReader getJsonReader(byte[] bytes, JsonOptions options) throws IOException;
    JsonReader getJsonReader(String string, JsonOptions options) throws IOException;
    JsonReader getJsonReader(InputStream stream, JsonOptions options) throws IOException;
    JsonReader getJsonReader(Reader reader, JsonOptions options) throws IOException;
    JsonWriter getJsonWriter(OutputStream stream, JsonOptions options) throws IOException;
    JsonWriter getJsonWriter(Writer writer, JsonOptions options) throws IOException;
    String getLibraryInfo();
}
