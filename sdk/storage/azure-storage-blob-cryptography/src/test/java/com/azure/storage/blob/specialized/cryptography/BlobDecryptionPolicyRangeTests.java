// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A ranged download of an encrypted blob must be expanded to whole encryption blocks, whichever range header the
 * request carries.
 */
public class BlobDecryptionPolicyRangeTests {
    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");
    private static final String ENCRYPTION_DATA_JSON
        = "{\"EncryptionMode\":\"FullBlob\",\"EncryptionAgent\":{\"Protocol\":\"1.0\","
            + "\"EncryptionAlgorithm\":\"AES_CBC_256\"}}";

    @ParameterizedTest
    @MethodSource("rangeHeaderSupplier")
    public void rangedRequestIsExpandedToEncryptedRange(String range, String xMsRange) throws IOException {
        EncryptionData encryptionData;
        try (JsonReader jsonReader = JsonProviders.createReader(ENCRYPTION_DATA_JSON)) {
            encryptionData = EncryptionData.fromJson(jsonReader);
        }
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new BlobDecryptionPolicy(new FakeKey("keyId", new byte[256]), null, false))
            .httpClient(request -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            })
            .build();

        HttpHeaders headers = new HttpHeaders();
        if (range != null) {
            headers.set(HttpHeaderName.RANGE, range);
        }
        if (xMsRange != null) {
            headers.set(X_MS_RANGE, xMsRange);
        }
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://account.blob.core.windows.net/c/b").setHeaders(headers);
        pipeline.send(request, new Context(CryptographyConstants.ENCRYPTION_DATA_KEY, encryptionData)).block();

        // x-ms-range takes precedence when both are sent, so only that header is expanded.
        String requestedRange = xMsRange != null ? xMsRange : range;
        String expandedRange = EncryptedBlobRange.getEncryptedBlobRangeFromHeader(requestedRange, encryptionData)
            .toBlobRange()
            .toString();
        HttpHeaders sentHeaders = sentRequest.get().getHeaders();
        assertNotEquals(requestedRange, expandedRange);
        assertEquals(xMsRange != null ? range : expandedRange, sentHeaders.getValue(HttpHeaderName.RANGE));
        assertEquals(xMsRange != null ? expandedRange : null, sentHeaders.getValue(X_MS_RANGE));
    }

    private static Stream<Arguments> rangeHeaderSupplier() {
        return Stream.of(Arguments.of("bytes=20-40", null), Arguments.of(null, "bytes=20-40"),
            Arguments.of("bytes=0-10", "bytes=20-40"));
    }
}
