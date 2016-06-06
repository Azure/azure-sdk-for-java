/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.Networks;
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
    private Networks networks;
    private NetworkSecurityGroups networkSecurityGroups;
    private NetworkInterfaces networkInterfaces;
    
    public static Configurable configure() {
        return new NetworkManager.ConfigurableImpl();
    }

    public static NetworkManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new NetworkManager(AzureEnvironment.AZURE.newRestClientBuilder()
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
    
    /**
     * @return entry point to virtual network management
     */
    public Networks networks() {
        if(this.networks == null) {
            this.networks = new NetworksImpl(
                    this.networkManagementClient.virtualNetworks(),
                    this.resourceManager.resourceGroups());
        }
        return this.networks;
    }
    
    /**
     * @return entry point to network security group management
     */
    public NetworkSecurityGroups networkSecurityGroups() {
        if(this.networkSecurityGroups == null) {
            this.networkSecurityGroups = new NetworkSecurityGroupsImpl(
                    this.networkManagementClient.networkSecurityGroups(),
                    this.resourceManager.resourceGroups());
        }
        return this.networkSecurityGroups;
    }
    
    /**
     * @return entry point to public IP address management
     */
    public PublicIpAddresses publicIpAddresses() {
    	if(this.publicIpAddresses == null) {
    		this.publicIpAddresses = new PublicIpAddressesImpl(
    			this.networkManagementClient.publicIPAddresses(), 
    			this.resourceManager.resourceGroups());
    	}
    	return this.publicIpAddresses;
    }

    /**
     * @return entry point to network interface management
     */
    public NetworkInterfaces networkInterfaces() {
        if (networkInterfaces == null) {
            this.networkInterfaces = new NetworkInterfacesImpl(
                this.networkManagementClient.networkInterfaces(),
                this.networks(),
                this.publicIpAddresses(),
                this.resourceManager.resourceGroups()
            );
        }
        return this.networkInterfaces;
    }
 }
