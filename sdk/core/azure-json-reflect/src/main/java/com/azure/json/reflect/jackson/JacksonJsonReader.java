package com.azure.json.reflect.jackson;


import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.io.Reader;

public class JacksonJsonReader extends JsonReader {
    public JacksonJsonReader(Reader reader) {

    }

    @Override
    public JsonToken currentToken() {
        return null;
    }

    @Override
    public JsonToken nextToken() {
        return null;
    }

    @Override
    public byte[] getBinary() {
        return new byte[0];
    }

    @Override
    public boolean getBoolean() {
        return false;
    }

    @Override
    public float getFloat() {
        return 0;
    }

    @Override
    public double getDouble() {
        return 0;
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public long getLong() {
        return 0;
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public String getFieldName() {
        return null;
    }

    @Override
    public void skipChildren() {

    }

    @Override
    public JsonReader bufferObject() {
        return null;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public JsonReader reset() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
