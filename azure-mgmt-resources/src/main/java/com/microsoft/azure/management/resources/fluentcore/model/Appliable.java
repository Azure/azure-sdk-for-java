/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * The base interface for all template interfaces that support update operations.
 *
 * @param <T> the type of the resource returned from the update.
 */
@LangDefinition(ContainerName = "ResourceActions", CreateAsyncMultiThreadMethodParam = true)
public interface Appliable<T> extends Indexable {
    /**
     * Execute the update request.
     *
     * @return the updated resource
     */
    @Method
    T apply();

    /**
     * Execute the update request asynchronously.
     *
     * @return the handle to the REST call
     */
    @Method
    Observable<T> applyAsync();

    /**
     * Execute the update request asynchronously.
     *
     * @param callback the callback for success and failure
     * @return the handle to the REST call
     */
    ServiceFuture<T> applyAsync(final ServiceCallback<T> callback);
}
