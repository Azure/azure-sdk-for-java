/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

// recover client from the underlying TransportStack-Stuck situation
public interface ITimeoutErrorHandler
{
	public void reportTimeoutError();
	
	public void resetTimeoutErrorTracking();
}
