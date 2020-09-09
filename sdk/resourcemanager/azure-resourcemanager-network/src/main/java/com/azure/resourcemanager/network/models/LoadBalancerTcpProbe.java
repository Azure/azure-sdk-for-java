// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of a TCP load balancing probe. */
@Fluent()
public interface LoadBalancerTcpProbe extends LoadBalancerProbe {

    /** Grouping of probe definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the probe definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithPort<ParentT> {
        }

        /**
         * The stage of the TCP probe definition allowing to specify the port number to monitor.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPort<ParentT> {
            /**
             * Specifies the port number to call for health monitoring.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPort(int port);
        }

        /**
         * The stage of the TCP probe definition allowing to specify the probe interval.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithIntervalInSeconds<ParentT> {
            /**
             * Specifies the interval between probes, in seconds.
             *
             * @param seconds number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIntervalInSeconds(int seconds);
        }

        /**
         * The stage of the TCP probe definition allowing to specify the number of unsuccessful probes before failure is
         * determined.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithNumberOfProbes<ParentT> {
            /**
             * Specifies the number of unsuccessful probes before failure is determined.
             *
             * @param probes number of probes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withNumberOfProbes(int probes);
        }

        /**
         * The final stage of the probe definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the probe definition can be attached
         * to the parent load balancer definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>, WithIntervalInSeconds<ParentT>, WithNumberOfProbes<ParentT> {
        }
    }

    /**
     * The entirety of a probe definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithPort<ParentT> {
    }

    /** Grouping of probe update stages. */
    interface UpdateStages {
        /** The stage of the TCP probe update allowing to modify the port number to monitor. */
        interface WithPort {
            /**
             * Specifies the port number to call for health monitoring.
             *
             * @param port a port number
             * @return the next stage of the update
             */
            Update withPort(int port);
        }

        /** The stage of the TCP probe update allowing to modify the probe interval. */
        interface WithIntervalInSeconds {
            /**
             * Specifies the interval between probes, in seconds.
             *
             * @param seconds number of seconds
             * @return the next stage of the update
             */
            Update withIntervalInSeconds(int seconds);
        }

        /**
         * The stage of the TCP probe update allowing to modify the number of unsuccessful probes before failure is
         * determined.
         */
        interface WithNumberOfProbes {
            /**
             * Specifies the number of unsuccessful probes before failure is determined.
             *
             * @param probes number of probes
             * @return the next stage of the update
             */
            Update withNumberOfProbes(int probes);
        }
    }

    /** The entirety of a probe update as part of a load balancer update. */
    interface Update
        extends Settable<LoadBalancer.Update>,
            UpdateStages.WithPort,
            UpdateStages.WithIntervalInSeconds,
            UpdateStages.WithNumberOfProbes {
    }

    /** Grouping of probe definition stages applicable as part of a load balancer update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the probe definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithPort<ParentT> {
        }

        /**
         * The stage of the TCP probe definition allowing to specify the port number to monitor.
         *
         * @param <ParentT> the parent resource type
         */
        interface WithPort<ParentT> {
            /**
             * Specifies the port number to call for health monitoring.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withPort(int port);
        }

        /**
         * The stage of the TCP probe definition allowing to specify the probe interval.
         *
         * @param <ParentT> the parent resource type
         */
        interface WithIntervalInSeconds<ParentT> {
            /**
             * Specifies the interval between probes, in seconds.
             *
             * @param seconds number of seconds
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIntervalInSeconds(int seconds);
        }

        /**
         * The stage of the TCP probe definition allowing to specify the number of unsuccessful probes before failure is
         * determined.
         *
         * @param <ParentT> the parent resource type
         */
        interface WithNumberOfProbes<ParentT> {
            /**
             * Specifies the number of unsuccessful probes before failure is determined.
             *
             * @param probes number of probes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withNumberOfProbes(int probes);
        }

        /**
         * The final stage of the probe definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the probe definition can be attached
         * to the parent load balancer definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdate<ParentT>, WithNumberOfProbes<ParentT>, WithIntervalInSeconds<ParentT> {
        }
    }

    /**
     * The entirety of a probe definition as part of a load balancer update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithPort<ParentT> {
    }
}
