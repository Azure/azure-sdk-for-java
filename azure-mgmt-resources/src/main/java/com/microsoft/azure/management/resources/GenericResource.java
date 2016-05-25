/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

public interface GenericResource extends
        GroupableResource,
        Refreshable<GenericResource>,
        Wrapper<GenericResourceInner> {

    /**
     * Get the plan of the resource.
     *
     * @return the plan of the resource.
     */
    Plan plan();

    /**
     * Get the resource properties.
     *
     * @return the resource properties.
     */
    Object properties();

    /**
     * A generic resource definition allowing region to be specified.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * A generic resource definition allowing resource group to be specified.
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithProviderNamespace> {
    }

    /**
     * A generic resource definition allowing provider namespace to be specified.
     */
    interface DefinitionWithProviderNamespace {
        DefinitionWithOrWithoutParentResource withProviderNamespace(String resourceProviderNamespace);
    }

    /**
     * A generic resource definition allowing parent resource to be specified.
     */
    interface DefinitionWithOrWithoutParentResource extends DefinitionWithPlan {
        DefinitionWithPlan withParentResource(String parentResourceId); // ParentResource is optional so user can navigate to DefinitionWithPlan with or without it.
    }

    /**
     * A generic resource definition allowing plan to be specified.
     */
    interface DefinitionWithPlan {
        DefinitionCreatable withPlan(String name, String publisher, String product, String promotionCode);
    }

    /**
     * A deployment definition with sufficient inputs to create a new
     * resource in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends
            Creatable<GenericResource>,
            Resource.DefinitionWithTags<DefinitionCreatable> {
        DefinitionCreatable withProperties(Object properties);
    }
}