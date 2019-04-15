/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.impl.SharedAccessSignatureTokenProvider;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

public class SecurityExceptionsTest extends ApiTestBase {
    final static String PARTITION_ID = "0";
    EventHubClient ehClient;

    @Test(expected = AuthorizationFailedException.class)
    public void testEventHubClientUnAuthorizedAccessKeyName() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("---------------wrongkey------------")
                .setSasKey(correctConnectionString.getSasKey());

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create("Test Message".getBytes()));
    }

    @Test(expected = AuthorizationFailedException.class)
    public void testEventHubClientUnAuthorizedAccessKey() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey("--------------wrongvalue-----------");

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create("Test Message".getBytes()));
    }

    @Test(expected = EventHubException.class)
    public void testEventHubClientInvalidAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature("--------------invalidtoken-------------");

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create(("Test Message".getBytes())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEventHubClientNullAccessToken() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSharedAccessSignature(null);

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create(("Test Message".getBytes())));
    }

    @Test(expected = AuthorizationFailedException.class)
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

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create("Test Message".getBytes()));
    }

    @Test(expected = AuthorizationFailedException.class)
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

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create("Test Message".getBytes()));
    }

    @Test(expected = AuthorizationFailedException.class)
    public void testUnAuthorizedAccessSenderCreation() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("------------wrongkeyname----------")
                .setSasKey(correctConnectionString.getSasKey());

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.createPartitionSenderSync(PARTITION_ID);
    }

    @Test(expected = AuthorizationFailedException.class)
    public void testUnAuthorizedAccessReceiverCreation() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName(correctConnectionString.getEventHubName())
                .setSasKeyName("---------------wrongkey------------")
                .setSasKey(correctConnectionString.getSasKey());

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromStartOfStream());
    }

    @Test(expected = IllegalEntityException.class)
    public void testSendToNonExistantEventHub() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName("non-existant-entity" + UUID.randomUUID().toString())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey(correctConnectionString.getSasKey());

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.sendSync(EventData.create("test string".getBytes()));
    }

    @Test(expected = IllegalEntityException.class)
    public void testReceiveFromNonExistantEventHub() throws Throwable {
        final ConnectionStringBuilder correctConnectionString = TestContext.getConnectionString();
        final ConnectionStringBuilder connectionString = new ConnectionStringBuilder()
                .setEndpoint(correctConnectionString.getEndpoint())
                .setEventHubName("non-existant-entity" + UUID.randomUUID().toString())
                .setSasKeyName(correctConnectionString.getSasKeyName())
                .setSasKey(correctConnectionString.getSasKey());

        ehClient = EventHubClient.createSync(connectionString.toString(), TestContext.EXECUTOR_SERVICE);
        ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromStartOfStream());
    }

    @After
    public void cleanup() throws EventHubException {
        ehClient.closeSync();
    }
}