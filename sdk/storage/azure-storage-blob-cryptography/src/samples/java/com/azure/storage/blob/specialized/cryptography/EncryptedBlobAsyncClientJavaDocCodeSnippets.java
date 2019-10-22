// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobParallelTransferOptions;
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
    private int bufferSize = 50;
    private int numberOfBuffers = 2;

    /**
     *
     */
    public EncryptedBlobAsyncClientJavaDocCodeSnippets() {
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#upload(Flux, BlobParallelTransferOptions)}
     */
    public void upload() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions
        client.upload(data, new BlobParallelTransferOptions(numberOfBuffers, bufferSize)).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadWithResponse(Flux, BlobParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void upload2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-BlobParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = new HashMap<>(Collections.singletonMap("metadata", "value"));
        BlobRequestConditions accessConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadWithResponse(data, new BlobParallelTransferOptions(numberOfBuffers, bufferSize), headers, metadata,
            AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadWithResponse#Flux-BlobParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions
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
     * Code snippet for {@link EncryptedBlobAsyncClient#uploadFromFile(String, BlobParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-BlobParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = new HashMap<>(Collections.singletonMap("metadata", "value"));
        BlobRequestConditions accessConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadFromFile(filePath, new BlobParallelTransferOptions(null, bufferSize), headers, metadata,
            AccessTier.HOT, accessConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobAsyncClient.uploadFromFile#String-BlobParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }
}
