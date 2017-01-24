/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;

/**
 * Base class for independent child resource collection class.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 * @param <ParentT> the type of the parent resource
 */
@LangDefinition
public abstract class IndependentChildResourcesImpl<
        T extends IndependentChildResource<ManagerT>,
        ImplT extends T,
        InnerT,
        InnerCollectionT,
        ManagerT extends ManagerBase,
        ParentT extends GroupableResource<ManagerT>>
    extends IndependentChildrenImpl<T, ImplT, InnerT, InnerCollectionT, ManagerT, ParentT> {

    protected IndependentChildResourcesImpl(InnerCollectionT innerCollection, ManagerT manager) {
        super(innerCollection, manager);
    }
}
