package com.microsoft.azure.servicebus;

import java.util.Iterator;

public final class IteratorUtil
{
	private IteratorUtil()
	{
	}

	public static <T> boolean sizeEquals(Iterable<T> iterable, int expectedSize)
	{
		Iterator<T> iterator = iterable.iterator();
		
		int currentSize = 0;
		while(iterator.hasNext())
		{
			if (expectedSize > currentSize)
			{
				currentSize++;
				iterator.next();
				continue;
			}
			else
			{
				return false;
			}
		}
		
		return true;		
	}
	
	public static <T> T getLast(Iterator<T> iterator)
	{
		T last = null;
		while(iterator.hasNext())
		{
			last = iterator.next();
		}
		
		return last;
	}
}
