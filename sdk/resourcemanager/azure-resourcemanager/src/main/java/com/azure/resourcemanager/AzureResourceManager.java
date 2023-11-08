// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.models.SpringServices;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrders;
import com.azure.resourcemanager.appservice.models.AppServiceCertificates;
import com.azure.resourcemanager.appservice.models.AppServiceDomains;
import com.azure.resourcemanager.appservice.models.AppServicePlans;
import com.azure.resourcemanager.appservice.models.FunctionApps;
import com.azure.resourcemanager.appservice.models.WebApps;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroups;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.models.RoleAssignments;
import com.azure.resourcemanager.authorization.models.RoleDefinitions;
import com.azure.resourcemanager.authorization.models.ServicePrincipals;
import com.azure.resourcemanager.cdn.CdnManager;
import com.azure.resourcemanager.cdn.models.CdnProfiles;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.AvailabilitySets;
import com.azure.resourcemanager.compute.models.ComputeSkus;
import com.azure.resourcemanager.compute.models.ComputeUsages;
import com.azure.resourcemanager.compute.models.DiskEncryptionSets;
import com.azure.resourcemanager.compute.models.Disks;
import com.azure.resourcemanager.compute.models.Galleries;
import com.azure.resourcemanager.compute.models.GalleryImageVersions;
import com.azure.resourcemanager.compute.models.GalleryImages;
import com.azure.resourcemanager.compute.models.Snapshots;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImages;
import com.azure.resourcemanager.compute.models.VirtualMachineImages;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.models.Registries;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRuns;
import com.azure.resourcemanager.containerregistry.models.RegistryTasks;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.cosmos.CosmosManager;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccounts;
import com.azure.resourcemanager.dns.DnsZoneManager;
import com.azure.resourcemanager.dns.models.DnsZones;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairings;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.models.ManagedHsms;
import com.azure.resourcemanager.keyvault.models.Vaults;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActionGroups;
import com.azure.resourcemanager.monitor.models.ActivityLogs;
import com.azure.resourcemanager.monitor.models.AlertRules;
import com.azure.resourcemanager.monitor.models.AutoscaleSettings;
import com.azure.resourcemanager.monitor.models.DiagnosticSettings;
import com.azure.resourcemanager.monitor.models.MetricDefinitions;
import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.msi.models.Identities;
import com.azure.resourcemanager.network.NetworkManager;
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
import com.azure.resourcemanager.network.models.PrivateEndpoints;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.resourcemanager.network.models.PublicIpPrefixes;
import com.azure.resourcemanager.network.models.RouteFilters;
import com.azure.resourcemanager.network.models.RouteTables;
import com.azure.resourcemanager.network.models.VirtualNetworkGateways;
import com.azure.resourcemanager.privatedns.PrivateDnsZoneManager;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZones;
import com.azure.resourcemanager.redis.RedisManager;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Deployments;
import com.azure.resourcemanager.resources.models.Features;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.ManagementLocks;
import com.azure.resourcemanager.resources.models.PolicyAssignments;
import com.azure.resourcemanager.resources.models.PolicyDefinitions;
import com.azure.resourcemanager.resources.models.Providers;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.azure.resourcemanager.resources.models.TagOperations;
import com.azure.resourcemanager.resources.models.Tenants;
import com.azure.resourcemanager.search.SearchServiceManager;
import com.azure.resourcemanager.search.models.SearchServices;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServers;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.resourcemanager.storage.models.StorageSkus;
import com.azure.resourcemanager.storage.models.Usages;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfiles;

import java.util.Objects;

/**
 * The entry point for accessing resource management APIs in Azure.
 *
 * <p><strong>Instantiating an Azure Client</strong></p>
 *
 * <!-- src_embed com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile -->
 * <pre>
 * AzureProfile profile = new AzureProfile&#40;tenantId, subscriptionId, AzureEnvironment.AZURE&#41;;
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;
 *     .authorityHost&#40;profile.getEnvironment&#40;&#41;.getActiveDirectoryEndpoint&#40;&#41;&#41;
 *     .build&#40;&#41;;
 * AzureResourceManager azure = AzureResourceManager
 *     .authenticate&#40;credential, profile&#41;
 *     .withDefaultSubscription&#40;&#41;;
 * </pre>
 * <!-- end com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile -->
 */
public final class AzureResourceManager {
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final ComputeManager computeManager;
    private final NetworkManager networkManager;
    private final KeyVaultManager keyVaultManager;
    private final TrafficManager trafficManager;
    private final RedisManager redisManager;
    private final CdnManager cdnManager;
    private final DnsZoneManager dnsZoneManager;
    private final AppServiceManager appServiceManager;
    private final SqlServerManager sqlServerManager;
    private final ServiceBusManager serviceBusManager;
    private final ContainerInstanceManager containerInstanceManager;
    private final ContainerRegistryManager containerRegistryManager;
    private final ContainerServiceManager containerServiceManager;
    private final SearchServiceManager searchServiceManager;
    private final CosmosManager cosmosManager;
    private final MsiManager msiManager;
    private final MonitorManager monitorManager;
    private final EventHubsManager eventHubsManager;
    private final AppPlatformManager appPlatformManager;
    private final PrivateDnsZoneManager privateDnsZoneManager;
    private final Authenticated authenticated;
    private final String subscriptionId;
    private final String tenantId;

    /**
     * Authenticate to Azure using an Azure credential object.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile -->
     * <pre>
     * AzureProfile profile = new AzureProfile&#40;tenantId, subscriptionId, AzureEnvironment.AZURE&#41;;
     * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;
     *     .authorityHost&#40;profile.getEnvironment&#40;&#41;.getActiveDirectoryEndpoint&#40;&#41;&#41;
     *     .build&#40;&#41;;
     * AzureResourceManager azure = AzureResourceManager
     *     .authenticate&#40;credential, profile&#41;
     *     .withDefaultSubscription&#40;&#41;;
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.authenticate#credential-profile -->
     *
     * @param credential the credential object
     * @param profile the profile to use
     * @return the authenticated Azure client
     */
    public static Authenticated authenticate(TokenCredential credential, AzureProfile profile) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return new AuthenticatedImpl(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Authenticates API access using a RestClient instance.
     *
     * @param httpPipeline the {@link HttpPipeline} configured with Azure authentication credential.
     * @param profile the profile used in Active Directory
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return new AuthenticatedImpl(httpPipeline, profile);
    }

    /**
     * Configures the Azure client.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.configure -->
     * <pre>
     * AzureResourceManager azure = AzureResourceManager
     *     .configure&#40;&#41;
     *     .withLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;
     *     .withPolicy&#40;customPolicy&#41;
     *     .withRetryPolicy&#40;customRetryPolicy&#41;
     *     .withHttpClient&#40;httpClient&#41;
     *     &#47;&#47;...
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.configure -->
     *
     * @return an interface allow configurations on the client.
     */
    public static Configurable configure() {
        return new ConfigurableImpl();
    }

    /** The interface allowing configurations to be made on the client. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Authenticates API access based on the provided credential.
         *
         * @param credential The credential to authenticate API access with
         * @param profile the profile to use
         * @return the authenticated Azure client
         */
        Authenticated authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for {@link Configurable}. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public Authenticated authenticate(TokenCredential credential, AzureProfile profile) {
            return AzureResourceManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    /**
     * Provides authenticated access to a subset of Azure APIs that do not require a specific subscription.
     *
     * <p>To access the subscription-specific APIs, use {@link Authenticated#withSubscription(String)}, or
     * withDefaultSubscription() if a default subscription has already been previously specified (for example, in a
     * previously specified authentication file).
     */
    public interface Authenticated extends AccessManagement {
        /** @return the currently selected tenant ID this client is authenticated to work with */
        String tenantId();

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
         * Specifies a specific tenant for azure.
         *
         * <p>Only Graph RBAC APIs require a tenant to be selected.</p>
         *
         * @param tenantId the ID of the tenant
         * @return the authenticated itself for chaining
         */
        Authenticated withTenantId(String tenantId);

        /**
         * Selects a specific subscription for the APIs to work with.
         *
         * <p>Most Azure APIs require a specific subscription to be selected.</p>
         *
         * @param subscriptionId the ID of the subscription
         * @return an authenticated Azure client configured to work with the specified subscription
         */
        AzureResourceManager withSubscription(String subscriptionId);

        /**
         * Selects the default subscription as the subscription for the APIs to work with.
         *
         * <p>The default subscription can be specified inside the Azure profile using {@link
         * AzureProfile}. If no default subscription provided, we will try to set the only
         * subscription if applicable returned by {@link Authenticated#subscriptions()}</p>
         *
         * @throws IllegalStateException when no subscription or more than one subscription found in the tenant.
         * @return an authenticated Azure client configured to work with the default subscription
         */
        AzureResourceManager withDefaultSubscription();
    }

    /** The implementation for the Authenticated interface. */
    private static final class AuthenticatedImpl implements Authenticated {
        private final HttpPipeline httpPipeline;
        private final ResourceManager.Authenticated resourceManagerAuthenticated;
        private AuthorizationManager authorizationManager;
        private String tenantId;
        private String subscriptionId;
        private final AzureEnvironment environment;

        private AuthenticatedImpl(HttpPipeline httpPipeline, AzureProfile profile) {
            this.resourceManagerAuthenticated = ResourceManager.authenticate(httpPipeline, profile);
            this.authorizationManager = AuthorizationManager.authenticate(httpPipeline, profile);
            this.httpPipeline = httpPipeline;
            this.tenantId = profile.getTenantId();
            this.subscriptionId = profile.getSubscriptionId();
            this.environment = profile.getEnvironment();
        }

        @Override
        public String tenantId() {
            return this.tenantId;
        }

        @Override
        public Subscriptions subscriptions() {
            return resourceManagerAuthenticated.subscriptions();
        }

        @Override
        public Tenants tenants() {
            return resourceManagerAuthenticated.tenants();
        }

        @Override
        public ActiveDirectoryUsers activeDirectoryUsers() {
            return authorizationManager.users();
        }

        @Override
        public ActiveDirectoryGroups activeDirectoryGroups() {
            return authorizationManager.groups();
        }

        @Override
        public ServicePrincipals servicePrincipals() {
            return authorizationManager.servicePrincipals();
        }

        @Override
        public ActiveDirectoryApplications activeDirectoryApplications() {
            return authorizationManager.applications();
        }

        @Override
        public RoleDefinitions roleDefinitions() {
            return authorizationManager.roleDefinitions();
        }

        @Override
        public RoleAssignments roleAssignments() {
            return authorizationManager.roleAssignments();
        }

        @Override
        public Authenticated withTenantId(String tenantId) {
            Objects.requireNonNull(tenantId);
            this.tenantId = tenantId;
            this.authorizationManager = AuthorizationManager.authenticate(
                this.httpPipeline, new AzureProfile(tenantId, subscriptionId, environment));
            return this;
        }

        @Override
        public AzureResourceManager withSubscription(String subscriptionId) {
            return new AzureResourceManager(
                httpPipeline, new AzureProfile(tenantId, subscriptionId, environment), this);
        }

        @Override
        public AzureResourceManager withDefaultSubscription() {
            if (subscriptionId == null) {
                subscriptionId = ResourceManagerUtils.getDefaultSubscription(this.subscriptions().list());
            }
            return new AzureResourceManager(
                httpPipeline, new AzureProfile(tenantId, subscriptionId, environment), this);
        }
    }

    private AzureResourceManager(HttpPipeline httpPipeline, AzureProfile profile) {
        this(httpPipeline, profile, AzureResourceManager.authenticate(httpPipeline, profile));
    }

    private AzureResourceManager(HttpPipeline httpPipeline, AzureProfile profile, Authenticated authenticated) {
        this.resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();
        this.storageManager = StorageManager.authenticate(httpPipeline, profile);
        this.computeManager = ComputeManager.authenticate(httpPipeline, profile);
        this.networkManager = NetworkManager.authenticate(httpPipeline, profile);
        this.keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile);
        this.trafficManager = TrafficManager.authenticate(httpPipeline, profile);
        this.redisManager = RedisManager.authenticate(httpPipeline, profile);
        this.cdnManager = CdnManager.authenticate(httpPipeline, profile);
        this.dnsZoneManager = DnsZoneManager.authenticate(httpPipeline, profile);
        this.appServiceManager = AppServiceManager.authenticate(httpPipeline, profile);
        this.sqlServerManager = SqlServerManager.authenticate(httpPipeline, profile);
        this.serviceBusManager = ServiceBusManager.authenticate(httpPipeline, profile);
        this.containerInstanceManager = ContainerInstanceManager.authenticate(httpPipeline, profile);
        this.containerRegistryManager = ContainerRegistryManager.authenticate(httpPipeline, profile);
        this.containerServiceManager = ContainerServiceManager.authenticate(httpPipeline, profile);
        this.cosmosManager = CosmosManager.authenticate(httpPipeline, profile);
        this.searchServiceManager = SearchServiceManager.authenticate(httpPipeline, profile);
        this.msiManager = MsiManager.authenticate(httpPipeline, profile);
        this.monitorManager = MonitorManager.authenticate(httpPipeline, profile);
        this.eventHubsManager = EventHubsManager.authenticate(httpPipeline, profile);
        this.appPlatformManager = AppPlatformManager.authenticate(httpPipeline, profile);
        this.privateDnsZoneManager = PrivateDnsZoneManager.authenticate(httpPipeline, profile);
        this.authenticated = authenticated;
        this.subscriptionId = profile.getSubscriptionId();
        this.tenantId = profile.getTenantId();
    }

    /** @return the currently selected subscription ID this client is authenticated to work with */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /** @return the currently selected tenant ID this client is authenticated to work with */
    public String tenantId() {
        return this.tenantId;
    }

    /** @return the currently selected subscription this client is authenticated to work with */
    public Subscription getCurrentSubscription() {
        return this.subscriptions().getById(this.subscriptionId());
    }

    /** @return entry point to managing subscriptions */
    public Subscriptions subscriptions() {
        return this.resourceManager.subscriptions();
    }

    /** @return entry point to managing tenants */
    public Tenants tenants() {
        return this.resourceManager.tenants();
    }

    /** @return entry point to managing resource groups */
    public ResourceGroups resourceGroups() {
        return this.resourceManager.resourceGroups();
    }

    /** @return entry point to managing deployments */
    public Deployments deployments() {
        return this.resourceManager.deployments();
    }

    /** @return entry point to managing generic resources */
    public GenericResources genericResources() {
        return resourceManager.genericResources();
    }

    /** @return entry point to managing features */
    public Features features() {
        return resourceManager.features();
    }

    /** @return entry point to managing resource providers */
    public Providers providers() {
        return resourceManager.providers();
    }

    /** @return entry point to managing policy definitions. */
    public PolicyDefinitions policyDefinitions() {
        return resourceManager.policyDefinitions();
    }

    /** @return entry point to managing policy assignments. */
    public PolicyAssignments policyAssignments() {
        return resourceManager.policyAssignments();
    }

    /** @return entry point to managing locks. */
    public ManagementLocks managementLocks() {
        return resourceManager.managementLocks();
    }

    /**
     * Entry point to managing storage accounts
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create an Azure Storage Account</p>
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.storageAccounts.createStorageAccount -->
     * <pre>
     * azure.storageAccounts&#40;&#41;.define&#40;&quot;&lt;storage-account-name&gt;&quot;&#41;
     *     .withRegion&#40;Region.US_EAST&#41;
     *     .withNewResourceGroup&#40;resourceGroupName&#41;
     *     .withSku&#40;StorageAccountSkuType.STANDARD_LRS&#41;
     *     .withGeneralPurposeAccountKindV2&#40;&#41;
     *     .withOnlyHttpsTraffic&#40;&#41;
     *     &#47;&#47;...
     *     .create&#40;&#41;;
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.storageAccounts.createStorageAccount -->
     *
     * @return entry point to managing storage accounts
     */
    public StorageAccounts storageAccounts() {
        return storageManager.storageAccounts();
    }

    /** @return entry point to managing storage account usages */
    public Usages storageUsages() {
        return storageManager.usages();
    }

    /** @return entry point to managing storage service SKUs */
    public StorageSkus storageSkus() {
        return storageManager.storageSkus();
    }

    /** @return entry point to managing availability sets */
    public AvailabilitySets availabilitySets() {
        return computeManager.availabilitySets();
    }

    /** @return entry point to managing virtual networks */
    public Networks networks() {
        return networkManager.networks();
    }

    /** @return entry point to managing route tables */
    public RouteTables routeTables() {
        return networkManager.routeTables();
    }

    /** @return entry point to managing load balancers */
    public LoadBalancers loadBalancers() {
        return networkManager.loadBalancers();
    }

    /** @return entry point to managing application gateways */
    public ApplicationGateways applicationGateways() {
        return networkManager.applicationGateways();
    }

    /** @return entry point to managing network security groups */
    public NetworkSecurityGroups networkSecurityGroups() {
        return networkManager.networkSecurityGroups();
    }

    /** @return entry point to managing network resource usages */
    public NetworkUsages networkUsages() {
        return networkManager.usages();
    }

    /** @return entry point to managing network watchers */
    public NetworkWatchers networkWatchers() {
        return networkManager.networkWatchers();
    }

    /** @return entry point to managing virtual network gateways */
    public VirtualNetworkGateways virtualNetworkGateways() {
        return networkManager.virtualNetworkGateways();
    }

    /** @return entry point to managing local network gateways */
    public LocalNetworkGateways localNetworkGateways() {
        return networkManager.localNetworkGateways();
    }

    /** @return entry point to managing express route circuits */
    public ExpressRouteCircuits expressRouteCircuits() {
        return networkManager.expressRouteCircuits();
    }

    /** @return entry point to managing express route cross connections */
    public ExpressRouteCrossConnections expressRouteCrossConnections() {
        return networkManager.expressRouteCrossConnections();
    }

    /** @return entry point to managing express route circuits */
    public ApplicationSecurityGroups applicationSecurityGroups() {
        return networkManager.applicationSecurityGroups();
    }

    /** @return entry point to managing route filters */
    public RouteFilters routeFilters() {
        return networkManager.routeFilters();
    }

    /** @return entry point to managing DDoS protection plans */
    public DdosProtectionPlans ddosProtectionPlans() {
        return networkManager.ddosProtectionPlans();
    }

    /**
     * Entry point to managing virtual machines.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a Virtual Machine instance.</p>
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.virtualMachines.createVirtualMachine -->
     * <pre>
     * VirtualMachine linuxVM = azure.virtualMachines&#40;&#41;
     *     .define&#40;linuxVMName&#41;
     *     .withRegion&#40;region&#41;
     *     .withNewResourceGroup&#40;resourceGroupName&#41;
     *     .withNewPrimaryNetwork&#40;&quot;10.0.0.0&#47;28&quot;&#41;
     *     .withPrimaryPrivateIPAddressDynamic&#40;&#41;
     *     .withoutPrimaryPublicIPAddress&#40;&#41;
     *     .withPopularLinuxImage&#40;KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2&#41;
     *     .withRootUsername&#40;userName&#41;
     *     .withSsh&#40;sshPublicKey&#41;
     *     .withNewDataDisk&#40;10&#41;
     *     .withExistingDataDisk&#40;dataDisk&#41;
     *     .withSize&#40;VirtualMachineSizeTypes.STANDARD_DS1_V2&#41;
     *     .create&#40;&#41;;
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.virtualMachines.createVirtualMachine -->
     *
     * <p>Restart Virtual Machine instance.</p>
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.virtualMachines.restartVirtualMachineAsync -->
     * <pre>
     * azure.virtualMachines&#40;&#41;.listByResourceGroupAsync&#40;resourceGroupName&#41;
     *     .flatMap&#40;VirtualMachine::restartAsync&#41;
     *     &#47;&#47;...
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.virtualMachines.restartVirtualMachineAsync -->
     *
     * @return entry point to managing virtual machines
     */
    public VirtualMachines virtualMachines() {
        return computeManager.virtualMachines();
    }

    /** @return entry point to managing virtual machine scale sets. */
    public VirtualMachineScaleSets virtualMachineScaleSets() {
        return computeManager.virtualMachineScaleSets();
    }

    /** @return entry point to managing virtual machine images */
    public VirtualMachineImages virtualMachineImages() {
        return computeManager.virtualMachineImages();
    }

    /** @return entry point to managing virtual machine custom images */
    public VirtualMachineCustomImages virtualMachineCustomImages() {
        return computeManager.virtualMachineCustomImages();
    }

    /** @return entry point to managing managed disks */
    public Disks disks() {
        return computeManager.disks();
    }

    /** @return entry point to managing managed snapshots */
    public Snapshots snapshots() {
        return computeManager.snapshots();
    }

    /** @return the compute service SKU management API entry point */
    public ComputeSkus computeSkus() {
        return computeManager.computeSkus();
    }

    /** @return entry point to managing public IP addresses */
    public PublicIpAddresses publicIpAddresses() {
        return this.networkManager.publicIpAddresses();
    }

    /** @return entry point to managing public IP prefixes */
    public PublicIpPrefixes publicIpPrefixes() {
        return this.networkManager.publicIpPrefixes();
    }

    /** @return entry point to managing network interfaces */
    public NetworkInterfaces networkInterfaces() {
        return this.networkManager.networkInterfaces();
    }

    /** @return entry point to managing compute resource usages */
    public ComputeUsages computeUsages() {
        return computeManager.usages();
    }

    /** @return entry point to managing key vaults */
    public Vaults vaults() {
        return this.keyVaultManager.vaults();
    }

    //    /**
    //     * @return entry point to managing batch accounts.
    //     */
    //    public BatchAccounts batchAccounts() {
    //        return batchManager.batchAccounts();
    //    }

    /**
     * @return entry point to managing traffic manager profiles.
     */
    public TrafficManagerProfiles trafficManagerProfiles() {
        return trafficManager.profiles();
    }

    /** @return entry point to managing Redis Caches. */
    public RedisCaches redisCaches() {
        return redisManager.redisCaches();
    }

    /**
     * @return entry point to managing cdn manager profiles.
     */
    public CdnProfiles cdnProfiles() {
        return cdnManager.profiles();
    }

    /** @return entry point to managing DNS zones. */
    public DnsZones dnsZones() {
        return dnsZoneManager.zones();
    }

    /** @return entry point to managing web apps. */
    public WebApps webApps() {
        return appServiceManager.webApps();
    }

    /**
     * Entry point to managing function apps.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create an Azure Function App</p>
     *
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.functionApps.createFunctionApp -->
     * <pre>
     * Creatable&lt;StorageAccount&gt; creatableStorageAccount = azure.storageAccounts&#40;&#41;
     *     .define&#40;&quot;&lt;storage-account-name&gt;&quot;&#41;
     *     .withRegion&#40;Region.US_EAST&#41;
     *     .withExistingResourceGroup&#40;resourceGroupName&#41;
     *     .withGeneralPurposeAccountKindV2&#40;&#41;
     *     .withSku&#40;StorageAccountSkuType.STANDARD_LRS&#41;;
     * Creatable&lt;AppServicePlan&gt; creatableAppServicePlan = azure.appServicePlans&#40;&#41;
     *     .define&#40;&quot;&lt;app-service-plan-name&gt;&quot;&#41;
     *     .withRegion&#40;Region.US_EAST&#41;
     *     .withExistingResourceGroup&#40;resourceGroupName&#41;
     *     .withPricingTier&#40;PricingTier.STANDARD_S1&#41;
     *     .withOperatingSystem&#40;OperatingSystem.LINUX&#41;;
     * FunctionApp linuxFunctionApp = azure.functionApps&#40;&#41;.define&#40;&quot;&lt;function-app-name&gt;&quot;&#41;
     *     .withRegion&#40;Region.US_EAST&#41;
     *     .withExistingResourceGroup&#40;resourceGroupName&#41;
     *     .withNewLinuxAppServicePlan&#40;creatableAppServicePlan&#41;
     *     .withBuiltInImage&#40;FunctionRuntimeStack.JAVA_8&#41;
     *     .withNewStorageAccount&#40;creatableStorageAccount&#41;
     *     .withHttpsOnly&#40;true&#41;
     *     .withAppSetting&#40;&quot;WEBSITE_RUN_FROM_PACKAGE&quot;, &quot;&lt;function-app-package-url&gt;&quot;&#41;
     *     .create&#40;&#41;;
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.functionApps.createFunctionApp -->
     *
     * @return entry point to managing function apps.
     */
    public FunctionApps functionApps() {
        return appServiceManager.functionApps();
    }

    /** @return entry point to managing app service plans. */
    public AppServicePlans appServicePlans() {
        return appServiceManager.appServicePlans();
    }

    /** @return entry point to managing domains. */
    public AppServiceDomains appServiceDomains() {
        return appServiceManager.domains();
    }

    /** @return entry point to managing certificates. */
    public AppServiceCertificates appServiceCertificates() {
        return appServiceManager.certificates();
    }

    /** @return entry point to managing certificates orders. */
    public AppServiceCertificateOrders appServiceCertificateOrders() {
        return appServiceManager.certificateOrders();
    }

    /** @return entry point to managing Sql server. */
    public SqlServers sqlServers() {
        return sqlServerManager.sqlServers();
    }

    /**
     * @return entry point to managing Service Bus.
     */
    public ServiceBusNamespaces serviceBusNamespaces() {
        return serviceBusManager.namespaces();
    }

    /** @return entry point to managing Service Bus operations. */
    // TODO: To be revisited in the future
    // @Beta(SinceVersion.V1_1_0)
    // public ServiceBusOperations serviceBusOperations() {
    //    return serviceBusManager.operations();
    // }

    /** @return entry point to managing Kubernetes clusters. */
    public KubernetesClusters kubernetesClusters() {
        return containerServiceManager.kubernetesClusters();
    }

    /** @return entry point to managing Azure Container Instances. */
    public ContainerGroups containerGroups() {
        return containerInstanceManager.containerGroups();
    }

    /** @return entry point to managing Container Registries. */
    public Registries containerRegistries() {
        return containerRegistryManager.containerRegistries();
    }

    /** @return entry point to managing Container Registry RegistryTasks. */
    public RegistryTasks containerRegistryTasks() {
        return containerRegistryManager.containerRegistryTasks();
    }

    /** @return entry point to managing Container Registry RegistryTask Runs. */
    public RegistryTaskRuns containerRegistryTaskRuns() {
        return containerRegistryManager.registryTaskRuns();
    }

    /** @return entry point to managing Container Regsitries. */
    public CosmosDBAccounts cosmosDBAccounts() {
        return cosmosManager.databaseAccounts();
    }

    /**
     * @return entry point to managing Search services.
     */
    public SearchServices searchServices() {
        return searchServiceManager.searchServices();
    }

    /** @return entry point to managing Managed Service Identity (MSI) identities. */
    public Identities identities() {
        return msiManager.identities();
    }

    /** @return entry point to authentication and authorization management in Azure */
    public AccessManagement accessManagement() {
        return this.authenticated;
    }

    /** @return entry point to listing activity log events in Azure */
    public ActivityLogs activityLogs() {
        return this.monitorManager.activityLogs();
    }

    /** @return entry point to listing metric definitions in Azure */
    public MetricDefinitions metricDefinitions() {
        return this.monitorManager.metricDefinitions();
    }

    /** @return entry point to listing diagnostic settings in Azure */
    public DiagnosticSettings diagnosticSettings() {
        return this.monitorManager.diagnosticSettings();
    }

    /** @return entry point to managing action groups in Azure */
    public ActionGroups actionGroups() {
        return this.monitorManager.actionGroups();
    }

    /** @return entry point to managing alertRules in Azure */
    public AlertRules alertRules() {
        return this.monitorManager.alertRules();
    }

    /** @return entry point to managing Autoscale Settings in Azure */
    public AutoscaleSettings autoscaleSettings() {
        return this.monitorManager.autoscaleSettings();
    }

    /**
     * @return entry point to managing event hub namespaces.
     */
    public EventHubNamespaces eventHubNamespaces() {
        return this.eventHubsManager.namespaces();
    }

    /**
     * @return entry point to managing event hubs.
     */
    public EventHubs eventHubs() {
        return this.eventHubsManager.eventHubs();
    }

    /**
     * @return entry point to managing event hub namespace geo disaster recovery.
     */
    public EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings() {
        return this.eventHubsManager.eventHubDisasterRecoveryPairings();
    }

    /** @return entry point to manage compute galleries. */
    public Galleries galleries() {
        return this.computeManager.galleries();
    }

    /** @return entry point to manage compute gallery images. */
    public GalleryImages galleryImages() {
        return this.computeManager.galleryImages();
    }

    /** @return entry point to manage compute gallery image versions. */
    public GalleryImageVersions galleryImageVersions() {
        return this.computeManager.galleryImageVersions();
    }

    /**
     * Entry point to blob container management API.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create a Blob Container</p>
     *
     * <!-- src_embed com.azure.resourcemanager.azureResourceManager.storageBlobContainers.createBlobContainer -->
     * <pre>
     * azure.storageBlobContainers&#40;&#41;
     *     .defineContainer&#40;&quot;container&quot;&#41;
     *     .withExistingStorageAccount&#40;storageAccount&#41;
     *     .withPublicAccess&#40;PublicAccess.NONE&#41;
     *     &#47;&#47;...
     *     .create&#40;&#41;;
     * </pre>
     * <!-- end com.azure.resourcemanager.azureResourceManager.storageBlobContainers.createBlobContainer -->
     *
     * @return the blob container management API entry point
     */
    public BlobContainers storageBlobContainers() {
        return this.storageManager.blobContainers();
    }

    /** @return the blob service management API entry point */
    public BlobServices storageBlobServices() {
        return this.storageManager.blobServices();
    }

    /** @return the blob service management API entry point */
    public ManagementPolicies storageManagementPolicies() {
        return this.storageManager.managementPolicies();
    }

    /** @return the spring service management API entry point */
    public SpringServices springServices() {
        return this.appPlatformManager.springServices();
    }

    /** @return the private DNS zone management API entry point */
    public PrivateDnsZones privateDnsZones() {
        return this.privateDnsZoneManager.privateZones();
    }

    /** @return entry point to private endpoints management */
    public PrivateEndpoints privateEndpoints() {
        return this.networkManager.privateEndpoints();
    }

    /** @return entry point to tag management management */
    public TagOperations tagOperations() {
        return this.resourceManager.tagOperations();
    }

    /** @return entry point to network profiles management */
    public NetworkProfiles networkProfiles() {
        return this.networkManager.networkProfiles();
    }

    /** @return entry point to disk encryption sets management */
    public DiskEncryptionSets diskEncryptionSets() {
        return this.computeManager.diskEncryptionSets();
    }

    /** @return entry point to Managed Hardware Security Module management */
    public ManagedHsms managedHsms() {
        return this.keyVaultManager.managedHsms();
    }
}
