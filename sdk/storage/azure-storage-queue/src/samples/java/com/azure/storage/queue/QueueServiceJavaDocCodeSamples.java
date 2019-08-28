// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.Utility;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link QueueServiceAsyncClient}.
 */
public class QueueServiceJavaDocCodeSamples {

    private String key1 = "key1";
    private String value1 = "val1";

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
     * Generates code sample for creating a {@link QueueServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.credential
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.credential
        return queueServiceClient;
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
     * Generates a code sample for using {@link QueueServiceClient#createQueue(String)}
     */
    public void createQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueue#string
        queueServiceClient.createQueue("myqueue");
        System.out.println("Complete creating queue.");
        // END: com.azure.storage.queue.queueServiceClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#createQueueWithResponse(String, Map, Context)}
     */
    public void createQueueMaxOverload() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-Context
        Response<QueueClient> response = queueServiceClient.createQueueWithResponse("myqueue",
            Collections.singletonMap("queue", "metadata"), new Context(key1, value1));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-Context
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
     * Generates a code sample for using {@link QueueServiceClient#deleteQueue(String)}
     */
    public void deleteQueue() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueue#string
        queueServiceClient.deleteQueue("myqueue");
        System.out.println("Complete deleting the queue.");
        // END: com.azure.storage.queue.queueServiceClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#deleteQueueWithResponse(String, Context)}
     */
    public void deleteQueueWithResponse() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-Context
        VoidResponse response = queueServiceClient.deleteQueueWithResponse("myqueue",
            new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-Context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getProperties()}
     */
    public void getProperties() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getProperties
        StorageServiceProperties properties = queueServiceClient.getProperties();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getPropertiesWithResponse(Context)}
     */
    public void getPropertiesWithResponse() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#Context
        StorageServiceProperties properties = queueServiceClient.getPropertiesWithResponse(
            new Context(key1, value1)).value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#Context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = queueServiceClient.getProperties();
        properties.cors(Collections.emptyList());

        queueServiceClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(StorageServiceProperties, Context)}
     */
    public void setPropertiesWithResponse() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#storageServiceProperties-Context
        StorageServiceProperties properties = queueServiceClient.getProperties();
        properties.cors(Collections.emptyList());
        VoidResponse response = queueServiceClient.setPropertiesWithResponse(properties, new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#storageServiceProperties-Context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(StorageServiceProperties, Context)} with metrics enabled.
     */
    public void setPropertiesWithResponseEnableMetrics() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties-Context
        StorageServiceProperties properties = queueServiceClient.getProperties();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        VoidResponse response = queueServiceClient.setPropertiesWithResponse(properties, new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties-Context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = queueServiceClient.getProperties();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        queueServiceClient.setProperties(properties);
        System.out.printf("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatistics()}
     */
    public void getStatistics() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatistics
        StorageServiceStats stats = queueServiceClient.getStatistics();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatisticsWithResponse(Context)}
     */
    public void getStatisticsWithResponse() {
        QueueServiceClient queueServiceClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#Context
        StorageServiceStats stats = queueServiceClient.getStatisticsWithResponse(new Context(key1, value1)).value();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#Context
    }
}
