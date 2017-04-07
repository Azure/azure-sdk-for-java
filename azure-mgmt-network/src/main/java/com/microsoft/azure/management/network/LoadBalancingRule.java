/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.LoadBalancingRuleInner;
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
 * An immutable client-side representation of an HTTP load balancing rule.
 */
@Fluent()
@Beta
public interface LoadBalancingRule extends
    HasInner<LoadBalancingRuleInner>,
    ChildResource<LoadBalancer>,
    HasBackendPort,
    HasFrontend,
    HasFloatingIP,
    HasProtocol<TransportProtocol>,
    HasFrontendPort {

    /**
     * @return the method of load distribution
     */
    LoadDistribution loadDistribution();

    /**
     * @return the number of minutes before an inactive connection is closed
     */
    int idleTimeoutInMinutes();

    /**
     * @return the backend associated with the load balancing rule
     */
    LoadBalancerBackend backend();

    /**
     * @return the probe associated with the load balancing rule
     */
    LoadBalancerProbe probe();

    /**
     * Grouping of load balancing rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the load balancing rule definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the transport protocol to apply the rule to.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.DefinitionStages.WithProtocol<WithFrontend<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the frontend port to load balance.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFrontendPort<ParentT> extends HasFrontendPort.DefinitionStages.WithFrontendPort<WithProbe<ParentT>> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the frontend to associate with the rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.DefinitionStages.WithFrontend<WithFrontendPort<ParentT>> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the probe to associate with the rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProbe<ParentT> {
            /**
             * Associates the specified existing HTTP or TCP probe of this load balancer with the load balancing rule.
             * @param name the name of an existing HTTP or TCP probe
             * @return the next stage of the definition
             */
            WithBackend<ParentT> withProbe(String name);
        }

        /** The stage of a load balancing rule definition allowing to specify the backend to associate the rule with.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the load balancing rule with the specified backend of this load balancer.
             * <p>
             * A backedn with the specified name must already exist on this load balancer.
             * @param backendName the name of an existing backend
             * @return the next stage of the definition
             */
            WithBackendPort<ParentT> withBackend(String backendName);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the backend port to send the load-balanced traffic to.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.DefinitionStages.WithBackendPort<WithAttach<ParentT>>,
            WithAttach<ParentT> {
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
            DefinitionStages.WithFloatingIP<ParentT>,
            DefinitionStages.WithIdleTimeoutInMinutes<ParentT>,
            DefinitionStages.WithLoadDistribution<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to enable the floating IP functionality.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFloatingIP<ParentT> extends HasFloatingIP.DefinitionStages.WithFloatingIP<WithAttach<ParentT>> {
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

        /**
         * The stage of a load balancing rule definition allowing to specify the load distribution.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithLoadDistribution<ParentT> {
            /**
             * Specifies the load distribution mode.
             * @param loadDistribution a supported load distribution mode
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLoadDistribution(LoadDistribution loadDistribution);
        }
    }

    /** The entirety of a load balancing rule definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithProtocol<ParentT>,
        DefinitionStages.WithFrontendPort<ParentT>,
        DefinitionStages.WithFrontend<ParentT>,
        DefinitionStages.WithProbe<ParentT>,
        DefinitionStages.WithBackend<ParentT>,
        DefinitionStages.WithBackendPort<ParentT> {
    }

    /**
     * Grouping of load balancing rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a load balancing rule update allowing to modify the transport protocol the rule applies to.
         */
        interface WithProtocol extends
            HasProtocol.UpdateStages.WithProtocol<Update, TransportProtocol> {
        }

        /**
         * The stage of a load balancing rule update allowing to modify the frontend port.
         */
        interface WithFrontendPort extends HasFrontendPort.UpdateStages.WithFrontendPort<Update> {
        }

        /**
         * The stage of a load balancing rule update allowing to modify the frontend reference.
         */
        interface WithFrontend extends
            HasFrontend.UpdateStages.WithFrontend<Update> {
        }

        /**
         * The stage of a load balancing rule update allowing to modify the backend port.
         */
        interface WithBackendPort extends
            HasBackendPort.UpdateStages.WithBackendPort<Update> {
        }

        /**
         * The stage of a load balancing rule update allowing to enable the floating IP functionality.
         */
        interface WithFloatingIP extends HasFloatingIP.UpdateStages.WithFloatingIP<Update> {
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

        /**
         * The stage of a load balancing rule update allowing to modify the load distribution.
         */
        interface WithLoadDistribution {
            /**
             * Specifies the load distribution mode.
             * @param loadDistribution a supported load distribution mode
             * @return the next stage of the definition
             */
            Update withLoadDistribution(LoadDistribution loadDistribution);
        }
    }

    /**
     * The entirety of a load balancing rule update as part of a load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithFrontendPort,
        UpdateStages.WithFrontend,
        UpdateStages.WithProtocol,
        UpdateStages.WithBackendPort,
        UpdateStages.WithFloatingIP,
        UpdateStages.WithIdleTimeoutInMinutes,
        UpdateStages.WithLoadDistribution {
    }

    /**
     * Grouping of load balancing rule definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the load balancing rule definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithProtocol<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the transport protocol to apply the rule to.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> extends
            HasProtocol.UpdateDefinitionStages.WithProtocol<WithFrontend<ParentT>, TransportProtocol> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the frontend port to load balance.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithFrontendPort<ParentT> extends HasFrontendPort.UpdateDefinitionStages.WithFrontendPort<WithProbe<ParentT>> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the frontend to associate with the rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithFrontend<ParentT> extends
            HasFrontend.UpdateDefinitionStages.WithFrontend<WithFrontendPort<ParentT>> {
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the probe to associate with the rule.
         * @param <ParentT> the parent load balancer type
         */
        interface WithProbe<ParentT> {
            /**
             * Associates the specified existing HTTP or TCP probe of this load balancer with the load balancing rule.
             * @param name the name of an existing HTTP or TCP probe
             * @return the next stage of the definition
             */
            WithBackend<ParentT> withProbe(String name);
        }

        /** The stage of a load balancing rule definition allowing to specify the backend to associate the rule with.
         * @param <ParentT> the parent load balancer type
         */
        interface WithBackend<ParentT> {
            /**
             * Associates the load balancing rule with the specified backend of this load balancer.
             * <p>
             * A backedn with the specified name must already exist on this load balancer.
             * @param backendName the name of an existing backend
             * @return the next stage of the definition
             */
            WithBackendPort<ParentT> withBackend(String backendName);
        }

        /**
         * The stage of a load balancing rule definition allowing to specify the backend port to send the load-balanced traffic to.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBackendPort<ParentT> extends
            HasBackendPort.UpdateDefinitionStages.WithBackendPort<WithAttach<ParentT>>,
            WithAttach<ParentT> {
        }

        /**
         * The stage of a load balancing rule definition allowing to enable the floating IP functionality.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithFloatingIP<ParentT> extends HasFloatingIP.UpdateDefinitionStages.WithFloatingIP<WithAttach<ParentT>> {
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

        /**
         * The stage of a load balancing rule definition allowing to specify the load distribution.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithLoadDistribution<ParentT> {
            /**
             * Specifies the load distribution mode.
             * @param loadDistribution a supported load distribution mode
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLoadDistribution(LoadDistribution loadDistribution);
        }

        /**
         * The final stage of the load balancing rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the load balancing rule definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT>,
            UpdateDefinitionStages.WithFloatingIP<ParentT>,
            UpdateDefinitionStages.WithIdleTimeoutInMinutes<ParentT>,
            UpdateDefinitionStages.WithLoadDistribution<ParentT> {
        }
    }

    /** The entirety of a load balancing rule definition as part of a load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithProtocol<ParentT>,
        UpdateDefinitionStages.WithFrontendPort<ParentT>,
        UpdateDefinitionStages.WithFrontend<ParentT>,
        UpdateDefinitionStages.WithProbe<ParentT>,
        UpdateDefinitionStages.WithBackend<ParentT>,
        UpdateDefinitionStages.WithBackendPort<ParentT> {
    }
}
