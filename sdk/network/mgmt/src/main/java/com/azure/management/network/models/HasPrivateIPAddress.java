/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.IPAllocationMethod;

/**
 * An interface representing a model's ability to reference a private IP address.
 */
@Fluent()
public interface HasPrivateIPAddress  {
    /**
     * @return the private IP address associated with this resource
     */
    String privateIPAddress();

    /**
     * @return the private IP address allocation method within the associated subnet
     */
    IPAllocationMethod privateIPAllocationMethod();

    /**
     * Grouping of definition stages involving specifying the private IP address.
     */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the private IP address.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the definition
             */
            ReturnT withPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIPAddressStatic(String ipAddress);
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
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the update
             */
            ReturnT withPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the update
             */
            ReturnT withPrivateIPAddressStatic(String ipAddress);
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
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             * @return the next stage of the definition
             */
            ReturnT withPrivateIPAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIPAddressStatic(String ipAddress);
        }
    }
}
