package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import com.microsoft.azure.servicebus.primitives.MessageNotFoundException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

public class TestCommons {
	
	public static final Duration SHORT_WAIT_TIME = Duration.ofSeconds(5);
	private static final Duration DRAIN_MESSAGES_WAIT_TIME = Duration.ofSeconds(2);
	
	public static void testBasicSend(IMessageSender sender) throws InterruptedException, ServiceBusException
	{		
		sender.send(new Message("AMQP message"));
	}
		
	public static void testBasicSendBatch(IMessageSender sender) throws InterruptedException, ServiceBusException
	{		
		List<Message> messages = new ArrayList<Message>();
		for(int i=0; i<10; i++)
		{
			messages.add(new Message("AMQP message"));
		}
		sender.sendBatch(messages);
	}
	
	public static void testBasicReceiveAndDeleteWithValueData(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
    {
		String messageData = "testBasicReceiveAndDeleteWithValueData";
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(MessageBody.fromValueData(messageData));
        message.setMessageId(messageId);
        if(sessionId != null)
        {
            message.setSessionId(sessionId);
        }
		
        sender.send(message);
        
        IMessage receivedMessage = receiver.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
        Assert.assertEquals("Message Body Type did not match", MessageBodyType.VALUE, receivedMessage.getMessageBody().getBodyType());
        Assert.assertEquals("Message content did not match", messageData, receivedMessage.getMessageBody().getValueData());
        receivedMessage = receiver.receive(SHORT_WAIT_TIME);
        Assert.assertNull("Message received again", receivedMessage);
    }
	
	public static void testBasicReceiveAndDeleteWithBinaryData(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{
		testBasicReceiveAndDeleteWithBinaryData(sender, sessionId, receiver, 64);
	}
	
	public static void testBasicReceiveAndDeleteWithLargeBinaryData(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{
		testBasicReceiveAndDeleteWithBinaryData(sender, sessionId, receiver, 64 * 1024);
	}
	
	private static void testBasicReceiveAndDeleteWithBinaryData(IMessageSender sender, String sessionId, IMessageReceiver receiver, int messageSize) throws InterruptedException, ServiceBusException, ExecutionException
    {
        String messageId = UUID.randomUUID().toString();
        byte[] binaryData = new byte[messageSize];
        for(int i=0; i< binaryData.length; i++)
        {
            binaryData[i] = (byte)i;
        }
        Message message = new Message(Utils.fromBinay(binaryData));
        message.setMessageId(messageId);
        if(sessionId != null)
        {
            message.setSessionId(sessionId);
        }
		
        sender.send(message);
        
        IMessage receivedMessage = receiver.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
        Assert.assertEquals("Message Body Type did not match", MessageBodyType.BINARY, receivedMessage.getMessageBody().getBodyType());
        Assert.assertArrayEquals("Message content did not match", binaryData, Utils.getDataFromMessageBody(receivedMessage.getMessageBody()));
        receivedMessage = receiver.receive(SHORT_WAIT_TIME);
        Assert.assertNull("Message received again", receivedMessage);
    }
	
	public static void testBasicReceiveAndDeleteWithSequenceData(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
    {
        String messageId = UUID.randomUUID().toString();
        List<Object> sequence = new ArrayList<Object>();
        sequence.add("azure");
        sequence.add("servicebus");
        sequence.add("messaging");
        Message message = new Message(Utils.fromSequence(sequence));
        message.setMessageId(messageId);
        if(sessionId != null)
        {
            message.setSessionId(sessionId);
        }
        sender.send(message);
                
        IMessage receivedMessage = receiver.receive();
        Assert.assertNotNull("Message not received", receivedMessage);
        Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
        Assert.assertEquals("Message Body Type did not match", MessageBodyType.SEQUENCE, receivedMessage.getMessageBody().getBodyType());
        Assert.assertArrayEquals("Message content did not match", sequence.toArray(new String[] {}), Utils.getSequenceFromMessageBody(receivedMessage.getMessageBody()).toArray(new String[] {}));
        receivedMessage = receiver.receive(SHORT_WAIT_TIME);
        Assert.assertNull("Message received again", receivedMessage);
    }
	
	public static void testBasicReceiveBatchAndDelete(IMessageSender sender, String sessionId, IMessageReceiver receiver, boolean isEntityPartitioned) throws InterruptedException, ServiceBusException, ExecutionException
	{
		int numMessages = 10;		
		
		if(isEntityPartitioned)
		{
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				sender.send(message);
			}
		}
		else
		{
			// Keep this batch send part as this test sendBatch API call
			List<Message> messages = new ArrayList<Message>();
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				messages.add(message);
			}
			sender.sendBatch(messages);
		}
				
		
		int totalReceivedMessages = 0;
		Collection<IMessage> receivedMessages = receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalReceivedMessages += receivedMessages.size();
			receivedMessages = receiver.receiveBatch(numMessages);
		}
		
		Assert.assertEquals("All messages not received", numMessages, totalReceivedMessages);
		receivedMessages = receiver.receiveBatch(numMessages, SHORT_WAIT_TIME);
		Assert.assertNull("Messages received again", receivedMessages);
	}
		
	public static void testBasicReceiveAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		receiver.complete(receivedMessage.getLockToken());
		receivedMessage = receiver.receive(SHORT_WAIT_TIME);
		Assert.assertNull("Message was not properly completed", receivedMessage);
	}
	
	public static void testBasicReceiveAndCompleteMessageWithProperties(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		HashMap<String, Object> messageProps = new HashMap<>();
		messageProps.put("key1", "value1");
		messageProps.put("key2", null); // Some customers are using it this way
		message.setProperties(messageProps);
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		Map<String, Object> receivedProps = receivedMessage.getProperties();
		Assert.assertEquals("All sent properties not recieved", "value1", receivedProps.get("key1"));
		Assert.assertNull("Property with null value not received",  receivedProps.get("key2"));
		receiver.complete(receivedMessage.getLockToken());
		receivedMessage = receiver.receive(SHORT_WAIT_TIME);
		Assert.assertNull("Message was not properly completed", receivedMessage);
	}
	
	public static void testBasicReceiveAndAbandon(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		long deliveryCount = receivedMessage.getDeliveryCount();		
		receiver.abandon(receivedMessage.getLockToken());
		receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("DeliveryCount not incremented", deliveryCount+1, receivedMessage.getDeliveryCount());
		receiver.complete(receivedMessage.getLockToken());
	}	
	
	public static void testBasicReceiveAndDeadLetter(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		String deadLetterReason = "java client deadletter test";
		receiver.deadLetter(receivedMessage.getLockToken(), deadLetterReason, null);
		receivedMessage = receiver.receive(SHORT_WAIT_TIME);
		Assert.assertNull("Message was not properly deadlettered", receivedMessage);
	}	
		
	public static void testBasicReceiveAndRenewLock(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		sender.send(message);
		
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		Instant oldLockedUntilTime = receivedMessage.getLockedUntilUtc();
		Thread.sleep(1000);
		Instant newLockedUntilUtc = receiver.renewMessageLock(receivedMessage);
		Assert.assertTrue("Lock not renewed. OldLockedUntilUtc:" + oldLockedUntilTime.toString() + ", newLockedUntilUtc:" + newLockedUntilUtc, newLockedUntilUtc.isAfter(oldLockedUntilTime));
		Assert.assertEquals("Renewed lockeduntil time not set in Message", newLockedUntilUtc, receivedMessage.getLockedUntilUtc());
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testBasicReceiveAndRenewLockBatch(IMessageSender sender, String sessionId, IMessageReceiver receiver, boolean isEntityPartitioned) throws InterruptedException, ServiceBusException, ExecutionException
	{		
		int numMessages = 2;
		if(isEntityPartitioned)
		{
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				sender.send(message);
			}
		}
		else
		{
			// Keep this batch send part as this test sendBatch API call
			List<Message> messages = new ArrayList<Message>();
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				messages.add(message);
			}
			sender.sendBatch(messages);
		}
				
		ArrayList<IMessage> totalReceivedMessages = new ArrayList<>();
		
		if(isEntityPartitioned && sessionId == null)
		{
			Collection<IMessage> receivedMessages = receiver.receiveBatch(1);
			Assert.assertTrue("Messages not received", receivedMessages != null && receivedMessages.size() > 0);
			totalReceivedMessages.addAll(receivedMessages);
		}
		else
		{
			Collection<IMessage> receivedMessages = receiver.receiveBatch(numMessages);
			while(receivedMessages != null && receivedMessages.size() > 0 && totalReceivedMessages.size() < numMessages)
			{
			    totalReceivedMessages.addAll(receivedMessages);
				receivedMessages = receiver.receiveBatch(numMessages);
			}
			Assert.assertEquals("All messages not received", numMessages, totalReceivedMessages.size());
		}
		
		ArrayList<Instant> oldLockTimes = new ArrayList<Instant>();
		for(IMessage message : totalReceivedMessages)
		{
			oldLockTimes.add(message.getLockedUntilUtc());
		}
		
		Thread.sleep(1000);
		Collection<Instant> newLockTimes = ((MessageReceiver)receiver).renewMessageLockBatch(totalReceivedMessages);
		Assert.assertEquals("RenewLock didn't return one instant per message in the collection", totalReceivedMessages.size(), newLockTimes.size());
		Iterator<Instant> newLockTimeIterator = newLockTimes.iterator();
		Iterator<Instant> oldLockTimeIterator = oldLockTimes.iterator();
		for(IMessage message : totalReceivedMessages)
		{	
			Instant oldLockTime = oldLockTimeIterator.next();
			Instant newLockTime = newLockTimeIterator.next();
			Assert.assertTrue("Lock not renewed. OldLockedUntilUtc:" + oldLockTime.toString() + ", newLockedUntilUtc:" + newLockTime.toString(), newLockTime.isAfter(oldLockTime));
			Assert.assertEquals("Renewed lockeduntil time not set in Message", newLockTime, message.getLockedUntilUtc());
			receiver.complete(message.getLockToken());			
		}		
	}
		
	public static void testBasicReceiveBatchAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver, boolean isEntityPartitioned) throws InterruptedException, ServiceBusException, ExecutionException
	{
		int numMessages = 10;		
		if(isEntityPartitioned)
		{
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				sender.send(message);
			}
		}
		else
		{
			// Keep this batch send part as this test sendBatch API call
			List<Message> messages = new ArrayList<Message>();
			for(int i=0; i<numMessages; i++)
			{
				Message message = new Message("AMQP message");
				if(sessionId != null)
				{
					message.setSessionId(sessionId);
				}
				messages.add(message);
			}
			sender.sendBatch(messages);
		}
				
		int totalMessagesReceived = 0;
		Collection<IMessage> receivedMessages = receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalMessagesReceived += receivedMessages.size();
			for(IMessage message : receivedMessages)
			{
				//System.out.println(message.getLockToken());
				receiver.complete(message.getLockToken());
			}
			receivedMessages = receiver.receiveBatch(numMessages);
		}
		Assert.assertEquals("All messages not received", numMessages, totalMessagesReceived);		
		
		receivedMessages = receiver.receiveBatch(numMessages, SHORT_WAIT_TIME);
		Assert.assertNull("Messages received again", receivedMessages);
	}
		
	public static void testSendSceduledMessageAndReceive(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		int secondsToWaitBeforeScheduling = 30;
		String msgId1 = UUID.randomUUID().toString();
		String msgId2 = UUID.randomUUID().toString();
		Message message1 = new Message("AMQP Scheduled message");
		message1.setMessageId(msgId1);		
		Message message2 = new Message("AMQP Scheduled message2");
		message2.setMessageId(msgId2);
		if(sessionId != null)
		{
			message1.setSessionId(sessionId);
			message2.setSessionId(sessionId);
		}
		
		sender.scheduleMessage(message1, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		sender.scheduleMessage(message2, Instant.now().plusSeconds(secondsToWaitBeforeScheduling));
		if(sessionId == null)
		{
			Thread.sleep(secondsToWaitBeforeScheduling * 1000 * 2);
		}
		else
		{
			Thread.sleep(secondsToWaitBeforeScheduling * 1000);
			((IMessageSession)receiver).renewSessionLock();
			Thread.sleep(secondsToWaitBeforeScheduling * 1000);
		}
		
		Collection<IMessage> allReceivedMessages = new LinkedList<IMessage>();
		Collection<IMessage> receivedMessages = receiver.receiveBatch(10);
				
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			allReceivedMessages.addAll(receivedMessages);
			receivedMessages = receiver.receiveBatch(10);
		}
		
		boolean firstMessageReceived = false;
		boolean secondMessageReceived = false;
		for(IMessage message : allReceivedMessages)
		{
			if(message.getMessageId().equals(msgId1))
				firstMessageReceived = true;
			else if(message.getMessageId().equals(msgId2))
				secondMessageReceived = true;
		}
		
		Assert.assertTrue("Scheduled messages not received", firstMessageReceived && secondMessageReceived);
	}
		
	public static void testSendSceduledMessageAndCancel(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		int secondsToWaitBeforeScheduling = 30;
		String msgId1 = UUID.randomUUID().toString();
		String msgId2 = UUID.randomUUID().toString();
		Message message1 = new Message("AMQP Scheduled message");
		Message message2 = new Message("AMQP Scheduled message2");
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
		if(sessionId == null)
		{
			Thread.sleep(secondsToWaitBeforeScheduling * 1000 * 2);
		}
		else
		{
			Thread.sleep(secondsToWaitBeforeScheduling * 1000);
			((IMessageSession)receiver).renewSessionLock();
			Thread.sleep(secondsToWaitBeforeScheduling * 1000);
		}
		
		Collection<IMessage> allReceivedMessages = new LinkedList<IMessage>();
		Collection<IMessage> receivedMessages = receiver.receiveBatch(10);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			allReceivedMessages.addAll(receivedMessages);
			receivedMessages = receiver.receiveBatch(10);
		}
		
		Assert.assertTrue("Scheduled messages not received", allReceivedMessages.removeIf(msg -> msg.getMessageId().equals(msgId1)));
		Assert.assertFalse("Cancelled scheduled messages also received", allReceivedMessages.removeIf(msg -> msg.getMessageId().equals(msgId2)));
	}
		
	public static void testPeekMessage(IMessageSender sender, String sessionId, IMessageBrowser browser) throws InterruptedException, ServiceBusException
	{
		Message message = new Message("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		message = new Message("AMQP Scheduled message2");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		Thread.sleep(5000);
		IMessage peekedMessage1 = browser.peek();
		long firstMessageSequenceNumber = peekedMessage1.getSequenceNumber();
		IMessage peekedMessage2 = browser.peek();
		Assert.assertNotEquals("Peek returned the same message again.", firstMessageSequenceNumber, peekedMessage2.getSequenceNumber());		
		
		// Now peek with fromSequnceNumber.. May not work for partitioned entities
		IMessage peekedMessage5 = browser.peek(firstMessageSequenceNumber);
		Assert.assertEquals("Peek with sequence number failed.", firstMessageSequenceNumber, peekedMessage5.getSequenceNumber());
	}
		
	public static void testPeekMessageBatch(IMessageSender sender, String sessionId, IMessageBrowser browser, boolean isEntityPartitioned) throws InterruptedException, ServiceBusException
	{
		String partitionKey = "pkey1";
		Message message = new Message("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		else
		{
			if(isEntityPartitioned)
			{
				message.setPartitionKey(partitionKey);
			}
		}
		sender.send(message);
		message = new Message("AMQP Scheduled message2");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		else
		{
			if(isEntityPartitioned)
			{
				message.setPartitionKey(partitionKey);
			}
		}
		sender.send(message);
		Thread.sleep(5000);
		Collection<IMessage> peekedMessages = browser.peekBatch(10);
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
		Collection<IMessage> peekedMessagesBatch2 = browser.peekBatch(firstMessageSequenceNumber, 10);
		Assert.assertEquals("PeekBatch with sequence number didnot return all messages.", 2, peekedMessagesBatch2.size());
		Assert.assertEquals("PeekBatch with sequence number failed.", firstMessageSequenceNumber, peekedMessagesBatch2.iterator().next().getSequenceNumber());
	}
		
	public static void testReceiveBySequenceNumberAndComplete(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{			
		Message message = new Message("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());
		
		// Now receive by sequence number
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());		
		receiver.complete(receivedMessage.getLockToken());
		
		// Try to receive by sequence number again
		try
		{
			receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
			Assert.fail("Message recieved by sequnce number was not properly completed.");
		}
		catch(MessageNotFoundException e)
		{
			// Expected
		}		
	}
		
	public static void testReceiveBySequenceNumberAndAbandon(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{			
		Message message = new Message("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());
		
		// Now receive by sequence number
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		long deliveryCount = receivedMessage.getDeliveryCount();
		receiver.abandon(receivedMessage.getLockToken());
		
		// Try to receive by sequence number again
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("Abandon didn't increase the delivery count for the message received by sequence number.", deliveryCount + 1, receivedMessage.getDeliveryCount());
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testReceiveBySequenceNumberAndDefer(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		// Use longer strings with each defer to avoid an assert check in debug builds of service
		String phaseKey = "phase";
		String initialPhase = "undeferred";
		String firstDeferredPhase = "deferred first time";
		String secondDeferredPhase = "deferred first time and second time";
		
		Message sentMessage = new Message("AMQP message");
		HashMap customProperties = new HashMap();
		customProperties.put(phaseKey, initialPhase);
		sentMessage.setProperties(customProperties);
		if(sessionId != null)
		{
			sentMessage.setSessionId(sessionId);			
		}
		sender.send(sentMessage);
				
		IMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		customProperties.put(phaseKey, firstDeferredPhase);
		receiver.defer(receivedMessage.getLockToken(), customProperties);
		
		// Now receive by sequence number
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		Assert.assertEquals("Defer didn't update properties of the message received by sequence number", firstDeferredPhase, receivedMessage.getProperties().get(phaseKey));
		customProperties.put(phaseKey, secondDeferredPhase);
		receiver.defer(receivedMessage.getLockToken(), customProperties);
		
		// Try to receive by sequence number again
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message after deferrring", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("Defer didn't update properties of the message received by sequence number", secondDeferredPhase, receivedMessage.getProperties().get(phaseKey));
		receiver.complete(receivedMessage.getLockToken());
	}
		
	public static void testReceiveBySequenceNumberAndDeadletter(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{			
		Message message = new Message("AMQP Scheduled message");
		if(sessionId != null)
		{
			message.setSessionId(sessionId);			
		}
		sender.send(message);
		
		IMessage receivedMessage = receiver.receive();
		long sequenceNumber = receivedMessage.getSequenceNumber();
		String messageId = receivedMessage.getMessageId();
		receiver.defer(receivedMessage.getLockToken());		
		
		// Now receive by sequence number
		receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", sequenceNumber, receivedMessage.getSequenceNumber());
		Assert.assertEquals("ReceiveBySequenceNumber didn't receive the right message.", messageId, receivedMessage.getMessageId());
		String deadLetterReason = "java client deadletter test";		
		receiver.deadLetter(receivedMessage.getLockToken(), deadLetterReason, null);
				
		// Try to receive by sequence number again
		try
		{
			receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
			Assert.fail("Message received by sequence number was not properly deadlettered");
		}
		catch(MessageNotFoundException e)
		{
			// Expected
		}
	}
	
	public static void testSendReceiveMessageWithVariousPropertyTypes(IMessageSender sender, String sessionId, IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		Map<String, Object> sentProperties = new HashMap<>();
		sentProperties.put("NullProperty", null);
		sentProperties.put("BooleanProperty", true);
		sentProperties.put("ByteProperty", (byte)1);
		sentProperties.put("ShortProperty", (short)2);
		sentProperties.put("IntProperty", 3);
		sentProperties.put("LongProperty", 4l);
		sentProperties.put("FloatProperty", 5.5f);
		sentProperties.put("DoubleProperty", 6.6f);
		sentProperties.put("CharProperty", 'z');
		sentProperties.put("UUIDProperty", UUID.randomUUID());
		sentProperties.put("StringProperty", "string");
		
//		These additional types are not supported in message properties by Azure Service Bus
//		sentProperties.put("ArrayProperty", new Object[]{1, 2, 3, 4, 5});
//		List<Integer> list = new ArrayList<>();
//		list.add(10);
//		list.add(11);
//		sentProperties.put("ListProperty", list);
//		Map<String, Object> map = new HashMap<>();
//		map.put("key1", 20);
//		map.put("key2", "thirty");
//		sentProperties.put("MapProperty", map);
		
		String messageId = UUID.randomUUID().toString();
		Message message = new Message("AMQP message");
		message.setMessageId(messageId);
		if(sessionId != null)
		{
			message.setSessionId(sessionId);
		}
		message.getProperties().putAll(sentProperties);
		sender.send(message);
				
		IMessage receivedMessage = receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		Map<String, Object> receivedProperties = receivedMessage.getProperties();
		for(Map.Entry<String, Object> sentEntry : sentProperties.entrySet())
		{
			if(sentEntry.getValue() != null && sentEntry.getValue().getClass().isArray())
			{
				Assert.assertArrayEquals("Sent property didn't match with received property", (Object[])sentEntry.getValue(), (Object[])receivedProperties.get(sentEntry.getKey()));
			}
			else
			{
				Assert.assertEquals("Sent property didn't match with received property", sentEntry.getValue(), receivedProperties.get(sentEntry.getKey()));
			}
		}
	}
		
	public static void testGetMessageSessions(IMessageSender sender, Object sessionsClient) throws InterruptedException, ServiceBusException
	{		
		int numSessions = 110; // More than default page size
		String[] sessionIds = new String[numSessions];
		for(int i=0; i<numSessions; i++)
		{
			sessionIds[i] = StringUtil.getRandomString();
			Message message = new Message("AMQP message");
			message.setSessionId(sessionIds[i]);
			sender.send(message);
		}
		
		Collection<? extends IMessageSession> sessions;
		if(sessionsClient instanceof QueueClient)
		    sessions = Utils.completeFuture(((QueueClient)sessionsClient).getMessageSessionsAsync());
		else
		    sessions = Utils.completeFuture(((SubscriptionClient)sessionsClient).getMessageSessionsAsync());
		
		Assert.assertTrue("GetMessageSessions didnot return all sessions", numSessions <= sessions.size()); // There could be sessions left over from other tests
		
		IMessageSession anySession = (IMessageSession)sessions.toArray()[0];
		try{
			anySession.receive();
			Assert.fail("Browsable session should not support receive operation");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
		
		try{
			anySession.setState(null);
			Assert.fail("Browsable session should not support setstate operation");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
		
		// shouldn't throw an exception
		byte[] sessionState = anySession.getState();
		
		IMessage peekedMessage = anySession.peek();
		Assert.assertNotNull("Peek on a browsable session failed.", peekedMessage);
		
		// Close all sessions
		for(IMessageSession session : sessions)
		{
			session.closeAsync();
		}
	}
	
	public static void drainAllMessagesFromReceiver(IMessageReceiver receiver) throws InterruptedException, ServiceBusException
	{
		drainAllMessagesFromReceiver(receiver, true);
	}
	
	public static void drainAllMessagesFromReceiver(IMessageReceiver receiver, boolean cleanDeferredMessages) throws InterruptedException, ServiceBusException
	{		
		final int batchSize = 10;		
		Collection<IMessage> messages = receiver.receiveBatch(batchSize, DRAIN_MESSAGES_WAIT_TIME);
		while(messages !=null && messages.size() > 0)
		{
			if(receiver.getReceiveMode() == ReceiveMode.PEEKLOCK)
			{
				for(IMessage message: messages)
				{
					receiver.complete(message.getLockToken());
				}
			}
			messages = receiver.receiveBatch(batchSize, DRAIN_MESSAGES_WAIT_TIME);
		}		
		
		if(cleanDeferredMessages)
		{
			IMessage peekedMessage;
			while((peekedMessage = receiver.peek()) != null)
			{
				try
				{
					IMessage message = receiver.receiveDeferredMessage(peekedMessage.getSequenceNumber());
					if(receiver.getReceiveMode() == ReceiveMode.PEEKLOCK)
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
	
	public static void drainAllMessages(String receivePath) throws InterruptedException, ServiceBusException
	{
		IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(TestUtils.getNamespaceEndpointURI(), receivePath, TestUtils.getClientSettings(), ReceiveMode.RECEIVEANDDELETE);
		TestCommons.drainAllMessagesFromReceiver(receiver);
		receiver.close();
	}
	
	public static void drainAllSessions(String receivePath, boolean isQueue) throws InterruptedException, ServiceBusException
	{
		int numParallelSessionDrains = 5;
		Collection<IMessageSession> browsableSessions;
		if(isQueue)
		{
			QueueClient qc = new QueueClient(TestUtils.getNamespaceEndpointURI(), receivePath, TestUtils.getClientSettings(), ReceiveMode.RECEIVEANDDELETE);
			browsableSessions = qc.getMessageSessions();
		}
		else
		{
			SubscriptionClient sc = new SubscriptionClient(TestUtils.getNamespaceEndpointURI(), receivePath, TestUtils.getClientSettings(), ReceiveMode.RECEIVEANDDELETE);
			browsableSessions = sc.getMessageSessions();
		}		
		
		if(browsableSessions != null && browsableSessions.size() > 0)
		{
			CompletableFuture[] drainFutures = new CompletableFuture[numParallelSessionDrains];
			int drainFutureIndex = 0;
			for(IMessageSession browsableSession : browsableSessions)
			{				
				CompletableFuture<Void> drainFuture = ClientFactory.acceptSessionFromEntityPathAsync
						(TestUtils.getNamespaceEndpointURI(), receivePath, browsableSession.getSessionId(), TestUtils.getClientSettings(), ReceiveMode.RECEIVEANDDELETE).thenAcceptAsync((session) -> {
							try {
								TestCommons.drainAllMessagesFromReceiver(session, false);
								session.setState(null);
								session.close();
							} catch (InterruptedException | ServiceBusException e) {								
								e.printStackTrace();
							}							
						});
				
				drainFutures[drainFutureIndex++] = drainFuture;
				if(drainFutureIndex == numParallelSessionDrains)
				{
					Utils.completeFuture(CompletableFuture.allOf(drainFutures));
					drainFutureIndex = 0;					
				}
			}
			
			if(drainFutureIndex > 0)
			{				
				Utils.completeFuture(CompletableFuture.allOf(Arrays.copyOf(drainFutures, drainFutureIndex)));
			}
		}	
	}
}
