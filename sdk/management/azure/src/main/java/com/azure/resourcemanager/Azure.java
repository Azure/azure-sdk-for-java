// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.appservice.AppServiceCertificateOrders;
import com.azure.resourcemanager.appservice.AppServiceCertificates;
import com.azure.resourcemanager.appservice.AppServiceDomains;
import com.azure.resourcemanager.appservice.AppServicePlans;
import com.azure.resourcemanager.appservice.FunctionApps;
import com.azure.resourcemanager.appservice.WebApps;
import com.azure.resourcemanager.appservice.implementation.AppServiceManager;
import com.azure.resourcemanager.compute.models.AvailabilitySets;
import com.azure.resourcemanager.compute.models.ComputeSkus;
import com.azure.resourcemanager.compute.models.ComputeUsages;
import com.azure.resourcemanager.compute.models.Disks;
import com.azure.resourcemanager.compute.models.Galleries;
import com.azure.resourcemanager.compute.models.GalleryImageVersions;
import com.azure.resourcemanager.compute.models.GalleryImages;
import com.azure.resourcemanager.compute.models.Snapshots;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImages;
import com.azure.resourcemanager.compute.models.VirtualMachineImages;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSets;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.containerregistry.Registries;
import com.azure.resourcemanager.containerregistry.RegistryTaskRuns;
import com.azure.resourcemanager.containerregistry.RegistryTasks;
import com.azure.resourcemanager.containerregistry.implementation.ContainerRegistryManager;
import com.azure.resourcemanager.containerservice.KubernetesClusters;
import com.azure.resourcemanager.containerservice.implementation.ContainerServiceManager;
import com.azure.resourcemanager.cosmos.CosmosDBAccounts;
import com.azure.resourcemanager.cosmos.implementation.CosmosDBManager;
import com.azure.resourcemanager.dns.DnsZones;
import com.azure.resourcemanager.dns.implementation.DnsZoneManager;
import com.azure.resourcemanager.authorization.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.ActiveDirectoryGroups;
import com.azure.resourcemanager.authorization.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.RoleAssignments;
import com.azure.resourcemanager.authorization.RoleDefinitions;
import com.azure.resourcemanager.authorization.ServicePrincipals;
import com.azure.resourcemanager.authorization.implementation.GraphRbacManager;
import com.azure.resourcemanager.keyvault.Vaults;
import com.azure.resourcemanager.keyvault.implementation.KeyVaultManager;
import com.azure.resourcemanager.monitor.ActionGroups;
import com.azure.resourcemanager.monitor.ActivityLogs;
import com.azure.resourcemanager.monitor.AlertRules;
import com.azure.resourcemanager.monitor.AutoscaleSettings;
import com.azure.resourcemanager.monitor.DiagnosticSettings;
import com.azure.resourcemanager.monitor.MetricDefinitions;
import com.azure.resourcemanager.monitor.implementation.MonitorManager;
import com.azure.resourcemanager.msi.Identities;
import com.azure.resourcemanager.msi.implementation.MSIManager;
import com.azure.resourcemanager.network.ApplicationGateways;
import com.azure.resourcemanager.network.ApplicationSecurityGroups;
import com.azure.resourcemanager.network.DdosProtectionPlans;
import com.azure.resourcemanager.network.ExpressRouteCircuits;
import com.azure.resourcemanager.network.ExpressRouteCrossConnections;
import com.azure.resourcemanager.network.LoadBalancers;
import com.azure.resourcemanager.network.LocalNetworkGateways;
import com.azure.resourcemanager.network.NetworkInterfaces;
import com.azure.resourcemanager.network.NetworkSecurityGroups;
import com.azure.resourcemanager.network.NetworkUsages;
import com.azure.resourcemanager.network.NetworkWatchers;
import com.azure.resourcemanager.network.Networks;
import com.azure.resourcemanager.network.PublicIpAddresses;
import com.azure.resourcemanager.network.RouteFilters;
import com.azure.resourcemanager.network.RouteTables;
import com.azure.resourcemanager.network.VirtualNetworkGateways;
import com.azure.resourcemanager.network.implementation.NetworkManager;
import com.azure.resourcemanager.resources.models.Deployments;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.PolicyAssignments;
import com.azure.resourcemanager.resources.models.PolicyDefinitions;
import com.azure.resourcemanager.resources.models.Providers;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.azure.resourcemanager.resources.models.Tenants;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.sql.SqlServers;
import com.azure.resourcemanager.sql.implementation.SqlServerManager;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.resourcemanager.storage.models.StorageSkus;
import com.azure.resourcemanager.storage.models.Usages;
import com.azure.resourcemanager.storage.StorageManager;

import java.util.Objects;

/** The entry point for accessing resource management APIs in Azure. */
public final class Azure {
    private final ResourceManager resourceManager;
    private final StorageManager storageManager;
    private final ComputeManager computeManager;
    private final NetworkManager networkManager;
    private final KeyVaultManager keyVaultManager;
    //    private final BatchManager batchManager;
    //    private final TrafficManager trafficManager;
    //    private final RedisManager redisManager;
    //    private final CdnManager cdnManager;
    private final DnsZoneManager dnsZoneManager;
    private final AppServiceManager appServiceManager;
    private final SqlServerManager sqlServerManager;
    //    private final ServiceBusManager serviceBusManager;
    //    private final ContainerInstanceManager containerInstanceManager;
    private final ContainerRegistryManager containerRegistryManager;
    private final ContainerServiceManager containerServiceManager;
    //    private final SearchServiceManager searchServiceManager;
    private final CosmosDBManager cosmosDBManager;
    //    private final AuthorizationManager authorizationManager;
    private final MSIManager msiManager;
    private final MonitorManager monitorManager;
    //    private final EventHubManager eventHubManager;
    private final String subscriptionId;
    private final Authenticated authenticated;
    private final SdkContext sdkContext;

    /**
     * Authenticate to Azure using an Azure credential object.
     *
     * @param credential the credential object
     * @param profile the profile to use
     * @return the authenticated Azure client
     */
    public static Authenticated authenticate(TokenCredential credential, AzureProfile profile) {
        return new AuthenticatedImpl(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Authenticates API access using a RestClient instance.
     *
     * @param httpPipeline the HttpPipeline configured with Azure authentication credential
     * @param profile the profile used in Active Directory
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new AuthenticatedImpl(httpPipeline, profile);
    }

    /** @return an interface allow configurations on the client. */
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
            return Azure.authenticate(buildHttpPipeline(credential, profile), profile);
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

        /** @return the sdk context in authenticated */
        SdkContext sdkContext();

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
         * Specifies sdk context for azure.
         *
         * @param sdkContext the sdk context
         * @return the authenticated itself for chaining
         */
        Authenticated withSdkContext(SdkContext sdkContext);

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
        Azure withSubscription(String subscriptionId);

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
        Azure withDefaultSubscription();
    }

    /** The implementation for the Authenticated interface. */
    private static final class AuthenticatedImpl implements Authenticated {
        private final HttpPipeline httpPipeline;
        private final ResourceManager.Authenticated resourceManagerAuthenticated;
        private final GraphRbacManager graphRbacManager;
        private SdkContext sdkContext;
        private String tenantId;
        private String subscriptionId;
        private final AzureEnvironment environment;

        private AuthenticatedImpl(HttpPipeline httpPipeline, AzureProfile profile) {
            this.resourceManagerAuthenticated = ResourceManager.authenticate(httpPipeline, profile);
            this.graphRbacManager = GraphRbacManager.authenticate(httpPipeline, profile);
            this.httpPipeline = httpPipeline;
            this.tenantId = profile.tenantId();
            this.subscriptionId = profile.subscriptionId();
            this.environment = profile.environment();
            this.sdkContext = new SdkContext();
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
            return graphRbacManager.users();
        }

        @Override
        public ActiveDirectoryGroups activeDirectoryGroups() {
            return graphRbacManager.groups();
        }

        @Override
        public ServicePrincipals servicePrincipals() {
            return graphRbacManager.servicePrincipals();
        }

        @Override
        public ActiveDirectoryApplications activeDirectoryApplications() {
            return graphRbacManager.applications();
        }

        @Override
        public RoleDefinitions roleDefinitions() {
            return graphRbacManager.roleDefinitions();
        }

        @Override
        public RoleAssignments roleAssignments() {
            return graphRbacManager.roleAssignments();
        }

        @Override
        public Authenticated withSdkContext(SdkContext sdkContext) {
            this.sdkContext = sdkContext;
            return this;
        }

        @Override
        public SdkContext sdkContext() {
            return this.sdkContext;
        }

        @Override
        public Authenticated withTenantId(String tenantId) {
            Objects.requireNonNull(tenantId);
            this.tenantId = tenantId;
            return this;
        }

        @Override
        public Azure withSubscription(String subscriptionId) {
            return new Azure(httpPipeline, new AzureProfile(tenantId, subscriptionId, environment), this);
        }

        @Override
        public Azure withDefaultSubscription() {
            if (subscriptionId == null) {
                subscriptionId = Utils.defaultSubscription(this.subscriptions().list());
            }
            return new Azure(httpPipeline, new AzureProfile(tenantId, subscriptionId, environment), this);
        }
    }

    private Azure(HttpPipeline httpPipeline, AzureProfile profile, Authenticated authenticated) {
        this.sdkContext = authenticated.sdkContext();
        this.resourceManager =
            ResourceManager.authenticate(httpPipeline, profile).withSdkContext(sdkContext).withDefaultSubscription();
        this.storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
        this.computeManager = ComputeManager.authenticate(httpPipeline, profile, sdkContext);
        this.networkManager = NetworkManager.authenticate(httpPipeline, profile, sdkContext);
        this.keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile, sdkContext);
        //        this.batchManager = BatchManager.authenticate(restClient, subscriptionId, sdkContext);
        //        this.trafficManager = TrafficManager.authenticate(restClient, subscriptionId, sdkContext);
        //        this.redisManager = RedisManager.authenticate(restClient, subscriptionId, sdkContext);
        //        this.cdnManager = CdnManager.authenticate(restClient, subscriptionId, sdkContext);
        this.dnsZoneManager = DnsZoneManager.authenticate(httpPipeline, profile, sdkContext);
        this.appServiceManager = AppServiceManager.authenticate(httpPipeline, profile, sdkContext);
        this.sqlServerManager = SqlServerManager.authenticate(httpPipeline, profile, sdkContext);
        //        this.serviceBusManager = ServiceBusManager.authenticate(restClient, subscriptionId, sdkContext);
        //        this.containerInstanceManager = ContainerInstanceManager.authenticate(restClient, subscriptionId,
        // sdkContext);
        this.containerRegistryManager = ContainerRegistryManager.authenticate(httpPipeline, profile, sdkContext);
        this.containerServiceManager = ContainerServiceManager.authenticate(httpPipeline, profile, sdkContext);
        this.cosmosDBManager = CosmosDBManager.authenticate(httpPipeline, profile, sdkContext);
        //        this.searchServiceManager = SearchServiceManager.authenticate(restClient, subscriptionId, sdkContext);
        //        this.authorizationManager = AuthorizationManager.authenticate(restClient, subscriptionId, sdkContext);
        this.msiManager = MSIManager.authenticate(httpPipeline, profile, sdkContext);
        this.monitorManager = MonitorManager.authenticate(httpPipeline, profile, sdkContext);
        //        this.eventHubManager = EventHubManager.authenticate(restClient, subscriptionId, sdkContext);
        this.subscriptionId = profile.subscriptionId();
        this.authenticated = authenticated;
    }

    /** @return the currently selected subscription ID this client is authenticated to work with */
    public SdkContext sdkContext() {
        return this.sdkContext;
    }

    /** @return the currently selected subscription ID this client is authenticated to work with */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /** @return the currently selected subscription this client is authenticated to work with */
    public Subscription getCurrentSubscription() {
        return this.subscriptions().getById(this.subscriptionId());
    }

    /** @return subscriptions that this authenticated client has access to */
    public Subscriptions subscriptions() {
        return this.authenticated.subscriptions();
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

    //    /**
    //     * @return entry point to managing management locks
    //     */
    //    public ManagementLocks managementLocks() {
    //        return this.authorizationManager.managementLocks();
    //    }
    //
    //    /**
    //     * @return entry point to managing features
    //     */
    //    public Features features() {
    //        return resourceManager.features();
    //    }

    /** @return entry point to managing resource providers */
    public Providers providers() {
        return resourceManager.providers();
    }

    /**
     * @return entry point to managing policy definitions.
     */
    public PolicyDefinitions policyDefinitions() {
        return resourceManager.policyDefinitions();
    }

    /**
     * @return entry point to managing policy assignments.
     */
    public PolicyAssignments policyAssignments() {
        return resourceManager.policyAssignments();
    }

    /** @return entry point to managing storage accounts */
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

    /** @return entry point to managing virtual machines */
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

    //    /**
    //     * @return entry point to managing public IP prefixes
    //     */
    //    public PublicIPPrefixes publicIPPrefixes() {
    //        return this.networkManager.publicIPPrefixes();
    //    }

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

    //    /**
    //     * @return entry point to managing traffic manager profiles.
    //     */
    //    public TrafficManagerProfiles trafficManagerProfiles() {
    //        return trafficManager.profiles();
    //    }
    //
    //    /**
    //     * @return entry point to managing Redis Caches.
    //     */
    //    public RedisCaches redisCaches() {
    //        return redisManager.redisCaches();
    //    }
    //
    //    /**
    //     * @return entry point to managing cdn manager profiles.
    //     */
    //    public CdnProfiles cdnProfiles() {
    //        return cdnManager.profiles();
    //    }

    /**
     * @return entry point to managing DNS zones.
     */
    public DnsZones dnsZones() {
        return dnsZoneManager.zones();
    }

    /** @return entry point to managing web apps. */
    public WebApps webApps() {
        return appServiceManager.webApps();
    }

    /** @return entry point to managing function apps. */
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

    //    /**
    //     * @return entry point to managing Service Bus.
    //     */
    //    public ServiceBusNamespaces serviceBusNamespaces() {
    //        return serviceBusManager.namespaces();
    //    }

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

    //    /**
    //     * @return entry point to managing Azure Container Instances.
    //     */
    //    @Beta(SinceVersion.V1_3_0)
    //    public ContainerGroups containerGroups() {
    //        return containerInstanceManager.containerGroups();
    //    }

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
        return cosmosDBManager.databaseAccounts();
    }

    //    /**
    //     * @return entry point to managing Search services.
    //     */
    //    @Beta(SinceVersion.V1_2_0)
    //    public SearchServices searchServices() {
    //        return searchServiceManager.searchServices();
    //    }

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
    //
    //    /**
    //     * @return entry point to managing event hub namespaces.
    //     */
    //    @Beta(SinceVersion.V1_7_0)
    //    public EventHubNamespaces eventHubNamespaces() {
    //        return this.eventHubManager.namespaces();
    //    }
    //
    //    /**
    //     * @return entry point to managing event hubs.
    //     */
    //    @Beta(SinceVersion.V1_7_0)
    //    public EventHubs eventHubs() {
    //        return this.eventHubManager.eventHubs();
    //    }
    //
    //    /**
    //     * @return entry point to managing event hub namespace geo disaster recovery.
    //     */
    //    @Beta(SinceVersion.V1_7_0)
    //    public EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings() {
    //        return this.eventHubManager.eventHubDisasterRecoveryPairings();
    //    }

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

    /** @return the blob container management API entry point */
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
}
