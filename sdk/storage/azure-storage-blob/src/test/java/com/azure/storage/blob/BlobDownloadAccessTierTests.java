// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Get Blob reports the blob's access tier, as Get Blob Properties does; the download's headers and the properties
 * built from them carry it rather than leaving a caller to ask for the properties again.
 */
public class BlobDownloadAccessTierTests {
    private static final byte[] CONTENT = "hello".getBytes(StandardCharsets.UTF_8);
    private static final OffsetDateTime TIER_CHANGE_TIME = OffsetDateTime.of(2026, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);

    @Test
    public void downloadHeadersCarryAccessTier() {
        BlobDownloadHeaders headers = blobClient(tierHeaders("Cool"))
            .downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)
            .getDeserializedHeaders();

        assertEquals(AccessTier.COOL, headers.getAccessTier());
        assertTrue(headers.isAccessTierInferred());
        assertEquals(TIER_CHANGE_TIME, headers.getAccessTierChangeTime());
        assertNull(headers.getSmartAccessTier());
    }

    @Test
    public void inputStreamPropertiesCarryAccessTier() throws IOException {
        try (BlobInputStream stream = blobClient(tierHeaders("Cool")).openInputStream()) {
            BlobProperties properties = stream.getProperties();
            assertEquals(AccessTier.COOL, properties.getAccessTier());
            assertTrue(properties.isAccessTierInferred());
            assertEquals(TIER_CHANGE_TIME, properties.getAccessTierChangeTime());
        }
    }

    @Test
    public void asyncDownloadHeadersCarryAccessTier() {
        StepVerifier.create(blobAsyncClient(tierHeaders("Cool")).downloadStreamWithResponse(null, null, null, false))
            .assertNext(response -> {
                BlobDownloadHeaders headers = response.getDeserializedHeaders();
                assertEquals(AccessTier.COOL, headers.getAccessTier());
                assertTrue(headers.isAccessTierInferred());
                assertEquals(TIER_CHANGE_TIME, headers.getAccessTierChangeTime());
            })
            .verifyComplete();
    }

    @Test
    public void downloadHeadersCarrySmartAccessTier() {
        HttpHeaders headers = tierHeaders("Smart").set(HttpHeaderName.fromString("x-ms-smart-access-tier"), "Hot");

        BlobDownloadHeaders downloadHeaders = blobClient(headers)
            .downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)
            .getDeserializedHeaders();

        assertEquals(AccessTier.SMART, downloadHeaders.getAccessTier());
        assertEquals(AccessTier.HOT, downloadHeaders.getSmartAccessTier());
    }

    @Test
    public void downloadWithoutAccessTierHasNone() {
        BlobDownloadHeaders headers = blobClient(baseHeaders())
            .downloadStreamWithResponse(new ByteArrayOutputStream(), null, null, null, false, null, null)
            .getDeserializedHeaders();

        assertNull(headers.getAccessTier());
        assertNull(headers.isAccessTierInferred());
        assertNull(headers.getAccessTierChangeTime());
    }

    @Test
    public void accessTierRoundTripsThroughSetters() {
        BlobDownloadHeaders headers = new BlobDownloadHeaders().setAccessTier(AccessTier.ARCHIVE)
            .setAccessTierInferred(false)
            .setAccessTierChangeTime(TIER_CHANGE_TIME)
            .setSmartAccessTier(AccessTier.COLD);

        assertEquals(AccessTier.ARCHIVE, headers.getAccessTier());
        assertEquals(Boolean.FALSE, headers.isAccessTierInferred());
        assertEquals(TIER_CHANGE_TIME, headers.getAccessTierChangeTime());
        assertEquals(AccessTier.COLD, headers.getSmartAccessTier());

        headers.setAccessTier(null).setSmartAccessTier(null);
        assertNull(headers.getAccessTier());
        assertNull(headers.getSmartAccessTier());
    }

    private static HttpHeaders baseHeaders() {
        return new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream")
            .set(HttpHeaderName.ETAG, "\"0x8DABC\"")
            .set(HttpHeaderName.LAST_MODIFIED, new DateTimeRfc1123(TIER_CHANGE_TIME).toString())
            .set(HttpHeaderName.fromString("x-ms-blob-type"), "BlockBlob")
            .set(HttpHeaderName.fromString("x-ms-version"), BlobServiceVersion.getLatest().getVersion());
    }

    private static HttpHeaders tierHeaders(String tier) {
        return baseHeaders().set(HttpHeaderName.fromString("x-ms-access-tier"), tier)
            .set(HttpHeaderName.fromString("x-ms-access-tier-inferred"), "true")
            .set(HttpHeaderName.fromString("x-ms-access-tier-change-time"),
                new DateTimeRfc1123(TIER_CHANGE_TIME).toString());
    }

    /** Answers every request as Get Blob does, with the range asked for. */
    private static HttpClient httpClient(HttpHeaders headers) {
        return request -> {
            HttpHeaders responseHeaders = new HttpHeaders(headers);
            String range = request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-range"));
            if (range == null) {
                responseHeaders.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(CONTENT.length));
                return Mono.just(new MockHttpResponse(request, 200, responseHeaders, CONTENT));
            }
            responseHeaders.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(CONTENT.length))
                .set(HttpHeaderName.CONTENT_RANGE, "bytes 0-" + (CONTENT.length - 1) + "/" + CONTENT.length);
            return Mono.just(new MockHttpResponse(request, 206, responseHeaders, CONTENT));
        };
    }

    private static BlobClient blobClient(HttpHeaders headers) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net/container/blob")
            .httpClient(httpClient(headers))
            .buildClient();
    }

    private static BlobAsyncClient blobAsyncClient(HttpHeaders headers) {
        return new BlobClientBuilder().endpoint("https://account.blob.core.windows.net/container/blob")
            .httpClient(httpClient(headers))
            .buildAsyncClient();
    }
}
