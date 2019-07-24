// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueStorageAsyncClient;
import com.azure.storage.queue.QueueStorageClient;
import com.azure.storage.queue.QueueStorageClientBuilder;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueStorageClient} and {@link QueueStorageAsyncClient}.
 */
public class QueueStorageJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link QueueStorageClient}.
     */
    public void buildQueueStorageClient() {
        // BEGIN: com.azure.storage.queue.queueStorageClient.instantiation
        QueueStorageClient client = new QueueStorageClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.storage.queue.queueStorageClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueStorageAsyncClient}.
     */
    public void buildQueueStorageAsyncClient() {
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.instantiation
        QueueStorageAsyncClient client = new QueueStorageClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueStorageAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueStorageClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueStorageClient}
     */
    public QueueStorageClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueStorageClient.instantiation.sastoken
        QueueStorageClient queueStorageClient = new QueueStorageClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueStorageClient.instantiation.sastoken
        return queueStorageClient;
    }

    /**
     * Generates code sample for creating a {@link QueueStorageAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueStorageAsyncClient}
     */
    public QueueStorageAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.instantiation.sastoken
        QueueStorageAsyncClient queueStorageAsyncClient = new QueueStorageClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueStorageAsyncClient.instantiation.sastoken
        return queueStorageAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueStorageClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueStorageClient}
     */
    public QueueStorageClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueStorageClient.instantiation.credential
        QueueStorageClient queueStorageClient = new QueueStorageClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildClient();
        // END: com.azure.storage.queue.queueStorageClient.instantiation.credential
        return queueStorageClient;
    }

    /**
     * Generates code sample for creating a {@link QueueStorageAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueStorageAsyncClient}
     */
    public QueueStorageAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.instantiation.credential
        QueueStorageAsyncClient queueStorageAsyncClient = new QueueStorageClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueStorageAsyncClient.instantiation.credential
        return queueStorageAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueStorageClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueStorageClient}
     */
    public QueueStorageClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueStorageClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                         + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueStorageClient queueStorageClient = new QueueStorageClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueStorageClient.instantiation.connectionstring
        return queueStorageClient;
    }

    /**
     * Generates code sample for creating a {@link QueueStorageAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueStorageAsyncClient}
     */
    public QueueStorageAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                        + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueStorageAsyncClient queueStorageAsyncClient = new QueueStorageClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueStorageAsyncClient.instantiation.connectionstring
        return queueStorageAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#createQueue(String)}
     */
    public void createQueue() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.createQueue#string
        Response<QueueClient> response = queueStorageClient.createQueue("myqueue");
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueStorageClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#createQueue(String)}
     */
    public void createQueueAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.createQueue#string
        queueStorageAsyncClient.createQueue("myqueue").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueStorageAsyncClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#createQueue(String, Map)}
     */
    public void createQueueMaxOverload() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.createQueue#string-map
        Response<QueueClient> response = queueStorageClient.createQueue("myqueue",
            Collections.singletonMap("queue", "metadata"));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueStorageClient.createQueue#string-map
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#createQueue(String, Map)}
     */
    public void createQueueAsyncMaxOverload() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.createQueue#string-map
        queueStorageAsyncClient.createQueue("myqueue", Collections.singletonMap("queue", "metadata"))
            .subscribe(
                response -> System.out.printf("Creating the queue with status code %d", response.statusCode()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete creating the queue!")
            );
        // END: com.azure.storage.queue.queueStorageAsyncClient.createQueue#string-map
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#listQueues()}
     */
    public void listQueues() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.listQueues
        queueStorageClient.listQueues().forEach(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name())
        );
        // END: com.azure.storage.queue.queueStorageClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#listQueues()}
     */
    public void listQueuesAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.listQueues
        queueStorageAsyncClient.listQueues().subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueStorageAsyncClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#listQueues(QueuesSegmentOptions)} )}
     */
    public void listQueuesWithOverload() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.listQueues#queueSergmentOptions
        queueStorageClient.listQueues(new QueuesSegmentOptions().prefix("azure")).forEach(
            queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.name(), queueItem.metadata())
        );
        // END: com.azure.storage.queue.queueStorageClient.listQueues#queueSergmentOptions
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#listQueues(QueuesSegmentOptions)}
     */
    public void listQueuesAsyncWithOverload() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.listQueues#queueSergmentOptions
        queueStorageAsyncClient.listQueues(new QueuesSegmentOptions().prefix("azure")).subscribe(
            queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.name(), queueItem.metadata()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete listing the queues!")
        );
        // END: com.azure.storage.queue.queueStorageAsyncClient.listQueues#queueSergmentOptions
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#deleteQueue(String)}
     */
    public void deleteQueue() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.deleteQueue#string
        VoidResponse response = queueStorageClient.deleteQueue("myqueue");
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueStorageClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#deleteQueue(String)}
     */
    public void deleteQueueAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.deleteQueue#string
        queueStorageAsyncClient.deleteQueue("myshare").subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueStorageAsyncClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#getProperties()}
     */
    public void getProperties() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.getProperties
        StorageServiceProperties properties = queueStorageClient.getProperties().value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueStorageClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.getProperties
        queueStorageAsyncClient.getProperties()
            .subscribe(response -> {
                StorageServiceProperties properties = response.value();
                System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
                    properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
            });
        // END: com.azure.storage.queue.queueStorageAsyncClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueStorageClient.getProperties().value();
        properties.cors(Collections.emptyList());

        VoidResponse response = queueStorageClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueStorageClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#setProperties(StorageServiceProperties)}
     */
    public void setPropertiesAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueStorageAsyncClient.getProperties().block().value();
        queueStorageAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueStorageAsyncClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = queueStorageClient.getProperties().value();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        VoidResponse response = queueStorageClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueStorageClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesAsyncEnableMetrics() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = queueStorageAsyncClient.getProperties().block().value();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        queueStorageAsyncClient.setProperties(properties)
            .subscribe(response -> System.out.printf("Setting Queue service properties completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueStorageAsyncClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueStorageClient#getStatistics()}
     */
    public void getStatistics() {
        QueueStorageClient queueStorageClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageClient.getStatistics
        StorageServiceStats stats = queueStorageClient.getStatistics().value();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueStorageClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueStorageAsyncClient#getStatistics()}
     */
    public void getStatisticsAsync() {
        QueueStorageAsyncClient queueStorageAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueStorageAsyncClient.getStatistics
        queueStorageAsyncClient.getStatistics()
            .subscribe(response -> {
                StorageServiceStats stats = response.value();
                System.out.printf("Geo replication status: %s, Last synced: %s",
                    stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
            });
        // END: com.azure.storage.queue.queueStorageAsyncClient.getStatistics
    }
}
