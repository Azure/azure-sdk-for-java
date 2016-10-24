/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Base interface for resources in resource groups.
 */
@Fluent()
public interface GroupableResource extends Resource, HasResourceGroup {


    /**
     * Grouping of all the definition stages.
     */
    interface DefinitionStages {
        /**
         * A resource definition allowing a resource group to be selected.
         * <p>
         * Region of the groupable resource will be used for new resource group
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithGroup<T> extends
            WithExistingResourceGroup<T>,
            WithNewResourceGroup<T> {
        }

        /**
         * A resource definition allowing a resource group to be selected.
         * <p>
         * Region has to be specified explicitly to create a new resource group
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithGroupAndRegion<T> extends
                WithExistingResourceGroup<T>,
                WithNewResourceGroupWithRegion<T> {
        }

        /**
         * A resource definition allowing a new resource group to be created in the same
         * region as groupable resource.
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithNewResourceGroup<T> extends
                WithCreatableResourceGroup<T> {
            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * @param name the name of the new group
             * @return the next stage of the resource definition
             */
            T withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * The group's name is automatically derived from the resource's name.
             * @return the next stage of the resource definition
             */
            T withNewResourceGroup();
        }

        /**
         * A resource definition allowing a new resource group to be created in a different
         * region .
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithNewResourceGroupWithRegion<T> extends
                WithCreatableResourceGroup<T> {
            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             *
             * @param name the name of the new group
             * @param region the region where resource group needs to be created
             * @return the next stage of the resource definition
             */
            T withNewResourceGroup(String name, Region region);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * The group's name is automatically derived from the resource's name.
             *
             * @param region the region where resource group needs to be created
             * @return the next stage of the resource definition
             */
            T withNewResourceGroup(Region region);
        }

        /**
         * A resource definition allowing a new creatable resource group to be specified.
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithCreatableResourceGroup<T> {
            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the resource definition
             */
            T withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /**
         * A resource definition allowing an existing resource group to be selected.
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithExistingResourceGroup<T> {
            /**
             * Associates the resource with an existing resource group.
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the resource definition
             */
            T withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             * @param group an existing resource group to put the resource in
             * @return the next stage of the resource definition
             */
            T withExistingResourceGroup(ResourceGroup group);
        }
    }
}
