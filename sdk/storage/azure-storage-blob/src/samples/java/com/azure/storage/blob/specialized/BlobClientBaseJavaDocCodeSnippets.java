// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.RequestConditions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobBeginCopySourceRequestConditions;
import com.azure.storage.blob.models.BlobDownloadContentResponse;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode;
import com.azure.storage.blob.options.BlobBeginCopyOptions;
import com.azure.storage.blob.options.BlobCopyFromUrlOptions;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobQueryDelimitedSerialization;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryJsonSerialization;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobGetTagsOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobQuerySerialization;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.BlobSetTagsOptions;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
 * Code snippets for {@link BlobClientBase}
 */
@SuppressWarnings("unused")
public class BlobClientBaseJavaDocCodeSnippets {
    private BlobClientBase client = new BlobClientBase(null);
    private String leaseId = "leaseId";
    private String tags = "tags";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = new BlobServiceClientBuilder().buildClient().getUserDelegationKey(null, null);

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
        final SyncPoller<BlobCopyInfo, Void> poller = client.beginCopy(url, Duration.ofSeconds(2));
        PollResponse<BlobCopyInfo> pollResponse = poller.poll();
        System.out.printf("Copy identifier: %s%n", pollResponse.getValue().getCopyId());
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
     * Code snippets for {@link BlobClientBase#downloadStream(OutputStream)}
     */
    public void downloadStream() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream
        client.downloadStream(new ByteArrayOutputStream());
        System.out.println("Download completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadStream#OutputStream
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadToFile(String)},
     * {@link BlobClientBase#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, DownloadRetryOptions, BlobRequestConditions,
     * boolean, Duration, Context)} and {@link BlobClientBase#downloadToFileWithResponse(BlobDownloadToFileOptions, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean
        boolean overwrite = false; // Default value
        client.downloadToFile(file, overwrite);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String-boolean

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions().setBlockSizeLong(4L * Constants.MB),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context
        BlobRange blobRange = new BlobRange(1024, 2048L);
        DownloadRetryOptions downloadRetryOptions = new DownloadRetryOptions().setMaxRetryRequests(5);
        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE, StandardOpenOption.READ)); // Default options

        client.downloadToFileWithResponse(file, blobRange, new ParallelTransferOptions().setBlockSizeLong(4L * Constants.MB),
            downloadRetryOptions, null, false, openOptions, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean-Set-Duration-Context

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context
        client.downloadToFileWithResponse(new BlobDownloadToFileOptions(file)
            .setRange(new BlobRange(1024, 2018L))
            .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(5))
            .setOpenOptions(new HashSet<>(Arrays.asList(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                StandardOpenOption.READ))), timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#BlobDownloadToFileOptions-Duration-Context
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
            .setContentLanguage("en-US")
            .setContentType("binary"));
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
     * Code snippets for {@link BlobClientBase#getTags()}
     */
    public void getTags() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getTags
        Map<String, String> tags = client.getTags();
        System.out.printf("Number of tags: %d%n", tags.size());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getTags
    }

    /**
     * Code snippets for {@link BlobClientBase#setTags(Map)}
     */
    public void setTags() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setTags#Map
        client.setTags(Collections.singletonMap("tag", "value"));
        System.out.println("Set tag completed");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setTags#Map
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
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        SyncPoller<BlobCopyInfo, Void> poller = client.beginCopy(url, metadata, AccessTier.HOT,
            RehydratePriority.STANDARD, modifiedRequestConditions, blobRequestConditions, Duration.ofSeconds(2));

        PollResponse<BlobCopyInfo> response = poller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        System.out.printf("Copy identifier: %s%n", response.getValue().getCopyId());
        // END: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#String-Map-AccessTier-RehydratePriority-RequestConditions-BlobRequestConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClientBase#beginCopy(BlobBeginCopyOptions)}
     */
    public void beginCopyFromUrl2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#BlobBeginCopyOptions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobBeginCopySourceRequestConditions modifiedRequestConditions = new BlobBeginCopySourceRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);
        SyncPoller<BlobCopyInfo, Void> poller = client.beginCopy(new BlobBeginCopyOptions(url).setMetadata(metadata)
            .setTags(tags).setTier(AccessTier.HOT).setRehydratePriority(RehydratePriority.STANDARD)
            .setSourceRequestConditions(modifiedRequestConditions)
            .setDestinationRequestConditions(blobRequestConditions).setPollInterval(Duration.ofSeconds(2)));

        PollResponse<BlobCopyInfo> response = poller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        System.out.printf("Copy identifier: %s%n", response.getValue().getCopyId());
        // END: com.azure.storage.blob.specialized.BlobClientBase.beginCopy#BlobBeginCopyOptions
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
     * Code snippets for {@link BlobClientBase#copyFromUrlWithResponse(String, Map, AccessTier, RequestConditions,
     * BlobRequestConditions, Duration, Context)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Copy identifier: %s%n",
            client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedRequestConditions,
                blobRequestConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#String-Map-AccessTier-RequestConditions-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#copyFromUrlWithResponse(BlobCopyFromUrlOptions, Duration, Context)}
     */
    public void copyFromUrlWithResponse2CodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Copy identifier: %s%n",
            client.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(url).setMetadata(metadata).setTags(tags)
                .setTier(AccessTier.HOT).setSourceRequestConditions(modifiedRequestConditions)
                .setDestinationRequestConditions(blobRequestConditions), timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromUrlWithResponse#BlobCopyFromUrlOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadWithResponse(OutputStream, BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context

    }

    /**
     * Code snippets for {@link BlobClientBase#downloadStreamWithResponse(OutputStream, BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadStreamWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadStreamWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadStreamWithResponse#OutputStream-BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadContentWithResponse(DownloadRetryOptions,
     * BlobRequestConditions, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadContentWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        BlobDownloadContentResponse contentResponse = client.downloadContentWithResponse(options, null,
            timeout, new Context(key2, value2));
        BinaryData content = contentResponse.getValue();
        System.out.printf("Download completed with status %d and content%s%n",
            contentResponse.getStatusCode(), content.toString());
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadContentWithResponse#DownloadRetryOptions-BlobRequestConditions-Duration-Context
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
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        BlobProperties properties = client.getPropertiesWithResponse(requestConditions, timeout,
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
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHttpHeadersWithResponse(new BlobHttpHeaders()
                .setContentLanguage("en-US")
                .setContentType("binary"), requestConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setMetadataWithResponse(Map, BlobRequestConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#getTagsWithResponse(BlobGetTagsOptions, Duration, Context)}
     */
    public void getTagsWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getTagsWithResponse#BlobGetTagsOptions-Duration-Context
        Map<String, String> tags = client.getTagsWithResponse(new BlobGetTagsOptions(), timeout,
            new Context(key1, value1)).getValue();
        System.out.printf("Number of tags: %d%n", tags.size());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getTagsWithResponse#BlobGetTagsOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setTagsWithResponse(BlobSetTagsOptions, Duration, Context)}
     */
    public void setTagsWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setTagsWithResponse#BlobSetTagsOptions-Duration-Context
        System.out.printf("Set metadata completed with status %d%n",
            client.setTagsWithResponse(new BlobSetTagsOptions(Collections.singletonMap("tag", "value")), timeout,
                new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setTagsWithResponse#BlobSetTagsOptions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#createSnapshotWithResponse(Map, BlobRequestConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobRequestConditions-Duration-Context
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, requestConditions, timeout,
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
     * Code snippets for {@link BlobClientBase#setAccessTierWithResponse(BlobSetAccessTierOptions, Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions-Duration-Context
        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
                .setPriority(RehydratePriority.STANDARD)
                .setLeaseId(leaseId)
                .setTagsConditions(tags),
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#BlobSetAccessTierOptions-Duration-Context
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

    /**
     * Code snippet for {@link BlobClientBase#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey)}
     * and {@link BlobClientBase#generateSas(BlobServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission myPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link BlobClientBase#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link BlobClientBase#generateSas(BlobServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context(key1, value1));
        // END: com.azure.storage.blob.specialized.BlobClientBase.generateSas#BlobServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobSasPermission myPermission = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context(key1, value1));
        // END: com.azure.storage.blob.specialized.BlobClientBase.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#openQueryInputStream(String)}
     */
    public void openQueryInputStream() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#String
        String expression = "SELECT * from BlobStorage";
        InputStream inputStream = client.openQueryInputStream(expression);
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#String
    }

    /**
     * Code snippet for {@link BlobClientBase#openQueryInputStreamWithResponse(BlobQueryOptions)}
     */
    public void openQueryInputStream2() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#BlobQueryOptions
        String expression = "SELECT * from BlobStorage";
        BlobQuerySerialization input = new BlobQueryDelimitedSerialization()
            .setColumnSeparator(',')
            .setEscapeChar('\n')
            .setRecordSeparator('\n')
            .setHeadersPresent(true)
            .setFieldQuote('"');
        BlobQuerySerialization output = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n');
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId("leaseId");
        Consumer<BlobQueryError> errorConsumer = System.out::println;
        Consumer<BlobQueryProgress> progressConsumer = progress -> System.out.println("total blob bytes read: "
            + progress.getBytesScanned());
        BlobQueryOptions queryOptions = new BlobQueryOptions(expression)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);

        InputStream inputStream = client.openQueryInputStreamWithResponse(queryOptions).getValue();
        // Now you can read from the input stream like you would normally.
        // END: com.azure.storage.blob.specialized.BlobClientBase.openQueryInputStream#BlobQueryOptions
    }

    /**
     * Code snippet for {@link BlobClientBase#query(OutputStream, String)}
     */
    public void query() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.query#OutputStream-String
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
        String expression = "SELECT * from BlobStorage";
        client.query(queryData, expression);
        System.out.println("Query completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.query#OutputStream-String
    }

    /**
     * Code snippet for {@link BlobClientBase#queryWithResponse(BlobQueryOptions, Duration, Context)}
     */
    public void queryWithResponse() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.queryWithResponse#BlobQueryOptions-Duration-Context
        ByteArrayOutputStream queryData = new ByteArrayOutputStream();
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
        BlobQueryOptions queryOptions = new BlobQueryOptions(expression, queryData)
            .setInputSerialization(input)
            .setOutputSerialization(output)
            .setRequestConditions(requestConditions)
            .setErrorConsumer(errorConsumer)
            .setProgressConsumer(progressConsumer);
        System.out.printf("Query completed with status %d%n",
            client.queryWithResponse(queryOptions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.queryWithResponse#BlobQueryOptions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#setImmutabilityPolicy(BlobImmutabilityPolicy)} and
     * {@link BlobClientBase#setImmutabilityPolicyWithResponse(BlobImmutabilityPolicy, BlobRequestConditions, Duration, Context)}
     */
    public void setImmutabilityPolicy() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy
        BlobImmutabilityPolicy policy = new BlobImmutabilityPolicy()
            .setPolicyMode(BlobImmutabilityPolicyMode.LOCKED)
            .setExpiryTime(OffsetDateTime.now().plusDays(1));
        BlobImmutabilityPolicy setPolicy = client.setImmutabilityPolicy(policy);
        System.out.println("Successfully completed setting the immutability policy");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicy#BlobImmutabilityPolicy

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions-Duration-Context
        BlobImmutabilityPolicy immutabilityPolicy = new BlobImmutabilityPolicy()
            .setPolicyMode(BlobImmutabilityPolicyMode.LOCKED)
            .setExpiryTime(OffsetDateTime.now().plusDays(1));
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(1));
        Response<BlobImmutabilityPolicy> response = client.setImmutabilityPolicyWithResponse(immutabilityPolicy,
            requestConditions, timeout, new Context(key1, value1));
        System.out.println("Successfully completed setting the immutability policy");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setImmutabilityPolicyWithResponse#BlobImmutabilityPolicy-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#deleteImmutabilityPolicy()} and
     * {@link BlobClientBase#deleteImmutabilityPolicyWithResponse(Duration, Context)}
     */
    public void deleteImmutabilityPolicy() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicy
        client.deleteImmutabilityPolicy();
        System.out.println("Completed immutability policy deletion.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicy

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicyWithResponse#Duration-Context
        System.out.println("Delete immutability policy completed with status: "
            + client.deleteImmutabilityPolicyWithResponse(timeout, new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.deleteImmutabilityPolicyWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobClientBase#setLegalHold(boolean)} and
     * {@link BlobClientBase#setLegalHoldWithResponse(boolean, Duration, Context)}
     */
    public void setLegalHold() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setLegalHold#boolean
        System.out.println("Legal hold status: " + client.setLegalHold(true));
        // END: com.azure.storage.blob.specialized.BlobClientBase.setLegalHold#boolean

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setLegalHoldWithResponse#boolean-Duration-Context
        System.out.println("Legal hold status: " + client.setLegalHoldWithResponse(true, timeout,
            new Context(key1, value1)));
        // END: com.azure.storage.blob.specialized.BlobClientBase.setLegalHoldWithResponse#boolean-Duration-Context
    }
}
