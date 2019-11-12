// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Code snippet for {@link BlockBlobAsyncClient}
 */
@SuppressWarnings({"unused"})
public class EncryptedBlobAsyncClientJavaDocCodeSnippets {
    private EncryptedBlobAsyncClient client = JavaDocCodeSnippetsHelpers.getEncryptedBlobAsyncClient(
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
    public EncryptedBlobAsyncClientJavaDocCodeSnippets() {
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#upload(Flux, ParallelTransferOptions)}
     */
    public void upload() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, numBuffers, null);
        client.upload(data, parallelTransferOptions).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#upload(Flux, ParallelTransferOptions, boolean)}
     */
    public void uploadWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, numBuffers, null);
        boolean overwrite = false; // Default behavior
        client.upload(data, parallelTransferOptions, overwrite).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void upload2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = new HashMap<>(Collections.singletonMap("metadata", "value"));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, numBuffers, null);

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadFromFile(String)}
     */
    public void uploadFromFile() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadFromFile(String, boolean)}
     */
    public void uploadFromFileWithOverwrite() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-boolean
        boolean overwrite = false; // Default behavior
        client.uploadFromFile(filePath, overwrite)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-boolean
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadFromFile(String, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = new HashMap<>(Collections.singletonMap("metadata", "value"));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, null, null);

        client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }
}
