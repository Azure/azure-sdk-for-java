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
public class BlobAsyncClientJavaDocCodeSnippets {

    private String blobName = "blob";
    private BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    private String leaseId = "lease";
    private String copyId = "copyId";
    private URL url = JavaDocCodeSnippetsHelpers.generateURL("https://sample.com");
    private String file = "file";

    /**
     * Code snippet for {@link BlobAsyncClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response.value()));
        // END: com.azure.storage.blob.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClient#startCopyFromURL(URL)} and
     * {@link BlobAsyncClient#startCopyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.startCopyFromURL#URL
        client.startCopyFromURL(url)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.startCopyFromURL#URL

        // BEGIN: com.azure.storage.blob.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions().ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.startCopyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromURL(String)} and
     * {@link BlobAsyncClient#abortCopyFromURL(String, LeaseAccessConditions)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.abortCopyFromURL#String
        client.abortCopyFromURL(copyId)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.abortCopyFromURL#String

        // BEGIN: com.azure.storage.blob.abortCopyFromURL#String-LeaseAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().leaseId(leaseId);
        client.abortCopyFromURL(copyId, leaseAccessConditions)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.abortCopyFromURL#String-LeaseAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromURL(URL)} and
     * {@link BlobAsyncClient#copyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.copyFromURL#URL
        client.copyFromURL(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.copyFromURL#URL

        // BEGIN: com.azure.storage.blob.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions().ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        client.copyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#download()} and
     * {@link BlobAsyncClient#download(BlobRange, BlobAccessConditions, boolean, ReliableDownloadOptions)}
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.download
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
        // END: com.azure.storage.blob.download

        // BEGIN: com.azure.storage.blob.download#BlobRange-BlobAccessConditions-boolean-ReliableDownloadOptions
        BlobRange range = new BlobRange(1024, 2048);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.download(range, null, false, options).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.value().subscribe(piece -> {
                try {
                    downloadData.write(piece.array());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
        // END: com.azure.storage.blob.download#BlobRange-BlobAccessConditions-boolean-ReliableDownloadOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadToFile(String)} and
     * {@link BlobAsyncClient#downloadToFile(String, BlobRange, Integer, BlobAccessConditions, boolean, ReliableDownloadOptions)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.downloadToFile#String

        // BEGIN: com.azure.storage.blob.downloadToFile#String-BlobRange-Integer-BlobAccessConditions-boolean-ReliableDownloadOptions
        BlobRange range = new BlobRange(1024, 2048);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFile(file, range, null, null, false, options)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.downloadToFile#String-BlobRange-Integer-BlobAccessConditions-boolean-ReliableDownloadOptions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#delete()} and
     * {@link BlobAsyncClient#delete(DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void delete() {
        // BEGIN: com.azure.storage.blob.delete
        client.delete().subscribe(response -> System.out.printf("Delete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.delete

        // BEGIN: com.azure.storage.blob.delete#DeleteSnapshotsOptionType-BlobAccessConditions
        client.delete(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.delete#DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getProperties()} and
     * {@link BlobAsyncClient#getProperties(BlobAccessConditions)}
     */
    public void getProperties() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders)} and
     * {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders, BlobAccessConditions)}
     */
    public void setHTTPHeaders() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadata(Metadata)} and
     * {@link BlobAsyncClient#setMetadata(Metadata, BlobAccessConditions)}
     */
    public void setMetadata() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshot()} and
     * {@link BlobAsyncClient#createSnapshot(Metadata, BlobAccessConditions)}
     */
    public void createSnapshot() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setTier(AccessTier)} and
     * {@link BlobAsyncClient#setTier(AccessTier, LeaseAccessConditions)}
     */
    public void setTier() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippet for {@link BlobAsyncClient#undelete()}
     */
    public void undelete() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#acquireLease(String, int)} and
     * {@link BlobAsyncClient#acquireLease(String, int, ModifiedAccessConditions)}
     */
    public void acquireLease() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#renewLease(String)} and
     * {@link BlobAsyncClient#renewLease(String, ModifiedAccessConditions)}
     */
    public void renewLease() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#releaseLease(String)} and
     * {@link BlobAsyncClient#releaseLease(String, ModifiedAccessConditions)}
     */
    public void releaseLease() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#breakLease()} and
     * {@link BlobAsyncClient#breakLease(Integer, ModifiedAccessConditions)}
     */
    public void breakLease() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippets for {@link BlobAsyncClient#changeLease(String, String)} and
     * {@link BlobAsyncClient#changeLease(String, String, ModifiedAccessConditions)}
     */
    public void changeLease() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        BlobAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobAsyncClient(blobName);
    }
}
