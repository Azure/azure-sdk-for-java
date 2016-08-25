package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;

public final class TestContext
{
	final static String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "EVENT_HUB_CONNECTION_STRING";
	final static String PARTIION_COUNT_ENV_NAME = "PARTITION_COUNT";

	private TestContext()
	{
		// eq. of c# static class
	}
	
	public static ConnectionStringBuilder getConnectionString()
	{
		return new ConnectionStringBuilder(System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME));
	}
	
	public static int getPartitionCount()
	{
		return Integer.parseInt(System.getenv(PARTIION_COUNT_ENV_NAME));
	}
	
	public static String getConsumerGroupName()
	{
		return "$default";
	}
	
	public static boolean isTestConfigurationSet()
	{
		return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME) != null &&
				System.getenv(PARTIION_COUNT_ENV_NAME) != null;
	}
}
