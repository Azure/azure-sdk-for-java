// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueClient;
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
public class QueueServiceJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link QueueServiceClient}.
     */
    public void buildQueueServiceClient() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation
        QueueServiceClient client = new QueueServiceClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation
    }

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
     * Generates code sample for creating a {@link QueueServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.sastoken
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.sastoken
        return queueServiceClient;
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
     * Generates code sample for creating a {@link QueueServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.credential
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.credential
        return queueServiceClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceAsyncClient}
     */
    public QueueServiceAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueServiceAsyncClient.instantiation.credential
        return queueServiceAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                         + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        return queueServiceClient;
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
     * Generates a code sample for using {@link QueueServiceClient#createQueue(String)}
     */
    public void createQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueue#string
        Response<QueueClient> response = queueServiceClient.createQueue("myqueue");
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueue#string
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
     * Generates a code sample for using {@link QueueServiceClient#createQueue(String, Map)}
     */
    public void createQueueMaxOverload() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueue#string-map
        Response<QueueClient> response = queueServiceClient.createQueue("myqueue",
            Collections.singletonMap("queue", "metadata"));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueue#string-map
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#createQueue(String, Map)}
     */
    public void createQueueAsyncMaxOverload() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string-map
        queueServiceAsyncClient.createQueue("myqueue", Collections.singletonMap("queue", "metadata"))
            .subscribe(
                response -> System.out.printf("Creating the queue with status code %d", response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the queue!")
            );
        // END: com.azure.storage.queue.queueServiceAsyncClient.createQueue#string-map
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues()}
     */
    public void listQueues() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues
        queueServiceClient.listQueues().forEach(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues
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
     * Generates a code sample for using {@link QueueServiceClient#listQueues(QueuesSegmentOptions)} )}
     */
    public void listQueuesWithOverload() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions
        queueServiceClient.listQueues(new QueuesSegmentOptions().prefix("azure")).forEach(
            queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.name(), queueItem.metadata())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions
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
     * Generates a code sample for using {@link QueueServiceClient#deleteQueue(String)}
     */
    public void deleteQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueue#string
        VoidResponse response = queueServiceClient.deleteQueue("myqueue");
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#deleteQueue(String)}
     */
    public void deleteQueueAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
        queueServiceAsyncClient.deleteQueue("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueServiceAsyncClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getProperties()}
     */
    public void getProperties() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getProperties
        StorageServiceProperties properties = queueServiceClient.getProperties().value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getProperties
        queueServiceAsyncClient.getProperties()
            .subscribe(response -> {
                StorageServiceProperties properties = response.value();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueServiceClient.getProperties().value();
        properties.cors(Collections.emptyList());

        VoidResponse response = queueServiceClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)}
     */
    public void setPropertiesAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueServiceAsyncClient.getProperties().block().value();
        queueServiceAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = queueServiceClient.getProperties().value();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        VoidResponse response = queueServiceClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesAsyncEnableMetrics() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = queueServiceAsyncClient.getProperties().block().value();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        queueServiceAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueServiceAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatistics()}
     */
    public void getStatistics() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatistics
        StorageServiceStats stats = queueServiceClient.getStatistics().value();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        QueueServiceAsyncClient queueServiceAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
        queueServiceAsyncClient.getStatistics()
            .subscribe(response -> {
                StorageServiceStats stats = response.value();
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
            });
        // END: com.azure.storage.queue.queueServiceAsyncClient.getStatistics
    }
}
