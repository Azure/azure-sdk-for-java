// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.AppendBlobAccessConditions;
import com.azure.storage.blob.models.AppendPositionAccessConditions;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import reactor.core.publisher.Flux;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link AppendBlobAsyncClient}
 */
public class AppendBlobAsyncClientJavaDocCodeSnippets {
    private AppendBlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blobName")
        .asAppendBlobAsyncClient();
    private String leaseId = "leaseId";
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes("UTF-8")));
    private long length = 4L;
    private static final Long POSITION = null;
    private Long maxSize = length;
    private URL sourceUrl = JavaDocCodeSnippetsHelpers.generateURL("https://example.com");
    private long offset = 1024;
    private long count = 1024;

    /**
     *
     * @throws UnsupportedEncodingException if cannot get bytes from sample sting as utf-8 encoding
     */
    AppendBlobAsyncClientJavaDocCodeSnippets() throws UnsupportedEncodingException {
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.create
        client.create().subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getLastModified()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.create
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#createWithResponse(BlobHTTPHeaders, Metadata, BlobAccessConditions)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.createWithResponse#BlobHTTPHeaders-Metadata-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentType("binary")
            .setBlobContentLanguage("en-US");
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.createWithResponse(headers, metadata, accessConditions).subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getValue().getLastModified()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.createWithResponse#BlobHTTPHeaders-Metadata-BlobAccessConditions
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlock(Flux, long)}
     */
    public void appendBlock() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.appendBlock#Flux-long
        client.appendBlock(data, length).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.appendBlock#Flux-long
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockWithResponse(Flux, long, AppendBlobAccessConditions)}
     */
    public void appendBlock2() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-AppendBlobAccessConditions
        AppendBlobAccessConditions accessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        client.appendBlockWithResponse(data, length, accessConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-AppendBlobAccessConditions
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrl(URL, BlobRange)}
     */
    public void appendBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockFromUrl#URL-BlobRange
        client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count)).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockFromUrl#URL-BlobRange
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrlWithResponse(URL, BlobRange, byte[], AppendBlobAccessConditions, SourceModifiedAccessConditions)}
     */
    public void appendBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions
        AppendBlobAccessConditions appendBlobAccessConditions = new AppendBlobAccessConditions()
            .setAppendPositionAccessConditions(new AppendPositionAccessConditions()
                .setAppendPosition(POSITION)
                .setMaxSize(maxSize));

        SourceModifiedAccessConditions modifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.appendBlockFromUrlWithResponse(sourceUrl, new BlobRange(offset, count), null,
            appendBlobAccessConditions, modifiedAccessConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#URL-BlobRange-byte-AppendBlobAccessConditions-SourceModifiedAccessConditions
    }

    /**
     * Generates a code sample for using {@link AppendBlobAsyncClient#getName()}
     */
    public void getNameAsync() {
        // BEGIN: com.azure.storage.blob.AppendBlobAsyncClient.getName
        String appendBlobName = client.getName();
        System.out.println("The name of the append blob is " + appendBlobName);
        // END: com.azure.storage.blob.AppendBlobAsyncClient.getName
    }
}
