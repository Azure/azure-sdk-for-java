// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.network.models.ExpressRouteCircuitPeeringInner;
import com.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;

/** Client-side representation of express route circuit peering object, associated with express route circuit. */
@Fluent
public interface ExpressRouteCircuitPeering
    extends IndependentChild<NetworkManager>,
        HasInner<ExpressRouteCircuitPeeringInner>,
        Refreshable<ExpressRouteCircuitPeering>,
        Updatable<ExpressRouteCircuitPeering.Update> {

    // Getters

    /** @return peering type */
    ExpressRoutePeeringType peeringType();

    /**
     * Gets the state of peering.
     *
     * @return peering state
     */
    ExpressRoutePeeringState state();

    /** @return the Azure ASN */
    int azureASN();

    /** @return the peer ASN */
    long peerASN();

    /** @return the primary address prefix */
    String primaryPeerAddressPrefix();

    /** @return the secondary address prefix. */
    String secondaryPeerAddressPrefix();

    /** @return the primary port */
    String primaryAzurePort();

    /** @return the secondary port */
    String secondaryAzurePort();

    /** @return the shared key */
    String sharedKey();

    /** @return the VLAN ID */
    int vlanId();

    /** @return The Microsoft peering configuration. */
    ExpressRouteCircuitPeeringConfig microsoftPeeringConfig();

    /** @return peering stats */
    ExpressRouteCircuitStats stats();

    /**
     * Gets the provisioning state of the resource.
     *
     * @return provisioningState
     */
    String provisioningState();

    /** @return whether the provider or the customer last modified the peering */
    String lastModifiedBy();

    /** @return the IPv6 peering configuration */
    Ipv6ExpressRouteCircuitPeeringConfig ipv6PeeringConfig();

    /** The entirety of the express route circuit peering definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithAdvertisedPublicPrefixes,
            DefinitionStages.WithPrimaryPeerAddressPrefix,
            DefinitionStages.WithSecondaryPeerAddressPrefix,
            DefinitionStages.WithVlanId,
            DefinitionStages.WithPeerASN,
            DefinitionStages.WithCreate {
    }

    /** Grouping of express route circuit peering definition stages. */
    interface DefinitionStages {
        interface Blank extends WithPrimaryPeerAddressPrefix {
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify advertised address prefixes. */
        interface WithAdvertisedPublicPrefixes {
            /**
             * Specify advertised prefixes: sets a list of all prefixes that are planned to advertise over the BGP
             * session. Only public IP address prefixes are accepted. A set of prefixes can be sent as a comma-separated
             * list. These prefixes must be registered to you in an RIR / IRR.
             *
             * @param publicPrefixes advertised prefixes
             * @return next stage of definition
             */
            WithPrimaryPeerAddressPrefix withAdvertisedPublicPrefixes(String publicPrefixes);
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify primary address prefix. */
        interface WithPrimaryPeerAddressPrefix {
            WithSecondaryPeerAddressPrefix withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify secondary address prefix. */
        interface WithSecondaryPeerAddressPrefix {
            WithVlanId withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify VLAN ID. */
        interface WithVlanId {
            /**
             * @param vlanId a valid VLAN ID to establish this peering on. No other peering in the circuit can use the
             *     same VLAN ID
             * @return next stage of definition
             */
            WithPeerASN withVlanId(int vlanId);
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify AS number for peering. */
        interface WithPeerASN {
            /**
             * @param peerASN AS number for peering. Both 2-byte and 4-byte AS numbers can be used
             * @return next stage of definition
             */
            WithCreate withPeerASN(long peerASN);
        }

        interface WithCreate extends Creatable<ExpressRouteCircuitPeering> {
        }
    }

    /** Grouping of express route circuit peering update stages. */
    interface Update
        extends Appliable<ExpressRouteCircuitPeering>,
            UpdateStages.WithAdvertisedPublicPrefixes,
            UpdateStages.WithPrimaryPeerAddressPrefix,
            UpdateStages.WithSecondaryPeerAddressPrefix,
            UpdateStages.WithVlanId,
            UpdateStages.WithPeerASN {
    }

    /**
     * The template for express route circuit peering update operation, containing all the settings that can be
     * modified.
     */
    interface UpdateStages {
        /** The stage of Express Route Circuit Peering update allowing to specify advertised address prefixes. */
        interface WithAdvertisedPublicPrefixes {
            Update withAdvertisedPublicPrefixes(String publicPrefixes);
        }

        /** The stage of Express Route Circuit Peering update allowing to specify primary address prefix. */
        interface WithPrimaryPeerAddressPrefix {
            Update withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Circuit Peering update allowing to specify secondary address prefix. */
        interface WithSecondaryPeerAddressPrefix {
            Update withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        /** The stage of Express Route Circuit Peering update allowing to specify VLAN ID. */
        interface WithVlanId {
            Update withVlanId(int vlanId);
        }

        /** The stage of Express Route Circuit Peering update allowing to specify AS number for peering. */
        interface WithPeerASN {
            Update withPeerASN(long peerASN);
        }
    }
}
