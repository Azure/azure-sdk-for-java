// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;
import com.azure.storage.common.Utility;
import com.azure.core.util.Context;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.models.QueuesSegmentOptions;
import com.azure.storage.queue.models.StorageServiceProperties;
import com.azure.storage.queue.models.StorageServiceStats;

import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link QueueServiceAsyncClient}.
 */
public class QueueServiceJavaDocCodeSamples {

    private QueueServiceClient client = createClientWithSASToken();
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
        QueueServiceClient client = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.sastoken
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.credential
        QueueServiceClient client = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .credential(SASTokenCredential.fromQueryParameters(Utility.parseQueryString("{SASTokenQueryParams}")))
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                         + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueServiceClient client = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.connectionstring
        return client;
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#createQueue(String)}
     */
    public void createQueue() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueue#string
        client.createQueue("myqueue");
        System.out.println("Complete creating queue.");
        // END: com.azure.storage.queue.queueServiceClient.createQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#createQueueWithResponse(String, Map, Duration,
     * Context)}
     */
    public void createQueueMaxOverload() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-duration-context
        Response<QueueClient> response = client.createQueueWithResponse("myqueue",
            Collections.singletonMap("queue", "metadata"), Duration.ofSeconds(1), new Context(key1, value1));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues()}
     */
    public void listQueues() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues
        client.listQueues().forEach(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.name())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues(QueuesSegmentOptions, Duration, Context)} )}
     */
    public void listQueuesWithOverload() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions-duration-context
        client.listQueues(new QueuesSegmentOptions().prefix("azure"), Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.name(), queueItem.metadata())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#deleteQueue(String)}
     */
    public void deleteQueue() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueue#string
        client.deleteQueue("myqueue");
        System.out.println("Complete deleting the queue.");
        // END: com.azure.storage.queue.queueServiceClient.deleteQueue#string
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#deleteQueueWithResponse(String, Duration, Context)}
     */
    public void deleteQueueWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-duration-context
        VoidResponse response = client.deleteQueueWithResponse("myqueue", Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getProperties
        StorageServiceProperties properties = client.getProperties();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#duration-context
        StorageServiceProperties properties = client.getPropertiesWithResponse(Duration.ofSeconds(1),
            new Context(key1, value1)).value();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.hourMetrics().enabled(), properties.minuteMetrics().enabled());
        // END: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)}
     */
    public void setProperties() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
        StorageServiceProperties properties = client.getProperties();
        properties.cors(Collections.emptyList());

        client.setProperties(properties);
        System.out.printf("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setProperties#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(StorageServiceProperties,
     * Duration, Context)}
     */
    public void setPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#storageServiceProperties-duration-context
        StorageServiceProperties properties = client.getProperties();
        properties.cors(Collections.emptyList());
        VoidResponse response = client.setPropertiesWithResponse(properties, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#storageServiceProperties-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(StorageServiceProperties,
     * Duration, Context)} with metrics enabled.
     */
    public void setPropertiesWithResponseEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties-duration-context
        StorageServiceProperties properties = client.getProperties();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        VoidResponse response = client.setPropertiesWithResponse(properties, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#storageServiceProperties-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(StorageServiceProperties)} with metrics enabled.
     */
    public void setPropertiesEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
        StorageServiceProperties properties = client.getProperties();
        properties.minuteMetrics().enabled(true);
        properties.hourMetrics().enabled(true);
        client.setProperties(properties);
        System.out.printf("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#storageServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatistics()}
     */
    public void getStatistics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatistics
        StorageServiceStats stats = client.getStatistics();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatisticsWithResponse(Duration, Context)}
     */
    public void getStatisticsWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#duration-context
        StorageServiceStats stats = client.getStatisticsWithResponse(Duration.ofSeconds(1),
            new Context(key1, value1)).value();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.geoReplication().status(), stats.geoReplication().lastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#generateAccountSAS(AccountSASService,
     * AccountSASResourceType, AccountSASPermission, OffsetDateTime, OffsetDateTime, String, IPRange, SASProtocol)}
     */
    public void generateAccountSAS() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
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
            .read(true)
            .add(true)
            .create(true)
            .write(true)
            .delete(true)
            .list(true)
            .processMessages(true)
            .update(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        String sas = client.generateAccountSAS(service, resourceType, permission, expiryTime, startTime, version,
            ipRange, sasProtocol);
        // END: com.azure.storage.queue.queueServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
    }
}
