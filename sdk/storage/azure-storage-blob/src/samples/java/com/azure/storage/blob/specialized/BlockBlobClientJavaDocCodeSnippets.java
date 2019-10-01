// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlockList;
import com.azure.storage.blob.models.BlockListType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.SourceModifiedAccessConditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private String filePath = "filePath";
    private String base64BlockID = "base64BlockID";
    private URL sourceURL = new URL("https://example.com");
    private long offset = 1024L;
    private long count = length;

    /**
     * @throws MalformedURLException Ignore
     */
    public BlockBlobClientJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippet for {@link BlockBlobClient#upload(InputStream, long)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void upload() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long
        System.out.printf("Uploaded BlockBlob MD5 is %s%n",
            Base64.getEncoder().encodeToString(client.upload(data, length).getContentMD5()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.upload#InputStream-long
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadWithResponse(InputStream, long, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions, Duration, Context)}
     */
    public void upload2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration-Context
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("key", "value");

        System.out.printf("Uploaded BlockBlob MD5 is %s%n", Base64.getEncoder()
            .encodeToString(client.uploadWithResponse(data, length, headers, metadata, AccessTier.HOT,
                accessConditions, timeout, context)
                .getValue()
                .getContentMD5()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadWithResponse#InputStream-long-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadFromFile(String)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#String
        try {
            client.uploadFromFile(filePath);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadFromFile(String, Integer, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions, Duration)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile2() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Integer blockSize = 100 * 1024 * 1024; // 100 MB;

        try {
            client.uploadFromFile(filePath, blockSize, headers, metadata,
                AccessTier.HOT, accessConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlock(String, InputStream, long)}
     */
    public void stageBlock() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long
        client.stageBlock(base64BlockID, data, length);
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlock#String-InputStream-long
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockWithResponse(String, InputStream, long, LeaseAccessConditions, Duration, Context)}
     */
    public void stageBlock2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("key", "value");
        System.out.printf("Staging block completed with status %d%n",
            client.stageBlockWithResponse(base64BlockID, data, length, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockWithResponse#String-InputStream-long-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockFromURL(String, URL, BlobRange)}
     */
    public void stageBlockFromURL() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromURL#String-URL-BlobRange
        client.stageBlockFromURL(base64BlockID, sourceURL, new BlobRange(offset, count));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromURL#String-URL-BlobRange
    }

    /**
     * Code snippet for {@link BlockBlobClient#stageBlockFromURLWithResponse(String, URL, BlobRange, byte[], LeaseAccessConditions, SourceModifiedAccessConditions, Duration, Context)}
     */
    public void stageBlockFromURL2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromURLWithResponse#String-URL-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions-Duration-Context
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        SourceModifiedAccessConditions sourceModifiedAccessConditions = new SourceModifiedAccessConditions()
            .setSourceIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("key", "value");

        System.out.printf("Staging block from URL completed with status %d%n",
            client.stageBlockFromURLWithResponse(base64BlockID, sourceURL, new BlobRange(offset, count), null,
                leaseAccessConditions, sourceModifiedAccessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.stageBlockFromURLWithResponse#String-URL-BlobRange-byte-LeaseAccessConditions-SourceModifiedAccessConditions-Duration-Context
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
     * Code snippet for {@link BlockBlobClient#listBlocksWithResponse(BlockListType, LeaseAccessConditions, Duration, Context)}
     */
    public void listBlocks2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("key", "value");
        BlockList block = client.listBlocksWithResponse(BlockListType.ALL, accessConditions, timeout, context).getValue();

        System.out.println("Committed Blocks:");
        block.getCommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));

        System.out.println("Uncommitted Blocks:");
        block.getUncommittedBlocks().forEach(b -> System.out.printf("Name: %s, Size: %d", b.getName(), b.getSize()));
        // END: com.azure.storage.blob.specialized.BlockBlobClient.listBlocksWithResponse#BlockListType-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#commitBlockList(List)}
     */
    public void commitBlockList() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List
        System.out.printf("Committing block list completed. Last modified: %s%n",
            client.commitBlockList(Collections.singletonList(base64BlockID)).getLastModified());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.commitBlockList#List
    }

    /**
     * Code snippet for {@link BlockBlobClient#commitBlockListWithResponse(List, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions, Duration, Context)}
     */
    public void commitBlockList2() {
        // BEGIN: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration-Context
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("key", "value");

        System.out.printf("Committing block list completed with status %d%n",
            client.commitBlockListWithResponse(Collections.singletonList(base64BlockID), headers, metadata,
                AccessTier.HOT, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlockBlobClient.uploadFromFile#List-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions-Duration-Context
    }
}
