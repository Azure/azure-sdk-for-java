// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobUploadFromUrlOptions;
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions;
import com.azure.storage.blob.options.BlockBlobListBlocksOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.options.BlockBlobStageBlockFromUrlOptions;
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
    private String tags = "tags";
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
     * Code snippet for {@link BlockBlobAsyncClient#uploadWithResponse(BlockBlobSimpleUploadOptions)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void upload3() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#BlockBlobSimpleUploadOptions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadWithResponse(new BlockBlobSimpleUploadOptions(data, length).setHeaders(headers)
            .setMetadata(metadata).setTags(tags).setTier(AccessTier.HOT).setContentMd5(md5)
            .setRequestConditions(requestConditions))
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadWithResponse#BlockBlobSimpleUploadOptions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromUrl(String)}
     */
    public void uploadFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String
        client.uploadFromUrl(sourceUrl)
            .subscribe(response ->
                System.out.printf("Uploaded BlockBlob from URL, MD5 is %s%n",
                    Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromUrl(String, boolean)}
     */
    public void uploadFromUrlWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String-boolean
        boolean overwrite = false; // Default behavior
        client.uploadFromUrl(sourceUrl, overwrite).subscribe(response ->
            System.out.printf("Uploaded BlockBlob from URL, MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrl#String-boolean
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromUrlWithResponse(BlobUploadFromUrlOptions)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void uploadFromUrlWithResponse() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrlWithResponse#BlobUploadFromUrlOptions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadFromUrlWithResponse(new BlobUploadFromUrlOptions(sourceUrl).setHeaders(headers)
            .setTags(tags).setTier(AccessTier.HOT).setContentMd5(md5)
            .setDestinationRequestConditions(requestConditions))
            .subscribe(response -> System.out.printf("Uploaded BlockBlob from URL, MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.uploadFromUrlWithResponse#BlobUploadFromUrlOptions
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
     * Code snippet for {@link BlockBlobAsyncClient#stageBlockFromUrlWithResponse(String, String, BlobRange, byte[], String, BlobRequestConditions)}
     */
    public void stageBlockFromUrlOptionsBag() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#BlockBlobStageBlockFromUrlOptions
        BlobRequestConditions sourceRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.stageBlockFromUrlWithResponse(new BlockBlobStageBlockFromUrlOptions(base64BlockID, sourceUrl)
            .setSourceRange(new BlobRange(offset, count)).setLeaseId(leaseId)
            .setSourceRequestConditions(sourceRequestConditions)).subscribe(response ->
            System.out.printf("Staging block from URL completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.stageBlockFromUrlWithResponse#BlockBlobStageBlockFromUrlOptions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#listBlocks(BlockListType)}
     */
    public void listBlocks() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocks#BlockListType
        client.listBlocks(BlockListType.ALL).subscribe(block -> {
            System.out.println("Committed Blocks:");
            block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSizeLong()));

            System.out.println("Uncommitted Blocks:");
            block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSizeLong()));
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
            block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSizeLong()));

            System.out.println("Uncommitted Blocks:");
            block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSizeLong()));
        });
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockListType-String
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#listBlocksWithResponse(BlockBlobListBlocksOptions)}
     */
    public void listBlocks3() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockBlobListBlocksOptions
        client.listBlocksWithResponse(new BlockBlobListBlocksOptions(BlockListType.ALL)
            .setLeaseId(leaseId)
            .setIfTagsMatch(tags)).subscribe(response -> {
                BlockList block = response.getValue();
                System.out.println("Committed Blocks:");
                block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(),
                    b.getSizeLong()));

                System.out.println("Uncommitted Blocks:");
                block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(),
                    b.getSizeLong()));
            });
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.listBlocksWithResponse#BlockBlobListBlocksOptions
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

    /**
     * Code snippet for {@link BlockBlobAsyncClient#commitBlockListWithResponse(BlockBlobCommitBlockListOptions)}
     */
    public void commitBlockList3() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#BlockBlobCommitBlockListOptions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        client.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(Collections.singletonList(base64BlockID))
            .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(AccessTier.HOT)
            .setRequestConditions(requestConditions))
            .subscribe(response ->
            System.out.printf("Committing block list completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlockBlobAsyncClient.commitBlockListWithResponse#BlockBlobCommitBlockListOptions
    }
}
