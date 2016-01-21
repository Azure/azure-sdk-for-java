package com.microsoft.azure.servicebus;

import java.util.Iterator;

public final class IteratorUtil
{
	private IteratorUtil()
	{
	}

	public static boolean sizeEquals(Iterator iterator, int expectedSize)
	{
		if (expectedSize == 0)
		{
			return !iterator.hasNext();
		}
		else if (!iterator.hasNext())
		{
			return false;
		}
		else 
		{
			iterator.next();
			return sizeEquals(iterator, expectedSize - 1);
		}
	}
}
