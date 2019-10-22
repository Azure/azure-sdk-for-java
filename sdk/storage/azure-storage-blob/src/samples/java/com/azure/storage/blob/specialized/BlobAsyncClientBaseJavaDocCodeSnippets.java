// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.AccessTier;
import com.azure.core.util.polling.Poller;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.BlobParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link BlobAsyncClientBase}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientBaseJavaDocCodeSnippets {
    private BlobAsyncClientBase client = new BlobAsyncClientBase(null, null, BlobServiceVersion.getLatest(),
        null, null, null, null, null);
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";

    /**
     * Code snippet for {@link BlobAsyncClientBase#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#beginCopy(String, Duration)}
     */
    public void beginCopyCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Duration
        client.beginCopy(url, Duration.ofSeconds(3)).getObserver()
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Duration
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#abortCopyFromUrl(String)}
     */
    public void abortCopyFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrl#String
        client.abortCopyFromUrl(copyId).doOnSuccess(response -> System.out.println("Aborted copy from URL"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#copyFromUrl(String)}
     */
    public void copyFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrl#String
        client.copyFromUrl(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrl#String
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#download()}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.download
        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        client.download().subscribe(piece -> {
            try {
                downloadData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.download
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadWithResponse(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.getValue().subscribe(piece -> {
                try {
                    downloadData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadToFile(String)} and {@link BlobAsyncClientBase#downloadToFileWithResponse(String,
     * BlobRange, BlobParallelTransferOptions, ReliableDownloadOptions, BlobRequestConditions, boolean)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-BlobParallelTransferOptions-ReliableDownloadOptions-BlobRequestConditions-boolean

        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-BlobParallelTransferOptions-ReliableDownloadOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#delete()}
     */
    public void deleteCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.delete
        client.delete().doOnSuccess(response -> System.out.println("Completed delete"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.delete
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#getProperties()}
     */
    public void getPropertiesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Type: %s, Size: %d%n", response.getBlobType(), response.getBlobSize()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getProperties
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setHttpHeaders(BlobHttpHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeaders#BlobHttpHeaders
        client.setHttpHeaders(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeaders#BlobHttpHeaders
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setMetadata(Map)}
     */
    public void setMetadataCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Map
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Map
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#createSnapshot()}
     */
    public void createSnapshotCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshot
        client.createSnapshot()
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n",
                response.getSnapshotId()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshot
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTier(AccessTier)}
     */
    public void setTierCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTier#AccessTier
        client.setAccessTier(AccessTier.HOT);
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTier#AccessTier
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#undelete()}
     */
    public void undeleteCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.undelete
        client.undelete().doOnSuccess(response -> System.out.println("Completed undelete"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.undelete
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#getAccountInfo()}
     */
    public void getAccountInfoCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfo
        client.getAccountInfo().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.getAccountKind(), response.getSkuName()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#existsWithResponse()}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.existsWithResponse
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#beginCopy(String, Map, AccessTier,
     * RehydratePriority, RequestConditions, BlobRequestConditions, Duration)}
     */
    public void beginCopyCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedAccessConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobAccessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.beginCopy(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
            modifiedAccessConditions, blobAccessConditions, Duration.ofSeconds(2))
            .getObserver()
            .subscribe(response -> {
                BlobCopyInfo info = response.getValue();
                System.out.printf("CopyId: %s. Status: %s%n", info.getCopyId(), info.getCopyStatus());
            });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#beginCopy(String, Map, AccessTier,
     * RehydratePriority, RequestConditions, BlobRequestConditions, Duration)}
     */
    public void beginCopyFromUrlCancelCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedAccessConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        Poller<BlobCopyInfo, Void> poller = client.beginCopy(url, metadata, AccessTier.HOT,
            RehydratePriority.STANDARD, modifiedAccessConditions, blobRequestConditions, Duration.ofSeconds(2));

        // Cancel a poll operation.
        poller.cancelOperation().block();
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#abortCopyFromUrlWithResponse(String, String)}
     */
    public void abortCopyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrlWithResponse#String-String
        client.abortCopyFromUrlWithResponse(copyId, leaseId)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromUrlWithResponse#String-String
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#copyFromURLWithResponse(String, Map, AccessTier,
     * RequestConditions, BlobRequestConditions)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedAccessConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions, blobRequestConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, (long) 2048);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadWithResponse(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.getValue().subscribe(piece -> {
                try {
                    downloadData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions
        client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#getPropertiesWithResponse(BlobRequestConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobRequestConditions
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(accessConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.getValue().getBlobType(),
                response.getValue().getBlobSize()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setHttpHeadersWithResponse(BlobHttpHeaders, BlobRequestConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setHttpHeadersWithResponse(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), accessConditions).subscribe(
                response ->
                    System.out.printf("Set HTTP headers completed with status %d%n",
                        response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setMetadataWithResponse(Map, BlobRequestConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Map-BlobRequestConditions
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Map-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#createSnapshotWithResponse(Map, BlobRequestConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Map-BlobRequestConditions
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions accessConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.createSnapshotWithResponse(snapshotMetadata, accessConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Map-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, String)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String
        client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, leaseId)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-String
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#undeleteWithResponse()}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.undeleteWithResponse
        client.undeleteWithResponse()
            .subscribe(response -> System.out.printf("Undelete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.undeleteWithResponse
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#getAccountInfoWithResponse()}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.getValue().getAccountKind(), response.getValue().getSkuName()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getAccountInfoWithResponse
    }
}
