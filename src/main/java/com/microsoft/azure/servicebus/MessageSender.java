package com.microsoft.azure.servicebus;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

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

import com.microsoft.azure.eventhubs.lib.Sender1MsgOnLinkFlowHandler;
import com.microsoft.azure.servicebus.amqp.AmqpConstants;
import com.microsoft.azure.servicebus.amqp.SendLinkHandler;

/**
 * Abstracts all amqp related details
 * translates event-driven reactor model into async send Api
 */
public class MessageSender extends ClientEntity
{
	
	public static final int MaxMessageLength = 255 * 1024;
	
	private final Object lock = new Object(); 
	private final Sender sendLink;
	private final ConcurrentHashMap<byte[], CompletableFuture<Void>> pendingSendWaiters;
	private final MessagingFactory underlyingFactory;
	private final String sendPath;
	
	private CompletableFuture<MessageSender> linkOpen; 
	
	private AtomicLong nextTag;
	private boolean firstSendFlowRecieved = false;
	
	public static CompletableFuture<MessageSender> Create(
			final MessagingFactory factory,
			final String sendLinkName,
			final String senderPath) throws IllegalEntityException
	{
		MessageSender msgSender = new MessageSender(factory, sendLinkName, senderPath);
		SendLinkHandler handler = new SendLinkHandler(sendLinkName, msgSender);
		BaseHandler.setHandler(msgSender.sendLink, handler);
		return msgSender.linkOpen;
	}
	
	private MessageSender(final MessagingFactory factory, final String sendLinkName, final String senderPath)
	{
		super(sendLinkName);
		this.sendPath = senderPath;
		this.underlyingFactory = factory;
		this.sendLink = MessageSender.createSendLink(factory.getConnection(), sendLinkName, senderPath);
		this.linkOpen = new CompletableFuture<MessageSender>();
		this.pendingSendWaiters = new ConcurrentHashMap<byte[], CompletableFuture<Void>>();
		this.nextTag = new AtomicLong(0);
	}
	
	public String getSendPath()
	{
		return this.sendPath;
	}
	
	public CompletableFuture<Void> send(Message msg, int messageFormat)
	{
		// TODO: fix allocation per call - use BufferPool
		byte[] bytes = new byte[MaxMessageLength];
		int encodedSize = msg.encode(bytes, 0, MaxMessageLength);
		
		byte[] tag = String.valueOf(nextTag.incrementAndGet()).getBytes();
        Delivery dlv = this.sendLink.delivery(tag);
        dlv.setMessageFormat(messageFormat);
        
        int sentMsgSize = this.sendLink.send(bytes, 0, encodedSize);
        assert sentMsgSize != encodedSize : "Contract of the ProtonJ library for Sender.Send API changed";
        
        CompletableFuture<Void> onSend = new CompletableFuture<Void>();
        this.pendingSendWaiters.put(tag, onSend);
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
					
			encodedSize = messageWrappedByData.encode(bytes, byteArrayOffset, MaxMessageLength - byteArrayOffset - 1);
			
			byteArrayOffset = byteArrayOffset + encodedSize;
			if (MaxMessageLength <= byteArrayOffset)
			{
				// TODO: is it intended for this purpose - else compute msg. size before hand.
				dlv.clear();
				
				// TODO: Translate to completableFuture
				throw new PayloadSizeExceededException("Size of the payload exceeded Maximum message size ");
			}
		}
		
		int sentMsgSize = this.sendLink.send(bytes, 0, byteArrayOffset);
		
		CompletableFuture<Void> onSend = new CompletableFuture<Void>();
        this.pendingSendWaiters.put(tag, onSend);
        this.sendLink.advance();
        return onSend;
	}
	
	public CompletableFuture<Void> send(Message msg)
	{
		return this.send(msg, DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
	}
	
	@Override
	public void close()
	{
		if (this.sendLink != null && this.sendLink.getLocalState() == EndpointState.ACTIVE)
		{
			this.sendLink.close();
		}
	}
	
	public void onOpenComplete(ErrorCondition condition)
	{
		if (condition == null)
		{
			this.linkOpen.complete(this);
		}
		else
		{		
			this.linkOpen.completeExceptionally(ExceptionUtil.toException(condition));
		}
	}
	
	public void onError(ErrorCondition error)
	{
		synchronized (this.linkOpen)
		{
			if (!this.linkOpen.isDone())
			{
				this.onOpenComplete(error);
				return;
			}
		}
		
		// TODO: what happens to Pending Sends
	}
	
	public void onSendComplete(byte[] deliveryTag, DeliveryState outcome)
	{
		if (outcome == Accepted.getInstance())
		{
			this.pendingSendWaiters.get(deliveryTag).complete(null);
		}
	}

	private static Sender createSendLink(final Connection connection, final String linkName, final String senderPath)
	{
		Session session = connection.session();
        session.open();
        
        Sender sender = session.sender(linkName);
        
        Target target = new Target();
        target.setAddress(senderPath);
        sender.setTarget(target);
        
        Source source = new Source();
        sender.setSource(source);
        
        sender.setSenderSettleMode(SenderSettleMode.UNSETTLED);
        
        sender.open();
        return sender;
	}
}
