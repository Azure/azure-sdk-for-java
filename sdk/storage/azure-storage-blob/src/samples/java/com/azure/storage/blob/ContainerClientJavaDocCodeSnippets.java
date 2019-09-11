// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessPolicy;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ContainerAccessConditions;
import com.azure.storage.blob.models.ContainerAccessPolicies;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageErrorCode;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused"})
public class ContainerClientJavaDocCodeSnippets {

    private ContainerClient client = JavaDocCodeSnippetsHelpers.getContainerClient();
    private String blobName = "blobName";
    private String snapshot = "snapshot";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link ContainerClient#generateUserDelegationSAS(UserDelegationKey, String,
     * ContainerSASPermission, OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String,
     * String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerClient.generateUserDelegationSAS#UserDelegationKey-String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
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
        // END: com.azure.storage.blob.ContainerClient.generateUserDelegationSAS#UserDelegationKey-String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link ContainerClient#generateSAS(String, ContainerSASPermission, OffsetDateTime,
     * OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerClient.generateSAS#String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
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
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange,
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.blob.ContainerClient.generateSAS#String-ContainerSASPermission-OffsetDateTime-OffsetDateTime-String-SASProtocol-IPRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link ContainerClient#getBlobClient(String)}
     */
    public void getBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getBlobClient#String
        BlobClient blobClient = client.getBlobClient(blobName);
        // END: com.azure.storage.blob.ContainerClient.getBlobClient#String
    }

    /**
     * Code snippet for {@link ContainerClient#getBlobClient(String, String)}
     */
    public void getSnapshotBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getBlobClient#String-String
        BlobClient blobClient = client.getBlobClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerClient.getBlobClient#String-String
    }

    /**
     * Code snippet for {@link ContainerClient#getAppendBlobClient(String)}
     */
    public void getAppendBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAppendBlobClient#String
        AppendBlobClient appendBlobClient = client.getAppendBlobClient(blobName);
        // END: com.azure.storage.blob.ContainerClient.getAppendBlobClient#String
    }

    /**
     * Code snippet for {@link ContainerClient#getAppendBlobClient(String, String)}
     */
    public void getSnapshotAppendBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAppendBlobClient#String-String
        AppendBlobClient appendBlobClient = client.getAppendBlobClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerClient.getAppendBlobClient#String-String
    }

    /**
     * Code snippet for {@link ContainerClient#getBlockBlobClient(String)}
     */
    public void getBlockBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getBlockBlobClient#String
        BlockBlobClient blockBlobClient = client.getBlockBlobClient(blobName);
        // END: com.azure.storage.blob.ContainerClient.getBlockBlobClient#String
    }

    /**
     * Code snippet for {@link ContainerClient#getBlockBlobClient(String, String)}
     */
    public void getSnapshotBlockBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getBlockBlobClient#String-String
        BlockBlobClient blockBlobClient = client.getBlockBlobClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerClient.getBlockBlobClient#String-String
    }

    /**
     * Code snippet for {@link ContainerClient#getPageBlobClient(String)}
     */
    public void getPageBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getPageBlobClient#String
        PageBlobClient pageBlobClient = client.getPageBlobClient(blobName);
        // END: com.azure.storage.blob.ContainerClient.getPageBlobClient#String
    }

    /**
     * Code snippet for {@link ContainerClient#getPageBlobClient(String, String)}
     */
    public void getSnapshotPageBlobClient() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getPageBlobClient#String-String
        PageBlobClient pageBlobClient = client.getPageBlobClient(blobName, snapshot);
        // END: com.azure.storage.blob.ContainerClient.getPageBlobClient#String-String
    }

    /**
     * Code snippets for {@link ContainerClient#exists()} and {@link ContainerClient#existsWithResponse(Duration,
     * Context)}
     */
    public void exists() {
        // BEGIN: com.azure.storage.blob.ContainerClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.ContainerClient.exists

        // BEGIN: com.azure.storage.blob.ContainerClient.existsWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, context).getValue());
        // END: com.azure.storage.blob.ContainerClient.existsWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#create()}
     */
    public void setCreate() {
        // BEGIN: com.azure.storage.blob.ContainerClient.create
        try {
            client.create();
            System.out.printf("Create completed%n");
        } catch (StorageException error) {
            if (error.getErrorCode().equals(StorageErrorCode.CONTAINER_ALREADY_EXISTS)) {
                System.out.printf("Can't create container. It already exists %n");
            }
        }
        // END: com.azure.storage.blob.ContainerClient.create
    }

    /**
     * Code snippet for {@link ContainerClient#createWithResponse(Metadata, PublicAccessType, Duration, Context)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.createWithResponse#Metadata-PublicAccessType-Duration-Context
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        Context context = new Context("Key", "Value");

        System.out.printf("Create completed with status %d%n",
            client.createWithResponse(metadata, PublicAccessType.CONTAINER, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.ContainerClient.createWithResponse#Metadata-PublicAccessType-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#setDelete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.ContainerClient.delete
        try {
            client.setDelete();
            System.out.printf("Delete completed%n");
        } catch (StorageException error) {
            if (error.getErrorCode().equals(StorageErrorCode.CONTAINER_NOT_FOUND)) {
                System.out.printf("Delete failed. Container was not found %n");
            }
        }
        // END: com.azure.storage.blob.ContainerClient.delete
    }

    /**
     * Code snippet for {@link ContainerClient#deleteWithResponse(ContainerAccessConditions, Duration, Context)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.deleteWithResponse#ContainerAccessConditions-Duration-Context
        ContainerAccessConditions accessConditions = new ContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(
            accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.ContainerClient.deleteWithResponse#ContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getProperties
        ContainerProperties properties = client.getProperties();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getBlobPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.blob.ContainerClient.getProperties
    }

    /**
     * Code snippet for {@link ContainerClient#getPropertiesWithResponse(LeaseAccessConditions, Duration, Context)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("Key", "Value");

        ContainerProperties properties = client.getPropertiesWithResponse(accessConditions, timeout, context)
            .getValue();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getBlobPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.blob.ContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#setMetadata(Metadata)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.ContainerClient.setMetadata#Metadata
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));

        try {
            client.setMetadata(metadata);
            System.out.printf("Set metadata completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Fail while setting metadata %n");
        }
        // END: com.azure.storage.blob.ContainerClient.setMetadata#Metadata
    }

    /**
     * Code snippet for {@link ContainerClient#setMetadataWithResponse(Metadata, ContainerAccessConditions, Duration,
     * Context)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.setMetadataWithResponse#Metadata-ContainerAccessConditions-Duration-Context
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        ContainerAccessConditions accessConditions = new ContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(metadata, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.ContainerClient.setMetadataWithResponse#Metadata-ContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAccessPolicy
        ContainerAccessPolicies accessPolicies = client.getAccessPolicy();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (SignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermission());
        }
        // END: com.azure.storage.blob.ContainerClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link ContainerClient#getAccessPolicyWithResponse(LeaseAccessConditions, Duration, Context)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("Key", "Value");
        ContainerAccessPolicies accessPolicies = client.getAccessPolicyWithResponse(accessConditions, timeout, context)
            .getValue();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (SignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermission());
        }
        // END: com.azure.storage.blob.ContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.ContainerClient.setAccessPolicy#PublicAccessType-List
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("name")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now())
                .setExpiry(OffsetDateTime.now().plusDays(7))
                .setPermission("permissionString"));

        try {
            client.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier));
            System.out.printf("Set Access Policy completed %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Set Access Policy completed %s%n", error);
        }
        // END: com.azure.storage.blob.ContainerClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link ContainerClient#setAccessPolicyWithResponse(PublicAccessType, List,
     * ContainerAccessConditions, Duration, Context)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-ContainerAccessConditions-Duration-Context
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

        Context context = new Context("Key", "Value");

        System.out.printf("Set access policy completed with status %d%n",
            client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER,
                Collections.singletonList(identifier),
                accessConditions,
                timeout,
                context).getStatusCode());
        // END: com.azure.storage.blob.ContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-ContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#listBlobsFlat()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.ContainerClient.listBlobsFlat
        client.listBlobsFlat().forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.ContainerClient.listBlobsFlat
    }

    /**
     * Code snippet for {@link ContainerClient#listBlobsFlat(ListBlobsOptions, Duration)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.listBlobsFlat#ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setDeletedBlobs(true)
                .setSnapshots(true));

        client.listBlobsFlat(options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.ContainerClient.listBlobsFlat#ListBlobsOptions-Duration
    }

    /**
     * Code snippet for {@link ContainerClient#listBlobsHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String
        client.listBlobsHierarchy("directoryName").forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String
    }

    /**
     * Code snippet for {@link ContainerClient#listBlobsHierarchy(String, ListBlobsOptions, Duration)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String-ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setDeletedBlobs(true)
                .setSnapshots(true));

        client.listBlobsHierarchy("/", options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.ContainerClient.listBlobsHierarchy#String-ListBlobsOptions-Duration
    }

    /**
     * Code snippet for {@link ContainerClient#acquireLease(String, int)}
     */
    public void acquireLease() {
        // BEGIN: com.azure.storage.blob.ContainerClient.acquireLease#String-int
        System.out.printf("Lease ID: %s%n", client.acquireLease(proposedId, leaseDuration));
        // END: com.azure.storage.blob.ContainerClient.acquireLease#String-int
    }

    /**
     * Code snippet for {@link ContainerClient#acquireLeaseWithResponse(String, int, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void acquireLease2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.acquireLeaseWithResponse#String-int-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Lease ID: %s%n",
            client.acquireLeaseWithResponse(proposedId, leaseDuration, accessConditions, timeout, context).getValue());
        // END: com.azure.storage.blob.ContainerClient.acquireLeaseWithResponse#String-int-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#renewLease(String)}
     */
    public void renewLease() {
        // BEGIN: com.azure.storage.blob.ContainerClient.renewLease#String
        System.out.printf("Renewed Lease ID: %s%n", client.renewLease(leaseId));
        // END: com.azure.storage.blob.ContainerClient.renewLease#String
    }

    /**
     * Code snippet for {@link ContainerClient#renewLease(String, ModifiedAccessConditions, Duration)}
     */
    public void renewLease2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.renewLease#String-ModifiedAccessConditions-Duration
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        System.out.printf("Renewed Lease ID: %s%n", client.renewLease(leaseId, accessConditions, timeout));
        // END: com.azure.storage.blob.ContainerClient.renewLease#String-ModifiedAccessConditions-Duration
    }

    /**
     * Code snippet for {@link ContainerClient#renewLeaseWithResponse(String, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void renewLease3() {
        // BEGIN: com.azure.storage.blob.ContainerClient.renewLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Renewed Lease ID: %s%n",
            client.renewLeaseWithResponse(leaseId, accessConditions, timeout, context));
        // END: com.azure.storage.blob.ContainerClient.renewLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#releaseLease(String)}
     */
    public void releaseLease() {
        // BEGIN: com.azure.storage.blob.ContainerClient.releaseLease#String
        try {
            client.releaseLease(leaseId);
            System.out.printf("Release lease completed %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Release lease completed %s%n", error);
        }
        // END: com.azure.storage.blob.ContainerClient.releaseLease#String
    }

    /**
     * Code snippet for {@link ContainerClient#releaseLeaseWithResponse(String, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void releaseLease2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.releaseLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Release lease completed with status %d%n",
            client.releaseLeaseWithResponse(leaseId, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.ContainerClient.releaseLeaseWithResponse#String-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#breakLease()}
     */
    public void breakLease() {
        // BEGIN: com.azure.storage.blob.ContainerClient.breakLease
        System.out.printf("Broken lease had %d seconds remaining on the lease%n",
            client.breakLease().getSeconds());
        // END: com.azure.storage.blob.ContainerClient.breakLease
    }

    /**
     * Code snippet for {@link ContainerClient#breakLeaseWithResponse(Integer, ModifiedAccessConditions, Duration,
     * Context)}
     */
    public void breakLease2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Broken lease had %d seconds remaining on the lease%n",
            client.breakLeaseWithResponse(10, accessConditions, timeout, context).getValue().getSeconds());
        // END: com.azure.storage.blob.ContainerClient.breakLeaseWithResponse#Integer-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#changeLease(String, String)}
     */
    public void changeLease() {
        // BEGIN: com.azure.storage.blob.ContainerClient.changeLease#String-String
        System.out.printf("Changed Lease ID: %s%n", client.changeLease(leaseId, proposedId));
        // END: com.azure.storage.blob.ContainerClient.changeLease#String-String
    }

    /**
     * Code snippet for {@link ContainerClient#changeLeaseWithResponse(String, String, ModifiedAccessConditions,
     * Duration, Context)}
     */
    public void changeLease2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.changeLeaseWithResponse#String-String-ModifiedAccessConditions-Duration-Context
        ModifiedAccessConditions accessConditions = new ModifiedAccessConditions()
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Changed Lease ID: %s%n",
            client.changeLeaseWithResponse(leaseId, proposedId, accessConditions, timeout, context).getValue());
        // END: com.azure.storage.blob.ContainerClient.changeLeaseWithResponse#String-String-ModifiedAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link ContainerClient#getAccountInfo(Duration)}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAccountInfo#Duration
        StorageAccountInfo accountInfo = client.getAccountInfo(timeout);
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.ContainerClient.getAccountInfo#Duration
    }

    /**
     * Code snippet for {@link ContainerClient#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfo2() {
        // BEGIN: com.azure.storage.blob.ContainerClient.getAccountInfoWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, context).getValue();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.ContainerClient.getAccountInfoWithResponse#Duration-Context
    }
}
