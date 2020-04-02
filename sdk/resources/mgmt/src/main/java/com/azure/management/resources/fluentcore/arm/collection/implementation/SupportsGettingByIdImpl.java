/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */


package com.azure.management.resources.fluentcore.arm.collection.implementation;

import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;

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
