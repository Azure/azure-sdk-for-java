// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;
import java.util.List;

/** An client-side representation of a load balancer frontend. */
@Fluent
public interface Ipv6PeeringConfig extends HasInnerModel<Ipv6ExpressRouteCircuitPeeringConfig> {

    /** Grouping of public frontend definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of a public frontend definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAdvertisedPublicPrefixes<ParentT> {
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify primary address
         * prefix.
         */
        interface WithPrimaryPeerAddressPrefix<ParentT> {
            /**
             * @param addressPrefix primary peer address prefix
             * @return the next stage of the definition
             */
            WithSecondaryPeerAddressPrefix<ParentT> withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify secondary address
         * prefix.
         */
        interface WithSecondaryPeerAddressPrefix<ParentT> {
            /**
             * @param addressPrefix secondary peer address prefix
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Cross Connection Peering IPv6 configuration definition allowing to specify customer ASN. */
        interface WithCustomerASN<ParentT> {
            /**
             * Specifies customer ASN.
             *
             * @param customerASN customer ASN
             * @return the next satge of the definition
             */
            WithRoutingRegistryName<ParentT> withCustomerAsn(int customerASN);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify routing registry
         * name.
         */
        interface WithRoutingRegistryName<ParentT> {
            /**
             * Specifies routing registry name.
             *
             * @param routingRegistryName routing registry name
             * @return the next stage of the definition
             */
            WithPrimaryPeerAddressPrefix<ParentT> withRoutingRegistryName(String routingRegistryName);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify secondary address
         * prefix.
         */
        interface WithAdvertisedPublicPrefixes<ParentT> {
            /**
             * Specify advertised prefixes: sets a list of all prefixes that are planned to advertise over the BGP
             * session. Method will overwrite existing list. Only public IP address prefixes are accepted. A set of
             * prefixes can be sent as a comma-separated list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefixes advertised prefixes
             * @return next stage of definition
             */
            WithCustomerASN<ParentT> withAdvertisedPublicPrefixes(List<String> publicPrefixes);

            /**
             * Specify advertised prefix: sets a prefix that is planned to advertise over the BGP session. Method will
             * add a prefix to existing list. Only public IP address prefixes are accepted. A set of prefixes can be
             * sent as a comma-separated list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefix advertised prefix
             * @return next stage of definition
             */
            WithCustomerASN<ParentT> withAdvertisedPublicPrefix(String publicPrefix);
        }

        /** The stage of Cross Connection Peering IPv6 configuration definition allowing to specify route filter. */
        interface WithRouteFilter<ParentT> {
            /**
             * Sets route filter id.
             *
             * @param routeFilterId route filter id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRouteFilter(String routeFilterId);

            /**
             * Remove route filter from IPv6 configuration.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutRouteFilter();
        }

        /**
         * The final stage of a public frontend definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the frontend definition can be
         * attached to the parent load balancer definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithRouteFilter<ParentT> {
        }
    }

    /**
     * The entirety of a public frontend definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithPrimaryPeerAddressPrefix<ParentT>,
            DefinitionStages.WithSecondaryPeerAddressPrefix<ParentT>,
            DefinitionStages.WithCustomerASN<ParentT>,
            DefinitionStages.WithRoutingRegistryName<ParentT> {
    }

    /** Grouping of public frontend update stages. */
    interface UpdateStages {
        /** The stage of a public frontend update allowing to specify an existing public IP address. */
        interface WithAdvertisedPublicPrefixes {
            /**
             * Specify advertised prefixes: sets a list of all prefixes that are planned to advertise over the BGP
             * session. Only public IP address prefixes are accepted. A set of prefixes can be sent as a comma-separated
             * list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefixes advertised prefixes
             * @return next stage of update
             */
            Update withAdvertisedPublicPrefixes(List<String> publicPrefixes);

            /**
             * Specify advertised prefix: sets a prefix that is planned to advertise over the BGP session. Method will
             * add a prefix to existing list. Only public IP address prefixes are accepted. A set of prefixes can be
             * sent as a comma-separated list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefix advertised prefix
             * @return next stage of definition
             */
            Update withAdvertisedPublicPrefix(String publicPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration update allowing to specify primary address prefix.
         */
        interface WithPrimaryPeerAddressPrefix {
            /**
             * @param addressPrefix primary peer address prefix
             * @return the next stage of the update
             */
            Update withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration update allowing to specify secondary address prefix.
         */
        interface WithSecondaryPeerAddressPrefix {
            /**
             * @param addressPrefix secondary peer address prefix
             * @return the next stage of the update
             */
            Update withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration update allowing to specify secondary customer ASN.
         */
        interface WithCustomerASN {
            /**
             * Specifies customer ASN.
             *
             * @param customerASN customer ASN
             * @return the next stage of the update
             */
            Update withCustomerAsn(int customerASN);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration update allowing to specify routing registry name.
         */
        interface WithRoutingRegistryName {
            /**
             * Specifies routing registry name.
             *
             * @param routingRegistryName routing registry name
             * @return the next stage of the definition
             */
            Update withRoutingRegistryName(String routingRegistryName);
        }

        /** The stage of Cross Connection Peering IPv6 configuration update allowing to specify route filter. */
        interface WithRouteFilter {
            /**
             * Sets route filter id.
             *
             * @param routeFilterId route filter id
             * @return the next stage of the definition
             */
            Update withRouteFilter(String routeFilterId);

            /**
             * Remove route filter from IPv6 configuration.
             *
             * @return the next stage of the definition
             */
            Update withoutRouteFilter();
        }
    }

    /** The entirety of a public frontend update as part of an Internet-facing load balancer update. */
    interface Update
        extends Settable<ExpressRouteCrossConnectionPeering.Update>,
            UpdateStages.WithAdvertisedPublicPrefixes,
            UpdateStages.WithPrimaryPeerAddressPrefix,
            UpdateStages.WithSecondaryPeerAddressPrefix,
            UpdateStages.WithCustomerASN,
            UpdateStages.WithRoutingRegistryName,
            UpdateStages.WithRouteFilter {
    }

    /** Grouping of public frontend definition stages applicable as part of an Internet-facing load balancer update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a public frontend definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends UpdateDefinitionStages.WithAdvertisedPublicPrefixes<ParentT> {
        }

        interface WithAdvertisedPublicPrefixes<ParentT> {
            /**
             * Specify advertised prefixes: sets a list of all prefixes that are planned to advertise over the BGP
             * session. Only public IP address prefixes are accepted. A set of prefixes can be sent as a comma-separated
             * list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefixes advertised prefixes
             * @return next stage of definition
             */
            WithCustomerASN<ParentT> withAdvertisedPublicPrefixes(List<String> publicPrefixes);

            /**
             * Specify advertised prefix: sets a prefix that is planned to advertise over the BGP session. Method will
             * add a prefix to existing list. Only public IP address prefixes are accepted. A set of prefixes can be
             * sent as a comma-separated list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefix advertised prefix
             * @return next stage of definition
             */
            WithCustomerASN<ParentT> withAdvertisedPublicPrefix(String publicPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify primary address
         * prefix.
         */
        interface WithPrimaryPeerAddressPrefix<ParentT> {
            /**
             * @param addressPrefix primary peer address prefix
             * @return the next stage of the definition
             */
            WithSecondaryPeerAddressPrefix<ParentT> withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify secondary address
         * prefix.
         */
        interface WithSecondaryPeerAddressPrefix<ParentT> {
            /**
             * @param addressPrefix secondary peer address prefix
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify secondary customer
         * ASN.
         */
        interface WithCustomerASN<ParentT> {
            /**
             * Specifies customer ASN.
             *
             * @param customerASN customer ASN
             * @return the next satge of the definition
             */
            WithRoutingRegistryName<ParentT> withCustomerAsn(int customerASN);
        }

        /**
         * The stage of Cross Connection Peering IPv6 configuration definition allowing to specify routing registry
         * name.
         */
        interface WithRoutingRegistryName<ParentT> {
            /**
             * Specifies routing registry name.
             *
             * @param routingRegistryName routing registry name
             * @return the next stage of the definition
             */
            WithPrimaryPeerAddressPrefix<ParentT> withRoutingRegistryName(String routingRegistryName);
        }

        /** The stage of Cross Connection Peering IPv6 configuration definition allowing to specify route filter. */
        interface WithRouteFilter<ParentT> {
            /**
             * Sets route filter id.
             *
             * @param routeFilterId route filter id
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withRouteFilter(String routeFilterId);

            /**
             * Remove route filter from IPv6 configuration.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withoutRouteFilter();
        }

        /**
         * The final stage of peering IPv6 configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the frontend definition can be
         * attached to the parent peering definition definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT>, WithRouteFilter<ParentT> {
        }
    }

    /**
     * The entirety of Cross Connection Peering IPv6 configuration definition as part of Cross Connection Peering
     * update.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT>,
            UpdateDefinitionStages.WithAdvertisedPublicPrefixes<ParentT>,
            UpdateDefinitionStages.WithPrimaryPeerAddressPrefix<ParentT>,
            UpdateDefinitionStages.WithSecondaryPeerAddressPrefix<ParentT>,
            UpdateDefinitionStages.WithCustomerASN<ParentT>,
            UpdateDefinitionStages.WithRoutingRegistryName<ParentT> {
    }
}
