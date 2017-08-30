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

import java.util.List;

/**
 * A client-side representation allowing user to verify the possibility of establishing a direct TCP connection
 * from a virtual machine to a given endpoint including another VM or an arbitrary remote server.
 */
@Fluent
@Beta
public interface ConnectivityCheck extends Executable<ConnectivityCheck>,
        HasParent<NetworkWatcher> {
    /**
     * @return list of hops between the source and the destination
     */
    List<ConnectivityHop> hops();

    /**
     * @return the connection status
     */
    ConnectionStatus connectionStatus();

    /**
     * @return average latency in milliseconds
     */
    int avgLatencyInMs();

    /**
     * @return minimum latency in milliseconds
     */
    int minLatencyInMs();

    /**
     * @return maximum latency in milliseconds
     */
    int maxLatencyInMs();

    /**
     * @return total number of probes sent
     */
    int probesSent();

    /**
     * @return number of failed probes
     */
    int probesFailed();

    /**
     * The entirety of next hop parameters definition.
     */
    interface Definition extends
            DefinitionStages.WithSource,
            DefinitionStages.WithDestination,
            DefinitionStages.WithDestinationPort,
            DefinitionStages.WithExecute {
    }

    /**
     * Grouping of connectivity check parameters definition stages.
     */
    interface DefinitionStages {
        /**
         * Sets the source property.
         */
        interface WithSource {
            WithDestination withSourceResourceId(String resourceId);
        }

        /**
         * Sets the destination property.
         */
        interface WithDestination {
            /**
             * @param resourceId the ID of the resource to which a connection attempt will be made
             * @return next definition stage
             */
            WithDestinationPort withDestinationResourceId(String resourceId);

            /**
             * @param address the IP address or URI the resource to which a connection attempt will be made
             * @return next definition stage
             */
            WithDestinationPort withDestinationAddress(String address);
        }

        /**
         * Sets the destination port on which check connectivity will be performed.
         */
        interface WithDestinationPort {
            /**
             * @param port destination port
             * @return next definition stage
             */
            WithExecute withDestinationPort(int port);
        }

        /**
         * Sets the source port from which a connectivity check will be performed.
         */
        interface  WithSourcePort {
            /**
             * @param port source port
             * @return next definition stage
             */
            WithExecute withSourcePort(int port);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows
         * for any other optional settings to be specified.
         */
        interface WithExecute extends
                Executable<ConnectivityCheck>,
                DefinitionStages.WithSourcePort {
        }
    }
}
