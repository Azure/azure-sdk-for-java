/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.appservice.WebApps;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.batch.BatchAccounts;
import com.microsoft.azure.management.batch.implementation.BatchManager;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.cdn.implementation.CdnManager;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.ComputeUsages;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.compute.Snapshots;
import com.microsoft.azure.management.compute.VirtualMachineCustomImages;
import com.microsoft.azure.management.compute.VirtualMachineImages;
import com.microsoft.azure.management.compute.VirtualMachineScaleSets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.ContainerServices;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.dns.DnsZones;
import com.microsoft.azure.management.dns.implementation.DnsZoneManager;
import com.microsoft.azure.management.documentdb.DatabaseAccounts;
import com.microsoft.azure.management.documentdb.implementation.DocumentDBManager;
import com.microsoft.azure.management.keyvault.Vaults;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.network.ApplicationGateways;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.NetworkUsages;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.network.RouteTables;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.redis.implementation.RedisManager;
import com.microsoft.azure.management.resources.Deployments;
import com.microsoft.azure.management.resources.Features;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.PolicyAssignments;
import com.microsoft.azure.management.resources.PolicyDefinitions;
import com.microsoft.azure.management.resources.Providers;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.implementation.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.servicebus.ServiceBusNamespaces;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.sql.SqlServers;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
import com.microsoft.azure.management.trafficmanager.implementation.TrafficManager;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

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
    private final BatchManager batchManager;
    private final TrafficManager trafficManager;
    private final RedisManager redisManager;
    private final CdnManager cdnManager;
    private final DnsZoneManager dnsZoneManager;
    private final AppServiceManager appServiceManager;
    private final SqlServerManager sqlServerManager;
    private final ServiceBusManager serviceBusManager;
    private final ContainerRegistryManager containerRegistryManager;
    private final DocumentDBManager documentDBManager;
    private final String subscriptionId;
    private final Authenticated authenticated;

    /**
     * Authenticate to Azure using an Azure credentials object.
     *
     * @param credentials the credentials object
     * @return the authenticated Azure client
     */
    public static Authenticated authenticate(AzureTokenCredentials credentials) {
        return new AuthenticatedImpl(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), credentials.domain());
    }

    /**
     * Authenticates API access using a properties file containing the required credentials.
     * @param credentialsFile the file containing the credentials in the standard Java properties file format,
     * with the following keys:<p>
     * <code>
        *   subscription= #subscription ID<br>
        *   tenant= #tenant ID<br>
        *   client= #client id<br>
        *   key= #client key<br>
        *   managementURI= #management URI<br>
        *   baseURL= #base URL<br>
        *   authURL= #authentication URL<br>
     *</code>
     * @return authenticated Azure client
     * @throws IOException exception thrown from file access
     */
    public static Authenticated authenticate(File credentialsFile) throws IOException {
        ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credentialsFile);
        return new AuthenticatedImpl(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), credentials.domain()).withDefaultSubscription(credentials.defaultSubscriptionId());
    }

    /**
     * Authenticates API access using a {@link RestClient} instance.
     * @param restClient the {@link RestClient} configured with Azure authentication credentials
     * @param tenantId the tenantId in Active Directory
     * @return authenticated Azure client
     */
    public static Authenticated authenticate(RestClient restClient, String tenantId) {
        return new AuthenticatedImpl(restClient, tenantId);
    }

    /**
     * Authenticates API access using a {@link RestClient} instance.
     * @param restClient the {@link RestClient} configured with Azure authentication credentials
     * @param tenantId the tenantId in Active Directory
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
        Authenticated authenticate(AzureTokenCredentials credentials);

        /**
         * Authenticates API access using a properties file containing the required credentials.
         *
         * @param credentialsFile the file containing the credentials in the standard Java properties file format following
         * the same schema as {@link Azure#authenticate(File)}.<p>
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
        public Authenticated authenticate(AzureTokenCredentials credentials) {
            return Azure.authenticate(buildRestClient(credentials), credentials.domain());
        }

        @Override
        public Authenticated authenticate(File credentialsFile) throws IOException {
            ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credentialsFile);
            return Azure.authenticate(buildRestClient(credentials), credentials.domain(), credentials.defaultSubscriptionId());
        }
    }

    /**
     * Provides authenticated access to a subset of Azure APIs that do not require a specific subscription.
     * <p>
     * To access the subscription-specific APIs, use {@link Authenticated#withSubscription(String)},
     * or {@link Authenticated#withDefaultSubscription()} if a default subscription has already been previously specified
     * (for example, in a previously specified authentication file).
     * @see Azure#authenticate(File)
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
         * The default subscription can be specified inside the authentication file using {@link Azure#authenticate(File)}.
         * If no default subscription has been previously provided, the first subscription as
         * returned by {@link Authenticated#subscriptions()} will be selected.
         * @return an authenticated Azure client configured to work with the default subscription
         * @throws CloudException exception thrown from Azure
         * @throws IOException exception thrown from serialization/deserialization
         */
        Azure withDefaultSubscription() throws CloudException, IOException;
    }

    /**
     * The implementation for {@link Authenticated}.
     */
    private static final class AuthenticatedImpl implements Authenticated {
        private final RestClient restClient;
        private final ResourceManager.Authenticated resourceManagerAuthenticated;
        private String defaultSubscription;
        private String tenantId;

        private AuthenticatedImpl(RestClient restClient, String tenantId) {
            this.resourceManagerAuthenticated = ResourceManager.authenticate(restClient);
            this.restClient = restClient;
            this.tenantId = tenantId;
        }

        private AuthenticatedImpl withDefaultSubscription(String subscriptionId) {
            this.defaultSubscription = subscriptionId;
            return this;
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
        public Azure withSubscription(String subscriptionId) {
            return new Azure(restClient, subscriptionId, tenantId, this);
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

    private Azure(RestClient restClient, String subscriptionId, String tenantId, Authenticated authenticated) {
        ResourceManagementClientImpl resourceManagementClient = new ResourceManagementClientImpl(restClient);
        resourceManagementClient.withSubscriptionId(subscriptionId);
        this.resourceManager = ResourceManager.authenticate(restClient).withSubscription(subscriptionId);
        this.storageManager = StorageManager.authenticate(restClient, subscriptionId);
        this.computeManager = ComputeManager.authenticate(restClient, subscriptionId);
        this.networkManager = NetworkManager.authenticate(restClient, subscriptionId);
        this.keyVaultManager = KeyVaultManager.authenticate(restClient, tenantId, subscriptionId);
        this.batchManager = BatchManager.authenticate(restClient, subscriptionId);
        this.trafficManager = TrafficManager.authenticate(restClient, subscriptionId);
        this.redisManager = RedisManager.authenticate(restClient, subscriptionId);
        this.cdnManager = CdnManager.authenticate(restClient, subscriptionId);
        this.dnsZoneManager = DnsZoneManager.authenticate(restClient, subscriptionId);
        this.appServiceManager = AppServiceManager.authenticate(restClient, tenantId, subscriptionId);
        this.sqlServerManager = SqlServerManager.authenticate(restClient, subscriptionId);
        this.serviceBusManager = ServiceBusManager.authenticate(restClient, subscriptionId);
        this.containerRegistryManager = ContainerRegistryManager.authenticate(restClient, subscriptionId);
        this.documentDBManager = DocumentDBManager.authenticate(restClient, subscriptionId);
        this.subscriptionId = subscriptionId;
        this.authenticated = authenticated;
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
     * @return entry point to management generic resources
     */
    public GenericResources genericResources() {
        return resourceManager.genericResources();
    }

    /**
     * @return entry point to managing features
     */
    public Features features() {
        return resourceManager.features();
    }

    /**
     * @return entry point to managing resource providers
     */
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
    @Beta
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
     * @return entry point to managing public IP addresses
     */
    public PublicIPAddresses publicIPAddresses() {
        return this.networkManager.publicIPAddresses();
    }

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

    /**
     * @return entry point to managing batch accounts.
     */
    public BatchAccounts batchAccounts() {
        return batchManager.batchAccounts();
    }

    /**
     * @return entry point to managing traffic manager profiles.
     */
    public TrafficManagerProfiles trafficManagerProfiles() {
        return trafficManager.profiles();
    }

    /**
     * @return entry point to managing Redis Caches.
     */
    public RedisCaches redisCaches() {
        return redisManager.redisCaches();
    }

    /**
     * @return entry point to managing cdn manager profiles.
     */
    public CdnProfiles cdnProfiles() {
        return cdnManager.profiles();
    }

    /**
     * @return entry point to managing DNS zones.
     */
    public DnsZones dnsZones() {
        return dnsZoneManager.zones();
    }

    /**
     * @return entry point to managing web apps.
     */
    @Beta
    public WebApps webApps() {
        return appServiceManager.webApps();
    }

    /**
     * @return entry point to managing app services.
     */
    @Beta
    public AppServiceManager appServices() {
        return appServiceManager;
    }

    /**
     * @return entry point to managing Sql server.
     */
    public SqlServers sqlServers() {
        return sqlServerManager.sqlServers();
    }

    /**
     * @return entry point to managing Service Bus.
     */
    public ServiceBusNamespaces serviceBusNamespaces() {
        return serviceBusManager.namespaces();
    }

    /**
     * @return entry point to managing Container Services.
     */
    @Beta
    public ContainerServices containerServices() {
        return computeManager.containerServices();
    }

    /**
     * @return entry point to managing Container Registries.
     */
    @Beta
    public Registries containerRegistries() {
        return containerRegistryManager.containerRegistries();
    }

    /**
     * @return entry point to managing Container Regsitries.
     */
    @Beta
    public DatabaseAccounts documentDBs() {
        return documentDBManager.databaseAccounts();
    }
}
