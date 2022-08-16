package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

import java.io.Reader;

public interface JsonFactory {
    JsonReader getJsonReader(Reader reader);
    JsonWriter getJsonWriter();
    String getLibraryInfo();
}
