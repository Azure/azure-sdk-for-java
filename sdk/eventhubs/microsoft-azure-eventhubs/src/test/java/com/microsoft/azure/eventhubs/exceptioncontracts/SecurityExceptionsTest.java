// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.AuthorizationFailedException;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.TimeoutException;
import com.microsoft.azure.eventhubs.impl.SharedAccessSignatureTokenProvider;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

public class SecurityExceptionsTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private EventHubClient ehClient;

    @Test
    public void testEventHubClientUnAuthorizedAccessKeyName() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("---------------wrongkey------------")
                .setSasKey(correctConnectionString.getSasKey());

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create("Test Message".getBytes()));
        });
    }

    @Test
    public void testEventHubClientUnAuthorizedAccessKey() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey("--------------wrongvalue-----------");

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create("Test Message".getBytes()));
        });
    }

    @Test
    public void testEventHubClientInvalidAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature("--------------invalidtoken-------------")
                .setOperationTimeout(Duration.ofSeconds(15));

        ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), RetryPolicy.getNoRetry(), TestContext.EXECUTOR_SERVICE);

        try {
            ehClient.sendSync(EventData.create(("Test Message".getBytes())));
        } catch (TimeoutException e) {
            Assertions.assertEquals(EventHubException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testEventHubClientNullKeyNameAndAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature(null)
                .setOperationTimeout(Duration.ofSeconds(10));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create(("Test Message".getBytes())));
        });
    }

    @Test
    public void testEventHubClientUnAuthorizedAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final String wrongToken = SharedAccessSignatureTokenProvider.generateSharedAccessSignature(
                "wrongkey",
                correctConnectionString.getSasKey(),
                String.format(Locale.US, "amqps://%s/%s", correctConnectionString.getEndpoint().getHost(), correctConnectionString.getEventHubName()),
                Duration.ofSeconds(10));
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature(wrongToken);

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create("Test Message".getBytes()));
        });
    }

    @Test
    public void testEventHubClientWrongResourceInAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final String wrongToken = SharedAccessSignatureTokenProvider.generateSharedAccessSignature(
                correctConnectionString.getSasKeyName(),
                correctConnectionString.getSasKey(),
                "----------wrongresource-----------",
                Duration.ofSeconds(10));
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature(wrongToken);

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create("Test Message".getBytes()));
        });
    }

    @Test
    public void testUnAuthorizedAccessSenderCreation() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("------------wrongkeyname----------")
                .setSasKey(correctConnectionString.getSasKey());

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.createPartitionSenderSync(PARTITION_ID);
        });
    }

    @Test
    public void testUnAuthorizedAccessReceiverCreation() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("---------------wrongkey------------")
                .setSasKey(correctConnectionString.getSasKey());

        Assertions.assertThrows(AuthorizationFailedException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromStartOfStream());
        });
    }

    @Test
    public void testSendToNonExistentEventHub() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName("non-existant-entity" + UUID.randomUUID().toString())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey(correctConnectionString.getSasKey());

        Assertions.assertThrows(IllegalEntityException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.sendSync(EventData.create("test string".getBytes()));
        });
    }

    @Test
    public void testReceiveFromNonExistentEventHub() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName("non-existant-entity" + UUID.randomUUID().toString())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey(correctConnectionString.getSasKey());

        Assertions.assertThrows(IllegalEntityException.class, () -> {
            ehClient = EventHubClient.createFromConnectionStringSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
            ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromStartOfStream());
        });
    }

    @AfterEach
    public void cleanup() throws EventHubException {
        if (ehClient != null) {
            ehClient.closeSync();
        }
    }
}
