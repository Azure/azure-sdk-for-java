// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private String sourceUrl = "https://example.com";
    private long offset = 1024;
    private long count = 1024;

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
     * Code snippet for {@link AppendBlobAsyncClient#create(boolean)}
     */
    public void createWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.create#boolean
        boolean overwrite = false; // Default behavior
        client.create(overwrite).subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getLastModified()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.create#boolean
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#createWithResponse(BlobHttpHeaders, Map, BlobRequestConditions)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.createWithResponse#BlobHttpHeaders-Map-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentType("binary")
            .setContentLanguage("en-US");
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.createWithResponse(headers, metadata, requestConditions).subscribe(response ->
            System.out.printf("Created AppendBlob at %s%n", response.getValue().getLastModified()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.createWithResponse#BlobHttpHeaders-Map-BlobRequestConditions
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
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockWithResponse(Flux, long, byte[], AppendBlobRequestConditions)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void appendBlock2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-byte-AppendBlobRequestConditions
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        AppendBlobRequestConditions requestConditions = new AppendBlobRequestConditions()
            .setAppendPosition(POSITION)
            .setMaxSize(maxSize);

        client.appendBlockWithResponse(data, length, md5, requestConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockWithResponse#Flux-long-byte-AppendBlobRequestConditions
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrl(String, BlobRange)}
     */
    public void appendBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrl#String-BlobRange
        client.appendBlockFromUrl(sourceUrl, new BlobRange(offset, count)).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrl#String-BlobRange
    }

    /**
     * Code snippet for {@link AppendBlobAsyncClient#appendBlockFromUrlWithResponse(String, BlobRange, byte[], AppendBlobRequestConditions, BlobRequestConditions)}
     */
    public void appendBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#String-BlobRange-byte-AppendBlobRequestConditions-BlobRequestConditions
        AppendBlobRequestConditions appendBlobRequestConditions = new AppendBlobRequestConditions()
            .setAppendPosition(POSITION)
            .setMaxSize(maxSize);

        BlobRequestConditions modifiedRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.appendBlockFromUrlWithResponse(sourceUrl, new BlobRange(offset, count), null,
            appendBlobRequestConditions, modifiedRequestConditions).subscribe(response ->
            System.out.printf("AppendBlob has %d committed blocks%n", response.getValue().getBlobCommittedBlockCount()));
        // END: com.azure.storage.blob.specialized.AppendBlobAsyncClient.appendBlockFromUrlWithResponse#String-BlobRange-byte-AppendBlobRequestConditions-BlobRequestConditions
    }
}
