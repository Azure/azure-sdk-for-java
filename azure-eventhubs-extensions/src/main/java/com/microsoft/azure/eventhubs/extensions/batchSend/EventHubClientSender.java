/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.extensions.batchSend;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;

public class EventHubClientSender implements ISender {

	final EventHubClient eventHubClient;
	
	EventHubClientSender(final EventHubClient eventHubClient) {
		this.eventHubClient = eventHubClient;
	}
	
	@Override
	public CompletableFuture<Void> send(Iterable<EventData> edatas, String partitionKey) {
			return this.eventHubClient.send(edatas, partitionKey);
	}

	@Override
	public CompletableFuture<Void> send(Iterable<EventData> edatas) {
		return this.eventHubClient.send(edatas);
	}
}
