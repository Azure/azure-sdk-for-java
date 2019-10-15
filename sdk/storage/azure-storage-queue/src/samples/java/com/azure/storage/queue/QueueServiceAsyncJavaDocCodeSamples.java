// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link
 * QueueServiceAsyncClient}.
 */
public class QueueServiceAsyncJavaDocCodeSamples {

    private QueueServiceAsyncClient client = createAsyncClientWithSASToken();

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient}.
     */
    public void buildQueueServiceAsyncClient() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation
        QueueServiceAsyncClient client = new QueueServiceClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with SAS token.
     *
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken
        QueueServiceAsyncClient client = new QueueServiceClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with SAS token.
     *
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        QueueServiceAsyncClient client = new QueueServiceClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .sasToken("{SASTokenQueryParams}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@code connectionString} which turns
     * into {@link SharedKeyCredential}
     *
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceAsyncClient client = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        return client;
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueue(String)}
     */
    public void createQueueAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
        client.createQueue("myqueue").subscribe(
            response -> {
            },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueueWithResponse(String, Map)}
     */
    public void createQueueAsyncMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map
        client.createQueueWithResponse("myqueue", Collections.singletonMap("queue", "metadata"))
            .subscribe(
                response -> System.out.printf("Creating the queue with status code %d", response.getStatusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the queue!")
            );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#listQueues()}
     */
    public void listQueuesAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.listQueues
        client.listQueues().subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.getName()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#listQueues(QueuesSegmentOptions)}
     */
    public void listQueuesAsyncWithOverload() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions
        client.listQueues(new QueuesSegmentOptions().setPrefix("azure")).subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.getName(), queueItem.getMetadata()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueue(String)}
     */
    public void deleteQueueAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
        client.deleteQueue("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed.")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueueWithResponse(String)}
     */
    public void deleteQueueWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string
        client.deleteQueueWithResponse("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.getStatusCode())
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getProperties
        client.getProperties()
            .subscribe(properties -> {
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getProperties
    }


    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse
        client.getPropertiesWithResponse()
            .subscribe(response -> {
                StorageServiceProperties properties = response.getValue();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)}
     */
    public void setPropertiesAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = client.getProperties().block();
        client.setProperties(properties)
            .doOnSuccess(response -> System.out.printf("Setting Queue service properties completed."));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
    }


    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setPropertiesWithResponse(StorageServiceProperties)}
     */
    public void setPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#storageServiceProperties
        StorageServiceProperties properties = client.getProperties().block();
        client.setPropertiesWithResponse(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)} with
     * metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = client.getProperties().block();
        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);
        client.setProperties(properties).subscribe(
            response -> System.out.printf("Setting Queue service properties completed."));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setPropertiesWithResponse(StorageServiceProperties)}
     * with metrics enabled.
     */
    public void setPropertiesAsyncEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = client.getProperties().block();
        properties.getMinuteMetrics().setEnabled(true);
        properties.getHourMetrics().setEnabled(true);
        client.setPropertiesWithResponse(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.getStatusCode()));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
        client.getStatistics()
            .subscribe(stats -> {
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.getGeoReplication().getStatus(), stats.getGeoReplication().getLastSyncTime());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getStatisticsWithResponse()}
     */
    public void getStatisticsWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse
        client.getStatisticsWithResponse()
            .subscribe(response -> {
                StorageServiceStats stats = response.getValue();
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.getGeoReplication().getStatus(), stats.getGeoReplication().getLastSyncTime());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse
    }
}
