/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.nio.BufferOverflowException;
import java.time.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.Timer;
import com.microsoft.azure.servicebus.amqp.*;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity implements IAmqpSender, IErrorContextProvider
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	private final Duration operationTimeout;
	private final RetryPolicy retryPolicy;
	private final Runnable operationTimer;
	private final Duration timerTimeout;
	
	private ConcurrentHashMap<byte[], ReplayableWorkItem<Void>> pendingSendWaiters;
	private Sender sendLink;
	private CompletableFuture<MessageSender> linkFirstOpen; 
	private AtomicLong nextTag;
	private TimeoutTracker openLinkTracker;
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	private Exception lastKnownLinkError;
	
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
		this.nextTag = new AtomicLong(0);
		 
		this.linkCreateLock = new Object();
		
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
								Exception cause = pendingSendWork.getLastKnownException() == null 
										? MessageSender.this.lastKnownLinkError : pendingSendWork.getLastKnownException();
								ServiceBusException exception = new ServiceBusException(
										cause != null && cause instanceof ServiceBusException ? ((ServiceBusException) cause).getIsTransient() : ClientConstants.DEFAULT_IS_TRANSIENT, 
										String.format(Locale.US, "Send operation timed out."
											, MessageSender.this.getSendPath()
											, MessageSender.this.sendLink.getName()),
										cause);
								ExceptionUtil.completeExceptionally(pendingSendWork.getWork(), exception, MessageSender.this);
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
	
	private CompletableFuture<Void> send(
			final byte[] bytes,
			final int arrayOffset,
			final int messageFormat,
			final CompletableFuture<Void> onSend,
			final TimeoutTracker tracker)
	{
		byte[] tag = String.valueOf(this.nextTag.incrementAndGet()).getBytes();
		
		if (this.sendLink.getLocalState() == EndpointState.CLOSED)
		{
			this.scheduleRecreate(Duration.ofMillis(1));
		}		
		else
        {
        	Delivery dlv = this.sendLink.delivery(tag);
        	dlv.setMessageFormat(messageFormat);

	        int sentMsgSize = this.sendLink.send(bytes, 0, arrayOffset);
	        assert sentMsgSize != arrayOffset : "Contract of the ProtonJ library for Sender.Send API changed";
	        
	        this.sendLink.advance();
        }
        
		CompletableFuture<Void> onSendFuture = (onSend == null) ? new CompletableFuture<Void>() : onSend; 
        this.pendingSendWaiters.put(
        		tag, 
        		new ReplayableWorkItem<Void>(
        				bytes, 
        				arrayOffset, 
        				messageFormat, 
        				onSendFuture, 
        				tracker == null ? this.operationTimeout : tracker.remaining()));
		
        return onSendFuture;
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
			
			byte[] messageBytes = new byte[ClientConstants.MAX_MESSAGE_LENGTH_BYTES];
			int messageSizeBytes = amqpMessage.encode(messageBytes, 0, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
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
		byte[] bytes = new byte[ClientConstants.MAX_MESSAGE_LENGTH_BYTES];
		int encodedSize = 0;
		try
		{
			encodedSize = msg.encode(bytes, 0, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
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
							ReplayableWorkItem<Void> pendingSend = MessageSender.this.pendingSendWaiters.remove(sendWork.getKey());
							if (pendingSend != null)
							{
								MessageSender.this.send(pendingSend.getMessage(), 
										pendingSend.getEncodedMessageSize(),
										pendingSend.getMessageFormat(),
										pendingSend.getWork(),
										pendingSend.getTimeoutTracker());
							}
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
		TRACE_LOGGER.log(Level.FINE, String.format("linkName[%s]", this.sendLink.getName()));
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
							MessageSender.this.reSend(deliveryTag);
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

	private void reSend(Object deliveryTag)
	{
		ReplayableWorkItem<Void> pendingSend = this.pendingSendWaiters.remove(deliveryTag);
		if (pendingSend != null)
		{
			this.send(pendingSend.getMessage(), 
					pendingSend.getEncodedMessageSize(), 
					pendingSend.getMessageFormat(),
					pendingSend.getWork(),
					pendingSend.getTimeoutTracker());
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
}
