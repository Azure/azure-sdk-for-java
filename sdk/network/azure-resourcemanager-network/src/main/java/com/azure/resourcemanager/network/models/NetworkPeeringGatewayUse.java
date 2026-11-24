// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

/** Possible gateway use scenarios. */
public enum NetworkPeeringGatewayUse {
    /** The remote network is allowed to use this network's gateway (but not necessarily using it currently). */
    BY_REMOTE_NETWORK,

    /** This network is configured to use the remote network's gateway. */
    ON_REMOTE_NETWORK,

    /** No gateway use is configured. */
    NONE
}
