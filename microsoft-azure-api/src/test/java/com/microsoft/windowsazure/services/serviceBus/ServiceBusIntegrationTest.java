package com.microsoft.windowsazure.services.serviceBus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.common.Configuration;
import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.common.ServiceFilter;
import com.microsoft.windowsazure.common.ServiceFilter.Request;
import com.microsoft.windowsazure.common.ServiceFilter.Response;
import com.microsoft.windowsazure.services.serviceBus.ListQueuesResult;
import com.microsoft.windowsazure.services.serviceBus.ListTopicsResult;
import com.microsoft.windowsazure.services.serviceBus.Message;
import com.microsoft.windowsazure.services.serviceBus.Queue;
import com.microsoft.windowsazure.services.serviceBus.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.serviceBus.ServiceBusContract;
import com.microsoft.windowsazure.services.serviceBus.Topic;

public class ServiceBusIntegrationTest extends IntegrationTestBase {

    private Configuration config;
    private ServiceBusContract service;

    static ReceiveMessageOptions RECEIVE_AND_DELETE_5_SECONDS = new ReceiveMessageOptions().setReceiveAndDelete().setTimeout(5);
    static ReceiveMessageOptions PEEK_LOCK_5_SECONDS = new ReceiveMessageOptions().setPeekLock().setTimeout(5);

    @Before
    public void createService() throws Exception {
        config = createConfiguration();
        service = config.create(ServiceBusContract.class);
    }

    @Test
    public void fetchQueueAndListQueuesWorks() throws Exception {
        // Arrange

        // Act
        Queue entry = service.getQueue("TestAlpha");
        ListQueuesResult feed = service.listQueues();

        // Assert
        assertNotNull(entry);
        assertNotNull(feed);
    }

    @Test
    public void createQueueWorks() throws Exception {
        // Arrange

        // Act
        Queue queue = new Queue("TestCreateQueueWorks").setMaxSizeInMegabytes(1024L);
        Queue saved = service.createQueue(queue);

        // Assert
        assertNotNull(saved);
        assertNotSame(queue, saved);
        assertEquals("TestCreateQueueWorks", saved.getName());
    }

    @Test
    public void deleteQueueWorks() throws Exception {
        // Arrange
        service.createQueue(new Queue("TestDeleteQueueWorks"));

        // Act
        service.deleteQueue("TestDeleteQueueWorks");

        // Assert
    }

    @Test
    public void sendMessageWorks() throws Exception {
        // Arrange
        Message message = new Message("sendMessageWorks");

        // Act
        service.sendQueueMessage("TestAlpha", message);

        // Assert
    }

    @Test
    public void receiveMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestReceiveMessageWorks";
        service.createQueue(new Queue(queueName));
        service.sendQueueMessage(queueName, new Message("Hello World"));

        // Act
        Message message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);
        byte[] data = new byte[100];
        int size = message.getBody().read(data);

        // Assert
        assertEquals(11, size);
        assertArrayEquals("Hello World".getBytes(), Arrays.copyOf(data, size));
    }

    @Test
    public void peekLockMessageWorks() throws Exception {
        // Arrange
        String queueName = "TestPeekLockMessageWorks";
        service.createQueue(new Queue(queueName));
        service.sendQueueMessage(queueName, new Message("Hello Again"));

        // Act
        Message message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

        // Assert
        byte[] data = new byte[100];
        int size = message.getBody().read(data);
        assertEquals(11, size);
        assertEquals("Hello Again", new String(data, 0, size));
    }

    @Test
    public void peekLockedMessageCanBeCompleted() throws Exception {
        // Arrange
        String queueName = "TestPeekLockedMessageCanBeCompleted";
        service.createQueue(new Queue(queueName));
        service.sendQueueMessage(queueName, new Message("Hello Again"));
        Message message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

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
        service.createQueue(new Queue(queueName));
        service.sendQueueMessage(queueName, new Message("Hello Again"));
        Message peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.unlockMessage(peekedMessage);
        Message receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);

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
        service.createQueue(new Queue(queueName));
        service.sendQueueMessage(queueName, new Message("Hello Again"));
        Message peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

        // Act
        String lockToken = peekedMessage.getLockToken();
        Date lockedUntil = peekedMessage.getLockedUntilUtc();

        service.deleteMessage(peekedMessage);
        Message receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(lockToken);
        assertNotNull(lockedUntil);
        assertNull(receivedMessage.getLockToken());
        assertNull(receivedMessage.getLockedUntilUtc());
    }

    @Test
    public void contentTypePassesThrough() throws Exception {
        // Arrange
        String queueName = "TestContentTypePassesThrough";
        service.createQueue(new Queue(queueName));

        // Act
        service.sendQueueMessage(queueName,
                new Message("<data>Hello Again</data>").setContentType("text/xml"));

        Message message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);

        // Assert
        assertNotNull(message);
        assertEquals("text/xml", message.getContentType());
    }

    @Test
    public void topicCanBeCreatedListedFetchedAndDeleted() throws ServiceException {
        // Arrange
        String topicName = "TestTopicCanBeCreatedListedFetchedAndDeleted";

        // Act
        Topic created = service.createTopic(new Topic().setName(topicName));
        ListTopicsResult listed = service.listTopics();
        Topic fetched = service.getTopic(topicName);
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
    public void filterCanSeeAndChangeRequestOrResponse() throws ServiceException {
        // Arrange
        final List<Request> requests = new ArrayList<Request>();
        final List<Response> responses = new ArrayList<Response>();

        ServiceBusContract filtered = service.withFilter(new ServiceFilter() {
            public Response handle(Request request, Next next) {
                requests.add(request);
                Response response = next.handle(request);
                responses.add(response);
                return response;
            }
        });

        // Act 
        Queue created = filtered.createQueue(new Queue("TestFilterCanSeeAndChangeRequestOrResponse"));

        // Assert
        assertNotNull(created);
        assertEquals(1, requests.size());
        assertEquals(1, responses.size());
    }
}
