package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.util.*;
import java.util.concurrent.*;

import org.junit.Test;

import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;

// TODO: Once ConnectionStringBuilder.setOperationTimeout() is implemented - reduce the timeout of this testcase
public class TimeoutExceptionTest
{
	
	@Test (expected = TimeoutException.class)
	public void testReceiverOpenTimeoutException() throws Throwable
	{
		MockServer server = MockServer.Create(null);
		MessagingFactory factory = MessagingFactory.createFromConnectionString(
				new ConnectionStringBuilder("Endpoint=amqps://localhost;SharedAccessKeyName=somename;EntityPath=eventhub1;SharedAccessKey=somekey").toString());
				
		try 
		{
			// this throws timeout as the Connector in MockServer doesn't have any AmqpConnection associated
			MessageReceiver.create(factory, "receiver1", "eventhub1/consumergroups/$default/partitions/0", "-1", false, null, 100, 0, false).get();
		}
		catch(ExecutionException exp)
		{
			throw exp.getCause();
		}
		finally
		{
			server.close();
		}
	}
}
