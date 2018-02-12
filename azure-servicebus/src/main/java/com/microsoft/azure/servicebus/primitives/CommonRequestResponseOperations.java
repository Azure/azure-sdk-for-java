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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.security.SecurityToken;

final class CommonRequestResponseOperations {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(CommonRequestResponseOperations.class);
	static CompletableFuture<Collection<Message>> peekMessagesAsync(RequestResponseLink requestResponseLink, Duration operationTimeout, long fromSequenceNumber, int messageCount, String sessionId, String associatedLinkName)
	{
	    TRACE_LOGGER.debug("Peeking '{}' messages from sequence number '{}' in entity '{}', sessionId '{}'", messageCount, fromSequenceNumber, requestResponseLink.getLinkPath(), sessionId);
		HashMap requestBodyMap = new HashMap();
		requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_FROM_SEQUENCE_NUMER, fromSequenceNumber);
		requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_MESSAGE_COUNT, messageCount);		
		if(sessionId != null)
		{
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SESSIONID, sessionId);
		}
		Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_PEEK_OPERATION, requestBodyMap, Util.adjustServerTimeout(operationTimeout), associatedLinkName);
		CompletableFuture<Message> responseFuture = requestResponseLink.requestAysnc(requestMessage, operationTimeout);
		return responseFuture.thenComposeAsync((responseMessage) -> {
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
				TRACE_LOGGER.debug("Peeked '{}' messages from sequence number '{}' in entity '{}', sessionId '{}'", peekedMessages.size(), fromSequenceNumber, requestResponseLink.getLinkPath(), sessionId);
				returningFuture.complete(peekedMessages);
			}
			else if(statusCode == ClientConstants.REQUEST_RESPONSE_NOCONTENT_STATUS_CODE ||
					(statusCode == ClientConstants.REQUEST_RESPONSE_NOTFOUND_STATUS_CODE && ClientConstants.MESSAGE_NOT_FOUND_ERROR.equals(RequestResponseUtils.getResponseErrorCondition(responseMessage))))
			{
			    TRACE_LOGGER.debug("Peek from sequence number '{}' in entity '{}', sessionId '{}' didnot find any messages", fromSequenceNumber, requestResponseLink.getLinkPath(), sessionId);
				returningFuture.complete(new ArrayList<Message>());
			}
			else
			{
				// error response
			    Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
			    TRACE_LOGGER.error("Peeking messages from sequence number '{}' in entity '{}', sessionId '{}' failed", fromSequenceNumber, requestResponseLink.getLinkPath(), sessionId, failureException);
				returningFuture.completeExceptionally(failureException);
			}
			return returningFuture;
		});
	}
	
	static CompletableFuture<Void> sendCBSTokenAsync(RequestResponseLink requestResponseLink, Duration operationTimeout, SecurityToken securityToken)
	{
	    TRACE_LOGGER.debug("Sending CBS Token of type '{}' to '{}'", securityToken.getTokenType(), securityToken.getTokenAudience());
        Message requestMessage = RequestResponseUtils.createRequestMessageFromValueBody(ClientConstants.REQUEST_RESPONSE_PUT_TOKEN_OPERATION, securityToken.getTokenValue(), Util.adjustServerTimeout(operationTimeout));
        requestMessage.getApplicationProperties().getValue().put(ClientConstants.REQUEST_RESPONSE_PUT_TOKEN_TYPE, securityToken.getTokenType().toString());
        requestMessage.getApplicationProperties().getValue().put(ClientConstants.REQUEST_RESPONSE_PUT_TOKEN_AUDIENCE, securityToken.getTokenAudience());
        requestMessage.getApplicationProperties().getValue().put(ClientConstants.REQUEST_RESPONSE_PUT_TOKEN_EXPIRATION, securityToken.getValidUntil().toEpochMilli());
        CompletableFuture<Message> responseFuture = requestResponseLink.requestAysnc(requestMessage, operationTimeout);
        return responseFuture.thenComposeAsync((responseMessage) -> {
            CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
            int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
            if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE || statusCode == ClientConstants.REQUEST_RESPONSE_ACCEPTED_STATUS_CODE)
            {
                TRACE_LOGGER.debug("CBS Token of type '{}' sent to '{}'", securityToken.getTokenType(), securityToken.getTokenAudience());
                returningFuture.complete(null);
            }
            else
            {
                // error response
                Exception failureException = RequestResponseUtils.genereateExceptionFromResponse(responseMessage);
                TRACE_LOGGER.error("Sending CBS Token to '{}' failed", securityToken.getTokenAudience());
                returningFuture.completeExceptionally(failureException);
            }
            return returningFuture;
        });
	}
}
