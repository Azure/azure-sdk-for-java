/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.ResourceConnectorBase;
import com.microsoft.rest.RestClient;

public class NetworkResourceConnector extends ResourceConnectorBase {
    private NetworkManager networkClient;
    private PublicIpAddresses.InGroup publicIpAddresses;

    private NetworkResourceConnector(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        super(restClient, subscriptionId, resourceGroup);
    }

    private static NetworkResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
        return new NetworkResourceConnector(restClient, subscriptionId, resourceGroup);
    }

    public static class Builder implements ResourceConnector.Builder<NetworkResourceConnector> {
        public NetworkResourceConnector create(RestClient restClient, String subscriptionId, ResourceGroup resourceGroup) {
            return NetworkResourceConnector.create(restClient, subscriptionId, resourceGroup);
        }
    }

    public PublicIpAddresses.InGroup publicIpAddresses() {
        if (this.publicIpAddresses == null) {
            this.publicIpAddresses = new PublicIpAddressesInGroupImpl(networkClient().publicIpAddresses(), resourceGroup);
        }
        return this.publicIpAddresses;
    }

    private NetworkManager networkClient() {
        if (this.networkClient == null) {
            this.networkClient = NetworkManager
                    .authenticate(restClient, subscriptionId);
        }
        return this.networkClient;
    }
}
