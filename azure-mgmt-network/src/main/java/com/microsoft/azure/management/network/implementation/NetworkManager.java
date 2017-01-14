/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.RestClient;
import com.microsoft.azure.SubResource;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.NetworkUsages;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.RouteTables;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;

/**
 * Entry point to Azure network management.
 */
public final class NetworkManager extends Manager<NetworkManager, NetworkManagementClientImpl> {

    // Collections
    private PublicIpAddresses publicIpAddresses;
    private Networks networks;
    private NetworkSecurityGroups networkSecurityGroups;
    private NetworkInterfaces networkInterfaces;
    private LoadBalancers loadBalancers;
    private RouteTables routeTables;
    private ApplicationGateways applicationGateways;
    private NetworkUsages networkUsages;

    /**
     * Get a Configurable instance that can be used to create {@link NetworkManager}
     * with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new NetworkManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of NetworkManager that exposes network resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the NetworkManager
     */
    public static NetworkManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new NetworkManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of NetworkManager that exposes network resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the NetworkManager
     */
    public static NetworkManager authenticate(RestClient restClient, String subscriptionId) {
        return new NetworkManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of NetworkManager that exposes network management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing network management API entry points that work across subscriptions
         */
        NetworkManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
        extends AzureConfigurableImpl<Configurable>
        implements Configurable {

        public NetworkManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return NetworkManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private NetworkManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new NetworkManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return entry point to route table management
     */
    public RouteTables routeTables() {
        if (this.routeTables == null) {
            this.routeTables = new RouteTablesImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.routeTables;
    }

    /**
     * @return entry point to virtual network management
     */
    public Networks networks() {
        if (this.networks == null) {
            this.networks = new NetworksImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.networks;
    }

    /**
     * @return entry point to network security group management
     */
    public NetworkSecurityGroups networkSecurityGroups() {
        if (this.networkSecurityGroups == null) {
            this.networkSecurityGroups = new NetworkSecurityGroupsImpl(
                    super.innerManagementClient.networkSecurityGroups(),
                    this);
        }
        return this.networkSecurityGroups;
    }

    /**
     * @return entry point to public IP address management
     */
    public PublicIpAddresses publicIpAddresses() {
        if (this.publicIpAddresses == null) {
            this.publicIpAddresses = new PublicIpAddressesImpl(
                    super.innerManagementClient.publicIPAddresses(),
                    this);
        }
        return this.publicIpAddresses;
    }

    /**
     * @return entry point to network interface management
     */
    public NetworkInterfaces networkInterfaces() {
        if (networkInterfaces == null) {
            this.networkInterfaces = new NetworkInterfacesImpl(
                    super.innerManagementClient.networkInterfaces(),
                    this);
        }
        return this.networkInterfaces;
    }

    /**
     * @return entry point to appplication gateway management
     */
    public ApplicationGateways applicationGateways() {
        if (this.applicationGateways == null) {
            this.applicationGateways = new ApplicationGatewaysImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.applicationGateways;
    }

    /**
     * @return entry point to load balancer management
     */
    public LoadBalancers loadBalancers() {
        if (this.loadBalancers == null) {
            this.loadBalancers = new LoadBalancersImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.loadBalancers;
    }

    /**
     * @return entry point to network resource usage management API entry point
     */
    public NetworkUsages usages() {
        if (this.networkUsages == null) {
            this.networkUsages = new NetworkUsagesImpl(super.innerManagementClient);
        }
        return this.networkUsages;
    }

    // Internal utility funtion
    Subnet getAssociatedSubnet(SubResource subnetRef) {
        if (subnetRef == null) {
            return null;
        }

        String vnetId = ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        String subnetName = ResourceUtils.nameFromResourceId(subnetRef.id());

        if (vnetId == null || subnetName == null) {
            return null;
        }

        Network network = this.networks().getById(vnetId);
        if (network == null) {
            return null;
        }

        return network.subnets().get(subnetName);
    }

    // Internal utility function
    List<Subnet> listAssociatedSubnets(List<SubnetInner> subnetRefs) {
        final Map<String, Network> networks = new HashMap<>();
        final List<Subnet> subnets = new ArrayList<>();

        if (subnetRefs != null) {
            for (SubnetInner subnetRef : subnetRefs) {
                String networkId = ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
                Network network = networks.get(networkId);
                if (network == null) {
                    network = this.networks().getById(networkId);
                    networks.put(networkId, network);
                }

                String subnetName = ResourceUtils.nameFromResourceId(subnetRef.id());
                subnets.add(network.subnets().get(subnetName));
            }
        }

        return Collections.unmodifiableList(subnets);
    }
}
