package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public class MessageBrowser extends ClientEntity
{
	private RequestResponseLink requestResponseLink;
	private final MessagingFactory underlyingFactory;
	private final String receivePath;
	
	private MessageBrowser(final MessagingFactory factory, final String name, final String recvPath)
	{
		super(name, factory);
		this.underlyingFactory = factory;
		this.receivePath = recvPath;
	}
	
	public static CompletableFuture<MessageBrowser> create(
			final MessagingFactory factory, 
			final String name, 
			final String recvPath)
	{
		MessageBrowser msgBrowser = new MessageBrowser(factory, name, recvPath);		
		return msgBrowser.createLink();
	}
	
	private CompletableFuture<MessageBrowser> createLink()
	{
		String requestResponseLinkPath = this.receivePath + AmqpConstants.MANAGEMENT_ADDRESS_SEGMENT;
		CompletableFuture<Void> crateAndAssignRequestResponseLink = 
				RequestResponseLink.createAsync(this.underlyingFactory, this.getClientId() + "-RequestResponse", requestResponseLinkPath).thenAccept((rrlink) -> {MessageBrowser.this.requestResponseLink = rrlink;});
		return crateAndAssignRequestResponseLink.thenApply((v) -> this);
	}
	
	@Override
	protected CompletableFuture<Void> onClose() {
		return this.requestResponseLink == null ? CompletableFuture.completedFuture(null) : this.requestResponseLink.closeAsync();
	}
	
	public CompletableFuture<Collection<Message>> peekMessages(long fromSequenceNumber, int messageCount, String sessionId, Duration timeout)
	{
		HashMap requestBodyMap = new HashMap();
		requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMER, fromSequenceNumber);
		requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_MESSAGE_COUNT, messageCount);
		if(!StringUtil.isNullOrEmpty(sessionId))
		{
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, sessionId);
		}
		Message requestMessage = RequestResponseUtils.createRequestMessage(ClientConstants.REQUEST_RESPONSE_PEEK_OPERATION, requestBodyMap, RequestResponseUtils.adjustServerTimeout(timeout));
		CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, timeout);
		return responseFuture.thenCompose((responseMessage) -> {
			CompletableFuture<Collection<Message>> returningFuture = new CompletableFuture<Collection<Message>>();
			int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
			if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
			{
				List<Message> peekedMessages = new ArrayList<Message>();
				Object responseBodyMap = ((AmqpValue)responseMessage.getBody()).getValue();
				if(responseBodyMap != null && responseBodyMap instanceof Map)
				{					
					Object messages = ((Map)responseBodyMap).get(ClientConstants.REQUEST_RESPONSE_MESSAGES);
					if(messages != null && messages instanceof Iterable)
					{
						for(Object message : (Iterable)messages)
						{
							if(message instanceof Map)
							{
								Message peekedMessage = Message.Factory.create();
								Binary messagePayLoad = (Binary)((Map)message).get(ClientConstants.REQUEST_RESPONSE_MESSAGE);
								peekedMessage.decode(messagePayLoad.getArray(), messagePayLoad.getArrayOffset(), messagePayLoad.getLength());
								peekedMessages.add(peekedMessage);
							}
						}
					}
				}				
				returningFuture.complete(peekedMessages);
			}
			else if(statusCode == ClientConstants.REQUEST_RESPONSE_NOCONTENT_STATUS_CODE ||
					(statusCode == ClientConstants.REQUEST_RESPONSE_NOTFOUND_STATUS_CODE && ClientConstants.MESSAGE_NOT_FOUND_ERROR.equals(RequestResponseUtils.getResponseErrorCondition(responseMessage))))
			{
				returningFuture.complete(new ArrayList<Message>());
			}
			else
			{
				// error response
				returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
			}
			return returningFuture;
		});
	}
}
