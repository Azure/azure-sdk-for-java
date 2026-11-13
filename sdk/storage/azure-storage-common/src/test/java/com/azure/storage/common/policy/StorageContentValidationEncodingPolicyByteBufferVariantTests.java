// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StorageCrc64Calculator;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageDecoder;
import com.azure.storage.common.test.shared.ByteBufferState;
import com.azure.storage.common.test.shared.TestByteBufferVariant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_MODE_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.V1_DEFAULT_SEGMENT_CONTENT_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link StorageContentValidationEncodingPolicy} computes checksums over, and sends, exactly the bytes
 * between each request body buffer's position and limit, for every kind of buffer (see {@link TestByteBufferVariant}), and
 * that a retry sends the same content again without the caller's buffers being consumed or modified.
 */
public class StorageContentValidationEncodingPolicyByteBufferVariantTests {
    private static final String URL = "https://account.blob.core.windows.net/container/blob";

    @ParameterizedTest
    @MethodSource("byteBufferVariantsSupplier")
    public void encodesOnlyExposedBytes(TestByteBufferVariant variant, String mode, int length) {
        byte[] content = randomBytes(length);
        ByteBufferState source = variant.wrap(content);

        List<CapturedRequest> requests = send(mode, content.length, source);

        // The first attempt fails and is retried, so the content must be sent correctly twice.
        assertEquals(2, requests.size());
        for (CapturedRequest request : requests) {
            assertSentContent(mode, content, request);
        }
        source.assertNoBytesChanged();
    }

    private static Stream<Arguments> byteBufferVariantsSupplier() {
        return Stream.of(TestByteBufferVariant.values())
            .flatMap(variant -> Stream.of(
                // variant | mode | length
                Arguments.of(variant, USE_CRC64_CHECKSUM_HEADER_CONTEXT, 1000),
                Arguments.of(variant, USE_STRUCTURED_MESSAGE_CONTEXT, 1000), // A single segment.
                // Several segments, the last one partial.
                Arguments.of(variant, USE_STRUCTURED_MESSAGE_CONTEXT, V1_DEFAULT_SEGMENT_CONTENT_LENGTH + 1000)));
    }

    @ParameterizedTest
    @MethodSource("mixedByteBufferVariantsSupplier")
    public void encodesOnlyExposedBytesOfMixedBuffers(String mode) {
        byte[] content = randomBytes(V1_DEFAULT_SEGMENT_CONTENT_LENGTH + 1000);
        // Pieces that don't align with segments, so a segment spans buffers of different kinds.
        List<ByteBufferState> sources = TestByteBufferVariant.mixed(content, 1024 * 1024 + 7);

        List<CapturedRequest> requests = send(mode, content.length, sources.toArray(new ByteBufferState[0]));

        assertEquals(2, requests.size());
        for (CapturedRequest request : requests) {
            assertSentContent(mode, content, request);
        }
        sources.forEach(ByteBufferState::assertNoBytesChanged);
    }

    private static Stream<String> mixedByteBufferVariantsSupplier() {
        return Stream.of(USE_CRC64_CHECKSUM_HEADER_CONTEXT, USE_STRUCTURED_MESSAGE_CONTEXT);
    }

    /*
     * Sends a request with the buffers as its body through the retry and encoding policies, with the first attempt
     * failing, and returns each attempt as sent.
     */
    private static List<CapturedRequest> send(String mode, int length, ByteBufferState... sources) {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (ByteBufferState source : sources) {
            buffers.add(source.getBuffer());
        }
        List<CapturedRequest> requests = new ArrayList<>();
        HttpClient httpClient = request -> FluxUtil.collectBytesInByteBufferStream(request.getBody()).map(body -> {
            synchronized (requests) {
                requests.add(new CapturedRequest(request.getHeaders(), body));
                // Fail the first attempt with a retriable status code.
                return new MockHttpResponse(request, requests.size() == 1 ? 503 : 201);
            }
        });
        HttpPipeline pipeline
            = new HttpPipelineBuilder()
                .policies(
                    new RequestRetryPolicy(new RequestRetryOptions(RetryPolicyType.FIXED, 2, (Duration) null,
                        Duration.ofMillis(1), Duration.ofMillis(1), null)),
                    new StorageContentValidationEncodingPolicy())
                .httpClient(httpClient)
                .build();
        HttpRequest request = new HttpRequest(HttpMethod.PUT, URL).setBody(Flux.fromIterable(buffers))
            .setHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(length));

        try (HttpResponse response = pipeline.send(request, new Context(CONTENT_VALIDATION_MODE_KEY, mode)).block()) {
            assertEquals(201, response.getStatusCode());
        }
        return requests;
    }

    private static void assertSentContent(String mode, byte[] content, CapturedRequest request) {
        if (USE_CRC64_CHECKSUM_HEADER_CONTEXT.equals(mode)) {
            TestUtils.assertArraysEqual(content, request.body);
            assertEquals(crc64Header(content), request.crc64);
        } else {
            assertEquals(String.valueOf(content.length), request.structuredContentLength);
            assertEquals(String.valueOf(request.body.length), request.contentLength);
            TestUtils.assertArraysEqual(content, decode(request.body));
        }
    }

    private static String crc64Header(byte[] content) {
        long crc = StorageCrc64Calculator.compute(content, 0);
        byte[] crcBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            crcBytes[i] = (byte) (crc >>> (i * 8));
        }
        return Base64.getEncoder().encodeToString(crcBytes);
    }

    /*
     * Decodes the structured message, which validates its CRC64s.
     */
    private static byte[] decode(byte[] message) {
        StructuredMessageDecoder decoder = new StructuredMessageDecoder(message.length);
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();
        for (ByteBuffer segment : decoder.decodeChunk(ByteBuffer.wrap(message))) {
            byte[] bytes = new byte[segment.remaining()];
            segment.get(bytes);
            decoded.write(bytes, 0, bytes.length);
        }
        assertTrue(decoder.isComplete());
        return decoded.toByteArray();
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static final class CapturedRequest {
        private final String contentLength;
        private final String crc64;
        private final String structuredContentLength;
        private final byte[] body;

        CapturedRequest(HttpHeaders headers, byte[] body) {
            this.contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
            this.crc64 = headers.getValue(Constants.HeaderConstants.CONTENT_CRC64_HEADER_NAME);
            this.structuredContentLength
                = headers.getValue(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME);
            this.body = body;
        }
    }
}
