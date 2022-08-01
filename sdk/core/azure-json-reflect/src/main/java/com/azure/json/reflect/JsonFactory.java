package com.azure.json.reflect;

import com.azure.json.JsonWriter;
import com.azure.json.JsonReader;

public interface JsonFactory {
    JsonReader getJsonReader();
    JsonWriter getJsonWriter();
}
