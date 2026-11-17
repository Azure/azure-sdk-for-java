// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests how the downloadContent APIs buffer the body of a download. A mock {@link HttpClient} controls how the body is
 * split into network buffers and what Content-Length the response reports.
 */
public class BlobDownloadContentBufferingTests {
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final int CHUNK_SIZE = 1000;
    private static final int DATA_LENGTH = 100 * CHUNK_SIZE + 7;
    private static final byte[] DATA = createData(DATA_LENGTH);
    private static final int FAILURE_OFFSET = 40 * CHUNK_SIZE + 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Test
    public void downloadContentBuffersIntoOneArray() {
        BlobClient client = syncClient(new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), -1));

        assertBufferedInOneArray(client.downloadContent(), DATA);
        assertBufferedInOneArray(
            client.downloadContentWithResponse(new BlobDownloadContentOptions(), null, Context.NONE).getValue(), DATA);
    }

    @Test
    public void downloadContentBuffersIntoOneArrayAsync() {
        BlobAsyncClient client = asyncClient(new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), -1));

        StepVerifier.create(client.downloadContent())
            .assertNext(content -> assertBufferedInOneArray(content, DATA))
            .expectComplete()
            .verify(TIMEOUT);
        StepVerifier.create(client.downloadContentWithResponse(new BlobDownloadContentOptions()))
            .assertNext(response -> assertBufferedInOneArray(response.getValue(), DATA))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void downloadContentResumesIntoTheSameArray() {
        BlobBodyHttpClient httpClient = new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), FAILURE_OFFSET);

        BinaryData content = syncClient(httpClient).downloadContent();

        assertBufferedInOneArray(content, DATA);
        assertEquals(2, httpClient.requestedRanges.size());
        assertEquals("bytes=" + FAILURE_OFFSET + "-" + (DATA_LENGTH - 1), httpClient.requestedRanges.get(1));
    }

    @Test
    public void downloadContentResumesIntoTheSameArrayAsync() {
        BlobBodyHttpClient httpClient = new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), FAILURE_OFFSET);

        StepVerifier.create(asyncClient(httpClient).downloadContent())
            .assertNext(content -> assertBufferedInOneArray(content, DATA))
            .expectComplete()
            .verify(TIMEOUT);
        assertEquals(2, httpClient.requestedRanges.size());
        assertEquals("bytes=" + FAILURE_OFFSET + "-" + (DATA_LENGTH - 1), httpClient.requestedRanges.get(1));
    }

    @Test
    public void downloadContentFailsWhenBodyFails() {
        BlobClient client = syncClient(new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), FAILURE_OFFSET));
        BlobDownloadContentOptions options = new BlobDownloadContentOptions()
            .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(0));

        // The body is read before the call returns, so failing to read it fails the call.
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> client.downloadContentWithResponse(options, null, Context.NONE));
        assertInstanceOf(IOException.class, Exceptions.unwrap(e));
    }

    @Test
    public void downloadContentFailsWhenBodyFailsAsync() {
        BlobAsyncClient client = asyncClient(new BlobBodyHttpClient(String.valueOf(DATA_LENGTH), FAILURE_OFFSET));
        BlobDownloadContentOptions options = new BlobDownloadContentOptions()
            .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(0));

        StepVerifier.create(client.downloadContentWithResponse(options)).expectError(IOException.class).verify(TIMEOUT);
    }

    @Test
    public void downloadContentEmptyBody() {
        BlobClient client = syncClient(new BlobBodyHttpClient(new byte[0], "0", -1));

        assertReplayableContent(client.downloadContent(), new byte[0]);
    }

    @Test
    public void downloadContentEmptyBodyAsync() {
        BlobAsyncClient client = asyncClient(new BlobBodyHttpClient(new byte[0], "0", -1));

        StepVerifier.create(client.downloadContent())
            .assertNext(content -> assertReplayableContent(content, new byte[0]))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void downloadContentWithoutContentLength() {
        BlobClient client = syncClient(new BlobBodyHttpClient(null, -1));
        BlobDownloadContentOptions options
            = new BlobDownloadContentOptions().setRange(new BlobRange(0, (long) DATA_LENGTH));

        assertReplayableContent(client.downloadContentWithResponse(options, null, Context.NONE).getValue(), DATA);
    }

    @Test
    public void downloadContentWithoutContentLengthAsync() {
        BlobAsyncClient client = asyncClient(new BlobBodyHttpClient(null, -1));
        BlobDownloadContentOptions options
            = new BlobDownloadContentOptions().setRange(new BlobRange(0, (long) DATA_LENGTH));

        StepVerifier.create(client.downloadContentWithResponse(options))
            .assertNext(response -> assertReplayableContent(response.getValue(), DATA))
            .expectComplete()
            .verify(TIMEOUT);
    }

    // The body can differ from Content-Length, for example after client-side decryption, or be too large for an
    // array. Either way the content returned must be exactly the bytes received.
    @ParameterizedTest
    @ValueSource(longs = { DATA_LENGTH + 100L, DATA_LENGTH - 100L, 3_000_000_000L })
    public void downloadContentWhenContentLengthDoesNotMatchBody(long contentLength) {
        BlobClient client = syncClient(new BlobBodyHttpClient(String.valueOf(contentLength), -1));

        assertReplayableContent(client.downloadContent(), DATA);
    }

    @ParameterizedTest
    @ValueSource(longs = { DATA_LENGTH + 100L, DATA_LENGTH - 100L, 3_000_000_000L })
    public void downloadContentWhenContentLengthDoesNotMatchBodyAsync(long contentLength) {
        BlobAsyncClient client = asyncClient(new BlobBodyHttpClient(String.valueOf(contentLength), -1));

        StepVerifier.create(client.downloadContent())
            .assertNext(content -> assertReplayableContent(content, DATA))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /*
     * A body collected into an array of exactly Content-Length is held as that one array, which every read returns
     * without copying.
     */
    private static void assertBufferedInOneArray(BinaryData content, byte[] expected) {
        assertReplayableContent(content, expected);
        assertEquals(1, readBuffered(content.toFluxByteBuffer()).size());
        assertSame(content.toBytes(), content.toBytes());
    }

    /*
     * Downloaded content is fully buffered: it can be read any number of times, in any form, and reading never waits
     * on the network.
     */
    private static void assertReplayableContent(BinaryData content, byte[] expected) {
        assertTrue(content.isReplayable());
        assertEquals((long) expected.length, content.getLength());
        TestUtils.assertArraysEqual(expected, content.toBytes());
        TestUtils.assertArraysEqual(expected, readFully(content.toStream()));
        TestUtils.assertArraysEqual(expected, concat(readBuffered(content.toFluxByteBuffer())));
        TestUtils.assertArraysEqual(expected, content.toBytes());
    }

    /*
     * Buffered content is emitted, and completes, while subscribing.
     */
    private static List<ByteBuffer> readBuffered(Flux<ByteBuffer> content) {
        List<ByteBuffer> buffers = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean();
        content.subscribe(buffers::add, error -> {
            throw new IllegalStateException(error);
        }, () -> completed.set(true));
        assertTrue(completed.get(), "Reading the content didn't complete while subscribing.");
        return buffers;
    }

    private static byte[] concat(List<ByteBuffer> buffers) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            out.write(bytes, 0, bytes.length);
        }
        return out.toByteArray();
    }

    private static byte[] readFully(InputStream stream) {
        try (InputStream in = stream) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] createData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 251);
        }
        return data;
    }

    private static BlobClient syncClient(HttpClient httpClient) {
        return builder(httpClient).buildClient();
    }

    private static BlobAsyncClient asyncClient(HttpClient httpClient) {
        return builder(httpClient).buildAsyncClient();
    }

    private static BlobClientBuilder builder(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net")
            .containerName("container")
            .blobName("blob")
            .credential(new StorageSharedKeyCredential("accountName", "accountKey"))
            .httpClient(httpClient);
    }

    /**
     * Serves one blob whose body arrives in {@link #CHUNK_SIZE} network buffers. The first response reports the
     * configured Content-Length (or none) and can drop the connection part way through the body; later responses are
     * the ranged requests the client sends to resume and report their real length.
     */
    private static final class BlobBodyHttpClient implements HttpClient {
        private final byte[] body;
        private final String firstContentLength;
        private final int failFirstResponseAfter;
        private final List<String> requestedRanges = new CopyOnWriteArrayList<>();

        BlobBodyHttpClient(String firstContentLength, int failFirstResponseAfter) {
            this(DATA, firstContentLength, failFirstResponseAfter);
        }

        BlobBodyHttpClient(byte[] body, String firstContentLength, int failFirstResponseAfter) {
            this.body = body;
            this.firstContentLength = firstContentLength;
            this.failFirstResponseAfter = failFirstResponseAfter;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String range = request.getHeaders().getValue(X_MS_RANGE);
            boolean firstResponse = requestedRanges.isEmpty();
            requestedRanges.add(String.valueOf(range));

            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8DTESTETAG\"")
                .set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            int start = 0;
            int end = body.length - 1;
            int statusCode = 200;
            if (range != null) {
                String[] bounds = range.substring("bytes=".length()).split("-");
                start = Integer.parseInt(bounds[0]);
                if (bounds.length > 1) {
                    end = Math.min(end, Integer.parseInt(bounds[1]));
                }
                statusCode = 206;
                headers.set(HttpHeaderName.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + body.length);
            }
            int length = end - start + 1;
            String contentLength = firstResponse ? firstContentLength : String.valueOf(length);
            if (contentLength != null) {
                headers.set(HttpHeaderName.CONTENT_LENGTH, contentLength);
            }

            Flux<ByteBuffer> responseBody = (firstResponse && failFirstResponseAfter >= 0)
                ? chunks(start, failFirstResponseAfter).concatWith(Flux.error(new IOException("Connection dropped.")))
                : chunks(start, length);
            return Mono.just(new MockHttpResponse(request, statusCode, headers) {
                @Override
                public Flux<ByteBuffer> getBody() {
                    return responseBody;
                }
            });
        }

        private Flux<ByteBuffer> chunks(int start, int length) {
            int count = (length + CHUNK_SIZE - 1) / CHUNK_SIZE;
            return Flux.range(0, count).map(i -> {
                int offset = start + i * CHUNK_SIZE;
                return ByteBuffer.wrap(body, offset, Math.min(CHUNK_SIZE, start + length - offset));
            });
        }
    }
}
