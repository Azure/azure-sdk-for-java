// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

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
    private String accountName = "accountName";
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

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
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
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
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_NOT_FOUND)) {
                System.out.printf("Delete failed. Container was not found %n");
            }
        }
        // END: com.azure.storage.blob.BlobContainerClient.delete
    }

    /**
     * Code snippet for {@link BlobContainerClient#deleteWithResponse(BlobRequestConditions, Duration, Context)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobRequestConditions-Duration-Context
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("Key", "Value");

        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(
            requestConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.deleteWithResponse#BlobRequestConditions-Duration-Context
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
     * Code snippet for {@link BlobContainerClient#getPropertiesWithResponse(String, Duration, Context)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#String-Duration-Context
        Context context = new Context("Key", "Value");

        BlobContainerProperties properties = client.getPropertiesWithResponse(leaseId, timeout, context)
            .getValue();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getBlobPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.blob.BlobContainerClient.getPropertiesWithResponse#String-Duration-Context
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
     * Code snippet for {@link BlobContainerClient#setMetadataWithResponse(Map, BlobRequestConditions, Duration,
     * Context)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));
        Context context = new Context("Key", "Value");

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(metadata, requestConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.setMetadataWithResponse#Map-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccessPolicy
        BlobContainerAccessPolicies accessPolicies = client.getAccessPolicy();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (BlobSignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermissions());
        }
        // END: com.azure.storage.blob.BlobContainerClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link BlobContainerClient#getAccessPolicyWithResponse(String, Duration, Context)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#String-Duration-Context
        Context context = new Context("Key", "Value");
        BlobContainerAccessPolicies accessPolicies = client.getAccessPolicyWithResponse(leaseId, timeout, context)
            .getValue();
        System.out.printf("Blob Access Type: %s%n", accessPolicies.getBlobAccessType());

        for (BlobSignedIdentifier identifier : accessPolicies.getIdentifiers()) {
            System.out.printf("Identifier Name: %s, Permissions %s%n",
                identifier.getId(),
                identifier.getAccessPolicy().getPermissions());
        }
        // END: com.azure.storage.blob.BlobContainerClient.getAccessPolicyWithResponse#String-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setAccessPolicy#PublicAccessType-List
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

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
     * BlobRequestConditions, Duration, Context)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions-Duration-Context
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        Context context = new Context("Key", "Value");

        System.out.printf("Set access policy completed with status %d%n",
            client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER,
                Collections.singletonList(identifier),
                requestConditions,
                timeout,
                context).getStatusCode());
        // END: com.azure.storage.blob.BlobContainerClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions-Duration-Context
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobs()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobs
        client.listBlobs().forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobs
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobs(ListBlobsOptions, Duration)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobs(options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-Duration
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobs(ListBlobsOptions, String, Duration)}
     */
    public void listBlobsFlat3() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-String-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        String continuationToken = "continuationToken";

        client.listBlobs(options, continuationToken, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobs#ListBlobsOptions-String-Duration
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsByHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String
        client.listBlobsByHierarchy("directoryName").forEach(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String
    }

    /**
     * Code snippet for {@link BlobContainerClient#listBlobsByHierarchy(String, ListBlobsOptions, Duration)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String-ListBlobsOptions-Duration
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsByHierarchy("/", options, timeout).forEach(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerClient.listBlobsByHierarchy#String-ListBlobsOptions-Duration
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

    /**
     * Code snippet for {@link BlobContainerClient#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey)}
     * and {@link BlobContainerClient#generateSas(BlobServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission permission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues

        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission myPermission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
    }

    /**
     * Code snippet for {@link BlobContainerClient#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey, String, Context)}
     * and {@link BlobContainerClient#generateSas(BlobServiceSasSignatureValues, Context)}
     */
    public void generateSasWithContext() {
        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues-Context
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission permission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        // Client must be authenticated via StorageSharedKeyCredential
        client.generateSas(values, new Context("key", "value"));
        // END: com.azure.storage.blob.BlobContainerClient.generateSas#BlobServiceSasSignatureValues-Context

        // BEGIN: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission myPermission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey, accountName, new Context("key", "value"));
        // END: com.azure.storage.blob.BlobContainerClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey-String-Context
    }
//
//    /**
//     * Code snippet for {@link BlobContainerClient#rename(String)}
//     */
//    public void renameContainer() {
//        // BEGIN: com.azure.storage.blob.BlobContainerClient.rename#String
//        BlobContainerClient blobContainerClient = client.rename("newContainerName");
//        // END: com.azure.storage.blob.BlobContainerClient.rename#String
//    }
//
//    /**
//     * Code snippet for {@link BlobContainerClient#renameWithResponse(BlobContainerRenameOptions, Duration, Context)}
//     */
//    public void renameContainerWithResponse() {
//        // BEGIN: com.azure.storage.blob.BlobContainerClient.renameWithResponse#BlobContainerRenameOptions-Duration-Context
//        BlobRequestConditions requestConditions = new BlobRequestConditions().setLeaseId("lease-id");
//        Context context = new Context("Key", "Value");
//
//        BlobContainerClient blobContainerClient = client.renameWithResponse(
//            new BlobContainerRenameOptions("newContainerName")
//                .setRequestConditions(requestConditions),
//            Duration.ofSeconds(1),
//            context).getValue();
//        // END: com.azure.storage.blob.BlobContainerClient.renameWithResponse#BlobContainerRenameOptions-Duration-Context
//    }
}
