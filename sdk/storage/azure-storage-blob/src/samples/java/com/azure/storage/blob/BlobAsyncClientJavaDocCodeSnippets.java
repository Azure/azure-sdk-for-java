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
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

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
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.download
        client.download().subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.subscribe(piece -> {
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

        client.downloadWithResponse(range, options, null, false).subscribe(response -> {
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
    public void downloadToFileCodeSnippet() {
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
            System.out.printf("Type: %s, Size: %d%n", response.blobType(), response.blobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getProperties
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeaders(BlobHTTPHeaders)}
     */
    public void setHTTPHeadersCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders
        client.setHTTPHeaders(new BlobHTTPHeaders()
            .blobContentLanguage("en-US")
            .blobContentType("binary"));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeaders#BlobHTTPHeaders
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadata(Metadata)}
     */
    public void setMetadataCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadata#Metadata
        client.setMetadata(new Metadata(Collections.singletonMap("metadata", "value")));
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
     * Code snippets for {@link BlobAsyncClient#setTier(AccessTier)}
     */
    public void setTierCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTier#AccessTier
        client.setTier(AccessTier.HOT);
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
     * Code snippets for {@link BlobAsyncClient#acquireLease(String, int)}
     */
    public void acquireLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int
        client.acquireLease("proposedId", 60)
            .subscribe(response -> System.out.printf("Lease ID is %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.acquireLease#String-int
    }

    /**
     * Code snippets for {@link BlobAsyncClient#renewLease(String)}
     */
    public void renewLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.renewLease#String
        client.renewLease(leaseId)
            .subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.renewLease#String
    }

    /**
     * Code snippets for {@link BlobAsyncClient#releaseLease(String)}
     */
    public void releaseLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.releaseLease#String
        client.releaseLease(leaseId).doOnSuccess(response -> System.out.println("Completed release lease"));
        // END: com.azure.storage.blob.BlobAsyncClient.releaseLease#String
    }

    /**
     * Code snippets for {@link BlobAsyncClient#breakLease()}
     */
    public void breakLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.breakLease
        client.breakLease()
            .subscribe(response ->
                System.out.printf("The broken lease has %d seconds remaining on the lease", response));
        // END: com.azure.storage.blob.BlobAsyncClient.breakLease
    }

    /**
     * Code snippets for {@link BlobAsyncClient#changeLease(String, String)}
     */
    public void changeLeaseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String
        client.changeLease(leaseId, "proposedId")
            .subscribe(response -> System.out.printf("Changed lease ID is %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.changeLease#String-String
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfo()}
     */
    public void getAccountInfoCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.accountKind(), response.skuName()));
        // END: com.azure.storage.blob.BlobAsyncClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobAsyncClient#existsWithResponse()}
     */
    public void existsWithResponseCodeSnippet() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.existsWithResponse
    }

    /**
     * Code snippets for {@link BlobAsyncClient#startCopyFromURLWithResponse(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void startCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.startCopyFromURLWithResponse#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(
                new LeaseAccessConditions().leaseId(leaseId));

        client.startCopyFromURLWithResponse(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.startCopyFromURLWithResponse#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#abortCopyFromURLWithResponse(String, LeaseAccessConditions)}
     */
    public void abortCopyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions
        LeaseAccessConditions leaseAccessConditions = new LeaseAccessConditions().leaseId(leaseId);
        client.abortCopyFromURLWithResponse(copyId, leaseAccessConditions)
            .subscribe(response -> System.out.printf("Aborted copy completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.abortCopyFromURLWithResponse#String-LeaseAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#copyFromURLWithResponse(URL, Metadata, ModifiedAccessConditions, BlobAccessConditions)}
     */
    public void copyFromURLWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.copyFromURLWithResponse#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(7));
        BlobAccessConditions blobAccessConditions = new BlobAccessConditions()
            .leaseAccessConditions(
                new LeaseAccessConditions().leaseId(leaseId));

        client.copyFromURLWithResponse(url, metadata, modifiedAccessConditions, blobAccessConditions)
            .subscribe(response -> System.out.printf("Copy identifier: %s%n", response));
        // END: com.azure.storage.blob.BlobAsyncClient.copyFromURLWithResponse#URL-Metadata-ModifiedAccessConditions-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#downloadWithResponse(BlobRange, ReliableDownloadOptions, BlobAccessConditions, boolean)}
     * @throws UncheckedIOException If an I/O error occurs
     */
    public void downloadWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.downloadWithResponse#BlobRange-ReliableDownloadOptions-BlobAccessConditions-boolean
        BlobRange range = new BlobRange(1024, (long) 2048);
        ReliableDownloadOptions options = new ReliableDownloadOptions().maxRetryRequests(5);

        client.downloadWithResponse(range, options, null, false).subscribe(response -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            response.value().subscribe(piece -> {
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
            .subscribe(response -> System.out.printf("Delete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.deleteWithResponse#DeleteSnapshotsOptionType-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#getPropertiesWithResponse(BlobAccessConditions)}
     */
    public void getPropertiesWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(
                new LeaseAccessConditions().leaseId(leaseId));

        client.getPropertiesWithResponse(accessConditions).subscribe(
            response -> System.out.printf("Type: %s, Size: %d%n", response.value().blobType(),
                response.value().blobSize()));
        // END: com.azure.storage.blob.BlobAsyncClient.getPropertiesWithResponse#BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setHTTPHeadersWithResponse(BlobHTTPHeaders, BlobAccessConditions)}
     */
    public void setHTTPHeadersWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(
                new LeaseAccessConditions().leaseId(leaseId));

        client.setHTTPHeadersWithResponse(new BlobHTTPHeaders()
            .blobContentLanguage("en-US")
            .blobContentType("binary"), accessConditions).subscribe(
                response ->
                System.out.printf("Set HTTP headers completed with status %d%n",
                    response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setHTTPHeadersWithResponse#BlobHTTPHeaders-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setMetadataWithResponse(Metadata, BlobAccessConditions)}
     */
    public void setMetadataWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobAccessConditions
        BlobAccessConditions accessConditions = new BlobAccessConditions()
            .leaseAccessConditions(
                new LeaseAccessConditions().leaseId(leaseId));

        client.setMetadataWithResponse(new Metadata(Collections.singletonMap("metadata", "value")), accessConditions)
            .subscribe(response -> System.out.printf("Set metadata completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setMetadataWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#createSnapshotWithResponse(Metadata, BlobAccessConditions)}
     */
    public void createSnapshotWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobAccessConditions
        Metadata snapshotMetadata = new Metadata(Collections.singletonMap("metadata", "value"));
        BlobAccessConditions accessConditions = new BlobAccessConditions().leaseAccessConditions(
            new LeaseAccessConditions().leaseId(leaseId));

        client.createSnapshotWithResponse(snapshotMetadata, accessConditions)
            .subscribe(response -> System.out.printf("Identifier for the snapshot is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.createSnapshotWithResponse#Metadata-BlobAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#setTierWithResponse(AccessTier, LeaseAccessConditions)}
     */
    public void setTierWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().leaseId(leaseId);

        client.setTierWithResponse(AccessTier.HOT, accessConditions)
            .subscribe(response -> System.out.printf("Set tier completed with status code %d%n",
                response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.setTierWithResponse#AccessTier-LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#undeleteWithResponse()}
     */
    public void undeleteWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.undeleteWithResponse
        client.undeleteWithResponse()
            .subscribe(response -> System.out.printf("Undelete completed with status %d%n", response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.undeleteWithResponse
    }

    /**
     * Code snippets for {@link BlobAsyncClient#acquireLeaseWithResponse(String, int, ModifiedAccessConditions)}
     */
    public void acquireLeaseWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.acquireLeaseWithResponse#String-int-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifModifiedSince(OffsetDateTime.now().minusDays(3));

        client.acquireLeaseWithResponse("proposedId", 60, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.acquireLeaseWithResponse#String-int-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#renewLeaseWithResponse(String, ModifiedAccessConditions)}
     */
    public void renewLeaseWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.renewLeaseWithResponse#String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.renewLeaseWithResponse(leaseId, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Renewed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.renewLeaseWithResponse#String-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#releaseLeaseWithResponse(String, ModifiedAccessConditions)}
     */
    public void releaseLeaseWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.releaseLeaseWithResponse#String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.releaseLeaseWithResponse(leaseId, modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Release lease completed with status %d%n",
                response.statusCode()));
        // END: com.azure.storage.blob.BlobAsyncClient.releaseLeaseWithResponse#String-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#breakLeaseWithResponse(Integer, ModifiedAccessConditions)}
     */
    public void breakLeaseWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions
        Integer retainLeaseInSeconds = 5;
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.breakLeaseWithResponse(retainLeaseInSeconds, modifiedAccessConditions)
            .subscribe(response ->
                System.out.printf("The broken lease has %d seconds remaining on the lease",
                    response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions
    }

    /**
     * Code snippets for {@link BlobAsyncClient#changeLeaseWithResponse(String, String, ModifiedAccessConditions)}
     */
    public void changeLeaseWithResponseCodeSnippets() {

        // BEGIN: com.azure.storage.blob.BlobAsyncClient.changeLeaseWithResponse#String-String-ModifiedAccessConditions
        ModifiedAccessConditions modifiedAccessConditions = new ModifiedAccessConditions()
            .ifUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.changeLeaseWithResponse(leaseId, "proposedId", modifiedAccessConditions)
            .subscribe(response -> System.out.printf("Changed lease ID is %s%n", response.value()));
        // END: com.azure.storage.blob.BlobAsyncClient.changeLeaseWithResponse#String-String-ModifiedAccessConditions
    }

    /**
     * Code snippet for {@link BlobAsyncClient#getAccountInfoWithResponse()}
     */
    public void getAccountInfoWithResponseCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response -> System.out.printf("Account Kind: %s, SKU: %s%n",
            response.value().accountKind(), response.value().skuName()));
        // END: com.azure.storage.blob.BlobAsyncClient.getAccountInfoWithResponse
    }

    /**
     * Code snippet for {@link BlobAsyncClient#generateUserDelegationSAS(UserDelegationKey, String, BlobSASPermission,
     * OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.generateUserDelegationSAS
        BlobSASPermission permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
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
        // END: com.azure.storage.blob.BlobAsyncClient.generateUserDelegationSAS
    }

    /**
     * Code snippet for {@link BlobAsyncClient#generateSAS(String, BlobSASPermission, OffsetDateTime, OffsetDateTime,
     * String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobAsyncClient.generateSAS
        BlobSASPermission permissions = new BlobSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
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
        // END: com.azure.storage.blob.BlobAsyncClient.generateSAS
    }
}
