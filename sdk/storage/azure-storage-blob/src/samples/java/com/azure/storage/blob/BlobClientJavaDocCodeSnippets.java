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
import com.azure.storage.blob.models.StorageAccountInfo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link BlobClient}
 */
@SuppressWarnings("unused")
public class BlobClientJavaDocCodeSnippets {
    private BlobClient client = JavaDocCodeSnippetsHelpers.getBlobClient("blobName");
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private URL url = JavaDocCodeSnippetsHelpers.generateURL("https://sample.com");
    private String file = "file";
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippets for {@link BlobClient#exists()} and {@link BlobClient#exists(Duration)}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.exists
        System.out.printf("Exists? %b%n", client.exists().value());
        // END: com.azure.storage.blob.BlobClient.exists

        // BEGIN: com.azure.storage.blob.BlobClient.exists#Duration
        System.out.printf("Exists? %b%n", client.exists(timeout).value());
        // END: com.azure.storage.blob.BlobClient.exists#Duration
    }

    /**
     * Code snippets for {@link BlobClient#startCopyFromURL(URL)} and
     * {@link BlobClient#startCopyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions, Duration)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.startCopyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.startCopyFromURL(url).value());
        // END: com.azure.storage.blob.BlobClient.startCopyFromURL#URL

        // BEGIN: com.azure.storage.blob.BlobClient.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.startCopyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.startCopyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromURL(String)} and
     * {@link BlobClient#abortCopyFromURL(String, LeaseAccessConditions, Duration)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
        System.out.printf("Aborted copy completed with status %d%n", client.abortCopyFromURL(copyId).statusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String

        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String-LeaseAccessConditions-Duration
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().leaseId(leaseId);
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromURL(copyId, leaseAccessConditions, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String-LeaseAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURL(URL)} and
     * {@link BlobClient#copyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions, Duration)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.copyFromURL(url).value());
        // END: com.azure.storage.blob.BlobClient.copyFromURL#URL

        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.copyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions, timeout).value());
        // END: com.azure.storage.blob.BlobClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#download(OutputStream)} and
     * {@link BlobClient#download(OutputStream, BlobRange, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration)}
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.BlobClient.download#OutputStream
        System.out.printf("Download completed with status %d%n",
            client.download(new ByteArrayOutputStream()).statusCode());
        // END: com.azure.storage.blob.BlobClient.download#OutputStream

        // BEGIN: com.azure.storage.blob.BlobClient.download#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.download(new ByteArrayOutputStream(), range, options, null, false, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.download#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
    }

    /**
     * Code snippets for {@link BlobClient#downloadToFile(String)} and
     * {@link BlobClient#downloadToFile(String, BlobRange, Integer, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFile(file, range, null, options, null, false, timeout);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
    }

    /**
     * Code snippets for {@link BlobClient#delete()} and
     * {@link BlobClient#delete(DeleteSnapshotsOptionType, BlobAccessConditions, Duration)}
     */
    public void delete() {
        // BEGIN: com.azure.storage.blob.BlobClient.delete
        System.out.printf("Delete completed with status %d%n", client.delete().statusCode());
        // END: com.azure.storage.blob.BlobClient.delete

        // BEGIN: com.azure.storage.blob.BlobClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions-Duration
        System.out.printf("Delete completed with status %d%n",
            client.delete(DeleteSnapshotsOptionType.INCLUDE, null, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobClient.getProperties
        BlobProperties properties = client.getProperties().value();
        System.out.printf("Type: %s, Size: %d%n", properties.blobType(), properties.blobSize());
        // END: com.azure.storage.blob.BlobClient.getProperties
    }

    /**
     * Code snippet for {@link BlobClient#getProperties(BlobAccessConditions, Duration)}
     */
    public void getPropertiesWithTimeout() {
        // BEGIN: com.azure.storage.blob.BlobClient.getProperties#BlobAccessConditions-Duration
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        BlobProperties properties = client.getProperties(accessConditions, timeout).value();
        System.out.printf("Type: %s, Size: %d%n", properties.blobType(), properties.blobSize());
        // END: com.azure.storage.blob.BlobClient.getProperties#BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#setHTTPHeaders(BlobHTTPHeaders)} and
     * {@link BlobClient#setHTTPHeaders(BlobHTTPHeaders, BlobAccessConditions, Duration)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders
        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHTTPHeaders(new BlobHTTPHeaders()
                .blobContentLanguage("en-US")
                .blobContentType("binary"))
                .statusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders

        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions-Duration
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHTTPHeaders(new BlobHTTPHeaders()
                .blobContentLanguage("en-US")
                .blobContentType("binary"), accessConditions, timeout)
                .statusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#setMetadata(Metadata)} and
     * {@link BlobClient#setMetadata(Metadata, BlobAccessConditions, Duration)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadata#Metadata
        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value"))).statusCode());
        // END: com.azure.storage.blob.BlobClient.setMetadata#Metadata

        // BEGIN: com.azure.storage.blob.BlobClient.setMetadata#Metadata-BlobAccessConditions-Duration
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadata(
                new Metadata(Collections.singletonMap("metadata", "value")), accessConditions, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.setMetadata#Metadata-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshot()} and
     * {@link BlobClient#createSnapshot(Metadata, BlobAccessConditions, Duration)}
     */
    public void createSnapshot() {
        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshot
        System.out.printf("Identifier for the snapshot is %s%n", client.createSnapshot().value().getSnapshotId());
        // END: com.azure.storage.blob.BlobClient.createSnapshot

        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshot#Metadata-BlobAccessConditions-Duration
        Metadata snapshotMetadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshot(snapshotMetadata, accessConditions, timeout).value().getSnapshotId());
        // END: com.azure.storage.blob.BlobClient.createSnapshot#Metadata-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#setTier(AccessTier)} and
     * {@link BlobClient#setTier(AccessTier, LeaseAccessConditions, Duration)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier
        System.out.printf("Set tier completed with status code %d%n", client.setTier(AccessTier.HOT).statusCode());
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier

        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier-LeaseAccessConditions-Duration
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().leaseId(leaseId);

        System.out.printf("Set tier completed with status code %d%n",
            client.setTier(AccessTier.HOT, accessConditions, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier-LeaseAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#undelete()} and {@link BlobClient#undelete(Duration)}
     */
    public void undelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.undelete
        System.out.printf("Undelete completed with status %d%n", client.undelete().statusCode());
        // END: com.azure.storage.blob.BlobClient.undelete

        // BEGIN: com.azure.storage.blob.BlobClient.undelete#Duration
        System.out.printf("Undelete completed with status %d%n", client.undelete(timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.undelete#Duration
    }

    /**
     * Code snippets for {@link BlobClient#acquireLease(String, int)} and
     * {@link BlobClient#acquireLease(String, int, ModifiedAccessConditions, Duration)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.acquireLease#String-int
        System.out.printf("Lease ID is %s%n", client.acquireLease("proposedId", 60).value());
        // END: com.azure.storage.blob.BlobClient.acquireLease#String-int

        // BEGIN: com.azure.storage.blob.BlobClient.acquireLease#String-int-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifModifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Lease ID is %s%n",
            client.acquireLease("proposedId", 60, modifiedAccessConditions, timeout).value());
        // END: com.azure.storage.blob.BlobClient.acquireLease#String-int-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#renewLease(String)} and
     * {@link BlobClient#renewLease(String, ModifiedAccessConditions, Duration)}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.renewLease#String
        System.out.printf("Renewed lease ID is %s%n", client.renewLease(leaseId).value());
        // END: com.azure.storage.blob.BlobClient.renewLease#String

        // BEGIN: com.azure.storage.blob.BlobClient.renewLease#String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Renewed lease ID is %s%n",
            client.renewLease(leaseId, modifiedAccessConditions, timeout).value());
        // END: com.azure.storage.blob.BlobClient.renewLease#String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#releaseLease(String)} and
     * {@link BlobClient#releaseLease(String, ModifiedAccessConditions, Duration)}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.releaseLease#String
        System.out.printf("Release lease completed with status %d%n", client.releaseLease(leaseId).statusCode());
        // END: com.azure.storage.blob.BlobClient.releaseLease#String

        // BEGIN: com.azure.storage.blob.BlobClient.releaseLease#String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLease(leaseId, modifiedAccessConditions, timeout).statusCode());
        // END: com.azure.storage.blob.BlobClient.releaseLease#String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#breakLease()} and
     * {@link BlobClient#breakLease(Integer, ModifiedAccessConditions, Duration)}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.breakLease
        System.out.printf("The broken lease has %d seconds remaining on the lease", client.breakLease().value());
        // END: com.azure.storage.blob.BlobClient.breakLease

        // BEGIN: com.azure.storage.blob.BlobClient.breakLease#Integer-ModifiedAccessConditions-Duration
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("The broken lease has %d seconds remaining on the lease",
            client.breakLease(retainLeaseInSeconds, modifiedAccessConditions, timeout).value());
        // END: com.azure.storage.blob.BlobClient.breakLease#Integer-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#changeLease(String, String)} and
     * {@link BlobClient#changeLease(String, String, ModifiedAccessConditions, Duration)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.changeLease#String-String
        System.out.printf("Changed lease ID is %s%n", client.changeLease(leaseId, "proposedId").value());
        // END: com.azure.storage.blob.BlobClient.changeLease#String-String

        // BEGIN: com.azure.storage.blob.BlobClient.changeLease#String-String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Changed lease ID is %s%n",
            client.changeLease(leaseId, "proposedId", modifiedAccessConditions, timeout).value());
        // END: com.azure.storage.blob.BlobClient.changeLease#String-String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo().value();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.accountKind(), accountInfo.skuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo(Duration)}
     */
    public void getAccountInfoWithTimeout() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo#Duration
        StorageAccountInfo accountInfo = client.getAccountInfo(timeout).value();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.accountKind(), accountInfo.skuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo#Duration
    }
}
