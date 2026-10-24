// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.test.shared.ByteBufferState;
import com.azure.storage.common.test.shared.TestByteBufferVariant;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that a file share upload split into multiple ranges doesn't modify the caller's buffers, not
 * even their position or limit, so the same buffers can be uploaded again.
 */
public class ShareBufferReuseUploadTests {
    private static final String ENDPOINT = "https://account.file.core.windows.net";
    // Small sizes keep the tests fast; the staging logic doesn't depend on the absolute sizes.
    private static final int RANGE_SIZE = 1024;
    // Smaller than a range, so ranges span several buffers and buffers straddle range boundaries.
    private static final int PIECE_LENGTH = 700;
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    @Test
    public void partitionedUploadDoesNotModifyCallerBuffers() {
        byte[] content = randomBytes(5 * RANGE_SIZE + 100);
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, PIECE_LENGTH);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(new ShareFileUploadOptions(Flux.fromIterable(buffersOf(sources)))
            .setParallelTransferOptions(rangeOptions()), null, null);

        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        sources.forEach(ByteBufferState::assertUnchanged);
    }

    @Test
    public void uploadingSameBuffersAgainSendsSameContent() {
        byte[] content = randomBytes(5 * RANGE_SIZE + 100);
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, PIECE_LENGTH);
        List<ByteBuffer> buffers = buffersOf(sources);

        for (int upload = 1; upload <= 2; upload++) {
            RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

            fileClient(httpClient).uploadWithResponse(
                new ShareFileUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(rangeOptions()), null,
                null);

            TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        }
        sources.forEach(ByteBufferState::assertUnchanged);
    }

    private static List<ByteBuffer> buffersOf(List<ByteBufferState> sources) {
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        return buffers;
    }

    private static ParallelTransferOptions rangeOptions() {
        return new ParallelTransferOptions().setBlockSizeLong((long) RANGE_SIZE)
            .setMaxSingleUploadSizeLong((long) RANGE_SIZE);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static ShareFileClient fileClient(HttpClient httpClient) {
        return new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .resourcePath("file")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildFileClient();
    }

    /*
     * Captures the content of each Put Range request by its offset. Reads request bodies through duplicates, as HTTP
     * clients do, so the mock itself never moves the caller's buffers.
     */
    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<Long, byte[]> ranges = new TreeMap<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() != HttpMethod.PUT) {
                return Mono.error(new IllegalStateException("Unexpected request: " + request.getHttpMethod()));
            }

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody().map(ByteBuffer::duplicate));
            return body.map(bytes -> {
                Matcher matcher = RANGE_PATTERN.matcher(request.getHeaders().getValue(X_MS_RANGE));
                assertTrue(matcher.matches());
                long start = Long.parseLong(matcher.group(1));
                assertEquals(Long.parseLong(matcher.group(2)) - start + 1, bytes.length);
                synchronized (this) {
                    ranges.put(start, bytes);
                }
                return new MockHttpResponse(request, 201);
            });
        }

        /*
         * Gets the content of the file as it would be after the upload, from the ranges ordered by their offset.
         */
        synchronized byte[] getFileContent() {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            for (Map.Entry<Long, byte[]> range : ranges.entrySet()) {
                assertEquals(content.size(), range.getKey().longValue());
                content.write(range.getValue(), 0, range.getValue().length);
            }
            return content.toByteArray();
        }
    }
}
