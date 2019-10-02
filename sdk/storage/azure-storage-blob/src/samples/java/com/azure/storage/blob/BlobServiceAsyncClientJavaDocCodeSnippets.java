// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.ListBlobContainersOptions;
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
     * Generates a code sample for using {@link BlobServiceAsyncClient#generateAccountSAS(AccountSASService,
     * AccountSASResourceType, AccountSASPermission, OffsetDateTime, OffsetDateTime, String, IPRange, SASProtocol)}
     */
    public void generateAccountSAS() {
        // BEGIN: com.azure.storage.blob.blobServiceAsyncClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
        AccountSASService service = new AccountSASService()
            .setBlob(true)
            .setFile(true)
            .setQueue(true)
            .setTable(true);
        AccountSASResourceType resourceType = new AccountSASResourceType()
            .setContainer(true)
            .setObject(true)
            .setService(true);
        AccountSASPermission permission = new AccountSASPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setListPermission(true)
            .setProcessMessages(true)
            .setUpdatePermission(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        String sas = client.generateAccountSAS(service, resourceType, permission, expiryTime, startTime, version,
            ipRange, sasProtocol);
        // END: com.azure.storage.blob.blobServiceAsyncClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#getBlobContainerAsyncClient(String)}
     */
    public void getContainerClient() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.getBlobContainerAsyncClient#String
        BlobContainerAsyncClient blobContainerAsyncClient = client.getBlobContainerAsyncClient("containerName");
        // END: com.azure.storage.blob.BlobServiceAsyncClient.getBlobContainerAsyncClient#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#createBlobContainer(String)}
     */
    public void createContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainer#String
        BlobContainerAsyncClient blobContainerAsyncClient =
            client.createBlobContainer("containerName").block();
        // END: com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#createBlobContainerWithResponse(String, Map, PublicAccessType)}
     */
    public void createContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerWithResponse#String-Metadata-PublicAccessType
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        BlobContainerAsyncClient containerClient =
            client.createBlobContainerWithResponse("containerName", metadata, PublicAccessType.CONTAINER).block().getValue();
        // END: com.azure.storage.blob.BlobServiceAsyncClient.createBlobContainerWithResponse#String-Metadata-PublicAccessType
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#deleteBlobContainer(String)}
     */
    public void deleteContainer() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainer#String
        client.deleteBlobContainer("containerName").subscribe(
            response -> System.out.printf("Delete container completed%n"),
            error -> System.out.printf("Delete container failed: %s%n", error));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainer#String
    }

    /**
     * Code snippet for {@link BlobServiceAsyncClient#deleteBlobContainerWithResponse(String, Context)}
     */
    public void deleteContainerWithResponse() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerWithResponse#String-Context
        Context context = new Context("Key", "Value");
        client.deleteBlobContainerWithResponse("containerName").subscribe(response ->
            System.out.printf("Delete container completed with status %d%n", response.getStatusCode()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.deleteBlobContainerWithResponse#String-Context
    }

    /**
     * Code snippets for {@link BlobServiceAsyncClient#listBlobContainers()} and
     * {@link BlobServiceAsyncClient#listBlobContainers(ListBlobContainersOptions)}
     */
    public void listContainers() {
        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers
        client.listBlobContainers().subscribe(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers

        // BEGIN: com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions
        ListBlobContainersOptions options = new ListBlobContainersOptions()
            .setPrefix("containerNamePrefixToMatch")
            .setDetails(new BlobContainerListDetails().setRetrieveMetadata(true));

        client.listBlobContainers(options).subscribe(container -> System.out.printf("Name: %s%n", container.getName()));
        // END: com.azure.storage.blob.BlobServiceAsyncClient.listBlobContainers#ListBlobContainersOptions
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
