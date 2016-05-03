/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

// multiple issues were identified in the proton-j layer which could lead to a stuck state in Transport
// https://issues.apache.org/jira/browse/PROTON-1185
// https://issues.apache.org/jira/browse/PROTON-1171
// This handler is built to - recover client from the underlying TransportStack-Stuck situation
public interface ITimeoutErrorHandler
{
	public void reportTimeoutError();
	
	public void resetTimeoutErrorTracking();
}
