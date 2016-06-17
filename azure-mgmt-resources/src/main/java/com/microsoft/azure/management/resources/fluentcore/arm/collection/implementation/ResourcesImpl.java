/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import java.io.IOException;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 */
public abstract class ResourcesImpl<T, ImplT>
    implements
        SupportsGettingById<T>,
        SupportsGettingByGroup<T> {

    @Override
    public abstract T getByGroup(String groupName, String name) throws CloudException, IOException;

    @Override
    public final T getById(String id) throws CloudException, IOException {
        return this.getByGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    protected abstract ImplT createFluentModel(String name);
}
