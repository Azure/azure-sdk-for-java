// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.storage.blob.implementation.models.BlobsDownloadHeaders;
import com.azure.storage.blob.implementation.util.ModelHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the access tier properties of {@link BlobDownloadHeaders} and the {@link BlobProperties} derived from
 * them.
 */
public class BlobDownloadHeadersTests {
    private static final HttpHeaderName X_MS_ACCESS_TIER = HttpHeaderName.fromString("x-ms-access-tier");
    private static final HttpHeaderName X_MS_ACCESS_TIER_INFERRED
        = HttpHeaderName.fromString("x-ms-access-tier-inferred");
    private static final HttpHeaderName X_MS_ACCESS_TIER_CHANGE_TIME
        = HttpHeaderName.fromString("x-ms-access-tier-change-time");
    private static final HttpHeaderName X_MS_SMART_ACCESS_TIER = HttpHeaderName.fromString("x-ms-smart-access-tier");

    private static final String CHANGE_TIME_HEADER = "Fri, 22 May 2026 02:07:44 GMT";
    private static final OffsetDateTime CHANGE_TIME = OffsetDateTime.of(2026, 5, 22, 2, 7, 44, 0, ZoneOffset.UTC);

    private static BlobDownloadHeaders fromRawHeaders(HttpHeaders rawHeaders) {
        return ModelHelper.populateBlobDownloadHeaders(new BlobsDownloadHeaders(rawHeaders), null);
    }

    @Test
    public void parsesSmartAccessTierHeaders() {
        HttpHeaders rawHeaders = new HttpHeaders().set(X_MS_ACCESS_TIER, "Smart")
            .set(X_MS_ACCESS_TIER_CHANGE_TIME, CHANGE_TIME_HEADER)
            .set(X_MS_SMART_ACCESS_TIER, "Hot");

        BlobDownloadHeaders headers = fromRawHeaders(rawHeaders);

        assertEquals(AccessTier.SMART, headers.getAccessTier());
        assertEquals(AccessTier.HOT, headers.getSmartAccessTier());
        assertEquals(CHANGE_TIME, headers.getAccessTierChangeTime());
        assertNull(headers.isAccessTierInferred());
    }

    @Test
    public void parsesInferredAccessTierHeaders() {
        HttpHeaders rawHeaders = new HttpHeaders().set(X_MS_ACCESS_TIER, "Hot").set(X_MS_ACCESS_TIER_INFERRED, "true");

        BlobDownloadHeaders headers = fromRawHeaders(rawHeaders);

        assertEquals(AccessTier.HOT, headers.getAccessTier());
        assertTrue(headers.isAccessTierInferred());
        assertNull(headers.getSmartAccessTier());
        assertNull(headers.getAccessTierChangeTime());
    }

    @Test
    public void parsesNonInferredAccessTierHeader() {
        HttpHeaders rawHeaders
            = new HttpHeaders().set(X_MS_ACCESS_TIER, "Cool").set(X_MS_ACCESS_TIER_INFERRED, "false");

        BlobDownloadHeaders headers = fromRawHeaders(rawHeaders);

        assertEquals(AccessTier.COOL, headers.getAccessTier());
        assertFalse(headers.isAccessTierInferred());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Hot", "Cool", "Cold", "Archive", "Smart", "P10" })
    public void parsesAccessTierValues(String tier) {
        BlobDownloadHeaders headers = fromRawHeaders(new HttpHeaders().set(X_MS_ACCESS_TIER, tier));

        assertEquals(AccessTier.fromString(tier), headers.getAccessTier());
    }

    @Test
    public void absentAccessTierHeadersAreNull() {
        BlobDownloadHeaders headers = fromRawHeaders(new HttpHeaders());

        assertNull(headers.getAccessTier());
        assertNull(headers.isAccessTierInferred());
        assertNull(headers.getAccessTierChangeTime());
        assertNull(headers.getSmartAccessTier());
    }

    @Test
    public void defaultConstructorAccessTierPropertiesAreNull() {
        BlobDownloadHeaders headers = new BlobDownloadHeaders();

        assertNull(headers.getAccessTier());
        assertNull(headers.isAccessTierInferred());
        assertNull(headers.getAccessTierChangeTime());
        assertNull(headers.getSmartAccessTier());
    }

    @Test
    public void asyncResponseExposesAccessTierHeaders() {
        BlobDownloadHeaders deserializedHeaders = new BlobDownloadHeaders().setAccessTier(AccessTier.SMART)
            .setSmartAccessTier(AccessTier.COOL)
            .setAccessTierChangeTime(CHANGE_TIME);
        BlobDownloadAsyncResponse response
            = new BlobDownloadAsyncResponse(null, 200, new HttpHeaders(), Flux.empty(), deserializedHeaders);

        BlobDownloadHeaders headers = response.getDeserializedHeaders();
        assertEquals(AccessTier.SMART, headers.getAccessTier());
        assertEquals(AccessTier.COOL, headers.getSmartAccessTier());
        assertEquals(CHANGE_TIME, headers.getAccessTierChangeTime());
        assertNull(headers.isAccessTierInferred());
    }

    @Test
    public void downloadDerivedBlobPropertiesExposeAccessTierHeaders() {
        HttpHeaders rawHeaders = new HttpHeaders().set(X_MS_ACCESS_TIER, "Smart")
            .set(X_MS_ACCESS_TIER_CHANGE_TIME, CHANGE_TIME_HEADER)
            .set(X_MS_SMART_ACCESS_TIER, "Cool");
        BlobDownloadAsyncResponse response
            = new BlobDownloadAsyncResponse(null, 200, rawHeaders, Flux.empty(), fromRawHeaders(rawHeaders));

        BlobProperties properties = ModelHelper.buildBlobPropertiesResponse(response).getValue();

        assertEquals(AccessTier.SMART, properties.getAccessTier());
        assertEquals(AccessTier.COOL, properties.getSmartAccessTier());
        assertEquals(CHANGE_TIME, properties.getAccessTierChangeTime());
        assertNull(properties.isAccessTierInferred());
    }

    @Test
    public void downloadDerivedBlobPropertiesExposeInferredAccessTier() {
        HttpHeaders rawHeaders = new HttpHeaders().set(X_MS_ACCESS_TIER, "Hot").set(X_MS_ACCESS_TIER_INFERRED, "true");
        BlobDownloadAsyncResponse response
            = new BlobDownloadAsyncResponse(null, 200, rawHeaders, Flux.empty(), fromRawHeaders(rawHeaders));

        BlobProperties properties = ModelHelper.buildBlobPropertiesResponse(response).getValue();

        assertEquals(AccessTier.HOT, properties.getAccessTier());
        assertTrue(properties.isAccessTierInferred());
        assertNull(properties.getSmartAccessTier());
        assertNull(properties.getAccessTierChangeTime());
    }

    @Test
    public void settersRoundTrip() {
        BlobDownloadHeaders headers = new BlobDownloadHeaders();

        assertSame(headers, headers.setAccessTier(AccessTier.SMART));
        assertSame(headers, headers.setSmartAccessTier(AccessTier.COLD));
        assertSame(headers, headers.setAccessTierInferred(true));
        assertSame(headers, headers.setAccessTierChangeTime(CHANGE_TIME));

        assertEquals(AccessTier.SMART, headers.getAccessTier());
        assertEquals(AccessTier.COLD, headers.getSmartAccessTier());
        assertTrue(headers.isAccessTierInferred());
        assertEquals(CHANGE_TIME, headers.getAccessTierChangeTime());

        headers.setAccessTierInferred(false);
        assertFalse(headers.isAccessTierInferred());
    }

    @Test
    public void settersAcceptNull() {
        BlobDownloadHeaders headers = new BlobDownloadHeaders().setAccessTier(AccessTier.SMART)
            .setSmartAccessTier(AccessTier.HOT)
            .setAccessTierInferred(true)
            .setAccessTierChangeTime(CHANGE_TIME);

        headers.setAccessTier(null).setSmartAccessTier(null).setAccessTierInferred(null).setAccessTierChangeTime(null);

        assertNull(headers.getAccessTier());
        assertNull(headers.getSmartAccessTier());
        assertNull(headers.isAccessTierInferred());
        assertNull(headers.getAccessTierChangeTime());
    }
}
