// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;

/**
 * Code snippet for {@link BlockBlobAsyncClient}
 */
@SuppressWarnings({"unused"})
public class EncryptedBlockBlobAsyncClientJavaDocCodeSnippets {
    private EncryptedBlockBlobAsyncClient client = JavaDocCodeSnippetsHelpers.getEncryptedBlockBlobAsyncClient(
        "blobName", "containerName");

    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private long length = 4L;
    private String leaseId = "leaseId";
    private String filePath = "filePath";
    private String base64BlockID = "base64BlockID";
    private URL sourceURL = JavaDocCodeSnippetsHelpers.generateURL("https://example.com");
    private long offset = 1024L;
    private long count = length;
    private int blockSize = 50;
    private int numBuffers = 2;

    /**
     *
     */
    public EncryptedBlockBlobAsyncClientJavaDocCodeSnippets() {
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, long)}
     */
    public void upload() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-long
        client.upload(data, length).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMD5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-long
    }

    /**
     * Code snippet for {@link EncryptedBlockBlobAsyncClient#uploadWithResponse(Flux, long, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void upload2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
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
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#upload(Flux, ParallelTransferOptions)}
     */
    public void upload3() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-ParallelTransferOptions
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize)
            .setNumBuffers(numBuffers);
        client.upload(data, parallelTransferOptions).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMD5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void upload4() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize)
            .setNumBuffers(numBuffers);

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMD5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromFile(String)}
     */
    public void uploadFromFile() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlockBlobAsyncClient#uploadFromFile(String, ParallelTransferOptions, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize);

        client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT, accessConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlockBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions
    }
}
