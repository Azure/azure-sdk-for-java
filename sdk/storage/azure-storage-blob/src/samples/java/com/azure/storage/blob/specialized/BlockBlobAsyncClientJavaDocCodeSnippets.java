// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Code snippet for {@link BlockBlobAsyncClient}
 */
@SuppressWarnings({"unused"})
public class BlockBlobAsyncClientJavaDocCodeSnippets {
    private BlockBlobAsyncClient client = new SpecializedBlobClientBuilder().buildBlockBlobAsyncClient();
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private String leaseId = "leaseId";
    private String base64BlockID = "base64BlockID";
    private String sourceUrl = "https://example.com";
    private long offset = 1024L;
    private long count = length;
    private byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));

    /**
     * Constructor for snippets.
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public BlockBlobAsyncClientJavaDocCodeSnippets() throws NoSuchAlgorithmException {
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, long)}
     */
    public void upload() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long
        client.upload(data, length).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, long, boolean)}
     */
    public void uploadWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long-boolean
        boolean overwrite = false; // Default behavior
        client.upload(data, length, overwrite).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.upload#Flux-long-boolean
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadWithResponse(Flux, long, BlobHttpHeaders, Map, AccessTier, byte[], BlobRequestConditions)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void upload2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadWithResponse(data, length, headers, metadata, AccessTier.HOT, md5, requestConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions
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
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockWithResponse(String, Flux, long, byte[], String)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void stageBlock2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-byte-String
        client.stageBlockWithResponse(base64BlockID, data, length, md5, leaseId).subscribe(response ->
            System.out.printf("Staging block completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockWithResponse#String-Flux-long-byte-String
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockFromUrl(String, String, BlobRange)}
     */
    public void stageBlockFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrl#String-String-BlobRange
        client.stageBlockFromUrl(base64BlockID, sourceUrl, new BlobRange(offset, count))
            .subscribe(
                response -> System.out.println("Staging block completed"),
                error -> System.out.printf("Error when calling stage Block: %s", error));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrl#String-String-BlobRange
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockFromUrlWithResponse(String, String, BlobRange, byte[], String, BlobRequestConditions)}
     */
    public void stageBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions
        BlobRequestConditions sourceRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.stageBlockFromUrlWithResponse(base64BlockID, sourceUrl, new BlobRange(offset, count), null,
            leaseId, sourceRequestConditions).subscribe(response ->
            System.out.printf("Staging block from URL completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions
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
     * Code snippet for {@link BlockBlobAsyncClient#listBlocksWithResponse(BlockListType, String)}
     */
    public void listBlocks2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-String
        client.listBlocksWithResponse(BlockListType.ALL, leaseId).subscribe(response -> {
            BlockList block = response.getValue();
            System.out.println("Committed Blocks:");
            block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

            System.out.println("Uncommitted Blocks:");
            block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        });
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-String
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
     * Code snippet for {@link BlockBlobAsyncClient#commitBlockList(List, boolean)}
     */
    public void commitBlockListWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List-boolean
        boolean overwrite = false; // Default behavior
        client.commitBlockList(Collections.singletonList(base64BlockID), overwrite).subscribe(response ->
            System.out.printf("Committing block list completed. Last modified: %s%n", response.getLastModified()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockList#List-boolean
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#commitBlockListWithResponse(List, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void commitBlockList2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        client.commitBlockListWithResponse(Collections.singletonList(base64BlockID), headers, metadata,
            AccessTier.HOT, requestConditions).subscribe(response ->
                System.out.printf("Committing block list completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }
}
