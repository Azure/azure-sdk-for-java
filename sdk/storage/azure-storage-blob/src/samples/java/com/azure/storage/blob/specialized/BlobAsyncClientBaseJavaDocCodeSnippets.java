// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobQueryDelimitedSerialization;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryJsonSerialization;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Code snippets for {@link BlobAsyncClientBase}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientBaseJavaDocCodeSnippets {
    private BlobAsyncClientBase client = new BlobAsyncClientBase(null, null, BlobServiceVersion.getLatest(),
        null, null, null, null, null, null, null);
    private String leaseId = "leaseId";
    private String tags = "tags";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = new BlobServiceClientBuilder().buildClient().getUserDelegationKey(null, null);

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
        client.beginCopy(url, Duration.ofSeconds(3))
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
     * Code snippets for {@link BlobAsyncClientBase#downloadStream()}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadStreamCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStream
        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        client.downloadStream().subscribe(piece -> {
            try {
                downloadData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStream
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

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
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadToFile(String)}, {@link BlobAsyncClientBase#downloadToFileWithResponse(String,
     * BlobRange, ParallelTransferOptions, DownloadRetryOptions, BlobRequestConditions, boolean)} and
     * {@link BlobAsyncClientBase#downloadToFileWithResponse(BlobDownloadToFileOptions)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String-boolean
        boolean overwrite = false; // Default value
        client.downloadToFile(file, overwrite).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String-boolean

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set
        BlobRange blobRange = new BlobRange(1024, 2048L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);
        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE, StandardOpenOption.READ)); // Default options

        client.downloadToFileWithResponse(file, blobRange, null, downloadRetryOptions, null, false, openOptions)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions
        client.downloadToFileWithResponse(new BlobDownloadToFileOptions(file)
            .setRange(new BlobRange(1024, 2018L))
            .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(5))
            .setOpenOptions(new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                StandardOpenOption.READ))))
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions
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
     * Code snippets for {@link BlobAsyncClientBase#getTags()}
     */
    public void getTagsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getTags
        client.getTags().subscribe(response ->
            System.out.printf("Num tags: %d%n", response.size()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getTags
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setTags(Map)}
     */
    public void setTagsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setTags#Map
        client.setTags(Collections.singletonMap("tag", "value"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setTags#Map
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
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.beginCopy(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
            modifiedRequestConditions, blobRequestConditions, Duration.ofSeconds(2))
            .subscribe(response -> {
                BlobCopyInfo info = response.getValue();
                System.out.printf("CopyId: %s. Status: %s%n", info.getCopyId(), info.getCopyStatus());
            });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#beginCopy(BlobBeginCopyOptions)}
     */
    public void beginCopyCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#BlobBeginCopyOptions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobBeginCopySourceRequestConditions modifiedRequestConditions = new BlobBeginCopySourceRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.beginCopy(new BlobBeginCopyOptions(url).setMetadata(metadata).setTags(tags).setTier(AccessTier.HOT)
            .setRehydratePriority(RehydratePriority.STANDARD).setSourceRequestConditions(modifiedRequestConditions)
            .setDestinationRequestConditions(blobRequestConditions).setPollInterval(Duration.ofSeconds(2)))
            .subscribe(response -> {
                BlobCopyInfo info = response.getValue();
                System.out.printf("CopyId: %s. Status: %s%n", info.getCopyId(), info.getCopyStatus());
            });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopy#BlobBeginCopyOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#beginCopy(BlobBeginCopyOptions)}
     */
    public void beginCopyFromUrlCancelCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#BlobBeginCopyOptions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobBeginCopySourceRequestConditions modifiedRequestConditions = new BlobBeginCopySourceRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        PollerFlux<BlobCopyInfo, Void> poller = client.beginCopy(new BlobBeginCopyOptions(url)
            .setMetadata(metadata).setTags(tags).setTier(AccessTier.HOT)
            .setRehydratePriority(RehydratePriority.STANDARD).setSourceRequestConditions(modifiedRequestConditions)
            .setDestinationRequestConditions(blobRequestConditions).setPollInterval(Duration.ofSeconds(2)));

        poller.take(Duration.ofMinutes(30))
                .last()
                .flatMap(asyncPollResponse -> {
                    if (!asyncPollResponse.getStatus().isComplete()) {
                        return asyncPollResponse
                                .cancelOperation()
                                .then(Mono.error(new RuntimeException("Blob copy taking long time, "
                                        + "operation is cancelled!")));
                    }
                    return Mono.just(asyncPollResponse);
                }).block();
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.beginCopyFromUrlCancel#BlobBeginCopyOptions
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
     * Code snippets for {@link BlobAsyncClientBase#copyFromUrlWithResponse(String, Map, AccessTier, RequestConditions,
     * BlobRequestConditions)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedRequestConditions, blobRequestConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#copyFromUrlWithResponse(BlobCopyFromUrlOptions)}
     */
    public void copyFromUrlWithResponseCodeSnippets2() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(url).setMetadata(metadata).setTags(tags)
            .setTier(AccessTier.HOT).setSourceRequestConditions(modifiedRequestConditions)
            .setDestinationRequestConditions(blobRequestConditions))
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
        BlobRange range = new BlobRange(1024, (long) 2048);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

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
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadStreamWithResponse(BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadStreamWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStreamWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
        BlobRange range = new BlobRange(1024, (long) 2048);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadStreamWithResponse(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.getValue().subscribe(piece -> {
                try {
                    downloadData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadStreamWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadContentWithResponse(DownloadRetryOptions,
     * BlobRequestConditions)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadContentWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadContentWithResponse(options, null).subscribe(response -> {
            BinaryData content = response.getValue();
            System.out.println(content.toString());
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions
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
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(requestConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.getValue().getBlobType(),
                response.getValue().getBlobSize()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setHttpHeadersWithResponse(BlobHttpHeaders, BlobRequestConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setHttpHeadersWithResponse(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), requestConditions).subscribe(
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
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Map-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#getTagsWithResponse(BlobGetTagsOptions)}
     */
    public void getTagsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getTagsWithResponse#BlobGetTagsOptions
        client.getTagsWithResponse(new BlobGetTagsOptions()).subscribe(response ->
            System.out.printf("Status code: %d. Num tags: %d%n", response.getStatusCode(), response.getValue().size()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getTagsWithResponse#BlobGetTagsOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setTagsWithResponse(BlobSetTagsOptions)}
     */
    public void setTagsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setTagsWithResponse#BlobSetTagsOptions
        client.setTagsWithResponse(new BlobSetTagsOptions(Collections.singletonMap("tag", "value")))
            .subscribe(response -> System.out.printf("Set tags completed with stats %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setTagsWithResponse#BlobSetTagsOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#createSnapshotWithResponse(Map, BlobRequestConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Map-BlobRequestConditions
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.createSnapshotWithResponse(snapshotMetadata, requestConditions)
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
     * Code snippets for {@link BlobAsyncClientBase#setAccessTierWithResponse(BlobSetAccessTierOptions)}
     */
    public void setTierWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions
        client.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
            .setPriority(RehydratePriority.STANDARD)
            .setLeaseId(leaseId)
            .setTagsConditions(tags))
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions
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

    /**
     * Code snippet for {@link BlobAsyncClientBase#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey)}
     * and {@link BlobAsyncClientBase#generateSas(BlobServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission myPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link BlobAsyncClientBase#generateSas(BlobServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSas#BlobServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission myPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context("key", "value"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#query(String)}
     * @throws UncheckedIOException for IOExceptions.
     */
    public void query() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.query#String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT * from BlobStorage";
        client.query(expression).subscribe(piece -> {
            try {
                queryData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.query#String
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#queryWithResponse(BlobQueryOptions)}
     * @throws UncheckedIOException for IOExceptions.
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.queryWithResponse#BlobQueryOptions
        String expression = "SELECT * from BlobStorage";
        BlobQueryJsonSerialization input = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n');
        BlobQueryDelimitedSerialization output = new BlobQueryDelimitedSerialization()
            .setEscapeChar('\0')
            .setColumnSeparator(',')
            .setRecordSeparator('\n')
            .setFieldQuote('\'')
            .setHeadersPresent(true);
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        Consumer<BlobQueryError> errorConsumer = System.out::println;
        Consumer<BlobQueryProgress> progressConsumer = progress -> System.out.println("total blob bytes read: "
            + progress.getBytesScanned());
        BlobQueryOptions queryOptions = new BlobQueryOptions(expression)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);

        client.queryWithResponse(queryOptions)
            .subscribe(response -> {
                ByteArrayOutputStream queryData = new ByteArrayOutputStream();
                response.getValue().subscribe(piece -> {
                    try {
                        queryData.write(piece.array());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            });
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.queryWithResponse#BlobQueryOptions
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#setImmutabilityPolicy(BlobImmutabilityPolicy)} and
     * {@link BlobAsyncClientBase#setImmutabilityPolicyWithResponse(BlobImmutabilityPolicy, BlobRequestConditions)}
     */
    public void setImmutabilityPolicy() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy
        BlobImmutabilityPolicy policy = new BlobImmutabilityPolicy()
            .setPolicyMode(BlobImmutabilityPolicyMode.LOCKED)
            .setExpiryTime(OffsetDateTime.now().plusDays(1));
        client.setImmutabilityPolicy(policy).subscribe(response -> System.out.println("Completed. Set immutability "
            + "policy to " + response.getPolicyMode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setPolicyMode(BlobImmutabilityPolicyMode.LOCKED)
            .setExpiryTime(OffsetDateTime.now().plusDays(1));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1));
        client.setImmutabilityPolicyWithResponse(immutabilityPolicy, requestConditions).subscribe(response ->
            System.out.println("Completed. Set immutability policy to " + response.getValue().getPolicyMode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#deleteImmutabilityPolicy()} and
     * {@link BlobAsyncClientBase#deleteImmutabilityPolicyWithResponse()}
     */
    public void deleteImmutabilityPolicy() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicy
        client.deleteImmutabilityPolicy().subscribe(response -> System.out.println("Completed immutability policy"
            + " deletion."));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicy

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicyWithResponse
        client.deleteImmutabilityPolicyWithResponse().subscribe(response ->
            System.out.println("Delete immutability policy completed with status: " + response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteImmutabilityPolicyWithResponse
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#setLegalHold(boolean)} and
     * {@link BlobAsyncClientBase#setLegalHoldWithResponse(boolean)}
     */
    public void setLegalHold() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHold#boolean
        client.setLegalHold(true).subscribe(response -> System.out.println("Legal hold status: "
            + response.hasLegalHold()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHold#boolean

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHoldWithResponse#boolean
        client.setLegalHoldWithResponse(true).subscribe(response ->
            System.out.println("Legal hold status: " + response.getValue().hasLegalHold()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setLegalHoldWithResponse#boolean
    }
}
