/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.extensions.batchSend;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.eventhubs.EventData;

public interface ISender {

	CompletableFuture<Void> send(final Iterable<EventData> edatas);
	
	CompletableFuture<Void> send(final Iterable<EventData> edatas,final String partitionKey);
	
}
