// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import java.util.List;

/**
 * A client-side representation allowing user to verify the possibility of establishing a direct TCP connection from a
 * virtual machine to a given endpoint including another VM or an arbitrary remote server.
 */
@Fluent
public interface ConnectivityCheck extends Executable<ConnectivityCheck>, HasParent<NetworkWatcher> {
    /**
     * Gets list of hops between the source and the destination.
     *
     * @return list of hops between the source and the destination
     */
    List<ConnectivityHop> hops();

    /**
     * Gets the connection status.
     *
     * @return the connection status
     */
    ConnectionStatus connectionStatus();

    /**
     * Gets average latency in milliseconds.
     *
     * @return average latency in milliseconds
     */
    int avgLatencyInMs();

    /**
     * Gets minimum latency in milliseconds.
     *
     * @return minimum latency in milliseconds
     */
    int minLatencyInMs();

    /**
     * Gets maximum latency in milliseconds.
     *
     * @return maximum latency in milliseconds
     */
    int maxLatencyInMs();

    /**
     * Gets total number of probes sent.
     *
     * @return total number of probes sent
     */
    int probesSent();

    /**
     * Gets number of failed probes.
     *
     * @return number of failed probes
     */
    int probesFailed();

    /** The entirety of connectivity check parameters definition. */
    interface Definition extends DefinitionStages.ToDestination, DefinitionStages.ToDestinationPort,
        DefinitionStages.FromSourceVirtualMachine, DefinitionStages.WithExecute {
    }

    /** Grouping of connectivity check parameters definition stages. */
    interface DefinitionStages {
        /** Sets the source property. */
        interface FromSourceVirtualMachine {
            /**
             * Specifies the ID of the virtual machine from which a connectivity check will be initiated.
             *
             * @param resourceId the ID of the virtual machine from which a connectivity check will be initiated
             * @return next definition stage
             */
            WithExecute fromSourceVirtualMachine(String resourceId);

            /**
             * Specifies virtual machine from which a connectivity check will be initiated.
             *
             * @param vm virtual machine from which a connectivity check will be initiated
             * @return next definition stage
             */
            WithExecute fromSourceVirtualMachine(HasNetworkInterfaces vm);
        }

        /** Sets the destination property. */
        interface ToDestination {
            /**
             * Specifies the ID of the resource to which a connection attempt will be made.
             *
             * @param resourceId the ID of the resource to which a connection attempt will be made
             * @return next definition stage
             */
            ToDestinationPort toDestinationResourceId(String resourceId);

            /**
             * Specifies the IP address or URI the resource to which a connection attempt will be made.
             *
             * @param address the IP address or URI the resource to which a connection attempt will be made
             * @return next definition stage
             */
            ToDestinationPort toDestinationAddress(String address);
        }

        /** Sets the destination port on which check connectivity will be performed. */
        interface ToDestinationPort {
            /**
             * Specifies destination port.
             *
             * @param port destination port
             * @return next definition stage
             */
            FromSourceVirtualMachine toDestinationPort(int port);
        }

        /** Sets the source port from which a connectivity check will be performed. */
        interface FromSourcePort {
            /**
             * Specifies source port.
             *
             * @param port source port
             * @return next definition stage
             */
            WithExecute fromSourcePort(int port);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows for
         * any other optional settings to be specified.
         */
        interface WithExecute extends Executable<ConnectivityCheck>, FromSourcePort,
            HasProtocol.DefinitionStages.WithProtocol<WithExecute, Protocol> {
        }
    }
}
