// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.ContainerListDetails;
import com.azure.storage.blob.models.ListContainersOptions;
import com.azure.storage.blob.models.Logging;
import com.azure.storage.blob.models.Metrics;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.RetentionPolicy;
import com.azure.storage.blob.models.StorageServiceProperties;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Code snippets for {@link BlobServiceAsyncClient}
 */
@SuppressWarnings({"unused"})
public class BlobServiceAsyncClientJavaDocCodeSnippets {

    private BlobServiceAsyncClient client = JavaDocCodeSnippetsHelpers.getBlobServiceAsyncClient();

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getContainerAsyncClient(String)}
     */
    public void getContainerClient() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getContainerAsyncClient#String
        ContainerAsyncClient containerAsyncClient = client.getContainerAsyncClient("containerName");
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getContainerAsyncClient#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#createContainer(String)}
     */
    public void createContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.createContainer#String
        ContainerAsyncClient containerAsyncClient =
            client.createContainer("containerName").block();
        // END: com.azure.storage.blob.BlobServiceAsyncClient.createContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#createContainerWithResponse(String, Map, PublicAccessType)}
     */
    public void createContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.createContainerWithResponse#String-Map-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");

        ContainerAsyncClient containerClient = client
            .createContainerWithResponse("containerName", metadata, PublicAccessType.CONTAINER).block().getValue();
        // END: com.azure.storage.blob.BlobServiceAsyncClient.createContainerWithResponse#String-Map-PublicAccessType
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#deleteContainer(String)}
     */
    public void deleteContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.deleteContainer#String
        client.deleteContainer("containerName").subscribe(
            response -> System.out.printf("Delete container completed%n"),
            error -> System.out.printf("Delete container failed: %s%n", error));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.deleteContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#deleteContainerWithResponse(String, Context)}
     */
    public void deleteContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.deleteContainerWithResponse#String-Context
        Context context = new Context("Key", "Value");
        client.deleteContainerWithResponse("containerName").subscribe(response ->
            System.out.printf("Delete container completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.deleteContainerWithResponse#String-Context
    }

    /**
     * Code snippets for {@link BlobServiceAsyncClient#listContainers()} and {@link
     * BlobServiceAsyncClient#listContainers(ListContainersOptions)}
     */
    public void listContainers() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.listContainers
        client.listContainers().subscribe(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.listContainers

        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.listContainers#ListContainersOptions
        ListContainersOptions options = new ListContainersOptions()
            .setPrefix("containerNamePrefixToMatch")
            .setDetails(new ContainerListDetails().setMetadata(true));

        client.listContainers(options).subscribe(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.listContainers#ListContainersOptions
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getProperties
        client.getProperties().subscribe(response ->
            System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
                response.getHourMetrics().isEnabled(),
                response.getMinuteMetrics().isEnabled()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getProperties
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse
        client.getPropertiesWithResponse().subscribe(response ->
            System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b%n",
                response.getValue().getHourMetrics().isEnabled(),
                response.getValue().getMinuteMetrics().isEnabled()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.setProperties#StorageServiceProperties
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

        client.setProperties(properties).subscribe(
            response -> System.out.printf("Setting properties completed%n"),
            error -> System.out.printf("Setting properties failed: %s%n", error));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.setProperties#StorageServiceProperties
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#setPropertiesWithResponse(StorageServiceProperties)}
     */
    public void setPropertiesWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#StorageServiceProperties
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

        client.setPropertiesWithResponse(properties).subscribe(response ->
            System.out.printf("Setting properties completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.setPropertiesWithResponse#StorageServiceProperties
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)}
     */
    public void getUserDelegationKey() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
        client.getUserDelegationKey(delegationKeyStartTime, delegationKeyExpiryTime).subscribe(response ->
            System.out.printf("User delegation key: %s%n", response.getValue()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getUserDelegationKeyWithResponse(OffsetDateTime, OffsetDateTime)}
     */
    public void getUserDelegationKeyWithResponse() {
        OffsetDateTime delegationKeyStartTime = OffsetDateTime.now();
        OffsetDateTime delegationKeyExpiryTime = OffsetDateTime.now().plusDays(7);
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime
        client.getUserDelegationKeyWithResponse(delegationKeyStartTime, delegationKeyExpiryTime).subscribe(response ->
            System.out.printf("User delegation key: %s%n", response.getValue().getValue()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime
    }

    /**
     * Code snippets for {@link BlobServiceAsyncClient#getStatistics()}
     */
    public void getStatistics() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getStatistics
        client.getStatistics().subscribe(response ->
            System.out.printf("Geo-replication status: %s%n", response.getGeoReplication().getStatus()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getStatistics
    }

    /**
     * Code snippets for {@link BlobServiceAsyncClient#getStatisticsWithResponse()}
     */
    public void getStatisticsWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse
        client.getStatisticsWithResponse().subscribe(response ->
            System.out.printf("Geo-replication status: %s%n", response.getValue().getGeoReplication().getStatus()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getStatisticsWithResponse
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getAccountInfo}
     */
    public void getAccountInfo() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo
        client.getAccountInfo().subscribe(response ->
            System.out.printf("Account kind: %s, SKU: %s%n", response.getAccountKind(), response.getSkuName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfo
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getAccountInfoWithResponse}
     */
    public void getAccountInfoWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse
        client.getAccountInfoWithResponse().subscribe(response ->
            System.out.printf("Account kind: %s, SKU: %s%n", response.getValue().getAccountKind(),
                response.getValue().getSkuName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getAccountInfoWithResponse
    }
}
