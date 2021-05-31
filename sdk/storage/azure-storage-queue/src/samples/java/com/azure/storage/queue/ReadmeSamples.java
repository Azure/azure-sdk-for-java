// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.azure.core.util.Context;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.QueueServiceProperties;
import com.azure.storage.queue.models.QueueServiceStatistics;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.QueuesSegmentOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String SAS_TOKEN = System.getenv("PRIMARY_SAS_TOKEN");
    String markers = "marker";
    QueuesSegmentOptions options = new QueuesSegmentOptions();
    Duration timeout = Duration.ofSeconds(1);
    Context context = Context.NONE;
    String messageId = "messageId";
    String popReceipt = "popReceipt";
    String messageText = "messageText";
    Duration visibilityTimeout = Duration.ofSeconds(1);
    String key = "key";
    String value = "value";
    String queueAsyncName = "queueAsyncName";
    String queueName = "queueName";
    Map<String, String> metadata = new HashMap<String, String>() {
        {
            put("key1", "val1");
            put("key2", "val2");
        }
    };

    private Logger logger = LoggerFactory.getLogger(ReadmeSamples.class);

    public void getQueueServiceClient1() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        String queueServiceURL = String.format("https://%s.queue.core.windows.net/?%s", ACCOUNT_NAME, SAS_TOKEN);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();
    }

    public void getQueueServiceClient2() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();
    }

    public void handleException() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();
        try {
            queueServiceClient.createQueue("myQueue");
        } catch (QueueStorageException e) {
            logger.error("Failed to create a queue with error code: " + e.getErrorCode());
        }
    }

    public void createQueue1() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();

        QueueClient newQueueClient = queueServiceClient.createQueue("myQueue");
    }

    public void createQueue2() {
        String queueServiceAsyncURL = String.format("https://%s.queue.core.windows.net/", ACCOUNT_NAME);
        QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder().endpoint(queueServiceAsyncURL)
                .sasToken(SAS_TOKEN).buildAsyncClient();
        queueServiceAsyncClient.createQueue("newAsyncQueue").subscribe(result -> {
            // do something when new queue created
        }, error -> {
            // do something if something wrong happened
            }, () -> {
            // completed, do something
            });
    }

    public void createWithResponse1() {
        String queueURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, queueName);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).buildClient();

        // metadata is map of key-value pair
        queueClient.createWithResponse(metadata, Duration.ofSeconds(30), Context.NONE);
    }

    public void createWithResponse2() {
        // Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
        String queueAsyncURL = String.format("https://%s.queue.core.windows.net/%s?%s", ACCOUNT_NAME, queueAsyncName,
                SAS_TOKEN);
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueAsyncURL).buildAsyncClient();
        queueAsyncClient.createWithResponse(metadata).subscribe(result -> {
            // do something when new queue created
        }, error -> {
            // do something if something wrong happened
            }, () -> {
            // completed, do something
            });
    }

    public void deleteQueue() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();

        queueServiceClient.deleteQueue("myqueue");
    }

    public void getQueueListInAccount() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();
        // @param marker: Starting point to list the queues
        // @param options: Filter for queue selection
        // @param timeout: An optional timeout applied to the operation.
        // @param context: Additional context that is passed through the Http pipeline during the service call.
        queueServiceClient.listQueues(options, timeout, context).stream().forEach(queueItem -> {
            System.out.printf("Queue %s exists in the account.", queueItem.getName());
        });
    }

    public void getPropertiesInQueueAccount() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();

        QueueServiceProperties properties = queueServiceClient.getProperties();
    }

    public void setPropertiesInQueueAccount() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();

        QueueServiceProperties properties = queueServiceClient.getProperties();
        properties.setCors(Collections.emptyList());
        queueServiceClient.setProperties(properties);
    }

    public void getQueueServiceStatistics() {
        String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
                .sasToken(SAS_TOKEN).buildClient();

        QueueServiceStatistics queueStats = queueServiceClient.getStatistics();
    }

    public void enqueueMessage() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();

        queueClient.sendMessage("myMessage");
    }

    public void updateMesage() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();
        // @param messageId: Id of the message
        // @param popReceipt: Unique identifier that must match the message for it to be updated
        // @param visibilityTimeout: How long the message will be invisible in the queue in seconds
        queueClient.updateMessage(messageId, popReceipt, "new message", visibilityTimeout);
    }

    public void peekAtMessage() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();
        // @param key: The key with which the specified value should be associated.
        // @param value: The value to be associated with the specified key.
        queueClient.peekMessages(5, Duration.ofSeconds(1), new Context(key, value)).forEach(message -> {
            System.out.println(message.getBody().toString());
        });
    }

    public void receiveMessageFromQueue() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();
        // Try to receive 10 mesages: Maximum number of messages to get
        queueClient.receiveMessages(10).forEach(message -> {
            System.out.println(message.getBody().toString());
        });
    }

    public void deleteMessageFromQueue() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();

        queueClient.deleteMessage(messageId, popReceipt);
    }

    public void getQueueProperties() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();

        QueueProperties properties = queueClient.getProperties();
    }

    public void setQueueMetadata() {
        String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
        QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
                .buildClient();

        Map<String, String> metadata = new HashMap<String, String>() {
            {
                put("key1", "val1");
                put("key2", "val2");
            }
        };
        queueClient.setMetadata(metadata);
    }
}
