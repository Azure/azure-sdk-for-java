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
import java.nio.charset.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class Send
{

	public static void main(String[] args) 
			throws ServiceBusException, ExecutionException, InterruptedException, IOException
	{
		final String namespaceName = "----ServiceBusNamespaceName-----";
		final String eventHubName = "----EventHubName-----";
		final String sasKeyName = "-----SharedAccessSignatureKeyName-----";
		final String sasKey = "---SharedAccessSignatureKey----";
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
		
		Gson gson = new GsonBuilder().create();
		
		PayloadEvent payload = new PayloadEvent(1);
		byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
		EventData sendEvent = new EventData(payloadBytes);
		
		// senders
		// Type-1 - Basic Send - not tied to any partition
		EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
		ehClient.send(sendEvent).get();
		
		// Advanced Sends
		// Type-2 - Send using PartitionKey - all Events with Same partitionKey will land on the Same Partition
		String partitionKey = "partitionTheStream";
		ehClient.send(sendEvent, partitionKey).get();
		
		// Type-3 - Send to a Specific Partition
		PartitionSender sender = ehClient.createPartitionSender("0").get();
		sender.send(sendEvent).get();
		
		System.out.println(Instant.now() + ": Send Complete...");
		System.in.read();
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent(final int seed)
		{
			this.id = "telemetryEvent1-critical-eventid-2345" + seed;
			this.strProperty = "This is a sample payloadEvent, which could be wrapped using eventdata and sent to eventhub." +
			" None of the payload event properties will be looked-at by EventHubs client or Service." + 
			" As far as EventHubs service/client is concerted, it is plain bytes being sent as 1 Event.";
			this.longProperty = seed * new Random().nextInt(seed);
			this.intProperty = seed * new Random().nextInt(seed);
		}
		
		public String id;
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}
}
