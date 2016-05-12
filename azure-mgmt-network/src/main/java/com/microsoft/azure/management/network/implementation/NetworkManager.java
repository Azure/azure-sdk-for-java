/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.implementation.api.NetworkManagementClientImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class NetworkManager {
    private final NetworkManagementClientImpl networkManagementClient;
    
    // Dependent managers
    private final ResourceManager resourceManager;
    
    // Collections
    private PublicIpAddresses publicIpAddresses;
    
    public static Configurable configure() {
        return new NetworkManager.ConfigurableImpl();
    }

    public static NetworkManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new NetworkManager(new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    public static NetworkManager authenticate(RestClient restClient, String subscriptionId) {
        return new NetworkManager(restClient, subscriptionId);
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
        NetworkManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public NetworkManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return NetworkManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private NetworkManager(RestClient restClient, String subscriptionId) {
        networkManagementClient = new NetworkManagementClientImpl(restClient);
        networkManagementClient.setSubscriptionId(subscriptionId);
        this.resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
    }
    
    public PublicIpAddresses publicIpAddresses() {
    	if(this.publicIpAddresses == null) {
    		this.publicIpAddresses = new PublicIpAddressesImpl(
    			this.networkManagementClient.publicIPAddresses(), 
    			this.resourceManager.resourceGroups());
    	}
    	return this.publicIpAddresses;
    }
 }
