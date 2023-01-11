package com.azure.core.implementation.util;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ListByteBufferInputStreamTests extends MultipleByteBufferBackedInputStreamTest {
    @Override
    public InputStream makeStream(List<ByteBuffer> content) {
        return new ListByteBufferInputStream(content);
    }
}
