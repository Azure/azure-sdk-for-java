// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.serializer;

import com.generic.core.models.TypeReference;

import java.io.InputStream;
import java.io.OutputStream;

public class MockSerializer implements JsonSerializer {
    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public <T> T deserializeFromStream(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        return null;
    }

    @Override
    public void serializeToStream(OutputStream stream, Object value) {
    }
}
