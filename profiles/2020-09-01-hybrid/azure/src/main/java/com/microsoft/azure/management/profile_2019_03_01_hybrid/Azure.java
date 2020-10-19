/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.profile_2019_03_01_hybrid;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.arm.resources.AzureConfigurable;
import com.microsoft.azure.arm.resources.implementation.AzureConfigurableCoreImpl;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.appservice.v2016_03_01.BillingMeters;
import com.microsoft.azure.management.appservice.v2018_02_01.AppServiceCertificateOrders;
import com.microsoft.azure.management.appservice.v2018_02_01.AppServiceEnvironments;
import com.microsoft.azure.management.appservice.v2018_02_01.CertificateRegistrationProviders;
import com.microsoft.azure.management.appservice.v2018_02_01.Certificates;
import com.microsoft.azure.management.appservice.v2018_02_01.DeletedWebApps;
import com.microsoft.azure.management.appservice.v2018_02_01.Diagnostics;
import com.microsoft.azure.management.appservice.v2018_02_01.DomainRegistrationProviders;
import com.microsoft.azure.management.appservice.v2018_02_01.Domains;
import com.microsoft.azure.management.appservice.v2018_02_01.Recommendations;
import com.microsoft.azure.management.appservice.v2018_02_01.ResourceHealthMetadatas;
import com.microsoft.azure.management.appservice.v2018_02_01.TopLevelDomains;
import com.microsoft.azure.management.appservice.v2018_02_01.WebApps;
import com.microsoft.azure.management.authorization.v2015_07_01.RoleAssignments;
import com.microsoft.azure.management.authorization.v2015_07_01.RoleDefinitions;
import com.microsoft.azure.management.compute.v2017_03_30.Disks;
import com.microsoft.azure.management.compute.v2017_12_01.Images;
import com.microsoft.azure.management.compute.v2017_03_30.ResourceSkus;
import com.microsoft.azure.management.compute.v2017_03_30.Snapshots;
import com.microsoft.azure.management.compute.v2017_12_01.LogAnalytics;
import com.microsoft.azure.management.compute.v2017_12_01.Usages;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineExtensionImages;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineExtensions;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineImages;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineRunCommands;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineScaleSetExtensions;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineScaleSetRollingUpgrades;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineScaleSetVMs;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineScaleSets;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachineSizes;
import com.microsoft.azure.management.compute.v2017_12_01.VirtualMachines;
import com.microsoft.azure.management.compute.v2017_12_01.AvailabilitySets;
import com.microsoft.azure.management.dns.v2016_04_01.RecordSets;
import com.microsoft.azure.management.dns.v2016_04_01.Zones;
import com.microsoft.azure.management.keyvault.v2016_10_01.Vaults;
import com.microsoft.azure.management.locks.v2016_09_01.AuthorizationOperations;
import com.microsoft.azure.management.locks.v2016_09_01.ManagementLocks;
import com.microsoft.azure.management.monitor.v2015_04_01.ActivityLogs;
import com.microsoft.azure.management.monitor.v2015_04_01.AutoscaleSettings;
import com.microsoft.azure.management.monitor.v2015_04_01.EventCategories;
import com.microsoft.azure.management.monitor.v2015_04_01.TenantActivityLogs;
import com.microsoft.azure.management.monitor.v2017_05_01_preview.DiagnosticSettings;
import com.microsoft.azure.management.monitor.v2017_05_01_preview.DiagnosticSettingsCategorys;
import com.microsoft.azure.management.monitor.v2018_01_01.MetricDefinitions;
import com.microsoft.azure.management.monitor.v2018_01_01.Metrics;
import com.microsoft.azure.management.network.v2017_10_01.ApplicationGateways;
import com.microsoft.azure.management.network.v2017_10_01.ApplicationSecurityGroups;
import com.microsoft.azure.management.network.v2017_10_01.AvailableEndpointServices;
import com.microsoft.azure.management.network.v2017_10_01.BgpServiceCommunities;
import com.microsoft.azure.management.network.v2017_10_01.ConnectionMonitors;
import com.microsoft.azure.management.network.v2017_10_01.DefaultSecurityRules;
import com.microsoft.azure.management.network.v2017_10_01.ExpressRouteCircuitAuthorizations;
import com.microsoft.azure.management.network.v2017_10_01.ExpressRouteCircuitPeerings;
import com.microsoft.azure.management.network.v2017_10_01.ExpressRouteCircuits;
import com.microsoft.azure.management.network.v2017_10_01.ExpressRouteServiceProviders;
import com.microsoft.azure.management.network.v2017_10_01.InboundNatRules;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancerBackendAddressPools;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancerFrontendIPConfigurations;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancerLoadBalancingRules;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancerNetworkInterfaces;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancerProbes;
import com.microsoft.azure.management.network.v2017_10_01.LoadBalancers;
import com.microsoft.azure.management.network.v2017_10_01.LocalNetworkGateways;
import com.microsoft.azure.management.network.v2017_10_01.NetworkInterfaceIPConfigurations;
import com.microsoft.azure.management.network.v2017_10_01.NetworkInterfaceLoadBalancers;
import com.microsoft.azure.management.network.v2017_10_01.NetworkInterfaces;
import com.microsoft.azure.management.network.v2017_10_01.NetworkSecurityGroups;
import com.microsoft.azure.management.network.v2017_10_01.NetworkWatchers;
import com.microsoft.azure.management.network.v2017_10_01.Operations;
import com.microsoft.azure.management.network.v2017_10_01.PacketCaptures;
import com.microsoft.azure.management.network.v2017_10_01.PublicIPAddresses;
import com.microsoft.azure.management.network.v2017_10_01.RouteFilterRules;
import com.microsoft.azure.management.network.v2017_10_01.RouteFilters;
import com.microsoft.azure.management.network.v2017_10_01.RouteTables;
import com.microsoft.azure.management.network.v2017_10_01.Routes;
import com.microsoft.azure.management.network.v2017_10_01.SecurityRules;
import com.microsoft.azure.management.network.v2017_10_01.Subnets;
import com.microsoft.azure.management.network.v2017_10_01.VirtualNetworkGatewayConnections;
import com.microsoft.azure.management.network.v2017_10_01.VirtualNetworkGateways;
import com.microsoft.azure.management.network.v2017_10_01.VirtualNetworkPeerings;
import com.microsoft.azure.management.network.v2017_10_01.VirtualNetworks;
import com.microsoft.azure.management.policy.v2016_12_01.PolicyAssignments;
import com.microsoft.azure.management.policy.v2016_12_01.PolicyDefinitions;
import com.microsoft.azure.management.resources.v2016_06_01.Subscription;
import com.microsoft.azure.management.resources.v2016_06_01.Subscriptions;
import com.microsoft.azure.management.resources.v2016_06_01.Tenants;
import com.microsoft.azure.management.resources.v2018_05_01.DeploymentOperations;
import com.microsoft.azure.management.resources.v2018_05_01.Deployments;
import com.microsoft.azure.management.resources.v2018_05_01.Providers;
import com.microsoft.azure.management.resources.v2018_05_01.ResourceGroups;
import com.microsoft.azure.management.resources.v2018_05_01.Resources;
import com.microsoft.azure.management.resources.v2018_05_01.Tags;
import com.microsoft.azure.management.storage.v2017_10_01.Skus;
import com.microsoft.azure.management.storage.v2017_10_01.StorageAccounts;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

import java.io.IOException;

/**
 * Entry point to Azure ContainerService resource management.
 */
public final class Azure {
    private com.microsoft.azure.management.compute.v2017_03_30.implementation.ComputeManager computeManager20170330;
    private com.microsoft.azure.management.compute.v2017_12_01.implementation.ComputeManager computeManager20171201;
    private com.microsoft.azure.management.resources.v2018_05_01.implementation.ResourcesManager resourceManager20180501;
    private com.microsoft.azure.management.policy.v2016_12_01.implementation.PolicyManager authorizationManager20161201;
    private com.microsoft.azure.management.monitor.v2018_01_01.implementation.MonitorManager monitorManager20180101;
    private com.microsoft.azure.management.monitor.v2017_05_01_preview.implementation.MonitorManager monitorManager20170501preview;
    private com.microsoft.azure.management.monitor.v2015_04_01.implementation.MonitorManager monitorManager20150401;
    private com.microsoft.azure.management.locks.v2016_09_01.implementation.LocksManager locksManager20160901;
    private com.microsoft.azure.management.network.v2017_10_01.implementation.NetworkManager networkManager20171001;
    private com.microsoft.azure.management.dns.v2016_04_01.implementation.NetworkManager dnsManager20160401;
    private com.microsoft.azure.management.storage.v2017_10_01.implementation.StorageManager storageManager20171001;
    private com.microsoft.azure.management.keyvault.v2016_10_01.implementation.KeyVaultManager keyVaultManager20161001;
    private com.microsoft.azure.management.appservice.v2016_03_01.implementation.AppServiceManager appServiceManager20160301;
    private com.microsoft.azure.management.appservice.v2016_09_01.implementation.AppServiceManager appServiceManager20160901;
    private com.microsoft.azure.management.appservice.v2018_02_01.implementation.AppServiceManager appServiceManager20180201;
    private final String subscriptionId;
    private final Authenticated authenticated;

    /**
     * Get a Configurable instance that can be used to create Azure with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new Azure.ConfigurableImpl();
    }

    /**
     * Creates an instance of Azure.Authenticated that exposes subscription, tenant, and authorization API entry points.
     *
     * @param credentials the credentials to use
     * @return the Azure.Authenticated
     */
    public static Authenticated authenticate(AzureTokenCredentials credentials) {
        return new AuthenticatedImpl(ConfigurableImpl.createRestClientBuilderWithAllCipherSuites()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .build());
    }

    /**
     * Creates an instance of Azure that exposes ContainerService resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @return the Azure.Authenticated
     */
    public static Authenticated authenticate(RestClient restClient) {
        return new AuthenticatedImpl(restClient);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of Azure that exposes ContainerService management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing ContainerService management API entry points that work across subscriptions
         */
        Azure authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * Provides authenticated access to a subset of Azure APIs that do not require a specific subscription.
     * <p>
     * To access the subscription-specific APIs, use {@link Authenticated#withSubscription(String)},
     * or withDefaultSubscription() if a default subscription has already been previously specified
     * (for example, in a previously specified authentication file).
     */
    public interface Authenticated {
        /**
         * Entry point to subscription management APIs.
         *
         * @return Subscriptions interface providing access to subscription management
         */
        Subscriptions subscriptions();

        /**
         * Entry point to tenant management APIs.
         *
         * @return Tenants interface providing access to tenant management
         */
        Tenants tenants();

        /**
         * Selects a specific subscription for the APIs to work with.
         * <p>
         * Most Azure APIs require a specific subscription to be selected.
         * @param subscriptionId the ID of the subscription
         * @return an authenticated Azure client configured to work with the specified subscription
         */
        Azure withSubscription(String subscriptionId);

        /**
         * Selects the default subscription as the subscription for the APIs to work with.
         * <p>
         * The default subscription can be specified inside the authentication file using {@link Azure#authenticate(AzureTokenCredentials)}.
         * If no default subscription has been previously provided, the first subscription as
         * returned by {@link Authenticated#subscriptions()} will be selected.
         * @return an authenticated Azure client configured to work with the default subscription
         * @throws CloudException exception thrown from Azure
         * @throws IOException exception thrown from serialization/deserialization
         */
        Azure withDefaultSubscription() throws CloudException, IOException;

        /**
         * Entry point to role definition management APIs.
         *
         * @return RoleDefinitions interface providing access to tenant management
         */
        RoleDefinitions roleDefinitions();

        /**
         * Entry point to role assignment management APIs.
         *
         * @return RoleAssignments interface providing access to tenant management
         */
        RoleAssignments roleAssignments();
    }

    /**
     * The implementation for the Authenticated interface.
     */
    private static final class AuthenticatedImpl implements Authenticated {
        private final RestClient restClient;
        private final com.microsoft.azure.management.resources.v2016_06_01.implementation.Manager subscriptionManager20160601;
        private final com.microsoft.azure.management.authorization.v2015_07_01.implementation.AuthorizationManager authorizationManager20150701;
        private String defaultSubscription;

        private AuthenticatedImpl(RestClient restClient) {
            this.subscriptionManager20160601 = com.microsoft.azure.management.resources.v2016_06_01.implementation.Manager.authenticate(restClient);
            this.authorizationManager20150701 = com.microsoft.azure.management.authorization.v2015_07_01.implementation.AuthorizationManager.authenticate(restClient, null);
            this.restClient = restClient;
        }

        private AuthenticatedImpl withDefaultSubscription(String subscriptionId) {
            this.defaultSubscription = subscriptionId;
            return this;
        }

        @Override
        public Subscriptions subscriptions() {
            return subscriptionManager20160601.subscriptions();
        }

        @Override
        public Tenants tenants() {
            return subscriptionManager20160601.tenants();
        }

        @Override
        public RoleDefinitions roleDefinitions() {
            return authorizationManager20150701.roleDefinitions();
        }

        @Override
        public RoleAssignments roleAssignments() {
            return authorizationManager20150701.roleAssignments();
        }

        @Override
        public Azure withSubscription(String subscriptionId) {
            return new Azure(restClient, subscriptionId, this);
        }

        @Override
        public Azure withDefaultSubscription() throws CloudException, IOException {
            if (this.defaultSubscription != null) {
                return withSubscription(this.defaultSubscription);
            } else {
                PagedList<Subscription> subs = this.subscriptions().list();
                if (!subs.isEmpty()) {
                    return withSubscription(subs.get(0).subscriptionId());
                } else {
                    return withSubscription(null);
                }
            }
        }
    }

    /**
     * @return Entry point to manage PolicyDefinitions.
     */
    public PolicyDefinitions policyDefinitions() {
        return this.authorizationManager20161201.policyDefinitions();
    }

    /**
     * @return Entry point to manage PolicyAssignments.
     */
    public PolicyAssignments policyAssignments() {
        return this.authorizationManager20161201.policyAssignments();
    }

    /**
     * @return Entry point to manage Deployments.
     */
    public Deployments deployments() {
        return this.resourceManager20180501.deployments();
    }

    /**
     * @return Entry point to manage Providers.
     */
    public Providers resourceProviders() {
        return this.resourceManager20180501.providers();
    }

    /**
     * @return Entry point to manage Resources.
     */
    public Resources resources() {
        return this.resourceManager20180501.resources();
    }

    /**
     * @return Entry point to manage ResourceGroups.
     */
    public ResourceGroups resourceGroups() {
        return this.resourceManager20180501.resourceGroups();
    }

    /**
     * @return Entry point to manage Tags.
     */
    public Tags tags() {
        return this.resourceManager20180501.tags();
    }

    /**
     * @return Entry point to manage DeploymentOperations.
     */
    public DeploymentOperations deploymentOperations() {
        return this.resourceManager20180501.deploymentOperations();
    }

    /**
     * @return Entry point to manage resource Operations.
     */
    public com.microsoft.azure.management.resources.v2018_05_01.Operations resourceOperations() { return this.resourceManager20180501.operations(); }

    /**
     * @return Entry point to manage AvailabilitySets.
     */
    public AvailabilitySets availabilitySets() {
        return this.computeManager20171201.availabilitySets();
    }

    /**
     * @return Entry point to manage computer operations.
     */
    public com.microsoft.azure.management.compute.v2017_12_01.Operations computerOperations() { return this.computeManager20171201.operations(); }

    /**
     * @return Entry point to manage VirtualMachineExtensionImages.
     */
    public VirtualMachineExtensionImages virtualMachineExtensionImages() {
        return this.computeManager20171201.virtualMachineExtensionImages();
    }

    /**
     * @return Entry point to manage VirtualMachineExtensions.
     */
    public VirtualMachineExtensions virtualMachineExtensions() {
        return this.computeManager20171201.virtualMachineExtensions();
    }

    /**
     * @return Entry point to manage VirtualMachines.
     */
    public VirtualMachines virtualMachines() {
        return this.computeManager20171201.virtualMachines();
    }

    /**
     * @return Entry point to manage VirtualMachineImages.
     */
    public VirtualMachineImages virtualMachineImages() {
        return this.computeManager20171201.virtualMachineImages();
    }

    /**
     * @return Entry point to manage Usages.
     */
    public Usages ComputeUsages() {
        return this.computeManager20171201.usages();
    }

    /**
     * @return Entry point to manage VirtualMachineSizes.
     */
    public VirtualMachineSizes virtualMachineSizes() {
        return this.computeManager20171201.virtualMachineSizes();
    }

    /**
     * @return Entry point to manage Images.
     */
    public Images images() {
        return this.computeManager20171201.images();
    }

    /**
     * @return Entry point to manage ResourceSkus.
     */
    public ResourceSkus resourceSkus() {
        return this.computeManager20170330.resourceSkus();
    }

    /**
     * @return Entry point to manage VirtualMachineScaleSets.
     */
    public VirtualMachineScaleSets virtualMachineScaleSets() {
        return this.computeManager20171201.virtualMachineScaleSets();
    }

    /**
     * @return Entry point to manage VirtualMachineScaleSetExtensions.
     */
    public VirtualMachineScaleSetExtensions virtualMachineScaleSetExtensions() {
        return this.computeManager20171201.virtualMachineScaleSetExtensions();
    }

    /**
     * @return Entry point to manage VirtualMachineScaleSetRollingUpgrades.
     */
    public VirtualMachineScaleSetRollingUpgrades virtualMachineScaleSetRollingUpgrades() {
        return this.computeManager20171201.virtualMachineScaleSetRollingUpgrades();
    }

    /**
     * @return Entry point to manage VirtualMachineScaleSetVMs.
     */
    public VirtualMachineScaleSetVMs virtualMachineScaleSetVMs() {
        return this.computeManager20171201.virtualMachineScaleSetVMs();
    }

    /**
     * @return Entry point to manage Disks.
     */
    public Disks disks() {
        return this.computeManager20170330.disks();
    }

    /**
     * @return Entry point to manage Snapshots.
     */
    public Snapshots snapshots() {
        return this.computeManager20170330.snapshots();
    }

    /**
     * @return Entry point to manage VirtualMachineRunCommands.
     */
    public VirtualMachineRunCommands virtualMachineRunCommands() {
        return this.computeManager20171201.virtualMachineRunCommands();
    }

    /**
     * @return Entry point to manage EventCategories.
     */
    public EventCategories eventCategories() { return this.monitorManager20150401.eventCategories(); }

    /**
     * @return Entry point to manage monitor Operations.
     */
    public com.microsoft.azure.management.monitor.v2015_04_01.Operations monitorOperations() { return this.monitorManager20150401.operations(); }

    /**
     * @return Entry point to manage DiagnosticSettings.
     */
    public DiagnosticSettings diagnosticSettings() { return this.monitorManager20170501preview.diagnosticSettings(); }

    /**
     * @return Entry point to manage DiagnosticSettingsCategories.
     */
    public DiagnosticSettingsCategorys diagnosticSettingsCategories() { return this.monitorManager20170501preview.diagnosticSettingsCategorys(); }

    /**
     * @return Entry point to manage MetricDefinitions.
     */
    public MetricDefinitions metricDefinitions() { return this.monitorManager20180101.metricDefinitions(); }

    /**
     * @return Entry point to manage Metrics.
     */
    public Metrics metrics() { return this.monitorManager20180101.metrics(); }

    /**
     * @return Entry point to manage AuthorizationOperations.
     */
    public AuthorizationOperations authorizationOperations() { return this.locksManager20160901.authorizationOperations(); }

    /**
     * @return Entry point to manage ManagementLocks.
     */
    public ManagementLocks managementLocks() { return this.locksManager20160901.managementLocks(); }

    /**
     * @return Entry point to manage LoadBalancers.
     */
    public LoadBalancers loadBalancers() {
        return this.networkManager20171001.loadBalancers();
    }

    /**
     * @return Entry point to manage LoadBalancerBackendAddressPools.
     */
    public LoadBalancerBackendAddressPools loadBalancerBackendAddressPools() {
        return this.networkManager20171001.loadBalancerBackendAddressPools();
    }

    /**
     * @return Entry point to manage LoadBalancerFrontendIPConfigurations.
     */
    public LoadBalancerFrontendIPConfigurations loadBalancerFrontendIPConfigurations() {
        return this.networkManager20171001.loadBalancerFrontendIPConfigurations();
    }

    /**
     * @return Entry point to manage InboundNatRules.
     */
    public InboundNatRules inboundNatRules() {
        return this.networkManager20171001.inboundNatRules();
    }

    /**
     * @return Entry point to manage LoadBalancerLoadBalancingRules.
     */
    public LoadBalancerLoadBalancingRules loadBalancerLoadBalancingRules() {
        return this.networkManager20171001.loadBalancerLoadBalancingRules();
    }

    /**
     * @return Entry point to manage LoadBalancerNetworkInterfaces.
     */
    public LoadBalancerNetworkInterfaces loadBalancerNetworkInterfaces() {
        return this.networkManager20171001.loadBalancerNetworkInterfaces();
    }

    /**
     * @return Entry point to manage LoadBalancerProbes.
     */
    public LoadBalancerProbes loadBalancerProbes() {
        return this.networkManager20171001.loadBalancerProbes();
    }

    /**
     * @return Entry point to manage NetworkInterfaces.
     */
    public NetworkInterfaces networkInterfaces() {
        return this.networkManager20171001.networkInterfaces();
    }

    /**
     * @return Entry point to manage NetworkInterfaceIPConfigurations.
     */
    public NetworkInterfaceIPConfigurations networkInterfaceIPConfigurations() {
        return this.networkManager20171001.networkInterfaceIPConfigurations();
    }

    /**
     * @return Entry point to manage NetworkInterfaceLoadBalancers.
     */
    public NetworkInterfaceLoadBalancers networkInterfaceLoadBalancers() {
        return this.networkManager20171001.networkInterfaceLoadBalancers();
    }

    /**
     * @return Entry point to manage NetworkSecurityGroups.
     */
    public NetworkSecurityGroups networkSecurityGroups() {
        return this.networkManager20171001.networkSecurityGroups();
    }

    /**
     * @return Entry point to manage SecurityRules.
     */
    public SecurityRules securityRules() {
        return this.networkManager20171001.securityRules();
    }

    /**
     * @return Entry point to manage DefaultSecurityRules.
     */
    public DefaultSecurityRules defaultSecurityRules() {
        return this.networkManager20171001.defaultSecurityRules();
    }

    /**
     * @return Entry point to manage ConnectionMonitors.
     */
    public ConnectionMonitors connectionMonitors() {
        return this.networkManager20171001.connectionMonitors();
    }

    /**
     * @return Entry point to manage Operations.
     */
    public Operations networkOperations() {
        return this.networkManager20171001.operations();
    }

    /**
     * @return Entry point to manage PublicIPAddresses.
     */
    public PublicIPAddresses publicIPAddresses() {
        return this.networkManager20171001.publicIPAddresses();
    }

    /**
     * @return Entry point to manage RouteFilters.
     */
    public RouteFilters routeFilters() {
        return this.networkManager20171001.routeFilters();
    }

    /**
     * @return Entry point to manage RouteFilterRules.
     */
    public RouteFilterRules routeFilterRules() {
        return this.networkManager20171001.routeFilterRules();
    }

    /**
     * @return Entry point to manage RouteTables.
     */
    public RouteTables routeTables() {
        return this.networkManager20171001.routeTables();
    }

    /**
     * @return Entry point to manage Routes.
     */
    public Routes routes() {
        return this.networkManager20171001.routes();
    }

    /**
     * @return Entry point to manage Usages.
     */
    public com.microsoft.azure.management.network.v2017_10_01.Usages networkUsages() {
        return this.networkManager20171001.usages();
    }

    /**
     * @return Entry point to manage VirtualNetworks.
     */
    public VirtualNetworks virtualNetworks() {
        return this.networkManager20171001.virtualNetworks();
    }

    /**
     * @return Entry point to manage Subnets.
     */
    public Subnets subnets() {
        return this.networkManager20171001.subnets();
    }

    /**
     * @return Entry point to manage VirtualNetworkPeerings.
     */
    public VirtualNetworkPeerings virtualNetworkPeerings() {
        return this.networkManager20171001.virtualNetworkPeerings();
    }

    /**
     * @return Entry point to manage VirtualNetworkGateways.
     */
    public VirtualNetworkGateways virtualNetworkGateways() {
        return this.networkManager20171001.virtualNetworkGateways();
    }

    /**
     * @return Entry point to manage VirtualNetworkGatewayConnections.
     */
    public VirtualNetworkGatewayConnections virtualNetworkGatewayConnections() {
        return this.networkManager20171001.virtualNetworkGatewayConnections();
    }

    /**
     * @return Entry point to manage LocalNetworkGateways.
     */
    public LocalNetworkGateways localNetworkGateways() {
        return this.networkManager20171001.localNetworkGateways();
    }

    /**
     * @return Entry point to manage RecordSets.
     */
    public RecordSets recordSets() {
        return this.dnsManager20160401.recordSets();
    }

    /**
     * @return Entry point to manage Zones.
     */
    public Zones zones() {
        return this.dnsManager20160401.zones();
    }

    /**
     * @return Entry point to manage StorageAccounts.
     */
    public StorageAccounts storageAccounts() {
        return this.storageManager20171001.storageAccounts();
    }

    /**
     * @return Entry point to manage Usages.
     */
    public com.microsoft.azure.management.storage.v2017_10_01.Usages storageUsages() {
        return this.storageManager20171001.usages();
    }

    /**
     * @return Entry point to manage storage Operations.
     */
    public com.microsoft.azure.management.storage.v2017_10_01.Operations storageOperations() { return this.storageManager20171001.operations(); }

    /**
     * @return Entry point to manage storage Skus.
     */
    public Skus storageSkus() { return this.storageManager20171001.skus(); }

    /**
     * @return Entry point to manage BillingMeters.
     */
    public BillingMeters billingMeters() {
        return this.appServiceManager20160301.billingMeters();
    }

    /**
     * @return Entry point to manage AppServicePlans.
     */
    public com.microsoft.azure.management.appservice.v2016_09_01.AppServicePlans appServicePlans() { return this.appServiceManager20160901.appServicePlans(); }

    /**
     * @return Entry point to manage Certificates.
     */
    public Certificates certificates() { return this.appServiceManager20180201.certificates(); }

    /**
     * @return Entry point to manage DeletedWebApps.
     */
    public DeletedWebApps deletedWebApps() { return this.appServiceManager20180201.deletedWebApps(); }

    /**
     * @return Entry point to manage Diagnostics.
     */
    public Diagnostics diagnostics() { return this.appServiceManager20180201.diagnostics(); }

    /**
     * @return Entry point to manage AppService Providers.
     */
    public com.microsoft.azure.management.appservice.v2018_02_01.Providers appServiceProviders() { return this.appServiceManager20180201.providers(); }

    /**
     * @return Entry point to manage ResourceHealthMetadatas.
     */
    public ResourceHealthMetadatas resourceHealthMetadatas() { return this.appServiceManager20180201.resourceHealthMetadatas(); }

    /**
     * @return Entry point to manage WebApps.
     */
    public WebApps webApps() { return this.appServiceManager20180201.webApps(); }

    /**
     * @return Entry point to manage key vaults.
     */
    public Vaults keyVaults() {
        return this.keyVaultManager20161001.vaults();
    }

    /**
     * @return Entry point to manage KeyVault Operations.
     */
    public com.microsoft.azure.management.keyvault.v2016_10_01.Operations keyVaultOperations() { return this.keyVaultManager20161001.operations(); }


    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableCoreImpl<Configurable> implements Configurable {
        private static RestClient.Builder createRestClientBuilderWithAllCipherSuites() {
            return new RestClient.Builder().withCipherSuites(CustomCipherSuites.ALL_CIPHER_SUITES);
        }

        public ConfigurableImpl() {
            this.restClientBuilder = createRestClientBuilderWithAllCipherSuites()
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory());
        }

        public Azure authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return Azure.authenticate(buildRestClient(credentials)).withSubscription(subscriptionId);
        }
    }

    private Azure(RestClient restClient, String subscriptionId, Authenticated authenticated) {
        this.computeManager20170330 = com.microsoft.azure.management.compute.v2017_03_30.implementation.ComputeManager.authenticate(restClient, subscriptionId);
        this.computeManager20171201 = com.microsoft.azure.management.compute.v2017_12_01.implementation.ComputeManager.authenticate(restClient, subscriptionId);
        this.resourceManager20180501 = com.microsoft.azure.management.resources.v2018_05_01.implementation.ResourcesManager.authenticate(restClient, subscriptionId);
        this.authorizationManager20161201 = com.microsoft.azure.management.policy.v2016_12_01.implementation.PolicyManager.authenticate(restClient, subscriptionId);
        this.locksManager20160901 = com.microsoft.azure.management.locks.v2016_09_01.implementation.LocksManager.authenticate(restClient, subscriptionId);
        this.networkManager20171001 = com.microsoft.azure.management.network.v2017_10_01.implementation.NetworkManager.authenticate(restClient, subscriptionId);
        this.monitorManager20170501preview = com.microsoft.azure.management.monitor.v2017_05_01_preview.implementation.MonitorManager.authenticate(restClient);
        this.monitorManager20180101 = com.microsoft.azure.management.monitor.v2018_01_01.implementation.MonitorManager.authenticate(restClient);
        this.monitorManager20150401 = com.microsoft.azure.management.monitor.v2015_04_01.implementation.MonitorManager.authenticate(restClient, subscriptionId);
        this.dnsManager20160401 = com.microsoft.azure.management.dns.v2016_04_01.implementation.NetworkManager.authenticate(restClient, subscriptionId);
        this.storageManager20171001 = com.microsoft.azure.management.storage.v2017_10_01.implementation.StorageManager.authenticate(restClient, subscriptionId);
        this.appServiceManager20160301 = com.microsoft.azure.management.appservice.v2016_03_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        this.appServiceManager20160901 = com.microsoft.azure.management.appservice.v2016_09_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        this.appServiceManager20180201 = com.microsoft.azure.management.appservice.v2018_02_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        this.keyVaultManager20161001 = com.microsoft.azure.management.keyvault.v2016_10_01.implementation.KeyVaultManager.authenticate(restClient, subscriptionId);
        this.subscriptionId = subscriptionId;
        this.authenticated = authenticated;
    }
}
