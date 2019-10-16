// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobProperties;
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
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.common.implementation.Constants;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
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
    private URL url = new URL("https://sample.com");
    private String file = "file";
    private Duration timeout = Duration.ofSeconds(30);
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * @throws MalformedURLException Ignore
     */
    public BlobClientBaseJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippets for {@link BlobClientBase#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.specialized.BlobClientBase.exists
    }

    /**
     * Code snippets for {@link BlobClientBase#startCopyFromURL(URL)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.startCopyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.startCopyFromURL(url));
        // END: com.azure.storage.blob.specialized.BlobClientBase.startCopyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobClientBase#abortCopyFromURL(String)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromURL#String
        client.abortCopyFromURL(copyId);
        System.out.println("Aborted copy completed.");
        // END: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobClientBase#copyFromURL(URL)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.copyFromURL(url));
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromURL#URL
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
     * {@link BlobClientBase#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobAccessConditions,
     * boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions().setBlockSize(4 * Constants.MB),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.specialized.BlobClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
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
     * Code snippets for {@link BlobClientBase#setHTTPHeaders(BlobHTTPHeaders)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
        System.out.println("Set HTTP headers completed");
        // END: com.azure.storage.blob.specialized.BlobClientBase.setHTTPHeaders#BlobHTTPHeaders
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
     * Code snippets for {@link BlobClientBase#setAccessTier(AccessTier)} and
     * {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions, Duration, Context)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setAccessTier#AccessTier
        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode());
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
     * Code snippets for {@link BlobClientBase#startCopyFromURLWithResponse(URL, Map, AccessTier, RehydratePriority,
     * ModifiedAccessConditions, BlobAccessConditions, Duration, Context)}
     */
    public void startCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.startCopyFromURLWithResponse#URL-Map-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.startCopyFromURLWithResponse(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
                modifiedAccessConditions, blobAccessConditions, timeout,
                new Context(key2, value2)));
        // END: com.azure.storage.blob.specialized.BlobClientBase.startCopyFromURLWithResponse#URL-Map-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#abortCopyFromURLWithResponse(String, LeaseAccessConditions, Duration, Context)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#copyFromURLWithResponse(URL, Map, AccessTier, ModifiedAccessConditions,
     * BlobAccessConditions, Duration, Context)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.copyFromURLWithResponse#URL-Map-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.copyFromURLWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions,
                blobAccessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.copyFromURLWithResponse#URL-Map-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#downloadWithResponse(OutputStream, BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean, Duration, Context)}
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
     * Code snippets for {@link BlobClientBase#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions, Duration,
     * Context)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#getPropertiesWithResponse(BlobAccessConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        BlobProperties properties = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2)).getValue();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.specialized.BlobClientBase.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setHTTPHeadersWithResponse(BlobHTTPHeaders, BlobAccessConditions, Duration,
     * Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHTTPHeadersWithResponse(new BlobHTTPHeaders()
                .setBlobContentLanguage("en-US")
                .setBlobContentType("binary"), accessConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setMetadataWithResponse(Map, BlobAccessConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(Collections.singletonMap("metadata", "value"), accessConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setMetadataWithResponse#Map-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#createSnapshotWithResponse(Map, BlobAccessConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobAccessConditions-Duration-Context
        Map<String, String> snapshotMetadata = Collections.singletonMap("metadata", "value");
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, accessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.specialized.BlobClientBase.createSnapshotWithResponse#Map-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions, Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        System.out.printf("Set tier completed with status code %d%n",
            client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, accessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.specialized.BlobClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
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
