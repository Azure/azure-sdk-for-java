// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;

import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import com.azure.storage.blob.models.ContainerListDetails;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Logging;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.Metrics;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.RetentionPolicy;
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.StorageServiceProperties;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Code snippets for {@link BlobServiceClient}
 */
@SuppressWarnings({"unused"})
public class BlobServiceClientJavaDocCodeSnippets {
    private final BlobServiceClient client = JavaDocCodeSnippetsHelpers.getBlobServiceClient();
    private final Duration timeout = Duration.ofSeconds(30);

    /**
     * Generates a code sample for using {@link BlobServiceClient#generateAccountSAS(AccountSASService,
     * AccountSASResourceType, AccountSASPermission, OffsetDateTime, OffsetDateTime, String, IPRange, SASProtocol)}
     */
    public void generateAccountSAS() {
        // BEGIN: com.azure.storage.blob.blobServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
        AccountSASService service = new AccountSASService()
            .blob(true)
            .file(true)
            .queue(true)
            .table(true);
        AccountSASResourceType resourceType = new AccountSASResourceType()
            .container(true)
            .object(true)
            .service(true);
        AccountSASPermission permission = new AccountSASPermission()
            .setRead(true)
            .setAdd(true)
            .setCreate(true)
            .setWrite(true)
            .setDelete(true)
            .setList(true)
            .setProcessMessages(true)
            .setUpdate(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        String sas = client.generateAccountSAS(service, resourceType, permission, expiryTime, startTime, version,
            ipRange, sasProtocol);
        // END: com.azure.storage.blob.blobServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
    }

    /**
     * Code snippet for {@link BlobServiceClient#getContainerClient(String)}
     */
    public void getContainerClient() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getContainerClient#String
        ContainerClient containerClient = client.getContainerClient("containerName");
        // END: com.azure.storage.blob.BlobServiceClient.getContainerClient#String
    }

    /**
     * Code snippet for {@link BlobServiceClient#createContainer(String)}
     */
    public void createContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.createContainer#String
        ContainerClient containerClient = client.createContainer("containerName");
        // END: com.azure.storage.blob.BlobServiceClient.createContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceClient#createContainerWithResponse(String, Metadata, PublicAccessType, Context)}
     */
    public void createContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.createContainerWithResponse#String-Metadata-PublicAccessType-Context
        Metadata metadata = new Metadata(Collections.singletonMap("metadata", "value"));
        Context context = new Context("Key", "Value");

        ContainerClient containerClient = client.createContainerWithResponse(
            "containerName",
            metadata,
            PublicAccessType.CONTAINER,
            context).value();
        // END: com.azure.storage.blob.BlobServiceClient.createContainerWithResponse#String-Metadata-PublicAccessType-Context
    }

    /**
     * Code snippet for {@link BlobServiceClient#deleteContainer(String)}
     */
    public void deleteContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.deleteContainer#String
        try {
            client.deleteContainer("container Name");
            System.out.printf("Delete container completed with status %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Delete container failed: %s%n", error);
        }
        // END: com.azure.storage.blob.BlobServiceClient.deleteContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceClient#deleteContainerWithResponse(String, Context)}
     */
    public void deleteContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.deleteContainerWithResponse#String-Context
        Context context = new Context("Key", "Value");
        System.out.printf("Delete container completed with status %d%n",
            client.deleteContainerWithResponse("containerName", context).statusCode());
        // END: com.azure.storage.blob.BlobServiceClient.deleteContainerWithResponse#String-Context
    }

    /**
     * Code snippets for {@link BlobServiceClient#listContainers()} and
     * {@link BlobServiceClient#listContainers(ListContainersOptions, Duration)}
     */
    public void listContainers() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.listContainers
        client.listContainers().forEach(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceClient.listContainers

        // BEGIN: com.azure.storage.blob.BlobServiceClient.listContainers#ListContainersOptions-Duration
        ListContainersOptions options = new ListContainersOptions()
            .setPrefix("containerNamePrefixToMatch")
            .setDetails(new ContainerListDetails().setMetadata(true));

        client.listContainers(options, timeout).forEach(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceClient.listContainers#ListContainersOptions-Duration
    }

    /**
     * Code snippet for {@link BlobServiceClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getProperties
        StorageServiceProperties properties = client.getProperties();

        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
            properties.getHourMetrics().getEnabled(),
            properties.getMinuteMetrics().getEnabled());
        // END: com.azure.storage.blob.BlobServiceClient.getProperties
    }

    /**
     * Code snippet for {@link BlobServiceClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context
        Context context = new Context("Key", "Value");
        StorageServiceProperties properties = client.getPropertiesWithResponse(timeout, context).value();

        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
            properties.getHourMetrics().getEnabled(),
            properties.getMinuteMetrics().getEnabled());
        // END: com.azure.storage.blob.BlobServiceClient.getPropertiesWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobServiceClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.setProperties#StorageServiceProperties
        RetentionPolicy loggingRetentionPolicy = new RetentionPolicy().setEnabled(true).setDays(3);
        RetentionPolicy metricsRetentionPolicy = new RetentionPolicy().setEnabled(true).setDays(1);

        StorageServiceProperties properties = new StorageServiceProperties()
            .setLogging(new Logging()
                .setWrite(true)
                .setDelete(true)
                .setRetentionPolicy(loggingRetentionPolicy))
            .setHourMetrics(new Metrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy))
            .setMinuteMetrics(new Metrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy));

        try {
            client.setProperties(properties);
            System.out.printf("Setting properties completed%n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Setting properties failed: %s%n", error);
        }
        // END: com.azure.storage.blob.BlobServiceClient.setProperties#StorageServiceProperties
    }

    /**
     * Code snippet for {@link BlobServiceClient#setPropertiesWithResponse(StorageServiceProperties, Duration, Context)}
     */
    public void setPropertiesWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#StorageServiceProperties-Duration-Context
        RetentionPolicy loggingRetentionPolicy = new RetentionPolicy().setEnabled(true).setDays(3);
        RetentionPolicy metricsRetentionPolicy = new RetentionPolicy().setEnabled(true).setDays(1);

        StorageServiceProperties properties = new StorageServiceProperties()
            .setLogging(new Logging()
                .setWrite(true)
                .setDelete(true)
                .setRetentionPolicy(loggingRetentionPolicy))
            .setHourMetrics(new Metrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy))
            .setMinuteMetrics(new Metrics()
                .setEnabled(true)
                .setRetentionPolicy(metricsRetentionPolicy));

        Context context = new Context("Key", "Value");

        System.out.printf("Setting properties completed with status %d%n",
            client.setPropertiesWithResponse(properties, timeout, context).statusCode());
        // END: com.azure.storage.blob.BlobServiceClient.setPropertiesWithResponse#StorageServiceProperties-Duration-Context
    }

    /**
     * Code snippets for {@link BlobServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)}
     * and {@link BlobServiceClient#getUserDelegationKeyWithResponse(OffsetDateTime, OffsetDateTime, Duration, Context)}
     */
    public void getUserDelegationKey() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        Context context = new Context("Key", "Value");
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
        System.out.printf("User delegation key: %s%n",
            client.getUserDelegationKey(delegationKeyStartTime, delegationKeyExpiryTime));
        // END: com.azure.storage.blob.BlobServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime

        // BEGIN: com.azure.storage.blob.BlobServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context
        System.out.printf("User delegation key: %s%n",
            client.getUserDelegationKeyWithResponse(delegationKeyStartTime, delegationKeyExpiryTime, timeout, context));
        // END: com.azure.storage.blob.BlobServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context
    }

    /**
     * Code snippets for {@link BlobServiceClient#getStatistics()} and {@link BlobServiceClient#getStatisticsWithResponse(Duration, Context)}
     */
    public void getStatistics() {
        Context context = new Context("Key", "Value");
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getStatistics
        System.out.printf("Geo-replication status: %s%n",
            client.getStatistics().getGeoReplication().getStatus());
        // END: com.azure.storage.blob.BlobServiceClient.getStatistics

        // BEGIN: com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context
        System.out.printf("Geo-replication status: %s%n",
            client.getStatisticsWithResponse(timeout, context).value().getGeoReplication().getStatus());
        // END: com.azure.storage.blob.BlobServiceClient.getStatisticsWithResponse#Duration-Context
    }

    /**
     * Code snippet for {@link BlobServiceClient#getAccountInfo}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getAccountInfo
        StorageAccountInfo accountInfo = client.getAccountInfo();

        System.out.printf("Account kind: %s, SKU: %s%n", accountInfo.getAccountKind(), accountInfo.getSkuName());
        // END: com.azure.storage.blob.BlobServiceClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobServiceClient#getAccountInfoWithResponse(Duration, Context)}
     */
    public void getAccountInfoWithResponse() {
        Context context = new Context("Key", "Value");
        // BEGIN: com.azure.storage.blob.BlobServiceClient.getAccountInfoWithResponse#Duration-Context
        StorageAccountInfo accountInfo = client.getAccountInfoWithResponse(timeout, context).value();
        // END: com.azure.storage.blob.BlobServiceClient.getAccountInfoWithResponse#Duration-Context
    }
}
