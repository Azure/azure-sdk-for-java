package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.management.EntityManager;
import com.microsoft.azure.servicebus.management.ManagementException;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
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
}
