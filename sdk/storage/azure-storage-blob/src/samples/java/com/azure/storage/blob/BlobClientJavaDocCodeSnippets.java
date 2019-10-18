// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.Poller;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.StorageAccountInfo;
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

    /**
     * Code snippets for {@link BlobClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.BlobClient.exists
    }

    /**
     * Code snippets for {@link BlobClient#beginCopy(String, Duration)}
     */
    public void beginCopyFromUrl() {
        // BEGIN: com.azure.storage.blob.BlobClient.beginCopy#String
        Poller<BlobCopyInfo, Void> poller = client.beginCopy(url, Duration.ofSeconds(2));

        // This blocks until either the copy operation has completed, failed, or been cancelled.
        poller.block();
        PollResponse<BlobCopyInfo> response = poller.getLastPollResponse();
        BlobCopyInfo operation = response.getValue();
        System.out.printf("Status: %s, Copy identifier: %s%n", poller.getStatus(), operation.getCopyId());
        // END: com.azure.storage.blob.BlobClient.beginCopy#String
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromURL(String)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
        client.abortCopyFromURL(copyId);
        System.out.println("Aborted copy completed.");
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURL(String)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#String
        System.out.printf("Copy identifier: %s%n", client.copyFromURL(url));
        // END: com.azure.storage.blob.BlobClient.copyFromURL#String
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
     * {@link BlobClient#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions().setBlockSize(4 * Constants.MB),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
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
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
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
     * Code snippets for {@link BlobClientBase#setAccessTier(AccessTier)} and
     * {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions, Duration, Context)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier
        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode());
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
     * Code snippets for {@link BlobClient#abortCopyFromURLWithResponse(String, LeaseAccessConditions, Duration, Context)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURLWithResponse(String, Map, AccessTier, ModifiedAccessConditions,
     * BlobAccessConditions, Duration, Context)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURLWithResponse#String-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.copyFromURLWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions,
                blobAccessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.copyFromURLWithResponse#String-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#downloadWithResponse(OutputStream, BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context

    }

    /**
     * Code snippets for {@link BlobClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions, Duration,
     * Context)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#getPropertiesWithResponse(BlobAccessConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        BlobProperties properties = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2)).getValue();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setHttpHeadersWithResponse(BlobHttpHeaders, BlobAccessConditions, Duration,
     * Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHttpHeaders-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHttpHeadersWithResponse(new BlobHttpHeaders()
                .setBlobContentLanguage("en-US")
                .setBlobContentType("binary"), accessConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHttpHeaders-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setMetadataWithResponse(Map, BlobAccessConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshotWithResponse(Map, BlobAccessConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobAccessConditions-Duration-Context
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, accessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions, Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, accessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
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
     * Code snippet for {@link BlobClient#uploadFromFile(String, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobAccessConditions, Duration)}
     *
     * @throws IOException If an I/O error occurs
     */
    public void uploadFromFile2() throws IOException {
        // BEGIN: com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions-Duration
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Integer blockSize = 100 * 1024 * 1024; // 100 MB;
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSize(blockSize);

        try {
            client.uploadFromFile(filePath, parallelTransferOptions, headers, metadata,
                AccessTier.HOT, accessConditions, timeout);
            System.out.println("Upload from file succeeded");
        } catch (UncheckedIOException ex) {
            System.err.printf("Failed to upload from file %s%n", ex.getMessage());
        }
        // END: com.azure.storage.blob.BlobClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobAccessConditions-Duration
    }
}
