// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link DataLakeServiceClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeServiceClientJavaDocCodeSnippets {

    private final DataLakeServiceClient client = JavaDocCodeSnippetsHelpers.getDataLakeServiceClient();
    private final Duration timeout = Duration.ofSeconds(30);

    /**
     * Code snippet for {@link DataLakeServiceClient#getFileSystemClient(String)}
     */
    public void getFileSystemClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.getFileSystemClient#String
        DataLakeFileSystemClient dataLakeFileSystemClient = client.getFileSystemClient("fileSystemName");
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.getFileSystemClient#String
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#createFileSystem(String)}
     */
    public void createFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystem#String
        DataLakeFileSystemClient dataLakeFileSystemClient = client.createFileSystem("fileSystemName");
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystem#String
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#createFileSystemWithResponse(String, Map, PublicAccessType, Context)}
     */
    public void createFileSystemWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystemWithResponse#String-Map-PublicAccessType-Context
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        Context context = new Context("Key", "Value");

        DataLakeFileSystemClient dataLakeFileSystemClient = client.createFileSystemWithResponse(
            "fileSystemName",
            metadata,
            PublicAccessType.CONTAINER,
            context).getValue();
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystemWithResponse#String-Map-PublicAccessType-Context
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#deleteFileSystem(String)}
     */
    public void deleteFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystem#String
        try {
            client.deleteFileSystem("fileSystemName");
            System.out.printf("Delete file system completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Delete file system failed: %s%n", error);
        }
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystem#String
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#deleteFileSystemWithResponse(String, DataLakeRequestConditions,
     * Context)}
     */
    public void deleteFileSystemWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Delete file system completed with status %d%n",
            client.deleteFileSystemWithResponse("fileSystemName", new DataLakeRequestConditions(), context)
                .getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions-Context
    }

    /**
     * Code snippets for {@link DataLakeServiceClient#listFileSystems()} and
     * {@link DataLakeServiceClient#listFileSystems(ListFileSystemsOptions, Duration)}
     */
    public void listFileSystems() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems
        client.listFileSystems().forEach(fileSystem -> System.out.printf("Name: %s%n", fileSystem.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems#ListFileSystemsOptions-Duration
        ListFileSystemsOptions options = new ListFileSystemsOptions()
            .setPrefix("filSystemNamePrefixToMatch")
            .setDetails(new FileSystemListDetails().setRetrieveMetadata(true));

        client.listFileSystems(options, timeout).forEach(fileSystem -> System.out.printf("Name: %s%n",
            fileSystem.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems#ListFileSystemsOptions-Duration
    }

    /**
     * Code snippets for {@link DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)}
     * and {@link DataLakeServiceClient#getUserDelegationKeyWithResponse(OffsetDateTime, OffsetDateTime, Duration, Context)}
     */
    public void getUserDelegationKey() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        Context context = new Context("Key", "Value");
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
        System.out.printf("User delegation key: %s%n",
            client.getUserDelegationKey(delegationKeyStartTime, delegationKeyExpiryTime));
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context
        System.out.printf("User delegation key: %s%n",
            client.getUserDelegationKeyWithResponse(delegationKeyStartTime, delegationKeyExpiryTime, timeout, context));
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#generateAccountSas(AccountSasSignatureValues)}
     */
    public void generateAccountSas() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = client.generateAccountSas(sasValues);
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues
    }

}
