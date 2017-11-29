package com.microsoft.rest.v2.http;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class InputStreamHttpRequestBodyTests {
    @Test
    public void constructor() {
        final InputStream content = new ByteArrayInputStream(new byte[10]);
        final InputStreamHttpRequestBody body = new InputStreamHttpRequestBody(10, "application/octet-stream", content);
        assertEquals(10, body.contentLength());
        assertEquals("application/octet-stream", body.contentType());
        assertSame(content, body.createInputStream());
    }

    @Test
    public void buffer() throws IOException {
        final byte[] bytes = new byte[] {0, 1, 2, 3, 4};
        final InputStreamHttpRequestBody body = new InputStreamHttpRequestBody(bytes.length, "application/octet-stream", new ByteArrayInputStream(bytes));
        final HttpRequestBody bufferedBody = body.buffer();
        assertTrue(bufferedBody instanceof ByteArrayHttpRequestBody);
        assertEquals(bytes.length, bufferedBody.contentLength());
        assertEquals("application/octet-stream", bufferedBody.contentType());
        assertArrayEquals(bytes, ByteStreams.toByteArray(bufferedBody.createInputStream()));
    }
}
