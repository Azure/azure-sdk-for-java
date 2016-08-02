/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.LoadBalancingRuleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an HTTP load balancing rule.
 */
public interface LoadBalancingRule extends
    Wrapper<LoadBalancingRuleInner>,
    ChildResource {

    /**
     * @return the transport protocol the load balancing rule applies to
     */
    TransportProtocol protocol();

    /**
     * @return true if floating IP is enabled, false otherwise
     */
    boolean floatingIp();

    //TODO:
    /* withLoadDistribution
     * withFrontend, withBackend, withProbe and return them
     */
    /**
     * @return the method of load distribution
     */
    LoadDistribution loadDistribution();

    /**
     * @return the number of minutes before an inactive connection is closed
     */
    int idleTimeoutInMinutes();

    /**
     * @return the load balanced front end port
     */
    int frontendPort();

    /**
     * @return the backed port the load balanced traffic is sent to
     */
    int backendPort();

    /**
     * Grouping of load balancing rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the load balancing rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the transport protocol to apply the rule to.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the transfer protocol to apply the load balancing rule to.
             * @param protocol a transfer protocol
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withProtocol(TransportProtocol protocol);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the front end port to load balance.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Specifies the frontend port to load balance.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int port);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the backend port to send the load-balanced traffic to.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithBackendPort<ParentT> {
            /**
             * Specifies the backend port to send the load-balanced traffic to.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBackendPort(int port);
        }

        /**
         * The final stage of the load balancing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the load balancing rule definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            DefinitionStages.WithBackendPort<ParentT>,
            DefinitionStages.WithFloatingIp<ParentT>,
            DefinitionStages.WithIdleTimeoutInMinutes<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to enable the floating IP functionality.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFloatingIp<ParentT> {
            /**
             * Controls the floating IP functionality.
             * @param enable set to true to turn on, false to turn off
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIp(boolean enable);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the connection timeout for idle connections.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithIdleTimeoutInMinutes<ParentT> {
            /**
             * Specifies the number of minutes before an idle connection is closed.
             * @param minutes the desired number of minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIdleTimeoutInMinutes(int minutes);
        }
    }

    /** The entirety of a load balancing rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithProtocol<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT> {
    }

    /**
     * Grouping of load balancing rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a load balancing rule update allowing to modify the transport protocol the rule applies to.
         */
        interface WithProtocol {
            /**
             * Specifies the transport protocol for the load balancing rule to apply to.
             * @param protocol a transport protocol
             * @return the next stage of the update
             */
            Update withProtocol(TransportProtocol protocol);
        }

        /**
         * The stage of a load balancing rule update allowing to modify the frontend port.
         */
        interface WithFrontendPort {
            /**
             * Specifies the frontend port to load balance.
             * @param port a port number
             * @return the next stage of the update
             */
            Update withFrontendPort(int port);
        }

        /**
         * The stage of a load balancing rule update allowing to modify the backend port.
         */
        interface WithBackendPort {
            /**
             * Specifies the backend port to send the load balanced traffic to.
             * @param port a port number
             * @return the next stage of the update
             */
            Update withBackendPort(int port);
        }

        /**
         * The stage of a load balancing rule update allowing to enable the floating IP functionality.
         */
        interface WithFloatingIp {
            /**
             * Controls the floating IP functionality.
             * @param enable set to true to turn on, false to turn off
             * @return the next stage of the update
             */
            Update withFloatingIp(boolean enable);
        }

        /**
         * The stage of a load balancing rule update allowing to modify the connection timeout for idle connections.
         */
        interface WithIdleTimeoutInMinutes {
            /**
             * Specifies the number of minutes before an idle connection is closed.
             * @param minutes the desired number of minutes
             * @return the next stage of the update
             */
            Update withIdleTimeoutInMinutes(int minutes);
        }
    }

    /**
     * The entirety of a load balancing rule update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithFrontendPort,
        UpdateStages.WithProtocol,
        UpdateStages.WithBackendPort,
        UpdateStages.WithFloatingIp,
        UpdateStages.WithIdleTimeoutInMinutes {
    }

    /**
     * Grouping of load balancing rule definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the load balancing rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the transport protocol to apply the rule to.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the transfer protocol to apply the load balancing rule to.
             * @param protocol a transfer protocol
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withProtocol(TransportProtocol protocol);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the front end port to load balance.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Specifies the frontend port to load balance.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int port);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the backend port to send the load-balanced traffic to.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithBackendPort<ParentT> {
            /**
             * Specifies the backend port to send the load-balanced traffic to.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBackendPort(int port);
        }

        /**
         * The stage of a load balancing rule definition allowing to enable the floating IP functionality.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFloatingIp<ParentT> {
            /**
             * Controls the floating IP functionality.
             * @param enable set to true to turn on, false to turn off
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIp(boolean enable);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the connection timeout for idle connections.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithIdleTimeoutInMinutes<ParentT> {
            /**
             * Specifies the number of minutes before an idle connection is closed.
             * @param minutes the desired number of minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIdleTimeoutInMinutes(int minutes);
        }

        /** The final stage of the load balancing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the load balancing rule definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithBackendPort<ParentT>,
            UpdateDefinitionStages.WithFloatingIp<ParentT>,
            UpdateDefinitionStages.WithIdleTimeoutInMinutes<ParentT> {
        }
    }

    /** The entirety of a load balancing rule definition as part of a load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithFrontendPort<ParentT>,
        UpdateDefinitionStages.WithProtocol<ParentT> {
    }
}
