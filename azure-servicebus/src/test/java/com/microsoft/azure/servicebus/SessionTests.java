package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TimeoutException;

public abstract class SessionTests {
	private static ConnectionStringBuilder sendBuilder;
	private static ConnectionStringBuilder receiveBuilder;
	private static MessagingFactory factory;
	private IMessageSender sender;
	private IMessageSession session;	
	
	@Before
	public void setup() throws InterruptedException, ExecutionException, ServiceBusException
	{
		sendBuilder = this.getSenderConnectionStringBuilder();
		receiveBuilder = this.getReceiverConnectionStringBuilder();		
		factory = MessagingFactory.createFromConnectionStringBuilder(sendBuilder);
		this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(sendBuilder);
	}
	
	private static String getRandomString()
	{
		return UUID.randomUUID().toString();
	}
	
	@After
	public void tearDown() throws ServiceBusException, InterruptedException
	{
		this.drainSession();
		
		this.sender.close();
		if(this.session != null)
			this.session.close();		
		
	}
	
	@AfterClass
	public static void afterClass() throws ServiceBusException, InterruptedException
	{
		factory.close();
	}	
	
	public abstract ConnectionStringBuilder getSenderConnectionStringBuilder();
	
	public abstract ConnectionStringBuilder getReceiverConnectionStringBuilder();
	
	@Test
	public void testBasicReceiveAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.ReceiveAndDelete);
		TestCommons.testBasicReceiveAndDelete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.ReceiveAndDelete);
		TestCommons.testBasicReceiveBatchAndDelete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndAbandon() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndAbandon(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndDeadLetter(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndRenewLock() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndRenewLock(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveAndRenewLockBatch() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveAndRenewLockBatch(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testBasicReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, ExecutionException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testBasicReceiveBatchAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testSendSceduledMessageAndReceive() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.ReceiveAndDelete);
		TestCommons.testSendSceduledMessageAndReceive(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testSendSceduledMessageAndCancel() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.ReceiveAndDelete);
		TestCommons.testSendSceduledMessageAndCancel(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testPeekMessage() throws InterruptedException, ServiceBusException
	{		
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testPeekMessage(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testPeekMessageBatch() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testPeekMessageBatch(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndComplete() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndComplete(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndAbandon() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndAbandon(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDefer() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndDefer(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testReceiveBySequenceNumberAndDeadletter() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		TestCommons.testReceiveBySequenceNumberAndDeadletter(this.sender, sessionId, this.session);
	}
	
	@Test
	public void testAcceptAnySession() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		String messageId = getRandomString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		this.sender.send(message);
		
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), null, ReceiveMode.PeekLock);
		Assert.assertNotNull("Did not receive a session", this.session);		
	}
	
	@Test
	public void testRenewSessionLock() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		Instant initialValidity = this.session.getLockedUntilUtc();
		this.session.renewLock();
		Instant renewedValidity = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time.", renewedValidity.isAfter(initialValidity));
		this.session.renewLock();
		Instant renewedValidity2 = this.session.getLockedUntilUtc();
		Assert.assertTrue("RenewSessionLock did not renew session lockeduntil time.", renewedValidity2.isAfter(renewedValidity));
	}
		
	@Test
	public void testGetAndSetState() throws InterruptedException, ServiceBusException
	{
		String sessionId = getRandomString();
		this.session = ClientFactory.acceptSessionFromEntityPath(factory, receiveBuilder.getEntityPath(), sessionId, ReceiveMode.PeekLock);
		byte[] initialState = this.session.getState();
		Assert.assertNull("Session state is not null for a new session", initialState);
		byte[] customState = "Custom Session State".getBytes();
		this.session.setState(customState);
		byte[] updatedState = this.session.getState();
		Assert.assertArrayEquals("Session state not updated properly", customState, updatedState);
		this.session.setState(null);
		updatedState = this.session.getState();
		Assert.assertNull("Session state is not removed by setting a null state", updatedState);
		this.session.setState(customState);
		updatedState = this.session.getState();
		Assert.assertArrayEquals("Session state not updated properly", customState, updatedState);
	}
	
	private void drainSession() throws InterruptedException, ServiceBusException
	{
		if(this.session != null)
		{
			TestCommons.drainAllMessagesFromReceiver(this.session);
			session.setState(null);
		}
	}	
}
