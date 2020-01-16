// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Code snippet for {@link BlockBlobClient}
 */
@SuppressWarnings({"unused"})
public class BlockBlobClientJavaDocCodeSnippets {
    private BlockBlobClient client = new SpecializedBlobClientBuilder().buildBlockBlobClient();
    private InputStream data = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
    private long length = 4L;
    private Duration timeout = Duration.ofSeconds(30);
    private String leaseId = "leaseId";
    private String base64BlockId = "base64BlockID";
    private String sourceUrl = "https://example.com";
    private long offset = 1024L;
    private long count = length;
    private byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));

    /**
     * Constructor for code snippets.
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public BlockBlobClientJavaDocCodeSnippets() throws NoSuchAlgorithmException {
    }

    /**
     * Code snippet for {@link BlockBlobClient#upload(InputStream, long)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void upload() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long
        System.out.printf("Uploaded BlockBlob MD5 is %s%n",
            Base64.getEncoder().encodeToString(client.upload(data, length).getContentMd5()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long
    }

    /**
     * Code snippet for {@link BlockBlobClient#upload(InputStream, long, boolean)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadWithOverwrite() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long-boolean
        boolean overwrite = false;
        System.out.printf("Uploaded BlockBlob MD5 is %s%n",
            Base64.getEncoder().encodeToString(client.upload(data, length, overwrite).getContentMd5()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long-boolean
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadWithResponse(InputStream, long, BlobHttpHeaders, Map, AccessTier, byte[], BlobRequestConditions, Duration, Context)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void upload2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions-Duration-Context
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");

        byte[] md5 = MessageDigest.getInstance("MD5").digest("data".getBytes(StandardCharsets.UTF_8));

        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Uploaded BlockBlob MD5 is %s%n", Base64.getEncoder()
            .encodeToString(client.uploadWithResponse(data, length, headers, metadata, AccessTier.HOT, md5,
                requestConditions, timeout, context)
                .getValue()
                .getContentMd5()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHttpHeaders-Map-AccessTier-byte-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlock(String, InputStream, long)}
     */
    public void stageBlock() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long
        client.stageBlock(base64BlockId, data, length);
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockWithResponse(String, InputStream, long, byte[], String, Duration, Context)}
     *
     * @throws NoSuchAlgorithmException If Md5 calculation fails
     */
    public void stageBlock2() throws NoSuchAlgorithmException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-byte-String-Duration-Context
        Context context = new Context("key", "value");
        System.out.printf("Staging block completed with status %d%n",
            client.stageBlockWithResponse(base64BlockId, data, length, md5, leaseId, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-byte-String-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockFromUrl(String, String, BlobRange)}
     */
    public void stageBlockFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrl#String-String-BlobRange
        client.stageBlockFromUrl(base64BlockId, sourceUrl, new BlobRange(offset, count));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrl#String-String-BlobRange
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockFromUrlWithResponse(String, String, BlobRange, byte[], String, BlobRequestConditions, Duration, Context)}
     */
    public void stageBlockFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions-Duration-Context
        BlobRequestConditions sourceRequestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Staging block from URL completed with status %d%n",
            client.stageBlockFromUrlWithResponse(base64BlockId, sourceUrl, new BlobRange(offset, count), null,
                leaseId, sourceRequestConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromUrlWithResponse#String-String-BlobRange-byte-String-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#listBlocks(BlockListType)}
     */
    public void listBlocks() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.listBlocks#BlockListType
        BlockList block = client.listBlocks(BlockListType.ALL);

        System.out.println("Committed Blocks:");
        block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

        System.out.println("Uncommitted Blocks:");
        block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.listBlocks#BlockListType
    }

    /**
     * Code snippet for {@link BlockBlobClient#listBlocksWithResponse(BlockListType, String, Duration, Context)}
     */
    public void listBlocks2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-String-Duration-Context
        Context context = new Context("key", "value");
        BlockList block = client.listBlocksWithResponse(BlockListType.ALL, leaseId, timeout, context).getValue();

        System.out.println("Committed Blocks:");
        block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

        System.out.println("Uncommitted Blocks:");
        block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-String-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#commitBlockList(List)}
     */
    public void commitBlockList() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List
        System.out.printf("Committing block list completed. Last modified: %s%n",
            client.commitBlockList(Collections.singletonList(base64BlockId)).getLastModified());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List
    }

    /**
     * Code snippet for {@link BlockBlobClient#commitBlockList(List, boolean)}
     */
    public void commitBlockListWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List-boolean
        boolean overwrite = false; // Default behavior
        System.out.printf("Committing block list completed. Last modified: %s%n",
            client.commitBlockList(Collections.singletonList(base64BlockId), overwrite).getLastModified());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List-boolean
    }

    /**
     * Code snippet for {@link BlockBlobClient#commitBlockListWithResponse(List, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions, Duration, Context)}
     */
    public void commitBlockList2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration-Context
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Committing block list completed with status %d%n",
            client.commitBlockListWithResponse(Collections.singletonList(base64BlockId), headers, metadata,
                AccessTier.HOT, requestConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration-Context
    }
}
