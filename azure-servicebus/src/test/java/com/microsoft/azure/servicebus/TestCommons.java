package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import com.microsoft.azure.servicebus.primitives.MessageNotFoundException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class TestCommons {
	
	private static Duration shortWaitTime = Duration.ofSeconds(5);
	
	public static void testBasicSend(IMessageSender sender) throws InterruptedException, ServiceBusException, IOException
	{		
		sender.send(new BrokeredMessage("AMQP message"));
	}
		
	public static void testBasicSendBatch(IMessageSender sender) throws InterruptedException, ServiceBusException, IOException
	{		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<10; i++)
		{
			messages.add(new BrokeredMessage("AMQP message"));
		}
		sender.sendBatch(messages);
	}
	
	public static void testBasicReceiveAndDelete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{	
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
 				
		IBrokeredMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		receivedMessage = receiver.receive(shortWaitTime);
		Assert.assertNull("Message received again", receivedMessage);
	}
	
	public static void testBasicReceiveBatchAndDelete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		int numMessages = 10;		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<numMessages; i++)
		{
			BrokeredMessage message = new BrokeredMessage("AMQP message");
			if(sessionId != null)
			{
				message.setSessionId(sessionId);
			}
			messages.add(message);
		}
		sender.sendBatch(messages);		
		
		int totalReceivedMessages = 0;
		Collection<IBrokeredMessage> receivedMessages = receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalReceivedMessages += receivedMessages.size();
			receivedMessages = receiver.receiveBatch(numMessages);
		}
		
		Assert.assertEquals("All messages not received", numMessages, totalReceivedMessages);
		receivedMessages = receiver.receiveBatch(numMessages, shortWaitTime);
		Assert.assertNull("Messages received again", receivedMessages);
	}
		
	public static void testBasicReceiveAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		receiver.complete(receivedMessage.getLockToken());
		receivedMessage = receiver.receive(shortWaitTime);
		Assert.assertNull("Message was not properly completed", receivedMessage);
	}
	
	public static void testBasicReceiveAndAbandon(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		long deliveryCount = receivedMessage.getDeliveryCount();		
		receiver.abandon(receivedMessage.getLockToken());
		receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("DeliveryCount not incremented", deliveryCount+1, receivedMessage.getDeliveryCount());
		receiver.complete(receivedMessage.getLockToken());
	}	
	
	public static void testBasicReceiveAndDeadLetter(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		String deadLetterReason = "java client deadletter test";
		receiver.deadLetter(receivedMessage.getLockToken(), deadLetterReason, null);
		receivedMessage = receiver.receive(shortWaitTime);
		Assert.assertNull("Message was not properly deadlettered", receivedMessage);
	}	
		
	public static void testBasicReceiveAndRenewLock(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
		
		IBrokeredMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		Instant oldLockedUntilTime = receivedMessage.getLockedUntilUtc();
		Thread.sleep(1000);
		Instant newLockedUntilUtc = receiver.renewMessageLock(receivedMessage);
		Assert.assertTrue("Lock not renewed. OldLockedUntilUtc:" + oldLockedUntilTime.toString() + ", newLockedUntilUtc:" + newLockedUntilUtc, newLockedUntilUtc.isAfter(oldLockedUntilTime));
		Assert.assertEquals("Renewed lockeduntil time not set in Message", newLockedUntilUtc, receivedMessage.getLockedUntilUtc());
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testBasicReceiveAndRenewLockBatch(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		int numMessages = 10;
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<numMessages; i++)
		{
			BrokeredMessage message = new BrokeredMessage("AMQP message");
			if(sessionId != null)
			{
				message.setSessionId(sessionId);
			}
			messages.add(message);
		}
		sender.sendBatch(messages);
				
		ArrayList<IBrokeredMessage> totalReceivedMessages = new ArrayList<>();		
		
		Collection<IBrokeredMessage> receivedMessages = receiver.receiveBatch(numMessages);
		totalReceivedMessages.addAll(receivedMessages);		
		while(receivedMessages != null && receivedMessages.size() > 0 && totalReceivedMessages.size() < numMessages)
		{						
			receivedMessages = receiver.receiveBatch(numMessages);
			totalReceivedMessages.addAll(receivedMessages);	
		}
		Assert.assertEquals("All messages not received", numMessages, totalReceivedMessages.size());	
		
		ArrayList<Instant> oldLockTimes = new ArrayList<Instant>();
		for(IBrokeredMessage message : totalReceivedMessages)
		{
			oldLockTimes.add(message.getLockedUntilUtc());
		}
		
		Thread.sleep(1000);
		Collection<Instant> newLockTimes = ((BrokeredMessageReceiver)receiver).renewMessageLockBatch(totalReceivedMessages);
		Assert.assertEquals("RenewLock didn't return one instant per message in the collection", totalReceivedMessages.size(), newLockTimes.size());
		Iterator<Instant> newLockTimeIterator = newLockTimes.iterator();
		Iterator<Instant> oldLockTimeIterator = oldLockTimes.iterator();
		for(IBrokeredMessage message : totalReceivedMessages)
		{	
			Instant oldLockTime = oldLockTimeIterator.next();
			Instant newLockTime = newLockTimeIterator.next();
			Assert.assertTrue("Lock not renewed. OldLockedUntilUtc:" + oldLockTime.toString() + ", newLockedUntilUtc:" + newLockTime.toString(), newLockTime.isAfter(oldLockTime));
			Assert.assertEquals("Renewed lockeduntil time not set in Message", newLockTime, message.getLockedUntilUtc());
			receiver.complete(message.getLockToken());			
		}		
	}
		
	public static void testBasicReceiveBatchAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		int numMessages = 10;		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<numMessages; i++)
		{
			BrokeredMessage message = new BrokeredMessage("AMQP message");
			if(sessionId != null)
			{
				message.setSessionId(sessionId);
			}
			messages.add(message);
		}
		sender.sendBatch(messages);
				
		int totalMessagesReceived = 0;
		Collection<IBrokeredMessage> receivedMessages = receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalMessagesReceived += receivedMessages.size();
			for(IBrokeredMessage message : receivedMessages)
			{
				//System.out.println(message.getLockToken());
				receiver.complete(message.getLockToken());
			}
			receivedMessages = receiver.receiveBatch(numMessages);
		}
		Assert.assertEquals("All messages not received", numMessages, totalMessagesReceived);		
		
		receivedMessages = receiver.receiveBatch(numMessages, shortWaitTime);
		Assert.assertNull("Messages received again", receivedMessages);
	}
		
	public static void testSendSceduledMessageAndReceive(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{
		int secondsToWaitBeforeScheduling = 30;
		String msgId1 = UUID.randomUUID().toString();
		String msgId2 = UUID.randomUUID().toString();
		BrokeredMessage message1 = new BrokeredMessage("AMQP Scheduled message");
		message1.setMessageId(msgId1);		
		BrokeredMessage message2 = new BrokeredMessage("AMQP Scheduled message2");
		message2.setMessageId(msgId2);
		if(sessionId != null)
		{
			message1.setSessionId(sessionId);
			message2.setSessionId(sessionId);
		}
		
		sender.scheduleMessage(message1, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		sender.scheduleMessage(message2, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		Thread.sleep(secondsToWaitBeforeScheduling * 1000);
		
		Collection<IBrokeredMessage> allReceivedMessages = new LinkedList<IBrokeredMessage>();
		Collection<IBrokeredMessage> receivedMessages = receiver.receiveBatch(10);
				
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			allReceivedMessages.addAll(receivedMessages);
			receivedMessages = receiver.receiveBatch(10);
		}
		
		boolean firstMessageReceived = false;
		boolean secondMessageReceived = false;
		for(IBrokeredMessage message : allReceivedMessages)
		{
			if(message.getMessageId().equals(msgId1))
				firstMessageReceived = true;
			else if(message.getMessageId().equals(msgId2))
				secondMessageReceived = true;
		}
		
		Assert.assertTrue("Scheduled messages not received", firstMessageReceived && secondMessageReceived);
	}
		
	public static void testSendSceduledMessageAndCancel(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{
		int secondsToWaitBeforeScheduling = 30;
		String msgId1 = UUID.randomUUID().toString();
		String msgId2 = UUID.randomUUID().toString();
		BrokeredMessage message1 = new BrokeredMessage("AMQP Scheduled message");
		BrokeredMessage message2 = new BrokeredMessage("AMQP Scheduled message2");
		message1.setMessageId(msgId1);
		message2.setMessageId(msgId2);
		if(sessionId != null)
		{
			message1.setSessionId(sessionId);
			message2.setSessionId(sessionId);
		}
		
		sender.scheduleMessage(message1, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		long sequnceNumberMsg2 = sender.scheduleMessage(message2, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		sender.cancelScheduledMessage(sequnceNumberMsg2);
		Thread.sleep(secondsToWaitBeforeScheduling * 1000);
		
		Collection<IBrokeredMessage> allReceivedMessages = new LinkedList<IBrokeredMessage>();
		Collection<IBrokeredMessage> receivedMessages = receiver.receiveBatch(10);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			allReceivedMessages.addAll(receivedMessages);
			receivedMessages = receiver.receiveBatch(10);
		}
		
		Assert.assertTrue("Scheduled messages not received", allReceivedMessages.removeIf(msg -> msg.getMessageId().equals(msgId1)));
		Assert.assertFalse("Cancelled scheduled messages also received", allReceivedMessages.removeIf(msg -> msg.getMessageId().equals(msgId2)));
	}
		
	public static void testPeekMessage(IMessageSender sender, String sessionId, IMessageBrowser browser) throws InterruptedException, ServiceBusException, IOException
	{
		BrokeredMessage message = new BrokeredMessage("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		message = new BrokeredMessage("AMQP Scheduled message2");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		Thread.sleep(5000);
		IBrokeredMessage peekedMessage1 = browser.peek();
		long firstMessageSequenceNumber = peekedMessage1.getSequenceNumber();
		IBrokeredMessage peekedMessage2 = browser.peek();
		Assert.assertNotEquals("Peek returned the same message again.", firstMessageSequenceNumber, peekedMessage2.getSequenceNumber());		
		
		// Now peek with fromSequnceNumber.. May not work for partitioned entities
		IBrokeredMessage peekedMessage5 = browser.peek(firstMessageSequenceNumber);
		Assert.assertEquals("Peek with sequence number failed.", firstMessageSequenceNumber, peekedMessage5.getSequenceNumber());
	}
		
	public static void testPeekMessageBatch(IMessageSender sender, String sessionId, IMessageBrowser browser) throws InterruptedException, ServiceBusException, IOException
	{			
		BrokeredMessage message = new BrokeredMessage("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		message = new BrokeredMessage("AMQP Scheduled message2");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		Thread.sleep(5000);
		Collection<IBrokeredMessage> peekedMessages = browser.peekBatch(10);
		long firstMessageSequenceNumber = peekedMessages.iterator().next().getSequenceNumber();
		int peekedMessagesCount = peekedMessages.size();
		if(peekedMessagesCount < 2)
		{
			// Not all messages peeked. May be topic pump hasn't finished pumping all messages			
			peekedMessages = browser.peekBatch(10);
			peekedMessagesCount += peekedMessages.size();
		}
		Assert.assertEquals("PeekBatch didnot return all messages.", 2, peekedMessagesCount);		
		
		// Now peek with fromSequnceNumber.. May not work for partitioned entities
		Collection<IBrokeredMessage> peekedMessagesBatch2 = browser.peekBatch(firstMessageSequenceNumber, 10);
		Assert.assertEquals("PeekBatch with sequence number didnot return all messages.", 2, peekedMessagesBatch2.size());
		Assert.assertEquals("PeekBatch with sequence number failed.", firstMessageSequenceNumber, peekedMessagesBatch2.iterator().next().getSequenceNumber());
	}
		
	public static void testReceiveBySequenceNumberAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{			
		BrokeredMessage message = new BrokeredMessage("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());
		
		// Now receive by sequence number
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());		
		receiver.complete(receivedMessage.getLockToken());
		
		// Try to receive by sequence number again
		try
		{
			receivedMessage = receiver.receive(sequenceNumber);
			Assert.fail("Message recieved by sequnce number was not properly completed.");
		}
		catch(MessageNotFoundException e)
		{
			// Expected
		}		
	}
		
	public static void testReceiveBySequenceNumberAndAbandon(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{			
		BrokeredMessage message = new BrokeredMessage("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());
		
		// Now receive by sequence number
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		long deliveryCount = receivedMessage.getDeliveryCount();
		receiver.abandon(receivedMessage.getLockToken());
		
		// Try to receive by sequence number again
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("Abandon didn't increase the delivery count for the message received by sequence number.", deliveryCount + 1, receivedMessage.getDeliveryCount());
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testReceiveBySequenceNumberAndDefer(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{
		// Use longer strings with each defer to avoid an assert check in debug builds of service
		String phaseKey = "phase";
		String initialPhase = "undeferred";
		String firstDeferredPhase = "deferred first time";
		String secondDeferredPhase = "deferred first time and second time";
		
		BrokeredMessage sentMessage = new BrokeredMessage("AMQP message");
		HashMap customProperties = new HashMap();
		customProperties.put(phaseKey, initialPhase);
		sentMessage.setProperties(customProperties);
		if(sessionId != null)
		{
			sentMessage.setSessionId(sessionId);			
		}
		sender.send(sentMessage);
				
		IBrokeredMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		customProperties.put(phaseKey, firstDeferredPhase);
		receiver.defer(receivedMessage.getLockToken(), customProperties);
		
		// Now receive by sequence number
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		Assert.assertEquals("Defer didn't update properties of the message received by sequence number", firstDeferredPhase, receivedMessage.getProperties().get(phaseKey));
		customProperties.put(phaseKey, secondDeferredPhase);
		receiver.defer(receivedMessage.getLockToken(), customProperties);
		
		// Try to receive by sequence number again
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message after deferrring", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("Defer didn't update properties of the message received by sequence number", secondDeferredPhase, receivedMessage.getProperties().get(phaseKey));
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testReceiveBySequenceNumberAndDeadletter(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, IOException
	{			
		BrokeredMessage message = new BrokeredMessage("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		
		IBrokeredMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());		
		
		// Now receive by sequence number
		receivedMessage = receiver.receive(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		String deadLetterReason = "java client deadletter test";		
		receiver.deadLetter(receivedMessage.getLockToken(), deadLetterReason, null);
				
		// Try to receive by sequence number again
		try
		{
			receivedMessage = receiver.receive(sequenceNumber);
			Assert.fail("Message received by sequence number was not properly deadlettered");
		}
		catch(MessageNotFoundException e)
		{
			// Expected
		}
	}
	
	public static void drainAllMessagesFromReceiver(IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		Duration waitTime = Duration.ofSeconds(5);
		final int batchSize = 10;		
		Collection<IBrokeredMessage> messages = receiver.receiveBatch(batchSize, waitTime);
		while(messages !=null && messages.size() > 0)
		{
			if(receiver.getReceiveMode() == ReceiveMode.PeekLock)
			{
				for(IBrokeredMessage message: messages)
				{
					receiver.complete(message.getLockToken());
				}
			}
			messages = receiver.receiveBatch(batchSize, waitTime);
		}		
		
		IBrokeredMessage peekedMessage;
		while((peekedMessage = receiver.peek()) != null)
		{
			try
			{
				IBrokeredMessage message = receiver.receive(peekedMessage.getSequenceNumber());
				if(receiver.getReceiveMode() == ReceiveMode.PeekLock)
				{
					receiver.complete(message.getLockToken());
				}
			}
			catch(MessageNotFoundException mnfe)
			{
				// Ignore. May be there were no deferred messages
				break;
			}			
		}
	}
}
