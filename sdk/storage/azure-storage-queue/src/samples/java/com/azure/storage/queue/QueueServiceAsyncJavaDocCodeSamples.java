// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueServiceAsyncClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link QueueServiceAsyncClient}.
 */
public class QueueServiceAsyncJavaDocCodeSamples {

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
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken
        QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.sastoken
        return queueServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        return queueServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.connectionstring
        return queueServiceAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueue(String)}
     */
    public void createQueueAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
        queueServiceAsyncClient.createQueue("myqueue").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueueWithResponse(String, Map)}
     */
    public void createQueueAsyncMaxOverload() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map
        queueServiceAsyncClient.createQueueWithResponse("myqueue", Collections.singletonMap("queue", "metadata"))
            .subscribe(
                response -> System.out.printf("Creating the queue with status code %d", response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the queue!")
            );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueueWithResponse#string-map
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#listQueues()}
     */
    public void listQueuesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.listQueues
        queueServiceAsyncClient.listQueues().subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#listQueues(QueuesSegmentOptions)}
     */
    public void listQueuesAsyncWithOverload() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions
        queueServiceAsyncClient.listQueues(new QueuesSegmentOptions().prefix("azure")).subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.name(), queueItem.metadata()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.listQueues#queueSergmentOptions
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueue(String)}
     */
    public void deleteQueueAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
        queueServiceAsyncClient.deleteQueue("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed.")
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueueWithResponse(String)}
     */
    public void deleteQueueWithResponse() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string
        queueServiceAsyncClient.deleteQueueWithResponse("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueueWithResponse#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getProperties
        queueServiceAsyncClient.getProperties()
            .subscribe(properties -> {
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getProperties
    }


    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getPropertiesWithResponse()}
     */
    public void getPropertiesWithResponse() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse
        queueServiceAsyncClient.getPropertiesWithResponse()
            .subscribe(response -> {
                StorageServiceProperties properties = response.value();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getPropertiesWithResponse
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)}
     */
    public void setPropertiesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueServiceAsyncClient.getProperties().block();
        queueServiceAsyncClient.setProperties(properties)
            .doOnSuccess(response -> System.out.println("Setting Queue service properties completed."));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
    }


    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setPropertiesWithResponse(StorageServiceProperties)}
     */
    public void setPropertiesWithResponse() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#storageServiceProperties
        StorageServiceProperties properties = queueServiceAsyncClient.getProperties().block();
        queueServiceAsyncClient.setPropertiesWithResponse(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponse#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
        queueServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.minuteMetrics().enabled(true);
            properties.hourMetrics().enabled(true);

            queueServiceAsyncClient.setProperties(properties).subscribe(response ->
                System.out.println("Setting Queue service properties completed."));
        });
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setPropertiesWithResponse(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesAsyncEnableMetrics() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties
        queueServiceAsyncClient.getProperties().subscribe(properties -> {
            properties.minuteMetrics().enabled(true);
            properties.hourMetrics().enabled(true);

            queueServiceAsyncClient.setPropertiesWithResponse(properties).subscribe(response ->
                System.out.printf("Setting Queue service properties completed with status code %d",
                    response.statusCode()));
        });
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
        queueServiceAsyncClient.getStatistics()
            .subscribe(stats -> {
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getStatisticsWithResponse()}
     */
    public void getStatisticsWithResponse() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse
        queueServiceAsyncClient.getStatisticsWithResponse()
            .subscribe(response -> {
                StorageServiceStats stats = response.value();
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getStatisticsWithResponse
    }
}
