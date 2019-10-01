// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.blob.BlobSasPermission;
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
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link BlobAsyncClientBase}
 */
@SuppressWarnings("unused")
public class BlobAsyncClientBaseJavaDocCodeSnippets {
    private BlobAsyncClientBase client = new BlobAsyncClientBase(null, null, null);
    private String leaseId = "leaseId";
    private String copyId = "copyId";
    private URL url = new URL("https://sample.com");
    private String file = "file";

    /**
     * @throws MalformedURLException Ignore
     */
    public BlobAsyncClientBaseJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#exists()}
     */
    public void existsCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.exists
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#startCopyFromURL(URL)}
     */
    public void startCopyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURL#URL
        client.startCopyFromURL(url)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURL#URL
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#abortCopyFromURL(String)}
     */
    public void abortCopyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURL#String
        client.abortCopyFromURL(copyId).doOnSuccess(response -> System.out.println("Aborted copy from URL"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURL#String
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#copyFromURL(URL)}
     */
    public void copyFromURLCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURL#URL
        client.copyFromURL(url).subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURL#URL
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
     * Code snippet for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
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
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.download#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadToFile(String)} and {@link BlobAsyncClientBase#downloadToFileWithResponse(String,
     * BlobRange, ParallelTransferOptions, ReliableDownloadOptions, BlobAccessConditions, boolean)}
     */
    public void downloadToFileCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String
        client.downloadToFile(file).subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFile#String

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean

        BlobRange range = new BlobRange(1024, 2048L);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadToFileWithResponse(file, range, null, options, null, false)
            .subscribe(response -> System.out.println("Completed download to file"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadToFileWithResponse#String-BlobRange-ParallelTransferOptions-ReliableDownloadOptions-BlobAccessConditions-boolean
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
     * Code snippets for {@link BlobAsyncClientBase#setHTTPHeaders(BlobHTTPHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeaders#BlobHTTPHeaders
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setMetadata(Metadata)}
     */
    public void setMetadataCodeSnippet() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Metadata
        client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value")));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadata#Metadata
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
     * Code snippets for {@link BlobAsyncClientBase#startCopyFromURLWithResponse(URL, Metadata, AccessTier,
     * RehydratePriority, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void startCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.startCopyFromURLWithResponse(url, metadata, AccessTier.HOT, RehydratePriority.STANDARD,
            modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.startCopyFromURLWithResponse#URL-Metadata-AccessTier-RehydratePriority-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#abortCopyFromURLWithResponse(String, LeaseAccessConditions)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURLWithResponse#String-LeaseAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.abortCopyFromURLWithResponse#String-LeaseAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#copyFromURLWithResponse(URL, Metadata, AccessTier,
     * ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.copyFromURLWithResponse(url, metadata, AccessTier.HOT, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.copyFromURLWithResponse#URL-Metadata-AccessTier-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#downloadWithResponse(BlobRange, ReliableDownloadOptions,
     * BlobAccessConditions, boolean)}
     *
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
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
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#deleteWithResponse(DeleteSnapshotsOptionType, BlobAccessConditions)}
     */
    public void deleteWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions
        client.deleteWithResponse(DeleteSnapshotsOptionType.INCLUDE, null)
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#getPropertiesWithResponse(BlobAccessConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.getPropertiesWithResponse(accessConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.getValue().getBlobType(),
                response.getValue().getBlobSize()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.getPropertiesWithResponse#BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setHTTPHeadersWithResponse(BlobHTTPHeaders, BlobAccessConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.setHTTPHeadersWithResponse(new BlobHTTPHeaders()
            .setBlobContentLanguage("en-US")
            .setBlobContentType("binary"), accessConditions).subscribe(
                response ->
                    System.out.printf("Set HTTP headers completed with status %d%n",
                        response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setMetadataWithResponse(Metadata, BlobAccessConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Metadata-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId));

        client.setMetadataWithResponse(new Metadata(Collections.singletonMap("metadata", "value")), accessConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setMetadataWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#createSnapshotWithResponse(Metadata, BlobAccessConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Metadata-BlobAccessConditions
        Metadata snapshotMetadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions().setLeaseAccessConditions(
            new LeaseAccessConditions().setLeaseId(leaseId));

        client.createSnapshotWithResponse(snapshotMetadata, accessConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n", response.getValue()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.createSnapshotWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClientBase#setAccessTierWithResponse(AccessTier, RehydratePriority, LeaseAccessConditions)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.setAccessTierWithResponse(AccessTier.HOT, RehydratePriority.STANDARD, accessConditions)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.getStatusCode()));
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.setAccessTierWithResponse#AccessTier-RehydratePriority-LeaseAccessConditions
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
     * Code snippet for {@link BlobAsyncClientBase#generateUserDelegationSAS(UserDelegationKey, String, BlobSasPermission,
     * OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSAS#UserDelegationKey-String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        BlobSasPermission permissions = new BlobSasPermission()
            .setAddPermission(true)
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
        String accountName = "accountName";
        UserDelegationKey userDelegationKey = new UserDelegationKey();

        String sas = client.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime,
            startTime, version, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding,
            contentLanguage, contentType);
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateUserDelegationSAS#UserDelegationKey-String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link BlobAsyncClientBase#generateSAS(String, BlobSasPermission, OffsetDateTime, OffsetDateTime,
     * String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSAS#String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        BlobSasPermission permissions = new BlobSasPermission()
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
        // END: com.azure.storage.blob.specialized.BlobAsyncClientBase.generateSAS#String-BlobSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }
}
