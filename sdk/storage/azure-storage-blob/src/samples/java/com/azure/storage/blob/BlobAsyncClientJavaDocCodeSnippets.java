// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * Code snippets for {@link BlobAsyncClient}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientJavaDocCodeSnippets {

    private BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blobName");
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private URL url = JavaDocCodeSnippetsHelpers.generateURL("https://sample.com");
    private String file = "file";
    private int blockSize = 50;
    private int numBuffers = 2;
    private String filePath = "filePath";

    /**
     * Code snippet for {@link BlobAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClient#startCopyFromURL(URL)}
     */
    public void startCopyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL
        client.startCopyFromURL(url)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromURL(String)}
     */
    public void abortCopyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String
        client.abortCopyFromURL(copyId).doOnSuccess(response -> System.out.println("Aborted copy from URL"));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromURL(URL)}
     */
    public void copyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL
        client.copyFromURL(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobAsyncClient#download()}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download
        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        client.download().subscribe(piece -> {
            try {
                downloadData.write(piece.array());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
        // END: com.azure.storage.blob.BlobAsyncClient.download
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
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
        // END: com.azure.storage.blob.BlobAsyncClient.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadToFile(String)} and {@link BlobAsyncClient#downloadToFileWithResponse(
     * String, BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobAccessConditions, boolean)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#delete()}
     */
    public void deleteCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.delete
        client.delete().doOnSuccess(response -> System.out.println("Completed delete"));
        // END: com.azure.storage.blob.BlobAsyncClient.delete
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getProperties()}
     */
    public void getPropertiesCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Type: %s, Size: %d%n", response.getBlobType(), response.getBlobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getProperties
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadata(Map)}
     */
    public void setMetadataCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata
        client.setMetadata(Collections.singletonMap("metadata", "value"));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshot()}
     */
    public void createSnapshotCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshot
        client.createSnapshot()
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n",
                response.getSnapshotId()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshot
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTier(AccessTier)}
     */
    public void setTierCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier
        client.setAccessTier(AccessTier.HOT);
        // END: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier
    }

    /**
     * Code snippet for {@link BlobAsyncClient#undelete()}
     */
    public void undeleteCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.undelete
        client.undelete().doOnSuccess(response -> System.out.println("Completed undelete"));
        // END: com.azure.storage.blob.BlobAsyncClient.undelete
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfo()}
     */
    public void getAccountInfoCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.getAccountKind(), response.getSkuName()));
        // END: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobAsyncClient#existsWithResponse()}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
    }

    /**
     * Code snippets for {@link BlobAsyncClient#startCopyFromURLWithResponse(URL, Map, AccessTier,
     * RehydratePriority, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void startCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.startCopyFromURLWithResponse(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
            modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.getValue()));
        // END: com.azure.storage.blob.BlobAsyncClient.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromURLWithResponse(String, LeaseAccessConditions)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions)
            .subscribe(
                response -> System.out.printf("Aborted copy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromURLWithResponse(URL, Map, AccessTier,
     * ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.copyFromURLWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
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
        // END: com.azure.storage.blob.BlobAsyncClient.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions
        client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getPropertiesWithResponse(BlobAccessConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.getPropertiesWithResponse(accessConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.getValue().getBlobType(),
                response.getValue().getBlobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeadersWithResponse(BlobHTTPHeaders, BlobAccessConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.setHTTPHeadersWithResponse(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"), accessConditions).subscribe(
                response ->
                    System.out.printf("Set HTTP headers completed with status %d%n",
                        response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadataWithResponse(Map, BlobAccessConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions)
            .subscribe(
                response -> System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshotWithResponse(Map, BlobAccessConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobAccessConditions
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.createSnapshotWithResponse(snapshotMetadata, accessConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n", response.getValue()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, accessConditions)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#undeleteWithResponse()}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.undeleteWithResponse
        client.undeleteWithResponse()
            .subscribe(response -> System.out.printf("Undelete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.undeleteWithResponse
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfoWithResponse()}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.getValue().getAccountKind(), response.getValue().getSkuName()));
        // END: com.azure.storage.blob.BlobAsyncClient.getAccountInfoWithResponse
    }

    /**
     * Generates a code sample for using {@link BlobAsyncClient#getContainerName()}
     */
    public void getContainerName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerName
        String containerName = client.getContainerName();
        System.out.println("The name of the container is " + containerName);
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerName
    }

    /**
     * Generates a code sample for using {@link BlobAsyncClient#getBlobName()}
     */
    public void getBlobName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getBlobName
        String blobName = client.getBlobName();
        System.out.println("The name of the blob is " + blobName);
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getBlobName
    }

    /**
     * Code snippet for {@link BlobAsyncClient#upload(Flux, ParallelTransferOptions)}
     */
    public void upload3() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize)
            .setNumBuffers(numBuffers);
        client.upload(data, parallelTransferOptions).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMD5())));
        // END: com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions)}
     */
    public void upload4() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize)
            .setNumBuffers(numBuffers);

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMD5())));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions)}
     */
    public void upload5() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions.ProgressReporter
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSize(blockSize)
            .setNumBuffers(numBuffers)
            .setProgressReceiver(bytesTransferred -> System.out.printf(
                "Upload progress: %s bytes sent", bytesTransferred));

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMD5())));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions.ProgressReporter
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadFromFile(String)}
     */
    public void uploadFromFile() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String
        client.uploadFromFile(filePath)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadFromFile(String, ParallelTransferOptions, BlobHTTPHeaders, Map, AccessTier, BlobAccessConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions
        BlobHTTPHeaders headers = new BlobHTTPHeaders()
            .setBlobContentMD5("data".getBytes(StandardCharsets.UTF_8))
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.uploadFromFile(filePath,
            new ParallelTransferOptions().setBlockSize(BlobAsyncClient.BLOB_MAX_UPLOAD_BLOCK_SIZE),
            headers, metadata, AccessTier.HOT, accessConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHTTPHeaders-Map-AccessTier-BlobAccessConditions
    }

}
