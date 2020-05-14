// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.SubResource;
import com.azure.management.network.ApplicationGateway;
import com.azure.management.network.ApplicationGatewayBackend;
import com.azure.management.network.ApplicationGatewayBackendAddressPool;
import com.azure.management.network.ApplicationGateways;
import com.azure.management.network.ApplicationSecurityGroups;
import com.azure.management.network.DdosProtectionPlans;
import com.azure.management.network.ExpressRouteCircuits;
import com.azure.management.network.ExpressRouteCrossConnections;
import com.azure.management.network.LoadBalancers;
import com.azure.management.network.LocalNetworkGateways;
import com.azure.management.network.Network;
import com.azure.management.network.NetworkInterfaces;
import com.azure.management.network.NetworkSecurityGroups;
import com.azure.management.network.NetworkUsages;
import com.azure.management.network.NetworkWatchers;
import com.azure.management.network.Networks;
import com.azure.management.network.PublicIPAddresses;
import com.azure.management.network.RouteFilters;
import com.azure.management.network.RouteTables;
import com.azure.management.network.Subnet;
import com.azure.management.network.VirtualNetworkGateways;
import com.azure.management.network.models.NetworkManagementClientBuilder;
import com.azure.management.network.models.NetworkManagementClientImpl;
import com.azure.management.network.models.SubnetInner;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Entry point to Azure network management. */
public final class NetworkManager extends Manager<NetworkManager, NetworkManagementClientImpl> {

    // Collections
    private PublicIPAddresses publicIPAddresses;
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
    public static NetworkManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    public static NetworkManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new NetworkManager(httpPipeline, profile, sdkContext);
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

    private NetworkManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new NetworkManagementClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
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
    public PublicIPAddresses publicIPAddresses() {
        if (this.publicIPAddresses == null) {
            this.publicIPAddresses = new PublicIPAddressesImpl(this);
        }
        return this.publicIPAddresses;
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
            this.networkUsages = new NetworkUsagesImpl(super.innerManagementClient);
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

    // Internal utility function
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
                Network network = networks.get(networkId.toLowerCase(Locale.ROOT));
                if (network == null) {
                    network = this.networks().getById(networkId);
                    networks.put(networkId.toLowerCase(Locale.ROOT), network);
                }

                String subnetName = ResourceUtils.nameFromResourceId(subnetRef.id());
                subnets.add(network.subnets().get(subnetName));
            }
        }

        return Collections.unmodifiableList(subnets);
    }

    // Internal utility function
    Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends(
        List<ApplicationGatewayBackendAddressPool> backendRefs) {
        final Map<String, ApplicationGateway> appGateways = new HashMap<>();
        final List<ApplicationGatewayBackend> backends = new ArrayList<>();

        if (backendRefs != null) {
            for (ApplicationGatewayBackendAddressPool backendRef : backendRefs) {
                String appGatewayId = ResourceUtils.parentResourceIdFromResourceId(backendRef.id());
                ApplicationGateway appGateway = appGateways.get(appGatewayId.toLowerCase(Locale.ROOT));
                if (appGateway == null) {
                    appGateway = this.applicationGateways().getById(appGatewayId);
                    appGateways.put(appGatewayId.toLowerCase(Locale.ROOT), appGateway);
                }

                String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
                backends.add(appGateway.backends().get(backendName));
            }
        }

        return Collections.unmodifiableCollection(backends);
    }
}
