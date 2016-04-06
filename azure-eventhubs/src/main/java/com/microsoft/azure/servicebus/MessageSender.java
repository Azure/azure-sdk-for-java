/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.IAmqpSender;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;
import com.microsoft.azure.servicebus.amqp.SessionHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;

import java.nio.BufferOverflowException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	private static final String SEND_TIMED_OUT = "Send operation timed out.";
	
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	private final Duration operationTimeout;
	private final RetryPolicy retryPolicy;
	private final Runnable operationTimer;
	private final Duration timerTimeout;
	
	private ConcurrentHashMap<byte[], ReplayableWorkItem<Void>> pendingSendWaiters;
	private ConcurrentLinkedQueue<byte[]> pendingSendsWaitingForCredit;
	
	private Sender sendLink;
	private CompletableFuture<MessageSender> linkFirstOpen; 
	private AtomicLong nextTag;
	private AtomicInteger linkCredit;
	private TimeoutTracker openLinkTracker;
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	private Object sendCall;
	
	public static CompletableFuture<MessageSender> create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath)
	{
		final MessageSender msgSender = new MessageSender(factory, sendLinkName, senderPath);
		msgSender.openLinkTracker = TimeoutTracker.create(factory.getOperationTimeout());
		msgSender.initializeLinkOpen(msgSender.openLinkTracker);
		msgSender.linkCreateScheduled = true;
		Timer.schedule(new Runnable()
		{
			@Override
			public void run()
			{
				msgSender.sendLink = msgSender.createSendLink();
			}
		}, Duration.ofSeconds(0), TimerType.OneTimeRun);
		return msgSender.linkFirstOpen;
	}
	
	private MessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName);
		this.sendPath = senderPath;
		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		this.timerTimeout = this.operationTimeout.getSeconds() > 9 ? this.operationTimeout.dividedBy(3) : Duration.ofSeconds(5);
		this.lastKnownLinkError = null;
		
		this.retryPolicy = factory.getRetryPolicy();
		
		this.pendingSendWaiters = new ConcurrentHashMap<byte[], ReplayableWorkItem<Void>>();
		this.pendingSendsWaitingForCredit = new ConcurrentLinkedQueue<byte[]>();
		this.nextTag = new AtomicLong(0);
		this.linkCredit = new AtomicInteger(0);
		 
		this.linkCreateLock = new Object();
		this.sendCall = new Object();
		
		this.operationTimer = new Runnable()
			{
				@Override
				public void run()
				{
					if (MessageSender.this.pendingSendWaiters != null)
					{
						Iterator<Entry<byte[], ReplayableWorkItem<Void>>> pendingDeliveries = MessageSender.this.pendingSendWaiters.entrySet().iterator();
						while(pendingDeliveries.hasNext())
						{
							Entry<byte[], ReplayableWorkItem<Void>> pendingSend = pendingDeliveries.next();
							if (pendingSend == null)
							{
								break;
							}
							
							ReplayableWorkItem<Void> pendingSendWork = pendingSend.getValue();
							if (pendingSendWork.getTimeoutTracker().remaining().compareTo(ClientConstants.TIMER_TOLERANCE) < 0)
							{
								pendingDeliveries.remove();
								MessageSender.this.throwSenderTimeout(pendingSendWork.getWork(), pendingSendWork.getLastKnownException());
							}
						}
					}
				}
			};
	}
	
	public String getSendPath()
	{
		return this.sendPath;
	}
	
	private CompletableFuture<Void> send(byte[] bytes, int arrayOffset, int messageFormat)
	{
		return this.send(bytes, arrayOffset, messageFormat, null, null);
	}
	
	private byte[] getNextDeliveryTag()
	{
		long nextDeliveryId = this.nextTag.incrementAndGet();
		byte[] nextDeliveryTag = new byte[Long.BYTES];
		
		for (int index = 0; index < Long.BYTES; index++)
		{
			nextDeliveryTag[index] = (byte) (nextDeliveryId >> (8 * (Long.BYTES - index - 1))); 	
		}
		
		return nextDeliveryTag;
	}
	
	// contract:
	// 1. actual send on the SenderLink should happen only in this method
	// 2. If there is any PendingSend waiting for Service to sendCreditFLow 
	//        - this will not Send - & only Enqueue's the message
	//  	  - except if the send msg is same as the one waiting for Credit
	private CompletableFuture<Void> send(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker,
			final byte[] deliveryTag)
	{
		if (tracker != null && onSend != null && (tracker.remaining().isNegative() || tracker.remaining().isZero()))
		{
			if (deliveryTag != null)
			{
				this.pendingSendWaiters.remove(deliveryTag);
			}
			
			MessageSender.this.throwSenderTimeout(onSend, null);
			return onSend;
		}
		
		byte[] tag = deliveryTag == null ? this.getNextDeliveryTag() : deliveryTag;
		boolean messageSent = false;
		
		if (this.sendLink.getLocalState() == EndpointState.CLOSED)
		{
			this.scheduleRecreate(Duration.ofMillis(1));
		}
		else if (this.linkCredit.get() > 0 && 
				(this.pendingSendsWaitingForCredit.isEmpty() || this.pendingSendsWaitingForCredit.peek() == deliveryTag))
        {
			synchronized (this.sendCall)
			{
				if (this.linkCredit.get() > 0 &&
						(this.pendingSendsWaitingForCredit.isEmpty() || this.pendingSendsWaitingForCredit.peek() == deliveryTag))
				{
					this.linkCredit.decrementAndGet();
					
		        	Delivery dlv = this.sendLink.delivery(tag);
		        	dlv.setMessageFormat(messageFormat);
	
			        int sentMsgSize = this.sendLink.send(bytes, 0, arrayOffset);
			        assert sentMsgSize == arrayOffset : "Contract of the ProtonJ library for Sender.Send API changed";
			        
			        this.sendLink.advance();
			        messageSent = true;
				}
			}
		}
		
		if (!messageSent)
		{
			this.pendingSendsWaitingForCredit.offer(tag);
		}
		
		CompletableFuture<Void> onSendFuture = (onSend == null) ? new CompletableFuture<Void>() : onSend; 
        this.pendingSendWaiters.put(
        		tag, 
        		tracker == null ?
        				new ReplayableWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, this.operationTimeout) : 
        				new ReplayableWorkItem<Void>(bytes, arrayOffset, messageFormat, onSendFuture, tracker));
		
        return onSendFuture;
	}
	
	private CompletableFuture<Void> send(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker)
	{
		return this.send(bytes, arrayOffset, messageFormat, onSend, tracker, null);
	}
	
	private int getPayloadSize(Message msg)
	{
		if (msg == null || msg.getBody() == null)
		{
			return 0;
		}
		
		Data payloadSection = (Data) msg.getBody();
		if (payloadSection == null)
		{
			return 0;
		}
		
		Binary payloadBytes = payloadSection.getValue();
		if (payloadBytes == null)
		{
			return 0;
		}
		
		return payloadBytes.getLength();
	}
	
	private int getDataSerializedSize(Message amqpMessage)
	{
		if (amqpMessage == null)
		{
			return 0;
		}
		
		int payloadSize = this.getPayloadSize(amqpMessage);
		
		// EventData - accepts only PartitionKey - which is a String & stuffed into MessageAnnotation
		MessageAnnotations msgAnnotations = amqpMessage.getMessageAnnotations();
		if (msgAnnotations == null)
		{
			return payloadSize;
		}
		
		int annotationsSize = 0;
		for(Object value: msgAnnotations.getValue().values())
		{
			annotationsSize += value.toString().length();
		}
		
		return annotationsSize + payloadSize;
	}
	
	public CompletableFuture<Void> send(final Iterable<Message> messages)
	{
		if (messages == null || IteratorUtil.sizeEquals(messages, 0))
		{
			throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
		}

		Message firstMessage = messages.iterator().next();			
		if (IteratorUtil.sizeEquals(messages, 1))
		{
			return this.send(firstMessage);
		}
		
		// proton-j doesn't support multiple dataSections to be part of AmqpMessage
		// here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
		Message batchMessage = Proton.message();
		batchMessage.setMessageAnnotations(firstMessage.getMessageAnnotations());
				
		byte[] bytes = new byte[ClientConstants.MAX_MESSAGE_LENGTH_BYTES];
		int encodedSize = batchMessage.encode(bytes, 0, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
		int byteArrayOffset = encodedSize;
		
		for(Message amqpMessage: messages)
		{
			Message messageWrappedByData = Proton.message();
			
			int payloadSize = this.getDataSerializedSize(amqpMessage);
			int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
			
			byte[] messageBytes = new byte[allocationSize];
			int messageSizeBytes = amqpMessage.encode(messageBytes, 0, allocationSize);
			messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));
			
			try
			{
				encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, ClientConstants.MAX_MESSAGE_LENGTH_BYTES - byteArrayOffset - 1);
			}
			catch(BufferOverflowException exception)
			{
				final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
				sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception));
				return sendTask;
			}
			
			byteArrayOffset = byteArrayOffset + encodedSize;
		}
		
		return this.send(bytes, byteArrayOffset, AmqpConstants.AMQP_BATCH_MESSAGE_FORMAT);
	}
	
	public CompletableFuture<Void> send(Message msg)
	{
		int payloadSize = this.getDataSerializedSize(msg);
		int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
		
		byte[] bytes = new byte[allocationSize];
		int encodedSize = 0;
		try
		{
			encodedSize = msg.encode(bytes, 0, allocationSize);
		}
		catch(BufferOverflowException exception)
		{
			final CompletableFuture<Void> sendTask = new CompletableFuture<Void>();
			sendTask.completeExceptionally(new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception));
			return sendTask;
		}
		
		return this.send(bytes, encodedSize, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
	}
	
	public void onOpenComplete(Exception completionException)
	{
		if (completionException == null)
		{
			this.openLinkTracker = null;
			this.retryPolicy.resetRetryCount(this.getClientId());
			
			this.lastKnownLinkError = null;
			
			if (!this.linkFirstOpen.isDone())
			{
				this.linkFirstOpen.complete(this);
				Timer.schedule(this.operationTimer, this.timerTimeout, TimerType.RepeatRun);
			}
			else if (!this.pendingSendWaiters.isEmpty())
			{
				ConcurrentHashMap<byte[], ReplayableWorkItem<Void>> unacknowledgedSends = new ConcurrentHashMap<>();
				unacknowledgedSends.putAll(this.pendingSendWaiters);

				if (unacknowledgedSends.size() > 0)
					unacknowledgedSends.forEachEntry(1, new Consumer<Map.Entry<byte[], ReplayableWorkItem<Void>>>()
					{
						@Override
						public void accept(Entry<byte[], ReplayableWorkItem<Void>> sendWork)
						{
							byte[] deliveryTag = sendWork.getKey();
							MessageSender.this.pendingSendsWaitingForCredit.remove(deliveryTag);
							MessageSender.this.reSend(deliveryTag, false);
						}
					});
				
				unacknowledgedSends.clear();
			}
		}
		else
		{		
			ExceptionUtil.completeExceptionally(this.linkFirstOpen, completionException, this);
		}
		
		synchronized(this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
	}
	
	@Override
	public void onClose(ErrorCondition condition)
	{
		// TODO: code-refactor pending - refer to issue: https://github.com/Azure/azure-event-hubs/issues/73
		Exception completionException = condition != null ? ExceptionUtil.toException(condition) 
				: new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT,
						"The entity has been close due to transient failures (underlying link closed), please retry the operation.");
		this.onError(completionException);
	}
	
	public void onError(Exception completionException)
	{
		Duration remainingTime = this.openLinkTracker == null 
						? this.operationTimeout
						: (this.openLinkTracker.elapsed().compareTo(this.operationTimeout) > 0) 
								? Duration.ofSeconds(0) 
								: this.operationTimeout.minus(this.openLinkTracker.elapsed());
		Duration retryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), completionException, remainingTime);
		
		if (completionException != null)
		{
			this.lastKnownLinkError = completionException;
		}
		
		if (retryInterval != null)
		{
			this.scheduleRecreate(retryInterval);			
			return;
		}
		
		this.onOpenComplete(completionException);
	}
	
	private void scheduleRecreate(Duration runAfter)
	{
		synchronized(this.linkCreateLock)
		{
			if (this.linkCreateScheduled)
			{
				return;
			}
			
			this.linkCreateScheduled = true;
		}
				
		Timer.schedule(
			new Runnable()
			{
				@Override
				public void run()
				{
					if (MessageSender.this.sendLink.getLocalState() != EndpointState.CLOSED)
					{
						return;
					}
					
					Sender sender = MessageSender.this.createSendLink();
					if (sender != null)
					{
						Sender oldSender = MessageSender.this.sendLink;
						MessageSender.this.underlyingFactory.deregisterForConnectionError(oldSender);
						
						MessageSender.this.sendLink = sender;
					}
					else
					{
						synchronized (MessageSender.this.linkCreateLock) 
						{
							MessageSender.this.linkCreateScheduled = false;
						}
					}
					
					MessageSender.this.retryPolicy.incrementRetryCount(MessageSender.this.getClientId());
				}
			},
			runAfter,
			TimerType.OneTimeRun);
	}
	
	public void onSendComplete(final byte[] deliveryTag, final DeliveryState outcome)
	{
		TRACE_LOGGER.log(Level.FINEST, String.format("linkName[%s]", this.sendLink.getName()));
		ReplayableWorkItem<Void> pendingSendWorkItem = this.pendingSendWaiters.get(deliveryTag);
        
		if (pendingSendWorkItem != null)
		{
			CompletableFuture<Void> pendingSend = pendingSendWorkItem.getWork();
			if (outcome instanceof Accepted)
			{
				this.retryPolicy.resetRetryCount(this.getClientId());
				this.pendingSendWaiters.remove(deliveryTag);
				pendingSend.complete(null);
			}
			else if (outcome instanceof Rejected)
			{
				Rejected rejected = (Rejected) outcome;
				ErrorCondition error = rejected.getError();
				Exception exception = ExceptionUtil.toException(error);

				Duration retryInterval = this.retryPolicy.getNextRetryInterval(
						this.getClientId(), exception, pendingSendWorkItem.getTimeoutTracker().remaining());
				if (retryInterval == null)
				{
					this.pendingSendWaiters.remove(deliveryTag);
					ExceptionUtil.completeExceptionally(pendingSend, exception, this);
				}
				else
				{
					pendingSendWorkItem.setLastKnownException(exception);
					Timer.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							MessageSender.this.reSend(deliveryTag, false);
						}
					}, retryInterval, TimerType.OneTimeRun);
				}
			}
			else 
			{
				this.pendingSendWaiters.remove(deliveryTag);
				ExceptionUtil.completeExceptionally(pendingSend, new ServiceBusException(false, outcome.toString()), this);
			}
		}
	}

	private void reSend(final byte[] deliveryTag, boolean reuseDeliveryTag)
	{
		ReplayableWorkItem<Void> pendingSend = this.pendingSendWaiters.remove(deliveryTag);

		if (pendingSend != null)
		{
			this.send(pendingSend.getMessage(), 
					pendingSend.getEncodedMessageSize(), 
					pendingSend.getMessageFormat(),
					pendingSend.getWork(),
					pendingSend.getTimeoutTracker(),
					reuseDeliveryTag ? deliveryTag : null);
		}
	}
	
	private Sender createSendLink()
	{
		Connection connection = null;
		try
		{
			connection = this.underlyingFactory.getConnection().get(this.operationTimeout.getSeconds(), TimeUnit.SECONDS);
		} 
		catch (InterruptedException|ExecutionException exception)
		{
			Throwable throwable = exception.getCause();
			
			if (throwable != null && throwable instanceof Exception)
			{
				this.onError((Exception) throwable);
			}
			
			if (exception instanceof InterruptedException)
			{
				Thread.currentThread().interrupt();
			}
			
			return null;
		}
		catch (TimeoutException exception)
        {
        	this.onError(new ServiceBusException(false, "Connection creation timed out.", exception));
        	return null;
        }
		
		if (connection == null || connection.getLocalState() == EndpointState.CLOSED)
        {
        	return null;
        }
		
		Session session = connection.session();
		session.setOutgoingWindow(Integer.MAX_VALUE);
        session.open();
        BaseHandler.setHandler(session, new SessionHandler(this.sendPath));
        
        String sendLinkName = StringUtil.getRandomString();
        sendLinkName = sendLinkName.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer());
        Sender sender = session.sender(sendLinkName);
        
        Target target = new Target();
        target.setAddress(this.sendPath);
        sender.setTarget(target);
        
        Source source = new Source();
        sender.setSource(source);
        
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        
        SendLinkHandler handler = new SendLinkHandler(this);
        BaseHandler.setHandler(sender, handler);
		
        this.underlyingFactory.registerForConnectionError(sender);
		
        sender.open();
        return sender;
	}
	
	// TODO: consolidate common-code written for timeouts in Sender/Receiver
	private void initializeLinkOpen(TimeoutTracker timeout)
	{
		this.linkFirstOpen = new CompletableFuture<MessageSender>();
		
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						if (!MessageSender.this.linkFirstOpen.isDone())
						{
							Exception operationTimedout = new ServiceBusException(true,
									String.format(Locale.US, "SendLink(%s).open() on Entity(%s) timed out",
											MessageSender.this.sendLink.getName(), MessageSender.this.getSendPath()));

							if (TRACE_LOGGER.isLoggable(Level.WARNING))
							{
								TRACE_LOGGER.log(Level.WARNING, 
										String.format(Locale.US, "message Sender(linkName: %s, path: %s) open call timedout", MessageSender.this.getClientId(), MessageSender.this.sendPath), 
										operationTimedout);
							}
							
							ExceptionUtil.completeExceptionally(MessageSender.this.linkFirstOpen, operationTimedout, MessageSender.this);
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}

	@Override
	public CompletableFuture<Void> close()
	{
		if (this.sendLink != null && this.sendLink.getLocalState() != EndpointState.CLOSED)
		{
			this.sendLink.close();
		}
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public ErrorContext getContext()
	{
		final boolean isLinkOpened = this.linkFirstOpen != null && this.linkFirstOpen.isDone();
		final String referenceId = this.sendLink != null && this.sendLink.getRemoteProperties() != null && this.sendLink.getRemoteProperties().containsKey(ClientConstants.TRACKING_ID_PROPERTY)
				? this.sendLink.getRemoteProperties().get(ClientConstants.TRACKING_ID_PROPERTY).toString()
				: ((this.sendLink != null) ? this.sendLink.getName(): null);
		
		SenderContext errorContext = new SenderContext(
				this.underlyingFactory!=null ? this.underlyingFactory.getHostName() : null,
				this.sendPath,
				referenceId,
				isLinkOpened ? this.sendLink.getCredit() : null);
		return errorContext;
	}

	@Override
	public void onFlow(int credit)
	{
		this.linkCredit.set(credit);
		
		while (!this.pendingSendsWaitingForCredit.isEmpty() && this.linkCredit.get() > 0)
		{
			byte[] deliveryTag = this.pendingSendsWaitingForCredit.peek();
			if (deliveryTag != null)
			{
				this.reSend(deliveryTag, true);
				this.pendingSendsWaitingForCredit.poll();
			}
		}
	}
	
	private void throwSenderTimeout(CompletableFuture<Void> pendingSendWork, Exception lastKnownException)
	{
		Exception cause = lastKnownException == null ? this.lastKnownLinkError : lastKnownException;
		ServiceBusException exception = new ServiceBusException(
				cause != null && cause instanceof ServiceBusException ? ((ServiceBusException) cause).getIsTransient() : ClientConstants.DEFAULT_IS_TRANSIENT, 
				MessageSender.SEND_TIMED_OUT, cause);
		ExceptionUtil.completeExceptionally(pendingSendWork, exception, this);
	}
}
