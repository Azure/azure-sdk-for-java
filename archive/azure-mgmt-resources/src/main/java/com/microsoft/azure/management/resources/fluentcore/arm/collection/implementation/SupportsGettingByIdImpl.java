/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;

/**
 * Provides access to getting a specific Azure resource based on its resource ID.
 *
 * @param <T> the type of the resource collection
 */
public abstract class SupportsGettingByIdImpl<T> implements SupportsGettingById<T> {
    @Override
    public T getById(String id) {
        return getByIdAsync(id).toBlocking().last();
    }

    @Override
    public ServiceFuture<T> getByIdAsync(String id, ServiceCallback<T> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }
}
