/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Alteration;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.windowsazure.services.servicebus.implementation.CorrelationFilter;
import com.microsoft.windowsazure.services.servicebus.implementation.EmptyRuleAction;
import com.microsoft.windowsazure.services.servicebus.implementation.EntityStatus;
import com.microsoft.windowsazure.services.servicebus.implementation.FalseFilter;
import com.microsoft.windowsazure.services.servicebus.implementation.MessageCountDetails;
import com.microsoft.windowsazure.services.servicebus.implementation.RuleDescription;
import com.microsoft.windowsazure.services.servicebus.implementation.SqlFilter;
import com.microsoft.windowsazure.services.servicebus.implementation.SqlRuleAction;
import com.microsoft.windowsazure.services.servicebus.implementation.TrueFilter;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.GetQueueResult;
import com.microsoft.windowsazure.services.servicebus.models.ListQueuesResult;
import com.microsoft.windowsazure.services.servicebus.models.ListRulesResult;
import com.microsoft.windowsazure.services.servicebus.models.ListSubscriptionsResult;
import com.microsoft.windowsazure.services.servicebus.models.ListTopicsOptions;
import com.microsoft.windowsazure.services.servicebus.models.ListTopicsResult;
import com.microsoft.windowsazure.services.servicebus.models.QueueInfo;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveQueueMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.RuleInfo;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.microsoft.windowsazure.services.servicebus.models.TopicInfo;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class ServiceBusIntegrationTest extends IntegrationTestBase {

    private ServiceBusContract service;

    static ReceiveMessageOptions RECEIVE_AND_DELETE_5_SECONDS = new ReceiveMessageOptions()
            .setReceiveAndDelete().setTimeout(5);
    static ReceiveMessageOptions PEEK_LOCK_5_SECONDS = new ReceiveMessageOptions()
            .setPeekLock().setTimeout(5);

    private String createLongString(int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result = result + "a";
        }
        return result;
    }

    @Before
    public void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(ServiceBusContract.class, Client.class,
                new Alteration<Client>() {
                    @Override
                    public Client alter(String profile, Client instance,
                            Builder builder, Map<String, Object> properties) {
                        instance.addFilter(new LoggingFilter());
                        return instance;
                    }
                });

        // applied as default configuration
        service = ServiceBusService.create(config);
    }

    @Test
    public void fetchQueueAndListQueuesWorks() throws Exception {
        // Arrange

        // Act
        QueueInfo entry = service.getQueue("TestAlpha").getValue();
        ListQueuesResult feed = service.listQueues();

        // Assert
        assertNotNull(entry);
        assertNotNull(feed);
    }

    @Test
    public void createQueueWorks() throws Exception {
        // Arrange

        // Act
        QueueInfo queue = new QueueInfo("TestCreateQueueWorks")
                .setMaxSizeInMegabytes(1024L);
        QueueInfo saved = service.createQueue(queue).getValue();

        // Assert
        assertNotNull(saved);
        assertNotSame(queue, saved);
        assertEquals(false, saved.isDeadLetteringOnMessageExpiration());
        assertEquals(false, saved.isAnonymousAccessible());
        assertNotNull(saved.getAutoDeleteOnIdle());
        assertEquals(true, saved.isSupportOrdering());
        assertEquals("TestCreateQueueWorks", saved.getPath());
    }

    @Test
    public void updateQueueWorks() throws Exception {
        // Arrange
        QueueInfo queue = new QueueInfo("TestUpdateQueueWorks")
                .setMaxSizeInMegabytes(1024L);
        QueueInfo originalQueue = service.createQueue(queue).getValue();
        Long expectedMaxSizeInMegaBytes = 512L;

        // Act
        QueueInfo updatedQueue = service.updateQueue(originalQueue
                .setMaxSizeInMegabytes(512L));

        // Assert
        assertEquals(expectedMaxSizeInMegaBytes,
                updatedQueue.getMaxSizeInMegabytes());
    }

    @Test
    public void getQueueWorks() throws Exception {
        // Arrange
        String queuePath = "TestGetQueueWorks";
        service.createQueue(new QueueInfo(queuePath));

        // Act
        GetQueueResult getQueueResult = service.getQueue(queuePath);

        // Assert
        assertNotNull(getQueueResult);

    }

    @Test(expected = ServiceException.class)
    public void getNonExistQueueFail() throws Exception {
        // Arrange
        String queuePath = "testGetNonExistQueueFail";

        // Act
        service.getQueue(queuePath);

        // Assert
    }

    @Test
    public void deleteQueueWorks() throws Exception {
        // Arrange
        service.createQueue(new QueueInfo("TestDeleteQueueWorks"));

        // Act
        service.deleteQueue("TestDeleteQueueWorks");

        // Assert
    }

    @Test
    public void sendMessageWorks() throws Exception {
        // Arrange
        BrokeredMessage message = new BrokeredMessage("sendMessageWorks");

        // Act
        service.sendQueueMessage("TestAlpha", message);

        // Assert
    }

    @Test
    public void getQueueMessageCountDetails() throws Exception {
        // Arrange
        String queueName = "testGetQueueMessageCountDetails";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello World"));
        Long expectedActiveMessageCount = 1L;
        Long expectedDeadLetterMessageCount = 0L;
        Long expectedScheduledMessageCount = 0L;
        Long expectedTransferMessageCount = 0L;
        Long expectedTransferDeadLetterMessageCount = 0L;

        // Act
        QueueInfo queueInfo = service.getQueue(queueName).getValue();
        MessageCountDetails countDetails = queueInfo.getCountDetails();

        // Assert
        assertEquals(true, queueInfo.isSupportOrdering());
        assertNotNull(countDetails);
        assertEquals(expectedActiveMessageCount,
                countDetails.getActiveMessageCount());
        assertEquals(expectedDeadLetterMessageCount,
                countDetails.getDeadLetterMessageCount());
        assertEquals(expectedScheduledMessageCount,
                countDetails.getScheduledMessageCount());
        assertEquals(expectedTransferMessageCount,
                countDetails.getTransferMessageCount());
        assertEquals(expectedTransferDeadLetterMessageCount,
                countDetails.getTransferDeadLetterMessageCount());

    }

    @Test
    public void getTopicMessageCountDetails() throws Exception {
        // Arrange
        String topicName = "TestGetTopicMessageCountDetails";
        service.createTopic(new TopicInfo(topicName)).getValue();
        Long expectedActiveMessageCount = 0L;
        Long expectedDeadLetterMessageCount = 0L;
        Long expectedScheduledMessageCount = 0L;
        Long expectedTransferMessageCount = 0L;
        Long expectedTransferDeadLetterMessageCount = 0L;

        // Act
        TopicInfo topicInfo = service.getTopic(topicName).getValue();
        MessageCountDetails countDetails = topicInfo.getCountDetails();

        // Assert
        assertNotNull(topicInfo);
        assertNotNull(countDetails);
        assertEquals(expectedActiveMessageCount,
                countDetails.getActiveMessageCount());
        assertEquals(expectedDeadLetterMessageCount,
                countDetails.getDeadLetterMessageCount());
        assertEquals(expectedScheduledMessageCount,
                countDetails.getScheduledMessageCount());
        assertEquals(expectedTransferMessageCount,
                countDetails.getTransferMessageCount());
        assertEquals(expectedTransferDeadLetterMessageCount,
                countDetails.getTransferDeadLetterMessageCount());

    }

    @Test
    public void getSubscriptionMessageCountDetails() throws Exception {
        // Arrange
        String topicName = "TestGetSubscriptionMessageCountDetails";
        String subscriptionName = "TestGetSubscriptionMessageCountDetails";
        service.createTopic(new TopicInfo(topicName)).getValue();
        service.createSubscription(topicName, new SubscriptionInfo(
                subscriptionName));
        Long expectedDeadLetterMessageCount = 0L;
        Long expectedScheduledMessageCount = 0L;
        Long expectedTransferMessageCount = 0L;
        Long expectedTransferDeadLetterMessageCount = 0L;

        // Act
        service.sendTopicMessage(topicName, new BrokeredMessage("Hello world!"));
        SubscriptionInfo subscriptionInfo = service.getSubscription(topicName,
                subscriptionName).getValue();
        MessageCountDetails countDetails = subscriptionInfo.getCountDetails();

        // Assert
        assertNotNull(subscriptionInfo);
        assertNotNull(countDetails);
        // TODO: makes the test flickery
        /* assertEquals(expectedActiveMessageCount,
                countDetails.getActiveMessageCount());*/
        assertEquals(expectedDeadLetterMessageCount,
                countDetails.getDeadLetterMessageCount());
        assertEquals(expectedScheduledMessageCount,
                countDetails.getScheduledMessageCount());
        assertEquals(expectedTransferMessageCount,
                countDetails.getTransferMessageCount());
        assertEquals(expectedTransferDeadLetterMessageCount,
                countDetails.getTransferDeadLetterMessageCount());
    }

    @Test
    public void receiveMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestReceiveMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello World"));

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                RECEIVE_AND_DELETE_5_SECONDS).getValue();
        byte[] data = new byte[100];
        int size = message.getBody().read(data);

        // Assert
        assertEquals(11, size);
        assertArrayEquals("Hello World".getBytes(), Arrays.copyOf(data, size));
    }

    @Test
    public void receiveLargeMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestReceiveLargeMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        String expectedBody = createLongString(64000);
        BrokeredMessage expectedMessage = new BrokeredMessage(expectedBody);
        service.sendQueueMessage(queueName, expectedMessage);

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                RECEIVE_AND_DELETE_5_SECONDS).getValue();
        byte[] data = new byte[64000];
        int size = message.getBody().read(data);

        // Assert
        assertEquals(expectedBody.length(), size);
        assertArrayEquals(expectedBody.getBytes(), Arrays.copyOf(data, size));

    }

    @Test
    public void renewSubscriptionMessageLockWorks() throws Exception {
        // Arrange
        String topicName = "TestRenewSubscriptionLockMessageWorks";
        String subscriptionName = "renewSubscriptionMessageLockWorks";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo(
                subscriptionName));
        service.sendTopicMessage(topicName, new BrokeredMessage("Hello Again"));

        // Act
        BrokeredMessage message = service.receiveSubscriptionMessage(topicName,
                subscriptionName, PEEK_LOCK_5_SECONDS).getValue();
        service.renewSubscriptionLock(topicName, subscriptionName,
                message.getMessageId(), message.getLockToken());

        // Assert
        assertNotNull(message);
    }

    @Test
    public void renewQueueMessageLockWorks() throws Exception {
        // Arrange
        String queueName = "TestRenewSubscriptionLockMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                PEEK_LOCK_5_SECONDS).getValue();
        service.renewQueueLock(queueName, message.getMessageId(),
                message.getLockToken());

        // Assert
        assertNotNull(message);
    }

    @Test
    public void receiveMessageEmptyQueueWorks() throws Exception {
        // Arrange
        String queueName = "TestReceiveMessageEmptyQueueWorks";
        service.createQueue(new QueueInfo(queueName));

        // Act
        ReceiveQueueMessageResult receiveQueueMessageResult = service
                .receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveQueueMessageResult);
        assertNull(receiveQueueMessageResult.getValue());
    }

    @Test
    public void receiveQueueForwardToQueueMessageSuccess() throws Exception {
        // Arrange
        String sourceQueueName = "TestReceiveQueueForwardToQueueMessageSuccessSource";
        String destinationQueueName = "TestReceiveQueueForwardToQueueMessageSuccessDestination";
        QueueInfo destinationQueueInfo = service.createQueue(
                new QueueInfo(destinationQueueName)).getValue();
        service.createQueue(
                new QueueInfo(sourceQueueName)
                        .setForwardTo(destinationQueueInfo.getUri().toString()))
                .getValue();

        // Act
        service.sendQueueMessage(sourceQueueName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveQueueMessageResult receiveQueueMessageResult = service
                .receiveQueueMessage(destinationQueueName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveQueueMessageResult);
        assertNotNull(receiveQueueMessageResult.getValue());
    }

    @Test
    public void receiveUpdatedQueueForwardToQueueMessageSuccess()
            throws Exception {
        // Arrange
        String sourceQueueName = "TestReceiveUpdatedQueueForwardToQueueMessageSuccessSource";
        String destinationQueueName = "TestReceiveUpdatedQueueForwardToQueueMessageSuccessDestination";
        QueueInfo destinationQueueInfo = service.createQueue(
                new QueueInfo(destinationQueueName)).getValue();
        QueueInfo sourceQueueInfo = new QueueInfo(sourceQueueName);
        service.createQueue(sourceQueueInfo).getValue();
        service.updateQueue(sourceQueueInfo.setForwardTo(destinationQueueInfo
                .getUri().toString()));

        // Act
        service.sendQueueMessage(sourceQueueName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveQueueMessageResult receiveQueueMessageResult = service
                .receiveQueueMessage(destinationQueueName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveQueueMessageResult);
        assertNotNull(receiveQueueMessageResult.getValue());
    }

    @Test
    public void receiveSubscriptionForwardToQueueMessageSuccess()
            throws Exception {
        // Arrange
        String sourceTopicName = "TestReceiveSubForwardToQueueMessageSuccessSource";
        String sourceSubscriptionName = "TestReceiveSubForwardToQueueMessageSuccessSource";
        String destinationQueueName = "TestReceiveSubForwardToQueueMessageSuccessDestination";
        service.createTopic(new TopicInfo(sourceTopicName)).getValue();
        QueueInfo destinationQueueInfo = service.createQueue(
                new QueueInfo(destinationQueueName)).getValue();
        service.createSubscription(sourceTopicName, new SubscriptionInfo(
                sourceSubscriptionName).setForwardTo(destinationQueueInfo
                .getUri().toString()));

        // Act
        service.sendTopicMessage(sourceTopicName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveQueueMessageResult receiveQueueMessageResult = service
                .receiveQueueMessage(destinationQueueName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveQueueMessageResult);
        assertNotNull(receiveQueueMessageResult.getValue());
    }

    @Test
    public void receiveUpdatedSubscriptionForwardToQueueMessageSuccess()
            throws Exception {
        // Arrange
        String sourceTopicName = "TestUpdatedReceiveSubForwardToQMessageSuccessSrc";
        String sourceSubscriptionName = "TestUpdatedReceiveSubForwardToQMessageSuccessSrc";
        String destinationQueueName = "TestUpdatedReceiveSubForwardToQMessageSuccessDest";
        service.createTopic(new TopicInfo(sourceTopicName)).getValue();
        QueueInfo destinationQueueInfo = service.createQueue(
                new QueueInfo(destinationQueueName)).getValue();
        SubscriptionInfo sourceSubscriptionInfo = service.createSubscription(
                sourceTopicName, new SubscriptionInfo(sourceSubscriptionName))
                .getValue();
        service.updateSubscription(sourceTopicName, sourceSubscriptionInfo
                .setForwardTo(destinationQueueInfo.getUri().toString()));
        // Act
        service.sendTopicMessage(sourceTopicName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveQueueMessageResult receiveQueueMessageResult = service
                .receiveQueueMessage(destinationQueueName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveQueueMessageResult);
        assertNotNull(receiveQueueMessageResult.getValue());
    }

    @Test
    @Ignore("Ignore due to server side failure")
    public void receiveQueueForwardToTopicMessageSuccess() throws Exception {
        // Arrange
        String sourceQueueName = "TestReceiveQueueForwardToTopicMessageSuccessSource";
        String destinationTopicName = "TestReceiveQueueForwardToTopicMessageSuccessDestination";
        String destinationSubscriptionName = "TestReceiveQueueForwardToTopicMessageSuccessDestination";
        TopicInfo destinationTopicInfo = service.createTopic(
                new TopicInfo(destinationTopicName)).getValue();
        service.createQueue(
                new QueueInfo(sourceQueueName)
                        .setForwardTo(destinationTopicInfo.getUri().toString()))
                .getValue();

        // Act
        service.sendQueueMessage(sourceQueueName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveSubscriptionMessageResult receiveSubscriptionMessageResult = service
                .receiveSubscriptionMessage(destinationTopicName,
                        destinationSubscriptionName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveSubscriptionMessageResult);
        assertNotNull(receiveSubscriptionMessageResult.getValue());
    }

    @Test
    @Ignore("Ignore because inconsistent server behavior.")
    public void receiveUpdatedQueueForwardToTopicMessageSuccess()
            throws Exception {
        // Arrange
        String sourceQueueName = "TestReceiveUpdatedQueueForwardToTopicMessageSuccessSource";
        String destinationTopicName = "TestReceiveUpdatedQueueForwardToTopicMessageSuccessDestination";
        String destinationSubscriptionName = "TestReceiveUpdatedQueueForwardToTopicMessageSuccessDestination";
        TopicInfo destinationTopicInfo = service.createTopic(
                new TopicInfo(destinationTopicName)).getValue();
        QueueInfo sourceQueueInfo = new QueueInfo(sourceQueueName);
        service.createQueue(sourceQueueInfo).getValue();
        service.updateQueue(sourceQueueInfo.setForwardTo(destinationTopicInfo
                .getUri().toString()));

        // Act
        service.sendQueueMessage(sourceQueueName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveSubscriptionMessageResult receiveSubscriptionMessageResult = service
                .receiveSubscriptionMessage(destinationTopicName,
                        destinationSubscriptionName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveSubscriptionMessageResult);
        assertNotNull(receiveSubscriptionMessageResult.getValue());
    }

    @Test
    @Ignore("due to inconsistent server behavior.")
    public void receiveSubscriptionForwardToTopicMessageSuccess()
            throws Exception {
        // Arrange
        String sourceTopicName = "TestReceiveSubForwardToTopMessageSuccessSrc";
        String sourceSubscriptionName = "TestReceiveSubForwardToTopMessageSuccessSrc";
        String destinationTopicName = "TestReceiveSubForwardToTopMessageSuccessDest";
        String destinationSubscriptionName = "TestReceiveSubForwardToTopMessageSuccessDest";
        service.createTopic(new TopicInfo(sourceTopicName)).getValue();
        TopicInfo destinationTopicInfo = service.createTopic(
                new TopicInfo(destinationTopicName)).getValue();
        service.createSubscription(destinationTopicName,
                new SubscriptionInfo(destinationSubscriptionName)).getValue();
        service.createSubscription(sourceTopicName, new SubscriptionInfo(
                sourceSubscriptionName).setForwardTo(destinationTopicInfo
                .getUri().toString()));

        // Act
        service.sendTopicMessage(sourceTopicName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveSubscriptionMessageResult receiveSubscriptionMessageResult = service
                .receiveSubscriptionMessage(destinationTopicName,
                        destinationSubscriptionName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveSubscriptionMessageResult);
        assertNotNull(receiveSubscriptionMessageResult.getValue());
    }

    @Test
    @Ignore("due to inconsistent server behavior.")
    public void receiveUpdatedSubscriptionForwardToTopicMessageSuccess()
            throws Exception {
        // Arrange
        String sourceTopicName = "TestReceiveSubForwardToTopMessageSuccessSrc";
        String sourceSubscriptionName = "TestReceiveSubForwardToTopMessageSuccessSrc";
        String destinationTopicName = "TestReceiveSubForwardToTopMessageSuccessDest";
        String destinationSubscriptionName = "TestReceiveSubForwardToTopMessageSuccessDest";
        service.createTopic(new TopicInfo(sourceTopicName)).getValue();
        TopicInfo destinationTopicInfo = service.createTopic(
                new TopicInfo(destinationTopicName)).getValue();
        service.createSubscription(destinationTopicName,
                new SubscriptionInfo(destinationSubscriptionName)).getValue();
        SubscriptionInfo sourceSubscriptionInfo = service.createSubscription(
                sourceTopicName, new SubscriptionInfo(sourceSubscriptionName))
                .getValue();
        service.updateSubscription(sourceTopicName, sourceSubscriptionInfo
                .setForwardTo(destinationTopicInfo.getUri().toString()));
        Thread.sleep(1000);

        // Act
        service.sendTopicMessage(sourceTopicName, new BrokeredMessage(
                "Hello source queue!"));
        ReceiveSubscriptionMessageResult receiveSubscriptionMessageResult = service
                .receiveSubscriptionMessage(destinationTopicName,
                        destinationSubscriptionName,
                        RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(receiveSubscriptionMessageResult);
        assertNotNull(receiveSubscriptionMessageResult.getValue());
    }

    @Test
    public void peekLockMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestPeekLockMessageWorks";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));

        // Act
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                PEEK_LOCK_5_SECONDS).getValue();

        // Assert
        byte[] data = new byte[100];
        int size = message.getBody().read(data);
        assertEquals(11, size);
        assertEquals("Hello Again", new String(data, 0, size));
    }

    @Test
    public void peekLockMessageEmptyQueueWorks() throws Exception {
        // Arrange
        String queueName = "TestPeekLockMessageEmptyQueueWorks";
        service.createQueue(new QueueInfo(queueName));

        // Act
        ReceiveQueueMessageResult result = service.receiveQueueMessage(
                queueName, PEEK_LOCK_5_SECONDS);

        // Assert
        assertNotNull(result);
        assertNull(result.getValue());
    }

    @Test
    public void peekLockedMessageCanBeCompleted() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeCompleted";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = message.getLockToken();
        Date lockedUntil = message.getLockedUntilUtc();
        String lockLocation = message.getLockLocation();

        service.deleteMessage(message);

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNotNull(lockLocation);
    }

    @Test
    public void peekLockedMessageCanBeUnlocked() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeUnlocked";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage peekedMessage = service.receiveQueueMessage(queueName,
                PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.unlockMessage(peekedMessage);
        BrokeredMessage receivedMessage = service.receiveQueueMessage(
                queueName, RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNull(receivedMessage.getLockToken());
        assertNull(receivedMessage.getLockedUntilUtc());
    }

    @Test
    public void peekLockedMessageCanBeDeleted() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeDeleted";
        service.createQueue(new QueueInfo(queueName));
        service.sendQueueMessage(queueName, new BrokeredMessage("Hello Again"));
        BrokeredMessage peekedMessage = service.receiveQueueMessage(queueName,
                PEEK_LOCK_5_SECONDS).getValue();

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.deleteMessage(peekedMessage);
        BrokeredMessage receivedMessage = service.receiveQueueMessage(
                queueName, RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNull(receivedMessage);
    }

    @Test
    public void emptyQueueReturnsNullMessage() throws Exception {
        // Arrange
        String queueName = "testEmptyQueueReturnsNullMessage";
        service.createQueue(new QueueInfo(queueName));

        // Act
        BrokeredMessage brokeredMessage = service.receiveQueueMessage(
                queueName, PEEK_LOCK_5_SECONDS).getValue();

        // Assert
        assertNull(brokeredMessage);
    }

    @Test
    public void contentTypePassesThrough() throws Exception {
        // Arrange
        String queueName = "TestContentTypePassesThrough";
        service.createQueue(new QueueInfo(queueName));

        // Act
        service.sendQueueMessage(queueName, new BrokeredMessage(
                "<data>Hello Again</data>").setContentType("text/xml"));

        BrokeredMessage message = service.receiveQueueMessage(queueName,
                RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertNotNull(message);
        assertEquals("text/xml", message.getContentType());
    }

    @Test
    public void topicCanBeCreatedListedFetchedAndDeleted()
            throws ServiceException {
        // Arrange
        String topicName = "TestTopicCanBeCreatedListedFetchedAndDeleted";

        // Act
        TopicInfo created = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listed = service.listTopics();
        TopicInfo fetched = service.getTopic(topicName).getValue();
        service.deleteTopic(topicName);
        ListTopicsResult listed2 = service.listTopics();

        // Assert
        assertNotNull(created);
        assertNotNull(listed);
        assertNotNull(fetched);
        assertNotNull(listed2);

        assertEquals(listed.getItems().size() - 1, listed2.getItems().size());
    }

    @Test
    public void listTopicsUnderASpecificPath() throws ServiceException {
        // Arrange
        String topicName = "testPathA/testPathB/listTopicUnderASpecificPath";

        // Act
        TopicInfo topicInfo = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listTopicResult = service
                .listTopics(new ListTopicsOptions()
                        .setFilter("startswith(path, 'testPathA/testPathB') eq true"));

        // Assert
        assertNotNull(topicInfo);
        assertEquals(1, listTopicResult.getItems().size());
    }

    @Test
    public void listTopicsUpdatedInLastFiveMinutes() throws ServiceException {
        String topicName = "testListTopicUpdatedInLastFiveMinutes";

        // Act
        TopicInfo topicInfo = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listTopicResult = service
                .listTopics(new ListTopicsOptions()
                        .setFilter("ModifiedAt gt '1/25/2012 3:41:41 PM'"));

        // Assert
        assertNotNull(topicInfo);
        assertEquals(1, listTopicResult.getItems().size());
    }

    @Test
    public void listTopicsAccessedSinceASpecificTime() throws ServiceException {
        removeTopics();
        String topicName = "testListTopicAccessedInLastFiveMinutes";

        // Act
        TopicInfo topicInfo = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listTopicResult = service
                .listTopics(new ListTopicsOptions()
                        .setFilter("AccessedAt gt '1/25/2012 3:41:41 PM'"));

        // Assert
        assertNotNull(topicInfo);
        assertEquals(0, listTopicResult.getItems().size());
    }

    @Test
    public void listTopicsCreatedSinceASpecificTime() throws ServiceException {
        removeTopics();
        String topicName = "testListTopicCreatedInLastFiveMinutes";

        // Act
        TopicInfo topicInfo = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();
        ListTopicsResult listTopicResult = service
                .listTopics(new ListTopicsOptions()
                        .setFilter("CreatedAt gt '1/25/2012 3:41:41 PM'"));

        // Assert
        assertNotNull(topicInfo);
        assertEquals(1, listTopicResult.getItems().size());
    }

    @Test
    public void topicCreatedContainsMetadata() throws ServiceException {
        // Arrange
        String topicName = "TestTopicCreatedContainsMetadata";

        // Act
        TopicInfo createdTopicInfo = service.createTopic(
                new TopicInfo().setPath(topicName)).getValue();

        // Assert
        assertNotNull(createdTopicInfo);
        assertNotNull(createdTopicInfo.getAutoDeleteOnIdle());
        assertEquals(false, createdTopicInfo.isRequiresDuplicateDetection());
        assertEquals(false,
                createdTopicInfo.isFilteringMessageBeforePublishing());
        assertEquals(EntityStatus.ACTIVE, createdTopicInfo.getStatus());
        assertEquals(true, createdTopicInfo.isSupportOrdering());
        assertEquals(false, createdTopicInfo.isAnonymousAccessible());

    }

    @Test
    public void topicCanBeUpdated() throws ServiceException {
        // Arrange
        String topicName = "testTopicCanBeUpdated";
        Long expectedMaxSizeInMegabytes = 2048L;

        // Act
        TopicInfo createdTopicInfo = service
                .createTopic(
                        new TopicInfo().setPath(topicName)
                                .setMaxSizeInMegabytes(1024L)).getValue();
        TopicInfo updatedTopicInfo = service.updateTopic(createdTopicInfo
                .setMaxSizeInMegabytes(expectedMaxSizeInMegabytes));

        // Assert
        assertEquals(expectedMaxSizeInMegabytes,
                updatedTopicInfo.getMaxSizeInMegabytes());
    }

    @Test
    public void filterCanSeeAndChangeRequestOrResponse()
            throws ServiceException {
        // Arrange
        final List<ServiceRequestContext> requests = new ArrayList<ServiceRequestContext>();
        final List<ServiceResponseContext> responses = new ArrayList<ServiceResponseContext>();

        ServiceBusContract filtered = service.withFilter(new ServiceFilter() {
            @Override
            public ServiceResponseContext handle(ServiceRequestContext request,
                    Next next) throws Exception {
                requests.add(request);
                ServiceResponseContext response = next.handle(request);
                responses.add(response);
                return response;
            }
        });

        // Act
        QueueInfo created = filtered.createQueue(
                new QueueInfo("TestFilterCanSeeAndChangeRequestOrResponse"))
                .getValue();

        // Assert
        assertNotNull(created);
        assertEquals(1, requests.size());
        assertEquals(1, responses.size());
    }

    @Test
    public void subscriptionsCanBeCreatedOnTopics() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsCanBeCreatedOnTopics";
        service.createTopic(new TopicInfo(topicName));

        // Act
        SubscriptionInfo created = service.createSubscription(topicName,
                new SubscriptionInfo("MySubscription")).getValue();

        // Assert
        assertNotNull(created);
        assertEquals("MySubscription", created.getName());
        assertEquals(false, created.isRequiresSession());
        assertEquals(true,
                created.isDeadLetteringOnFilterEvaluationExceptions());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertNotNull(created.getAccessedAt());
        assertNotNull(created.getAutoDeleteOnIdle());
    }

    @Test
    public void createSubscriptionWithCorrelationFilter() throws Exception {
        // Arrange
        String topicName = "testCreateSubscriptionWithCorrelationFilter";
        String expectedCorrelationId = "sampleCorrelationId";
        String expectedContentType = "sampleContentType";
        String expectedLabel = "sampleLabel";
        String expectedMessageId = "sampleMessageId";
        String expectedSessionId = "sampleSessionId";
        String expectedReplyTo = "sampleReplyTo";
        String expectedTo = "sampleTo";
        service.createTopic(new TopicInfo(topicName));
        CorrelationFilter correlationFilter = new CorrelationFilter();
        correlationFilter.setCorrelationId(expectedCorrelationId);
        correlationFilter.setContentType(expectedContentType);
        correlationFilter.setLabel(expectedLabel);
        correlationFilter.setMessageId(expectedMessageId);
        correlationFilter.setReplyTo(expectedReplyTo);
        correlationFilter.setSessionId(expectedSessionId);
        correlationFilter.setTo(expectedTo);
        RuleDescription ruleDescription = new RuleDescription();
        ruleDescription.setFilter(correlationFilter);

        // Act
        SubscriptionInfo created = service.createSubscription(
                topicName,
                new SubscriptionInfo("MySubscription")
                        .setDefaultRuleDescription(ruleDescription)).getValue();

        RuleInfo ruleInfo = service.getRule(topicName, "MySubscription",
                "$Default").getValue();
        CorrelationFilter correlationFilterResult = (CorrelationFilter) ruleInfo
                .getFilter();

        // Assert
        assertNotNull(created);
        assertEquals("MySubscription", created.getName());
        assertEquals(false, created.isRequiresSession());
        assertEquals(true,
                created.isDeadLetteringOnFilterEvaluationExceptions());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertNotNull(created.getAccessedAt());
        assertNotNull(created.getAutoDeleteOnIdle());
        assertNotNull(correlationFilterResult);
        assertEquals(expectedCorrelationId,
                correlationFilterResult.getCorrelationId());
        assertEquals(expectedContentType,
                correlationFilterResult.getContentType());
        assertEquals(expectedLabel, correlationFilterResult.getLabel());
        assertEquals(expectedMessageId, correlationFilterResult.getMessageId());
        assertEquals(expectedSessionId, correlationFilterResult.getSessionId());
        assertEquals(expectedReplyTo, correlationFilterResult.getReplyTo());
        assertEquals(expectedTo, correlationFilterResult.getTo());
    }

    @Test
    public void subscriptionsCanBeListed() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsCanBeListed";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo(
                "MySubscription2"));

        // Act
        ListSubscriptionsResult result = service.listSubscriptions(topicName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MySubscription2", result.getItems().get(0).getName());
    }

    @Test
    public void subscriptionsDetailsMayBeFetched() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsDetailsMayBeFetched";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo(
                "MySubscription3"));

        // Act
        SubscriptionInfo result = service.getSubscription(topicName,
                "MySubscription3").getValue();

        // Assert
        assertNotNull(result);
        assertEquals("MySubscription3", result.getName());
    }

    @Test
    public void subscriptionsMayBeDeleted() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionsMayBeDeleted";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo(
                "MySubscription4"));
        service.createSubscription(topicName, new SubscriptionInfo(
                "MySubscription5"));

        // Act
        service.deleteSubscription(topicName, "MySubscription4");

        // Assert
        ListSubscriptionsResult result = service.listSubscriptions(topicName);
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MySubscription5", result.getItems().get(0).getName());
    }

    @Test
    public void subscriptionWillReceiveMessage() throws Exception {
        // Arrange
        String topicName = "TestSubscriptionWillReceiveMessage";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.sendTopicMessage(topicName, new BrokeredMessage(
                "<p>Testing subscription</p>").setContentType("text/html"));

        // Act
        BrokeredMessage message = service.receiveSubscriptionMessage(topicName,
                "sub", RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertNotNull(message);

        byte[] data = new byte[100];
        int size = message.getBody().read(data);
        assertEquals("<p>Testing subscription</p>", new String(data, 0, size));
        assertEquals("text/html", message.getContentType());
    }

    @Test
    public void subscriptionCanBeUpdated() throws Exception {
        // Arrange
        String topicName = "testSubscriptionCanBeUpdated";
        service.createTopic(new TopicInfo(topicName));
        SubscriptionInfo originalSubscription = service.createSubscription(
                topicName, new SubscriptionInfo("sub")).getValue();
        Integer expectedMaxDeliveryCount = 1024;

        // Act
        SubscriptionInfo updatedSubscription = service.updateSubscription(
                topicName, originalSubscription
                        .setMaxDeliveryCount(expectedMaxDeliveryCount));

        // Assert
        assertEquals(expectedMaxDeliveryCount,
                updatedSubscription.getMaxDeliveryCount());
    }

    @Test
    public void rulesCanBeCreatedOnSubscriptions() throws Exception {
        // Arrange
        String topicName = "TestrulesCanBeCreatedOnSubscriptions";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo created = service.createRule(topicName, "sub",
                new RuleInfo("MyRule1")).getValue();

        // Assert
        assertNotNull(created);
        assertEquals("MyRule1", created.getName());
    }

    @Test
    public void rulesCanBeListedAndDefaultRuleIsPrecreated() throws Exception {
        // Arrange
        String topicName = "TestrulesCanBeListedAndDefaultRuleIsPrecreated";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule2"));

        // Act
        ListRulesResult result = service.listRules(topicName, "sub");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        RuleInfo rule0 = result.getItems().get(0);
        RuleInfo rule1 = result.getItems().get(1);
        if (rule0.getName().equals("MyRule2")) {
            RuleInfo swap = rule1;
            rule1 = rule0;
            rule0 = swap;
        }

        assertEquals("$Default", rule0.getName());
        assertEquals("MyRule2", rule1.getName());
        assertNotNull(result.getItems().get(0).getModel());
    }

    @Test
    public void ruleDetailsMayBeFetched() throws Exception {
        // Arrange
        String topicName = "TestruleDetailsMayBeFetched";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo result = service.getRule(topicName, "sub", "$Default")
                .getValue();

        // Assert
        assertNotNull(result);
        assertEquals("$Default", result.getName());
    }

    @Test
    public void rulesMayBeDeleted() throws Exception {
        // Arrange
        String topicName = "TestRulesMayBeDeleted";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule4"));
        service.createRule(topicName, "sub", new RuleInfo("MyRule5"));

        // Act
        service.deleteRule(topicName, "sub", "MyRule5");
        service.deleteRule(topicName, "sub", "$Default");

        // Assert
        ListRulesResult result = service.listRules(topicName, "sub");
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("MyRule4", result.getItems().get(0).getName());
    }

    @Test
    public void rulesMayHaveActionAndFilter() throws ServiceException {
        // Arrange
        String topicName = "TestRulesMayHaveAnActionAndFilter";
        service.createTopic(new TopicInfo(topicName));
        service.createSubscription(topicName, new SubscriptionInfo("sub"));

        // Act
        RuleInfo ruleOne = service.createRule(topicName, "sub",
                new RuleInfo("One").withCorrelationIdFilter("my-id"))
                .getValue();
        RuleInfo ruleTwo = service.createRule(topicName, "sub",
                new RuleInfo("Two").withTrueFilter()).getValue();
        RuleInfo ruleThree = service.createRule(topicName, "sub",
                new RuleInfo("Three").withFalseFilter()).getValue();
        RuleInfo ruleFour = service.createRule(topicName, "sub",
                new RuleInfo("Four").withEmptyRuleAction()).getValue();
        RuleInfo ruleFive = service.createRule(topicName, "sub",
                new RuleInfo("Five").withSqlRuleAction("SET x = 5")).getValue();
        RuleInfo ruleSix = service.createRule(topicName, "sub",
                new RuleInfo("Six").withSqlExpressionFilter("x != 5"))
                .getValue();

        // Assert
        assertEquals(CorrelationFilter.class, ruleOne.getFilter().getClass());
        assertEquals(TrueFilter.class, ruleTwo.getFilter().getClass());
        assertEquals(FalseFilter.class, ruleThree.getFilter().getClass());
        assertEquals(EmptyRuleAction.class, ruleFour.getAction().getClass());
        assertEquals(SqlRuleAction.class, ruleFive.getAction().getClass());
        assertEquals(SqlFilter.class, ruleSix.getFilter().getClass());

    }

    @Test
    public void messagesMayHaveCustomProperties() throws ServiceException {
        // Arrange
        String queueName = "TestMessagesMayHaveCustomProperties";
        service.createQueue(new QueueInfo(queueName));

        // Act
        service.sendQueueMessage(queueName, new BrokeredMessage("")
                .setProperty("hello", "world").setProperty("foo", 42));
        BrokeredMessage message = service.receiveQueueMessage(queueName,
                RECEIVE_AND_DELETE_5_SECONDS).getValue();

        // Assert
        assertEquals("world", message.getProperty("hello"));
        assertEquals(42, message.getProperty("foo"));
    }
}
