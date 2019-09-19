// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessPolicy;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ContainerAccessConditions;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Code snippets for {@link ContainerAsyncClient}
 */
@SuppressWarnings({"unused"})
public class ContainerAsyncClientJavaDocCodeSnippets {

    private ContainerAsyncClient client = JavaDocCodeSnippetsHelpers.getContainerAsyncClient();
    private String blobName = "blobName";
    private String snapshot = "snapshot";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();

    /**
     * Code snippet for {@link ContainerAsyncClient#generateUserDelegationSAS(UserDelegationKey, String,
     * ContainerSASPermission, OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String,
     * String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.generateUserDelegationSAS#UserDelegationKey-String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        ContainerSASPermission permissions = new ContainerSASPermission()
            .setRead(true)
            .setWrite(true)
            .setCreate(true)
            .setDelete(true)
            .setAdd(true)
            .setList(true);
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
        // END: com.azure.storage.blob.ContainerAsyncClient.generateUserDelegationSAS#UserDelegationKey-String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#generateSAS(String, ContainerSASPermission, OffsetDateTime,
     * OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.generateSAS#String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
        ContainerSASPermission permissions = new ContainerSASPermission()
            .setRead(true)
            .setWrite(true)
            .setCreate(true)
            .setDelete(true)
            .setAdd(true)
            .setList(true);
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
        // END: com.azure.storage.blob.ContainerAsyncClient.generateSAS#String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getBlobAsyncClient(String)}
     */
    public void getBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getBlobAsyncClient#String
        BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient(blobName);
        // END: com.azure.storage.blob.ContainerAsyncClient.getBlobAsyncClient#String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getBlobAsyncClient(String, String)}
     */
    public void getSnapshotBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getBlobAsyncClient#String-String
        BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerAsyncClient.getBlobAsyncClient#String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAppendBlobAsyncClient(String)}
     */
    public void getAppendBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAppendBlobAsyncClient#String
        AppendBlobAsyncClient appendBlobAsyncClient = client.getAppendBlobAsyncClient(blobName);
        // END: com.azure.storage.blob.ContainerAsyncClient.getAppendBlobAsyncClient#String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAppendBlobAsyncClient(String, String)}
     */
    public void getSnapshotAppendBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAppendBlobAsyncClient#String-String
        AppendBlobAsyncClient appendBlobAsyncClient = client.getAppendBlobAsyncClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerAsyncClient.getAppendBlobAsyncClient#String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getBlockBlobAsyncClient(String)}
     */
    public void getBlockBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getBlockBlobAsyncClient#String
        BlockBlobAsyncClient blockBlobAsyncClient = client.getBlockBlobAsyncClient(blobName);
        // END: com.azure.storage.blob.ContainerAsyncClient.getBlockBlobAsyncClient#String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getBlockBlobAsyncClient(String, String)}
     */
    public void getSnapshotBlockBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getBlockBlobAsyncClient#String-String
        BlockBlobAsyncClient blockBlobAsyncClient = client.getBlockBlobAsyncClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerAsyncClient.getBlockBlobAsyncClient#String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getPageBlobAsyncClient(String)}
     */
    public void getPageBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getPageBlobAsyncClient#String
        PageBlobAsyncClient pageBlobAsyncClient = client.getPageBlobAsyncClient(blobName);
        // END: com.azure.storage.blob.ContainerAsyncClient.getPageBlobAsyncClient#String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getPageBlobAsyncClient(String, String)}
     */
    public void getSnapshotPageBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getPageBlobAsyncClient#String-String
        PageBlobAsyncClient pageBlobAsyncClient = client.getPageBlobAsyncClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerAsyncClient.getPageBlobAsyncClient#String-String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#exists()}
     */
    public void exists() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.ContainerAsyncClient.exists
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#existsWithResponse()}
     */
    public void existsWithResponse() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.ContainerAsyncClient.existsWithResponse
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#existsWithResponse(Context)}
     */
    public void existsWithResponse2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.existsWithResponse-Context
        Context context = new Context("key", "value");
        client.existsWithResponse(context).subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.ContainerAsyncClient.existsWithResponse-Context
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#create()}
     */
    public void setCreate() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.create
        client.create().subscribe(
            response -> System.out.printf("Create completed%n"),
            error -> System.out.printf("Error while creating container %s%n", error));
        // END: com.azure.storage.blob.ContainerAsyncClient.create
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#createWithResponse(Metadata, PublicAccessType)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.createWithResponse#Metadata-PublicAccessType
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));

        client.createWithResponse(metadata, PublicAccessType.CONTAINER).subscribe(response ->
            System.out.printf("Create completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.ContainerAsyncClient.createWithResponse#Metadata-PublicAccessType
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.delete
        client.delete().subscribe(
            response -> System.out.printf("Delete completed%n"),
            error -> System.out.printf("Delete failed: %s%n", error));
        // END: com.azure.storage.blob.ContainerAsyncClient.delete
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#deleteWithResponse(ContainerAccessConditions)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.deleteWithResponse#ContainerAccessConditions
        ContainerAccessConditions accessConditions = new ContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.deleteWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.ContainerAsyncClient.deleteWithResponse#ContainerAccessConditions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getBlobPublicAccess(),
                response.hasLegalHold(),
                response.hasImmutabilityPolicy()));
        // END: com.azure.storage.blob.ContainerAsyncClient.getProperties
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getPropertiesWithResponse(LeaseAccessConditions)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getPropertiesWithResponse#LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getValue().getBlobPublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.blob.ContainerAsyncClient.getPropertiesWithResponse#LeaseAccessConditions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#setMetadata(Metadata)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.setMetadata#Metadata
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));

        client.setMetadata(metadata).subscribe(
            response -> System.out.printf("Set metadata completed%n"),
            error -> System.out.printf("Set metadata failed: %s%n", error));
        // END: com.azure.storage.blob.ContainerAsyncClient.setMetadata#Metadata
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#setMetadataWithResponse(Metadata, ContainerAccessConditions)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.setMetadataWithResponse#Metadata-ContainerAccessConditions
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ContainerAccessConditions accessConditions = new ContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.setMetadataWithResponse(metadata, accessConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.ContainerAsyncClient.setMetadataWithResponse#Metadata-ContainerAccessConditions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAccessPolicy
        client.getAccessPolicy().subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getBlobAccessType());

            for (SignedIdentifier identifier : response.getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermission());
            }
        });
        // END: com.azure.storage.blob.ContainerAsyncClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAccessPolicyWithResponse(LeaseAccessConditions)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAccessPolicyWithResponse#LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.getAccessPolicyWithResponse(accessConditions).subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getValue().getBlobAccessType());

            for (SignedIdentifier identifier : response.getValue().getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermission());
            }
        });
        // END: com.azure.storage.blob.ContainerAsyncClient.getAccessPolicyWithResponse#LeaseAccessConditions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.setAccessPolicy#PublicAccessType-List
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("name")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now())
                .setExpiry(OffsetDateTime.now().plusDays(7))
                .setPermission("permissionString"));

        client.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier)).subscribe(
            response -> System.out.printf("Set access policy completed%n"),
            error -> System.out.printf("Set access policy failed: %s%n", error));
        // END: com.azure.storage.blob.ContainerAsyncClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#setAccessPolicyWithResponse(PublicAccessType, List, ContainerAccessConditions)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-ContainerAccessConditions
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("name")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now())
                .setExpiry(OffsetDateTime.now().plusDays(7))
                .setPermission("permissionString"));

        ContainerAccessConditions accessConditions = new ContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, Collections.singletonList(identifier), accessConditions)
            .subscribe(response ->
                System.out.printf("Set access policy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.ContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-ContainerAccessConditions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#listBlobsFlat()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.listBlobsFlat
        client.listBlobsFlat().subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.ContainerAsyncClient.listBlobsFlat
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#listBlobsFlat(ListBlobsOptions)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.listBlobsFlat#ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setDeletedBlobs(true)
                .setSnapshots(true));

        client.listBlobsFlat(options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.ContainerAsyncClient.listBlobsFlat#ListBlobsOptions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#listBlobsHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.listBlobsHierarchy#String
        client.listBlobsHierarchy("directoryName").subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isDeleted()));
        // END: com.azure.storage.blob.ContainerAsyncClient.listBlobsHierarchy#String
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#listBlobsHierarchy(String, ListBlobsOptions)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.listBlobsHierarchy#String-ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setDeletedBlobs(true)
                .setSnapshots(true));

        client.listBlobsHierarchy("/", options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.ContainerAsyncClient.listBlobsHierarchy#String-ListBlobsOptions
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response ->
            System.out.printf("Account Kind: %s, SKU: %s%n",
                response.getAccountKind(),
                response.getSkuName()));
        // END: com.azure.storage.blob.ContainerAsyncClient.getAccountInfo
    }

    /**
     * Code snippet for {@link ContainerAsyncClient#getAccountInfoWithResponse()}
     */
    public void getAccountInfo2() {
        // BEGIN: com.azure.storage.blob.ContainerAsyncClient.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response ->
            System.out.printf("Account Kind: %s, SKU: %s%n",
                response.getValue().getAccountKind(),
                response.getValue().getSkuName()));
        // END: com.azure.storage.blob.ContainerAsyncClient.getAccountInfoWithResponse
    }
}
