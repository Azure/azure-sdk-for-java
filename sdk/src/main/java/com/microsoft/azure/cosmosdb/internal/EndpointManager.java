/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
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

package com.microsoft.azure.cosmosdb.internal;

import java.net.URI;

import com.microsoft.azure.cosmosdb.DatabaseAccount;

import rx.Observable;

/**
 * Defines an interface used to manage endpoint selection for geo-distributed database accounts in the Azure Cosmos DB
 * database service.
 */
public interface EndpointManager {

    /**
     * Returns the current write region endpoint.
     *
     * @return the write endpoint URI
     */
    public URI getWriteEndpoint();

    /**
     * Returns the current read region endpoint.
     *
     * @return the read endpoint URI
     */
    public URI getReadEndpoint();

    /**
     * Returns the target endpoint for a given request.
     *
     * @param operationType the operation type
     * @return the service endpoint URI
     */
    public URI resolveServiceEndpoint(OperationType operationType);

    /**
     * Refreshes the client side endpoint cache.
     */
    public void refreshEndpointList();

    /**
     * Gets the Database Account resource
     *
     * @return the database account
     */
    public Observable<DatabaseAccount> getDatabaseAccountFromAnyEndpoint();

    /**
     * Mark the current endpoint as unavailable
     */
    public void markEndpointUnavailable();

    /**
     * Close the endpoint manager
     */
    public void close();

    /**
     * Gets a boolean value indicating whether the endpoint manager has been closed.
     *
     * @return true if the endpoint manager has been closed
     */
    public boolean isClosed();
}
