/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.network.IPAllocationMethod;

/**
 * An interface representing a model's ability to reference a private IP address.
 */
public interface HasPrivateIpAddress  {
    /**
     * @return the protocol
     */
    String privateIpAddress();

    /**
     * @return the private IP address allocation method within the associated subnet
     */
    IPAllocationMethod privateIpAllocationMethod();

    /**
     * Grouping of definition stages involving specifying the private IP address.
     */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the private IP address.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPrivateIpAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }

    /**
     * Grouping of update stages involving modifying the private IP address.
     */
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify the private IP address.
         * @param <ReturnT> the next stage of the update
         */
        interface WithPrivateIpAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the update
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the update
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update, involving specifying the private IP address.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the private IP address.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPrivateIpAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }
}
