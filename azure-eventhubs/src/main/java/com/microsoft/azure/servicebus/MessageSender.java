package com.microsoft.azure.servicebus;

import java.nio.BufferOverflowException;
import java.time.*;
import java.util.*;
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
import org.omg.PortableInterceptor.ACTIVE;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.Timer;
import com.microsoft.azure.servicebus.amqp.*;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	public static final int MaxMessageLength = 256 * 1024;
	
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	private final Duration operationTimeout;
	private final RetryPolicy retryPolicy;
	
	private ConcurrentHashMap<byte[], ReplayableWorkItem<Void>> pendingSendWaiters;
	private Sender sendLink;
	private CompletableFuture<MessageSender> linkOpen; 
	private AtomicLong nextTag;
	private TimeoutTracker currentOperationTracker;
	private boolean linkCreateScheduled;
	private Object linkCreateLock;
	
	public static CompletableFuture<MessageSender> Create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath)
	{
		MessageSender msgSender = new MessageSender(factory, sendLinkName, senderPath);
		SendLinkHandler handler = new SendLinkHandler(msgSender);
		BaseHandler.setHandler(msgSender.sendLink, handler);
		return msgSender.linkOpen;
	}
	
	private MessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName);
		this.sendPath = senderPath;
		this.underlyingFactory = factory;
		this.operationTimeout = factory.getOperationTimeout();
		
		// clone ?
		this.retryPolicy = factory.getRetryPolicy();
		this.sendLink = this.createSendLink();
		this.currentOperationTracker = TimeoutTracker.create(factory.getOperationTimeout());
		this.initializeLinkOpen(this.currentOperationTracker);
		this.linkCreateScheduled = true;
		
		this.pendingSendWaiters = new ConcurrentHashMap<byte[], ReplayableWorkItem<Void>>();
		this.nextTag = new AtomicLong(0);
		 
		this.linkCreateLock = new Object();
	}
	
	public String getSendPath()
	{
		return this.sendPath;
	}
	
	public CompletableFuture<Void> send(Message msg, int messageFormat) throws PayloadSizeExceededException
	{
		if (this.sendLink.getLocalState() != EndpointState.ACTIVE)
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}
		
		// TODO: fix allocation per call - use BufferPool
		byte[] bytes = new byte[MaxMessageLength];
		int encodedSize;
		
		try
		{
			encodedSize = msg.encode(bytes, 0, MaxMessageLength);
		}
		catch(BufferOverflowException exception)
		{
			throw new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s", MaxMessageLength), exception);
		}
		
		byte[] tag = String.valueOf(nextTag.incrementAndGet()).getBytes();
        Delivery dlv = this.sendLink.delivery(tag);
        dlv.setMessageFormat(messageFormat);
        
        int sentMsgSize = this.sendLink.send(bytes, 0, encodedSize);
        assert sentMsgSize != encodedSize : "Contract of the ProtonJ library for Sender.Send API changed";
        
        CompletableFuture<Void> onSend = new CompletableFuture<Void>();
        
        synchronized (this.pendingSendWaiters)
        {
        	this.pendingSendWaiters.put(tag, new ReplayableWorkItem<Void>(bytes, sentMsgSize, messageFormat, onSend, this.operationTimeout));
		}
        
        this.sendLink.advance();
        return onSend;
	}
	
	// accepts even if PartitionKey is null - and hence, the layer above this api is supposed to enforce
	public CompletableFuture<Void> send(final Iterable<Message> messages, final String partitionKey)
		throws ServiceBusException
	{
		if (messages == null || IteratorUtil.sizeEquals(messages.iterator(), 0))
		{
			throw new IllegalArgumentException("Sending Empty batch of messages is not allowed.");
		}
		
		if (IteratorUtil.sizeEquals(messages.iterator(), 1))
		{
			Message firstMessage = messages.iterator().next();			
			return this.send(firstMessage);
		}
		
		if (this.sendLink.getLocalState() != EndpointState.ACTIVE)
		{
			this.scheduleRecreate(Duration.ofSeconds(0));
		}
		
		// proton-j doesn't support multiple dataSections to be part of AmqpMessage
		// here's the alternate approach provided by them: https://github.com/apache/qpid-proton/pull/54
		Message batchMessage = Proton.message();
		MessageAnnotations messageAnnotations = batchMessage.getMessageAnnotations() == null ? new MessageAnnotations(new HashMap<Symbol, Object>()) 
				: batchMessage.getMessageAnnotations();
		messageAnnotations.getValue().put(AmqpConstants.PartitionKey, partitionKey);
		batchMessage.setMessageAnnotations(messageAnnotations);
		
		// TODO: fix allocation per call - use BufferPool
		byte[] bytes = new byte[MaxMessageLength];
		int encodedSize = batchMessage.encode(bytes, 0, MaxMessageLength);
		int byteArrayOffset = encodedSize;
		
		byte[] tag = String.valueOf(nextTag.incrementAndGet()).getBytes();
        Delivery dlv = this.sendLink.delivery(tag);
        dlv.setMessageFormat(AmqpConstants.AmqpBatchMessageFormat);
        
		for(Message amqpMessage: messages)
		{
			Message messageWrappedByData = Proton.message();
			
			// TODO: essential optimization
			byte[] messageBytes = new byte[MaxMessageLength];
			int messageSizeBytes = amqpMessage.encode(messageBytes, 0, MaxMessageLength);
			messageWrappedByData.setBody(new Data(new Binary(messageBytes, 0, messageSizeBytes)));
			
			try
			{
				encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, MaxMessageLength - byteArrayOffset - 1);
			}
			catch(BufferOverflowException exception)
			{
				// TODO: is it intended for this purpose - else compute msg. size before hand.
				dlv.clear();
				throw new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s", MaxMessageLength), exception);
			}
			
			byteArrayOffset = byteArrayOffset + encodedSize;
		}
		
		int sentMsgSize = this.sendLink.send(bytes, 0, byteArrayOffset);
		assert sentMsgSize != byteArrayOffset : "Contract of the ProtonJ library for Sender.Send API changed";
        
		CompletableFuture<Void> onSend = new CompletableFuture<Void>();
		synchronized (this.pendingSendWaiters)
		{
			this.pendingSendWaiters.put(tag, 
					new ReplayableWorkItem<Void>(bytes, sentMsgSize, AmqpConstants.AmqpBatchMessageFormat, onSend, this.operationTimeout));
		}
		
        this.sendLink.advance();
        return onSend;
	}
	
	public CompletableFuture<Void> send(Message msg) throws ServiceBusException
	{
		return this.send(msg, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
	}
	
	public void close()
	{
		if (this.sendLink != null && this.sendLink.getLocalState() == EndpointState.ACTIVE)
		{
			this.sendLink.close();
		}
	}
	
	public void onOpenComplete(Exception completionException)
	{
		synchronized(this.linkCreateLock)
		{
			this.linkCreateScheduled = false;
		}
		
		if (completionException == null)
		{
			this.currentOperationTracker = null;
			this.retryPolicy.resetRetryCount(this.getClientId());
			if (!this.linkOpen.isDone())
			{
				this.linkOpen.complete(this);
			}
			else if (!this.pendingSendWaiters.isEmpty())
			{
				synchronized(this.pendingSendWaiters)
				{
					ConcurrentHashMap<byte[], ReplayableWorkItem<Void>> unacknowledgedSends = this.pendingSendWaiters;
					this.pendingSendWaiters = new ConcurrentHashMap<byte[], ReplayableWorkItem<Void>>();
					
					unacknowledgedSends.forEachValue(1, new Consumer<ReplayableWorkItem<Void>>(){
						@Override
						public void accept(ReplayableWorkItem<Void> sendWork)
						{
							byte[] tag = String.valueOf(nextTag.incrementAndGet()).getBytes();
					        Delivery dlv = sendLink.delivery(tag);
					        dlv.setMessageFormat(sendWork.getMessageFormat());
					        
					        int sentMsgSize = sendLink.send(sendWork.getMessage(), 0, sendWork.getEncodedMessageSize());
					        assert sentMsgSize != sendWork.getEncodedMessageSize() : "Contract of the ProtonJ library for Sender.Send API changed";
					        
					        CompletableFuture<Void> onSend = new CompletableFuture<Void>();
					        pendingSendWaiters.put(tag, new ReplayableWorkItem<Void>(sendWork.getMessage(), sendWork.getEncodedMessageSize(), sendWork.getMessageFormat(), onSend, operationTimeout));
					        
					        sendLink.advance();
						}
					});
					
					unacknowledgedSends.clear();
				}
			}
		}
		else
		{		
			this.linkOpen.completeExceptionally(completionException);
		}
	}
	
	public void onError(ErrorCondition error)
	{
		Exception completionException = ExceptionUtil.toException(error);
		
		// if CurrentOpTracker is null - no operation is in progress
		Duration remainingTime = this.currentOperationTracker == null 
						? Duration.ofSeconds(0)
						: (this.currentOperationTracker.elapsed().compareTo(this.operationTimeout) > 0) 
								? Duration.ofSeconds(0) 
								: this.operationTimeout.minus(this.currentOperationTracker.elapsed());
		Duration retryInterval = this.retryPolicy.getNextRetryInterval(this.getClientId(), completionException, remainingTime);
		
		if (retryInterval != null)
		{
			this.scheduleRecreate(retryInterval);			
			return;
		}
		
		synchronized (this.linkOpen)
		{
			if (!this.linkOpen.isDone())
			{
				this.onOpenComplete(completionException);
				return;
			}
		}
	}
	
	private void scheduleRecreate(Duration runAfter)
	{
		synchronized(this.linkCreateLock)
		{
			if (!this.linkCreateScheduled)
			{
				this.linkCreateScheduled = true;
		
				Timer.schedule(
					new Runnable()
					{
						@Override
						public void run()
						{
							MessageSender.this.sendLink = MessageSender.this.createSendLink();
							SendLinkHandler handler = new SendLinkHandler(MessageSender.this);
							BaseHandler.setHandler(MessageSender.this.sendLink, handler);
							MessageSender.this.retryPolicy.incrementRetryCount(MessageSender.this.getClientId());
						}
					},
					runAfter,
					TimerType.OneTimeRun);
			}
		}
	}
	
	public void onSendComplete(byte[] deliveryTag, DeliveryState outcome)
	{
		synchronized(this.pendingSendWaiters)
        {
			CompletableFuture<Void> pendingSend = this.pendingSendWaiters.get(deliveryTag).getWork();
			if (pendingSend != null)
			{
				if (outcome == Accepted.getInstance())
				{
					this.retryPolicy.resetRetryCount(this.getClientId());
					pendingSend.complete(null);
				}
				else 
				{
					// TODO: enumerate all cases - if we ever return failed delivery from Service - do they translate to exceptions ?
					pendingSend.completeExceptionally(ServiceBusException.create(false, outcome.toString()));
				}
				
				this.pendingSendWaiters.remove(deliveryTag);
			}
        }
	}

	private Sender createSendLink()
	{
		Connection connection = this.underlyingFactory.getConnection();
		Session session = connection.session();
        session.open();
        
        String sendLinkName = this.getClientId();
        sendLinkName = sendLinkName.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(connection.getRemoteContainer());
        Sender sender = session.sender(sendLinkName);
        
        Target target = new Target();
        target.setAddress(this.sendPath);
        sender.setTarget(target);
        
        Source source = new Source();
        sender.setSource(source);
        
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        
        sender.open();
        return sender;
	}
	
	// TODO: consolidate common-code written for timeouts in Sender/Receiver
	private void initializeLinkOpen(TimeoutTracker timeout)
	{
		this.linkOpen = new CompletableFuture<MessageSender>();
		
		// timer to signal a timeout if exceeds the operationTimeout on MessagingFactory
		Timer.schedule(
			new Runnable()
				{
					public void run()
					{
						synchronized(MessageSender.this.linkOpen)
						{
							if (!MessageSender.this.linkOpen.isDone())
							{
								Exception operationTimedout = new TimeoutException(
										String.format(Locale.US, "Send Link(%s) open() timed out", MessageSender.this.getClientId()));

								if (TRACE_LOGGER.isLoggable(Level.WARNING))
								{
									TRACE_LOGGER.log(Level.WARNING, 
											String.format(Locale.US, "message Sender(linkName: %s, path: %s) open call timedout", MessageSender.this.getClientId(), MessageSender.this.sendPath), 
											operationTimedout);
								}
								
								MessageSender.this.linkOpen.completeExceptionally(operationTimedout);
							}
						}
					}
				}
			, timeout.remaining()
			, TimerType.OneTimeRun);
	}

	@Override
	public CompletableFuture<Void> closeAsync() {
		// TODO Auto-generated method stub
		return null;
	}
}
