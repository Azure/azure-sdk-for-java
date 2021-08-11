// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.LoadBalancerInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;
import java.util.Map;

/** Entry point for load balancer management API in Azure. */
@Fluent
public interface LoadBalancer
    extends GroupableResource<NetworkManager, LoadBalancerInner>,
        Refreshable<LoadBalancer>,
        Updatable<LoadBalancer.Update>,
        UpdatableWithTags<LoadBalancer>,
        HasLoadBalancingRules {

    // Getters

    /** @return resource IDs of the public IP addresses assigned to the frontends of this load balancer */
    List<String> publicIpAddressIds();

    /** @return TCP probes of this load balancer, indexed by the name */
    Map<String, LoadBalancerTcpProbe> tcpProbes();

    /** @return HTTP probes of this load balancer, indexed by the name */
    Map<String, LoadBalancerHttpProbe> httpProbes();

    /** @return HTTPS probes of this load balancer, indexed by the name */
    Map<String, LoadBalancerHttpProbe> httpsProbes();

    /** @return backends for this load balancer to load balance the incoming traffic among, indexed by name */
    Map<String, LoadBalancerBackend> backends();

    /** @return inbound NAT rules for this balancer */
    Map<String, LoadBalancerInboundNatRule> inboundNatRules();

    /** @return frontends for this load balancer, for the incoming traffic to come from. */
    Map<String, LoadBalancerFrontend> frontends();

    /** @return private (internal) frontends */
    Map<String, LoadBalancerPrivateFrontend> privateFrontends();

    /**
     * Searches for the public frontend that is associated with the provided public IP address, if one exists.
     *
     * @param publicIPAddress a public IP address to search by
     * @return a public frontend associated with the provided public IP address
     */
    LoadBalancerPublicFrontend findFrontendByPublicIpAddress(PublicIpAddress publicIPAddress);

    /**
     * Searches for the public frontend that is associated with the provided public IP address, if one exists.
     *
     * @param publicIPAddressId the resource ID of a public IP address to search by
     * @return a public frontend associated with the provided public IP address
     */
    LoadBalancerPublicFrontend findFrontendByPublicIpAddress(String publicIPAddressId);

    /** @return public (Internet-facing) frontends */
    Map<String, LoadBalancerPublicFrontend> publicFrontends();

    /** @return inbound NAT pools, indexed by name */
    Map<String, LoadBalancerInboundNatPool> inboundNatPools();

    /** @return load balancer sku. */
    LoadBalancerSkuType sku();

    /** The entirety of the load balancer definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithCreate,
            DefinitionStages.WithBackend,
            DefinitionStages.WithLoadBalancingRule,
            DefinitionStages.WithLBRuleOrNat,
            DefinitionStages.WithLBRuleOrNatOrCreate,
            DefinitionStages.WithCreateAndInboundNatPool,
            DefinitionStages.WithCreateAndInboundNatRule,
            DefinitionStages.WithCreateAndOutboundRule,
            DefinitionStages.WithCreateAndNatChoice {
    }

    /** Grouping of load balancer definition stages. */
    interface DefinitionStages {
        /** The first stage of a load balancer definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the load balancer definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithLBRuleOrNat> {
        }

        /**
         * The stage of a load balancer definition describing the nature of the frontend of the load balancer: internal
         * or Internet-facing.
         */
        interface WithFrontend extends WithPublicFrontend, WithPrivateFrontend {
        }

        /** The stage of an internal load balancer definition allowing to define one or more private frontends. */
        interface WithPrivateFrontend {
            /**
             * Begins an explicit definition of a new private (internal) load balancer frontend.
             *
             * <p>(Note that private frontends can also be created implicitly as part of a load balancing rule, inbound
             * NAT rule or inbound NAT pool definition, by referencing an existing subnet within those definitions.)
             *
             * @param name the name for the frontend
             * @return the first stage of a new frontend definition
             */
            LoadBalancerPrivateFrontend.DefinitionStages.Blank<WithCreate> definePrivateFrontend(String name);
        }

        /** The stage of an Internet-facing load balancer definition allowing to define one or more public frontends. */
        interface WithPublicFrontend {
            /**
             * Begins an explicit definition of a new public (Internet-facing) load balancer frontend.
             *
             * <p>(Note that frontends can also be created implicitly as part of a load balancing rule, inbound NAT rule
             * or inbound NAT pool definition, by referencing an existing public IP address within those definitions.)
             *
             * @param name the name for the frontend
             * @return the first stage of a new frontend definition
             */
            LoadBalancerPublicFrontend.DefinitionStages.Blank<WithCreateAndOutboundRule> definePublicFrontend(String name);
        }

        /** The stage of a load balancer definition allowing to add a backend. */
        interface WithBackend {
            /**
             * Starts the definition of a backend.
             *
             * @param name the name to assign to the backend
             * @return the next stage of the update
             */
            LoadBalancerBackend.DefinitionStages.Blank<WithCreate> defineBackend(String name);
        }

        /**
         * The stage of a load balancer definition allowing to add a load blanacing rule, or an inbound NAT rule or
         * pool.
         */
        interface WithLBRuleOrNat extends WithLoadBalancingRule, WithInboundNatRule, WithInboundNatPool {
        }

        /** The stage of the load balancer definition allowing to add a load balancing probe. */
        interface WithProbe {
            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             *
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            LoadBalancerTcpProbe.DefinitionStages.Blank<WithCreate> defineTcpProbe(String name);

            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             *
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            LoadBalancerHttpProbe.DefinitionStages.Blank<WithCreate> defineHttpProbe(String name);

            /**
             * Begins the definition of a new HTTPS probe to add to the load balancer.
             *
             * @param name the name of the probe
             * @return the first stage of the new probe definition
             */
            LoadBalancerHttpProbe.DefinitionStages.Blank<WithCreate> defineHttpsProbe(String name);
        }

        /** The stage of a load balancer definition allowing to create a load balancing rule. */
        interface WithLoadBalancingRule {
            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             *
             * @param name the name of the load balancing rule
             * @return the first stage of the new load balancing rule definition
             */
            LoadBalancingRule.DefinitionStages.Blank<WithLBRuleOrNatOrCreate> defineLoadBalancingRule(String name);
        }

        /**
         * The stage of a load balancer definition allowing to create a load balancing rule or create the load balancer.
         */
        interface WithLBRuleOrNatOrCreate extends WithLoadBalancingRule, WithCreateAndNatChoice {
        }

        /** The stage of the load balancer definition allowing to specify SKU. */
        interface WithSku {
            /**
             * Specifies the SKU for the load balancer.
             *
             * @param skuType the SKU type
             * @return the next stage of the definition
             */
            WithCreate withSku(LoadBalancerSkuType skuType);
        }

        /**
         * The stage of a load balancer definition containing all the required inputs for the resource to be created,
         * but also allowing for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<LoadBalancer>,
                Resource.DefinitionWithTags<WithCreate>,
                WithBackend,
                WithFrontend,
                WithProbe,
                WithSku {
        }

        /**
         * The stage of a load balancer definition allowing to create the load balancer or start configuring optional
         * inbound NAT rules or pools.
         */
        interface WithCreateAndNatChoice extends WithCreate, WithInboundNatRule, WithInboundNatPool {
        }

        /** The stage of a load balancer definition allowing to create the load balancer or add an inbound NAT pool. */
        interface WithCreateAndInboundNatPool extends WithCreate, WithInboundNatPool {
        }

        /** The stage of a load balancer definition allowing to create the load balancer or add an inbound NAT rule. */
        interface WithCreateAndInboundNatRule extends WithCreate, WithInboundNatRule {
        }

        /**
         * The stage of a load balancer definition allowing to create the load balancer or add an outbound rule
         */
        interface WithCreateAndOutboundRule extends  WithCreate, WithOutboundRule {
        }

        /** The stage of a load balancer definition allowing to create a new inbound NAT rule. */
        interface WithInboundNatRule {
            /**
             * Begins the definition of a new inbound NAT rule to add to the load balancer.
             *
             * @param name the name of the inbound NAT rule
             * @return the first stage of the new inbound NAT rule definition
             */
            LoadBalancerInboundNatRule.DefinitionStages.Blank<WithCreateAndInboundNatRule> defineInboundNatRule(
                String name);
        }

        /**
         * The stage of a load balancer definition allowing to create a new outbound rule
         */
        interface WithOutboundRule {
            /**
             * Begins the definition of a new outbound rule to add to the load balancer
             *
             * @param name the name of the outbound rule
             * @return the first stage of the new outbound rule definition
             */
            LoadBalancerOutboundRule.DefinitionStages.Blank<? extends WithCreateAndOutboundRule> defineOutboundRule(String name);
        }

        /**
         * The stage of a load balancer definition allowing to create a new inbound NAT pool for a virtual machine scale
         * set.
         */
        interface WithInboundNatPool {
            /**
             * Begins the definition of a new inbount NAT pool to add to the load balancer.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerInboundNatPool.DefinitionStages.WithAttach#attach()}
             *
             * @param name the name of the inbound NAT pool
             * @return the first stage of the new inbound NAT pool definition
             */
            LoadBalancerInboundNatPool.DefinitionStages.Blank<WithCreateAndInboundNatPool> defineInboundNatPool(
                String name);
        }
    }

    /** Grouping of load balancer update stages. */
    interface UpdateStages {
        /** The stage of the load balancer update allowing to add or remove backends. */
        interface WithBackend {
            /**
             * Removes the specified backend from the load balancer.
             *
             * @param name the name of the backend to remove
             * @return the next stage of the update
             */
            Update withoutBackend(String name);

            /**
             * Begins the definition of a new backend as part of this load balancer update.
             *
             * @param name the name for the new backend
             * @return the first stage of the backend definition
             */
            LoadBalancerBackend.UpdateDefinitionStages.Blank<Update> defineBackend(String name);

            /**
             * Begins the description of an update to an existing backend of this load balancer.
             *
             * @param name the name of the backend to update
             * @return the first stage of the update
             */
            LoadBalancerBackend.Update updateBackend(String name);
        }

        /** The stage of the load balancer update allowing to add, remove or modify probes. */
        interface WithProbe {
            /**
             * Begins the definition of a new HTTP probe to add to the load balancer.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             *
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            LoadBalancerHttpProbe.UpdateDefinitionStages.Blank<Update> defineHttpProbe(String name);

            /**
             * Begins the definition of a new HTTPS probe to add to the load balancer.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             *
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            LoadBalancerHttpProbe.UpdateDefinitionStages.Blank<Update> defineHttpsProbe(String name);

            /**
             * Begins the definition of a new TCP probe to add to the load balancer.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerHttpProbe.DefinitionStages.WithAttach#attach()}
             *
             * @param name the name of the new probe
             * @return the next stage of the definition
             */
            LoadBalancerTcpProbe.UpdateDefinitionStages.Blank<Update> defineTcpProbe(String name);

            /**
             * Removes the specified probe from the load balancer, if present.
             *
             * @param name the name of the probe to remove
             * @return the next stage of the update
             */
            Update withoutProbe(String name);

            /**
             * Begins the description of an update to an existing TCP probe on this load balancer.
             *
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            LoadBalancerTcpProbe.Update updateTcpProbe(String name);

            /**
             * Begins the description of an update to an existing HTTP probe on this load balancer.
             *
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            LoadBalancerHttpProbe.Update updateHttpProbe(String name);

            /**
             * Begins the description of an update to an existing HTTPS probe on this load balancer.
             *
             * @param name the name of the probe to update
             * @return the first stage of the probe update
             */
            LoadBalancerHttpProbe.Update updateHttpsProbe(String name);
        }

        /** The stage of the load balancer update allowing to add, remove or modify load balancing rules. */
        interface WithLoadBalancingRule {
            /**
             * Begins the definition of a new load balancing rule to add to the load balancer.
             *
             * @param name the name of the load balancing rule
             * @return the first stage of the new load balancing rule definition
             */
            LoadBalancingRule.UpdateDefinitionStages.Blank<Update> defineLoadBalancingRule(String name);

            /**
             * Removes the specified load balancing rule from the load balancer, if present.
             *
             * @param name the name of the load balancing rule to remove
             * @return the next stage of the update
             */
            Update withoutLoadBalancingRule(String name);

            /**
             * Begins the description of an update to an existing load balancing rule on this load balancer.
             *
             * @param name the name of the load balancing rule to update
             * @return the first stage of the load balancing rule update
             */
            LoadBalancingRule.Update updateLoadBalancingRule(String name);
        }

        /** The stage of a load balancer update allowing to define, remove or edit Internet-facing frontends. */
        interface WithPublicFrontend {
            /**
             * Begins the update of a load balancer frontend.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerPublicFrontend.UpdateDefinitionStages.WithAttach#attach()}
             *
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            LoadBalancerPublicFrontend.UpdateDefinitionStages.Blank<Update> definePublicFrontend(String name);

            /**
             * Removes the specified frontend from the load balancer.
             *
             * @param name the name of an existing front end on this load balancer
             * @return the next stage of the update
             */
            Update withoutFrontend(String name);

            /**
             * Begins the description of an update to an existing Internet-facing frontend.
             *
             * @param name the name of the frontend to update
             * @return the first stage of the frontend update
             */
            LoadBalancerPublicFrontend.Update updatePublicFrontend(String name);
        }

        /** The stage of a load balancer update allowing to define one or more private frontends. */
        interface WithPrivateFrontend {
            /**
             * Begins the update of an internal load balancer frontend.
             *
             * @param name the name for the frontend
             * @return the first stage of the new frontend definition
             */
            LoadBalancerPrivateFrontend.UpdateDefinitionStages.Blank<Update> definePrivateFrontend(String name);

            /**
             * Begins the description of an update to an existing internal frontend.
             *
             * @param name the name of an existing frontend from this load balancer
             * @return the first stage of the frontend update
             */
            LoadBalancerPrivateFrontend.Update updatePrivateFrontend(String name);
        }

        /** The stage of a load balancer update allowing to define, remove or edit inbound NAT rules. */
        interface WithInboundNatRule {
            /**
             * Removes the specified inbound NAT rule from the load balancer.
             *
             * @param name the name of an existing inbound NAT rule on this load balancer
             * @return the next stage of the update
             */
            Update withoutInboundNatRule(String name);

            /**
             * Begins the definition of a new inbound NAT rule.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerInboundNatRule.UpdateDefinitionStages.WithAttach#attach()}
             *
             * @param name the name for the inbound NAT rule
             * @return the first stage of the new inbound NAT rule definition
             */
            LoadBalancerInboundNatRule.UpdateDefinitionStages.Blank<Update> defineInboundNatRule(String name);

            /**
             * Begins the description of an update to an existing inbound NAT rule.
             *
             * @param name the name of the inbound NAT rule to update
             * @return the first stage of the inbound NAT rule update
             */
            LoadBalancerInboundNatRule.Update updateInboundNatRule(String name);
        }

        /**
         * The stage of a load balancer update allowing to create a new inbound NAT pool for a virtual machine scale
         * set.
         */
        interface WithInboundNatPool {
            /**
             * Removes the specified inbound NAT pool from the load balancer.
             *
             * @param name the name of an existing inbound NAT pool on this load balancer
             * @return the next stage of the update
             */
            Update withoutInboundNatPool(String name);

            /**
             * Begins the definition of a new inbound NAT pool.
             *
             * @param name the name of the inbound NAT pool
             * @return the first stage of the new inbound NAT pool definition
             */
            LoadBalancerInboundNatPool.UpdateDefinitionStages.Blank<Update> defineInboundNatPool(String name);

            /**
             * Begins the description of an update to an existing inbound NAT pool.
             *
             * @param name the name of the inbound NAT pool to update
             * @return the first stage of the inbound NAT pool update
             */
            LoadBalancerInboundNatPool.Update updateInboundNatPool(String name);
        }

        /** The stage of a load balancer update allowing to define, remove or edit outbound rules. */
        interface WithOutboundRule {
            /**
             * Removes the specified outbound rule from the load balancer.
             *
             * @param name the name of an existing outbound rule on this load balancer
             * @return the next stage of the update
             */
            Update withoutOutboundRule(String name);

            /**
             * Begins the definition of a new inbound NAT rule.
             *
             * <p>The definition must be completed with a call to {@link
             * LoadBalancerOutboundRule.DefinitionStages.WithAttach#attach()}
             *
             * @param name the name for the outbound rule
             * @return the first stage of the new outbound rule definition
             */
            LoadBalancerOutboundRule.DefinitionStages.Blank<? extends Update> defineOutboundRule(String name);

            /**
             * Begins the description of an update to an existing outbound rule.
             *
             * @param name the name of the outbound rule to update
             * @return the first stage of the outbound rule update
             */
            LoadBalancerOutboundRule.Update<? extends Update> updateOutboundRule(String name);
        }
    }

    /** The template for a load balancer update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<LoadBalancer>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.WithProbe,
            UpdateStages.WithBackend,
            UpdateStages.WithLoadBalancingRule,
            UpdateStages.WithPublicFrontend,
            UpdateStages.WithPrivateFrontend,
            UpdateStages.WithOutboundRule,
            UpdateStages.WithInboundNatRule,
            UpdateStages.WithInboundNatPool {
    }
}
