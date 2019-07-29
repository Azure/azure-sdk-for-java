// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.eventhubs.TimeoutException;
import com.microsoft.azure.eventhubs.impl.MessageReceiver;
import com.microsoft.azure.eventhubs.impl.MessageSender;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientEntityCreateTest extends ApiTestBase {
    static final String PARTITION_ID = "0";
    static ConnectionStringBuilder connStr;
    static final int SHORT_TIMEOUT = 8;

    @BeforeClass
    public static void initialize() {
        connStr = TestContext.getConnectionString();
    }

    @Test()
    public void createReceiverShouldRetryAndThrowTimeoutExceptionUponRepeatedTransientErrors() throws Exception {
        setIsTransientOnIllegalEntityException(true);

        try {
            final ConnectionStringBuilder localConnStr = new ConnectionStringBuilder(connStr.toString());
            localConnStr.setOperationTimeout(Duration.ofSeconds(SHORT_TIMEOUT)); // to retry atleast once

            final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnStr.toString(), TestContext.EXECUTOR_SERVICE);

            try {
                eventHubClient.createReceiverSync("nonexistantcg", PARTITION_ID, EventPosition.fromStartOfStream());
                Assert.assertTrue(false); // this should be unreachable
            } catch (TimeoutException exception) {
                Assert.assertTrue(exception.getCause() instanceof IllegalEntityException);
            }

            eventHubClient.closeSync();
        } finally {
            setIsTransientOnIllegalEntityException(false);
        }
    }

    @Test()
    public void createSenderShouldRetryAndThrowTimeoutExceptionUponRepeatedTransientErrors() throws Exception {
        setIsTransientOnIllegalEntityException(true);

        try {
            final ConnectionStringBuilder localConnStr = new ConnectionStringBuilder(connStr.toString());
            localConnStr.setOperationTimeout(Duration.ofSeconds(SHORT_TIMEOUT)); // to retry atleast once
            localConnStr.setEventHubName("nonexistanteventhub");
            final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnStr.toString(), TestContext.EXECUTOR_SERVICE);

            try {
                eventHubClient.createPartitionSenderSync(PARTITION_ID);
                Assert.assertTrue(false); // this should be unreachable
            } catch (TimeoutException exception) {
                Assert.assertTrue(exception.getCause() instanceof IllegalEntityException);
            }

            eventHubClient.closeSync();
        } finally {
            setIsTransientOnIllegalEntityException(false);
        }
    }

    @Test()
    public void createInternalSenderShouldRetryAndThrowTimeoutExceptionUponRepeatedTransientErrors() throws Exception {
        setIsTransientOnIllegalEntityException(true);

        try {
            final ConnectionStringBuilder localConnStr = new ConnectionStringBuilder(connStr.toString());
            localConnStr.setOperationTimeout(Duration.ofSeconds(SHORT_TIMEOUT)); // to retry atleast once
            localConnStr.setEventHubName("nonexistanteventhub");
            final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnStr.toString(), TestContext.EXECUTOR_SERVICE);

            try {
                eventHubClient.sendSync(EventData.create("Testmessage".getBytes()));
                Assert.assertTrue(false); // this should be unreachable
            } catch (TimeoutException exception) {
                Assert.assertTrue(exception.getCause() instanceof IllegalEntityException);
            }

            eventHubClient.closeSync();
        } finally {
            setIsTransientOnIllegalEntityException(false);
        }
    }

    @Test()
    public void createReceiverFailsOnTransientErrorAndThenSucceedsOnRetry() throws Exception {
        final TestObject testObject = new TestObject();
        testObject.isRetried = false;
        final String nonExistentEventHubName = "nonexistanteh" + UUID.randomUUID();

        Consumer<MessageReceiver> onOpenRetry = new Consumer<MessageReceiver>() {
            @Override
            public void accept(MessageReceiver messageReceiver) {
                try {
                    final Field receivePathField = MessageReceiver.class.getDeclaredField("receivePath");
                    receivePathField.setAccessible(true);
                    String receivePath = (String) receivePathField.get(messageReceiver);
                    receivePathField.set(messageReceiver, receivePath.replace(nonExistentEventHubName, connStr.getEventHubName()));

                    final Field tokenAudienceField = MessageReceiver.class.getDeclaredField("tokenAudience");
                    tokenAudienceField.setAccessible(true);
                    String tokenAudience = (String) tokenAudienceField.get(messageReceiver);
                    tokenAudienceField.set(messageReceiver, tokenAudience.replace(nonExistentEventHubName, connStr.getEventHubName()));

                    testObject.isRetried = true;
                } catch (Exception ignore) {
                    System.out.println("this testcase depends on receivepath & tokenAudience in MessageReceiver class for faultinjection...");
                }
            }
        };

        final Field openRetryField = MessageReceiver.class.getDeclaredField("onOpenRetry");
        openRetryField.setAccessible(true);
        openRetryField.set(null, onOpenRetry);

        setIsTransientOnIllegalEntityException(true);

        try {
            ConnectionStringBuilder localConnectionStringBuilder = new ConnectionStringBuilder(connStr.toString());
            localConnectionStringBuilder.setEventHubName(nonExistentEventHubName);
            final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnectionStringBuilder.toString(), TestContext.EXECUTOR_SERVICE);
            eventHubClient.createReceiverSync(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.fromStartOfStream());
            eventHubClient.closeSync();
        } finally {
            setIsTransientOnIllegalEntityException(false);
        }

        Assert.assertTrue(testObject.isRetried);
    }

    @Test()
    public void createSenderFailsOnTransientErrorAndThenSucceedsOnRetry() throws Exception {
        final TestObject testObject = new TestObject();
        testObject.isRetried = false;
        final String nonExistentEventHubName = "nonexistanteh" + UUID.randomUUID();

        Consumer<MessageSender> onOpenRetry = new Consumer<MessageSender>() {
            @Override
            public void accept(MessageSender messageSender) {
                try {
                    final Field receivePathField = MessageSender.class.getDeclaredField("sendPath");
                    receivePathField.setAccessible(true);
                    String receivePath = (String) receivePathField.get(messageSender);
                    receivePathField.set(messageSender, receivePath.replace(nonExistentEventHubName, connStr.getEventHubName()));

                    final Field tokenAudienceField = MessageSender.class.getDeclaredField("tokenAudience");
                    tokenAudienceField.setAccessible(true);
                    String tokenAudience = (String) tokenAudienceField.get(messageSender);
                    tokenAudienceField.set(messageSender, tokenAudience.replace(nonExistentEventHubName, connStr.getEventHubName()));

                    testObject.isRetried = true;
                } catch (Exception ignore) {
                    if (logger.isInfoEnabled()) {
                        logger.info("this testcase depends on sendPath & tokenAudience in MessageReceiver class for FaultInjection...");
                    }
                }
            }
        };

        final Field openRetryField = MessageSender.class.getDeclaredField("onOpenRetry");
        openRetryField.setAccessible(true);
        openRetryField.set(null, onOpenRetry);

        setIsTransientOnIllegalEntityException(true);

        try {
            ConnectionStringBuilder localConnectionStringBuilder = new ConnectionStringBuilder(connStr.toString());
            localConnectionStringBuilder.setEventHubName(nonExistentEventHubName);
            final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnectionStringBuilder.toString(), TestContext.EXECUTOR_SERVICE);
            eventHubClient.createPartitionSenderSync(PARTITION_ID);
            eventHubClient.closeSync();
        } finally {
            setIsTransientOnIllegalEntityException(false);
        }

        Assert.assertTrue(testObject.isRetried);
    }

    @Test(expected = IllegalEntityException.class)
    public void createReceiverShouldThrowRespectiveExceptionUponNonTransientErrors() throws Exception {
        setIsTransientOnIllegalEntityException(false);
        final ConnectionStringBuilder localConnStr = new ConnectionStringBuilder(connStr.toString());
        localConnStr.setOperationTimeout(Duration.ofSeconds(SHORT_TIMEOUT)); // to retry atleast once

        final EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(localConnStr.toString(), TestContext.EXECUTOR_SERVICE);

        try {
            eventHubClient.createReceiverSync("nonexistantcg", PARTITION_ID, EventPosition.fromStartOfStream());
        } finally {
            eventHubClient.closeSync();
        }
    }

    static void setIsTransientOnIllegalEntityException(final boolean value) throws Exception {
        final Field isTransientField = IllegalEntityException.class.getDeclaredField("isTransient");
        isTransientField.setAccessible(true);
        isTransientField.setBoolean(null, value);
    }

    private class TestObject {
        boolean isRetried;
    }
}
