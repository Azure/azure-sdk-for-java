/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * The final stage of the resource definition, at which it can be created using create().
 *
 * @param <T> the fluent type of the resource to be created
 */
@LangDefinition(ContainerName = "ResourceActions", CreateAsyncMultiThreadMethodParam = true)
public interface Creatable<T> extends
    Indexable,
    HasName {

    /**
     * Execute the create request.
     *
     * @return the create resource
     */
    @Method
    T create();

    /**
     * Puts the request into the queue and allow the HTTP client to execute
     * it when system resources are available.
     *
     * @param callback the callback to handle success and failure
     * @return a handle to cancel the request
     */
    @Method
    ServiceFuture<T> createAsync(final ServiceCallback<T> callback);

    /**
     * Puts the request into the queue and allow the HTTP client to execute
     * it when system resources are available.
     *
     * @return an observable of the request
     */
    @Method
    Observable<Indexable> createAsync();
}