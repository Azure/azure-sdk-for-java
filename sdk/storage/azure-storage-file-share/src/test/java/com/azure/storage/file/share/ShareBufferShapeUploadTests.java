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
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that file share uploads send only the bytes between each source buffer's position and limit,
 * and don't modify the source buffers, for every kind of buffer (see {@link TestBufferShape}).
 */
public class ShareBufferShapeUploadTests {
    private static final String ENDPOINT = "https://account.file.core.windows.net";
    // Small sizes keep the tests fast; the partitioning logic doesn't depend on the absolute sizes.
    private static final int RANGE_SIZE = 1024;
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    @ParameterizedTest
    @MethodSource("partitionedUploadSupplier")
    public void partitionedUploadSendsOnlyExposedBytes(TestBufferShape shape, int length, int expectedRanges) {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(
            new ShareFileUploadOptions(Flux.just(source.getBuffer())).setParallelTransferOptions(rangeOptions()), null,
            null);

        assertEquals(expectedRanges, httpClient.getRangeCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        source.assertUnchanged();
    }

    private static Stream<Arguments> partitionedUploadSupplier() {
        return Stream.of(TestBufferShape.values())
            .flatMap(shape -> Stream.of(
                // shape | length | expected ranges
                Arguments.of(shape, RANGE_SIZE / 2, 1), // A single range.
                Arguments.of(shape, RANGE_SIZE, 1), // A single range, exactly the threshold.
                Arguments.of(shape, 2 * RANGE_SIZE + 100, 3), // Several ranges, with a partial last one.
                Arguments.of(shape, 3 * RANGE_SIZE, 3))); // Several ranges, an exact multiple of the range size.
    }

    @Test
    public void partitionedUploadSendsOnlyExposedBytesOfMixedBuffers() {
        byte[] content = randomBytes(5 * RANGE_SIZE + 100);
        // Pieces that don't align with ranges, so ranges span buffers of different kinds.
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(
            new ShareFileUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(rangeOptions()), null,
            null);

        assertEquals(6, httpClient.getRangeCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void uploadingSameBuffersAgainSendsSameContent() {
        byte[] content = randomBytes(5 * RANGE_SIZE + 100);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 700);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));

        for (int upload = 1; upload <= 2; upload++) {
            RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

            fileClient(httpClient).uploadWithResponse(
                new ShareFileUploadOptions(Flux.fromIterable(buffers)).setParallelTransferOptions(rangeOptions()), null,
                null);

            TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        }
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void partitionedUploadWithDefaultOptionsSendsOnlyExposedBytes() {
        // The default options split content larger than the maximum range size, 4 MiB, into ranges.
        int length = 4 * Constants.MB + 100;
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = TestBufferShape.POSITIONED.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileClient(httpClient).uploadWithResponse(new ShareFileUploadOptions(Flux.just(source.getBuffer())), null,
            null);

        assertEquals(2, httpClient.getRangeCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        source.assertUnchanged();
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void uploadRangeSendsOnlyExposedBytes(TestBufferShape shape) {
        byte[] content = randomBytes(RANGE_SIZE + 100);
        ShapedByteBuffer source = shape.wrap(content);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        fileAsyncClient(httpClient).uploadRange(Flux.just(source.getBuffer()), content.length).block();

        assertEquals(1, httpClient.getRangeCount());
        TestUtils.assertArraysEqual(content, httpClient.getFileContent());
        source.assertUnchanged();
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
        return builder(httpClient).buildFileClient();
    }

    private static ShareFileAsyncClient fileAsyncClient(HttpClient httpClient) {
        return builder(httpClient).buildFileAsyncClient();
    }

    private static ShareFileClientBuilder builder(HttpClient httpClient) {
        return new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .resourcePath("file")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build());
    }

    /*
     * Captures the content of each Put Range request by its offset.
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

        synchronized int getRangeCount() {
            return ranges.size();
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
