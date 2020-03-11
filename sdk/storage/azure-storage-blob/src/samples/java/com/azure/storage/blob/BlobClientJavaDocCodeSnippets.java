// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.common.implementation.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link BlobClient}
 */
@SuppressWarnings("unused")
public class BlobClientJavaDocCodeSnippets {
    private BlobClient client = JavaDocCodeSnippetsHelpers.getBlobClient("blobName");
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";
    private String filePath = "filePath";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

    /**
     * Code snippets for {@link BlobClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.BlobClient.exists
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromUrl(String)}
     */
    public void abortCopyFromUrl() {
        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromUrl#String
        client.abortCopyFromUrl(copyId);
        System.out.println("Aborted copy completed.");
        // END: com.azure.storage.blob.BlobClient.abortCopyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobClient#copyFromUrl(String)}
     */
    public void copyFromUrl() {
        // BEGIN: com.azure.storage.blob.BlobClient.copyFromUrl#String
        System.out.printf("Copy identifier: %s%n", client.copyFromUrl(url));
        // END: com.azure.storage.blob.BlobClient.copyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobClient#download(OutputStream)}
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.BlobClient.download#OutputStream
        client.download(new ByteArrayOutputStream());
        System.out.println("Download completed.");
        // END: com.azure.storage.blob.BlobClient.download#OutputStream
    }

    /**
     * Code snippets for {@link BlobClient#downloadToFile(String)} and
     * {@link BlobClient#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, DownloadRetryOptions, BlobRequestConditions, boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions(4 * Constants.MB, null, null),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.delete
        client.delete();
        System.out.println("Delete completed.");
        // END: com.azure.storage.blob.BlobClient.delete
    }

    /**
     * Code snippets for {@link BlobClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobClient.getProperties
        BlobProperties properties = client.getProperties();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.BlobClient.getProperties
    }

    /**
     * Code snippets for {@link BlobClient#setHttpHeaders(BlobHttpHeaders)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHttpHeaders
        client.setHttpHeaders(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        System.out.println("Set HTTP headers completed");
        // END: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHttpHeaders
    }

    /**
     * Code snippets for {@link BlobClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadata#Metadata
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.blob.BlobClient.setMetadata#Metadata
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshot()}
     */
    public void createSnapshot() {
        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshot
        System.out.printf("Identifier for the snapshot is %s%n", client.createSnapshot().getSnapshotId());
        // END: com.azure.storage.blob.BlobClient.createSnapshot
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTier(AccessTier)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier
        client.setAccessTier(AccessTier.HOT);
        System.out.println("Set tier completed.");
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier


    }

    /**
     * Code snippets for {@link BlobClient#undelete()}
     */
    public void unsetDelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.undelete
        client.undelete();
        System.out.println("Undelete completed");
        // END: com.azure.storage.blob.BlobClient.undelete
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobClient#existsWithResponse(Duration, Context)}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.existsWithResponse#Duration-Context
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, new Context(key2, value2)).getValue());
        // END: com.azure.storage.blob.BlobClient.existsWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromUrlWithResponse(String, String, Duration, Context)}
     */
    public void abortCopyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromUrlWithResponse#String-String-Duration-Context
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromUrlWithResponse(copyId, leaseId, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromUrlWithResponse#String-String-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#copyFromUrlWithResponse(String, Map, AccessTier, RequestConditions,
     * BlobRequestConditions, Duration, Context)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.copyFromUrlWithResponse#String-Metadata-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Copy identifier: %s%n",
            client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedRequestConditions,
                blobRequestConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.copyFromUrlWithResponse#String-Metadata-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#downloadWithResponse(OutputStream, BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context

    }

    /**
     * Code snippets for {@link BlobClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration,
     * Context)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#getPropertiesWithResponse(BlobRequestConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobRequestConditions-Duration-Context
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        BlobProperties properties = client.getPropertiesWithResponse(requestConditions, timeout,
            new Context(key2, value2)).getValue();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setHttpHeadersWithResponse(BlobHttpHeaders, BlobRequestConditions, Duration,
     * Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHttpHeadersWithResponse(new BlobHttpHeaders()
                .setContentLanguage("en-US")
                .setContentType("binary"), requestConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setMetadataWithResponse(Map, BlobRequestConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobRequestConditions-Duration-Context
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshotWithResponse(Map, BlobRequestConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobRequestConditions-Duration-Context
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, requestConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, String, Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context
        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, leaseId, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context
    }

    /**
     * Code snippet for {@link BlobClient#undeleteWithResponse(Duration, Context)}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.undeleteWithResponse#Duration-Context
        System.out.printf("Undelete completed with status %d%n", client.undeleteWithResponse(timeout,
            new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.undeleteWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfoWithResponse#Duration-Context
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, new Context(key1, value1)).getValue();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfoWithResponse#Duration-Context
    }

    /**
     * Generates a code sample for using {@link BlobClient#getContainerName()}
     */
    public void getContainerName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getContainerName
        String containerName = client.getContainerName();
        System.out.println("The name of the blob is " + containerName);
        // END: com.azure.storage.blob.specialized.BlobClientBase.getContainerName
    }

    /**
     * Generates a code sample for using {@link BlobClient#getBlobName()}
     */
    public void getBlobName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getBlobName
        String blobName = client.getBlobName();
        System.out.println("The name of the blob is " + blobName);
        // END: com.azure.storage.blob.specialized.BlobClientBase.getBlobName
    }

    /**
     * Code snippet for {@link BlobClient#uploadFromFile(String)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile() throws IOException {
        // BEGIN: com.azure.storage.blob.BlobClient.uploadFromFile#String
        try {
            client.uploadFromFile(filePath);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.BlobClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlobClient#uploadFromFile(String, boolean)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFileOverwrite() throws IOException {
        // BEGIN: com.azure.storage.blob.BlobClient.uploadFromFile#String-boolean
        try {
            boolean overwrite = false;
            client.uploadFromFile(filePath, overwrite);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.BlobClient.uploadFromFile#String-boolean
    }

    /**
     * Code snippet for {@link BlobClient#uploadFromFile(String, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions, Duration)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile2() throws IOException {
        // BEGIN: com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Integer blockSize = 100 * 1024 * 1024; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(blockSize, null, null);

        try {
            client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata,
                AccessTier.HOT, requestConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions-Duration
    }
}
