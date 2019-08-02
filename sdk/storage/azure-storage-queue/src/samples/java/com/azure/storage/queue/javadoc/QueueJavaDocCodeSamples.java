// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.javadoc;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.common.credentials.SASTokenCredential;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.AccessPolicy;
import com.azure.storage.queue.models.DequeuedMessage;
import com.azure.storage.queue.models.EnqueuedMessage;
import com.azure.storage.queue.models.QueueProperties;
import com.azure.storage.queue.models.SignedIdentifier;
import com.azure.storage.queue.models.UpdatedMessage;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains code snippets when generating javadocs through doclets for {@link QueueClient} and {@link QueueAsyncClient}.
 */

public class QueueJavaDocCodeSamples {
    /**
     * Generates code sample for creating a {@link QueueClient}.
     */
    public void buildQueueClient() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation
        QueueClient client = new QueueClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient}.
     */
    public void buildQueueAsyncClient() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation
        QueueAsyncClient client = new QueueClientBuilder()
            .connectionString("connectionstring")
            .endpoint("endpoint")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link QueueClient}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.sastoken
        QueueClient queueClient = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net?${SASToken}")
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.sastoken
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithSASToken() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.sastoken
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net?{SASToken}")
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.sastoken
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.credential
        QueueClient queueClient = new QueueClientBuilder()
            .endpoint("https://${accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.credential
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@link SASTokenCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithCredential() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .endpoint("https://{accountName}.queue.core.windows.net")
            .queueName("myqueue")
            .credential(SASTokenCredential.fromQuery("{SASTokenQueryParams}"))
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.credential
        return queueAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link QueueClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueClient}
     */
    public QueueClient createClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                    + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueClient queueClient = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.storage.queue.queueClient.instantiation.connectionstring
        return queueClient;
    }

    /**
     * Generates code sample for creating a {@link QueueAsyncClient} with {@code connectionString} which turns into {@link SharedKeyCredential}
     * @return An instance of {@link QueueAsyncClient}
     */
    public QueueAsyncClient createAsyncClientWithConnectionString() {
        // BEGIN: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        String connectionString = "DefaultEndpointsProtocol=https;AccountName={name};"
                    + "AccountKey={key};EndpointSuffix={core.windows.net}";
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.storage.queue.queueAsyncClient.instantiation.connectionstring
        return queueAsyncClient;
    }

    /**
     * Generates a code sample for using {@link QueueClient#create()}
     */
    public void createQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.create
        VoidResponse response = queueClient.create();
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.create
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create()}
     */
    public void createQueueAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create
        queueAsyncClient.create().subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create
    }

    /**
     * Generates a code sample for using {@link QueueClient#create(Map)}
     */
    public void createQueueMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.create#map
        VoidResponse response = queueClient.create(Collections.singletonMap("queue", "metadataMap"));
        System.out.println("Complete creating queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.create#map
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#create(Map)}
     */
    public void createQueueAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.create#map
        queueAsyncClient.create(Collections.singletonMap("queue", "metadataMap")).subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete creating the queue!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.create#map
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String)}
     */
    public void enqueueMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessage#string
        Response<EnqueuedMessage> response = queueClient.enqueueMessage("hello msg");
        System.out.println("Complete enqueuing the message with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String)}
     */
    public void enqueueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
        queueAsyncClient.enqueueMessage("Hello, Azure").subscribe(
            response -> { },
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String, Duration, Duration)}
     */
    public void enqueueMessageWithTimeoutOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessage#string-duration-duration
        EnqueuedMessage enqueuedMessage = queueClient.enqueueMessage("Hello, Azure",
            Duration.ofSeconds(5), null).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.queueClient.enqueueMessage#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String, Duration, Duration)}
     */
    public void enqueueMessageAsyncWithTimeoutOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string-duration-duration
        queueAsyncClient.enqueueMessage("Hello, Azure",
            Duration.ofSeconds(5), null).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().messageId(),
                    response.value().expirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessage#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#enqueueMessage(String, Duration, Duration)}
     */
    public void enqueueMessageWithLiveTimeOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.enqueueMessageLiveTime#string-duration-duration
        EnqueuedMessage enqueuedMessage = queueClient.enqueueMessage("Goodbye, Azure",
            null, Duration.ofSeconds(5)).value();
        System.out.printf("Message %s expires at %s", enqueuedMessage.messageId(), enqueuedMessage.expirationTime());
        // END: com.azure.storage.queue.queueClient.enqueueMessageLiveTime#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#enqueueMessage(String, Duration, Duration)}
     */
    public void enqueueMessageAsyncWithLiveTimeOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.enqueueMessageLiveTime#string-duration-duration
        queueAsyncClient.enqueueMessage("Goodbye, Azure",
            null, Duration.ofSeconds(5)).subscribe(
                response -> System.out.printf("Message %s expires at %s", response.value().messageId(),
                    response.value().expirationTime()),
                error -> System.err.print(error.toString()),
                () -> System.out.println("Complete enqueuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.enqueueMessageLiveTime#string-duration-duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages()}
     */
    public void dequeueMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                System.out.println("Complete dequeuing the message: " + dequeuedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.queueClient.dequeueMessages
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages()}
     */
    public void dequeueMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                    + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageWithOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer
        for (DequeuedMessage dequeuedMessage : queueClient.dequeueMessages(5)) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer)}
     */
    public void dequeueMessageAsyncWithOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
        queueAsyncClient.dequeueMessages(5).subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration
        for (DequeuedMessage dequeuedMessage : queueClient.dequeueMessages(5, Duration.ofSeconds(60))) {
            System.out.printf("Dequeued %s and it becomes visible at %s",
                dequeuedMessage.messageId(), dequeuedMessage.timeNextVisible());
        }
        // END: com.azure.storage.queue.queueClient.dequeueMessages#integer-duration
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#dequeueMessages(Integer, Duration)}
     */
    public void dequeueMessageAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
        queueAsyncClient.dequeueMessages(5, Duration.ofSeconds(60)).subscribe(
            dequeuedMessage -> System.out.println("The message got from dequeue operation: "
                + dequeuedMessage.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete dequeuing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.dequeueMessages#integer-duration
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages()}
     */
    public void peekMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.peekMessages
        queueClient.peekMessages().forEach(
            peekedMessage -> {
                System.out.println("Complete peeking the message: " + peekedMessage.messageText());
            }
        );
        // END: com.azure.storage.queue.queueClient.peekMessages
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages()}
     */
    public void peekMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages
        queueAsyncClient.peekMessages().subscribe(
            peekMessages -> System.out.println("The message got from peek operation: " + peekMessages.messageText()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages
    }

    /**
     * Generates a code sample for using {@link QueueClient#peekMessages(Integer)}
     */
    public void peekMessageMaxOverload() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.peekMessages#integer
        queueClient.peekMessages(5).forEach(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.messageId(), peekMessage.dequeueCount())
        );
        // END: com.azure.storage.queue.queueClient.peekMessages#integer
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#peekMessages(Integer)}
     */
    public void peekMessageAsyncMaxOverload() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
        queueAsyncClient.peekMessages(5).subscribe(
            peekMessage -> System.out.printf("Peeked message %s has been dequeued %d times",
                peekMessage.messageId(), peekMessage.dequeueCount()),
            error -> System.err.print(error.toString()),
            () -> System.out.println("Complete peeking the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.peekMessages#integer
    }

    /**
     * Generates a code sample for using {@link QueueClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.updateMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                Response<UpdatedMessage> response = queueClient.updateMessage("newText",
                    dequeuedMessage.messageId(), dequeuedMessage.popReceipt(), null);
                System.out.println("Complete updating the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.updateMessage
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#updateMessage(String, String, String, Duration)}
     */
    public void updateMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.updateMessage
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.updateMessage("newText", dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt(), null).subscribe(
                        response -> { },
                        updateError -> System.err.print(updateError.toString()),
                        () -> System.out.println("Complete updating the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.updateMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#deleteMessage(String, String)}
     */
    public void deleteMessage() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.deleteMessage
        queueClient.dequeueMessages().forEach(
            dequeuedMessage -> {
                VoidResponse response = queueClient.deleteMessage(dequeuedMessage.messageId(),
                    dequeuedMessage.popReceipt());
                System.out.println("Complete deleting the message with status code " + response.statusCode());
            }
        );
        // END: com.azure.storage.queue.queueClient.deleteMessage
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#deleteMessage(String, String)}
     */
    public void deleteMessageAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.deleteMessage
        queueAsyncClient.dequeueMessages().subscribe(
            dequeuedMessage -> {
                queueAsyncClient.deleteMessage(dequeuedMessage.messageId(), dequeuedMessage.popReceipt()).subscribe(
                    response -> { },
                    deleteError -> System.err.print(deleteError.toString()),
                    () -> System.out.println("Complete deleting the message!")
                );
            },
            dequeueError -> System.err.print(dequeueError.toString()),
            () -> System.out.println("Complete dequeueing the message!")
        );
        // END: com.azure.storage.queue.queueAsyncClient.deleteMessage
    }

    /**
     * Generates a code sample for using {@link QueueClient#delete()}
     */
    public void deleteQueue() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.delete
        VoidResponse response = queueClient.delete();
        System.out.println("Complete deleting the queue with status code: " + response.statusCode());
        // END: com.azure.storage.queue.queueClient.delete
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#delete()}
     */
    public void deleteQueueAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.delete
        queueAsyncClient.delete().subscribe(
            response -> System.out.println("Deleting the queue completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.delete
    }

    /**
     * Generates a code sample for using {@link QueueClient#getProperties()}
     */
    public void getProperties() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.getProperties
        QueueProperties properties = queueClient.getProperties().value();
        System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
            properties.approximateMessagesCount());
        // END: com.azure.storage.queue.queueClient.getProperties
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getProperties()}
     */
    public void getPropertiesAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getProperties
        queueAsyncClient.getProperties()
            .subscribe(response -> {
                QueueProperties properties = response.value();
                System.out.printf("Metadata: %s, Approximate message count: %d", properties.metadata(),
                    properties.approximateMessagesCount());
            });
        // END: com.azure.storage.queue.queueAsyncClient.getProperties
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadata() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setMetadata#map
        VoidResponse response = queueClient.setMetadata(Collections.singletonMap("queue", "metadataMap"));
        System.out.printf("Setting metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to set metadata.
     */
    public void setMetadataAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setMetadata#map
        queueAsyncClient.setMetadata(Collections.singletonMap("queue", "metadataMap"))
            .subscribe(response -> System.out.printf("Setting metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.setMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadata() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMetadata#map
        VoidResponse response = queueClient.setMetadata(null);
        System.out.printf("Clearing metadata completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.clearMetadata#map
    }

    /**
     * Generate a code sample for using {@link QueueAsyncClient#setMetadata(Map)} to clear metadata.
     */
    public void clearMetadataAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
        queueAsyncClient.setMetadata(null)
            .subscribe(response -> System.out.printf("Clearing metadata completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.clearMetadata#map
    }

    /**
     * Generates a code sample for using {@link QueueClient#getAccessPolicy()}
     */
    public void getAccessPolicy() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.getAccessPolicy
        for (SignedIdentifier permission : queueClient.getAccessPolicy()) {
            System.out.printf("Access policy %s allows these permissions: %s", permission.id(),
                permission.accessPolicy().permission());
        }
        // END: com.azure.storage.queue.queueClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#getAccessPolicy()}
     */
    public void getAccessPolicyAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
        queueAsyncClient.getAccessPolicy()
            .subscribe(result -> System.out.printf("Access policy %s allows these permissions: %s",
                result.id(), result.accessPolicy().permission()));
        // END: com.azure.storage.queue.queueAsyncClient.getAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#setAccessPolicy(List)}
     */
    public void setAccessPolicy() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.setAccessPolicy
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        VoidResponse response = queueClient.setAccessPolicy(Collections.singletonList(permission));
        System.out.printf("Setting access policies completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.setAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueAsyncClient#setAccessPolicy(List)}
     */
    public void setAccessPolicyAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.setAccessPolicy
        AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

        SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
        queueAsyncClient.setAccessPolicy(Collections.singletonList(permission))
            .subscribe(response -> System.out.printf("Setting access policies completed with status code %d",
                response.statusCode()));
        // END: com.azure.storage.queue.queueAsyncClient.setAccessPolicy
    }

    /**
     * Generates a code sample for using {@link QueueClient#clearMessages()}
     */
    public void clearMessages() {
        QueueClient queueClient = createClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueClient.clearMessages
        VoidResponse response = queueClient.clearMessages();
        System.out.printf("Clearing messages completed with status code %d", response.statusCode());
        // END: com.azure.storage.queue.queueClient.clearMessages
    }


    /**
     * Generates a code sample for using {@link QueueAsyncClient#clearMessages()}
     */
    public void clearMessagesAsync() {
        QueueAsyncClient queueAsyncClient = createAsyncClientWithSASToken();
        // BEGIN: com.azure.storage.queue.queueAsyncClient.clearMessages
        queueAsyncClient.clearMessages().subscribe(
            response -> System.out.println("Clearing messages completed with status code: " + response.statusCode())
        );
        // END: com.azure.storage.queue.queueAsyncClient.clearMessages
    }

}
