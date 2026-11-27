// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.shared;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-level tests shared by the ClientCore HTTP transports.
 */
@Timeout(value = 20, unit = TimeUnit.SECONDS)
public abstract class ExpectContinueTests {
    private static final byte[] BODY = new byte[8193];

    /**
     * Gets the transport under test.
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
    @ValueSource(
        strings = { "100-continue", "100-Continue", "other, 100-CONTINUE", "other=\"a,100-continue,b\", 100-continue" })
    public void waitsForContinue(String expectation) throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.CONTINUE)) {
            HttpRequest request = request(server, BinaryData.fromBytes(BODY), expectation);
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            Result result = server.result();
            String unquoted = result.expectation.replace("other=\"a,100-continue,b\"", "");
            assertTrue(unquoted.toLowerCase(Locale.ROOT).contains("100-continue"),
                "Unexpected Expect header: " + result.expectation);
            assertEquals(2, unquoted.toLowerCase(Locale.ROOT).split("100-continue", -1).length,
                "Exactly one 100-continue expectation must be sent.");
            if (forwardsOtherExpectations() && expectation.startsWith("other")) {
                assertTrue(
                    result.expectation
                        .contains(expectation.startsWith("other=") ? "other=\"a,100-continue,b\"" : "other"),
                    "Unexpected Expect header: " + result.expectation);
            }
            assertFalse(result.bodyBeforeContinue, "Request content was sent before 100 Continue.");
            assertArrayEquals(BODY, result.body);
        }
    }

    @Test
    public void preservesSeparateExpectationValues() throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.CONTINUE)) {
            HttpRequest request = request(server, BinaryData.fromBytes(BODY), "other");
            request.getHeaders().add(HttpHeaderName.EXPECT, "100-CONTINUE");
            request.getHeaders().add(HttpHeaderName.EXPECT, "another");
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            Result result = server.result();
            assertEquals(2, result.expectation.toLowerCase(Locale.ROOT).split("100-continue", -1).length);
            if (forwardsOtherExpectations()) {
                assertTrue(result.expectation.contains("other"));
                assertTrue(result.expectation.contains("another"));
            }
            assertFalse(result.bodyBeforeContinue);
            assertArrayEquals(BODY, result.body);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void streamsOnlyAfterContinue(boolean knownLength) throws Exception {
        AtomicInteger reads = new AtomicInteger();
        try (ContinueServer server = new ContinueServer(Mode.CONTINUE)) {
            BinaryData body
                = BinaryData.fromStream(new CountingInputStream(BODY, reads), knownLength ? (long) BODY.length : null);
            HttpRequest request = request(server, body, "100-continue");
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            Result result = server.result();
            assertFalse(result.bodyBeforeContinue, "Request content was sent before 100 Continue.");
            assertArrayEquals(BODY, result.body);
            assertTrue(reads.get() > 0);
        }
    }

    @Test
    public void doesNotReadBodyAfterEarlyFinalResponse() throws Exception {
        AtomicInteger reads = new AtomicInteger();
        try (ContinueServer server = new ContinueServer(Mode.REJECT)) {
            HttpRequest request = request(server,
                BinaryData.fromStream(new CountingInputStream(BODY, reads), (long) BODY.length), "100-continue");
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(417, response.getStatusCode());
            }
            Result result = server.result();
            assertFalse(result.bodyBeforeContinue, "Request content was sent before the final response.");
            assertFalse(result.bodyAfterFinal, "Request content was sent after the final response.");
            assertEquals(0, reads.get(), "An early final response must not consume the request body.");
        }
    }

    @Test
    public void sendsFileAfterContinue(@TempDir Path directory) throws Exception {
        Path file = Files.write(directory.resolve("body.bin"), BODY);
        try (ContinueServer server = new ContinueServer(Mode.CONTINUE); BinaryData body = BinaryData.fromFile(file)) {
            try (Response<BinaryData> response = getHttpClient().send(request(server, body, "100-continue"))) {
                assertEquals(201, response.getStatusCode());
            }
            Result result = server.result();
            assertFalse(result.bodyBeforeContinue);
            assertArrayEquals(BODY, result.body);
        }
    }

    @Test
    public void doesNotOpenFileAfterEarlyFinalResponse(@TempDir Path directory) throws Exception {
        Path file = Files.write(directory.resolve("body.bin"), BODY);
        try (ContinueServer server = new ContinueServer(Mode.REJECT); BinaryData body = BinaryData.fromFile(file)) {
            Files.delete(file);
            try (Response<BinaryData> response = getHttpClient().send(request(server, body, "100-continue"))) {
                assertEquals(417, response.getStatusCode());
            }
            assertFalse(server.result().bodyAfterFinal);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void bodylessRequestDoesNotSendContinueExpectation(boolean emptyBody) throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.BODYLESS)) {
            HttpRequest request = request(server, emptyBody ? BinaryData.empty() : null, "100-continue");
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            assertTrue(server.result().expectation.isEmpty());
        }
    }

    @Test
    public void bodylessRequestPreservesOtherExpectations() throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.BODYLESS)) {
            HttpRequest request = request(server, null, "other, 100-continue");
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            assertEquals(forwardsOtherExpectations() ? "other" : "", server.result().expectation);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "other=\"a,100-continue,b\"" })
    public void sendsImmediatelyWithoutContinueExpectation(String expectation) throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.IMMEDIATE)) {
            HttpRequest request = request(server, BinaryData.fromBytes(BODY), expectation);
            try (Response<BinaryData> response = getHttpClient().send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            assertArrayEquals(BODY, server.result().body);
        }
    }

    /**
     * Verifies that a transport sends the request body if the server doesn't answer the expectation.
     *
     * @param client The client to test.
     * @throws Exception If the server or client fails.
     */
    public static void verifyContinueFallback(HttpClient client) throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.FALLBACK)) {
            HttpRequest request = request(server, BinaryData.fromBytes(BODY), "100-continue");
            try (Response<BinaryData> response = client.send(request)) {
                assertEquals(201, response.getStatusCode());
            }
            Result result = server.result();
            assertFalse(result.bodyBeforeContinue);
            assertArrayEquals(BODY, result.body);
        }
    }

    /**
     * Verifies that a response timeout while waiting for continue closes the request without reading its body.
     *
     * @param client The client to test.
     * @throws Exception If the server or client fails.
     */
    public static void verifyContinueTimeout(HttpClient client) throws Exception {
        AtomicInteger reads = new AtomicInteger();
        try (ContinueServer server = new ContinueServer(Mode.HOLD)) {
            HttpRequest request = request(server,
                BinaryData.fromStream(new CountingInputStream(BODY, reads), (long) BODY.length), "100-continue");
            assertThrows(CoreException.class, () -> client.send(request));
            Result result = server.result();
            assertTrue(result.expectation.equalsIgnoreCase("100-continue"));
            assertFalse(result.bodyBeforeContinue);
            assertEquals(0, reads.get());
        }
    }

    /**
     * Verifies that interrupting a request waiting for continue closes its connection.
     *
     * @param client The client to test.
     * @throws Exception If the server or client fails.
     */
    public static void verifyContinueInterruption(HttpClient client) throws Exception {
        AtomicInteger reads = new AtomicInteger();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (ContinueServer server = new ContinueServer(Mode.HOLD)) {
            HttpRequest request = request(server,
                BinaryData.fromStream(new CountingInputStream(BODY, reads), (long) BODY.length), "100-continue");
            Future<?> send = executor.submit(() -> client.send(request));
            assertTrue(server.headersReceived.await(5, TimeUnit.SECONDS));
            assertTrue(send.cancel(true));
            Result result = server.result();
            assertFalse(result.bodyBeforeContinue);
            assertEquals(0, reads.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /**
     * Verifies that an incomplete request isn't reused for a subsequent pooled request.
     *
     * @param client The client to test.
     * @throws Exception If the server or client fails.
     */
    public static void verifyConnectionAfterEarlyRejection(HttpClient client) throws Exception {
        try (ContinueServer server = new ContinueServer(Mode.REJECT_KEEP_ALIVE)) {
            try (Response<BinaryData> response
                = client.send(request(server, BinaryData.fromBytes(BODY), "100-continue"))) {
                assertEquals(417, response.getStatusCode());
            }
            try (Response<BinaryData> response
                = client.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(server.uri()))) {
                assertEquals(204, response.getStatusCode());
            }
            assertFalse(server.result().bodyAfterFinal, "The incomplete connection must not be reused.");
        }
    }

    private static HttpRequest request(ContinueServer server, BinaryData body, String expectation) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT).setUri(server.uri()).setBody(body);
        if (!expectation.isEmpty()) {
            request.getHeaders().set(HttpHeaderName.EXPECT, expectation);
        }
        return request;
    }

    private enum Mode {
        CONTINUE, REJECT, REJECT_KEEP_ALIVE, BODYLESS, IMMEDIATE, FALLBACK, HOLD
    }

    private static final class Result {
        private String expectation = "";
        private boolean bodyBeforeContinue;
        private boolean bodyAfterFinal;
        private byte[] body;
    }

    private static final class ContinueServer implements AutoCloseable {
        private final ServerSocket server;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final CountDownLatch headersReceived = new CountDownLatch(1);
        private final Future<Result> result;

        private ContinueServer(Mode mode) throws IOException {
            server = ServerSocketChannel.open()
                .bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 1)
                .socket();
            server.setSoTimeout(10000);
            result = executor.submit(() -> serve(mode));
        }

        private String uri() {
            return "http://127.0.0.1:" + server.getLocalPort() + "/";
        }

        private Result result() throws Exception {
            return result.get(10, TimeUnit.SECONDS);
        }

        private Result serve(Mode mode) throws IOException {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(5000);
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                List<String> headers = readHeaders(input);
                Result response = new Result();
                response.expectation = headerValue(headers, "expect");
                headersReceived.countDown();

                if (mode == Mode.BODYLESS) {
                    write(output, "HTTP/1.1 201 Created\r\nContent-Length: 0\r\nConnection: close\r\n\r\n");
                    return response;
                }

                if (mode == Mode.HOLD) {
                    response.bodyBeforeContinue = input.read() >= 0;
                    return response;
                }

                int firstByte = -1;
                if (mode != Mode.IMMEDIATE) {
                    socket.setSoTimeout(200);
                    try {
                        firstByte = input.read();
                        response.bodyBeforeContinue = firstByte >= 0;
                    } catch (SocketTimeoutException ignored) {
                        // No content is expected before the server decides whether to continue.
                    }
                    socket.setSoTimeout(5000);
                }

                if (mode == Mode.REJECT_KEEP_ALIVE) {
                    write(output, "HTTP/1.1 417 Expectation Failed\r\nContent-Length: 0\r\n\r\n");
                    response.bodyAfterFinal = input.read() >= 0;
                    if (response.bodyAfterFinal) {
                        write(output, "HTTP/1.1 500 Internal Server Error\r\nContent-Length: 0\r\n\r\n");
                    } else {
                        try (Socket next = server.accept()) {
                            next.setSoTimeout(5000);
                            readHeaders(next.getInputStream());
                            write(next.getOutputStream(),
                                "HTTP/1.1 204 No Content\r\nContent-Length: 0\r\nConnection: close\r\n\r\n");
                        }
                    }
                    return response;
                }

                if (mode == Mode.REJECT) {
                    write(output,
                        "HTTP/1.1 417 Expectation Failed\r\nContent-Length: 0\r\n" + "Connection: close\r\n\r\n");
                    socket.setSoTimeout(300);
                    try {
                        response.bodyAfterFinal = input.read() >= 0;
                    } catch (SocketTimeoutException ignored) {
                        // No content arrived after the final response.
                    }
                    return response;
                }

                if (mode == Mode.CONTINUE) {
                    write(output, "HTTP/1.1 100 Continue\r\n\r\n");
                } else if (mode == Mode.FALLBACK) {
                    firstByte = input.read();
                    // A late 100 must not cause a second body write.
                    write(output, "HTTP/1.1 100 Continue\r\n\r\n");
                }

                InputStream bodyInput = firstByte < 0
                    ? input
                    : new SequenceInputStream(new ByteArrayInputStream(new byte[] { (byte) firstByte }), input);
                response.body = "chunked".equalsIgnoreCase(headerValue(headers, "transfer-encoding"))
                    ? readChunkedBody(bodyInput)
                    : readExactly(bodyInput, Integer.parseInt(headerValue(headers, "content-length")));
                write(output, "HTTP/1.1 201 Created\r\nContent-Length: 0\r\nConnection: close\r\n\r\n");
                return response;
            }
        }

        @Override
        public void close() throws IOException {
            server.close();
            executor.shutdownNow();
            try {
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while stopping the server.", ex);
            }
        }
    }

    private static List<String> readHeaders(InputStream input) throws IOException {
        List<String> headers = new ArrayList<>();
        String line = readLine(input);
        while (!line.isEmpty()) {
            headers.add(line);
            line = readLine(input);
        }
        return headers;
    }

    private static String headerValue(List<String> headers, String name) {
        StringBuilder values = new StringBuilder();
        for (String line : headers) {
            int colon = line.indexOf(':');
            if (colon >= 0 && name.equalsIgnoreCase(line.substring(0, colon))) {
                if (values.length() > 0) {
                    values.append(',');
                }
                values.append(line.substring(colon + 1).trim());
            }
        }
        return values.toString();
    }

    private static byte[] readChunkedBody(InputStream input) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while (true) {
            String sizeLine = readLine(input);
            int semicolon = sizeLine.indexOf(';');
            int size = Integer.parseInt(semicolon < 0 ? sizeLine : sizeLine.substring(0, semicolon), 16);
            if (size == 0) {
                while (true) {
                    if (readLine(input).isEmpty()) {
                        return body.toByteArray();
                    }
                }
            }
            body.write(readExactly(input, size));
            if (!readLine(input).isEmpty()) {
                throw new IOException("Invalid chunk terminator.");
            }
        }
    }

    private static byte[] readExactly(InputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = input.read(bytes, offset, length - offset);
            if (count < 0) {
                throw new IOException("Unexpected end of request body.");
            }
            offset += count;
        }
        return bytes;
    }

    private static String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int previous = -1;
        int current;
        while ((current = input.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                byte[] bytes = line.toByteArray();
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.ISO_8859_1);
            }
            line.write(current);
            previous = current;
        }
        throw new IOException("Unexpected end of request headers.");
    }

    private static void write(OutputStream output, String value) throws IOException {
        output.write(value.getBytes(StandardCharsets.ISO_8859_1));
        output.flush();
    }

    private static final class CountingInputStream extends ByteArrayInputStream {
        private final AtomicInteger reads;

        private CountingInputStream(byte[] content, AtomicInteger reads) {
            super(content);
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
