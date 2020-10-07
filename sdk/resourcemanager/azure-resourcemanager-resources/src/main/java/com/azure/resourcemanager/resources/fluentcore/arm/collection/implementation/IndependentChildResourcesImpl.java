// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;

/**
 * Base class for independent child resource collection class.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 * @param <ParentT> the type of the parent resource
 */
public abstract class IndependentChildResourcesImpl<
        T extends IndependentChildResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT,
        InnerCollectionT,
        ManagerT extends Manager<?>,
        ParentT extends Resource & HasResourceGroup>
        extends IndependentChildrenImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT, ParentT> {

    protected IndependentChildResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }
}
