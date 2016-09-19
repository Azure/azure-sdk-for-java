/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;

/**
 * An interface representing a model's ability to reference a subnet by its name and network's ID.
 */
@LangDefinition()
public interface HasSubnet {
    /**
     * @return the resource ID of the virtual network whose subnet is associated with this resource
     */
    String networkId();

    /**
     * @return the name of the subnet associated with this resource
     */
    String subnetName();

    /**
     * Grouping of definition stages involving associating an existing subnet with a resource.
     */
    @LangDefinition(
                ContainerName = "Definition",
                ContainerFileName = "IDefinition",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to associate a subnet with a resource.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }

    /**
     * Grouping of update stages involving associating an existing subnet with a resource.
     */
    @LangDefinition(
                ContainerName = "Update",
                ContainerFileName = "IUpdate",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface UpdateStages {
        /**
         * The stage of an update allowing to associate a subnet with a resource.
         * @param <ReturnT> the next stage of the update
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update, involving associating a subnet with a resource.
     */
    @LangDefinition(
                ContainerName = "UpdateDefinition",
                ContainerFileName = "IUpdateDefinition",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to associate a subnet with a resource.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithSubnet<ReturnT> {
            /**
             * Assigns the specified subnet to this resource.
             * @param parentNetworkResourceId the resource ID of the virtual network the subnet is part of
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            ReturnT withExistingSubnet(String parentNetworkResourceId, String subnetName);
        }
    }
}
