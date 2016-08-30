/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * The final stage of the resource definition, at which it can be create, using {@link #create()}.
 *
 * @param <T> the fluent type of the resource to be created
 */
public interface Creatable<T> extends Indexable {
    /**
     * @return the name of the creatable resource.
     */
    String name();

    /**
     * Execute the create request.
     *
     * @return the create resource
     * @throws Exception exceptions from Azure
     */
    T create() throws Exception;

    /**
     * Puts the request into the queue and allow the HTTP client to execute
     * it when system resources are available.
     *
     * @param callback the callback to handle success and failure
     * @return a handle to cancel the request
     */
    ServiceCall<T> createAsync(final ServiceCallback<T> callback);

    /**
     * Puts the request into the queue and allow the HTTP client to execute
     * it when system resources are available.
     *
     * @return a handle to cancel the request
     */
    Observable<T> createAsync();
}