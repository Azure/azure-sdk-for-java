/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.InboundNatPoolInner;
import com.microsoft.azure.management.network.model.HasBackendPort;
import com.microsoft.azure.management.network.model.HasFrontend;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an inbound NAT rule.
 */
@Fluent()
public interface LoadBalancerInboundNatPool extends
    HasFrontend,
    HasBackendPort,
    HasProtocol<TransportProtocol>,
    Wrapper<InboundNatPoolInner>,
    ChildResource<LoadBalancer> {

    /**
     * @return the starting frontend port number
     */
    int frontendPortRangeStart();

    /**
     * @return the ending frontend port number
     */
    int frontendPortRangeEnd();

    /**
     * Grouping of inbound NAT pool definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the inbound NAT pool definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The final stage of the inbound NAT pool definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the inbound NAT pool definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the transport protocol for the pool to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.DefinitionStages.WithProtocol<WithFrontend<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the frontend for the inbound NAT rules in the pool to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.DefinitionStages.WithFrontend<WithFrontendPortRange<ParentT>> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the frontend port range.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPortRange<ParentT> {
            /**
             * Specifies the frontend port range.
             * @param from the starting port number, between 1 and 65534
             * @param to the ending port number, greater than the starting port number and no more than 65534
             * @return the next stage of the definition
             */
            WithBackendPort<ParentT> withFrontendPortRange(int from, int to);
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.DefinitionStages.WithBackendPort<WithAttach<ParentT>> {
        }
    }

    /** The entirety of an inbound NAT pool definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithProtocol<ParentT>,
        DefinitionStages.WithFrontend<ParentT>,
        DefinitionStages.WithFrontendPortRange<ParentT>,
        DefinitionStages.WithBackendPort<ParentT> {
    }

    /**
     * Grouping of inbound NAT pool update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an inbound NAT pool update allowing to specify the transport protocol for the pool to apply to.
         */
        interface WithProtocol extends
            HasProtocol.UpdateStages.WithProtocol<Update, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT pool update allowing to specify the frontend for the inbound NAT rules in the pool to apply to.
         */
        interface WithFrontend extends
            HasFrontend.UpdateStages.WithFrontend<Update> {
        }

        /**
         * The stage of an inbound NAT pool update allowing to specify the frontend port range.
         */
        interface WithFrontendPortRange {
            /**
             * Specifies the frontend port range.
             * @param from the starting port number, between 1 and 65534
             * @param to the ending port number, greater than the starting port number and no more than 65534
             * @return the next stage of the definition
             */
            Update withFrontendPortRange(int from, int to);
        }

        /**
         * The stage of an inbound NAT pool update allowing to specify the backend port.
         */
        interface WithBackendPort extends
            HasBackendPort.UpdateStages.WithBackendPort<Update> {
        }
    }

    /**
     * The entirety of an inbound NAT pool update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithProtocol,
        UpdateStages.WithFrontend,
        UpdateStages.WithBackendPort,
        UpdateStages.WithFrontendPortRange {
    }

    /**
     * Grouping of inbound NAT pool definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the inbound NAT pool definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The final stage of the inbound NAT pool definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the inbound NAT pool
         * definition can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the transport protocol for the pool to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.UpdateDefinitionStages.WithProtocol<WithAttach<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the frontend for the inbound NAT rules in the pool to apply to.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.UpdateDefinitionStages.WithFrontend<WithAttach<ParentT>> {
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the frontend port range.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontendPortRange<ParentT> {
            /**
             * Specified the frontend port range.
             * @param from the starting port number, between 1 and 65534
             * @param to the ending port number, greater than the starting port number and no more than 65534
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withFrontendPortRange(int from, int to);
        }

        /**
         * The stage of an inbound NAT pool definition allowing to specify the backend port.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.UpdateDefinitionStages.WithBackendPort<WithAttach<ParentT>> {
        }
    }

    /** The entirety of an inbound NAT pool definition as part of a load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithProtocol<ParentT>,
        UpdateDefinitionStages.WithFrontend<ParentT>,
        UpdateDefinitionStages.WithFrontendPortRange<ParentT>,
        UpdateDefinitionStages.WithBackendPort<ParentT> {
    }
}
