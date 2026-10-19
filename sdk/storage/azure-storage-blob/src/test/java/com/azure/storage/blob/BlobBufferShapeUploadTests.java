// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that blob uploads send only the bytes between each source buffer's position and limit, and
 * don't modify the source buffers, for every kind of buffer (see {@link TestBufferShape}).
 */
public class BlobBufferShapeUploadTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    // Small sizes keep the tests fast; the partitioning logic doesn't depend on the absolute sizes.
    private static final int BLOCK_SIZE = 1024;
    private static final int PAGE_SIZE = 512;
    private static final Pattern LATEST_BLOCK_PATTERN = Pattern.compile("<Latest>([^<]+)</Latest>");

    @ParameterizedTest
    @MethodSource("partitionedUploadSupplier")
    public void partitionedUploadSendsOnlyExposedBytes(TestBufferShape shape, int length, int expectedStagedBlocks) {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobClient(httpClient).uploadWithResponse(
            new BlobParallelUploadOptions(Flux.just(source.getBuffer())).setParallelTransferOptions(blockOptions()),
            null, null);

        assertEquals(expectedStagedBlocks, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        source.assertUnchanged();
    }

    private static Stream<Arguments> partitionedUploadSupplier() {
        return Stream.of(TestBufferShape.values())
            .flatMap(shape -> Stream.of(
                // shape | length | expected staged blocks
                Arguments.of(shape, BLOCK_SIZE / 2, 0), // A single request.
                Arguments.of(shape, BLOCK_SIZE, 0), // A single request, exactly the threshold.
                Arguments.of(shape, 2 * BLOCK_SIZE + 100, 3), // Staged, with a partial last block.
                Arguments.of(shape, 3 * BLOCK_SIZE, 3))); // Staged, an exact multiple of the block size.
    }

    @Test
    public void partitionedUploadSendsOnlyExposedBytesOfMixedBuffers() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        // Pieces that don't align with blocks, so blocks span buffers of different kinds.
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobClient(httpClient).uploadWithResponse(
            new BlobParallelUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(blockOptions()), null,
            null);

        assertEquals(6, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void uploadingSameBuffersAgainSendsSameContent() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));

        for (int upload = 1; upload <= 2; upload++) {
            RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

            blobClient(httpClient).uploadWithResponse(
                new BlobParallelUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(blockOptions()),
                null, null);

            TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        }
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void retryingUploadBySubscribingAgainSendsSameContent() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        // The first commit fails, so the upload is retried by subscribing to it again, which re-reads the buffers.
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(1);

        blobAsyncClient(httpClient)
            .uploadWithResponse(
                new BlobParallelUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(blockOptions()))
            .retry(1)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void putBlobSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getBlockBlobAsyncClient()
            .upload(Flux.just(source.getBuffer()), content.length, true)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody(null));
        source.assertUnchanged();
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void stageBlockSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        String blockId = Base64.getEncoder().encodeToString("block".getBytes(StandardCharsets.UTF_8));

        blobAsyncClient(httpClient).getBlockBlobAsyncClient()
            .stageBlock(blockId, Flux.just(source.getBuffer()), content.length)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("block"));
        source.assertUnchanged();
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void appendBlockSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getAppendBlobAsyncClient()
            .appendBlock(Flux.just(source.getBuffer()), content.length)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("appendblock"));
        source.assertUnchanged();
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void uploadPagesSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(2 * PAGE_SIZE);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getPageBlobAsyncClient()
            .uploadPages(new PageRange().setStart(0).setEnd(content.length - 1), Flux.just(source.getBuffer()))
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("page"));
        source.assertUnchanged();
    }

    private static ParallelTransferOptions blockOptions() {
        return new ParallelTransferOptions().setBlockSizeLong((long) BLOCK_SIZE)
            .setMaxSingleUploadSizeLong((long) BLOCK_SIZE);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static BlobClient blobClient(HttpClient httpClient) {
        return builder(httpClient).buildClient();
    }

    private static BlobAsyncClient blobAsyncClient(HttpClient httpClient) {
        return builder(httpClient).buildAsyncClient();
    }

    private static BlobClientBuilder builder(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
    }

    /*
     * Captures the content of each PUT request, by its "comp" query parameter, and the content of staged blocks.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<String, byte[]> bodies = new HashMap<>();
        private final Map<String, byte[]> stagedBlocks = new HashMap<>();
        private int failedCommitsRemaining;

        RequestCapturingHttpClient() {
            this(0);
        }

        RequestCapturingHttpClient(int failedCommits) {
            this.failedCommitsRemaining = failedCommits;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() != HttpMethod.PUT) {
                return Mono.error(new IllegalStateException("Unexpected request: " + request.getHttpMethod()));
            }

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody().map(ByteBuffer::duplicate));
            return body.map(bytes -> {
                String comp = getQueryParameter(request, "comp");
                synchronized (this) {
                    if ("blocklist".equals(comp) && failedCommitsRemaining > 0) {
                        // A failure the SDK doesn't retry itself, so the caller has to retry the whole upload.
                        failedCommitsRemaining--;
                        return new MockHttpResponse(request, 400);
                    }
                    bodies.put(comp, bytes);
                    if ("block".equals(comp)) {
                        stagedBlocks.put(getQueryParameter(request, "blockid"), bytes);
                    }
                }
                // Some upload responses must say whether the content was encrypted by the service.
                return new MockHttpResponse(request, 201,
                    new HttpHeaders().set(HttpHeaderName.fromString("x-ms-request-server-encrypted"), "true"));
            });
        }

        /*
         * Gets the content of the last request with the given "comp" query parameter, or of the last Put Blob request
         * when null.
         */
        synchronized byte[] getBody(String comp) {
            return bodies.get(comp);
        }

        synchronized int getStagedBlockCount() {
            return stagedBlocks.size();
        }

        /*
         * Gets the content of the blob as it would be after the upload, from either the Put Blob request or the staged
         * blocks in the order they were committed.
         */
        synchronized byte[] getBlobContent() {
            String commitBody
                = bodies.containsKey("blocklist") ? new String(bodies.get("blocklist"), StandardCharsets.UTF_8) : null;
            if (commitBody == null) {
                return bodies.get(null);
            }
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            Matcher matcher = LATEST_BLOCK_PATTERN.matcher(commitBody);
            while (matcher.find()) {
                byte[] block = stagedBlocks.get(matcher.group(1));
                content.write(block, 0, block.length);
            }
            return content.toByteArray();
        }

        private static String getQueryParameter(HttpRequest request, String name) {
            String query = request.getUrl().getQuery();
            if (query == null) {
                return null;
            }
            for (String parameter : query.split("&")) {
                if (parameter.startsWith(name + "=")) {
                    try {
                        return URLDecoder.decode(parameter.substring(name.length() + 1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
            return null;
        }
    }
}
