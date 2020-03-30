/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.CloudException;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.appservice.WebApps;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.compute.AvailabilitySets;
import com.azure.management.compute.ComputeSkus;
import com.azure.management.compute.ComputeUsages;
import com.azure.management.compute.Disks;
import com.azure.management.compute.Galleries;
import com.azure.management.compute.GalleryImageVersions;
import com.azure.management.compute.GalleryImages;
import com.azure.management.compute.Snapshots;
import com.azure.management.compute.VirtualMachineCustomImages;
import com.azure.management.compute.VirtualMachineImages;
import com.azure.management.compute.VirtualMachineScaleSets;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.compute.implementation.ComputeManager;
import com.azure.management.containerregistry.Registries;
import com.azure.management.containerregistry.RegistryTaskRuns;
import com.azure.management.containerregistry.RegistryTasks;
import com.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.azure.management.containerservice.ContainerServices;
import com.azure.management.containerservice.KubernetesClusters;
import com.azure.management.containerservice.implementation.ContainerServiceManager;
import com.azure.management.cosmosdb.CosmosDBAccounts;
import com.azure.management.cosmosdb.implementation.CosmosDBManager;
import com.azure.management.graphrbac.ActiveDirectoryApplications;
import com.azure.management.graphrbac.ActiveDirectoryGroups;
import com.azure.management.graphrbac.ActiveDirectoryUsers;
import com.azure.management.graphrbac.RoleAssignments;
import com.azure.management.graphrbac.RoleDefinitions;
import com.azure.management.graphrbac.ServicePrincipals;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.Vaults;
import com.azure.management.keyvault.implementation.KeyVaultManager;
import com.azure.management.monitor.ActionGroups;
import com.azure.management.monitor.ActivityLogs;
import com.azure.management.monitor.AlertRules;
import com.azure.management.monitor.AutoscaleSettings;
import com.azure.management.monitor.DiagnosticSettings;
import com.azure.management.monitor.MetricDefinitions;
import com.azure.management.monitor.implementation.MonitorManager;
import com.azure.management.msi.Identities;
import com.azure.management.msi.implementation.MSIManager;
import com.azure.management.network.ApplicationGateways;
import com.azure.management.network.ApplicationSecurityGroups;
import com.azure.management.network.DdosProtectionPlans;
import com.azure.management.network.ExpressRouteCircuits;
import com.azure.management.network.ExpressRouteCrossConnections;
import com.azure.management.network.LoadBalancers;
import com.azure.management.network.LocalNetworkGateways;
import com.azure.management.network.NetworkInterfaces;
import com.azure.management.network.NetworkSecurityGroups;
import com.azure.management.network.NetworkUsages;
import com.azure.management.network.NetworkWatchers;
import com.azure.management.network.Networks;
import com.azure.management.network.PublicIPAddresses;
import com.azure.management.network.RouteFilters;
import com.azure.management.network.RouteTables;
import com.azure.management.network.VirtualNetworkGateways;
import com.azure.management.network.implementation.NetworkManager;
import com.azure.management.resources.Deployments;
import com.azure.management.resources.GenericResources;
import com.azure.management.resources.Providers;
import com.azure.management.resources.ResourceGroups;
import com.azure.management.resources.Subscription;
import com.azure.management.resources.Subscriptions;
import com.azure.management.resources.Tenants;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.implementation.ResourceManager;
import com.azure.management.sql.SqlServers;
import com.azure.management.sql.implementation.SqlServerManager;
import com.azure.management.storage.BlobContainers;
import com.azure.management.storage.BlobServices;
import com.azure.management.storage.ManagementPolicies;
import com.azure.management.storage.StorageAccounts;
import com.azure.management.storage.StorageSkus;
import com.azure.management.storage.Usages;
import com.azure.management.storage.implementation.StorageManager;

import java.io.File;
import java.io.IOException;

/**
 * The entry point for accessing resource management APIs in Azure.
 */
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
//    private final DnsZoneManager dnsZoneManager;
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
     * Authenticate to Azure using an Azure credentials object.
     *
     * @param credential the credentials object
     * @return the authenticated Azure client
     */
    public static Authenticated authenticate(AzureTokenCredential credential) {
        return new AuthenticatedImpl(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain());
    }

    /**
     * Authenticates API access using a properties file containing the required credentials.
     *
     * @param credentialsFile the file containing the credentials in the standard Java properties file format,
     *                        with the following keys:<p>
     *                        <code>
     *                        subscription= #subscription ID<br>
     *                        tenant= #tenant ID<br>
     *                        client= #client id<br>
     *                        key= #client key<br>
     *                        managementURI= #management URI<br>
     *                        baseURL= #base URL<br>
     *                        authURL= #authentication URL<br>
     *                        </code>
     * @return authenticated Azure client
     * @throws IOException exception thrown from file access
     */
    public static Authenticated authenticate(File credentialsFile) throws IOException {
        ApplicationTokenCredential credential = ApplicationTokenCredential.fromFile(credentialsFile);
        return new AuthenticatedImpl(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain()).withDefaultSubscription(credential.getDefaultSubscriptionId());
    }

    /**
     * Authenticates API access using a RestClient instance.
     *
     * @param restClient the RestClient configured with Azure authentication credentials
     * @param tenantId   the tenantId in Active Directory
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(RestClient restClient, String tenantId) {
        return new AuthenticatedImpl(restClient, tenantId);
    }

    /**
     * Authenticates API access using a RestClient instance.
     *
     * @param restClient     the RestClient configured with Azure authentication credentials
     * @param tenantId       the tenantId in Active Directory
     * @param subscriptionId the ID of the subscription
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(RestClient restClient, String tenantId, String subscriptionId) {
        return new AuthenticatedImpl(restClient, tenantId).withDefaultSubscription(subscriptionId);
    }

    /**
     * @return an interface allow configurations on the client.
     */
    public static Configurable configure() {
        return new ConfigurableImpl();
    }

    /**
     * The interface allowing configurations to be made on the client.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Authenticates API access based on the provided credentials.
         *
         * @param credentials The credentials to authenticate API access with
         * @return the authenticated Azure client
         */
        Authenticated authenticate(AzureTokenCredential credentials);

        /**
         * Authenticates API access using a properties file containing the required credentials.
         *
         * @param credentialsFile the file containing the credentials in the standard Java properties file format following
         *                        the same schema as {@link Azure#authenticate(File)}.<p>
         * @return Authenticated Azure client
         * @throws IOException exceptions thrown from file access
         */
        Authenticated authenticate(File credentialsFile) throws IOException;
    }

    /**
     * The implementation for {@link Configurable}.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public Authenticated authenticate(AzureTokenCredential credentials) {
            if (credentials.getDefaultSubscriptionId() != null) {
                return Azure.authenticate(buildRestClient(credentials), credentials.getDomain(), credentials.getDefaultSubscriptionId());
            } else {
                return Azure.authenticate(buildRestClient(credentials), credentials.getDomain());
            }
        }

        @Override
        public Authenticated authenticate(File credentialsFile) throws IOException {
            ApplicationTokenCredential credentials = ApplicationTokenCredential.fromFile(credentialsFile);
            return Azure.authenticate(buildRestClient(credentials), credentials.getDomain(), credentials.getDefaultSubscriptionId());
        }
    }

    /**
     * Provides authenticated access to a subset of Azure APIs that do not require a specific subscription.
     * <p>
     * To access the subscription-specific APIs, use {@link Authenticated#withSubscription(String)},
     * or withDefaultSubscription() if a default subscription has already been previously specified
     * (for example, in a previously specified authentication file).
     */
    public interface Authenticated extends AccessManagement {
        /**
         * @return the currently selected tenant ID this client is authenticated to work with
         */
        String tenantId();

        /**
         * @return the sdk context in authenticated
         */
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
         * Selects a specific subscription for the APIs to work with.
         * <p>
         * Most Azure APIs require a specific subscription to be selected.
         *
         * @param subscriptionId the ID of the subscription
         * @return an authenticated Azure client configured to work with the specified subscription
         */
        Azure withSubscription(String subscriptionId);

        /**
         * Selects the default subscription as the subscription for the APIs to work with.
         * <p>
         * The default subscription can be specified inside the authentication file using {@link Azure#authenticate(File)}.
         * If no default subscription has been previously provided, the first subscription as
         * returned by {@link Authenticated#subscriptions()} will be selected.
         *
         * @return an authenticated Azure client configured to work with the default subscription
         * @throws CloudException exception thrown from Azure
         * @throws IOException    exception thrown from serialization/deserialization
         */
        Azure withDefaultSubscription() throws CloudException, IOException;
    }

    /**
     * The implementation for the Authenticated interface.
     */
    private static final class AuthenticatedImpl implements Authenticated {
        private final RestClient restClient;
        private final ResourceManager.Authenticated resourceManagerAuthenticated;
        private final GraphRbacManager graphRbacManager;
        private SdkContext sdkContext;
        private String defaultSubscription;
        private String tenantId;

        private AuthenticatedImpl(RestClient restClient, String tenantId) {
            this.resourceManagerAuthenticated = ResourceManager.authenticate(restClient);
            this.graphRbacManager = GraphRbacManager.authenticate(restClient, tenantId);
            this.restClient = restClient;
            this.tenantId = tenantId;
            this.sdkContext = new SdkContext();
        }

        private AuthenticatedImpl withDefaultSubscription(String subscriptionId) {
            this.defaultSubscription = subscriptionId;
            return this;
        }

        @Override
        public String tenantId() {
            return tenantId;
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
        public Azure withSubscription(String subscriptionId) {
            return new Azure(restClient, subscriptionId, tenantId, this);
        }

        @Override
        public Azure withDefaultSubscription() throws CloudException, IOException {
            if (this.defaultSubscription != null) {
                return withSubscription(this.defaultSubscription);
            } else {
                PagedIterable<Subscription> subs = this.subscriptions().list();
                if (subs.iterator().hasNext()) {
                    return withSubscription(subs.iterator().next().subscriptionId());
                } else {
                    return withSubscription(null);
                }
            }
        }
    }

    private Azure(RestClient restClient, String subscriptionId, String tenantId, Authenticated authenticated) {
        this.sdkContext = authenticated.sdkContext();
        this.resourceManager = ResourceManager.authenticate(restClient).withSdkContext(sdkContext).withSubscription(subscriptionId);
        this.storageManager = StorageManager.authenticate(restClient, subscriptionId, sdkContext);
        this.computeManager = ComputeManager.authenticate(restClient, subscriptionId, sdkContext);
        this.networkManager = NetworkManager.authenticate(restClient, subscriptionId, sdkContext);
        this.keyVaultManager = KeyVaultManager.authenticate(restClient, tenantId, subscriptionId, sdkContext);
//        this.batchManager = BatchManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.trafficManager = TrafficManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.redisManager = RedisManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.cdnManager = CdnManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.dnsZoneManager = DnsZoneManager.authenticate(restClient, subscriptionId, sdkContext);
        this.appServiceManager = AppServiceManager.authenticate(restClient, tenantId, subscriptionId, sdkContext);
        this.sqlServerManager = SqlServerManager.authenticate(restClient, tenantId, subscriptionId, sdkContext);
//        this.serviceBusManager = ServiceBusManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.containerInstanceManager = ContainerInstanceManager.authenticate(restClient, subscriptionId, sdkContext);
        this.containerRegistryManager = ContainerRegistryManager.authenticate(restClient, subscriptionId, sdkContext);
        this.containerServiceManager = ContainerServiceManager.authenticate(restClient, subscriptionId, sdkContext);
        this.cosmosDBManager = CosmosDBManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.searchServiceManager = SearchServiceManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.authorizationManager = AuthorizationManager.authenticate(restClient, subscriptionId, sdkContext);
        this.msiManager = MSIManager.authenticate(restClient, subscriptionId, sdkContext);
        this.monitorManager = MonitorManager.authenticate(restClient, subscriptionId, sdkContext);
//        this.eventHubManager = EventHubManager.authenticate(restClient, subscriptionId, sdkContext);
        this.subscriptionId = subscriptionId;
        this.authenticated = authenticated;
    }


    /**
     * @return the currently selected subscription ID this client is authenticated to work with
     */
    public SdkContext sdkContext() {
        return this.sdkContext;
    }

    /**
     * @return the currently selected subscription ID this client is authenticated to work with
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @return the currently selected subscription this client is authenticated to work with
     */
    public Subscription getCurrentSubscription() {
        return this.subscriptions().getById(this.subscriptionId());
    }

    /**
     * @return subscriptions that this authenticated client has access to
     */
    public Subscriptions subscriptions() {
        return this.authenticated.subscriptions();
    }

    /**
     * @return entry point to managing resource groups
     */
    public ResourceGroups resourceGroups() {
        return this.resourceManager.resourceGroups();
    }

    /**
     * @return entry point to managing deployments
     */
    public Deployments deployments() {
        return this.resourceManager.deployments();
    }

    /**
     * @return entry point to managing generic resources
     */
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

    /**
     * @return entry point to managing resource providers
     */
    public Providers providers() {
        return resourceManager.providers();
    }

//    /**
//     * @return entry point to managing policy definitions.
//     */
//    public PolicyDefinitions policyDefinitions() {
//        return resourceManager.policyDefinitions();
//    }
//
//    /**
//     * @return entry point to managing policy assignments.
//     */
//    public PolicyAssignments policyAssignments() {
//        return resourceManager.policyAssignments();
//    }

    /**
     * @return entry point to managing storage accounts
     */
    public StorageAccounts storageAccounts() {
        return storageManager.storageAccounts();
    }

    /**
     * @return entry point to managing storage account usages
     */
    public Usages storageUsages() {
        return storageManager.usages();
    }

    /**
     * @return entry point to managing storage service SKUs
     */
    public StorageSkus storageSkus() {
        return storageManager.storageSkus();
    }

    /**
     * @return entry point to managing availability sets
     */
    public AvailabilitySets availabilitySets() {
        return computeManager.availabilitySets();
    }

    /**
     * @return entry point to managing virtual networks
     */
    public Networks networks() {
        return networkManager.networks();
    }

    /**
     * @return entry point to managing route tables
     */
    public RouteTables routeTables() {
        return networkManager.routeTables();
    }

    /**
     * @return entry point to managing load balancers
     */
    public LoadBalancers loadBalancers() {
        return networkManager.loadBalancers();
    }

    /**
     * @return entry point to managing application gateways
     */
    public ApplicationGateways applicationGateways() {
        return networkManager.applicationGateways();
    }

    /**
     * @return entry point to managing network security groups
     */
    public NetworkSecurityGroups networkSecurityGroups() {
        return networkManager.networkSecurityGroups();
    }

    /**
     * @return entry point to managing network resource usages
     */
    public NetworkUsages networkUsages() {
        return networkManager.usages();
    }

    /**
     * @return entry point to managing network watchers
     */
    public NetworkWatchers networkWatchers() {
        return networkManager.networkWatchers();
    }

    /**
     * @return entry point to managing virtual network gateways
     */
    public VirtualNetworkGateways virtualNetworkGateways() {
        return networkManager.virtualNetworkGateways();
    }

    /**
     * @return entry point to managing local network gateways
     */
    public LocalNetworkGateways localNetworkGateways() {
        return networkManager.localNetworkGateways();
    }

    /**
     * @return entry point to managing express route circuits
     */
    public ExpressRouteCircuits expressRouteCircuits() {
        return networkManager.expressRouteCircuits();
    }

    /**
     * @return entry point to managing express route cross connections
     */
    public ExpressRouteCrossConnections expressRouteCrossConnections() {
        return networkManager.expressRouteCrossConnections();
    }

    /**
     * @return entry point to managing express route circuits
     */
    public ApplicationSecurityGroups applicationSecurityGroups() {
        return networkManager.applicationSecurityGroups();
    }

    /**
     * @return entry point to managing route filters
     */
    public RouteFilters routeFilters() {
        return networkManager.routeFilters();
    }

    /**
     * @return entry point to managing DDoS protection plans
     */
    public DdosProtectionPlans ddosProtectionPlans() {
        return networkManager.ddosProtectionPlans();
    }

    /**
     * @return entry point to managing virtual machines
     */
    public VirtualMachines virtualMachines() {
        return computeManager.virtualMachines();
    }

    /**
     * @return entry point to managing virtual machine scale sets.
     */
    public VirtualMachineScaleSets virtualMachineScaleSets() {
        return computeManager.virtualMachineScaleSets();
    }

    /**
     * @return entry point to managing virtual machine images
     */
    public VirtualMachineImages virtualMachineImages() {
        return computeManager.virtualMachineImages();
    }

    /**
     * @return entry point to managing virtual machine custom images
     */
    public VirtualMachineCustomImages virtualMachineCustomImages() {
        return computeManager.virtualMachineCustomImages();
    }

    /**
     * @return entry point to managing managed disks
     */
    public Disks disks() {
        return computeManager.disks();
    }

    /**
     * @return entry point to managing managed snapshots
     */
    public Snapshots snapshots() {
        return computeManager.snapshots();
    }

    /**
     * @return the compute service SKU management API entry point
     */
    public ComputeSkus computeSkus() {
        return computeManager.computeSkus();
    }

    /**
     * @return entry point to managing public IP addresses
     */
    public PublicIPAddresses publicIPAddresses() {
        return this.networkManager.publicIPAddresses();
    }

//    /**
//     * @return entry point to managing public IP prefixes
//     */
//    public PublicIPPrefixes publicIPPrefixes() {
//        return this.networkManager.publicIPPrefixes();
//    }

    /**
     * @return entry point to managing network interfaces
     */
    public NetworkInterfaces networkInterfaces() {
        return this.networkManager.networkInterfaces();
    }

    /**
     * @return entry point to managing compute resource usages
     */
    public ComputeUsages computeUsages() {
        return computeManager.usages();
    }

    /**
     * @return entry point to managing key vaults
     */
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

//    /**
//     * @return entry point to managing DNS zones.
//     */
//    public DnsZones dnsZones() {
//        return dnsZoneManager.zones();
//    }

    /**
     * @return entry point to managing web apps.
     */
    public WebApps webApps() {
        return appServiceManager.webApps();
    }

    /**
     * @return entry point to managing app services.
     */
    public AppServiceManager appServices() {
        return appServiceManager;
    }

    /**
     * @return entry point to managing Sql server.
     */
    public SqlServers sqlServers() {
        return sqlServerManager.sqlServers();
    }

//    /**
//     * @return entry point to managing Service Bus.
//     */
//    public ServiceBusNamespaces serviceBusNamespaces() {
//        return serviceBusManager.namespaces();
//    }

    /**
     * @return entry point to managing Service Bus operations.
     */
    // TODO: To be revisited in the future
    //@Beta(SinceVersion.V1_1_0)
    //public ServiceBusOperations serviceBusOperations() {
    //    return serviceBusManager.operations();
    //}
//
    /**
     * @return entry point to managing Container Services.
     */
    public ContainerServices containerServices() {
        return containerServiceManager.containerServices();
    }

    /**
     * @return entry point to managing Kubernetes clusters.
     */
    public KubernetesClusters kubernetesClusters() {
        return containerServiceManager.kubernetesClusters();
    }
//
//    /**
//     * @return entry point to managing Azure Container Instances.
//     */
//    @Beta(SinceVersion.V1_3_0)
//    public ContainerGroups containerGroups() {
//        return containerInstanceManager.containerGroups();
//    }

    /**
     * @return entry point to managing Container Registries.
     */
    public Registries containerRegistries() {
        return containerRegistryManager.containerRegistries();
    }

    /**
     * @return entry point to managing Container Registry RegistryTasks.
     */
    public RegistryTasks containerRegistryTasks() {
        return containerRegistryManager.containerRegistryTasks();
    }

    /**
     * @return entry point to managing Container Registry RegistryTask Runs.
     */
    public RegistryTaskRuns containerRegistryTaskRuns() {
        return containerRegistryManager.registryTaskRuns();
    }

    /**
     * @return entry point to managing Container Regsitries.
     */
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

    /**
     * @return entry point to managing Managed Service Identity (MSI) identities.
     */
    public Identities identities() {
        return msiManager.identities();
    }

    /**
     * @return entry point to authentication and authorization management in Azure
     */
    public AccessManagement accessManagement() {
        return this.authenticated;
    }

    /**
     * @return entry point to listing activity log events in Azure
     */
    public ActivityLogs activityLogs() {
        return this.monitorManager.activityLogs();
    }

    /**
     * @return entry point to listing metric definitions in Azure
     */
    public MetricDefinitions metricDefinitions() {
        return this.monitorManager.metricDefinitions();
    }

    /**
     * @return entry point to listing diagnostic settings in Azure
     */
    public DiagnosticSettings diagnosticSettings() {
        return this.monitorManager.diagnosticSettings();
    }

    /**
     * @return entry point to managing action groups in Azure
     */
    public ActionGroups actionGroups() {
        return this.monitorManager.actionGroups();
    }

    /**
     * @return entry point to managing alertRules in Azure
     */
    public AlertRules alertRules() {
        return this.monitorManager.alertRules();
    }

    /**
     * @return entry point to managing Autoscale Settings in Azure
     */
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

    /**
     * @return entry point to manage compute galleries.
     */
    public Galleries galleries() {
        return this.computeManager.galleries();
    }

    /**
     * @return entry point to manage compute gallery images.
     */
    public GalleryImages galleryImages() {
        return this.computeManager.galleryImages();
    }

    /**
     * @return entry point to manage compute gallery image versions.
     */
    public GalleryImageVersions galleryImageVersions() {
        return this.computeManager.galleryImageVersions();
    }

    /**
     * @return the blob container management API entry point
     */
    public BlobContainers storageBlobContainers() {
        return this.storageManager.blobContainers();
    }

    /**
     * @return the blob service management API entry point
     */
    public BlobServices storageBlobServices() {
        return this.storageManager.blobServices();
    }

    /**
     * @return the blob service management API entry point
     */
    public ManagementPolicies storageManagementPolicies() {
        return this.storageManager.managementPolicies();
    }
}
