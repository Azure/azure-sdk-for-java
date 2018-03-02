/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.connstrbuilder;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.IllegalConnectionStringFormatException;

public class ConnStrBuilderTest extends ApiTestBase
{
	static final String correctConnectionString = "Endpoint=sb://endpoint1;EntityPath=eventhub1;SharedAccessKeyName=somevalue;SharedAccessKey=something;OperationTimeout=PT5S";
	static final Consumer<ConnectionStringBuilder> validateConnStrBuilder = new Consumer<ConnectionStringBuilder>()
	{
		@Override
		public void accept(ConnectionStringBuilder connStrBuilder)
		{
			Assert.assertTrue(connStrBuilder.getEventHubName().equals("eventhub1"));
			Assert.assertTrue(connStrBuilder.getEndpoint().getHost().equals("endpoint1"));
			Assert.assertTrue(connStrBuilder.getSasKey().equals("something"));
			Assert.assertTrue(connStrBuilder.getSasKeyName().equals("somevalue"));
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
		final ConnectionStringBuilder secondConnStr = new ConnectionStringBuilder()
                .setEndpoint(connStrBuilder.getEndpoint())
                .setEventHubName(connStrBuilder.getEventHubName())
                .setSasKeyName(connStrBuilder.getSasKeyName())
                .setSasKey(connStrBuilder.getSasKey());
		secondConnStr.setOperationTimeout(connStrBuilder.getOperationTimeout());

		validateConnStrBuilder.accept(new ConnectionStringBuilder(secondConnStr.toString()));
	}
	
	@Test
	public void testPropertySetters()
	{
		final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(correctConnectionString);
		final ConnectionStringBuilder testConnStrBuilder = new ConnectionStringBuilder(connStrBuilder.toString());
		validateConnStrBuilder.accept(testConnStrBuilder);
		
		connStrBuilder.setOperationTimeout(Duration.ofSeconds(8));

		ConnectionStringBuilder testConnStrBuilder1 = new ConnectionStringBuilder(connStrBuilder.toString());
		Assert.assertTrue(testConnStrBuilder1.getOperationTimeout().getSeconds() == 8);
	}
}
