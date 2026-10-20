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
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline tests verifying that a blob upload staged in blocks doesn't modify the caller's buffers, not even their
 * position or limit, so the same buffers can be uploaded again, such as when the upload is retried by subscribing to
 * it again.
 */
public class BlobBufferReuseUploadTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    // Small sizes keep the tests fast; the staging logic doesn't depend on the absolute sizes.
    private static final int BLOCK_SIZE = 1024;
    // Smaller than a block, so blocks span several buffers and buffers straddle block boundaries.
    private static final int PIECE_LENGTH = 700;
    private static final Pattern LATEST_BLOCK_PATTERN = Pattern.compile("<Latest>([^<]+)</Latest>");

    @Test
    public void stagedUploadDoesNotModifyCallerBuffers() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, PIECE_LENGTH);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(0);

        blobClient(httpClient).uploadWithResponse(new BlobParallelUploadOptions(Flux.fromIterable(buffersOf(sources)))
            .setParallelTransferOptions(blockOptions()), null, null);

        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void uploadingSameBuffersAgainSendsSameContent() {
        byte[] content = randomBytes(5 * BLOCK_SIZE + 100);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, PIECE_LENGTH);
        List<ByteBuffer> buffers = buffersOf(sources);

        for (int upload = 1; upload <= 2; upload++) {
            RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(0);

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
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, PIECE_LENGTH);
        // The first commit fails, so the upload is retried by subscribing to it again, which re-reads the buffers.
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient(1);

        blobAsyncClient(httpClient)
            .uploadWithResponse(new BlobParallelUploadOptions(Flux.fromIterable(buffersOf(sources)))
                .setParallelTransferOptions(blockOptions()))
            .retry(1)
            .block();

        TestUtils.assertArraysEqual(content, httpClient.getBlobContent());
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    private static List<ByteBuffer> buffersOf(List<ShapedByteBuffer> sources) {
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        return buffers;
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
     * Captures staged blocks and the committed block list. Reads request bodies through duplicates, as HTTP clients do,
     * so the mock itself never moves the caller's buffers.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<String, byte[]> stagedBlocks = new HashMap<>();
        private int failedCommitsRemaining;
        private String commitBody;

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
                    if ("blocklist".equals(comp)) {
                        if (failedCommitsRemaining > 0) {
                            // A failure the SDK doesn't retry itself, so the caller has to retry the whole upload.
                            failedCommitsRemaining--;
                            return new MockHttpResponse(request, 400);
                        }
                        commitBody = new String(bytes, StandardCharsets.UTF_8);
                    } else if ("block".equals(comp)) {
                        stagedBlocks.put(getQueryParameter(request, "blockid"), bytes);
                    } else {
                        throw new IllegalStateException("Expected the upload to be staged in blocks.");
                    }
                }
                return new MockHttpResponse(request, 201,
                    new HttpHeaders().set(HttpHeaderName.fromString("x-ms-request-server-encrypted"), "true"));
            });
        }

        /*
         * Gets the content of the blob as it would be after the upload, from the staged blocks in the order they were
         * committed.
         */
        synchronized byte[] getBlobContent() {
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
