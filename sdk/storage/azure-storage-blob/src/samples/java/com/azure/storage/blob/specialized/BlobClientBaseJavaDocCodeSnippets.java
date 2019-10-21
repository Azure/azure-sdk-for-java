// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.Poller;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.common.implementation.Constants;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link BlobClientBase}
 */
@SuppressWarnings("unused")
public class BlobClientBaseJavaDocCodeSnippets {
    private BlobClientBase client = new BlobClientBase(null);
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Code snippets for {@link BlobClientBase#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.specialized.BlobClientBase.exists
    }

    /**
     * Code snippets for {@link BlobClientBase#beginCopy(String, Duration)}
     */
    public void beginCopy() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Duration
        final Poller<BlobCopyInfo, Void> poller = client.beginCopy(url, Duration.ofSeconds(2));
        poller.getObserver().subscribe(response -> {
            System.out.printf("Copy identifier: %s%n", response.getValue().getCopyId());
        });
        // END: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Duration
    }

    /**
     * Code snippets for {@link BlobClientBase#abortCopyFromUrl(String)}
     */
    public void abortCopyFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrl#String
        client.abortCopyFromUrl(copyId);
        System.out.println("Aborted copy completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobClientBase#copyFromUrl(String)}
     */
    public void copyFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrl#String
        System.out.printf("Copy identifier: %s%n", client.copyFromUrl(url));
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobClientBase#download(OutputStream)}
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.download#OutputStream
        client.download(new ByteArrayOutputStream());
        System.out.println("Download completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.download#OutputStream
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadToFile(String)} and
     * {@link BlobClientBase#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobRequestConditions,
     * boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions().setBlockSize(4 * Constants.MB),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobRequestConditions-boolean-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.delete
        client.delete();
        System.out.println("Delete completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.delete
    }

    /**
     * Code snippets for {@link BlobClientBase#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getProperties
        BlobProperties properties = client.getProperties();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getProperties
    }

    /**
     * Code snippets for {@link BlobClientBase#setHttpHeaders(BlobHttpHeaders)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setHttpHeaders#BlobHttpHeaders
        client.setHttpHeaders(new BlobHttpHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
        System.out.println("Set HTTP headers completed");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setHttpHeaders#BlobHttpHeaders
    }

    /**
     * Code snippets for {@link BlobClientBase#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setMetadata#Map
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setMetadata#Map
    }

    /**
     * Code snippets for {@link BlobClientBase#createSnapshot()}
     */
    public void createSnapshot() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.createSnapshot
        System.out.printf("Identifier for the snapshot is %s%n", client.createSnapshot().getSnapshotId());
        // END: com.azure.storage.blob.specialized.BlobClientBase.createSnapshot
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTier(AccessTier)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier
        client.setAccessTier(AccessTier.HOT);
        System.out.println("Set tier completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier


    }

    /**
     * Code snippets for {@link BlobClientBase#undelete()}
     */
    public void unsetDelete() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.undelete
        client.undelete();
        System.out.println("Undelete completed");
        // END: com.azure.storage.blob.specialized.BlobClientBase.undelete
    }

    /**
     * Code snippet for {@link BlobClientBase#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobClientBase#existsWithResponse(Duration, Context)}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.existsWithResponse#Duration-Context
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, new Context(key2, value2)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.existsWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#beginCopy(String, Map, AccessTier, RehydratePriority,
     * RequestConditions, BlobRequestConditions, Duration)}
     */
    public void beginCopyFromUrl() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedAccessConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobAccessConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Poller<BlobCopyInfo, Void> poller = client.beginCopy(url, metadata, AccessTier.HOT,
            RehydratePriority.STANDARD, modifiedAccessConditions, blobAccessConditions, Duration.ofSeconds(2));

        PollResponse<BlobCopyInfo> response = poller.blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);
        System.out.printf("Copy identifier: %s%n", response.getValue().getCopyId());
        // END: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClientBase#abortCopyFromUrlWithResponse(String, String, Duration, Context)}
     */
    public void abortCopyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrlWithResponse#String-String-Duration-Context
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromUrlWithResponse(copyId, leaseId, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromUrlWithResponse#String-String-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#copyFromURLWithResponse(String, Map, AccessTier, RequestConditions,
     * BlobRequestConditions, Duration, Context)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedAccessConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Copy identifier: %s%n",
            client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions,
                blobRequestConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadWithResponse(OutputStream, BlobRange, ReliableDownloadOptions,
     * BlobRequestConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context

    }

    /**
     * Code snippets for {@link BlobClientBase#deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions, Duration,
     * Context)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#getPropertiesWithResponse(BlobRequestConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobRequestConditions-Duration-Context
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        BlobProperties properties = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2)).getValue();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setHttpHeadersWithResponse(BlobHttpHeaders, BlobRequestConditions, Duration,
     * Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHttpHeadersWithResponse(new BlobHttpHeaders()
                .setBlobContentLanguage("en-US")
                .setBlobContentType("binary"), accessConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setMetadataWithResponse(Map, BlobRequestConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#createSnapshotWithResponse(Map, BlobRequestConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, accessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, String, Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context
        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, leaseId, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String-Duration-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#undeleteWithResponse(Duration, Context)}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.undeleteWithResponse#Duration-Context
        System.out.printf("Undelete completed with status %d%n", client.undeleteWithResponse(timeout,
            new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.undeleteWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getAccountInfoWithResponse#Duration-Context
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, new Context(key1, value1)).getValue();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getAccountInfoWithResponse#Duration-Context
    }
}
