// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP/2 tests shared by the ClientCore HTTP transports.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public abstract class Http2ExpectContinueTests {
    private static final byte[] BODY = new byte[8193];

    /**
     * Gets an HTTP/2 client configured to trust the local test server.
     *
     * @return The HTTP client.
     */
    protected abstract HttpClient getHttpClient();

    /**
     * Whether the transport forwards other expectations alongside {@code 100-continue}.
     *
     * @return Whether other expectations are forwarded.
     */
    protected boolean forwardsOtherExpectations() {
        return true;
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void withholdsHttp2BodyUntilContinue(boolean knownLength) {
        verifyHttp2Continue(knownLength, false, false);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void withholdsHttp2BodyUntilContinueWithEmptyFinalResponse(boolean knownLength) {
        verifyHttp2Continue(knownLength, true, false);
    }

    @Test
    public void preservesMultipleHttp2Expectations() {
        verifyHttp2Continue(true, false, true);
    }

    private void verifyHttp2Continue(boolean knownLength, boolean emptyResponse, boolean multipleExpectations) {
        AtomicInteger reads = new AtomicInteger();
        AtomicInteger readsBeforeContinue = new AtomicInteger(-1);
        AtomicReference<byte[]> received = new AtomicReference<>();
        AtomicReference<String> protocol = new AtomicReference<>();
        AtomicReference<String> expectations = new AtomicReference<>();
        HttpServlet servlet
            = uploadServlet(reads, readsBeforeContinue, received, protocol, expectations, emptyResponse);
        LocalTestServer server = new LocalTestServer(HttpProtocolVersion.HTTP_2, true, servlet);
        server.start();
        try {
            HttpRequest request = request(server, reads, knownLength);
            if (multipleExpectations) {
                request.getHeaders().set(HttpHeaderName.EXPECT, "other");
                request.getHeaders().add(HttpHeaderName.EXPECT, "100-CONTINUE");
                request.getHeaders().add(HttpHeaderName.EXPECT, "another");
            }
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
                assertArrayEquals(emptyResponse ? new byte[0] : new byte[] { 7 }, response.getValue().toBytes());
            }
            assertEquals("HTTP/2.0", protocol.get());
            assertEquals(2, expectations.get().toLowerCase(Locale.ROOT).split("100-continue", -1).length);
            if (multipleExpectations && forwardsOtherExpectations()) {
                assertTrue(expectations.get().contains("other"));
                assertTrue(expectations.get().contains("another"));
            }
            assertEquals(0, readsBeforeContinue.get(), "The body was read before the server sent 100 Continue.");
            assertArrayEquals(BODY, received.get());
            assertTrue(reads.get() > 0);
        } finally {
            server.stop();
        }
    }

    private static HttpServlet uploadServlet(AtomicInteger reads, AtomicInteger readsBeforeContinue,
        AtomicReference<byte[]> received, AtomicReference<String> protocol, AtomicReference<String> expectations,
        boolean emptyResponse) {
        return new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                protocol.set(request.getProtocol());
                expectations.set(String.join(",", Collections.list(request.getHeaders("expect"))));
                pause(250);
                readsBeforeContinue.set(reads.get());
                received.set(readAll(request.getInputStream()));
                response.setStatus(201);
                if (emptyResponse) {
                    response.setContentLength(0);
                } else {
                    response.setContentLength(1);
                    response.getOutputStream().write(7);
                }
            }
        };
    }

    @Test
    public void finalHeadersStopUploadEvenWhenResponseBodyIsDelayed() {
        AtomicInteger reads = new AtomicInteger();
        AtomicReference<String> protocol = new AtomicReference<>();
        LocalTestServer server = new LocalTestServer(HttpProtocolVersion.HTTP_2, true, earlyResponseServlet(protocol));
        server.start();
        try {
            try (Response<BinaryData> response = getHttpClient().send(request(server, reads, false))) {
                assertEquals(417, response.getStatusCode());
                assertArrayEquals(new byte[] { 7 }, response.getValue().toBytes());
            }
            assertEquals("HTTP/2.0", protocol.get());
            assertEquals(0, reads.get(), "A final response must prevent the request body from being read.");
            try (Response<BinaryData> response = getHttpClient()
                .send(new HttpRequest().setMethod(HttpMethod.GET).setUri(server.getHttpsUri() + "/next"))) {
                assertEquals(204, response.getStatusCode());
            }
        } finally {
            server.stop();
        }
    }

    private static HttpServlet earlyResponseServlet(AtomicReference<String> protocol) {
        return new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                if ("/next".equals(request.getRequestURI())) {
                    response.setStatus(204);
                    return;
                }
                protocol.set(request.getProtocol());
                response.setStatus(417);
                response.setContentLength(1);
                response.flushBuffer();
                pause(1500);
                response.getOutputStream().write(7);
            }
        };
    }

    private static HttpRequest request(LocalTestServer server, AtomicInteger reads, boolean knownLength) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri(server.getHttpsUri())
            .setBody(BinaryData.fromStream(new CountingInputStream(reads), knownLength ? (long) BODY.length : null));
        request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");
        return request;
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = input.read(buffer);
        while (count != -1) {
            output.write(buffer, 0, count);
            count = input.read(buffer);
        }
        return output.toByteArray();
    }

    private static void pause(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while handling the request.", ex);
        }
    }

    private static final class CountingInputStream extends ByteArrayInputStream {
        private final AtomicInteger reads;

        private CountingInputStream(AtomicInteger reads) {
            super(BODY);
            this.reads = reads;
        }

        @Override
        public synchronized int read() {
            reads.incrementAndGet();
            return super.read();
        }

        @Override
        public synchronized int read(byte[] bytes, int offset, int length) {
            reads.incrementAndGet();
            return super.read(bytes, offset, length);
        }
    }
}
