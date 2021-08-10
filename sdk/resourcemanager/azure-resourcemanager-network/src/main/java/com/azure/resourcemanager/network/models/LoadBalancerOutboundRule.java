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

/** An immutable client-side representation of an outbound rule. */
@Fluent()
public interface LoadBalancerOutboundRule
    extends HasInnerModel<OutboundRuleInner>,
    HasProtocol<LoadBalancerOutboundRuleProtocol>,
    ChildResource<LoadBalancer> {


    /** @return the associated frontend IP configuration ids */
    List<String> frontendIpConfigurationIds();

    /** @return the associated frontends */
    List<LoadBalancerFrontend> frontends();

    /** @return the associated backend address pool id */
    String backendAddressPoolId();

    /** @return the associated backend */
    LoadBalancerBackend backend();

    /** @return the number of outbound ports to be used for NAT */
    int allocatedOutboundPortsNumber(); // TODO: need this or not?

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
            LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> toFrontend(String name);
        }

        /**
         * The stage of an outbound rule definition allowing to specify the frontend IP address.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFrontends<ParentT> {
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
        LoadBalancerOutboundRule.DefinitionStages.WithFrontends<ParentT>,
        LoadBalancerOutboundRule.DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of outbound rule update stages. */
    interface UpdateStages {
        /** The stage of an outbound rule update allowing to specify the backend pool. */
        interface WithBackend {
            /**
             * Specifies the backend pool.
             *
             * @param name backend pool name
             * @return the next stage of the update
             */
            Update fromBackend(String name);
        }

        /** The stage of an outbound rule update allowing to specify a frontend for the rule to apply to. */
        interface WithFrontend {
            Update toFrontend(String name);
        }

        /** The stage of an outbound rule update allowing to specify frontends for the rule to apply to. */
        interface WithFrontends {
            Update toFrontends(List<String> names);
        }

        /**
         * The stage of an outbound rule update allowing to specify the transport protocol for the rule to apply to.
         */
        interface WithProtocol extends HasProtocol.UpdateStages.WithProtocol<Update, LoadBalancerOutboundRuleProtocol> {
        }

        /**
         * The stage of an outbound rule definition allowing to update the TCP reset enablement for this outbound
         * rule.
         *
         */
        interface WithEnableTcpReset {
            /**
             * Specifies the idle connection timeout in minutes.
             *
             * @param enableTcpReset the TCP reset enablement boolean
             * @return the next stage of the definition
             */
            Update withEnableTcpReset(boolean enableTcpReset);
        }

        /**
         * The stage of an inbound NAT rule update allowing to specify the idle connection timeout for this inbound NAT
         * rule.
         */
        interface WithIdleTimeout {
            /**
             * Specifies the idle connection timeout in minutes.
             *
             * @param minutes a number of minutes
             * @return the next stage of the update
             */
            Update withIdleTimeoutInMinutes(int minutes);
        }
    }

    /** The entirety of an inbound NAT rule update as part of a load balancer update. */
    interface Update
        extends Settable<LoadBalancer.Update>,
        LoadBalancerOutboundRule.UpdateStages.WithProtocol,
        LoadBalancerOutboundRule.UpdateStages.WithBackend,
        LoadBalancerOutboundRule.UpdateStages.WithFrontend,
        LoadBalancerOutboundRule.UpdateStages.WithFrontends,
        LoadBalancerOutboundRule.UpdateStages.WithEnableTcpReset,
        LoadBalancerOutboundRule.UpdateStages.WithIdleTimeout {
    }

}
