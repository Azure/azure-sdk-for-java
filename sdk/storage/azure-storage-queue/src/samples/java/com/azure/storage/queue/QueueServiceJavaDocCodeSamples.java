// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.QueueMessageDecodingError;
import com.azure.storage.queue.models.QueueMessageItem;
import com.azure.storage.queue.models.QueueRetentionPolicy;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.storage.queue.models.QueuesSegmentOptions;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueServiceClient} and {@link
 * QueueServiceAsyncClient}.
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
     * Generates code sample for creating a {@link QueueServiceClient} with SAS token.
     *
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
     * Generates code sample for creating a {@link QueueServiceClient} with SAS token.
     *
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.instantiation.credential
        QueueServiceClient client = new QueueServiceClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .sasToken("{SASTokenQueryParams}")
            .buildClient();
        // END: com.azure.storage.queue.queueServiceClient.instantiation.credential
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient} with {@code connectionString} which turns into
     * {@link StorageSharedKeyCredential}
     *
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
     * Generates code sample for creating a {@link QueueServiceClient}
     * with {@link QueueServiceClientBuilder#processMessageDecodingErrorAsync(Function)}.
     *
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithDecodingFailedAsyncHandler() {
        // BEGIN: com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorAsyncHandler
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";

        Function<QueueMessageDecodingError, Mono<Void>> processMessageDecodingErrorHandler =
            (queueMessageDecodingFailure) -> {
                QueueMessageItem queueMessageItem = queueMessageDecodingFailure.getQueueMessageItem();
                PeekedMessageItem peekedMessageItem = queueMessageDecodingFailure.getPeekedMessageItem();
                if (queueMessageItem != null) {
                    System.out.printf("Received badly encoded message, messageId=%s, messageBody=%s",
                        queueMessageItem.getMessageId(),
                        queueMessageItem.getBody().toString());
                    return queueMessageDecodingFailure
                        .getQueueAsyncClient()
                        .deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
                } else if (peekedMessageItem != null) {
                    System.out.printf("Peeked badly encoded message, messageId=%s, messageBody=%s",
                        peekedMessageItem.getMessageId(),
                        peekedMessageItem.getBody().toString());
                    return Mono.empty();
                } else {
                    return Mono.empty();
                }
            };

        QueueServiceClient client = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .processMessageDecodingErrorAsync(processMessageDecodingErrorHandler)
            .buildClient();
        // END: com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorAsyncHandler
        return client;
    }

    /**
     * Generates code sample for creating a {@link QueueServiceClient}
     * with {@link QueueServiceClientBuilder#processMessageDecodingError(Consumer)}.
     *
     * @return An instance of {@link QueueServiceClient}
     */
    public QueueServiceClient createClientWithDecodingFailedHandler() {
        // BEGIN: com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorHandler
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
            + "AccountKey={key};EndpointSuffix={core.windows.net}";

        Consumer<QueueMessageDecodingError> processMessageDecodingErrorHandler =
            (queueMessageDecodingFailure) -> {
                QueueMessageItem queueMessageItem = queueMessageDecodingFailure.getQueueMessageItem();
                PeekedMessageItem peekedMessageItem = queueMessageDecodingFailure.getPeekedMessageItem();
                if (queueMessageItem != null) {
                    System.out.printf("Received badly encoded message, messageId=%s, messageBody=%s",
                        queueMessageItem.getMessageId(),
                        queueMessageItem.getBody().toString());
                    queueMessageDecodingFailure
                        .getQueueClient()
                        .deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
                } else if (peekedMessageItem != null) {
                    System.out.printf("Peeked badly encoded message, messageId=%s, messageBody=%s",
                        peekedMessageItem.getMessageId(),
                        peekedMessageItem.getBody().toString());
                }
            };

        QueueServiceClient client = new QueueServiceClientBuilder()
            .connectionString(connectionString)
            .processMessageDecodingError(processMessageDecodingErrorHandler)
            .buildClient();
        // END: com.azure.storage.queue.QueueServiceClientBuilder#processMessageDecodingErrorHandler
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
        System.out.println("Complete creating queue with status code: " + response.getStatusCode());
        // END: com.azure.storage.queue.queueServiceClient.createQueueWithResponse#string-map-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues()}
     */
    public void listQueues() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues
        client.listQueues().forEach(
            queueItem -> System.out.printf("Queue %s exists in the account", queueItem.getName())
        );
        // END: com.azure.storage.queue.queueServiceClient.listQueues
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#listQueues(QueuesSegmentOptions, Duration, Context)}
     * )}
     */
    public void listQueuesWithOverload() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.listQueues#queueSergmentOptions-duration-context
        client.listQueues(new QueuesSegmentOptions().setPrefix("azure"), Duration.ofSeconds(1),
            new Context(key1, value1)).forEach(
                queueItem -> System.out.printf("Queue %s exists in the account and has metadata %s",
                queueItem.getName(), queueItem.getMetadata())
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
        Response<Void> response = client.deleteQueueWithResponse("myqueue", Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.println("Complete deleting the queue with status code: " + response.getStatusCode());
        // END: com.azure.storage.queue.queueServiceClient.deleteQueueWithResponse#string-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getProperties()}
     */
    public void getProperties() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getProperties
        QueueServiceProperties properties = client.getProperties();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.queue.queueServiceClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getPropertiesWithResponse(Duration, Context)}
     */
    public void getPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#duration-context
        QueueServiceProperties properties = client.getPropertiesWithResponse(Duration.ofSeconds(1),
            new Context(key1, value1)).getValue();
        System.out.printf("Hour metrics enabled: %b, Minute metrics enabled: %b",
            properties.getHourMetrics().isEnabled(), properties.getMinuteMetrics().isEnabled());
        // END: com.azure.storage.queue.queueServiceClient.getPropertiesWithResponse#duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(QueueServiceProperties)}
     */
    public void setProperties() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setProperties#QueueServiceProperties
        QueueServiceProperties properties = client.getProperties();
        properties.setCors(Collections.emptyList());

        client.setProperties(properties);
        System.out.println("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setProperties#QueueServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(QueueServiceProperties,
     * Duration, Context)}
     */
    public void setPropertiesWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#QueueServiceProperties-duration-context
        QueueServiceProperties properties = client.getProperties();
        properties.setCors(Collections.emptyList());
        Response<Void> response = client.setPropertiesWithResponse(properties, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponse#QueueServiceProperties-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setPropertiesWithResponse(QueueServiceProperties,
     * Duration, Context)} with metrics enabled.
     */
    public void setPropertiesWithResponseEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties-duration-context
        QueueServiceProperties properties = client.getProperties();
        properties.getMinuteMetrics().setEnabled(true);
        properties.getMinuteMetrics().setIncludeApis(true);
        properties.getMinuteMetrics().setRetentionPolicy(new QueueRetentionPolicy().setDays(7).setEnabled(true));
        properties.getHourMetrics().setEnabled(true);
        properties.getHourMetrics().setIncludeApis(true);
        properties.getHourMetrics().setRetentionPolicy(new QueueRetentionPolicy().setDays(7).setEnabled(true));
        Response<Void> response = client.setPropertiesWithResponse(properties, Duration.ofSeconds(1),
            new Context(key1, value1));
        System.out.printf("Setting Queue service properties completed with status code %d", response.getStatusCode());
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesWithResponseEnableMetrics#QueueServiceProperties-duration-context
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#setProperties(QueueServiceProperties)} with metrics
     * enabled.
     */
    public void setPropertiesEnableMetrics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#QueueServiceProperties
        QueueServiceProperties properties = client.getProperties();
        properties.getMinuteMetrics().setEnabled(true);
        properties.getMinuteMetrics().setIncludeApis(true);
        properties.getMinuteMetrics().setRetentionPolicy(new QueueRetentionPolicy().setDays(7).setEnabled(true));
        properties.getHourMetrics().setEnabled(true);
        properties.getHourMetrics().setIncludeApis(true);
        properties.getHourMetrics().setRetentionPolicy(new QueueRetentionPolicy().setDays(7).setEnabled(true));
        client.setProperties(properties);
        System.out.println("Setting Queue service properties completed.");
        // END: com.azure.storage.queue.queueServiceClient.setPropertiesEnableMetrics#QueueServiceProperties
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatistics()}
     */
    public void getStatistics() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatistics
        QueueServiceStatistics stats = client.getStatistics();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.getGeoReplication().getStatus(), stats.getGeoReplication().getLastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatistics
    }

    /**
     * Generates a code sample for using {@link QueueServiceClient#getStatisticsWithResponse(Duration, Context)}
     */
    public void getStatisticsWithResponse() {
        // BEGIN: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#duration-context
        QueueServiceStatistics stats = client.getStatisticsWithResponse(Duration.ofSeconds(1),
            new Context(key1, value1)).getValue();
        System.out.printf("Geo replication status: %s, Last synced: %s",
            stats.getGeoReplication().getStatus(), stats.getGeoReplication().getLastSyncTime());
        // END: com.azure.storage.queue.queueServiceClient.getStatisticsWithResponse#duration-context
    }

    /**
     * Code snippet for {@link QueueServiceClient#generateAccountSas(AccountSasSignatureValues)}
     */
    public void generateAccountSas() {
        QueueServiceClient queueServiceClient = createClientWithCredential();
        // BEGIN: com.azure.storage.queue.QueueServiceClient.generateAccountSas#AccountSasSignatureValues
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true).setObject(true);
        AccountSasService services = new AccountSasService().setQueueAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = queueServiceClient.generateAccountSas(sasValues);
        // END: com.azure.storage.queue.QueueServiceClient.generateAccountSas#AccountSasSignatureValues
    }

    /**
     * Code snippet for {@link QueueServiceClient#generateAccountSas(AccountSasSignatureValues, Context)}
     */
    public void generateAccountSasWithContext() {
        QueueServiceClient queueServiceClient = createClientWithCredential();
        // BEGIN: com.azure.storage.queue.QueueServiceClient.generateAccountSas#AccountSasSignatureValues-Context
        AccountSasPermission permissions = new AccountSasPermission()
            .setListPermission(true)
            .setReadPermission(true);
        AccountSasResourceType resourceTypes = new AccountSasResourceType().setContainer(true).setObject(true);
        AccountSasService services = new AccountSasService().setQueueAccess(true).setFileAccess(true);
        OffsetDateTime expiryTime = OffsetDateTime.now().plus(Duration.ofDays(2));

        AccountSasSignatureValues sasValues =
            new AccountSasSignatureValues(expiryTime, permissions, services, resourceTypes);

        // Client must be authenticated via StorageSharedKeyCredential
        String sas = queueServiceClient.generateAccountSas(sasValues, new Context("key", "value"));
        // END: com.azure.storage.queue.QueueServiceClient.generateAccountSas#AccountSasSignatureValues-Context
    }
}
