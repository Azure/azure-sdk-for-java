package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public abstract class SendReceiveTests {	
	private ConnectionStringBuilder sendBuilder;
	private ConnectionStringBuilder receiveBuilder;
	private MessagingFactory factory;
	private IMessageSender sender;
	private IMessageReceiver receiver;	
	private final String sessionId = null;	
	
	@Before
	public void setup() throws IOException, InterruptedException, ExecutionException, ServiceBusException
	{
		this.sendBuilder = this.getSenderConnectionStringBuilder();
		this.receiveBuilder = this.getReceiverConnectionStringBuilder();		
		this.factory = MessagingFactory.createFromConnectionStringBuilder(this.sendBuilder);
		this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(this.sendBuilder);
	}
	
	@After
	public void tearDown() throws ServiceBusException, IOException, InterruptedException, ExecutionException
	{
		this.drainAllMessages();
		
		this.sender.close();
		if(this.receiver != null)
			this.receiver.close();
		this.factory.close();
	}	
	
	public abstract ConnectionStringBuilder getSenderConnectionStringBuilder();
	
	public abstract ConnectionStringBuilder getReceiverConnectionStringBuilder();
	
	@Test
	public void testBasicSend() throws InterruptedException, ServiceBusException, IOException
	{		
		TestCommons.testBasicSend(this.sender);
	}
	
	@Test
	public void testBasicSendBatch() throws InterruptedException, ServiceBusException, IOException
	{
		TestCommons.testBasicSendBatch(this.sender);
	}
	
	@Test
	public void testBasicReceiveAndDelete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		TestCommons.testBasicReceiveAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		TestCommons.testBasicReceiveBatchAndDelete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndDeadLetter(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndRenewLock(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveAndRenewLockBatch() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndRenewLockBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveBatchAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException, IOException
	{	
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		TestCommons.testSendSceduledMessageAndReceive(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		TestCommons.testSendSceduledMessageAndCancel(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessage() throws InterruptedException, ServiceBusException, IOException
	{		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testPeekMessage(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testPeekMessageBatch() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testPeekMessageBatch(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, this.sessionId, this.receiver);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException, IOException
	{
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, this.receiveBuilder.getEntityPath(), ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, this.sessionId, this.receiver);
	}
	
	private void drainAllMessages() throws IOException, InterruptedException, ServiceBusException
	{
		if(this.receiver != null)
		{
			TestCommons.drainAllMessagesFromReceiver(this.receiver);
		}		
	}	
}
