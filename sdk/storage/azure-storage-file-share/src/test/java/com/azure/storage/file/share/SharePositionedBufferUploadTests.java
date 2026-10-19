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
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.models.ShareFileUploadOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests verifying that uploading a {@link ByteBuffer} whose position is not zero uploads only the bytes between
 * its position and limit, both when uploaded in a single range and when split into multiple ranges.
 */
public class SharePositionedBufferUploadTests {
    private static final String ENDPOINT = "https://account.file.core.windows.net";
    // The default, and maximum, size of a range. Content larger than this is split into multiple ranges.
    private static final int RANGE_SIZE = 4 * Constants.MB;
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)");

    @ParameterizedTest
    @MethodSource("positionedBufferSupplier")
    public void uploadPositionedBuffer(int position, int remaining, int expectedRanges) {
        byte[] backing = new byte[position + remaining + 3];
        new Random(position + remaining).nextBytes(backing);
        ByteBuffer buffer = ByteBuffer.wrap(backing, position, remaining);
        RequestCapturingHttpClient httpClient = new RequestCapturingHttpClient();

        // The default transfer options split content larger than the range size into multiple ranges.
        fileClient(httpClient).uploadWithResponse(new ShareFileUploadOptions(Flux.just(buffer)), null, null);

        assertEquals(expectedRanges, httpClient.getRangeCount());
        TestUtils.assertArraysEqual(Arrays.copyOfRange(backing, position, position + remaining),
            httpClient.getUploadedContent());
    }

    private static Stream<Arguments> positionedBufferSupplier() {
        return Stream.of(
            // position | remaining | expected ranges
            Arguments.of(1, RANGE_SIZE, 1), Arguments.of(1, RANGE_SIZE + Constants.MB, 2),
            Arguments.of(Constants.MB + 7, 2 * RANGE_SIZE + 5, 3),
            // An exact multiple of the range size.
            Arguments.of(1, 2 * RANGE_SIZE, 2));
    }

    private static ShareFileClient fileClient(HttpClient httpClient) {
        return new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .resourcePath("file")
            .credential(new MockTokenCredential())
            .pipeline(new HttpPipelineBuilder().httpClient(httpClient).build())
            .buildFileClient();
    }

    private static final class RequestCapturingHttpClient implements HttpClient {
        private final Map<Long, byte[]> ranges = new TreeMap<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() != HttpMethod.PUT) {
                return Mono.error(new IllegalStateException("Unexpected request: " + request.getHttpMethod()));
            }

            Mono<byte[]> body = request.getBody() == null
                ? Mono.just(new byte[0])
                : FluxUtil.collectBytesInByteBufferStream(request.getBody());
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
        synchronized byte[] getUploadedContent() {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            for (Map.Entry<Long, byte[]> range : ranges.entrySet()) {
                assertEquals(content.size(), range.getKey().longValue());
                content.write(range.getValue(), 0, range.getValue().length);
            }
            return content.toByteArray();
        }
    }
}
