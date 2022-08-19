package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

import java.io.InputStream;
import java.io.OutputStream;

public interface JsonFactory {
    JsonReader getJsonReader(byte[] bytes);
    JsonReader getJsonReader(String string);
    JsonReader getJsonReader(InputStream stream);
    JsonWriter getJsonWriter(OutputStream stream);
    String getLibraryInfo();
}
