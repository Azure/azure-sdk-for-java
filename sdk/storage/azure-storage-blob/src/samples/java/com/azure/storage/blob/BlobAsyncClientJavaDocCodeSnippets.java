// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ReliableDownloadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link BlobAsyncClient}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientJavaDocCodeSnippets {
    private BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient("blobName");
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private URL url = JavaDocCodeSnippetsHelpers.generateURL("https://sample.com");
    private String file = "file";

    /**
     * Code snippet for {@link BlobAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClient#startCopyFromURL(URL)} and
     * {@link BlobAsyncClient#startCopyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL
        client.startCopyFromURL(url)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.startCopyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromURL(String)} and
     * {@link BlobAsyncClient#abortCopyFromURL(String, LeaseAccessConditions)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String
        client.abortCopyFromURL(copyId)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String-LeaseAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().leaseId(leaseId);
        client.abortCopyFromURL(copyId, leaseAccessConditions)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURL#String-LeaseAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromURL(URL)} and
     * {@link BlobAsyncClient#copyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL
        client.copyFromURL(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.copyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#download()} and
     * {@link BlobAsyncClient#download(BlobRange, ReliableDownloadOptions, BlobAccessConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download
        client.download().subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.value().subscribe(piece -> {
                try {
                    downloadData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.blob.BlobAsyncClient.download

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.download(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.value().subscribe(piece -> {
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
     * Code snippets for {@link BlobAsyncClient#downloadToFile(String)} and
     * {@link BlobAsyncClient#downloadToFile(String, BlobRange, Integer, ReliableDownloadOptions, BlobAccessConditions, boolean)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFile(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.BlobAsyncClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClient#delete()} and
     * {@link BlobAsyncClient#delete(DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void delete() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.delete
        client.delete()
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.delete

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions
        client.delete(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getProperties()} and
     * {@link BlobAsyncClient#getProperties(BlobAccessConditions)}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Type: %s, Size: %d%n", response.value().blobType(), response.value().blobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getProperties

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getProperties#BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.getProperties(accessConditions).subscribe(response ->
            System.out.printf("Type: %s, Size: %d%n", response.value().blobType(), response.value().blobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getProperties#BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders)} and
     * {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders, BlobAccessConditions)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .blobContentLanguage("en-US")
            .blobContentType("binary")).subscribe(response ->
            System.out.printf("Set HTTP headers completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.setHTTPHeaders(new BlobHTTPHeaders()
            .blobContentLanguage("en-US")
            .blobContentType("binary"), accessConditions).subscribe(response ->
            System.out.printf("Set HTTP headers completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadata(Metadata)} and
     * {@link BlobAsyncClient#setMetadata(Metadata, BlobAccessConditions)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata
        client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value")))
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value")), accessConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshot()} and
     * {@link BlobAsyncClient#createSnapshot(Metadata, BlobAccessConditions)}
     */
    public void createSnapshot() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshot
        client.createSnapshot()
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n",
                response.value().getSnapshotId()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshot

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshot#Metadata-BlobAccessConditions
        Metadata snapshotMetadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.createSnapshot(snapshotMetadata, accessConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n",
                response.value().getSnapshotId()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshot#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setTier(AccessTier)} and
     * {@link BlobAsyncClient#setTier(AccessTier, LeaseAccessConditions)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier
        client.setTier(AccessTier.HOT)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().leaseId(leaseId);

        client.setTier(AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#undelete()}
     */
    public void undelete() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.undelete
        client.undelete()
            .subscribe(response -> System.out.printf("Undelete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.undelete
    }

    /**
     * Code snippets for {@link BlobAsyncClient#acquireLease(String, int)} and
     * {@link BlobAsyncClient#acquireLease(String, int, ModifiedAccessConditions)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int
        client.acquireLease("proposedId", 60)
            .subscribe(response -> System.out.printf("Lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifModifiedSince(OffsetDateTime.now().minusDays(3));

        client.acquireLease("proposedId", 60, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#renewLease(String)} and
     * {@link BlobAsyncClient#renewLease(String, ModifiedAccessConditions)}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.renewLease#String
        client.renewLease(leaseId)
            .subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.renewLease#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.renewLease#String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.renewLease(leaseId, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.renewLease#String-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#releaseLease(String)} and
     * {@link BlobAsyncClient#releaseLease(String, ModifiedAccessConditions)}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.releaseLease#String
        client.releaseLease(leaseId)
            .subscribe(response -> System.out.printf("Release lease completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.releaseLease#String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.releaseLease#String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.releaseLease(leaseId, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Release lease completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.releaseLease#String-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#breakLease()} and
     * {@link BlobAsyncClient#breakLease(Integer, ModifiedAccessConditions)}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.breakLease
        client.breakLease()
            .subscribe(response ->
                System.out.printf("The broken lease has %d seconds remaining on the lease", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.breakLease

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.breakLease#Integer-ModifiedAccessConditions
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.breakLease(retainLeaseInSeconds, modifiedAccessConditions)
            .subscribe(response ->
                System.out.printf("The broken lease has %d seconds remaining on the lease", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.breakLease#Integer-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#changeLease(String, String)} and
     * {@link BlobAsyncClient#changeLease(String, String, ModifiedAccessConditions)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String
        client.changeLease(leaseId, "proposedId")
            .subscribe(response -> System.out.printf("Changed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.changeLease(leaseId, "proposedId", modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Changed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String-ModifiedAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.value().accountKind(), response.value().skuName()));
        // END: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
    }
}
