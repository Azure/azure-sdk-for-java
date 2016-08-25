/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.InboundNatRuleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an inbound NAT rule.
 */
public interface InboundNatRule extends
    Wrapper<InboundNatRuleInner>,
    ChildResource {

    /**
     * @return the resource ID of the network interface IP configuration associated with this NAT rule
     */
    String networkInterfaceIpConfigurationId();

    /**
     * @return the backend port number associated with this NAT rule
     */
    int backendPort();

    /**
     * @return the frontend port number associated with this NAT rule
     */
    int frontendPort();

    /**
     * @return the state of the floating IP enablement
     */
    boolean floatingIpEnabled();

    /**
     * @return the frontend IP configuration associated with this NAT rule
     */
    Frontend frontend();

    /**
     * Grouping of inbound NAT rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the inbound NAT rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithFrontend<ParentT> {
        }

        /**
         * The final stage of the inbound NAT rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the inbound NAT rule definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT>,
            DefinitionStages.WithBackendPort<ParentT>,
            DefinitionStages.WithFloatingIp<ParentT>,
            DefinitionStages.WithFrontendPort<ParentT>,
            DefinitionStages.WithIdleTimeout<ParentT>,
            DefinitionStages.WithProtocol<ParentT>,
            DefinitionStages.WithFrontend<ParentT> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> {
            /**
             * Specifies the backend port for this inbound NAT rule.
             * <p>
             * If not specified, the same backend port number is assumed as that used by the frontend.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBackendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify whether floating IP should be enabled.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFloatingIp<ParentT> {
            /**
             * Enables the floating IP feature.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIpEnabled();

            /**
             * Disables the floating IP feature.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIpDisabled();

            /**
             * Specifies whether the floating IP feature should be enabled or disabled.
             * @param enabled true if enabled, else false
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIp(boolean enabled);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify a frontend for the rule to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> {
            /**
             * Specifies the frontend for the inbound NAT rule to apply to.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the frontend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Specifies the frontend port.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the idle connection timeout for this inbound NAT rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithIdleTimeout<ParentT> {
            /**
             * Specifies the idle connection timeout in minutes.
             * @param minutes a number of minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIdleTimeoutInMinutes(int minutes);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the transport protocol for the rule to apply to.
         * @param <ParentT> the patent load balancer type
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the transport protocol for the inbound NAT rule to apply to.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtocol(TransportProtocol protocol);
        }
    }

    /** The entirety of an inbound NAT rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of inbound NAT rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an inbound NAT rule update allowing to specify the backend port.
         */
        interface WithBackendPort {
            /**
             * Specifies the backend port for this inbound NAT rule.
             * @param port a port number
             * @return the next stage of the update
             */
            Update withBackendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify whether floating IP should be enabled.
         */
        interface WithFloatingIp {
            /**
             * Enables the floating IP feature.
             * @return the next stage of the update
             */
            Update withFloatingIpEnabled();

            /**
             * Disables the floating IP feature.
             * @return the next stage of the update
             */
            Update withFloatingIpDisabled();

            /**
             * Specifies whether the floating IP feature should be enabled or disabled.
             * @param enabled true if enabled, else false
             * @return the next stage of the update
             */
            Update withFloatingIp(boolean enabled);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify a frontend for the rule to apply to.
         */
        interface WithFrontend {
            /**
             * Specifies the frontend for the inbound NAT rule to apply to.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the update
             */
            Update withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the frontend port.
         */
        interface WithFrontendPort {
            /**
             * Specifies the frontend port.
             * @param port a port number
             * @return the next stage of the update
             */
            Update withFrontendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the idle connection timeout for this inbound NAT rule.
         */
        interface WithIdleTimeout {
            /**
             * Specifies the idle connection timeout in minutes.
             * @param minutes a number of minutes
             * @return the next stage of the update
             */
            Update withIdleTimeoutInMinutes(int minutes);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the transport protocol for the rule to apply to.
         */
        interface WithProtocol {
            /**
             * Specifies the transport protocol for the inbound NAT rule to apply to.
             * @param protocol a transport protocol
             * @return the next stage of the update
             */
            Update withProtocol(TransportProtocol protocol);
        }
    }

    /**
     * The entirety of an inbound NAT rule update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithBackendPort,
        UpdateStages.WithFloatingIp,
        UpdateStages.WithFrontend,
        UpdateStages.WithFrontendPort,
        UpdateStages.WithIdleTimeout,
        UpdateStages.WithProtocol {
    }

    /**
     * Grouping of inbound NAT rule definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the inbound NAT rule definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithFrontend<ParentT> {
        }

        /**
         * The final stage of the inbound NAT rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the inbound NAT rule
         * definition can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithBackendPort<ParentT>,
            UpdateDefinitionStages.WithFloatingIp<ParentT>,
            UpdateDefinitionStages.WithFrontend<ParentT>,
            UpdateDefinitionStages.WithFrontendPort<ParentT>,
            UpdateDefinitionStages.WithIdleTimeout<ParentT>,
            UpdateDefinitionStages.WithProtocol<ParentT> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> {
            /**
             * Specifies the backend port for this inbound NAT rule.
             * <p>
             * If not specified, the same backend port number is assumed as that used by the frontend.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBackendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify whether floating IP should be enabled.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFloatingIp<ParentT> {
            /**
             * Enables the floating IP feature.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIpEnabled();

            /**
             * Disables the floating IP feature.
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIpDisabled();

            /**
             * Specifies whether the floating IP feature should be enabled or disabled.
             * @param enabled true if enabled, else false
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFloatingIp(boolean enabled);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify a frontend for the rule to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> {
            /**
             * Specifies the frontend for the inbound NAT rule to apply to.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            WithFrontendPort<ParentT> withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the frontend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPort<ParentT> {
            /**
             * Specifies the frontend port.
             * @param port a port number
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPort(int port);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the idle connection timeout for this inbound NAT rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithIdleTimeout<ParentT> {
            /**
             * Specifies the idle connection timeout in minutes.
             * @param minutes a number of minutes
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withIdleTimeoutInMinutes(int minutes);
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the transport protocol for the rule to apply to.
         * @param <ParentT> the patent load balancer type
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the transport protocol for the inbound NAT rule to apply to.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtocol(TransportProtocol protocol);
        }
    }

    /** The entirety of an inbound NAT rule definition as part of a load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
