/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.InboundNatRuleInner;
import com.microsoft.azure.management.network.model.HasBackendPort;
import com.microsoft.azure.management.network.model.HasFloatingIP;
import com.microsoft.azure.management.network.model.HasFrontend;
import com.microsoft.azure.management.network.model.HasFrontendPort;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an inbound NAT rule.
 */
@Fluent()
@Beta
public interface LoadBalancerInboundNatRule extends
    HasFrontend,
    HasBackendPort,
    HasProtocol<TransportProtocol>,
    HasFloatingIP,
    HasFrontendPort,
    HasInner<InboundNatRuleInner>,
    ChildResource<LoadBalancer> {

    /**
     * @return the name of the IP configuration within the network interface associated with this NAT rule
     */
    String backendNicIPConfigurationName();

    /**
     * @return the resource ID of the network interface assigned as the backend of this inbound NAT rule
     */
    String backendNetworkInterfaceId();

    /**
     * @return the number of minutes before an idle connection is closed
     */
    int idleTimeoutInMinutes();

    /**
     * Grouping of inbound NAT rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the inbound NAT rule definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
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
            DefinitionStages.WithFloatingIP<ParentT>,
            DefinitionStages.WithIdleTimeout<ParentT> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the transport protocol.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.DefinitionStages.WithProtocol<WithFrontend<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify a frontend for the rule to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.DefinitionStages.WithFrontend<WithFrontendPort<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.DefinitionStages.WithBackendPort<WithAttach<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify whether floating IP should be enabled.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFloatingIP<ParentT> extends HasFloatingIP.DefinitionStages.WithFloatingIP<WithAttach<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the frontend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPort<ParentT> extends HasFrontendPort.DefinitionStages.WithFrontendPort<WithAttach<ParentT>> {
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
    }

    /**
     * The entirety of an inbound NAT rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithProtocol<ParentT>,
        DefinitionStages.WithFrontend<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of inbound NAT rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an inbound NAT rule update allowing to specify the backend port.
         */
        interface WithBackendPort extends
            HasBackendPort.UpdateStages.WithBackendPort<Update> {
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify a frontend for the rule to apply to.
         */
        interface WithFrontend extends
            HasFrontend.UpdateStages.WithFrontend<Update> {
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the transport protocol for the rule to apply to.
         */
        interface WithProtocol extends
            HasProtocol.UpdateStages.WithProtocol<Update, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify whether floating IP should be enabled.
         */
        interface WithFloatingIP extends HasFloatingIP.UpdateStages.WithFloatingIP<Update> {
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the frontend port.
         */
        interface WithFrontendPort extends HasFrontendPort.UpdateStages.WithFrontendPort<Update> {
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
    }

    /**
     * The entirety of an inbound NAT rule update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithBackendPort,
        UpdateStages.WithFloatingIP,
        UpdateStages.WithFrontend,
        UpdateStages.WithFrontendPort,
        UpdateStages.WithIdleTimeout,
        UpdateStages.WithProtocol {
    }

    /**
     * Grouping of inbound NAT rule definition stages as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the inbound NAT rule definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The final stage of the inbound NAT rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the inbound NAT rule definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithBackendPort<ParentT>,
            UpdateDefinitionStages.WithFloatingIP<ParentT>,
            UpdateDefinitionStages.WithIdleTimeout<ParentT> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the transport protocol.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.UpdateDefinitionStages.WithProtocol<WithFrontend<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify a frontend for the rule to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.UpdateDefinitionStages.WithFrontend<WithFrontendPort<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.UpdateDefinitionStages.WithBackendPort<WithAttach<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify whether floating IP should be enabled.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFloatingIP<ParentT> extends HasFloatingIP.UpdateDefinitionStages.WithFloatingIP<WithAttach<ParentT>> {
        }

        /**
         * The stage of an inbound NAT rule definition allowing to specify the frontend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPort<ParentT> extends HasFrontendPort.UpdateDefinitionStages.WithFrontendPort<WithAttach<ParentT>> {
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
    }

    /**
     * The entirety of an inbound NAT rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithProtocol<ParentT>,
        UpdateDefinitionStages.WithFrontend<ParentT>,
        UpdateDefinitionStages.WithFrontendPort<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
