// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.AccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.LeaseAccessConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ModifiedAccessConditions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.SignedIdentifier;
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
     * Code snippet for {@link BlobContainerAsyncClient#deleteWithResponse(BlobContainerAccessConditions)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobContainerAccessConditions
        BlobContainerAccessConditions accessConditions = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.deleteWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Delete completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.deleteWithResponse#BlobContainerAccessConditions
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
     * Code snippet for {@link BlobContainerAsyncClient#getPropertiesWithResponse(LeaseAccessConditions)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.getPropertiesWithResponse(accessConditions).subscribe(response ->
            System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
                response.getValue().getBlobPublicAccess(),
                response.getValue().hasLegalHold(),
                response.getValue().hasImmutabilityPolicy()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getPropertiesWithResponse#LeaseAccessConditions
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
     * Code snippet for {@link BlobContainerAsyncClient#setMetadataWithResponse(Map, BlobContainerAccessConditions)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobContainerAccessConditions
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobContainerAccessConditions accessConditions = new BlobContainerAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));

        client.setMetadataWithResponse(metadata, accessConditions).subscribe(response ->
            System.out.printf("Set metadata completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setMetadataWithResponse#Map-BlobContainerAccessConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy
        client.getAccessPolicy().subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getBlobAccessType());

            for (SignedIdentifier identifier : response.getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermission());
            }
        });
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicy
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#getAccessPolicyWithResponse(LeaseAccessConditions)}
     */
    public void getAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#LeaseAccessConditions
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);

        client.getAccessPolicyWithResponse(accessConditions).subscribe(response -> {
            System.out.printf("Blob Access Type: %s%n", response.getValue().getBlobAccessType());

            for (SignedIdentifier identifier : response.getValue().getIdentifiers()) {
                System.out.printf("Identifier Name: %s, Permissions %s%n",
                    identifier.getId(),
                    identifier.getAccessPolicy().getPermission());
            }
        });
        // END: com.azure.storage.blob.BlobContainerAsyncClient.getAccessPolicyWithResponse#LeaseAccessConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setAccessPolicy(PublicAccessType, List)}
     */
    public void setAccessPolicy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List
        SignedIdentifier identifier = new SignedIdentifier()
            .setId("name")
            .setAccessPolicy(new AccessPolicy()
                .setStart(OffsetDateTime.now())
                .setExpiry(OffsetDateTime.now().plusDays(7))
                .setPermission("permissionString"));

        client.setAccessPolicy(PublicAccessType.CONTAINER, Collections.singletonList(identifier)).subscribe(
            response -> System.out.printf("Set access policy completed%n"),
            error -> System.out.printf("Set access policy failed: %s%n", error));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicy#PublicAccessType-List
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#setAccessPolicyWithResponse(PublicAccessType, List, BlobContainerAccessConditions)}
     */
    public void setAccessPolicy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions
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

        client.setAccessPolicyWithResponse(PublicAccessType.CONTAINER, Collections.singletonList(identifier), accessConditions)
            .subscribe(response ->
                System.out.printf("Set access policy completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.setAccessPolicyWithResponse#PublicAccessType-List-BlobContainerAccessConditions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsFlat()}
     */
    public void listBlobsFlat() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat
        client.listBlobsFlat().subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isPrefix()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsFlat(ListBlobsOptions)}
     */
    public void listBlobsFlat2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat#ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("prefixToMatch")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsFlat(options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsFlat#ListBlobsOptions
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsHierarchy(String)}
     */
    public void listBlobsHierarchy() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String
        client.listBlobsHierarchy("directoryName").subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b%n", blob.getName(), blob.isDeleted()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String
    }

    /**
     * Code snippet for {@link BlobContainerAsyncClient#listBlobsHierarchy(String, ListBlobsOptions)}
     */
    public void listBlobsHierarchy2() {
        // BEGIN: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String-ListBlobsOptions
        ListBlobsOptions options = new ListBlobsOptions()
            .setPrefix("directoryName")
            .setDetails(new BlobListDetails()
                .setRetrieveDeletedBlobs(true)
                .setRetrieveSnapshots(true));

        client.listBlobsHierarchy("/", options).subscribe(blob ->
            System.out.printf("Name: %s, Directory? %b, Deleted? %b, Snapshot ID: %s%n",
                blob.getName(),
                blob.isPrefix(),
                blob.isDeleted(),
                blob.getSnapshot()));
        // END: com.azure.storage.blob.BlobContainerAsyncClient.listBlobsHierarchy#String-ListBlobsOptions
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
}
