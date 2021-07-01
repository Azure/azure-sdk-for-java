// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.RequestConditions;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link BlobAsyncClient}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientJavaDocCodeSnippets {

    private BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blobName");
    private Flux<ByteBuffer> data = Flux.just(ByteBuffer.wrap("data".getBytes(StandardCharsets.UTF_8)));
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private String url = "https://sample.com";
    private String file = "file";
    private long blockSize = 50;
    private int maxConcurrency = 2;
    private String filePath = "filePath";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

    /**
     * Code snippet for {@link BlobAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromUrl(String)}
     */
    public void abortCopyFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String
        client.abortCopyFromUrl(copyId).doOnSuccess(response -> System.out.println("Aborted copy from URL"));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromUrl(String)}
     */
    public void copyFromUrlCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURL#String
        client.copyFromUrl(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURL#String
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
     * Code snippets for {@link BlobAsyncClient#downloadContent()}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadContentCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadContent
        client.downloadContent().subscribe(data -> {
            System.out.printf("Downloaded %s", data.toString());
        });
        // END: com.azure.storage.blob.BlobAsyncClient.downloadContent
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadWithResponse(BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
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
        // END: com.azure.storage.blob.BlobAsyncClient.download#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadToFile(String)} and {@link BlobAsyncClient#downloadToFileWithResponse(
     * String, BlobRange, ParallelTransferOptions, DownloadRetryOptions, BlobRequestConditions, boolean)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        DownloadRetryOptions options = new DownloadRetryOptions().setMaxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-DownloadRetryOptions-BlobRequestConditions-boolean
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
     * Code snippets for {@link BlobAsyncClient#setHttpHeaders(BlobHttpHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHttpHeaders
        client.setHttpHeaders(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHttpHeaders
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
     * Code snippet for {@link BlobAsyncClient#existsWithResponse()}abortCopyFromURL
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromUrlWithResponse(String, String)}
     */
    public void abortCopyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromUrlWithResponse#String-String
        client.abortCopyFromUrlWithResponse(copyId, leaseId)
            .subscribe(
                response -> System.out.printf("Aborted copy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromUrlWithResponse#String-String
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromUrlWithResponse(String, Map, AccessTier,
     * RequestConditions, BlobRequestConditions)}
     */
    public void copyFromUrlWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromUrlWithResponse#String-Metadata-AccessTier-RequestConditions-BlobRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        RequestConditions modifiedRequestConditions = new RequestConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobRequestConditions blobRequestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.copyFromUrlWithResponse(url, metadata, AccessTier.HOT, modifiedRequestConditions, blobRequestConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromUrlWithResponse#String-Metadata-AccessTier-RequestConditions-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadWithResponse(BlobRange, DownloadRetryOptions,
     * BlobRequestConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
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
        // END: com.azure.storage.blob.BlobAsyncClient.downloadWithResponse#BlobRange-DownloadRetryOptions-BlobRequestConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobRequestConditions)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions
        client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getPropertiesWithResponse(BlobRequestConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobRequestConditions
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(requestConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.getValue().getBlobType(),
                response.getValue().getBlobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHttpHeadersWithResponse(BlobHttpHeaders, BlobRequestConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setHttpHeadersWithResponse(new BlobHttpHeaders()
            .setContentLanguage("en-US")
            .setContentType("binary"), requestConditions).subscribe(
                response ->
                    System.out.printf("Set HTTP headers completed with status %d%n",
                        response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setHttpHeadersWithResponse#BlobHttpHeaders-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadataWithResponse(Map, BlobRequestConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobRequestConditions
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), requestConditions)
            .subscribe(
                response -> System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshotWithResponse(Map, BlobRequestConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobRequestConditions
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId(leaseId);

        client.createSnapshotWithResponse(snapshotMetadata, requestConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n", response.getValue()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobRequestConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, String)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-RehydratePriority-String
        client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, leaseId)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-RehydratePriority-String
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
     * Generates a code sample for using {@link BlobAsyncClient#getContainerAsyncClient()}
     */
    public void getContainerClient() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerAsyncClient
        BlobContainerAsyncClient containerClient = client.getContainerAsyncClient();
        System.out.println("The name of the container is " + containerClient.getBlobContainerName());
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getContainerAsyncClient
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
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency);
        client.upload(data, parallelTransferOptions).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#upload(Flux, ParallelTransferOptions, boolean)}
     */
    public void uploadOverwrite() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency);
        boolean overwrite = false; // Default behavior
        client.upload(data, parallelTransferOptions, overwrite).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.upload#Flux-ParallelTransferOptions-boolean
    }

    /**
     * Code snippet for {@link BlobAsyncClient#upload(BinaryData)}
     */
    public void uploadBinaryData() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.upload#BinaryData
        client.upload(BinaryData.fromString("Data!")).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.upload#BinaryData
    }

    /**
     * Code snippet for {@link BlobAsyncClient#upload(BinaryData, boolean)}
     */
    public void uploadBinaryDataOverwrite() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.upload#BinaryData-boolean
        boolean overwrite = false; // Default behavior
        client.upload(BinaryData.fromString("Data!"), overwrite).subscribe(response ->
            System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.upload#BinaryData-boolean
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void upload4() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency);

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadWithResponse(Flux, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void upload5() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions.ProgressReporter
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressReceiver(bytesTransferred -> System.out.printf("Upload progress: %s bytes sent", bytesTransferred));

        client.uploadWithResponse(data, parallelTransferOptions, headers, metadata, AccessTier.HOT, requestConditions)
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#Flux-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions.ProgressReporter
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadWithResponse(BlobParallelUploadOptions)}
     */
    public void upload6() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency).setProgressReceiver(bytesTransferred ->
                System.out.printf("Upload progress: %s bytes sent", bytesTransferred));

        client.uploadWithResponse(new BlobParallelUploadOptions(data)
            .setParallelTransferOptions(parallelTransferOptions).setHeaders(headers).setMetadata(metadata).setTags(tags)
            .setTier(AccessTier.HOT).setRequestConditions(requestConditions))
            .subscribe(response -> System.out.printf("Uploaded BlockBlob MD5 is %s%n",
                Base64.getEncoder().encodeToString(response.getValue().getContentMd5())));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadWithResponse#BlobParallelUploadOptions
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
     * Code snippet for {@link BlobAsyncClient#uploadFromFile(String, boolean)}
     */
    public void uploadFromFileOverwrite() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-boolean
        boolean overwrite = false; // Default behavior
        client.uploadFromFile(filePath, overwrite)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-boolean
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadFromFile(String, ParallelTransferOptions, BlobHttpHeaders, Map, AccessTier, BlobRequestConditions)}
     */
    public void uploadFromFile2() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadFromFile(filePath,
            new ParallelTransferOptions().setBlockSizeLong(BlockBlobClient.MAX_STAGE_BLOCK_BYTES_LONG),
            headers, metadata, AccessTier.HOT, requestConditions)
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadFromFile#String-ParallelTransferOptions-BlobHttpHeaders-Map-AccessTier-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#uploadFromFileWithResponse(BlobUploadFromFileOptions)}
     */
    public void uploadFromFile3() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions
        BlobHttpHeaders headers = new BlobHttpHeaders()
            .setContentMd5("data".getBytes(StandardCharsets.UTF_8))
            .setContentLanguage("en-US")
            .setContentType("binary");

        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Map<String, String> tags = Collections.singletonMap("tag", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.uploadFromFileWithResponse(new BlobUploadFromFileOptions(filePath)
            .setParallelTransferOptions(
                new ParallelTransferOptions().setBlockSizeLong(BlobAsyncClient.BLOB_MAX_UPLOAD_BLOCK_SIZE))
            .setHeaders(headers).setMetadata(metadata).setTags(tags).setTier(AccessTier.HOT)
            .setRequestConditions(requestConditions))
            .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
            .subscribe(completion -> System.out.println("Upload from file succeeded"));
        // END: com.azure.storage.blob.BlobAsyncClient.uploadFromFileWithResponse#BlobUploadFromFileOptions
    }
}
