package com.microsoft.azure.eventhubs.connstrbuilder;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.IllegalConnectionStringFormatException;
import com.microsoft.azure.servicebus.RetryPolicy;

public class ConnStrBuilderTest extends ApiTestBase
{
	static final String correctConnectionString = "Endpoint=sb://endpoint1;EntityPath=eventhub1;SharedAccessKeyName=somevalue;SharedAccessKey=something;OperationTimeout=PT5S;RetryPolicy=NoRetry";
	static final Consumer<ConnectionStringBuilder> validateConnStrBuilder = new Consumer<ConnectionStringBuilder>()
	{
		@Override
		public void accept(ConnectionStringBuilder connStrBuilder)
		{
			Assert.assertTrue(connStrBuilder.getEntityPath().equals("eventhub1"));
			Assert.assertTrue(connStrBuilder.getEndpoint().getHost().equals("endpoint1"));
			Assert.assertTrue(connStrBuilder.getSasKey().equals("something"));
			Assert.assertTrue(connStrBuilder.getSasKeyName().equals("somevalue"));
			Assert.assertTrue(connStrBuilder.getRetryPolicy() == RetryPolicy.getNoRetry());
			Assert.assertTrue(connStrBuilder.getOperationTimeout().equals(Duration.ofSeconds(5)));
		}
	};
	
	@Test (expected = IllegalConnectionStringFormatException.class)
	public void parseInvalidConnectionString()
	{
		new ConnectionStringBuilder("something");
	}
	
	@Test (expected = IllegalConnectionStringFormatException.class)
	public void throwOnUnrecognizedParts()
	{
		new ConnectionStringBuilder(correctConnectionString + ";" + "something");
	}
	
	@Test
	public void parseValidConnectionString()
	{
		final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(correctConnectionString);
		validateConnStrBuilder.accept(connStrBuilder);
	}
	
	@Test
	public void exchangeConnectionStringAcrossConstructors()
	{
		final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(correctConnectionString);
		final ConnectionStringBuilder secondConnStr = new ConnectionStringBuilder(connStrBuilder.getEndpoint(),
				connStrBuilder.getEntityPath(), connStrBuilder.getSasKeyName(), connStrBuilder.getSasKey());
		secondConnStr.setOperationTimeout(connStrBuilder.getOperationTimeout());
		secondConnStr.setRetryPolicy(connStrBuilder.getRetryPolicy());
		
		validateConnStrBuilder.accept(new ConnectionStringBuilder(secondConnStr.toString()));
	}
}
