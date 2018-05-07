package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class QueueSendReceiveTests extends SendReceiveTests
{
    @Override
    public String getEntityNamePrefix() {
       return "QueueSendReceiveTests";
    }

    @Override
    public boolean isEntityQueue() {
        return true;
    }

    @Override
    public boolean shouldCreateEntityForEveryTest() {
        return TestUtils.shouldCreateEntityForEveryTest();
    }

    @Override
    public boolean isEntityPartitioned() {
        return false;
    }

    @Test
    public void transactionalSendCommitTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);

        String messageId = UUID.randomUUID().toString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        this.sender.send(message, transaction);

        this.factory.endTransactionAsync(transaction, true).get();

        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());

        this.receiver.complete(receivedMessage.getLockToken());
    }

    @Test
    public void transactionalSendRollbackTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);

        String messageId = UUID.randomUUID().toString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        this.sender.send(message, transaction);

        this.factory.endTransactionAsync(transaction, false).get();

        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        Assert.assertNull(receivedMessage);
    }

    @Test
    public void transactionalCompleteCommitTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        Message message = new Message("AMQP message");
        this.sender.send(message);
        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        this.receiver.complete(receivedMessage.getLockToken(), transaction);
        transaction.commit();
        receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        Assert.assertNull(receivedMessage);
    }

    @Test
    public void transactionalCompleteRollbackTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        Message message = new Message("AMQP message");
        this.sender.send(message);

        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);
        this.receiver.complete(receivedMessage.getLockToken(), transaction);
        transaction.rollback();
        this.receiver.complete(receivedMessage.getLockToken());
    }

    @Test
    public void transactionalRequestResponseDispositionTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        Message message = new Message("AMQP message");
        this.sender.send(message);
        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        this.receiver.defer(receivedMessage.getLockToken());
        receivedMessage = this.receiver.receiveDeferredMessage(receivedMessage.getSequenceNumber());
        Assert.assertNotNull(receivedMessage);

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        this.receiver.complete(receivedMessage.getLockToken(), transaction);
        this.factory.endTransactionAsync(transaction, true).get();

        receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        Assert.assertNull(receivedMessage);
    }

    @Test
    public void transactionalRequestResponseDispositionRollbackTest() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        Message message = new Message("AMQP message");
        this.sender.send(message);
        IMessage receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        this.receiver.defer(receivedMessage.getLockToken());
        receivedMessage = this.receiver.receiveDeferredMessage(receivedMessage.getSequenceNumber());
        Assert.assertNotNull(receivedMessage);

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);
        this.receiver.complete(receivedMessage.getLockToken(), transaction);
        this.factory.endTransactionAsync(transaction, false).get();

        this.receiver.complete(receivedMessage.getLockToken());
    }

    @Test
    public void transactionThrowsWhenOperationsOfDifferentPartitionAreInSameTransaction() throws ManagementException, ServiceBusException, InterruptedException, ExecutionException {
        // Need a partitioned entity for this test. Creating one manually.
        String partitionedEntityName = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        QueueDescription queueDescription = new QueueDescription(partitionedEntityName);
        queueDescription.setEnablePartitioning(true);

        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);

        try {
            IMessageSender pSender = ClientFactory.createMessageSenderFromEntityPath(factory, partitionedEntityName);
            IMessageReceiver pReceiver = ClientFactory.createMessageReceiverFromEntityPath(factory, partitionedEntityName, ReceiveMode.PEEKLOCK);

            TransactionContext transaction = this.factory.startTransactionAsync().get();
            Message message1 = new Message("AMQP message");
            message1.setPartitionKey("1");
            Message message2 = new Message("AMQP message 2");
            message2.setPartitionKey("2");

            pSender.send(message1, transaction);
            boolean caught = false;
            try {
                pSender.send(message2, transaction);
            } catch (ServiceBusException ex) {
                caught = true;
                Assert.assertTrue(ex.getCause() instanceof UnsupportedOperationException);
            }

            Assert.assertTrue(caught);
            this.factory.endTransactionAsync(transaction, false).get();

            pSender.send(message1);
            pSender.send(message2);

            IMessage receivedMessage1 = pReceiver.receive();
            IMessage receivedMessage2 = pReceiver.receive();
            Assert.assertNotNull("Message not received", receivedMessage1);
            Assert.assertNotNull("Message not received", receivedMessage2);

            transaction = this.factory.startTransactionAsync().get();
            pReceiver.complete(receivedMessage1.getLockToken(), transaction);
            try {
                caught = false;
                pReceiver.complete(receivedMessage2.getLockToken(), transaction);
            } catch (ServiceBusException ex) {
                caught = true;
                Assert.assertTrue(ex.getCause() instanceof UnsupportedOperationException);
            }

            Assert.assertTrue(caught);

            this.factory.endTransactionAsync(transaction, false);
        }
        finally {
            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, partitionedEntityName);
        }
    }

    @Test
    public void transactionCommitWorksAcrossClientsUsingSameFactoryToSameEntity() throws ServiceBusException, InterruptedException, ExecutionException {
        this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveEntityPath, ReceiveMode.PEEKLOCK);

        Message message1 = new Message("AMQP message");
        message1.setMessageId("1");
        Message message2 = new Message("AMQP message 2");
        message2.setMessageId("2");
        this.sender.send(message1);
        IMessage receivedMessage = this.receiver.receive();

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        this.receiver.complete(receivedMessage.getLockToken(), transaction);
        this.sender.send(message2, transaction);
        this.factory.endTransactionAsync(transaction, true).get();

        receivedMessage = this.receiver.receive();
        Assert.assertNotNull(receivedMessage);
        Assert.assertEquals("2", receivedMessage.getMessageId());

        receivedMessage = this.receiver.receive(TestCommons.SHORT_WAIT_TIME);
        Assert.assertNull(receivedMessage);
    }

    @Test
    public void transactionalSendViaTest() throws ManagementException, ServiceBusException, InterruptedException, ExecutionException {
        // Need three partitioned entities for this test. Creating manually.
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();

        String intermediateQueue = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        QueueDescription queueDescription = new QueueDescription(intermediateQueue);
        queueDescription.setEnablePartitioning(true);
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
        IMessageSender intermediateSender = ClientFactory.createMessageSenderFromEntityPath(factory, intermediateQueue);
        IMessageReceiver intermediateReceiver = ClientFactory.createMessageReceiverFromEntityPath(factory, intermediateQueue, ReceiveMode.PEEKLOCK);

        String destination1 = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        queueDescription = new QueueDescription(destination1);
        queueDescription.setEnablePartitioning(true);
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
        IMessageSender destination1Sender = ClientFactory.createMessageSenderFromEntityPath(factory, destination1);
        IMessageSender destination1ViaSender = ClientFactory.createTransferMessageSenderFromEntityPathAsync(factory, destination1, intermediateQueue).get();
        IMessageReceiver destination1Receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, destination1, ReceiveMode.PEEKLOCK);

        String destination2 = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        queueDescription = new QueueDescription(destination2);
        queueDescription.setEnablePartitioning(true);
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);
        IMessageSender destination2ViaSender = ClientFactory.createTransferMessageSenderFromEntityPathAsync(factory, destination2, intermediateQueue).get();
        IMessageReceiver destination2Receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, destination2, ReceiveMode.PEEKLOCK);

        try {
            Message message1 = new Message("message");
            message1.setMessageId("1");
            message1.setPartitionKey("pk1");
            Message message2 = new Message("message");
            message2.setMessageId("2");
            message2.setPartitionKey("pk2");
            message2.setViaPartitionKey("pk1");
            Message message3 = new Message("message");
            message3.setMessageId("3");
            message3.setPartitionKey("pk3");
            message3.setViaPartitionKey("pk1");

            intermediateSender.send(message1);
            IMessage receivedMessage = intermediateReceiver.receive();
            Assert.assertNotNull(receivedMessage);
            Assert.assertEquals("pk1", receivedMessage.getPartitionKey());

            // If the transaction succeeds, then all the operations occurred on the same partition.
            TransactionContext transaction = this.factory.startTransactionAsync().get();
            intermediateReceiver.complete(receivedMessage.getLockToken(), transaction);
            destination1ViaSender.send(message2, transaction);
            destination2ViaSender.send(message3, transaction);
            this.factory.endTransactionAsync(transaction, true).get();

            // Assert that first message indeed completed.
            receivedMessage = intermediateReceiver.receive();
            Assert.assertNull(receivedMessage);

            // Assert that second message reached its destination.
            IMessage receivedMessage1 = destination1Receiver.receive();
            Assert.assertNotNull(receivedMessage1);
            Assert.assertEquals("pk2", receivedMessage1.getPartitionKey());

            // Assert destination1 message indeed used partitionKey in the destination entity.
            Message destination1Message = new Message("message");
            destination1Message.setPartitionKey("pk2");

            transaction = this.factory.startTransaction();
            destination1Receiver.complete(receivedMessage1.getLockToken(), transaction);
            destination1Sender.send(destination1Message, transaction);
            transaction.commit();

            // Assert that third message reached its destination.
            IMessage receivedMessage2 = destination2Receiver.receive();
            Assert.assertNotNull(receivedMessage2);
            Assert.assertEquals("pk3", receivedMessage2.getPartitionKey());
            destination2Receiver.complete(receivedMessage2.getLockToken());

            // Cleanup
            receivedMessage1 = destination1Receiver.receive();
            destination1Receiver.complete(receivedMessage1.getLockToken());
        }
        finally {
            intermediateSender.close();
            intermediateReceiver.close();
            destination1Sender.close();
            destination1ViaSender.close();
            destination2ViaSender.close();
            destination1Receiver.close();
            destination2Receiver.close();

            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, intermediateQueue);
            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, destination1);
            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, destination2);
        }
    }

    @Test
    public void sendViaScheduledMessagesTest() throws ManagementException, ServiceBusException, InterruptedException, ExecutionException {
        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();

        String viaQueue = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        QueueDescription queueDescription = new QueueDescription(viaQueue);
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);

        String destinationQ = TestUtils.randomizeEntityName(this.getEntityNamePrefix());
        queueDescription = new QueueDescription(destinationQ);
        EntityManager.createEntity(namespaceEndpointURI, managementClientSettings, queueDescription);

        IMessageSender destination1ViaSender = ClientFactory.createTransferMessageSenderFromEntityPathAsync(factory, destinationQ, viaQueue).get();
        IMessageReceiver destinationReceiver = ClientFactory.createMessageReceiverFromEntityPath(factory, destinationQ, ReceiveMode.PEEKLOCK);

        try {
            Message message1 = new Message("message");
            message1.setMessageId("1");

            Message message2 = new Message("message");
            message2.setMessageId("2");

            long sequenceNum;

            TransactionContext transaction = this.factory.startTransaction();
            sequenceNum = destination1ViaSender.scheduleMessage(message1, Instant.now().plusSeconds(2), transaction);
            transaction.rollback();

            transaction = this.factory.startTransaction();
            sequenceNum = destination1ViaSender.scheduleMessage(message2, Instant.now().plusSeconds(2), transaction);
            transaction.commit();

            Assert.assertNotEquals(0, sequenceNum);

            IMessage message = destinationReceiver.receive();
            Assert.assertEquals("2", message.getMessageId());

            message = destinationReceiver.receive(Duration.ofSeconds(5));
            Assert.assertNull(message);
        }
        finally {
            destination1ViaSender.close();
            destinationReceiver.close();

            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, viaQueue);
            EntityManager.deleteEntity(namespaceEndpointURI, managementClientSettings, destinationQ);
        }
    }
}
