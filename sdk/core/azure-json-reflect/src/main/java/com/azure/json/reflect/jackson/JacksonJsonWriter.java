package com.azure.json.reflect.jackson;

import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class JacksonJsonWriter extends JsonWriter {
    @Override
    public JsonWriteContext getWriteContext() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public JsonWriter flush() {
        return null;
    }

    @Override
    public JsonWriter writeStartObject() {
        return null;
    }

    @Override
    public JsonWriter writeEndObject() {
        return null;
    }

    @Override
    public JsonWriter writeStartArray() {
        return null;
    }

    @Override
    public JsonWriter writeEndArray() {
        return null;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) {
        return null;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) {
        return null;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) {
        return null;
    }

    @Override
    public JsonWriter writeDouble(double value) {
        return null;
    }

    @Override
    public JsonWriter writeFloat(float value) {
        return null;
    }

    @Override
    public JsonWriter writeInt(int value) {
        return null;
    }

    @Override
    public JsonWriter writeLong(long value) {
        return null;
    }

    @Override
    public JsonWriter writeNull() {
        return null;
    }

    @Override
    public JsonWriter writeString(String value) {
        return null;
    }

    @Override
    public JsonWriter writeRawValue(String value) {
        return null;
    }
}
