// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.models.PublicAccessType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link FileSystemClient}
 */
@SuppressWarnings({"unused"})
public class FileSystemClientJavaDocCodeSamples {

    private FileSystemClient client = JavaDocCodeSnippetsHelpers.getFileSystemClient();
    private String fileName = "fileName";
    private String directoryName = "directoryName";
    private String leaseId = "leaseId";
    private String proposedId = "proposedId";
    private int leaseDuration = (int) Duration.ofSeconds(30).getSeconds();
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link FileSystemClient#getFileClient(String)}
     */
    public void getFileClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getFileClient#String
        FileClient fileClient = client.getFileClient(fileName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getFileClient#String
    }

    /**
     * Code snippet for {@link FileSystemClient#getDirectoryClient(String)}
     */
    public void getDirectoryClient() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getDirectoryClient#String
        DirectoryClient directoryClient = client.getDirectoryClient(directoryName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getDirectoryClient#String
    }

    /**
     * Generates a code sample for using {@link FileSystemClient#getFileSystemName()}
     */
    public void getFileSystemName() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getFileSystemName
        String fileSystemName = client.getFileSystemName();
        System.out.println("The name of the file system is " + fileSystemName);
        // END: com.azure.storage.file.datalake.FileSystemClient.getFileSystemName
    }

    /**
     * Code snippet for {@link FileSystemClient#create()}
     */
    public void create() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.create
        try {
            client.create();
            System.out.printf("Create completed%n");
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                System.out.printf("Can't create container. It already exists %n");
            }
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.create
    }

    /**
     * Code snippet for {@link FileSystemClient#createWithResponse(Map, PublicAccessType, Duration, Context)}
     */
    public void create2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Context context = new Context("Key", "Value");

        System.out.printf("Create completed with status %d%n",
            client.createWithResponse(metadata, PublicAccessType.CONTAINER, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.createWithResponse#Map-PublicAccessType-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#delete()}
     */
    public void setDelete() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.delete
        try {
            client.delete();
            System.out.printf("Delete completed%n");
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_NOT_FOUND)) {
                System.out.printf("Delete failed. File System was not found %n");
            }
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.delete
    }

    /**
     * Code snippet for {@link FileSystemClient#deleteWithResponse(FileSystemAccessConditions, Duration, Context)}
     */
    public void delete2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.deleteWithResponse#FileSystemAccessConditions-Duration-Context
        FileSystemAccessConditions accessConditions = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Delete completed with status %d%n", client.deleteWithResponse(
            accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.deleteWithResponse#FileSystemAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getProperties
        FileSystemProperties properties = client.getProperties();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.file.datalake.FileSystemClient.getProperties
    }

    /**
     * Code snippet for {@link FileSystemClient#getPropertiesWithResponse(LeaseAccessConditions, Duration, Context)}
     */
    public void getProperties2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
        LeaseAccessConditions accessConditions = new LeaseAccessConditions().setLeaseId(leaseId);
        Context context = new Context("Key", "Value");

        FileSystemProperties properties = client.getPropertiesWithResponse(accessConditions, timeout, context)
            .getValue();
        System.out.printf("Public Access Type: %s, Legal Hold? %b, Immutable? %b%n",
            properties.getPublicAccess(),
            properties.hasLegalHold(),
            properties.hasImmutabilityPolicy());
        // END: com.azure.storage.file.datalake.FileSystemClient.getPropertiesWithResponse#LeaseAccessConditions-Duration-Context
    }

    /**
     * Code snippet for {@link FileSystemClient#setMetadata(Map)}
     */
    public void setMetadata() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.setMetadata#Map
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        try {
            client.setMetadata(metadata);
            System.out.printf("Set metadata completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Fail while setting metadata %n");
        }
        // END: com.azure.storage.file.datalake.FileSystemClient.setMetadata#Map
    }

    /**
     * Code snippet for {@link FileSystemClient#setMetadataWithResponse(Map, FileSystemAccessConditions, Duration,
     * Context)}
     */
    public void setMetadata2() {
        // BEGIN: com.azure.storage.file.datalake.FileSystemClient.setMetadataWithResponse#Map-FileSystemAccessConditions-Duration-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        FileSystemAccessConditions accessConditions = new FileSystemAccessConditions()
            .setLeaseAccessConditions(new LeaseAccessConditions().setLeaseId(leaseId))
            .setModifiedAccessConditions(new ModifiedAccessConditions()
                .setIfUnmodifiedSince(OffsetDateTime.now().minusDays(3)));
        Context context = new Context("Key", "Value");

        System.out.printf("Set metadata completed with status %d%n",
            client.setMetadataWithResponse(metadata, accessConditions, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.FileSystemClient.setMetadataWithResponse#Map-FileSystemAccessConditions-Duration-Context
    }

}
