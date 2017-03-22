/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.documentdb.internal;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.internal.routing.ClientCollectionCache;

/**
 * This is meant to be used only internally as a bridge access to 
 * classes in com.microsoft.azure.documentdb.internal
 **/
public class RetryPolicyBridgeInternal {

    public static RetryPolicy createSessionReadRetryPolicy(EndpointManager globalEndpointManager, AbstractDocumentServiceRequest request) {
        return new SessionReadRetryPolicy(globalEndpointManager, request);
    }
    
    public static RetryPolicy createEndpointDiscoveryRetryPolicy(ConnectionPolicy connectionPolicy, EndpointManager globalEndpointManager) {
        return new EndpointDiscoveryRetryPolicy(connectionPolicy, globalEndpointManager);
    }

    public static RetryPolicy createResourceThrottleRetryPolicy(int maxRetryAttemptsOnThrottledRequests,
            int maxRetryWaitTimeInSeconds) {
        return new ResourceThrottleRetryPolicy(maxRetryAttemptsOnThrottledRequests, maxRetryWaitTimeInSeconds);
    }
    
    public static RetryPolicy createPartitionKeyMismatchRetryPolicy(String resourcePath,
            ClientCollectionCache clientCollectionCache) {
        return new PartitionKeyMismatchRetryPolicy(resourcePath, clientCollectionCache);
    }
}
