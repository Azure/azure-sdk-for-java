/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.servicebus.primitives;

import java.lang.reflect.Array;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Decimal128;
import org.apache.qpid.proton.amqp.Decimal32;
import org.apache.qpid.proton.amqp.Decimal64;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedByte;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.UnsignedShort;
import org.apache.qpid.proton.amqp.messaging.AmqpSequence;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.codec.UnsignedIntegerType;
import org.apache.qpid.proton.message.Message;

public class Util
{
	private static final long EPOCHINDOTNETTICKS = 621355968000000000l;
	private static final int GUIDSIZE = 16;
	
	private Util()
	{
	}

	static int sizeof(Object obj)
	{
		if(obj == null)
		{
			return 0;
		}
		
		if (obj instanceof String)
		{
			return obj.toString().length() << 1;
		}
		
		if (obj instanceof Symbol)
		{
			return ((Symbol) obj).length() << 1;
		}
		
		if (obj instanceof Byte || obj instanceof UnsignedByte)
		{
			return Byte.BYTES;
		}
		
		if (obj instanceof Integer || obj instanceof UnsignedInteger)
		{
			return Integer.BYTES;
		}
		
		if (obj instanceof Long || obj instanceof UnsignedLong || obj instanceof Date)
		{
			return Long.BYTES;
		}
		
		if (obj instanceof Short || obj instanceof UnsignedShort)
		{
			return Short.BYTES;
		}
		
		if (obj instanceof Character)
		{
			return 4;
		}
		
		if (obj instanceof Float)
		{
			return Float.BYTES;
		}
		
		if (obj instanceof Double)
		{
			return Double.BYTES;
		}
		
		if (obj instanceof UUID)
		{
			// UUID is internally represented as 16 bytes. But how does ProtonJ encode it? To be safe..we can treat it as a string of 36 chars = 72 bytes.
			//return 72;
			return 16;
		}
		
		if(obj instanceof Decimal32)
		{
			return 4;
		}
		
		if(obj instanceof Decimal64)
		{
			return 8;
		}
		
		if(obj instanceof Decimal128)
		{
			return 16;
		}		
		
		if (obj instanceof Binary)
		{
			return ((Binary)obj).getLength();
		}
		
		if (obj instanceof Map)
		{
			// Size and Count each take a max of 4 bytes
			int size = 8;
			Map map = (Map) obj;
			for(Object value: map.keySet())
			{
				size += Util.sizeof(value);
			}
			
			for(Object value: map.values())
			{
				size += Util.sizeof(value);
			}
			
			return size;
		}
		
		if (obj instanceof Iterable)
		{
			// Size and Count each take a max of 4 bytes
			int size = 8;
			for(Object innerObject : (Iterable)obj)
			{
				size += Util.sizeof(innerObject);
			}
			
			return size;
		}
		
		if(obj.getClass().isArray())
		{
			// Size and Count each take a max of 4 bytes
			int size = 8;
			int length = Array.getLength(obj);
			for(int i=0; i<length; i++)
			{
				size += Util.sizeof(Array.get(obj, i));
			}
			
			return size;
		}
		
		throw new IllegalArgumentException(String.format(Locale.US, "Encoding Type: %s is not supported", obj.getClass()));
	}
	
	// Unused now.. ServiceBus service serializes DateTime types as java time as per AMQP spec 
	// .Net ticks are measured from 01/01/0001, java instants are measured from 01/01/1970
	public static Instant convertDotNetTicksToInstant(long dotNetTicks)
	{
		long ticksFromEpoch = dotNetTicks - EPOCHINDOTNETTICKS;
		long millisecondsFromEpoch = ticksFromEpoch/10000;
		long fractionTicks = ticksFromEpoch%10000;
		return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks*100);
	}
	
	public static long convertInstantToDotNetTicks(Instant instant)
	{
		return (instant.getEpochSecond()* 10000000) + (instant.getNano()/100) + EPOCHINDOTNETTICKS ;
	}
			
	//.Net GUID bytes are ordered in a different way.
	// First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order, 7th and 8th bytes are also in reverse order
	public static UUID convertDotNetBytesToUUID(byte[] dotNetBytes)
	{
		if(dotNetBytes == null || dotNetBytes.length != GUIDSIZE)
		{
			return new UUID(0l, 0l);
		}
		
		byte[] reOrderedBytes = new byte[GUIDSIZE];
		for(int i=0; i<GUIDSIZE; i++)
		{
			int indexInReorderedBytes;
			switch(i)
			{
				case 0:
					indexInReorderedBytes = 3;
					break;
				case 1:
					indexInReorderedBytes = 2;
					break;
				case 2:
					indexInReorderedBytes = 1;
					break;
				case 3:
					indexInReorderedBytes = 0;
					break;
				case 4:
					indexInReorderedBytes = 5;
					break;
				case 5:
					indexInReorderedBytes = 4;
					break;
				case 6:
					indexInReorderedBytes = 7;
					break;
				case 7:
					indexInReorderedBytes = 6;
					break;
				default:
					indexInReorderedBytes = i;
			}
			
			reOrderedBytes[indexInReorderedBytes] = dotNetBytes[i];
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(reOrderedBytes);
		long mostSignificantBits = buffer.getLong();
		long leastSignificantBits = buffer.getLong();
		return new UUID(mostSignificantBits, leastSignificantBits);
	}
	
	public static byte[] convertUUIDToDotNetBytes(UUID uniqueId)
	{
		if(uniqueId == null || uniqueId.equals(ClientConstants.ZEROLOCKTOKEN))
		{
			return new byte[GUIDSIZE];
		}
				
		ByteBuffer buffer = ByteBuffer.allocate(GUIDSIZE);
		buffer.putLong(uniqueId.getMostSignificantBits());
		buffer.putLong(uniqueId.getLeastSignificantBits());
		byte[] javaBytes = buffer.array();
		
		byte[] dotNetBytes = new byte[GUIDSIZE];
		for(int i=0; i<GUIDSIZE; i++)
		{
			int indexInReorderedBytes;
			switch(i)
			{
				case 0:
					indexInReorderedBytes = 3;
					break;
				case 1:
					indexInReorderedBytes = 2;
					break;
				case 2:
					indexInReorderedBytes = 1;
					break;
				case 3:
					indexInReorderedBytes = 0;
					break;
				case 4:
					indexInReorderedBytes = 5;
					break;
				case 5:
					indexInReorderedBytes = 4;
					break;
				case 6:
					indexInReorderedBytes = 7;
					break;
				case 7:
					indexInReorderedBytes = 6;
					break;
				default:
					indexInReorderedBytes = i;
			}
			
			dotNetBytes[indexInReorderedBytes] = javaBytes[i];
		}	
		
		return dotNetBytes;
	}
	
	private static int getPayloadSize(Message msg)
	{
		if (msg == null || msg.getBody() == null)
		{
			return 0;
		}

		Section bodySection = msg.getBody();
		if(bodySection instanceof AmqpValue)
		{
			return Util.sizeof(((AmqpValue)bodySection).getValue());
		}
		else if(bodySection instanceof AmqpSequence)
		{
			return Util.sizeof(((AmqpSequence)bodySection).getValue());
		}
		else if (bodySection instanceof Data)
		{
			Data payloadSection = (Data) bodySection;
			Binary payloadBytes = payloadSection.getValue();
			return Util.sizeof(payloadBytes);
		}
		else
		{
			return 0;
		}
	}

	// Remove this.. Too many cases, too many types...
	public static int getDataSerializedSize(Message amqpMessage)
	{
		if (amqpMessage == null)
		{
			return 0;
		}

		int payloadSize = getPayloadSize(amqpMessage);

		// EventData - accepts only PartitionKey - which is a String & stuffed into MessageAnnotation
		MessageAnnotations messageAnnotations = amqpMessage.getMessageAnnotations();
		ApplicationProperties applicationProperties = amqpMessage.getApplicationProperties();
		
		int annotationsSize = 0;
		int applicationPropertiesSize = 0;

		if (messageAnnotations != null)
		{
			annotationsSize += Util.sizeof(messageAnnotations.getValue());
		}
		
		if (applicationProperties != null)
		{
			applicationPropertiesSize += Util.sizeof(applicationProperties.getValue());	
		}
		
		return annotationsSize + applicationPropertiesSize + payloadSize;
	}
	
	static Pair<byte[], Integer> encodeMessageToOptimalSizeArray(Message message) throws PayloadSizeExceededException
	{
		int payloadSize = Util.getDataSerializedSize(message);
		int allocationSize = Math.min(payloadSize + ClientConstants.MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
		byte[] encodedBytes = new byte[allocationSize];
		int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, allocationSize);
		return new Pair<byte[], Integer>(encodedBytes, encodedSize);
	}
	
	static Pair<byte[], Integer> encodeMessageToMaxSizeArray(Message message) throws PayloadSizeExceededException
	{
		// May be we should reduce memory allocations. Use a pool of byte arrays or something
		byte[] encodedBytes = new byte[ClientConstants.MAX_MESSAGE_LENGTH_BYTES];
		int encodedSize = encodeMessageToCustomArray(message, encodedBytes, 0, ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
		return new Pair<byte[], Integer>(encodedBytes, encodedSize);
	}
	
	static int encodeMessageToCustomArray(Message message, byte[] encodedBytes, int offset, int length) throws PayloadSizeExceededException
	{
		try
		{
			return message.encode(encodedBytes, offset, length);
		}
		catch(BufferOverflowException exception)
		{
			throw new PayloadSizeExceededException(String.format("Size of the payload exceeded Maximum message size: %s kb", ClientConstants.MAX_MESSAGE_LENGTH_BYTES / 1024), exception);		
		}
	}
}
