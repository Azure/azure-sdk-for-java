package com.microsoft.azure.servicebus.messaging;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.ServiceBusException;

class Utils {
	
	private static final long EPOCHINDOTNETTICKS = 621355968000000000l;
	private static final int GUIDSIZE = 16;
	
	static <T> T completeFuture(CompletableFuture<T> future) throws InterruptedException, ServiceBusException
	{
		try
		{
			return future.get();
		}
		catch(InterruptedException ie)
		{
			Thread.currentThread().interrupt();
			throw ie;
		}
		catch(ExecutionException ee)
		{
			Throwable cause = ee.getCause();
			if(cause instanceof ServiceBusException)
			{
				throw (ServiceBusException)cause;
			}
			else
			{
				throw new ServiceBusException(true, cause);
			}			
		}		
	}
	
	static void assertNonNull(String argumentName, Object argument)
	{
		if(argument == null)
			throw new IllegalArgumentException("Argument '" + argumentName +"' is null.");
	}
	
	// Unused now.. ServiceBus service serializes DateTime types as java time as per AMQP spec 
	// .Net ticks are measured from 01/01/0001, java instants are measured from 01/01/1970
	static Instant convertDotNetTicksToInstant(long dotNetTicks)
	{
		long ticksFromEpoch = dotNetTicks - EPOCHINDOTNETTICKS;
		long millisecondsFromEpoch = ticksFromEpoch/10000;
		long fractionTicks = ticksFromEpoch%10000;
		return Instant.ofEpochMilli(millisecondsFromEpoch).plusNanos(fractionTicks*100);
	}
	
	static long convertInstantToDotNetTicks(Instant instant)
	{
		return (instant.toEpochMilli() + EPOCHINDOTNETTICKS) * 10000;
	}
		
	//.Net GUID bytes are ordered in a different way.
	// First 4 bytes are in reverse order, 5th and 6th bytes are in reverse order, 7th and 8th bytes are also in reverse order
	static UUID convertDotNetBytesToUUID(byte[] dotNetBytes)
	{
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
}
