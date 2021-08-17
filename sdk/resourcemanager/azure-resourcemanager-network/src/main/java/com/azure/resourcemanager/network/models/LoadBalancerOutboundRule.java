// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.OutboundRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an outbound rule. */
public interface LoadBalancerOutboundRule
    extends HasInnerModel<OutboundRuleInner>,
    HasProtocol<LoadBalancerOutboundRuleProtocol>,
    ChildResource<LoadBalancer> {


    /** @return the associated frontend IP configuration ids */
    List<String> frontendIpConfigurationIds();

    /** @return the associated frontends */
    Map<String, LoadBalancerFrontend> frontends();

    /** @return the associated backend address pool id */
    String backendAddressPoolId();

    /** @return the associated backend */
    LoadBalancerBackend backend();

    /** @return the number of outbound ports to be used for NAT */
    int allocatedOutboundPorts();

    /** @return the provisioning state of the outbound rule resource */
    ProvisioningState provisioningState();

    /** @return the number of minutes before an idle connection is closed */
    int idleTimeoutInMinutes();

    /** @return if TCP reset is enabled */
    boolean tcpResetEnabled();

    /** @return outbound rule protocol */
    LoadBalancerOutboundRuleProtocol protocol();

    /** Grouping of outbound rule definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the outbound rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends LoadBalancerOutboundRule.DefinitionStages.WithProtocol<ParentT> {
        }

        /**
         * The final stage of the outbound rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the outbound rule definition can be
         * attached to the parent load balancer definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>,
            LoadBalancerOutboundRule.DefinitionStages.WithEnableTcpReset<ParentT>,
            LoadBalancerOutboundRule.DefinitionStages.WithIdleTimeout<ParentT> {
        }

        /**
         * The stage of an outbound rule definition allowing to specify the transport protocol.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT>
            extends HasProtocol.DefinitionStages.WithProtocol<LoadBalancerOutboundRule.DefinitionStages.WithBackend<ParentT>, LoadBalancerOutboundRuleProtocol> {
        }

        /**
         * The stage of an outbound rule definition allowing to specify a backend pool for the rule to apply to.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBackend<ParentT>  {
            LoadBalancerOutboundRule.DefinitionStages.WithFrontend<ParentT> fromBackend(String name);
        }

        /**
         * The stage of an outbound rule definition allowing to specify the frontend IP address.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFrontend<ParentT> {
            /**
             * Specifies a frontend for outbound rule to apply to
             * @param name the frontend name
             * @return the next stage of the parent definition to return to after attaching this definition
             */
            LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> toFrontend(String name);

            /**
             * Specifies a list of frontends for outbound rule to apply to
             * @param names a list of frontend names
             * @return the next stage of the parent definition to return to after attaching this definition
             */
            LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> toFrontends(List<String> names);
        }

        /**
         * The stage of an outbound rule definition allowing to specify the idle connection timeout for this outbound
         * rule.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithIdleTimeout<ParentT> {
            /**
             * Specifies the idle connection timeout in minutes.
             *
             * @param minutes a number of minutes
             * @return the next stage of the definition
             */
            LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> withIdleTimeoutInMinutes(int minutes);
        }

        /**
         * The stage of an outbound rule definition allowing to specify the TCP reset enablement for this outbound
         * rule.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithEnableTcpReset<ParentT> {
            /**
             * Specifies the TCP reset enablement for this outbound rule
             * @param enableTcpReset
             * @return the next stage of the definition
             */
            LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> withEnableTcpReset(boolean enableTcpReset);
        }
    }

    /**
     * The entirety of an outbound rule definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends LoadBalancerOutboundRule.DefinitionStages.Blank<ParentT>,
        LoadBalancerOutboundRule.DefinitionStages.WithProtocol<ParentT>,
        LoadBalancerOutboundRule.DefinitionStages.WithBackend<ParentT>,
        LoadBalancerOutboundRule.DefinitionStages.WithFrontend<ParentT>,
        LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of outbound rule update stages. */
    interface UpdateStages {
        /** The stage of an outbound rule update allowing to specify the backend pool. */
        interface WithBackend<ParentT> {
            /**
             * Specifies the backend pool.
             *
             * @param name backend pool name
             * @return the next stage of the update
             */
            Update<ParentT> fromBackend(String name);
        }

        /** The stage of an outbound rule update allowing to specify a frontend for the rule to apply to. */
        interface WithFrontend<ParentT> {
            /**
             * Specifies the frontend IP Address
             * @param name frontend name
             * @return the next stage of the Update
             */
            Update<ParentT> toFrontend(String name);

            /**
             * Specifies the frontend IP Addresses
             * @param names a list of frontend names
             * @return the next stage of the Update
             */
            Update<ParentT> toFrontends(List<String> names);
        }

        /**
         * The stage of an outbound rule update allowing to specify the transport protocol for the rule to apply to.
         */
        interface WithProtocol<ParentT> extends HasProtocol.UpdateStages.WithProtocol<Update<ParentT>, LoadBalancerOutboundRuleProtocol> {
        }

        /**
         * The stage of an outbound rule definition allowing to update the TCP reset enablement for this outbound
         * rule.
         *
         */
        interface WithEnableTcpReset<ParentT> {
            /**
             * Specifies the idle connection timeout in minutes.
             *
             * @param enableTcpReset the TCP reset enablement boolean
             * @return the next stage of the definition
             */
            Update<ParentT> withEnableTcpReset(boolean enableTcpReset);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the idle connection timeout for this inbound NAT
         * rule.
         */
        interface WithIdleTimeout<ParentT> {
            /**
             * Specifies the idle connection timeout in minutes.
             *
             * @param minutes a number of minutes
             * @return the next stage of the update
             */
            Update<ParentT> withIdleTimeoutInMinutes(int minutes);
        }
    }

    /** The entirety of an inbound NAT rule update as part of a load balancer update. */
    interface Update<ParentT>
        extends Settable<ParentT>,
        LoadBalancerOutboundRule.UpdateStages.WithProtocol<ParentT>,
        LoadBalancerOutboundRule.UpdateStages.WithBackend<ParentT>,
        LoadBalancerOutboundRule.UpdateStages.WithFrontend<ParentT>,
        LoadBalancerOutboundRule.UpdateStages.WithEnableTcpReset<ParentT>,
        LoadBalancerOutboundRule.UpdateStages.WithIdleTimeout<ParentT> {
    }

}
