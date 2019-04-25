package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.MessageNotFoundException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.microsoft.azure.servicebus.TestCommons.SHORT_WAIT_TIME;

public class QueueSessionTests extends SessionTests
{
    @Override
    public String getEntityNamePrefix() {
       return "QueueSessionTests";
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
    public void transactionalSessionDispositionCommitTest() throws ServiceBusException, InterruptedException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);

        String messageId = UUID.randomUUID().toString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        message.setSessionId(sessionId);
        sender.send(message);

        IMessage receivedMessage = this.session.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);
        this.session.complete(receivedMessage.getLockToken(), transaction);
        this.factory.endTransactionAsync(transaction, true).get();

        receivedMessage = this.session.receive(SHORT_WAIT_TIME);
        Assert.assertNull("Message received again", receivedMessage);
    }

    @Test
    public void transactionalSessionDispositionRollbackTest() throws ServiceBusException, InterruptedException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);

        String messageId = UUID.randomUUID().toString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        message.setSessionId(sessionId);
        sender.send(message);

        IMessage receivedMessage = this.session.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);
        this.session.complete(receivedMessage.getLockToken(), transaction);
        this.factory.endTransactionAsync(transaction, false).get();
        this.session.close();

        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.RECEIVEANDDELETE);
        receivedMessage = this.session.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
    }

    @Test(expected = MessageNotFoundException.class)
    public void transactionalSessionDeferredDispositionCommitTest() throws ServiceBusException, InterruptedException, ExecutionException {
        String sessionId = TestUtils.getRandomString();
        this.session = ClientFactory.acceptSessionFromEntityPath(this.factory, this.receiveEntityPath, sessionId, ReceiveMode.PEEKLOCK);

        String messageId = UUID.randomUUID().toString();
        Message message = new Message("AMQP message");
        message.setMessageId(messageId);
        message.setSessionId(sessionId);
        sender.send(message);

        IMessage receivedMessage = this.session.receive();
        this.session.defer(receivedMessage.getLockToken());
        receivedMessage = this.session.receiveDeferredMessage(receivedMessage.getSequenceNumber());

        TransactionContext transaction = this.factory.startTransactionAsync().get();
        Assert.assertNotNull(transaction);
        this.session.complete(receivedMessage.getLockToken(), transaction);
        this.factory.endTransactionAsync(transaction, true).get();

        receivedMessage = this.session.receiveDeferredMessage(receivedMessage.getSequenceNumber());
    }
}
