/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Base interface for resources in resource groups.
 */
public interface GroupableResource extends Resource {
    /**
     * @return the name of the resource group
     */
    String resourceGroupName();

    /**
     * Grouping of all the definition stages.
     */
    interface DefinitionStages {
        /**
         * A resource definition allowing a resource group to be selected.
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithGroup<T> extends
            WithExistingResourceGroup<T>,
            WithNewResourceGroup<T> {
        }

        /**
         * A resource definition allowing a new resource group to be created.
         *
         * @param <T> the next stage of the resource definition
         */
        interface WithNewResourceGroup<T> {
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
