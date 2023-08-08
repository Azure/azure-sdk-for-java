// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.monitor.models.EventData;
import com.azure.resourcemanager.monitor.models.Metric;
import com.azure.resourcemanager.monitor.models.MetricCollection;
import com.azure.resourcemanager.monitor.models.MetricDefinition;
import com.azure.resourcemanager.monitor.models.MetricValue;
import com.azure.resourcemanager.monitor.models.TimeSeriesElement;
import com.azure.resourcemanager.monitor.fluent.models.MetadataValueInner;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.AccessTier;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobAnalyticsLogging;
import com.azure.storage.blob.models.BlobMetrics;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * This sample shows examples of retrieving metrics and activity logs for Storage Account.
 *  - List all metric definitions available for a storage account
 *  - Retrieve and show metrics for the past 7 days for Transactions where
 *    - Api name was 'PutBlob' and
 *    - response type was 'Success' and
 *    - Geo type was 'Primary'
 *  -  Retrieve and show all activity logs for the past 7 days for the same Storage account.
 */
public final class QueryMetricsAndActivityLogs {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws IOException {
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "saMonitor", 20);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgMonitor", 20);

        try {
            // ============================================================
            // Create a storage account

            System.out.println("Creating a Storage Account");

            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withBlobStorageAccountKind()
                    .withAccessTier(AccessTier.COOL)
                    .create();

            System.out.println("Created a Storage Account:");
            Utils.print(storageAccount);

            List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
            final String storageConnectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    storageAccount.name(),
                    storageAccountKeys.get(0).value());

            // Add some blob transaction events
            addBlobTransactions(storageConnectionString, storageAccount.manager().httpPipeline().getHttpClient());

            OffsetDateTime recordDateTime = OffsetDateTime.now();
            // get metric definitions for storage account.
            for (MetricDefinition metricDefinition : azureResourceManager.metricDefinitions().listByResource(storageAccount.id())) {
                // find metric definition for Transactions
                if (metricDefinition.name().localizedValue().equalsIgnoreCase("transactions")) {
                    // get metric records
                    MetricCollection metricCollection = metricDefinition.defineQuery()
                            .startingFrom(recordDateTime.minusDays(7))
                            .endsBefore(recordDateTime)
                            .withAggregation("Average")
                            .withInterval(Duration.ofMinutes(5))
                            .withOdataFilter("apiName eq 'PutBlob' and responseType eq 'Success' and geoType eq 'Primary'")
                            .execute();

                    System.out.println("Metrics for '" + storageAccount.id() + "':");
                    System.out.println("Namespacse: " + metricCollection.namespace());
                    System.out.println("Query time: " + metricCollection.timespan());
                    System.out.println("Time Grain: " + metricCollection.interval());
                    System.out.println("Cost: " + metricCollection.cost());

                    for (Metric metric : metricCollection.metrics()) {
                        System.out.println("\tMetric: " + metric.name().localizedValue());
                        System.out.println("\tType: " + metric.type());
                        System.out.println("\tUnit: " + metric.unit());
                        System.out.println("\tTime Series: ");
                        for (TimeSeriesElement timeElement : metric.timeseries()) {
                            System.out.println("\t\tMetadata: ");
                            for (MetadataValueInner metadata : timeElement.metadatavalues()) {
                                System.out.println("\t\t\t" + metadata.name().localizedValue() + ": " + metadata.value());
                            }
                            System.out.println("\t\tData: ");
                            for (MetricValue data : timeElement.data()) {
                                System.out.println("\t\t\t" + data.timestamp()
                                        + " : (Min) " + data.minimum()
                                        + " : (Max) " + data.maximum()
                                        + " : (Avg) " + data.average()
                                        + " : (Total) " + data.total()
                                        + " : (Count) " + data.count());
                            }
                        }
                    }
                    break;
                }
            }

            // get activity logs for the same period.
            PagedIterable<EventData> logs = azureResourceManager.activityLogs().defineQuery()
                    .startingFrom(recordDateTime.minusDays(7))
                    .endsBefore(recordDateTime)
                    .withAllPropertiesInResponse()
                    .filterByResource(storageAccount.id())
                    .execute();

            System.out.println("Activity logs for the Storage Account:");

            for (EventData event : logs) {
                if (event.eventName() != null) {
                    System.out.println("\tEvent: " + event.eventName().localizedValue());
                }
                if (event.operationName() != null) {
                    System.out.println("\tOperation: " + event.operationName().localizedValue());
                }
                System.out.println("\tCaller: " + event.caller());
                System.out.println("\tCorrelationId: " + event.correlationId());
                System.out.println("\tSubscriptionId: " + event.subscriptionId());
            }

            return true;
        } finally {
            if (azureResourceManager.resourceGroups().getByName(rgName) != null) {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } else {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addBlobTransactions(String storageConnectionString, HttpClient httpClient) throws IOException {
        // Get the script to upload
        //
        try (InputStream scriptFileAsStream = QueryMetricsAndActivityLogs.class.getResourceAsStream("/install_apache.sh")) {

            // Get the size of the stream
            //
            byte[] scriptFileBytes = IOUtils.toByteArray(scriptFileAsStream);
            ByteArrayInputStream scriptFileStream = new ByteArrayInputStream(scriptFileBytes);
            int fileSize = scriptFileBytes.length;

            // Upload the script file as block blob
            //
            BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName("scripts")
                .httpClient(httpClient)
                .buildClient();

            blobContainerClient.create();

            // Get the service properties.
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .httpClient(httpClient)
                .buildClient();
            BlobServiceProperties serviceProps = blobServiceClient.getProperties();

            // configure Storage logging and metrics
            BlobAnalyticsLogging logProps = new BlobAnalyticsLogging()
                .setRead(true)
                .setWrite(true)
                .setRetentionPolicy(new BlobRetentionPolicy()
                    .setEnabled(true)
                    .setDays(2))
                .setVersion("1.0");
            serviceProps.setLogging(logProps);

            BlobMetrics metricProps = new BlobMetrics()
                .setEnabled(true)
                .setIncludeApis(true)
                .setRetentionPolicy(new BlobRetentionPolicy()
                    .setEnabled(true)
                    .setDays(2))
                .setVersion("1.0");
            serviceProps.setHourMetrics(metricProps);
            serviceProps.setMinuteMetrics(metricProps);

            // Set the default service version to be used for anonymous requests.
            serviceProps.setDefaultServiceVersion("2015-04-05");

            // Set the service properties.
            blobServiceClient.setProperties(serviceProps);

            BlobClient blobClient = blobContainerClient.getBlobClient("install_apache.sh");
            BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();
            blockBlobClient.upload(scriptFileStream, fileSize);

            // give sometime for the infrastructure to process the records and fit into time grain.
            ResourceManagerUtils.sleep(Duration.ofMinutes(6));
        }
    }
}
