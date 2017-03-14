package com.microsoft.azure.servicebus.primitives;

public class TestConnectionStringBuilder extends ConnectionStringBuilder
{
	final static String oneBoxEndpointFormat = "amqps://%s.servicebus.onebox.windows-int.net";
	
	public TestConnectionStringBuilder(
			final String namespaceName, 
			final String entityPath, 
			final String sharedAccessKeyName,
			final String sharedAccessKey)
	{
		super(namespaceName, entityPath, sharedAccessKeyName, sharedAccessKey);
	}
	
	@Override
	String getEndPointFormat()
	{
		return oneBoxEndpointFormat;
	}
}
