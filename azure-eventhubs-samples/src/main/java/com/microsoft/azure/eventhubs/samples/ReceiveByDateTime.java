/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.eventhubs.samples;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.*;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.function.*;
import java.util.logging.*;

import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class ReceiveByDateTime
{

	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder("----namespaceName-----", "----EventHubName-----", "-----sayKeyName-----", "---SasKey----");
		
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		// receiver
		String partitionId = "0"; // API to get PartitionIds will be released as part of V0.2
		
		PartitionReceiver receiver = ehClient.createEpochReceiver(
				EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, 
				partitionId, 
				Instant.now(),
				2345).get();
		
		System.out.println("date-time receiver created...");
		
		try
		{
			while (true)
			{
				receiver.receive().thenAccept(new Consumer<Iterable<EventData>>()
				{
					public void accept(Iterable<EventData> receivedEvents)
					{
						int batchSize = 0;
						if (receivedEvents != null)
						{
							for(EventData receivedEvent: receivedEvents)
							{
								System.out.print(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
										receivedEvent.getSystemProperties().getOffset(), 
										receivedEvent.getSystemProperties().getSequenceNumber(), 
										receivedEvent.getSystemProperties().getEnqueuedTime()));
								System.out.println(String.format("| Message Payload: %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
								batchSize++;
							}
						}
						
						System.out.println(String.format("ReceivedBatch Size: %s", batchSize));
					}
				}).get();
			}
		}
		finally
		{
			receiver.close();
		}
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent()	{}
		
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}

}
