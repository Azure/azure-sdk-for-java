package com.microsoft.rest.v3.util;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class ByteBufferUtilTests {
    @Test
    public void toByteArrayWithNullByteBuffer()
    {
        assertNull(ByteBufferUtil.toByteArray(null));
    }

    @Test
    public void toByteArrayWithEmptyByteBuffer()
    {
        assertArrayEquals(new byte[0], ByteBufferUtil.toByteArray(ByteBuffer.wrap(new byte[0])));
    }

    @Test
    public void toByteArrayWithNonEmptyByteBuffer()
    {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
        assertEquals(5, byteBuffer.remaining());
        final byte[] byteArray = ByteBufferUtil.toByteArray(byteBuffer);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4 }, byteArray);
        assertEquals(0, byteBuffer.remaining());
        assertNotSame(byteBuffer.array(), byteArray);
    }
}
