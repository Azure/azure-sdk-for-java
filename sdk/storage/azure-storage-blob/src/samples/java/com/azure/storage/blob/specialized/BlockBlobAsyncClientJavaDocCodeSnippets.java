// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Code snippet for {@link BlockBlobAsyncClient}
 */
@SuppressWarnings({"unused"})
public class BlockBlobAsyncClientJavaDocCodeSnippets {
    private BlockBlobAsyncClient client = new SpecializedBlobClientBuilder().buildBlockBlobAsyncClient();
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private String leaseId = "leaseId";
    private String filePath = "filePath";
    private String base64BlockID = "base64BlockID";
    private URL sourceURL = new URL("https://example.com");
    private long offset = 1024L;
    private long count = length;
    private int blockSize = 50;
    private int numBuffers = 2;

    /**
     * @throws MalformedURLException Ignore
     */
    public BlockBlobAsyncClientJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, long)}
     */
    public void upload() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long
        client.upload(data, length).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMD5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadWithResponse(Flux, long, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void upload2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.uploadWithResponse(data, length, headers, metadata, AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMD5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, int, int)}
     */
    public void upload3() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-int-int
        client.upload(data, blockSize, numBuffers).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMD5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-int-int
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadWithResponse(Flux, int, int, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void upload4() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-int-int-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.uploadWithResponse(data, blockSize, numBuffers, headers, metadata, AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMD5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-int-int-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromFile(String)}
     */
    public void uploadFromFile() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromFile(String, Integer, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.uploadFromFile(filePath, BlockBlobAsyncClient.BLOB_MAX_UPLOAD_BLOCK_SIZE,
            headers, metadata, AccessTier.HOT, accessConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlock(String, Flux, long)}
     */
    public void stageBlock() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlock#String-Flux-long
        client.stageBlock(base64BlockID, data, length)
            .subscribe(
                response -> System.out.println("Staging block completed"),
                error -> System.out.printf("Error when calling stage Block: %s", error));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlock#String-Flux-long
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long, LeaseAccessConditions)}
     */
    public void stageBlock2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        client.stageBlockWithResponse(base64BlockID, data, length, accessConditions).subscribe(response ->
            System.out.printf("Staging block completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockFromURL(String, URL, BlobRange)}
     */
    public void stageBlockFromURL() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURL#String-URL-BlobRange
        client.stageBlockFromURL(base64BlockID, sourceURL, new BlobRange(offset, count))
            .subscribe(
                response -> System.out.println("Staging block completed"),
                error -> System.out.printf("Error when calling stage Block: %s", error));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURL#String-URL-BlobRange
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockFromURLWithResponse(String, URL, BlobRange, byte[], LeaseAccessConditions, SourceModifiedAccessConditions)}
     */
    public void stageBlockFromURL2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURLWithResponse#String-URL-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        SourceModifiedAccessConditions sourceModifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.stageBlockFromURLWithResponse(base64BlockID, sourceURL, new BlobRange(offset, count), null,
            leaseAccessConditions, sourceModifiedAccessConditions).subscribe(response ->
            System.out.printf("Staging block from URL completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromURLWithResponse#String-URL-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#listBlocks(BlockListType)}
     */
    public void listBlocks() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocks#BlockListType
        client.listBlocks(BlockListType.ALL).subscribe(block -> {
            System.out.println("Committed Blocks:");
            block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

            System.out.println("Uncommitted Blocks:");
            block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        });
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocks#BlockListType
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#listBlocksWithResponse(BlockListType, LeaseAccessConditions)}
     */
    public void listBlocks2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.listBlocksWithResponse(BlockListType.ALL, accessConditions).subscribe(response -> {
            BlockList block = response.getValue();
            System.out.println("Committed Blocks:");
            block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

            System.out.println("Uncommitted Blocks:");
            block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        });
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#commitBlockList(List)}
     */
    public void commitBlockList() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List
        client.commitBlockList(Collections.singletonList(base64BlockID)).subscribe(response ->
            System.out.printf("Committing block list completed. Last modified: %s%n", response.getLastModified()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#commitBlockListWithResponse(List, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void commitBlockList2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        client.commitBlockListWithResponse(Collections.singletonList(base64BlockID), headers, metadata,
            AccessTier.HOT, accessConditions).subscribe(response ->
                System.out.printf("Committing block list completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }
}
