// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;

/**
 * Base class for creatable wrapper collections, i.e. those where a new member of the collection can be created.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class CreatableWrappersImpl<T, ImplT extends T, InnerT>
        extends ReadableWrappersImpl<T, ImplT, InnerT>
        implements
        // Assume anything creatable is deletable
        SupportsDeletingById {

    protected CreatableWrappersImpl() {
    }

    protected abstract ImplT wrapModel(String name);

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }
}
