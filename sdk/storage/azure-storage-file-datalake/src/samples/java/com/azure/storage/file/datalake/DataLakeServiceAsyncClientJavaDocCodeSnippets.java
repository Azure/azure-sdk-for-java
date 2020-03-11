// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

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
 * Code snippets for {@link DataLakeServiceAsyncClient}
 */
@SuppressWarnings({"unused"})
public class DataLakeServiceAsyncClientJavaDocCodeSnippets {
    private DataLakeServiceAsyncClient client = JavaDocCodeSnippetsHelpers.getDataLakeServiceAsyncClient();

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#getFileSystemAsyncClient(String)}
     */
    public void getFileSystemClient() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getFileSystemAsyncClient#String
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = client.getFileSystemAsyncClient("fileSystemName");
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getFileSystemAsyncClient#String
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#createFileSystem(String)}
     */
    public void createFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystem#String
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient =
            client.createFileSystem("fileSystemName").block();
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystem#String
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#createFileSystemWithResponse(String, Map, PublicAccessType)}
     */
    public void createFileSystemWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystemWithResponse#String-Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");

        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = client
            .createFileSystemWithResponse("fileSystemName", metadata, PublicAccessType.CONTAINER).block().getValue();
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.createFileSystemWithResponse#String-Map-PublicAccessType
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#deleteFileSystem(String)}
     */
    public void deleteFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystem#String
        client.deleteFileSystem("fileSystemName").subscribe(
            response -> System.out.printf("Delete file system completed%n"),
            error -> System.out.printf("Delete file system failed: %s%n", error));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystem#String
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#deleteFileSystemWithResponse(String, DataLakeRequestConditions)}
     */
    public void deleteFileSystemWithResponse() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions
        client.deleteFileSystemWithResponse("fileSystemName", new DataLakeRequestConditions()).subscribe(response ->
            System.out.printf("Delete file system completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions
    }

    /**
     * Code snippets for {@link DataLakeServiceAsyncClient#listFileSystems()} and
     * {@link DataLakeServiceAsyncClient#listFileSystems(ListFileSystemsOptions)}
     */
    public void listFileSystems() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listFileSystems
        client.listFileSystems().subscribe(fileSystem -> System.out.printf("Name: %s%n", fileSystem.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listFileSystems

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listFileSystems#ListFileSystemsOptions
        ListFileSystemsOptions options = new ListFileSystemsOptions()
            .setPrefix("fileSystemNamePrefixToMatch")
            .setDetails(new FileSystemListDetails().setRetrieveMetadata(true));

        client.listFileSystems(options).subscribe(fileSystem -> System.out.printf("Name: %s%n", fileSystem.getName()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.listFileSystems#ListFileSystemsOptions
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)}
     */
    public void getUserDelegationKey() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
        client.getUserDelegationKey(delegationKeyStartTime, delegationKeyExpiryTime).subscribe(response ->
            System.out.printf("User delegation key: %s%n", response.getValue()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#getUserDelegationKeyWithResponse(OffsetDateTime, OffsetDateTime)}
     */
    public void getUserDelegationKeyWithResponse() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime
        client.getUserDelegationKeyWithResponse(delegationKeyStartTime, delegationKeyExpiryTime).subscribe(response ->
            System.out.printf("User delegation key: %s%n", response.getValue().getValue()));
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link DataLakeServiceAsyncClient#generateAccountSas(AccountSasSignatureValues)}
     */
    public void generateAccountSas() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.generateAccountSas#AccountSasSignatureValues
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
        // END: com.azure.storage.file.datalake.DataLakeServiceAsyncClient.generateAccountSas#AccountSasSignatureValues
    }
}
