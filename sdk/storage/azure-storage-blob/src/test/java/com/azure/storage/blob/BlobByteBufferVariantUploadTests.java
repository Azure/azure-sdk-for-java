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
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.test.shared.ByteBufferState;
import com.azure.storage.common.test.shared.TestByteBufferVariant;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
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
 * don't modify the source buffers, for every kind of buffer (see {@link TestByteBufferVariant}).
 */
public class BlobByteBufferVariantUploadTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    // Small sizes keep the tests fast; the partitioning logic doesn't depend on the absolute sizes.
    private static final int BLOCK_SIZE = 1024;
    private static final int PAGE_SIZE = 512;
    private static final Pattern LATEST_BLOCK_PATTERN = Pattern.compile("<Latest>([^<]+)</Latest>");

    /**
     * The ways to pass the caller's buffers to a partitioned upload. The sync and async clients share an
     * implementation today, but both are tested so they stay covered if their implementations diverge.
     */
    enum UploadMethod {
        SYNC_FLUX, ASYNC_FLUX, SYNC_BINARY_DATA, ASYNC_BINARY_DATA
    }

    @ParameterizedTest
    @MethodSource("partitionedUploadSupplier")
    public void partitionedUploadSendsOnlyExposedBytes(UploadMethod method, TestByteBufferVariant variant, int length,
        int expectedStagedBlocks) {
        byte[] content = randomBytes(length);
        ByteBufferState source = variant.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        upload(method, httpClient, Collections.singletonList(source.getBuffer()), length);

        assertEquals(expectedStagedBlocks, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        source.assertNoBytesChanged();
    }

    private static Stream<Arguments> partitionedUploadSupplier() {
        return Stream.of(UploadMethod.values())
            .flatMap(method -> Stream.of(TestByteBufferVariant.values())
                .flatMap(variant -> Stream.of(
                    // method | variant | length | expected staged blocks
                    Arguments.of(method, variant, BLOCK_SIZE / 2, 0), // A single request.
                    Arguments.of(method, variant, BLOCK_SIZE, 0), // A single request, exactly the threshold.
                    Arguments.of(method, variant, 2 * BLOCK_SIZE + 100, 3), // Staged, with a partial last block.
                    Arguments.of(method, variant, 3 * BLOCK_SIZE, 3)))); // Staged, an exact multiple of the block size.
    }

    @ParameterizedTest
    @EnumSource(UploadMethod.class)
    public void partitionedUploadSendsOnlyExposedBytesOfMixedBuffers(UploadMethod method) {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        // Pieces that don't align with blocks, so blocks span buffers of different kinds.
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        upload(method, httpClient, buffers, content.length);

        assertEquals(6, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ByteBufferState::assertNoBytesChanged);
    }

    @ParameterizedTest
    @MethodSource("mixedSizedBuffersSupplier")
    public void partitionedUploadSendsOnlyExposedBytesOfMixedSizedBuffers(UploadMethod method,
        TestByteBufferVariant oversizedVariant) {
        int oversizedLength = 2 * BLOCK_SIZE + 100;
        byte[] content = randomBytes(700 + oversizedLength + 300);
        // The middle buffer must split, while staged blocks also span differently positioned source buffers.
        List<ByteBufferState> sources = Arrays.asList(
            TestByteBufferVariant.POSITIONED.wrap(Arrays.copyOfRange(content, 0, 700)),
            oversizedVariant.wrap(Arrays.copyOfRange(content, 700, 700 + oversizedLength)),
            TestByteBufferVariant.READ_ONLY.wrap(Arrays.copyOfRange(content, 700 + oversizedLength, content.length)));
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        upload(method, httpClient, buffers, content.length);

        assertEquals(4, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ByteBufferState::assertNoBytesChanged);
    }

    private static Stream<Arguments> mixedSizedBuffersSupplier() {
        return Stream.of(UploadMethod.values())
            .flatMap(method -> Stream.of(TestByteBufferVariant.values()).map(variant -> Arguments.of(method, variant)));
    }

    /*
     * Uploads the buffers with a partitioned upload, using the given client and kind of data.
     */
    private static void upload(UploadMethod method, HttpClient httpClient, List<ByteBuffer> buffers, long length) {
        Flux<ByteBuffer> data = Flux.fromIterable(buffers);
        BlobParallelUploadOptions options;
        if (method == UploadMethod.SYNC_FLUX || method == UploadMethod.ASYNC_FLUX) {
            options = new BlobParallelUploadOptions(data);
        } else {
            // BinaryData from a single buffer, or from several without copying them, keeps the caller's buffers.
            BinaryData binaryData = buffers.size() == 1
                ? BinaryData.fromByteBuffer(buffers.get(0))
                : BinaryData.fromFlux(data, length, false).block();
            options = new BlobParallelUploadOptions(binaryData);
        }
        options.setParallelTransferOptions(blockOptions());

        if (method == UploadMethod.SYNC_FLUX || method == UploadMethod.SYNC_BINARY_DATA) {
            blobClient(httpClient).uploadWithResponse(options, null, null);
        } else {
            blobAsyncClient(httpClient).uploadWithResponse(options).block();
        }
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void putBlobSendsOnlyExposedBytes(TestByteBufferVariant variant) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ByteBufferState source = variant.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getBlockBlobAsyncClient()
            .upload(Flux.just(source.getBuffer()), content.length, true)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody(null));
        source.assertNoBytesChanged();
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void stageBlockSendsOnlyExposedBytes(TestByteBufferVariant variant) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ByteBufferState source = variant.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();
        String blockId = Base64.getEncoder().encodeToString("block".getBytes(StandardCharsets.UTF_8));

        blobAsyncClient(httpClient).getBlockBlobAsyncClient()
            .stageBlock(blockId, Flux.just(source.getBuffer()), content.length)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("block"));
        source.assertNoBytesChanged();
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void appendBlockSendsOnlyExposedBytes(TestByteBufferVariant variant) {
        byte[] content = randomBytes(BLOCK_SIZE + 100);
        ByteBufferState source = variant.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getAppendBlobAsyncClient()
            .appendBlock(Flux.just(source.getBuffer()), content.length)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("appendblock"));
        source.assertNoBytesChanged();
    }

    @ParameterizedTest
    @EnumSource(TestByteBufferVariant.class)
    public void uploadPagesSendsOnlyExposedBytes(TestByteBufferVariant variant) {
        byte[] content = randomBytes(2 * PAGE_SIZE);
        ByteBufferState source = variant.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        blobAsyncClient(httpClient).getPageBlobAsyncClient()
            .uploadPages(new PageRange().setStart(0).setEnd(content.length - 1), Flux.just(source.getBuffer()))
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBody("page"));
        source.assertNoBytesChanged();
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

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() != HttpMethod.PUT) {
                return Mono.error(new IllegalStateException("Unexpected request: " + request.getHttpMethod()));
            }

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody());
            return body.map(bytes -> {
                String comp = getQueryParameter(request, "comp");
                synchronized (this) {
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
