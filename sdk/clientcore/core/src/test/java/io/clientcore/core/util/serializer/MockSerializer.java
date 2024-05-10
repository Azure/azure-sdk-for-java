// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public class MockSerializer implements JsonSerializer {
    @Override
    public <T> T deserializeFromBytes(byte[] data, Type type) {
        return null;
    }

    @Override
    public <T> T deserializeFromStream(InputStream stream, Type type) {
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
