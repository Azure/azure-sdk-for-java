// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that uploading a {@link ByteBuffer} whose position is not zero uploads only the bytes between
 * its position and limit, both when uploaded in a single request and when staged in blocks.
 */
public class BlobPositionedBufferUploadTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final long BLOCK_SIZE = 4L * Constants.MB;
    private static final Pattern LATEST_BLOCK_PATTERN = Pattern.compile("<Latest>([^<]+)</Latest>");

    @ParameterizedTest
    @MethodSource("positionedBufferSupplier")
    public void uploadPositionedBuffer(int position, int remaining, int expectedStagedBlocks) {
        byte[] backing = new byte[position + remaining + 3];
        new Random(position + remaining).nextBytes(backing);
        ByteBuffer buffer = ByteBuffer.wrap(backing, position, remaining);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        // Content larger than the block size is staged in blocks.
        ParallelTransferOptions options
            = new ParallelTransferOptions().setBlockSizeLong(BLOCK_SIZE).setMaxSingleUploadSizeLong(BLOCK_SIZE);
        blobClient(httpClient).uploadWithResponse(
            new BlobParallelUploadOptions(Flux.just(buffer)).setParallelTransferOptions(options), null, null);

        assertEquals(expectedStagedBlocks, httpClient.getStagedBlockCount());
        TestUtils.assertArraysEqual(Arrays.copyOfRange(backing, position, position + remaining),
            httpClient.getUploadedContent());
    }

    private static Stream<Arguments> positionedBufferSupplier() {
        return Stream.of(
            // position | remaining | expected staged blocks
            Arguments.of(1, (int) BLOCK_SIZE, 0), Arguments.of(1, (int) BLOCK_SIZE + Constants.MB, 2),
            Arguments.of(Constants.MB + 7, 2 * (int) BLOCK_SIZE + 5, 3),
            // An exact multiple of the block size.
            Arguments.of(1, 2 * (int) BLOCK_SIZE, 2));
    }

    private static BlobClient blobClient(HttpClient httpClient) {
        return new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildClient();
    }

    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<String, byte[]> stagedBlocks = new HashMap<>();
        private byte[] putBlobBody;
        private String commitBody;

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
                    if ("blocklist".equals(comp)) {
                        commitBody = new String(bytes, StandardCharsets.UTF_8);
                    } else if ("block".equals(comp)) {
                        stagedBlocks.put(getQueryParameter(request, "blockid"), bytes);
                    } else {
                        putBlobBody = bytes;
                    }
                }
                return new MockHttpResponse(request, 201);
            });
        }

        synchronized int getStagedBlockCount() {
            return stagedBlocks.size();
        }

        /*
         * Gets the content of the blob as it would be after the upload, from either the Put Blob request or the staged
         * blocks in the order they were committed.
         */
        synchronized byte[] getUploadedContent() {
            if (putBlobBody != null) {
                return putBlobBody;
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
