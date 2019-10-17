// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link AppendBlobAsyncClient}
 */
public class AppendBlobAsyncClientJavaDocCodeSnippets {
    private AppendBlobAsyncClient client = new SpecializedBlobClientBuilder().buildAppendBlobAsyncClient();
    private String leaseId = "leaseId";
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private static final Long POSITION = null;
    private Long maxSize = length;
    private URL sourceUrl = new URL("https://example.com");
    private long offset = 1024;
    private long count = 1024;

    /**
     * @throws MalformedURLException Ignore
     */
    public AppendBlobAsyncClientJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.create
        client.create().subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getLastModified()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.create
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#createWithResponse(boolean, BlobHttpHeaders, Map, BlobAccessConditions)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.createWithResponse#boolean-BlobHttpHeaders-Map-BlobAccessConditions
        boolean overwrite = false; // default behavior
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setBlobContentType("binary")
            .setBlobContentLanguage("en-US");
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.createWithResponse(overwrite, headers, metadata, accessConditions).subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getValue().getLastModified()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.createWithResponse#BlobHttpHeaders-Map-BlobAccessConditions
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlock(Flux, long)}
     */
    public void appendBlock() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlock#Flux-long
        client.appendBlock(data, length).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlock#Flux-long
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockWithResponse(Flux, long, AppendBlobAccessConditions)}
     */
    public void appendBlock2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-AppendBlobAccessConditions
        AppendBlobAccessConditions accessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        client.appendBlockWithResponse(data, length, accessConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-AppendBlobAccessConditions
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrl(URL, BlobRange)}
     */
    public void appendBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrl#URL-BlobRange
        client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count)).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrl#URL-BlobRange
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrlWithResponse(URL, BlobRange, byte[], AppendBlobAccessConditions, SourceModifiedAccessConditions)}
     */
    public void appendBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions
        AppendBlobAccessConditions appendBlobAccessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        SourceModifiedAccessConditions modifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.appendBlockFromUrlWithResponse(sourceUrl, new BlobRange(offset, count), null,
            appendBlobAccessConditions, modifiedAccessConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions
    }
}
