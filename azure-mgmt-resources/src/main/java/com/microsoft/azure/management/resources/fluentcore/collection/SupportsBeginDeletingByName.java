/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;

/**
 * Provides access to deleting a resource from Azure, identifying it by its resource ID.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true)
public interface SupportsBeginDeletingByName {
    /**
     * Begins deleting a resource from Azure, identifying it by its resource name. The
     * resource will stay until get() returns null.
     *
     * @param name the name of the resource to delete
     */
    void beginDeleteByName(String name);

    /**
     * Asynchronously begins deleting a resource from Azure, identifying it by its resource name.
     * The resource will stay until get() returns null.
     *
     * @param name the name of the resource to delete
     * @param callback the callback on success or failure
     * @return a handle to cancel the request
     */
    @Beta
    ServiceFuture<Void> beginDeleteByNameAsync(String name, ServiceCallback<Void> callback);

    /**
     * Asynchronously begins deleting a resource from Azure, identifying it by its resource name.
     * The resource will stay until get() returns null.
     *
     * @param name the name the resource to delete
     * @return an observable of the request
     */
    @Beta
    Observable<Void> beginDeleteByNameAsync(String name);
}
