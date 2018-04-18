package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

import org.junit.Assert;

public class MessageAndSessionPumpTests {
	private static final int DEFAULT_MAX_CONCURRENT_CALLS = 5;
	private static final int DEFAULT_MAX_CONCURRENT_SESSIONS = 5;
	private static final int DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION = 5;
	
	public static void testMessagePumpAutoComplete(IMessageSender sender, IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		int numMessages = 10;
		for(int i=0; i<numMessages; i++)
		{
			sender.send(new Message("AMQPMessage"));
		}
		boolean autoComplete = true;
		CountingMessageHandler messageHandler = new CountingMessageHandler(messagePump, !autoComplete, numMessages, false);		
		messagePump.registerMessageHandler(messageHandler, new MessageHandlerOptions(DEFAULT_MAX_CONCURRENT_CALLS, autoComplete, Duration.ofMinutes(10)));
		if(!messageHandler.getMessageCountDownLatch().await(2, TimeUnit.MINUTES))
		{
			Assert.assertEquals("All messages not pumped even after waiting for 2 minutes.", numMessages, numMessages - messageHandler.getMessageCountDownLatch().getCount());
		}
		
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= DEFAULT_MAX_CONCURRENT_CALLS);
		// So completes will pass before links are closed by teardown
		Thread.sleep(1000);
	}
	
	
	public static void testMessagePumpClientComplete(IMessageSender sender, IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		int numMessages = 10;
		for(int i=0; i<numMessages; i++)
		{
			sender.send(new Message("AMQPMessage"));
		}
		boolean autoComplete = false;
		CountingMessageHandler messageHandler = new CountingMessageHandler(messagePump, !autoComplete, numMessages, false);		
		messagePump.registerMessageHandler(messageHandler, new MessageHandlerOptions(DEFAULT_MAX_CONCURRENT_CALLS, autoComplete, Duration.ofMinutes(10)));
		if(!messageHandler.getMessageCountDownLatch().await(2, TimeUnit.MINUTES))
		{
			Assert.assertEquals("All messages not pumped even after waiting for 2 minutes.", numMessages, numMessages - messageHandler.getMessageCountDownLatch().getCount());
		}
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= DEFAULT_MAX_CONCURRENT_CALLS);
	}
	
	public static void testMessagePumpAbandonOnException(IMessageSender sender, IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		int numMessages = 10;
		for(int i=0; i<numMessages; i++)
		{
			sender.send(new Message("AMQPMessage"));
		}
		boolean autoComplete = false;
		CountingMessageHandler messageHandler = new CountingMessageHandler(messagePump, !autoComplete, numMessages, true);
		messagePump.registerMessageHandler(messageHandler, new MessageHandlerOptions(DEFAULT_MAX_CONCURRENT_CALLS, autoComplete, Duration.ofMinutes(10)));
		if(!messageHandler.getMessageCountDownLatch().await(4, TimeUnit.MINUTES))
		{
			Assert.assertEquals("All messages not pumped even after waiting for 4 minutes.", numMessages * 2, numMessages * 2 - messageHandler.getMessageCountDownLatch().getCount());
		}
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= DEFAULT_MAX_CONCURRENT_CALLS);
	}
	
	public static void testMessagePumpRenewLock(IMessageSender sender, IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		int numMessages = 5;
		for(int i=0; i<numMessages; i++)
		{
			sender.send(new Message("AMQPMessage"));
		}
		boolean autoComplete = true;
		int sleepMinutes = 1; // This should be less than message lock duration of the queue or subscription
		CountingMessageHandler messageHandler = new CountingMessageHandler(messagePump, !autoComplete, numMessages, false, Duration.ofMinutes(sleepMinutes));		
		messagePump.registerMessageHandler(messageHandler, new MessageHandlerOptions(numMessages, autoComplete, Duration.ofMinutes(10)));
		int waitMinutes = 2 * sleepMinutes;
		if(!messageHandler.getMessageCountDownLatch().await(waitMinutes, TimeUnit.MINUTES))
		{
			Assert.assertEquals("All messages not pumped even after waiting for " + waitMinutes + " minutes.", numMessages, numMessages - messageHandler.getMessageCountDownLatch().getCount());
		}
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", messageHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= numMessages);
		// So completes will pass before links are closed by teardown
		Thread.sleep(1000);
	}
	
	public static void testRegisterAnotherHandlerAfterMessageHandler(IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		CountingMessageHandler messageHandler = new CountingMessageHandler(messagePump, true, 1, false, Duration.ofMinutes(1));
		messagePump.registerMessageHandler(messageHandler);
		
		try
		{
			messagePump.registerMessageHandler(messageHandler);
			Assert.fail("Once a handler is already registered, another handle shouldn't be registered.");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
		
		try
		{
			messagePump.registerSessionHandler(new CountingSessionHandler(messagePump, true, 1, false, Duration.ofMinutes(1)));
			Assert.fail("Once a handler is already registered, another handle shouldn't be registered.");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
	}
	
	public static void testRegisterAnotherHandlerAfterSessionHandler(IMessageAndSessionPump messagePump) throws InterruptedException, ServiceBusException
	{
		CountingSessionHandler countingSessionHandler = new CountingSessionHandler(messagePump, true, 1, false, Duration.ofMinutes(1));
		messagePump.registerSessionHandler(countingSessionHandler);
		
		try
		{
			messagePump.registerSessionHandler(countingSessionHandler);
			Assert.fail("Once a handler is already registered, another handle shouldn't be registered.");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
		
		try
		{
			messagePump.registerMessageHandler(new CountingMessageHandler(messagePump, true, 1, false, Duration.ofMinutes(1)));
			Assert.fail("Once a handler is already registered, another handle shouldn't be registered.");
		}
		catch(UnsupportedOperationException e)
		{
			// Expected
		}
	}
	
	public static void testSessionPumpAutoCompleteWithOneConcurrentCallPerSession(IMessageSender sender, IMessageAndSessionPump sessionPump) throws InterruptedException, ServiceBusException
	{
		MessageAndSessionPumpTests.testSessionPumpAutoComplete(sender, sessionPump, 1);
	}
	
	public static void testSessionPumpAutoCompleteWithMultipleConcurrentCallsPerSession(IMessageSender sender, IMessageAndSessionPump sessionPump) throws InterruptedException, ServiceBusException
	{
		MessageAndSessionPumpTests.testSessionPumpAutoComplete(sender, sessionPump, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION);
	}
	
	private static void testSessionPumpAutoComplete(IMessageSender sender, IMessageAndSessionPump sessionPump, int maxConcurrentCallsPerSession) throws InterruptedException, ServiceBusException
	{
		int numSessions = 10;
		int numMessagePerSession = 10;
		ArrayList<String> sessionIds = new ArrayList<>();
		for(int i=0; i<numSessions; i++)
		{
			String sessionId = StringUtil.getRandomString();
			sessionIds.add(sessionId);
			for(int j=0; j<numMessagePerSession; j++)
			{
				Message message = new Message("AMQPMessage");
				message.setSessionId(sessionId);
				sender.send(message);
			}			
		}
		
		boolean autoComplete = true;
		CountingSessionHandler sessionHandler = new CountingSessionHandler(sessionPump, !autoComplete, numSessions * numMessagePerSession, false);
		sessionPump.registerSessionHandler(sessionHandler, new SessionHandlerOptions(DEFAULT_MAX_CONCURRENT_SESSIONS, maxConcurrentCallsPerSession, autoComplete, Duration.ofMinutes(10)));
		if(!sessionHandler.getMessageCountDownLatch().await(5, TimeUnit.MINUTES))
		{
			Assert.assertEquals("All messages not pumped even after waiting for 5 minutes.", numSessions * numMessagePerSession, numSessions * numMessagePerSession - sessionHandler.getMessageCountDownLatch().getCount());
		}
		
		Assert.assertTrue("All sessions not received by session pump", sessionHandler.getReceivedSessions().containsAll(sessionIds));
		
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= (DEFAULT_MAX_CONCURRENT_SESSIONS * maxConcurrentCallsPerSession));
		// So completes will pass before links are closed by teardown
		Thread.sleep(1000);
	}
	
	public static void testSessionPumpClientComplete(IMessageSender sender, IMessageAndSessionPump sessionPump) throws InterruptedException, ServiceBusException
	{
		int numSessions = 10;
		int numMessagePerSession = 10;
		ArrayList<String> sessionIds = new ArrayList<>();
		for(int i=0; i<numSessions; i++)
		{
			String sessionId = StringUtil.getRandomString();
			sessionIds.add(sessionId);
			for(int j=0; j<numMessagePerSession; j++)
			{
				Message message = new Message("AMQPMessage");
				message.setSessionId(sessionId);
				sender.send(message);
			}			
		}
		
		boolean autoComplete = false;
		CountingSessionHandler sessionHandler = new CountingSessionHandler(sessionPump, !autoComplete, numSessions * numMessagePerSession, false);
		sessionPump.registerSessionHandler(sessionHandler, new SessionHandlerOptions(DEFAULT_MAX_CONCURRENT_SESSIONS, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION, autoComplete, Duration.ofMinutes(10)));
		if(!sessionHandler.getMessageCountDownLatch().await(5, TimeUnit.MINUTES))
		{			
			Assert.assertEquals("All messages not pumped even after waiting for 5 minutes.", numSessions * numMessagePerSession, numSessions * numMessagePerSession - sessionHandler.getMessageCountDownLatch().getCount());
		}
		
		Assert.assertTrue("All sessions not received by session pump", sessionHandler.getReceivedSessions().containsAll(sessionIds));
		
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= (DEFAULT_MAX_CONCURRENT_SESSIONS * DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION));		
	}
	
	public static void testSessionPumpAbandonOnException(IMessageSender sender, IMessageAndSessionPump sessionPump) throws InterruptedException, ServiceBusException
	{
		int numSessions = 10;
		int numMessagePerSession = 10;
		ArrayList<String> sessionIds = new ArrayList<>();
		for(int i=0; i<numSessions; i++)
		{
			String sessionId = StringUtil.getRandomString();
			sessionIds.add(sessionId);
			for(int j=0; j<numMessagePerSession; j++)
			{
				Message message = new Message("AMQPMessage");
				message.setSessionId(sessionId);
				sender.send(message);
			}			
		}
		
		boolean autoComplete = true;
		CountingSessionHandler sessionHandler = new CountingSessionHandler(sessionPump, !autoComplete, numSessions * numMessagePerSession, true);
		sessionPump.registerSessionHandler(sessionHandler, new SessionHandlerOptions(DEFAULT_MAX_CONCURRENT_SESSIONS, DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION, autoComplete, Duration.ofMinutes(10)));
		if(!sessionHandler.getMessageCountDownLatch().await(5, TimeUnit.MINUTES))
		{			
			Assert.assertEquals("All messages not pumped even after waiting for 5 minutes.", 2 * numSessions * numMessagePerSession, 2 * numSessions * numMessagePerSession - sessionHandler.getMessageCountDownLatch().getCount());
		}
		
		Assert.assertTrue("All sessions not received by session pump", sessionHandler.getReceivedSessions().containsAll(sessionIds));
		
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= (DEFAULT_MAX_CONCURRENT_SESSIONS * DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION));
		// So completes will pass before links are closed by teardown
		Thread.sleep(1000);
	}
	
	public static void testSessionPumpRenewLock(IMessageSender sender, IMessageAndSessionPump sessionPump) throws InterruptedException, ServiceBusException
	{
		int numSessions = 5;
		int numMessagePerSession = 2;
		ArrayList<String> sessionIds = new ArrayList<>();
		for(int i=0; i<numSessions; i++)
		{
			String sessionId = StringUtil.getRandomString();
			sessionIds.add(sessionId);
			for(int j=0; j<numMessagePerSession; j++)
			{
				Message message = new Message("AMQPMessage");
				message.setSessionId(sessionId);
				sender.send(message);
			}			
		}
		
		boolean autoComplete = true;
		int sleepMinutes = 2; // This should be less than message lock duration of the queue or subscription
		CountingSessionHandler sessionHandler = new CountingSessionHandler(sessionPump, !autoComplete, numSessions * numMessagePerSession, false, Duration.ofMinutes(sleepMinutes));
		sessionPump.registerSessionHandler(sessionHandler, new SessionHandlerOptions(DEFAULT_MAX_CONCURRENT_SESSIONS, 1, autoComplete, Duration.ofMinutes(10)));
		int waitMinutes = 5 * sleepMinutes;
		if(!sessionHandler.getMessageCountDownLatch().await(waitMinutes, TimeUnit.MINUTES))
		{			
			Assert.assertEquals("All messages not pumped even after waiting for" + waitMinutes + " minutes.", numSessions * numMessagePerSession, numSessions * numMessagePerSession - sessionHandler.getMessageCountDownLatch().getCount());
		}
		
		Assert.assertTrue("All sessions not received by session pump", sessionHandler.getReceivedSessions().containsAll(sessionIds));
		
		Assert.assertTrue("OnMessage called by maximum of one concurrent thread.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() > 1);
		Assert.assertTrue("OnMessage called by more than maxconcurrentcalls threads.", sessionHandler.getMaxConcurrencyCounter().getMaxConcurrencyCount() <= (DEFAULT_MAX_CONCURRENT_SESSIONS * DEFAULT_MAX_CONCURRENT_CALLS_PER_SESSION));
		// So completes will pass before links are closed by teardown
		Thread.sleep(1000);
	}
	
	private static class CountingMessageHandler extends TestMessageHandler
	{
		private IMessageAndSessionPump messagePump;
		private boolean completeMessage;
		private CountDownLatch messageCountDownLatch;
		private boolean firstThrowException;
		private Duration sleepDuration;
		private MaxConcurrencyCounter maxConcurrencyCounter;
		
		CountingMessageHandler(IMessageAndSessionPump messagePump, boolean completeMessages, int messageCount, boolean firstThrowException)
		{
			this(messagePump, completeMessages, messageCount, firstThrowException, Duration.ZERO);
		}
		
		CountingMessageHandler(IMessageAndSessionPump messagePump, boolean completeMessages, int messageCount, boolean firstThrowException, Duration sleepDuration)
		{
			this.maxConcurrencyCounter = new MaxConcurrencyCounter(); 
			this.messagePump = messagePump;
			this.completeMessage = completeMessages;			
			this.firstThrowException = firstThrowException;
			this.sleepDuration = sleepDuration;
			
			if(firstThrowException)
			{
				this.messageCountDownLatch = new CountDownLatch(messageCount * 2);
			}
			else
			{
				this.messageCountDownLatch = new CountDownLatch(messageCount);
			}			
		}
		
		@Override
		public CompletableFuture<Void> onMessageAsync(IMessage message) {
			CompletableFuture<Void> countingFuture = CompletableFuture.runAsync(() -> {
				this.maxConcurrencyCounter.incrementCount();
				//System.out.println("Message Received - " + message.getMessageId() + " - delivery count:" + message.getDeliveryCount() + " - Thread:" + Thread.currentThread());				
				if(this.firstThrowException && message.getDeliveryCount() == 0)
				{
					this.messageCountDownLatch.countDown();
					this.maxConcurrencyCounter.decrementCount();
					throw new RuntimeException("Dummy exception to cause abandon");
				}
				
				if(!this.sleepDuration.isZero())
				{
					try {
						Thread.sleep(this.sleepDuration.toMillis());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					};
				}				
			});
			
			CompletableFuture<Void> completeFuture = countingFuture;
			if(this.completeMessage)
			{
				completeFuture = countingFuture.thenComposeAsync((v) -> this.messagePump.completeAsync(message.getLockToken()));
			}
			else
			{
				completeFuture = countingFuture;
			}
			
			return completeFuture.thenRunAsync(() -> {
				this.messageCountDownLatch.countDown();
				this.maxConcurrencyCounter.decrementCount();
				});
		}
		
		public CountDownLatch getMessageCountDownLatch()
		{
			return this.messageCountDownLatch;
		}
		
		public MaxConcurrencyCounter getMaxConcurrencyCounter()
		{
			return this.maxConcurrencyCounter;
		}
	}
	
	private static class CountingSessionHandler extends TestSessionHandler
	{
		private IMessageAndSessionPump sessionPump;
		private boolean completeMessage;
		private CountDownLatch messageCountDownLatch;
		private boolean firstThrowException;
		private Duration sleepDuration;
		private MaxConcurrencyCounter maxConcurrencyCounter;
		private Set<String> receivedSeesions;
		
		CountingSessionHandler(IMessageAndSessionPump sessionPump, boolean completeMessages, int totalMessageCount, boolean firstThrowException)
		{
			this(sessionPump, completeMessages, totalMessageCount, firstThrowException, Duration.ZERO);
		}
		
		CountingSessionHandler(IMessageAndSessionPump sessionPump, boolean completeMessages, int totalMessageCount, boolean firstThrowException, Duration sleepDuration)
		{
			this.maxConcurrencyCounter = new MaxConcurrencyCounter(); 
			this.sessionPump = sessionPump;
			this.completeMessage = completeMessages;			
			this.firstThrowException = firstThrowException;
			this.sleepDuration = sleepDuration;
			this.receivedSeesions = Collections.synchronizedSet(new HashSet<String>());
			if(firstThrowException)
			{
				this.messageCountDownLatch = new CountDownLatch(totalMessageCount * 2);
			}
			else
			{
				this.messageCountDownLatch = new CountDownLatch(totalMessageCount);
			}			
		}
		
		@Override
		public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message) {
			CompletableFuture<Void> countingFuture = CompletableFuture.runAsync(() -> {
				this.maxConcurrencyCounter.incrementCount();
				this.receivedSeesions.add(session.getSessionId());
				//System.out.println("SessionID:" + session.getSessionId() + " - Message Received - " + message.getMessageId() + " - delivery count:" + message.getDeliveryCount() + " - Thread:" + Thread.currentThread() + ":" + Instant.now());				
				if(this.firstThrowException && message.getDeliveryCount() == 0)
				{
					this.messageCountDownLatch.countDown();
					this.maxConcurrencyCounter.decrementCount();
					throw new RuntimeException("Dummy exception to cause abandon");
				}
				
				if(!this.sleepDuration.isZero())
				{
					try {
						Thread.sleep(this.sleepDuration.toMillis());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					};
				}				
			});
			
			CompletableFuture<Void> completeFuture = countingFuture;
			if(this.completeMessage)
			{
				completeFuture = countingFuture.thenComposeAsync((v) -> session.completeAsync(message.getLockToken()));
			}
			else
			{
				completeFuture = countingFuture;
			}
			
			return completeFuture.thenRunAsync(() -> {
				this.messageCountDownLatch.countDown();
				this.maxConcurrencyCounter.decrementCount();
				});
		}

		@Override
		public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session) {
			System.out.println("Session closed.-" + session.getSessionId() + ":" + Instant.now());
			return CompletableFuture.completedFuture(null);
		}
		
		public CountDownLatch getMessageCountDownLatch()
		{
			return this.messageCountDownLatch;
		}
		
		public MaxConcurrencyCounter getMaxConcurrencyCounter()
		{
			return this.maxConcurrencyCounter;
		}
		
		public Set<String> getReceivedSessions()
		{
			return this.receivedSeesions;
		}
	}
}
