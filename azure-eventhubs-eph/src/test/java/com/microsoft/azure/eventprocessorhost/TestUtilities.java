/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.logging.Level;
import java.util.logging.Logger;

class TestUtilities
{
	static String getStorageConnectionString()
	{
		String retval = System.getenv("EPHTESTSTORAGE");
		return ((retval != null) ? retval : "");
	}
	
	static final Logger TEST_LOGGER = Logger.getLogger("servicebus.test-eph.trace");
	
	static final String syntacticallyCorrectDummyConnectionString =
			"Endpoint=sb://notreal.servicebus.windows.net/;SharedAccessKeyName=notreal;SharedAccessKey=NOTREALNOTREALNOTREALNOTREALNOTREALNOTREALN=;EntityPath=NOTREAL";
	
	static Boolean logToConsole = null;
	
	static void setupLogging()
	{
		logToConsole = (System.getenv("LOGTOCONSOLE") != null);
	}
	
	static void console(String message)
	{
		if (logToConsole == null)
		{
			setupLogging();
		}
		if (logToConsole.booleanValue())
		{
			System.out.print(message);
		}
	}

	static void log(String message)
	{
		console(message);
		TEST_LOGGER.log(Level.INFO, message);
	}
	
	static void logConditional(boolean doLog, String message)
	{
		if (doLog)
		{
			log(message);
		}
	}
}
