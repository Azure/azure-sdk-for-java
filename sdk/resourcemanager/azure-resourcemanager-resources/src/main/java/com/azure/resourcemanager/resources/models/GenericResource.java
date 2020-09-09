// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluent.inner.GenericResourceInner;

/**
 * An immutable client-side representation of an Azure generic resource.
 */
@Fluent
public interface GenericResource extends
        GroupableResource<ResourceManager, GenericResourceInner>,
        Refreshable<GenericResource>,
        Updatable<GenericResource.Update> {
    /**
     * @return the namespace of the resource provider
     */
    String resourceProviderNamespace();

    /**
     * @return the id of the parent resource if this is a child resource
     */
    String parentResourcePath();

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
     * The entirety of the generic resource definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithResourceType,
            DefinitionStages.WithProviderNamespace,
            DefinitionStages.WithParentResource,
            DefinitionStages.WithPlan,
            DefinitionStages.WithApiVersion,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of generic resource definition stages.
     */
    interface DefinitionStages {
        /**
         * A generic resource definition allowing region to be specified.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A generic resource definition allowing resource group to be specified.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithResourceType> {
        }

        /**
         * A generic resource definition allowing resource type to be specified.
         */
        interface WithResourceType {
            /**
             * Specifies the resource's type.
             *
             * @param resourceType the type of the resources
             * @return the next stage of generic resource definition
             */
            WithProviderNamespace withResourceType(String resourceType);
        }

        /**
         * A generic resource definition allowing provider namespace to be specified.
         */
        interface WithProviderNamespace {
            /**
             * Specifies the resource provider's namespace.
             *
             * @param resourceProviderNamespace the namespace of the resource provider
             * @return the next stage of the definition
             */
            WithPlan withProviderNamespace(String resourceProviderNamespace);
        }

        /**
         * A generic resource definition allowing plan to be specified.
         */
        interface WithPlan {
            /**
             * Specifies the plan of the resource. The plan can only be set for 3rd party resources.
             *
             * @param name the name of the plan
             * @param publisher the publisher of the plan
             * @param product the name of the product
             * @param promotionCode the promotion code, if any
             * @return the next stage of the definition
             */
            WithCreate withPlan(String name, String publisher, String product, String promotionCode);

            /**
             * Specifies the plan of the resource.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutPlan();
        }

        /**
         * A generic resource definition allowing api version to be specified.
         */
        interface WithApiVersion {
            /**
             * Specifies the api version.
             *
             * @param apiVersion the API version of the resource
             * @return the next stage of the definition
             */
            WithCreate withApiVersion(String apiVersion);
        }

        /**
         * A generic resource definition allowing parent resource to be specified.
         */
        interface WithParentResource {
            /**
             * Specifies the parent resource.
             *
             * @param parentResourceId the parent resource id
             * @return the next stage of the definition
             */
            WithCreate withParentResourceId(String parentResourceId);

            /**
             * Specifies the parent resource relative path.
             *
             * @param parentResourcePath the relative path of parent resource
             * @return the next stage of the definition
             */
            WithCreate withParentResourcePath(String parentResourcePath);
        }

        /**
         * A deployment definition with sufficient inputs to create a new
         * resource in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                WithParentResource,
                WithApiVersion,
                Creatable<GenericResource>,
                Resource.DefinitionWithTags<WithCreate> {
            /**
             * Specifies other properties.
             *
             * @param properties the properties object
             * @return the next stage of generic resource definition
             */
            WithCreate withProperties(Object properties);

            /**
             * Begins creating the Azure resource.
             *
             * @return the accepted create operation
             */
            Accepted<GenericResource> beginCreate();
        }
    }

    /**
     * Grouping of generic resource update stages.
     */
    interface UpdateStages {
        /**
         * A generic resource update allowing to change the resource properties.
         */
        interface WithProperties {
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
        interface WithParentResource {
            /**
             * Specifies the parent resource.
             *
             * @param parentResourceId the parent resource id
             * @return the next stage of the generic resource update
             */
            Update withParentResourceId(String parentResourceId);

            /**
             * Specifies the parent resource relative path.
             *
             * @param parentResourcePath the relative path of parent resource
             * @return the next stage of the generic resource update
             */
            Update withParentResourcePath(String parentResourcePath);
        }

        /**
         * A generic resource update allowing to change the resource plan.
         */
        interface WithPlan {
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
        interface WithApiVersion {
            /**
             * Specifies the API version of the resource provider.
             *
             * @param apiVersion the API version
             * @return the next stage of the generic resource update
             */
            Update withApiVersion(String apiVersion);
        }
    }

    /**
     * The template for a generic resource update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<GenericResource>,
            UpdateStages.WithApiVersion,
            UpdateStages.WithPlan,
            UpdateStages.WithParentResource,
            UpdateStages.WithProperties,
            Resource.UpdateWithTags<Update> {
    }
}
