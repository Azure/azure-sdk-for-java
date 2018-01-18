/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TestContext
{
	public final static ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	final static String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "EVENT_HUB_CONNECTION_STRING";
	final static String PARTIION_COUNT_ENV_NAME = "PARTITION_COUNT";
        
        private static String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

	private TestContext()
	{
		// eq. of c# static class
	}
	
	public static ConnectionStringBuilder getConnectionString()
	{
		return new ConnectionStringBuilder(CONNECTION_STRING);
	}
	
	public static int getPartitionCount()
	{
		return Integer.parseInt(System.getenv(PARTIION_COUNT_ENV_NAME));
	}
	
	public static String getConsumerGroupName()
	{
		return "$default";
	}
        
        public static void setConnectionString(final String connectionString)
        {
            CONNECTION_STRING = connectionString;
        }
	
	public static boolean isTestConfigurationSet()
	{
		return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME) != null &&
				System.getenv(PARTIION_COUNT_ENV_NAME) != null;
	}
}
