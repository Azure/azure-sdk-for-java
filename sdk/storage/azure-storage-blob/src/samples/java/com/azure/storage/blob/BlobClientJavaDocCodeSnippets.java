// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
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
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Code snippets for {@link BlobClient#exists()} and {@link BlobClient#exists(Duration)}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.BlobClient.exists

        // BEGIN: com.azure.storage.blob.BlobClient.exists#Duration
        System.out.printf("Exists? %b%n", client.exists(timeout));
        // END: com.azure.storage.blob.BlobClient.exists#Duration
    }

    /**
     * Code snippets for {@link BlobClient#startCopyFromURL(URL)} and
     * {@link BlobClient#startCopyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions, Duration)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.startCopyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.startCopyFromURL(url));
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
     * {@link BlobClient#abortCopyFromURLWithResponse(String, LeaseAccessConditions, Duration, Context)} (String, LeaseAccessConditions, Duration, Context)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
        System.out.printf("Aborted copy completed with status %d%n", client.abortCopyFromURLWithResponse(copyId, null, null, new Context("key1", "value1")).statusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String

        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String-LeaseAccessConditions-Duration
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().leaseId(leaseId);
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions, timeout, new Context(key1, value1)).statusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String-LeaseAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURL(URL)} and
     * {@link BlobClient#copyFromURL(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions, Duration)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.copyFromURL(url));
        // END: com.azure.storage.blob.BlobClient.copyFromURL#URL

        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(new LeaseAccessConditions().leaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.copyFromURL(url, metadata, modifiedAccessConditions, blobAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.copyFromURL#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#download(OutputStream)} and
     * {@link BlobClient#download(OutputStream, BlobRange, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration, Context)}
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
            client.download(new ByteArrayOutputStream(), range, options, null, false, timeout, new Context(key2, value2)).statusCode());
        // END: com.azure.storage.blob.BlobClient.download#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
    }

    /**
     * Code snippets for {@link BlobClient#downloadToFile(String)} and
     * {@link BlobClient#downloadToFile(String, BlobRange, Integer, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFile(file, range, null, options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String-BlobRange-Integer-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration
    }

    /**
     * Code snippets for {@link BlobClient#delete()} and
     * {@link BlobClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions, Duration, Context)} (DeleteSnapshotsOptionType, BlobAccessConditions, Duration, Context)}
     */
    public void delete() {
        // BEGIN: com.azure.storage.blob.BlobClient.delete
        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(null, null, null, null).statusCode());
        // END: com.azure.storage.blob.BlobClient.delete

        // BEGIN: com.azure.storage.blob.BlobClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions-Duration
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout, new Context(key2, value2)).statusCode());
        // END: com.azure.storage.blob.BlobClient.delete#DeleteSnapshotsOptionType-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobClient.getProperties
        BlobProperties properties = client.getProperties();
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

        BlobProperties properties = client.getProperties(accessConditions, timeout);
        System.out.printf("Type: %s, Size: %d%n", properties.blobType(), properties.blobSize());
        // END: com.azure.storage.blob.BlobClient.getProperties#BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#setHTTPHeaders(BlobHTTPHeaders)} and
     * {@link BlobClient#setHTTPHeaders(BlobHTTPHeaders, BlobAccessConditions, Duration, Context)}
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
                .blobContentType("binary"), accessConditions, timeout, new Context(key2, value2))
                .statusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders-BlobAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#setMetadata(Metadata)} and
     * {@link BlobClient#setMetadata(Metadata, BlobAccessConditions, Duration, Context)}
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
                new Metadata(Collections.singletonMap("metadata", "value")), accessConditions, timeout, new Context(key2, value2)).statusCode());
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
     * {@link BlobClient#setTierWithResponse(AccessTier, LeaseAccessConditions, Duration, Context)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier
        System.out.printf("Set tier completed with status code %d%n", client.setTierWithResponse(AccessTier.HOT, null, null, null).statusCode());
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier

        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier-LeaseAccessConditions-Duration
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().leaseId(leaseId);

        System.out.printf("Set tier completed with status code %d%n",
            client.setTierWithResponse(AccessTier.HOT, accessConditions, timeout, new Context(key2, value2)).statusCode());
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier-LeaseAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#undelete()} and {@link BlobClient#undelete(Duration, Context)}
     */
    public void undelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.undelete
        System.out.printf("Undelete completed with status %d%n", client.undelete().statusCode());
        // END: com.azure.storage.blob.BlobClient.undelete

        // BEGIN: com.azure.storage.blob.BlobClient.undelete#Duration
        System.out.printf("Undelete completed with status %d%n", client.undelete(timeout, new Context(key2, value2)).statusCode());
        // END: com.azure.storage.blob.BlobClient.undelete#Duration
    }

    /**
     * Code snippets for {@link BlobClient#acquireLease(String, int)} and
     * {@link BlobClient#acquireLease(String, int, ModifiedAccessConditions, Duration)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.acquireLease#String-int
        System.out.printf("Lease ID is %s%n", client.acquireLease("proposedId", 60));
        // END: com.azure.storage.blob.BlobClient.acquireLease#String-int

        // BEGIN: com.azure.storage.blob.BlobClient.acquireLease#String-int-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifModifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Lease ID is %s%n",
            client.acquireLease("proposedId", 60, modifiedAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.acquireLease#String-int-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#renewLease(String)} and
     * {@link BlobClient#renewLease(String, ModifiedAccessConditions, Duration)}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.renewLease#String
        System.out.printf("Renewed lease ID is %s%n", client.renewLease(leaseId));
        // END: com.azure.storage.blob.BlobClient.renewLease#String

        // BEGIN: com.azure.storage.blob.BlobClient.renewLease#String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Renewed lease ID is %s%n",
            client.renewLease(leaseId, modifiedAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.renewLease#String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#releaseLease(String)} and
     * {@link BlobClient#releaseLease(String, ModifiedAccessConditions, Duration, Context)}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.releaseLease#String
        System.out.printf("Release lease completed with status %d%n", client.releaseLease(leaseId).statusCode());
        // END: com.azure.storage.blob.BlobClient.releaseLease#String

        // BEGIN: com.azure.storage.blob.BlobClient.releaseLease#String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLease(leaseId, modifiedAccessConditions, timeout, new Context(key2, value2)).statusCode());
        // END: com.azure.storage.blob.BlobClient.releaseLease#String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#breakLease()} and
     * {@link BlobClient#breakLease(Integer, ModifiedAccessConditions, Duration)}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.breakLease
        System.out.printf("The broken lease has %d seconds remaining on the lease", client.breakLease());
        // END: com.azure.storage.blob.BlobClient.breakLease

        // BEGIN: com.azure.storage.blob.BlobClient.breakLease#Integer-ModifiedAccessConditions-Duration
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("The broken lease has %d seconds remaining on the lease",
            client.breakLease(retainLeaseInSeconds, modifiedAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.breakLease#Integer-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippets for {@link BlobClient#changeLease(String, String)} and
     * {@link BlobClient#changeLease(String, String, ModifiedAccessConditions, Duration)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.BlobClient.changeLease#String-String
        System.out.printf("Changed lease ID is %s%n", client.changeLease(leaseId, "proposedId"));
        // END: com.azure.storage.blob.BlobClient.changeLease#String-String

        // BEGIN: com.azure.storage.blob.BlobClient.changeLease#String-String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Changed lease ID is %s%n",
            client.changeLease(leaseId, "proposedId", modifiedAccessConditions, timeout));
        // END: com.azure.storage.blob.BlobClient.changeLease#String-String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.accountKind(), accountInfo.skuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo(Duration)}
     */
    public void getAccountInfoWithTimeout() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo#Duration
        StorageAccountInfo accountInfo = client.getAccountInfo(timeout);
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.accountKind(), accountInfo.skuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo#Duration
    }
}
