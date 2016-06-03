/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

/**
 * An immutable client-side representation of an Azure generic resource.
 */
public interface GenericResource extends
        GroupableResource,
        Refreshable<GenericResource>,
        Updatable<GenericResource.UpdateWithApiVersion>,
        Wrapper<GenericResourceInner> {
    /**
     * @return the namespace of the resource provider
     */
    String resourceProviderNamespace();

    /**
     * @return the id of the parent resource if this is a child resource
     */
    String parentResourceId();

    /**
     * @return the type of the resource
     */
    String resourceType();

    /**
     * @return the api version of the resource
     */
    String apiVersion();

    /**
     * @return the plan of the resource
     */
    Plan plan();

    /**
     * @return other properties of the resource
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
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithResourceType> {
    }

    /**
     * A generic resource definition allowing resource type to be specified.
     */
    interface DefinitionWithResourceType {
        /**
         * Specifies the resource's type.
         *
         * @param resourceType the type of the resources
         * @return the next stage of generic resource definition
         */
        DefinitionWithProviderNamespace withResourceType(String resourceType);
    }

    /**
     * A generic resource definition allowing provider namespace to be specified.
     */
    interface DefinitionWithProviderNamespace {
        /**
         * Specifies the resource provider's namespace.
         *
         * @param resourceProviderNamespace the namespace of the resource provider
         * @return the next stage of the generic resource definition
         */
        DefinitionWithOrWithoutParentResource withProviderNamespace(String resourceProviderNamespace);
    }

    /**
     * A generic resource definition allowing parent resource to be specified.
     */
    interface DefinitionWithOrWithoutParentResource extends DefinitionWithPlan {
        /**
         * Specifies the parent resource.
         *
         * @param parentResourceId the parent resource id
         * @return the next stage of the generic resource definition
         */
        DefinitionWithPlan withParentResource(String parentResourceId); // ParentResource is optional so user can navigate to DefinitionWithPlan with or without it.
    }

    /**
     * A generic resource definition allowing plan to be specified.
     */
    interface DefinitionWithPlan {
        /**
         * Specifies the plan of the resource. The plan can only be set for 3rd party resources.
         *
         * @param name the name of the plan
         * @param publisher the publisher of the plan
         * @param product the name of the product
         * @param promotionCode the promotion code, if any
         * @return the next stage of the generic resource definition
         */
        DefinitionWithApiVersion withPlan(String name, String publisher, String product, String promotionCode);

        /**
         * Specifies the plan of the resource.
         *
         * @return the next stage of the generic resource definition
         */
        DefinitionWithApiVersion withoutPlan();
    }

    /**
     * A generic resource definition allowing api version to be specified.
     */
    interface DefinitionWithApiVersion {
        /**
         * Specifies the api version.
         *
         * @param apiVersion the API version of the resource
         * @return the next stage of the generic resource definition
         */
        DefinitionCreatable withApiVersion(String apiVersion);
    }

    /**
     * A deployment definition with sufficient inputs to create a new
     * resource in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends
            Creatable<GenericResource>,
            Resource.DefinitionWithTags<DefinitionCreatable> {
        /**
         * Specifies other properties.
         *
         * @param properties the properties object
         * @return the next stage of generic resource definition
         */
        DefinitionCreatable withProperties(Object properties);
    }

    /**
     * A generic resource update allowing to change the resource properties.
     */
    interface UpdateWithProperties {
        /**
         * Specifies other properties of the resource.
         *
         * @param properties the properties object
         * @return the next stage of generic resource update
         */
        Update withProperties(Object properties);
    }

    /**
     * A generic resource update allowing to change the parent resource.
     */
    interface UpdateWithParentResource {
        /**
         * Specifies the parent resource.
         *
         * @param parentResourceId the parent resource ID
         * @return the next stage of the generic resource definition
         */
        Update withParentResource(String parentResourceId);
    }

    /**
     * A generic resource update allowing to change the resource plan.
     */
    interface UpdateWithPlan {
        /**
         * Specifies the plan of the resource.
         *
         * @param name the name of the plan
         * @param publisher the publisher of the plan
         * @param product the name of the product
         * @param promotionCode the promotion code, if any
         * @return the next stage of the generic resource update
         */
        Update withPlan(String name, String publisher, String product, String promotionCode);

        /**
         * Specifies the plan of the resource.
         *
         * @return the next stage of the generic resource update
         */
        Update withoutPlan();
    }

    /**
     * The template for a generic resource update operation for specifying the resource provider API version.
     */
    interface UpdateWithApiVersion {
        /**
         * Specifies the API version of the resource provider.
         *
         * @return the next stage of the generic resource update
         */
        Update withApiVersion(String apiVersion);
    }

    /**
     * The template for a generic resource update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<GenericResource>,
            UpdateWithPlan,
            UpdateWithParentResource,
            UpdateWithProperties,
            Resource.UpdateWithTags<Update> {
    }
}