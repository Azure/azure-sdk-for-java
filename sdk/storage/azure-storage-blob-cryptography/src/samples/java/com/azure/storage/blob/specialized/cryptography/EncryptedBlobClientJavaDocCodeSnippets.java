// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.ParallelTransferOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Code snippet for {@link EncryptedBlobClient}
 */
@SuppressWarnings({"unused"})
public class EncryptedBlobClientJavaDocCodeSnippets {
    private EncryptedBlobClient client = JavaDocCodeSnippetsHelpers.getEncryptedBlobClient(
        "blobName", "containerName");
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
    public EncryptedBlobClientJavaDocCodeSnippets() {
    }
    /**
     * Code snippet for {@link EncryptedBlobClient#uploadFromFile(String)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String
        try {
            client.uploadFromFile(filePath);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link EncryptedBlobClient#uploadFromFile(String, boolean)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFileWithOverwrite() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean
        try {
            boolean overwrite = false; // Default value
            client.uploadFromFile(filePath, overwrite);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-boolean
    }

    /**
     * Code snippet for {@link EncryptedBlobClient#uploadFromFile(String, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions, Duration)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile2() throws IOException {
        // BEGIN: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = new HashMap<>(Collections.singletonMap("metadata", "value"));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        int blockSize = 100 * 1024 * 1024; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, null, null);

        try {
            client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata, AccessTier.HOT,
                requestConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.specialized.cryptography.EncryptedBlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration
    }
}
