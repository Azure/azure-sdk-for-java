// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.ExpressRouteCrossConnectionPeeringInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChild;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * Client-side representation of express route cross connection peering object, associated with express route cross
 * connection.
 */
@Fluent
public interface ExpressRouteCrossConnectionPeering
    extends IndependentChild<NetworkManager>,
        HasInner<ExpressRouteCrossConnectionPeeringInner>,
        Refreshable<ExpressRouteCrossConnectionPeering>,
        Updatable<ExpressRouteCrossConnectionPeering.Update> {
    /** @return the peering type */
    ExpressRoutePeeringType peeringType();

    /** @return the peering state */
    ExpressRoutePeeringState state();

    /** @return the Azure ASN */
    int azureAsn();

    /** @return the peer ASN */
    long peerAsn();

    /** @return the primary address prefix */
    String primaryPeerAddressPrefix();

    /** @return the secondary address prefix */
    String secondaryPeerAddressPrefix();

    /** @return the primary port */
    String primaryAzurePort();

    /** @return the secondary port */
    String secondaryAzurePort();

    /** @return the shared key */
    String sharedKey();

    /** @return the VLAN ID */
    int vlanId();

    /** @return the Microsoft peering configuration */
    ExpressRouteCircuitPeeringConfig microsoftPeeringConfig();

    /** @return the provisioning state of the public IP resource */
    String provisioningState();

    /** @return the GatewayManager Etag */
    String gatewayManagerEtag();

    /** @return whether the provider or the customer last modified the peering */
    String lastModifiedBy();

    /** @return the IPv6 peering configuration. */
    Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig();

    /** The entirety of the express route Cross Connection peering definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithAdvertisedPublicPrefixes,
            DefinitionStages.WithCustomerASN,
            DefinitionStages.WithRoutingRegistryName,
            DefinitionStages.WithPrimaryPeerAddressPrefix,
            DefinitionStages.WithSecondaryPeerAddressPrefix,
            DefinitionStages.WithVlanId,
            DefinitionStages.WithPeerASN,
            DefinitionStages.WithCreate {
    }

    /** Grouping of express route Cross Connection peering definition stages. */
    interface DefinitionStages {
        interface Blank extends WithPrimaryPeerAddressPrefix {
        }

        /**
         * The stage of Express Route Cross Connection Peering definition allowing to specify advertised address
         * prefixes.
         */
        interface WithAdvertisedPublicPrefixes {
            /**
             * Specify advertised prefixes: sets a list of all prefixes that are planned to advertise over the BGP
             * session. Only public IP address prefixes are accepted. A set of prefixes can be sent as a comma-separated
             * list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefixes advertised prefixes
             * @return next stage of definition
             */
            WithCustomerASN withAdvertisedPublicPrefixes(String publicPrefixes);
        }

        /** The stage of Cross Connection Peering configuration definition allowing to specify customer ASN. */
        interface WithCustomerASN {
            /**
             * Specifies customer ASN.
             *
             * @param customerASN customer ASN
             * @return the next satge of the definition
             */
            WithRoutingRegistryName withCustomerAsn(int customerASN);
        }

        /** The stage of Cross Connection Peering definition allowing to specify routing registry name. */
        interface WithRoutingRegistryName {
            /**
             * Specifies routing registry name.
             *
             * @param routingRegistryName routing registry name
             * @return the next stage of the definition
             */
            WithPrimaryPeerAddressPrefix withRoutingRegistryName(String routingRegistryName);
        }

        /**
         * The stage of Express Route Cross Connection Peering definition allowing to specify primary address prefix.
         */
        interface WithPrimaryPeerAddressPrefix {
            WithSecondaryPeerAddressPrefix withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /**
         * The stage of Express Route Cross Connection Peering definition allowing to specify secondary address prefix.
         */
        interface WithSecondaryPeerAddressPrefix {
            WithVlanId withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Cross Connection Peering definition allowing to specify VLAN ID. */
        interface WithVlanId {
            /**
             * @param vlanId a valid VLAN ID to establish this peering on. No other peering in the circuit can use the
             *     same VLAN ID
             * @return next stage of definition
             */
            WithPeerASN withVlanId(int vlanId);
        }

        /** The stage of Express Route Cross Connection Peering definition allowing to specify AS number for peering. */
        interface WithPeerASN {
            /**
             * @param peerASN AS number for peering. Both 2-byte and 4-byte AS numbers can be used
             * @return next stage of definition
             */
            WithCreate withPeerAsn(long peerASN);
        }

        interface WithSharedKey {
            WithCreate withSharedKey(String sharedKey);
        }

        interface WithIpv6PeeringConfig {
            Ipv6PeeringConfig.DefinitionStages.Blank<WithCreate> defineIpv6Config();

            WithCreate withoutIpv6Config();
        }

        /** The stage of Express Route Cross Connection Peering definition allowing to specify the peering state. */
        interface WithState {
            /**
             * Specifies the peering state.
             *
             * @param state the peering state
             * @return the next stage of the definition
             */
            WithCreate withState(ExpressRoutePeeringState state);
        }

        interface WithCreate
            extends Creatable<ExpressRouteCrossConnectionPeering>,
                DefinitionStages.WithSharedKey,
                DefinitionStages.WithIpv6PeeringConfig,
                DefinitionStages.WithState {
        }
    }

    /** Grouping of express route cross connection peering update stages. */
    interface Update
        extends Appliable<ExpressRouteCrossConnectionPeering>,
            UpdateStages.WithAdvertisedPublicPrefixes,
            UpdateStages.WithCustomerASN,
            UpdateStages.WithRoutingRegistryName,
            UpdateStages.WithPrimaryPeerAddressPrefix,
            UpdateStages.WithSecondaryPeerAddressPrefix,
            UpdateStages.WithVlanId,
            UpdateStages.WithPeerASN,
            UpdateStages.WithIpv6PeeringConfig,
            UpdateStages.WithState {
    }

    /**
     * The template for express route Cross Connection peering update operation, containing all the settings that can be
     * modified.
     */
    interface UpdateStages {
        /**
         * The stage of Express Route Cross Connection Peering update allowing to specify advertised address prefixes.
         */
        interface WithAdvertisedPublicPrefixes {
            Update withAdvertisedPublicPrefixes(String publicPrefixes);
        }

        /** The stage of Cross Connection Peering configuration definition allowing to specify customer ASN. */
        interface WithCustomerASN {
            /**
             * Specifies customer ASN.
             *
             * @param customerASN customer ASN
             * @return the next stage of the definition
             */
            Update withCustomerAsn(int customerASN);
        }

        /** The stage of Cross Connection Peering definition allowing to specify routing registry name. */
        interface WithRoutingRegistryName {
            /**
             * Specifies routing registry name.
             *
             * @param routingRegistryName routing registry name
             * @return the next stage of the definition
             */
            Update withRoutingRegistryName(String routingRegistryName);
        }

        /** The stage of Express Route Cross Connection Peering update allowing to specify primary address prefix. */
        interface WithPrimaryPeerAddressPrefix {
            Update withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Cross Connection Peering update allowing to specify secondary address prefix. */
        interface WithSecondaryPeerAddressPrefix {
            /**
             * @param addressPrefix secondary address prefix
             * @return the next stage of the update
             */
            Update withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Cross Connection Peering update allowing to specify VLAN ID. */
        interface WithVlanId {
            /**
             * Sets the VLAN ID.
             *
             * @param vlanId VLAN ID
             * @return the next stage of the update
             */
            Update withVlanId(int vlanId);
        }

        /** The stage of Express Route Cross Connection Peering update allowing to specify AS number for peering. */
        interface WithPeerASN {
            /**
             * Sets peer ASN.
             *
             * @param peerASN the AS number for peering
             * @return the next stage of the update
             */
            Update withPeerAsn(long peerASN);
        }

        /** Specifies IPv6 configuration. */
        interface WithIpv6PeeringConfig {
            /**
             * Begins the definition of IPv6 configuration.
             *
             * @return next stage of Ipv6 configuration definition
             */
            Ipv6PeeringConfig.UpdateDefinitionStages.Blank<Update> defineIpv6Config();

            /**
             * Removes IPv6 configuration from peering.
             *
             * @return the next stage of the update
             */
            Update withoutIpv6Config();
        }

        /** The stage of Express Route Cross Connection Peering update allowing to specify the peering state. */
        interface WithState {
            /**
             * Specifies the peering state.
             *
             * @param state the peering state
             * @return the next stage of the update
             */
            Update withState(ExpressRoutePeeringState state);
        }
    }
}
