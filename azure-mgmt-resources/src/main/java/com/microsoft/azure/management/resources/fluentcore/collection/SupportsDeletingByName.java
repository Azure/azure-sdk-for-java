/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.collection;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceCallback;
import rx.Completable;

/**
 * Provides access to deleting a resource from Azure, identifying it by its resource name.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 */
@LangDefinition(ContainerName = "CollectionActions", CreateAsyncMethods = true)
public interface SupportsDeletingByName {
    /**
     * Deletes a resource from Azure, identifying it by its resource name.
     *
     * @param name the name of the resource to delete
     */
    void deleteByName(String name);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its resource name.
     *
     * @param name the name of the resource to delete
     * @param callback the callback on success or failure
     * @return a handle to cancel the request
     */
    ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback);

    /**
     * Asynchronously delete a resource from Azure, identifying it by its resource name.
     *
     * @param name the name of the resource to delete
     * @return a representation of the deferred computation of this call
     */
    Completable deleteByNameAsync(String name);
}
