/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.Executable;

/**
 * A client-side representation allowing user to get next hop for a packet from specific vm.
 */
@Fluent
@Beta
public interface NextHop extends Executable<NextHop>,
        HasParent<NetworkWatcher> {
    /**
     * Get the resource identifier of the target resource against which the action
     * is to be performed.
     *
     * @return the targetResourceId value
     */
    String targetResourceId();

    /**
     * @return the source IP address
     */
    String sourceIPAddress();

    /**
     * @return the destination IP address
     */
    String destinationIPAddress();

    /**
     * @return the network interface id
     */
    String targetNetworkInterfaceId();

    /**
     * @return the next hop type
     */
    NextHopType nextHopType();

    /**
     * @return the next hop IP Address
     */
    String nextHopIpAddress();

    /**
     * Get the resource identifier for the route table associated with the route
     * being returned. If the route being returned does not correspond to any
     * user created routes then this field will be the string 'System Route'.
     *
     * @return the routeTableId value
     */
    String routeTableId();

    /**
     * The entirety of next hop parameters definition.
     */
    interface Definition extends
            DefinitionStages.WithTargetResource,
            DefinitionStages.WithSourceIP,
            DefinitionStages.WithDestinationIP,
            DefinitionStages.WithExecute {
    }


    /**
     * Grouping of virtual machine definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a virtual machine definition.
         */
        interface WithTargetResource {
            /**
             * Set the targetResourceId value.
             *
             * @param vmId the targetResourceId value to set
             * @return the VerificationIPFlow object itself.
             */
            WithSourceIP withTargetResourceId(String vmId);
        }

        /**
         * Sets the source IP address.
         */
        interface WithSourceIP {
            /**
             * Set the sourceIPAddress value.
             *
             * @param sourceIPAddress the sourceIPAddress value to set
             * @return the VerificationIPFlow object itself.
             */
            WithDestinationIP withSourceIPAddress(String sourceIPAddress);
        }

        /**
         * Sets the destination IP address.
         */
        interface WithDestinationIP {
            /**
             * Set the destinationIPAddress value.
             *
             * @param destinationIPAddress the destinationIPAddress value to set
             * @return the VerificationIPFlow object itself.
             */
            WithExecute withDestinationIPAddress(String destinationIPAddress);
        }

        /**
         * Sets the NIC ID. (If VM has multiple NICs and IP forwarding is enabled on any
         * of the nics, then this parameter must be specified. Otherwise optional).
         */
        interface WithNetworkInterface {
            /**
             * Set the targetNetworkInterfaceId value.
             *
             * @param targetNetworkInterfaceId the targetNetworkInterfaceId value to set
             * @return the VerificationIPFlow object itself.
             */
            WithExecute withTargetNetworkInterfaceId(String targetNetworkInterfaceId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows
         * for any other optional settings to be specified.
         */
        interface WithExecute extends
                Executable<NextHop>,
                WithNetworkInterface {
        }
    }
}
