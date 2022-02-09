// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.datalake.models.DataLakeAnalyticsLogging;
import com.azure.storage.file.datalake.models.DataLakeMetrics;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions;

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
     * Code snippet for {@link DataLakeServiceClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.getProperties
        DataLakeServiceProperties properties = client.getProperties();

        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
            properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.getProperties

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.getPropertiesWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        properties = client.getPropertiesWithResponse(timeout, context).getValue();

        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
            properties.getHourMetrics().isEnabled(),
            properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.getPropertiesWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#setProperties(DataLakeServiceProperties)}
     */
    public void setProperties() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.setProperties#DataLakeServiceProperties
        DataLakeRetentionPolicy loggingRetentionPolicy = new DataLakeRetentionPolicy().setEnabled(true).setDays(3);
        DataLakeRetentionPolicy metricsRetentionPolicy = new DataLakeRetentionPolicy().setEnabled(true).setDays(1);

        DataLakeServiceProperties properties = new DataLakeServiceProperties()
            .setLogging(new DataLakeAnalyticsLogging()
                .setWrite(true)
                .setDelete(true)
                .setRetentionPolicy(loggingRetentionPolicy))
            .setHourMetrics(new DataLakeMetrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy))
            .setMinuteMetrics(new DataLakeMetrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy));

        try {
            client.setProperties(properties);
            System.out.printf("Setting properties completed%n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Setting properties failed: %s%n", error);
        }
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.setProperties#DataLakeServiceProperties

        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.setPropertiesWithResponse#DataLakeServiceProperties-Duration-Context
        loggingRetentionPolicy = new DataLakeRetentionPolicy().setEnabled(true).setDays(3);
        metricsRetentionPolicy = new DataLakeRetentionPolicy().setEnabled(true).setDays(1);

        properties = new DataLakeServiceProperties()
            .setLogging(new DataLakeAnalyticsLogging()
                .setWrite(true)
                .setDelete(true)
                .setRetentionPolicy(loggingRetentionPolicy))
            .setHourMetrics(new DataLakeMetrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy))
            .setMinuteMetrics(new DataLakeMetrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy));

        Context context = new Context("Key", "Value");

        System.out.printf("Setting properties completed with status %d%n",
            client.setPropertiesWithResponse(properties, timeout, context).getStatusCode());
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.setPropertiesWithResponse#DataLakeServiceProperties-Duration-Context
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
    /**
     * Code snippet for {@link DataLakeServiceClient#undeleteFileSystem(String, String)}
     */
    public void undeleteFileSystem() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystem#String-String
        ListFileSystemsOptions listFileSystemsOptions = new ListFileSystemsOptions();
        listFileSystemsOptions.getDetails().setRetrieveDeleted(true);
        client.listFileSystems(listFileSystemsOptions, null).forEach(
            deletedFileSystem -> {
                DataLakeFileSystemClient fileSystemClient = client.undeleteFileSystem(
                    deletedFileSystem.getName(), deletedFileSystem.getVersion());
            }
        );
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystem#String-String
    }

    /**
     * Code snippet for {@link DataLakeServiceClient#undeleteFileSystemWithResponse(FileSystemUndeleteOptions, Duration, Context)}
     */
    public void undeleteFileSystemWithResponse() {
        Context context = new Context("Key", "Value");
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystemWithResponse#FileSystemUndeleteOptions-Duration-Context
        ListFileSystemsOptions listFileSystemsOptions = new ListFileSystemsOptions();
        listFileSystemsOptions.getDetails().setRetrieveDeleted(true);
        client.listFileSystems(listFileSystemsOptions, null).forEach(
            deletedFileSystem -> {
                DataLakeFileSystemClient fileSystemClient = client.undeleteFileSystemWithResponse(
                    new FileSystemUndeleteOptions(deletedFileSystem.getName(), deletedFileSystem.getVersion()), timeout,
                    context).getValue();
            }
        );
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystemWithResponse#FileSystemUndeleteOptions-Duration-Context
    }

//    /**
//     * Code snippet for {@link DataLakeServiceClient#renameFileSystem(String, String)}
//     */
//    public void renameFileSystem() {
//        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystem#String-String
//        DataLakeFileSystemClient fileSystemClient = client.renameFileSystem("oldFileSystemName", "newFileSystemName");
//        // END: com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystem#String-String
//    }
//
//    /**
//     * Code snippet for {@link DataLakeServiceClient#renameFileSystemWithResponse(String, FileSystemRenameOptions, Duration, Context)}
//     */
//    public void renameFileSystemWithResponse() {
//        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystemWithResponse#String-FileSystemRenameOptions-Duration-Context
//        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId("lease-id");
//        Context context = new Context("Key", "Value");
//
//        DataLakeFileSystemClient fileSystemClient = client.renameFileSystemWithResponse("oldFileSystemName",
//            new FileSystemRenameOptions("newFileSystemName")
//                .setRequestConditions(requestConditions), Duration.ofSeconds(1), context).getValue();
//        // END: com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystemWithResponse#String-FileSystemRenameOptions-Duration-Context
//    }


    /**
     * Code snippet for {@link DataLakeServiceClient#generateAccountSas(AccountSasSignatureValues, Context)}
     */
    public void generateAccountSasWithContext() {
        // BEGIN: com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues-Context
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true);
        AccountSasService services = new AccountSasService().setBlobAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = client.generateAccountSas(sasValues, new Context("key", "value"));
        // END: com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues-Context
    }

}
