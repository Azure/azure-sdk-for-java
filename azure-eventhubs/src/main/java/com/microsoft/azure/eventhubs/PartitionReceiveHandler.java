/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

public abstract class PartitionReceiveHandler
{
	public abstract void onReceive(Iterable<EventData> events);
	
	public abstract void onError(Throwable error);
	
	public abstract void onClose(Throwable error);
}
