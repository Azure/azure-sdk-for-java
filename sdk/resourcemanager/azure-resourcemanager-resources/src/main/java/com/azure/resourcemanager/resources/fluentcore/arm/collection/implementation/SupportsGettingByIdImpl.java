// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;

/**
 * Provides access to getting a specific Azure resource based on its resource ID.
 *
 * @param <T> the type of the resource collection
 */
public abstract class SupportsGettingByIdImpl<T> implements SupportsGettingById<T> {
    @Override
    public T getById(String id) {
        return getByIdAsync(id).block();
    }
}
