// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;

/**
 * Code snippet for {@link EncryptedBlockBlobClient}
 */
@SuppressWarnings({"unused"})
public class EncryptedBlockBlobClientJavaDocCodeSnippets {
    private EncryptedBlockBlobClient client = JavaDocCodeSnippetsHelpers.getEncryptedBlockBlobClient
        ("blobName", "containerName");
    private InputStream data = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
    private long length = 4L;
    private Duration timeout = Duration.ofSeconds(30);
    private String leaseId = "leaseId";
    private String filePath = "filePath";
    private String base64BlockID = "base64BlockID";
    private long offset = 1024L;
    private long count = length;

    /**
     *
     */
    public EncryptedBlockBlobClientJavaDocCodeSnippets() {
    }

    /**
     * Code snippet for {@link BlockBlobClient#upload(InputStream, long)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void upload() throws IOException {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.upload#InputStream-long
        System.out.printf("Uploaded BlockBlob MD5 is %s%n",
            Base64.getEncoder().encodeToString(client.upload(data, length).getContentMD5()));
        // END: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.upload#InputStream-long
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadWithResponse(InputStream, long, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions, Duration, Context)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void upload2() throws IOException {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadWithResponse#InputStream-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration-Context
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
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
        // END: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadWithResponse#InputStream-long-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadFromFile(String)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile() throws IOException {
        // BEGIN: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String
        try {
            client.uploadFromFile(filePath);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlockBlobClient#uploadFromFile(String, Integer, BlobHTTPHeaders, Metadata, AccessTier, BlobAccessConditions, Duration)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile2() throws IOException {
        // BEGIN: 'com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Integer blockSize = 100 * 1024 * 1024; // 100 MB;

        try {
            client.uploadFromFile(filePath, blockSize, headers, metadata, AccessTier.HOT,
                accessConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: 'com.azure.storage.blob.cryptography.EncryptedBlockBlobClient.uploadFromFile#String-Integer-BlobHTTPHeaders-Metadata-AccessTier-BlobAccessConditions-Duration
    }
}
