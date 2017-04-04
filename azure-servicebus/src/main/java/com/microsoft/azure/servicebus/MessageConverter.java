package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.MessageWithDeliveryTag;
import com.microsoft.azure.servicebus.primitives.MessageWithLockToken;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Util;

public class MessageConverter
{	
	public static org.apache.qpid.proton.message.Message convertBrokeredMessageToAmqpMessage(Message brokeredMessage)	
	{
		org.apache.qpid.proton.message.Message amqpMessage = Proton.message();
		if(brokeredMessage.getContent() != null)
		{
			amqpMessage.setBody(new Data(new Binary(brokeredMessage.getContent())));
		}
		
		if(brokeredMessage.getProperties() != null)
		{
			amqpMessage.setApplicationProperties(new ApplicationProperties(brokeredMessage.getProperties()));
		}
		
		if(brokeredMessage.getTimeToLive() != null)
		{
			amqpMessage.setTtl(brokeredMessage.getTimeToLive().toMillis());
		}		
		
		amqpMessage.setMessageId(brokeredMessage.getMessageId());
		amqpMessage.setContentType(brokeredMessage.getContentType());
		amqpMessage.setCorrelationId(brokeredMessage.getCorrelationId());
		amqpMessage.setSubject(brokeredMessage.getLabel());
		amqpMessage.getProperties().setTo(brokeredMessage.getTo());
		amqpMessage.setReplyTo(brokeredMessage.getReplyTo());
		amqpMessage.setReplyToGroupId(brokeredMessage.getReplyToSessionId());
		amqpMessage.setGroupId(brokeredMessage.getSessionId());
		
		Map<Symbol, Object> messageAnnotationsMap = new HashMap<Symbol, Object>();
		if(brokeredMessage.getScheduledEnqueuedTimeUtc() != null)
		{
			messageAnnotationsMap.put(Symbol.valueOf(ClientConstants.SCHEDULEDENQUEUETIMENAME), Date.from(brokeredMessage.getScheduledEnqueuedTimeUtc()));
		}
		
		if(!StringUtil.isNullOrEmpty(brokeredMessage.getPartitionKey()))
		{
			messageAnnotationsMap.put(Symbol.valueOf(ClientConstants.PARTITIONKEYNAME), brokeredMessage.getPartitionKey());
		}
		
		amqpMessage.setMessageAnnotations(new MessageAnnotations(messageAnnotationsMap));
		
		return amqpMessage;
	}
	
	public static Message convertAmqpMessageToBrokeredMessage(org.apache.qpid.proton.message.Message amqpMessage)
	{
		return convertAmqpMessageToBrokeredMessage(amqpMessage, (byte[])null);
	}
	
	public static Message convertAmqpMessageToBrokeredMessage(MessageWithDeliveryTag amqpMessageWithDeliveryTag)
	{
		org.apache.qpid.proton.message.Message amqpMessage = amqpMessageWithDeliveryTag.getMessage();
		byte[] deliveryTag = amqpMessageWithDeliveryTag.getDeliveryTag();
		return convertAmqpMessageToBrokeredMessage(amqpMessage, deliveryTag);
	}
	
	public static Message convertAmqpMessageToBrokeredMessage(MessageWithLockToken amqpMessageWithLockToken)
	{
		Message convertedMessage = convertAmqpMessageToBrokeredMessage(amqpMessageWithLockToken.getMessage(), (byte[])null);
		convertedMessage.setLockToken(amqpMessageWithLockToken.getLockToken());
		return convertedMessage;		
	}
		
	public static Message convertAmqpMessageToBrokeredMessage(org.apache.qpid.proton.message.Message amqpMessage, byte[] deliveryTag)
	{		
		Message brokeredMessage;
		Section body = amqpMessage.getBody();
		if(body != null)
		{
			if(body instanceof Data)
			{
				Binary messageData = ((Data)body).getValue();
				brokeredMessage = new Message(messageData.getArray());
			}
			else
			{
				// TODO: handle other types of message body
				brokeredMessage = new Message();
			}
		}
		else
		{
			brokeredMessage = new Message();
		}
		
		// Application properties
		ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();
		if(applicationProperties != null)
		{
			brokeredMessage.setProperties(applicationProperties.getValue());
		}		
		
		// Header
		brokeredMessage.setTimeToLive(Duration.ofMillis(amqpMessage.getTtl()));
		brokeredMessage.setDeliveryCount(amqpMessage.getDeliveryCount());
		
		// Properties
		brokeredMessage.setMessageId(amqpMessage.getMessageId().toString());
		brokeredMessage.setContentType(amqpMessage.getContentType());
		Object correlationId = amqpMessage.getCorrelationId();
		if(correlationId != null)
		{
			brokeredMessage.setCorrelationId(amqpMessage.getCorrelationId().toString());
		}		
		brokeredMessage.setLabel(amqpMessage.getSubject());
		brokeredMessage.setTo(amqpMessage.getProperties().getTo());
		brokeredMessage.setReplyTo(amqpMessage.getReplyTo());
		brokeredMessage.setReplyToSessionId(amqpMessage.getReplyToGroupId());
		brokeredMessage.setSessionId(amqpMessage.getGroupId());
		
		// Message Annotations
		MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
		if(messageAnnotations != null)
		{
			Map<Symbol, Object> messageAnnotationsMap = messageAnnotations.getValue();
			if(messageAnnotationsMap != null)
			{
				for(Map.Entry<Symbol, Object> entry : messageAnnotationsMap.entrySet())
				{
					String entryName = entry.getKey().toString();
					switch(entryName)
					{
						case ClientConstants.ENQUEUEDTIMEUTCNAME:
							//brokeredMessage.setEnqueuedTimeUtc(Utils.convertDotNetTicksToInstant((long)entry.getValue()));
							brokeredMessage.setEnqueuedTimeUtc(((Date)entry.getValue()).toInstant());
							break;
						case ClientConstants.SCHEDULEDENQUEUETIMENAME:
	                        //brokeredMessage.setScheduledEnqueuedTimeUtc(Utils.convertDotNetTicksToInstant((long)entry.getValue()));
	                        brokeredMessage.setScheduledEnqueuedTimeUtc(((Date)entry.getValue()).toInstant());
	                        break;
	                    case ClientConstants.SEQUENCENUBMERNAME:
	                        brokeredMessage.setSequenceNumber((long)entry.getValue());
	                        break;                    
	                    case ClientConstants.LOCKEDUNTILNAME:
	                        //brokeredMessage.setLockedUntilUtc(Utils.convertDotNetTicksToInstant((long)entry.getValue()));
	                        brokeredMessage.setLockedUntilUtc(((Date)entry.getValue()).toInstant());
	                        break;                    
	                    case ClientConstants.PARTITIONKEYNAME:
	                        brokeredMessage.setPartitionKey((String)entry.getValue());
	                        break;                    
	                    case ClientConstants.DEADLETTERSOURCENAME:
	                        brokeredMessage.setDeadLetterSource((String)entry.getValue());
	                        break;
					}				
				}
			}
		}
		
		if(deliveryTag != null && deliveryTag.length == ClientConstants.LOCKTOKENSIZE)
		{
			UUID lockToken = Util.convertDotNetBytesToUUID(deliveryTag);
			brokeredMessage.setLockToken(lockToken);
		}
		else
		{
			brokeredMessage.setLockToken(ClientConstants.ZEROLOCKTOKEN);
		}
		
		brokeredMessage.setDeliveryTag(deliveryTag);
		
		return brokeredMessage;
	}	
}
