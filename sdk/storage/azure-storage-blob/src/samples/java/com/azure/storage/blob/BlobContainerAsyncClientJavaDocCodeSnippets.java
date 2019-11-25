// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Code snippets for {@link BlobContainerAsyncClient}
 */
@SuppressWarnings({"unused"})
public class BlobContainerAsyncClientJavaDocCodeSnippets {

    private BlobContainerAsyncClient client = JavaDocCodeSnippetsHelpers.getContainerAsyncClient();
    private String blobName = "blobName";
    private String snapshot = "snapshot";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();
    private UserDelegationKey userDelegationKey = JavaDocCodeSnippetsHelpers.getUserDelegationKey();

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getBlobAsyncClient(String)}
     */
    public void getBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String
        BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient(blobName);
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getBlobAsyncClient(String, String)}
     */
    public void getSnapshotBlobAsyncClient() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String-String
        BlobAsyncClient blobAsyncClient = client.getBlobAsyncClient(blobName, snapshot);
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getBlobAsyncClient#String-String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#exists()}
     */
    public void exists() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.exists
        client.exists().subscribe(response -> System.out.printf("Exists? %b%n", response));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.exists
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#existsWithResponse()}
     */
    public void existsWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse
        client.existsWithResponse().subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#existsWithResponse(Context)}
     */
    public void existsWithResponse2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse-Context
        Context context = new Context("key", "value");
        client.existsWithResponse(context).subscribe(response -> System.out.printf("Exists? %b%n", response.getValue()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.existsWithResponse-Context
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#create()}
     */
    public void setCreate() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.create
        client.create().subscribe(
            response -> System.out.printf("Create completed%n"),
            error -> System.out.printf("Error while creating container %s%n", error));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.create
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#createWithResponse(Map, PublicAccessType)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.createWithResponse#Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.createWithResponse(metadata, PublicAccessType.CONTAINER).subscribe(response ->
            System.out.printf("Create completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.createWithResponse#Map-PublicAccessType
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.delete
        client.delete().subscribe(
            response -> System.out.printf("Delete completed%n"),
            error -> System.out.printf("Delete failed: %s%n", error));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.delete
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#deleteWithResponse(BlobRequestConditions)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobRequestConditions
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.deleteWithResponse(requestConditions).subscribe(response ->
            System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getBlobPublicAccess(),
                response.hasLegalHold(),
                response.hasImmutabilityPolicy()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getProperties
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getPropertiesWithResponse(String)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#String
        client.getPropertiesWithResponse(leaseId).subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getValue().getBlobPublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        client.setMetadata(metadata).subscribe(
            response -> System.out.printf("Set metadata completed%n"),
            error -> System.out.printf("Set metadata failed: %s%n", error));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setMetadataWithResponse(Map, BlobRequestConditions)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobRequestConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.setMetadataWithResponse(metadata, requestConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy
        client.getAccessPolicy().subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getBlobAccessType());

            for (BlobSignedIdentifier identifier : response.getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermissions());
            }
        });
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccessPolicyWithResponse(String)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#String
        client.getAccessPolicyWithResponse(leaseId).subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getValue().getBlobAccessType());

            for (BlobSignedIdentifier identifier : response.getValue().getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermissions());
            }
        });
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

        client.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier)).subscribe(
            response -> System.out.printf("Set access policy completed%n"),
            error -> System.out.printf("Set access policy failed: %s%n", error));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setAccessPolicyWithResponse(PublicAccessType, List, BlobRequestConditions)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("name")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(OffsetDateTime.now())
                .setExpiresOn(OffsetDateTime.now().plusDays(7))
                .setPermissions("permissionString"));

        BlobRequestConditions requestConditions = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3));

        client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, Collections.singletonList(identifier), requestConditions)
            .subscribe(response ->
                System.out.printf("Set access policy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobRequestConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobs()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobs
        client.listBlobs().subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobs
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobs(ListBlobsOptions)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobs(options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobs#ListBlobsOptions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsByHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String
        client.listBlobsByHierarchy("directoryName").subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isDeleted()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsByHierarchy(String, ListBlobsOptions)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String-ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsByHierarchy("/", options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsByHierarchy#String-ListBlobsOptions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccountInfo()}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response ->
            System.out.printf("Account Kind: %s, SKU: %s%n",
                response.getAccountKind(),
                response.getSkuName()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccountInfoWithResponse()}
     */
    public void getAccountInfo2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response ->
            System.out.printf("Account Kind: %s, SKU: %s%n",
                response.getValue().getAccountKind(),
                response.getValue().getSkuName()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccountInfoWithResponse
    }

    /**
     * Generates a code sample for using {@link BlobContainerAsyncClient#getBlobContainerName()}
     */
    public void getContainerName() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName
        String containerName = client.getBlobContainerName();
        System.out.println("The name of the blob is " + containerName);
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getBlobContainerName
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#generateUserDelegationSas(BlobServiceSasSignatureValues, UserDelegationKey)}
     * and {@link BlobContainerAsyncClient#generateSas(BlobServiceSasSignatureValues)}
     */
    public void generateSas() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission permission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateSas(values); // Client must be authenticated via StorageSharedKeyCredential
        // END: com.azure.storage.blob.BlobContainerAsyncClient.generateSas#BlobServiceSasSignatureValues

        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
        OffsetDateTime myExpiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission myPermission = new BlobContainerSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues myValues = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        client.generateUserDelegationSas(values, userDelegationKey);
        // END: com.azure.storage.blob.BlobContainerAsyncClient.generateUserDelegationSas#BlobServiceSasSignatureValues-UserDelegationKey
    }
}
