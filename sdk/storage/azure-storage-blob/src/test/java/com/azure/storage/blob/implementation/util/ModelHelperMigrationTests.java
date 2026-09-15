// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.implementation.models.BlobCopySourceTags;
import com.azure.storage.blob.implementation.models.BlobTag;
import com.azure.storage.blob.implementation.models.BlobTags;
import com.azure.storage.blob.models.BlobCopySourceTagsMode;
import com.azure.storage.blob.models.BlobServiceStatistics;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the {@link ModelHelper} transformations the TypeSpec migration introduced. These sit between the
 * hand-written clients and the generated layer, so playback coverage alone would not say which side of a
 * round-trip was wrong.
 */
public class ModelHelperMigrationTests {

    /**
     * Metadata is written as an {@code x-ms-meta-<key>} header collection on the request options rather than passed
     * to the generated parameter, so this asserts on the request that actually goes out.
     */
    @Test
    public void metadataIsSentAsThePrefixedHeaderCollection() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        HttpClient capturingClient = request -> {
            captured.set(request);
            return Mono.just(new MockHttpResponse(request, 200));
        };

        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net")
                .containerName("container")
                .credential(new StorageSharedKeyCredential("accountName", "YWNjb3VudEtleQ=="))
                .httpClient(capturingClient)
                .buildClient();

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("foo", "bar");
        metadata.put("empty", "");
        containerClient.setMetadata(metadata);

        HttpRequest request = captured.get();
        assertNotNull(request);
        assertEquals("bar", request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-meta-foo")));
        assertEquals("", request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-meta-empty")));
        // The single-header form the emitter would otherwise produce must not be on the wire.
        assertNull(request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-meta")));
    }

    /**
     * The shipped SDK sends Accept: application/xml even on operations that return no body; the generated layer
     * would otherwise fall back to the transport default.
     */
    @Test
    public void operationsWithNoResponseBodySendTheXmlAcceptHeader() {
        AtomicReference<HttpRequest> captured = new AtomicReference<>();
        HttpClient capturingClient = request -> {
            captured.set(request);
            return Mono.just(new MockHttpResponse(request, 202));
        };

        BlobContainerClient containerClient
            = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net")
                .containerName("container")
                .credential(new StorageSharedKeyCredential("accountName", "YWNjb3VudEtleQ=="))
                .httpClient(capturingClient)
                .buildClient();

        containerClient.delete();

        assertEquals("application/xml", captured.get().getHeaders().getValue(HttpHeaderName.ACCEPT));
    }

    @ParameterizedTest
    @CsvSource({ "REPLACE,REPLACE", "COPY,COPY" })
    public void toCopySourceTagsMapsOntoTheGeneratedEnum(String mode, String expected) {
        BlobCopySourceTags actual = ModelHelper.toCopySourceTags(BlobCopySourceTagsMode.fromString(mode));
        assertNotNull(actual);
        assertEquals(expected, actual.toString());
    }

    @Test
    public void toCopySourceTagsPassesNullThrough() {
        assertNull(ModelHelper.toCopySourceTags(null));
    }

    @Test
    public void xmlBodyRoundTripsThroughSerializeAndDeserialize() {
        BlobTags tags = new BlobTags().setBlobTagSet(
            Arrays.asList(new BlobTag().setKey("k1").setValue("v1"), new BlobTag().setKey("k2").setValue("v2")));

        BinaryData serialized = ModelHelper.serializeXmlBody(tags);
        assertNotNull(serialized);

        BlobTags deserialized = ModelHelper.deserializeXmlBody(serialized, BlobTags::fromXml);
        assertEquals(2, deserialized.getBlobTagSet().size());
        assertEquals("k1", deserialized.getBlobTagSet().get(0).getKey());
        assertEquals("v1", deserialized.getBlobTagSet().get(0).getValue());
        assertEquals("k2", deserialized.getBlobTagSet().get(1).getKey());
    }

    @Test
    public void serializeXmlBodyPassesNullThrough() {
        assertNull(ModelHelper.serializeXmlBody(null));
    }

    @Test
    public void deserializeXmlBodyReadsAServiceResponseBody() {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<StorageServiceStats><GeoReplication><Status>live</Status>"
            + "<LastSyncTime>Wed, 21 Oct 2020 07:28:00 GMT</LastSyncTime></GeoReplication></StorageServiceStats>";

        BlobServiceStatistics statistics
            = ModelHelper.deserializeXmlBody(BinaryData.fromString(xml), BlobServiceStatistics::fromXml);

        assertNotNull(statistics.getGeoReplication());
        assertEquals("live", statistics.getGeoReplication().getStatus().toString());
        assertNotNull(statistics.getGeoReplication().getLastSyncTime());
    }
}
