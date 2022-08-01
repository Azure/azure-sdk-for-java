package com.azure.json.reflect.gson;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

public class JsonGsonReader extends JsonReader {
    @Override
    public JsonToken currentToken() {
        return null;
    }

    @Override
    public JsonToken nextToken() {
        return null;
    }

    @Override
    public byte[] getBinaryValue() {
        return new byte[0];
    }

    @Override
    public boolean getBooleanValue() {
        return false;
    }

    @Override
    public float getFloatValue() {
        return 0;
    }

    @Override
    public double getDoubleValue() {
        return 0;
    }

    @Override
    public int getIntValue() {
        return 0;
    }

    @Override
    public long getLongValue() {
        return 0;
    }

    @Override
    public String getStringValue() {
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
