// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.eventdata;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.impl.MessageSender;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class BackCompatTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    private static final Message ORIGINAL_MESSAGE = Proton.message();
    private static final String APPLICATION_PROPERTY = "firstProp";
    private static final String INT_APPLICATION_PROPERTY = "intProp";
    private static final String PAYLOAD = "testmsg";

    private static EventHubClient ehClient;
    private static MessagingFactory msgFactory;
    private static PartitionReceiver receiver;
    private static MessageSender partitionMsgSender;
    private static EventData receivedEvent;

    private final Consumer<EventData> validateAmqpPropertiesInEventData = new Consumer<EventData>() {
        @Override
        public void accept(EventData eData) {
            Assert.assertTrue(eData.getProperties().containsKey(APPLICATION_PROPERTY)
                    && eData.getProperties().get(APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(APPLICATION_PROPERTY)));

            Assert.assertTrue(eData.getProperties().containsKey(INT_APPLICATION_PROPERTY)
                    && eData.getProperties().get(INT_APPLICATION_PROPERTY).equals(ORIGINAL_MESSAGE.getApplicationProperties().getValue().get(INT_APPLICATION_PROPERTY)));

            Assert.assertTrue(eData.getProperties().size() == 2);

            Assert.assertTrue(new String(eData.getBytes()).equals(PAYLOAD));
        }
    };

    @BeforeClass
    public static void initialize() throws EventHubException, IOException, InterruptedException, ExecutionException {
        final ConnectionStringBuilder connStrBuilder = TestContext.getConnectionString();
        final String connectionString = connStrBuilder.toString();

        ehClient = EventHubClient.createSync(connectionString, TestContext.EXECUTOR_SERVICE);
        msgFactory = MessagingFactory.createFromConnectionString(connectionString, TestContext.EXECUTOR_SERVICE).get();
        receiver = ehClient.createReceiverSync(TestContext.getConsumerGroupName(), PARTITION_ID, EventPosition.fromEnqueuedTime(Instant.now()));
        partitionMsgSender = MessageSender.create(msgFactory, "link1", connStrBuilder.getEventHubName() + "/partitions/" + PARTITION_ID).get();

        // until version 0.10.0 - we used to have Properties as HashMap<String,String>
        // This specific combination is intended to test the back compat - with the new Properties type as HashMap<String, Object>
        final HashMap<String, Object> appProperties = new HashMap<>();
        appProperties.put(APPLICATION_PROPERTY, "value1");
        appProperties.put(INT_APPLICATION_PROPERTY, "3");
        // back compat end

        final ApplicationProperties applicationProperties = new ApplicationProperties(appProperties);
        ORIGINAL_MESSAGE.setApplicationProperties(applicationProperties);

        ORIGINAL_MESSAGE.setBody(new Data(Binary.create(ByteBuffer.wrap(PAYLOAD.getBytes()))));

        partitionMsgSender.send(ORIGINAL_MESSAGE).get();
        receivedEvent = receiver.receiveSync(10).iterator().next();
    }

    @AfterClass
    public static void cleanup() throws EventHubException {
        if (partitionMsgSender != null) {
            partitionMsgSender.closeSync();
        }

        if (receiver != null) {
            receiver.closeSync();
        }

        if (ehClient != null) {
            ehClient.closeSync();
        }

        if (msgFactory != null) {
            msgFactory.closeSync();
        }
    }

    /**
     * Verifies test work with versions before 0.11.0.
     */
    @Test
    public void backCompatWithJavaSDKOlderThan0110() {
        validateAmqpPropertiesInEventData.accept(receivedEvent);
    }
}
