// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobHTTPHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
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
     * Code snippets for {@link BlobClient#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.BlobClient.exists
    }

    /**
     * Code snippets for {@link BlobClient#startCopyFromURL(URL)}
     */
    public void startCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.startCopyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.startCopyFromURL(url));
        // END: com.azure.storage.blob.BlobClient.startCopyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromURL(String)}
     */
    public void abortCopyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
        client.abortCopyFromURL(copyId);
        System.out.println("Aborted copy completed.");
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURL(URL)}
     */
    public void copyFromURL() {
        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURL#URL
        System.out.printf("Copy identifier: %s%n", client.copyFromURL(url));
        // END: com.azure.storage.blob.BlobClient.copyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobClient#download(OutputStream)}
     */
    public void download() {
        // BEGIN: com.azure.storage.blob.BlobClient.download#OutputStream
        client.download(new ByteArrayOutputStream());
        System.out.println("Download completed.");
        // END: com.azure.storage.blob.BlobClient.download#OutputStream
    }

    /**
     * Code snippets for {@link BlobClient#downloadToFile(String)} and
     * {@link BlobClient#downloadToFileWithResponse(String, BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobAccessConditions, boolean, Duration, Context)}
     */
    public void downloadToFile() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFile#String
        client.downloadToFile(file);
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFile#String

        // BEGIN: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, new ParallelTransferOptions().setBlockSize(4 * Constants.MB),
            options, null, false, timeout, new Context(key2, value2));
        System.out.println("Completed download to file");
        // END: com.azure.storage.blob.BlobClient.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.delete
        client.delete();
        System.out.println("Delete completed.");
        // END: com.azure.storage.blob.BlobClient.delete
    }

    /**
     * Code snippets for {@link BlobClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobClient.getProperties
        BlobProperties properties = client.getProperties();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.BlobClient.getProperties
    }

    /**
     * Code snippets for {@link BlobClient#setHTTPHeaders(BlobHTTPHeaders)}
     */
    public void setHTTPHeaders() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
        System.out.println("Set HTTP headers completed");
        // END: com.azure.storage.blob.BlobClient.setHTTPHeaders#BlobHTTPHeaders
    }

    /**
     * Code snippets for {@link BlobClient#setMetadata(Metadata)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadata#Metadata
        client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value")));
        System.out.println("Set metadata completed");
        // END: com.azure.storage.blob.BlobClient.setMetadata#Metadata
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshot()}
     */
    public void createSnapshot() {
        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshot
        System.out.printf("Identifier for the snapshot is %s%n", client.createSnapshot().getSnapshotId());
        // END: com.azure.storage.blob.BlobClient.createSnapshot
    }

    /**
     * Code snippets for {@link BlobClient#setTier(AccessTier)} and
     * {@link BlobClient#setTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions, Duration, Context)}
     */
    public void setTier() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTier#AccessTier
        System.out.printf("Set tier completed with status code %d%n",
            client.setTierWithResponse(AccessTier.HOT, null, null, null, null).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setTier#AccessTier


    }

    /**
     * Code snippets for {@link BlobClient#undelete()}
     */
    public void unsetDelete() {
        // BEGIN: com.azure.storage.blob.BlobClient.undelete
        client.undelete();
        System.out.println("Undelete completed");
        // END: com.azure.storage.blob.BlobClient.undelete
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobClient#existsWithResponse(Duration, Context)}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobClient.existsWithResponse#Duration-Context
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, new Context(key2, value2)).getValue());
        // END: com.azure.storage.blob.BlobClient.existsWithResponse#Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#startCopyFromURLWithResponse(URL, Metadata, AccessTier, RehydratePriority,
     * ModifiedAccessConditions, BlobAccessConditions, Duration, Context)}
     */
    public void startCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.startCopyFromURLWithResponse(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
                modifiedAccessConditions, blobAccessConditions, timeout,
                new Context(key2, value2)));
        // END: com.azure.storage.blob.BlobClient.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#abortCopyFromURLWithResponse(String, LeaseAccessConditions, Duration, Context)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        System.out.printf("Aborted copy completed with status %d%n",
            client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#copyFromURLWithResponse(URL, Metadata, AccessTier, ModifiedAccessConditions,
     * BlobAccessConditions, Duration, Context)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Copy identifier: %s%n",
            client.copyFromURLWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions,
                blobAccessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#downloadWithResponse(OutputStream, BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean, Duration, Context)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context
        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        System.out.printf("Download completed with status %d%n",
            client.downloadWithResponse(new ByteArrayOutputStream(), range, options, null, false,
                timeout, new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.downloadWithResponse#OutputStream-BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean-Duration-Context

    }

    /**
     * Code snippets for {@link BlobClient#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions, Duration,
     * Context)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
        System.out.printf("Delete completed with status %d%n",
            client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#getPropertiesWithResponse(BlobAccessConditions, Duration, Context)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        BlobProperties properties = client.getPropertiesWithResponse(accessConditions, timeout,
            new Context(key2, value2)).getValue();
        System.out.printf("Type: %s, Size: %d%n", properties.getBlobType(), properties.getBlobSize());
        // END: com.azure.storage.blob.BlobClient.getPropertiesWithResponse#BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setHTTPHeadersWithResponse(BlobHTTPHeaders, BlobAccessConditions, Duration,
     * Context)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set HTTP headers completed with status %d%n",
            client.setHTTPHeadersWithResponse(new BlobHTTPHeaders()
                .setBlobContentLanguage("en-US")
                .setBlobContentType("binary"), accessConditions, timeout, new Context(key1, value1))
                .getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setMetadataWithResponse(Metadata, BlobAccessConditions, Duration, Context)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobAccessConditions-Duration-Context
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(
                new Metadata(Collections.singletonMap("metadata", "value")), accessConditions, timeout,
                new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setMetadataWithResponse#Metadata-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#createSnapshotWithResponse(Metadata, BlobAccessConditions, Duration,
     * Context)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobAccessConditions-Duration-Context
        Metadata snapshotMetadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        System.out.printf("Identifier for the snapshot is %s%n",
            client.createSnapshotWithResponse(snapshotMetadata, accessConditions, timeout,
                new Context(key1, value1)).getValue());
        // END: com.azure.storage.blob.BlobClient.createSnapshotWithResponse#Metadata-BlobAccessConditions-Duration-Context
    }

    /**
     * Code snippets for {@link BlobClient#setTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions,
     * Duration, Context)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        System.out.printf("Set tier completed with status code %d%n",
            client.setTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, accessConditions, timeout,
                new Context(key2, value2)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.setTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobClient#undeleteWithResponse(Duration, Context)}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.undeleteWithResponse#Duration-Context
        System.out.printf("Undelete completed with status %d%n", client.undeleteWithResponse(timeout,
            new Context(key1, value1)).getStatusCode());
        // END: com.azure.storage.blob.BlobClient.undeleteWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobClient#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.getAccountInfoWithResponse#Duration-Context
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, new Context(key1, value1)).getValue();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobClient.getAccountInfoWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobClient#generateUserDelegationSAS(UserDelegationKey, String, BlobSASPermission,
     * OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.generateUserDelegationSAS#UserDelegationKey-String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        BlobSASPermission permissions = new BlobSASPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        UserDelegationKey userDelegationKey = new UserDelegationKey();
        String accountName = "accountName";

        String sas = client.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime,
            startTime, version, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding,
            contentLanguage, contentType);
        // END: com.azure.storage.blob.BlobClient.generateUserDelegationSAS#UserDelegationKey-String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link BlobClient#generateSAS(String, BlobSASPermission, OffsetDateTime, OffsetDateTime, String,
     * SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobClient.generateSAS#String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        BlobSASPermission permissions = new BlobSASPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String identifier = "identifier";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange,
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.blob.BlobClient.generateSAS#String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Generates a code sample for using {@link BlobClient#getContainerName()}
     */
    public void getContainerName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getContainerName
        String containerName = client.getContainerName();
        System.out.println("The name of the blob is " + containerName);
        // END: com.azure.storage.blob.specialized.BlobClientBase.getContainerName
    }

    /**
     * Generates a code sample for using {@link BlobClient#getBlobName()}
     */
    public void getBlobName() {
        // BEGIN: com.azure.storage.blob.specialized.BlobClientBase.getBlobName
        String blobName = client.getBlobName();
        System.out.println("The name of the blob is " + blobName);
        // END: com.azure.storage.blob.specialized.BlobClientBase.getBlobName
    }
}
