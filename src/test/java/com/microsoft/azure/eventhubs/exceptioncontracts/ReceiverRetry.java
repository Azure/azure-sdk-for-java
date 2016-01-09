package com.microsoft.azure.eventhubs.exceptioncontracts;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.driver.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.*;
import org.apache.qpid.proton.reactor.*;
import org.junit.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.eventhubs.lib.*;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.amqp.AmqpErrorCode;

public class ReceiverRetry extends TestBase
{
	@Test
	public void testRetryWhenReceiveFails() throws Exception
	{
    	final TestData data = new TestData();
    	data.retryCount = 0;
    	
    	// first flow --> Receiver.open succeeds.
    	// after that this will continuously throw InternalError
    	Sender1MsgOnLinkFlowHandler recvFlowHandler = new Sender1MsgOnLinkFlowHandler()
    	{
			boolean firstRequest = true;			
			@Override
			public void onLinkFlow(Event event)
			{
				super.onLinkFlow(event);
				System.out.println("onLinkFlow");
				if (firstRequest)
				{
					this.firstRequest = false;
				}
				else 
				{
					Link link = event.getLink();
					if (link.getLocalState()== EndpointState.ACTIVE)
					{
						link.setCondition(new ErrorCondition(AmqpErrorCode.InternalError, "SimulateInternalError"));
						data.retryCount++;
						link.detach();
						link.close();
					}
				}
			}
		};
		
		MockServer server = MockServer.Create(null, recvFlowHandler);
		
		MessagingFactory factory = MessagingFactory.create(
				new ConnectionStringBuilder("Endpoint=amqps://localhost;SharedAccessKeyName=somename;EntityPath=eventhub1;SharedAccessKey=somekey"),
				MockServer.reactor);
		
		try 
		{	
			MessageReceiver receiver = MessageReceiver.Create(factory, 
					"receiver1", "eventhub1/consumergroups/$default/partitions/0", "-1", false, 100, 0, false, null).get();
			Collection<Message> messages = receiver.receive().get();
			if (messages != null) {
				receiver.receive().get();
			}
			
			System.out.println(String.format("actual retries: %s", data.retryCount));
			Assert.assertTrue(data.retryCount > 3);
		}
		finally
		{
			// server.close();
		}
	}

	public class TestData
	{
		public int retryCount;
	}
}
