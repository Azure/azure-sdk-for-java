// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.core.test.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that {@link AzureSeekableByteChannel} reads into and writes from only the bytes between a
 * buffer's position and limit, for every kind of buffer (see {@link TestBufferShape}).
 */
public class AzureSeekableByteChannelBufferShapeTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    // Any path on an open file system; the channel only checks that the path's file system is open.
    private static final Path PATH = Paths.get("blob");
    private static final int DATA_LENGTH = 10_000;
    private static final int READ_BLOCK_SIZE = 4096;
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void writeUploadsOnlyExposedBytes(TestBufferShape shape) throws IOException {
        byte[] content = randomBytes(DATA_LENGTH, 1);
        ShapedByteBuffer src = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(null);

        AzureSeekableByteChannel channel = writeChannel(httpClient);
        assertEquals(DATA_LENGTH, channel.write(src.getBuffer()));
        channel.close();

        assertArraysEqual(content, httpClient.getUploadedContent());
        // The source buffer is consumed, but its data is not modified.
        src.assertNoBytesChanged();
        src.assertPositionAndLimit(src.getOriginalLimit(), src.getOriginalLimit());
    }

    @Test
    public void writeUploadsOnlyExposedBytesOfMixedBuffers() throws IOException {
        byte[] content = randomBytes(DATA_LENGTH, 2);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 1000);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(null);

        AzureSeekableByteChannel channel = writeChannel(httpClient);
        for (ShapedByteBuffer src : sources) {
            assertEquals(src.getLength(), channel.write(src.getBuffer()));
        }
        channel.close();

        assertArraysEqual(content, httpClient.getUploadedContent());
        for (ShapedByteBuffer src : sources) {
            src.assertNoBytesChanged();
        }
    }

    @ParameterizedTest
    @EnumSource(value = TestBufferShape.class, names = "READ_ONLY", mode = EnumSource.Mode.EXCLUDE)
    public void readFillsOnlyExposedBytes(TestBufferShape shape) throws IOException {
        byte[] blobContent = randomBytes(DATA_LENGTH, 3);
        ShapedByteBuffer dst = shape.allocate(DATA_LENGTH);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(blobContent);

        assertEquals(DATA_LENGTH, readChannel(httpClient).read(dst.getBuffer()));

        assertArraysEqual(blobContent, dst.getExposedBytes());
        dst.assertOnlyExposedBytesChanged();
        dst.assertPositionAndLimit(dst.getOriginalLimit(), dst.getOriginalLimit());
    }

    @Test
    public void readFillsOnlyExposedBytesOfMixedBuffers() throws IOException {
        byte[] blobContent = randomBytes(DATA_LENGTH, 4);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(blobContent);
        AzureSeekableByteChannel channel = readChannel(httpClient);

        List<TestBufferShape> shapes = TestBufferShape.writableShapes();
        int pieceLength = 1000;
        for (int offset = 0, i = 0; offset < DATA_LENGTH; offset += pieceLength, i++) {
            int length = Math.min(pieceLength, DATA_LENGTH - offset);
            ShapedByteBuffer dst = shapes.get(i % shapes.size()).allocate(length);

            assertEquals(length, channel.read(dst.getBuffer()));

            assertArraysEqual(Arrays.copyOfRange(blobContent, offset, offset + length), dst.getExposedBytes());
            dst.assertOnlyExposedBytesChanged();
        }
    }

    private static AzureSeekableByteChannel writeChannel(HttpClient httpClient) {
        return new AzureSeekableByteChannel(
            new NioBlobOutputStream(blobClient(httpClient).getBlockBlobClient().getBlobOutputStream(true), PATH), PATH);
    }

    private static AzureSeekableByteChannel readChannel(HttpClient httpClient) {
        return new AzureSeekableByteChannel(new NioBlobInputStream(
            blobClient(httpClient).openInputStream(new BlobInputStreamOptions().setBlockSize(READ_BLOCK_SIZE)), PATH),
            PATH);
    }

    private static byte[] randomBytes(int length, long seed) {
        byte[] bytes = new byte[length];
        new Random(seed).nextBytes(bytes);
        return bytes;
    }

    private static BlobClient blobClient(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildClient();
    }

    /*
     * Serves range downloads of the given blob content, and captures the content of an upload.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final byte[] blobContent;
        private byte[] uploadedContent;

        RequestCapturingHttpClient(byte[] blobContent) {
            this.blobContent = blobContent;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() == HttpMethod.GET) {
                Matcher matcher = RANGE_PATTERN.matcher(request.getHeaders().getValue(X_MS_RANGE));
                assertTrue(matcher.matches());
                int start = Integer.parseInt(matcher.group(1));
                int end = Math.min(Integer.parseInt(matcher.group(2)), blobContent.length - 1);
                byte[] body = Arrays.copyOfRange(blobContent, start, end + 1);
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"0x8D000000000000\"")
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.length))
                    .set(HttpHeaderName.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + blobContent.length)
                    .set(HttpHeaderName.fromString("x-ms-blob-type"), "BlockBlob");
                return Mono.just(new MockHttpResponse(request, 206, headers, body));
            } else if (request.getHttpMethod() == HttpMethod.PUT) {
                String query = request.getUrl().getQuery();
                if (query != null && query.contains("comp=")) {
                    return Mono.error(new IllegalStateException("Expected a single Put Blob request: " + query));
                }
                return FluxUtil.collectBytesInByteBufferStream(request.getBody()).map(bytes -> {
                    synchronized (this) {
                        uploadedContent = bytes;
                    }
                    return new MockHttpResponse(request, 201);
                });
            }
            return Mono.error(new IllegalStateException("Unexpected request: " + request.getHttpMethod()));
        }

        synchronized byte[] getUploadedContent() {
            return uploadedContent;
        }
    }
}
