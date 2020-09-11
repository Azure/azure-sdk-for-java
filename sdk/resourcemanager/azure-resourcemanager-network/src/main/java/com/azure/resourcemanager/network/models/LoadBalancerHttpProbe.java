// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of an HTTP load balancing probe. */
@Fluent()
public interface LoadBalancerHttpProbe extends LoadBalancerProbe {

    /** @return the HTTP request path for the HTTP probe to call to check the health status */
    String requestPath();

    /** Grouping of probe definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the probe definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithRequestPath<ParentT> {
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
            extends Attachable.InDefinition<ParentT>,
                WithPort<ParentT>,
                WithIntervalInSeconds<ParentT>,
                WithNumberOfProbes<ParentT> {
        }

        /**
         * The stage of the probe definition allowing to specify the HTTP request path for the probe to monitor.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRequestPath<ParentT> {
            WithAttach<ParentT> withRequestPath(String requestPath);
        }

        /**
         * The stage of the HTTP probe definition allowing to specify the probe interval.
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
         * The stage of the probe definition allowing to specify the port to monitor.
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
         * The stage of the HTTP probe definition allowing to specify the number of unsuccessful probes before failure
         * is determined.
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
    }

    /**
     * The entirety of a probe definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithRequestPath<ParentT> {
    }

    /** Grouping of probe update stages. */
    interface UpdateStages {
        /** The stage of the HTTP probe update allowing to modify the port number to monitor. */
        interface WithPort {
            /**
             * Specifies the port number to call for health monitoring.
             *
             * @param port a port number
             * @return the next stage of the update
             */
            Update withPort(int port);
        }

        /** The stage of the HTTP probe update allowing to modify the probe interval. */
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
         * The stage of the HTTP probe update allowing to modify the number of unsuccessful probes before failure is
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

        /** The stage of the HTTP probe update allowing to modify the HTTP request path for the probe to monitor. */
        interface WithRequestPath {
            /**
             * Specifies the HTTP request path for the probe to monitor.
             *
             * @param requestPath a request path
             * @return the next stage of the definition
             */
            Update withRequestPath(String requestPath);
        }
    }

    /** The entirety of a probe update as part of a load balancer update. */
    interface Update
        extends Settable<LoadBalancer.Update>,
            UpdateStages.WithIntervalInSeconds,
            UpdateStages.WithNumberOfProbes,
            UpdateStages.WithPort,
            UpdateStages.WithRequestPath {
    }

    /** Grouping of probe definition stages applicable as part of a load balancer update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the probe definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithRequestPath<ParentT> {
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
            extends Attachable.InUpdate<ParentT>,
                WithPort<ParentT>,
                WithIntervalInSeconds<ParentT>,
                WithNumberOfProbes<ParentT> {
        }

        /**
         * The stage of the probe definition allowing to specify the HTTP request path for the probe to monitor.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithRequestPath<ParentT> {
            /**
             * Specifies the HTTP request path for the probe to monitor.
             *
             * @param requestPath a request path
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRequestPath(String requestPath);
        }

        /**
         * The stage of the HTTP probe definition allowing to specify the probe interval.
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
         * The stage of the probe definition allowing to specify the port to monitor.
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
         * The stage of the HTTP probe definition allowing to specify the number of unsuccessful probes before failure
         * is determined.
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
    }

    /**
     * The entirety of a probe definition as part of a load balancer update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithRequestPath<ParentT> {
    }
}
