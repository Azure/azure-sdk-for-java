// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.network.fluent.NetworkManagementClient;
import com.azure.resourcemanager.network.implementation.NetworkManagementClientBuilder;
import com.azure.resourcemanager.network.implementation.ApplicationGatewaysImpl;
import com.azure.resourcemanager.network.implementation.ApplicationSecurityGroupsImpl;
import com.azure.resourcemanager.network.implementation.DdosProtectionPlansImpl;
import com.azure.resourcemanager.network.implementation.ExpressRouteCircuitsImpl;
import com.azure.resourcemanager.network.implementation.ExpressRouteCrossConnectionsImpl;
import com.azure.resourcemanager.network.implementation.LoadBalancersImpl;
import com.azure.resourcemanager.network.implementation.LocalNetworkGatewaysImpl;
import com.azure.resourcemanager.network.implementation.NetworkInterfacesImpl;
import com.azure.resourcemanager.network.implementation.NetworkProfilesImpl;
import com.azure.resourcemanager.network.implementation.NetworkSecurityGroupsImpl;
import com.azure.resourcemanager.network.implementation.NetworkUsagesImpl;
import com.azure.resourcemanager.network.implementation.NetworkWatchersImpl;
import com.azure.resourcemanager.network.implementation.NetworksImpl;
import com.azure.resourcemanager.network.implementation.PublicIpAddressesImpl;
import com.azure.resourcemanager.network.implementation.PublicIpPrefixesImpl;
import com.azure.resourcemanager.network.implementation.RouteFiltersImpl;
import com.azure.resourcemanager.network.implementation.RouteTablesImpl;
import com.azure.resourcemanager.network.implementation.VirtualNetworkGatewaysImpl;
import com.azure.resourcemanager.network.models.ApplicationGateways;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroups;
import com.azure.resourcemanager.network.models.DdosProtectionPlans;
import com.azure.resourcemanager.network.models.ExpressRouteCircuits;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnections;
import com.azure.resourcemanager.network.models.LoadBalancers;
import com.azure.resourcemanager.network.models.LocalNetworkGateways;
import com.azure.resourcemanager.network.models.NetworkInterfaces;
import com.azure.resourcemanager.network.models.NetworkProfiles;
import com.azure.resourcemanager.network.models.NetworkSecurityGroups;
import com.azure.resourcemanager.network.models.NetworkUsages;
import com.azure.resourcemanager.network.models.NetworkWatchers;
import com.azure.resourcemanager.network.models.Networks;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.resourcemanager.network.models.PublicIpPrefixes;
import com.azure.resourcemanager.network.models.RouteFilters;
import com.azure.resourcemanager.network.models.RouteTables;
import com.azure.resourcemanager.network.models.VirtualNetworkGateways;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/** Entry point to Azure network management. */
public final class NetworkManager extends Manager<NetworkManagementClient> {

    // Collections
    private PublicIpAddresses publicIPAddresses;
    private PublicIpPrefixes publicIpPrefixes;
    private Networks networks;
    private NetworkSecurityGroups networkSecurityGroups;
    private NetworkInterfaces networkInterfaces;
    private LoadBalancers loadBalancers;
    private RouteTables routeTables;
    private ApplicationGateways applicationGateways;
    private NetworkUsages networkUsages;
    private NetworkWatchers networkWatchers;
    private VirtualNetworkGateways virtualNetworkGateways;
    private LocalNetworkGateways localNetworkGateways;
    private ExpressRouteCircuits expressRouteCircuits;
    private ApplicationSecurityGroups applicationSecurityGroups;
    private RouteFilters routeFilters;
    private DdosProtectionPlans ddosProtectionPlans;
    private ExpressRouteCrossConnections expressRouteCrossConnections;
    private NetworkProfiles networkProfiles;

    /**
     * Get a Configurable instance that can be used to create {@link NetworkManager} with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new NetworkManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of NetworkManager that exposes network resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the NetworkManager
     */
    public static NetworkManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of NetworkManager that exposes network resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the NetworkManager
     */
    private static NetworkManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new NetworkManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of NetworkManager that exposes network management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing network management API entry points that work across subscriptions
         */
        NetworkManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {

        public NetworkManager authenticate(TokenCredential credential, AzureProfile profile) {
            return NetworkManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private NetworkManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new NetworkManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /** @return entry point to route table management */
    public RouteTables routeTables() {
        if (this.routeTables == null) {
            this.routeTables = new RouteTablesImpl(this);
        }
        return this.routeTables;
    }

    /** @return entry point to virtual network management */
    public Networks networks() {
        if (this.networks == null) {
            this.networks = new NetworksImpl(this);
        }
        return this.networks;
    }

    /** @return entry point to network security group management */
    public NetworkSecurityGroups networkSecurityGroups() {
        if (this.networkSecurityGroups == null) {
            this.networkSecurityGroups = new NetworkSecurityGroupsImpl(this);
        }
        return this.networkSecurityGroups;
    }

    /** @return entry point to public IP address management */
    public PublicIpAddresses publicIpAddresses() {
        if (this.publicIPAddresses == null) {
            this.publicIPAddresses = new PublicIpAddressesImpl(this);
        }
        return this.publicIPAddresses;
    }

    /** @return entry point to public IP prefix management */
    public PublicIpPrefixes publicIpPrefixes() {
        if (this.publicIpPrefixes == null) {
            this.publicIpPrefixes = new PublicIpPrefixesImpl(this);
        }
        return this.publicIpPrefixes;
    }

    /** @return entry point to network interface management */
    public NetworkInterfaces networkInterfaces() {
        if (networkInterfaces == null) {
            this.networkInterfaces = new NetworkInterfacesImpl(this);
        }
        return this.networkInterfaces;
    }

    /** @return entry point to application gateway management */
    public ApplicationGateways applicationGateways() {
        if (this.applicationGateways == null) {
            this.applicationGateways = new ApplicationGatewaysImpl(this);
        }
        return this.applicationGateways;
    }

    /** @return entry point to load balancer management */
    public LoadBalancers loadBalancers() {
        if (this.loadBalancers == null) {
            this.loadBalancers = new LoadBalancersImpl(this);
        }
        return this.loadBalancers;
    }

    /** @return entry point to network resource usage management API entry point */
    public NetworkUsages usages() {
        if (this.networkUsages == null) {
            this.networkUsages = new NetworkUsagesImpl(this.serviceClient());
        }
        return this.networkUsages;
    }

    /** @return entry point to network watchers management API entry point */
    public NetworkWatchers networkWatchers() {
        if (this.networkWatchers == null) {
            this.networkWatchers = new NetworkWatchersImpl(this);
        }
        return this.networkWatchers;
    }

    /** @return entry point to virtual network gateways management */
    public VirtualNetworkGateways virtualNetworkGateways() {
        if (this.virtualNetworkGateways == null) {
            this.virtualNetworkGateways = new VirtualNetworkGatewaysImpl(this);
        }
        return this.virtualNetworkGateways;
    }

    /** @return entry point to local network gateway management */
    public LocalNetworkGateways localNetworkGateways() {
        if (this.localNetworkGateways == null) {
            this.localNetworkGateways = new LocalNetworkGatewaysImpl(this);
        }
        return this.localNetworkGateways;
    }

    /** @return entry point to express route circuit management */
    public ExpressRouteCircuits expressRouteCircuits() {
        if (this.expressRouteCircuits == null) {
            this.expressRouteCircuits = new ExpressRouteCircuitsImpl(this);
        }
        return this.expressRouteCircuits;
    }

    /** @return entry point to application security groups management */
    public ApplicationSecurityGroups applicationSecurityGroups() {
        if (this.applicationSecurityGroups == null) {
            this.applicationSecurityGroups = new ApplicationSecurityGroupsImpl(this);
        }
        return this.applicationSecurityGroups;
    }

    /** @return entry point to application security groups management */
    public RouteFilters routeFilters() {
        if (this.routeFilters == null) {
            this.routeFilters = new RouteFiltersImpl(this);
        }
        return this.routeFilters;
    }

    /** @return entry point to DDoS protection plans management */
    public DdosProtectionPlans ddosProtectionPlans() {
        if (this.ddosProtectionPlans == null) {
            this.ddosProtectionPlans = new DdosProtectionPlansImpl(this);
        }
        return this.ddosProtectionPlans;
    }

    /** @return entry point to express route cross connections management */
    public ExpressRouteCrossConnections expressRouteCrossConnections() {
        if (this.expressRouteCrossConnections == null) {
            this.expressRouteCrossConnections = new ExpressRouteCrossConnectionsImpl(this);
        }
        return this.expressRouteCrossConnections;
    }

    /** @return entry point to network profiles management */
    public NetworkProfiles networkProfiles() {
        if (this.networkProfiles == null) {
            this.networkProfiles = new NetworkProfilesImpl(this);
        }
        return this.networkProfiles;
    }
}
