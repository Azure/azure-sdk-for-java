// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.ExpressRouteCircuitPeeringInner;
import com.azure.resourcemanager.network.fluent.models.Ipv6ExpressRouteCircuitPeeringConfigInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChild;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** Client-side representation of express route circuit peering object, associated with express route circuit. */
@Fluent
public interface ExpressRouteCircuitPeering
    extends IndependentChild<NetworkManager>,
        HasInnerModel<ExpressRouteCircuitPeeringInner>,
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
    int azureAsn();

    /** @return the peer ASN */
    long peerAsn();

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
    Ipv6ExpressRouteCircuitPeeringConfigInner ipv6PeeringConfig();

    /** The entirety of the express route circuit peering definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithAdvertisedPublicPrefixes,
            DefinitionStages.WithPrimaryPeerAddressPrefix,
            DefinitionStages.WithSecondaryPeerAddressPrefix,
            DefinitionStages.WithVlanId,
            DefinitionStages.WithPeerAsn,
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
            WithPeerAsn withVlanId(int vlanId);
        }

        /** The stage of Express Route Circuit Peering definition allowing to specify AS number for peering. */
        interface WithPeerAsn {
            /**
             * @param peerAsn AS number for peering. Both 2-byte and 4-byte AS numbers can be used
             * @return next stage of definition
             */
            WithCreate withPeerAsn(long peerAsn);
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
            UpdateStages.WithPeerAsn {
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
        interface WithPeerAsn {
            Update withPeerAsn(long peerAsn);
        }
    }
}
