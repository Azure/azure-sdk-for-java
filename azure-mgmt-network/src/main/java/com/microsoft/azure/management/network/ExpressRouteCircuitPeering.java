/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ExpressRouteCircuitPeeringInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Client-side representation of express route circuit peering object, associated with express route circuit.
 */
@Fluent
@Beta
public interface ExpressRouteCircuitPeering extends
        HasInner<ExpressRouteCircuitPeeringInner>,
        HasName,
        HasId,
        Indexable,
        Refreshable<ExpressRouteCircuitPeering>,
        Updatable<ExpressRouteCircuitPeering.Update>,
        HasParent<ExpressRouteCircuit> {

    /**
     * The entirety of the express route circuit peering definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithAdvertisedPublicPrefixes,
        DefinitionStages.WithPrimaryPeerAddressPrefix,
        DefinitionStages.WithSecondaryPeerAddressPrefix,
        DefinitionStages.WithVlanId,
        DefinitionStages.WithPeerAsn,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of express route circuit peering definition stages.
     */
    interface DefinitionStages {
        interface Blank extends WithPrimaryPeerAddressPrefix {
        }

        interface WithAdvertisedPublicPrefixes {
            WithPrimaryPeerAddressPrefix withAdvertisedPublicPrefixes(String publicPrefixes);
        }

        interface WithPrimaryPeerAddressPrefix {
            WithSecondaryPeerAddressPrefix withPrimaryPeerAddressPrefix(String addressPrefix);
        }

        interface WithSecondaryPeerAddressPrefix {
            WithVlanId withSecondaryPeerAddressPrefix(String addressPrefix);
        }

        interface WithVlanId {
            WithPeerAsn withVlanId(int vlanId);
        }

        interface WithPeerAsn {
            WithCreate withPeerAsn(int peerAsn);
        }

        interface WithCreate extends
                Creatable<ExpressRouteCircuitPeering> {
        }
    }

    /**
     * Grouping of express route circuit peering update stages.
     */
    interface Update extends Appliable<ExpressRouteCircuitPeering> {
    }

    /**
     * The template for express route circuit peering update operation, containing all the settings that
     * can be modified.
     */
    interface UpdateStages {
    }
}
