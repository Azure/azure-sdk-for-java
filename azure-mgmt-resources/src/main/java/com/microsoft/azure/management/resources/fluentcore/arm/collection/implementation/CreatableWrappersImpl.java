/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

/**
 * Base class for creatable wrapper collections, i.e. those where a new member of the collection can be created.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class CreatableWrappersImpl<T, ImplT extends T, InnerT>
    extends ReadableWrappersImpl<T, ImplT, InnerT> {

    protected CreatableWrappersImpl() {
    }

    protected abstract ImplT wrapModel(String name);
}
