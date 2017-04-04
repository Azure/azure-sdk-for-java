/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.nio.charset.Charset;
import java.util.UUID;

public final class StringUtil
{
	public final static String EMPTY = "";
	private final static Charset UTF8CharSet = Charset.forName("UTF-8");

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

	public static String getShortRandomString()
	{
		return getRandomString().substring(0, 6);
	}
	
	public static String getRandomString()
	{
		return UUID.randomUUID().toString();
	}
	
	static String convertBytesToString(byte[] bytes)
	{
		return new String(bytes, UTF8CharSet);
	}
	
	static byte[] convertStringToBytes(String string)
	{
		return string.getBytes(UTF8CharSet);
	}	
}
