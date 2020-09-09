// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to reference a private IP address. */
@Fluent
public interface HasPrivateIpAddress {
    /** @return the private IP address associated with this resource */
    String privateIpAddress();

    /** @return the private IP address allocation method within the associated subnet */
    IpAllocationMethod privateIpAllocationMethod();

    /** Grouping of definition stages involving specifying the private IP address. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the private IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             *
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             *
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }

    /** Grouping of update stages involving modifying the private IP address. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify the private IP address.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             *
             * @return the next stage of the update
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             *
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the update
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a parent resource update, involving specifying the private IP
     * address.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the private IP address.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPrivateIPAddress<ReturnT> {
            /**
             * Enables dynamic private IP address allocation within the associated subnet.
             *
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressDynamic();

            /**
             * Assigns the specified static private IP address within the associated subnet.
             *
             * @param ipAddress a static IP address within the associated private IP range
             * @return the next stage of the definition
             */
            ReturnT withPrivateIpAddressStatic(String ipAddress);
        }
    }
}
