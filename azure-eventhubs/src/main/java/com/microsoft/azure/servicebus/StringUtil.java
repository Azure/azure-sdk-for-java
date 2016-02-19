package com.microsoft.azure.servicebus;

import java.util.UUID;

public final class StringUtil
{
	public final static String EMPTY = "";
	
	public static boolean isNullOrEmpty(String string)
	{
		return (string == null || string.isEmpty());
	}
	
	public static boolean isNullOrWhiteSpace(String string)
	{
		if (string == null)
			return true;
		
		for (int index=0; index < string.length(); index++)
		{
			if (!Character.isWhitespace(string.charAt(index)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static String getRandomString()
	{
		return UUID.randomUUID().toString().substring(0, 6);
	}
}
