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
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

public class SendBatch
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
		EventHubClient sender = EventHubClient.createFromConnectionString(connStr.toString()).get();
		
		while (true)
		{
			LinkedList<EventData> events = new LinkedList<EventData>();
			for (int count = 1; count < 11; count++)
			{
				PayloadEvent payload = new PayloadEvent(count);
				byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
				EventData sendEvent = new EventData(payloadBytes);
				Map<String, String> applicationProperties = new HashMap<String, String>();
				applicationProperties.put("from", "javaClient");
				sendEvent.setProperties(applicationProperties);
				events.add(sendEvent);
			}

			sender.send(events).get();
			System.out.println(String.format("Sent Batch... Size: %s", events.size()));
		}		
	}

	/**
	 * actual application-payload, ex: a telemetry event
	 */
	static final class PayloadEvent
	{
		PayloadEvent(final int seed)
		{
			this.id = "telemetryEvent1-critical-eventid-2345" + seed;
			this.strProperty = "I am a mock telemetry event from JavaClient.";
			this.longProperty = seed * new Random().nextInt(seed);
			this.intProperty = seed * new Random().nextInt(seed);
		}
		
		public String id;
		public String strProperty;
		public long longProperty;
		public int intProperty;
	}

}
