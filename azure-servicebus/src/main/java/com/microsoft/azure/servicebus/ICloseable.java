// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Defines a standard way of properly closing and disposing objects.
 * @since 1.0
 * 
 */
public interface ICloseable {
    /**
     * Closes and disposes any resources associated with this object. An object cannot be used after it is closed. This is an asynchronous method that returns a CompletableFuture immediately.
     * This object is completely closed when the returned CompletableFuture is completed.
     * @return a CompletableFuture representing the closing of this object.
     */
	CompletableFuture<Void> closeAsync();
	
	/**
	 * Synchronously closes and disposes any resources associated with this object. Calling this method is equivalent of calling <code>closeAsync().get()</code>. This method blocks until this object is closed.
	 * @throws ServiceBusException If this object cannot be properly closed. 
	 */
	void close() throws ServiceBusException;
}
