package com.generic.core.util.serializer;

import com.generic.core.models.Headers;
import com.generic.core.models.TypeReference;
import com.generic.core.util.serializer.JsonSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

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
    public <T> T deserialize(Headers headers, Type type) throws IOException {
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
