/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.servicebus.primitives;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.apache.qpid.proton.amqp.Symbol;

public class Util
{
	private static final long EPOCHINDOTNETTICKS = 621355968000000000l;
	private static final int GUIDSIZE = 16;
	
	private Util()
	{
	}

	static int sizeof(Object obj)
	{
		if (obj instanceof String)
		{
			return obj.toString().length() << 1;
		}
		
		if (obj instanceof Symbol)
		{
			return ((Symbol) obj).length() << 1;
		}
		
		if (obj instanceof Integer)
		{
			return Integer.BYTES;
		}
		
		if (obj instanceof Long)
		{
			return Long.BYTES;
		}
		
		if (obj instanceof Short)
		{
			return Short.BYTES;
		}
		
		if (obj instanceof Character)
		{
			return Character.BYTES;
		}
		
		if (obj instanceof Float)
		{
			return Float.BYTES;
		}
		
		if (obj instanceof Double)
		{
			return Double.BYTES;
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
}
