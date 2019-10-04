// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageErrorCode;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IpRange;
import com.azure.storage.common.SasProtocol;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class BlobContainerClientJavaDocCodeSnippets {

    private BlobContainerClient client = JavaDocCodeSnippetsHelpers.getContainerClient();
    private String blobName = "blobName";
    private String snapshot = "snapshot";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link BlobContainerClient#generateUserDelegationSAS(UserDelegationKey, String,
     * BlobContainerSasPermission, OffsetDateTime, OffsetDateTime, String, SasProtocol, IpRange, String, String, String,
     * String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#UserDelegationKey-String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange-String-String-String-String-String
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IpRange ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SasProtocol sasProtocol = SasProtocol.HTTPS_HTTP;
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
        // END: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#UserDelegationKey-String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link BlobContainerClient#generateSas(String, BlobContainerSasPermission, OffsetDateTime,
     * OffsetDateTime, String, SasProtocol, IpRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateSas#String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange-String-String-String-String-String
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IpRange ipRange = new IpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SasProtocol sasProtocol = SasProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSas(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange,
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.blob.BlobContainerClient.generateSas#String-BlobContainerSasPermission-OffsetDateTime-OffsetDateTime-String-SasProtocol-IpRange-String-String-String-String-String
    }

    /**
     * Code snippet for {@link BlobContainerClient#getBlobClient(String)}
     */
    public void getBlobClient() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getBlobClient#String
        BlobClient blobClient = client.getBlobClient(blobName);
        // END: com.azure.storage.blob.BlobContainerClient.getBlobClient#String
    }

    /**
     * Code snippet for {@link BlobContainerClient#getBlobClient(String, String)}
     */
    public void getSnapshotBlobClient() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getBlobClient#String-String
        BlobClient blobClient = client.getBlobClient(blobName, snapshot);
        // END: com.azure.storage.blob.BlobContainerClient.getBlobClient#String-String
    }

    /**
     * Code snippets for {@link BlobContainerClient#exists()} and {@link BlobContainerClient#existsWithResponse(Duration,
     * Context)}
     */
    public void exists() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.exists
        System.out.printf("Exists? %b%n", client.exists());
        // END: com.azure.storage.blob.BlobContainerClient.exists

        // BEGIN: com.azure.storage.blob.BlobContainerClient.existsWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Exists? %b%n", client.existsWithResponse(timeout, context).getValue());
        // END: com.azure.storage.blob.BlobContainerClient.existsWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#create()}
     */
    public void setCreate() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.create
        try {
            client.create();
            System.out.printf("Create completed%n");
        } catch (StorageException error) {
            if (error.getErrorCode().equals(StorageErrorCode.CONTAINER_ALREADY_EXISTS)) {
                System.out.printf("Can't create container. It already exists %n");
            }
        }
        // END: com.azure.storage.blob.BlobContainerClient.create
    }

    /**
     * Code snippet for {@link BlobContainerClient#createWithResponse(Map, PublicAccessType, Duration, Context)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.createWithResponse#Map-PublicAccessType-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Context context = new Context("Key", "Value");

        System.out.printf("Create completed with status %d%n",
            client.createWithResponse(metadata, PublicAccessType.CONTAINER, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.createWithResponse#Map-PublicAccessType-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.delete
        try {
            client.delete();
            System.out.printf("Delete completed%n");
        } catch (StorageException error) {
            if (error.getErrorCode().equals(StorageErrorCode.CONTAINER_NOT_FOUND)) {
                System.out.printf("Delete failed. Container was not found %n");
            }
        }
        // END: com.azure.storage.blob.BlobContainerClient.delete
    }

    /**
     * Code snippet for {@link BlobContainerClient#deleteWithResponse(BlobContainerAccessConditions, Duration, Context)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobContainerAccessConditions-Duration-Context
        BlobContainerAccessConditions accessConditions = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(
            accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getProperties
        BlobContainerProperties properties = client.getProperties();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getBlobPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.blob.BlobContainerClient.getProperties
    }

    /**
     * Code snippet for {@link BlobContainerClient#getPropertiesWithResponse(LeaseAccessConditions, Duration, Context)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("Key", "Value");

        BlobContainerProperties properties = client.getPropertiesWithResponse(accessConditions, timeout, context)
            .getValue();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getBlobPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        try {
            client.setMetadata(metadata);
            System.out.printf("Set metadata completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Fail while setting metadata %n");
        }
        // END: com.azure.storage.blob.BlobContainerClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link BlobContainerClient#setMetadataWithResponse(Map, BlobContainerAccessConditions, Duration,
     * Context)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobContainerAccessConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobContainerAccessConditions accessConditions = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(metadata, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccessPolicy
        BlobContainerAccessPolicies accessPolicies = client.getAccessPolicy();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (SignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermission());
        }
        // END: com.azure.storage.blob.BlobContainerClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccessPolicyWithResponse(LeaseAccessConditions, Duration, Context)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("Key", "Value");
        BlobContainerAccessPolicies accessPolicies = client.getAccessPolicyWithResponse(accessConditions, timeout, context)
            .getValue();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (SignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermission());
        }
        // END: com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List
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
        // END: com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link BlobContainerClient#setAccessPolicyWithResponse(PublicAccessType, List,
     * BlobContainerAccessConditions, Duration, Context)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions-Duration-Context
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("name")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now())
                .setExpiry(OffsetDateTime.now().plusDays(7))
                .setPermission("permissionString"));

        BlobContainerAccessConditions accessConditions = new BlobContainerAccessConditions()
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
        // END: com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsFlat()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsFlat
        client.listBlobsFlat().forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsFlat
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsFlat(ListBlobsOptions, Duration)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsFlat#ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsFlat(options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsFlat#ListBlobsOptions-Duration
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsHierarchy#String
        client.listBlobsHierarchy("directoryName").forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsHierarchy#String
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsHierarchy(String, ListBlobsOptions, Duration)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsHierarchy#String-ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsHierarchy("/", options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsHierarchy#String-ListBlobsOptions-Duration
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccountInfo(Duration)}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccountInfo#Duration
        StorageAccountInfo accountInfo = client.getAccountInfo(timeout);
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobContainerClient.getAccountInfo#Duration
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfo2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccountInfoWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, context).getValue();
        System.out.printf("Account Kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobContainerClient.getAccountInfoWithResponse#Duration-Context
    }

    /**
     * Generates a code sample for using {@link BlobContainerClient#getBlobContainerName()}
     */
    public void getContainerName() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getBlobContainerName
        String containerName = client.getBlobContainerName();
        System.out.println("The name of the blob is " + containerName);
        // END: com.azure.storage.blob.BlobContainerClient.getBlobContainerName
    }
}
