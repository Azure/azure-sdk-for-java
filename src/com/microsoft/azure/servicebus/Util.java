/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.servicebus;

import java.util.Locale;

import org.apache.qpid.proton.amqp.Symbol;

class Util
{
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
}
