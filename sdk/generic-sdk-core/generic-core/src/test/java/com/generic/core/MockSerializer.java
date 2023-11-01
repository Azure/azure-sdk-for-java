package com.generic.core;

import com.generic.core.models.TypeReference;
import com.generic.core.util.serializer.JsonSerializer;

import java.io.InputStream;
import java.io.OutputStream;

public class MockSerializer implements JsonSerializer {
    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        return null;
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
    }
}
