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
import com.microsoft.azure.management.appservice.v2018_02_01.implementation.AppServiceManager;
import com.microsoft.azure.management.authorization.v2015_07_01.implementation.AuthorizationManager;
import com.microsoft.azure.management.commerce.v2015_06_01_preview.implementation.CommerceManager;
import com.microsoft.azure.management.compute.v2020_06_01.implementation.ComputeManager;
import com.microsoft.azure.management.databoxedge.v2019_08_01.implementation.DataBoxEdgeManager;
import com.microsoft.azure.management.eventhubs.v2018_01_01_preview.implementation.EventHubsManager;
import com.microsoft.azure.management.iothub.v2019_07_01_preview.implementation.IoTHubManager;
import com.microsoft.azure.management.keyvault.v2019_09_01.implementation.KeyVaultManager;
import com.microsoft.azure.management.locks.v2016_09_01.implementation.LocksManager;
import com.microsoft.azure.management.monitor.v2018_01_01.implementation.MonitorManager;
import com.microsoft.azure.management.network.v2018_11_01.implementation.NetworkManager;
import com.microsoft.azure.management.policy.v2016_12_01.implementation.PolicyManager;
import com.microsoft.azure.management.resources.v2016_06_01.Subscription;
import com.microsoft.azure.management.resources.v2016_06_01.implementation.Manager;
import com.microsoft.azure.management.resources.v2019_10_01.implementation.ResourcesManager;
import com.microsoft.azure.management.storage.v2019_06_01.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

import java.io.IOException;

/**
 * Entry point to Azure ContainerService resource management.
 */
public final class Azure {
    private final com.microsoft.azure.management.locks.v2016_09_01.implementation.LocksManager locksManager20160901;
    private final com.microsoft.azure.management.policy.v2016_12_01.implementation.PolicyManager policyManager20161201;
    private final com.microsoft.azure.management.commerce.v2015_06_01_preview.implementation.CommerceManager commerceManager20150601;
    private final com.microsoft.azure.management.compute.v2020_06_01.implementation.ComputeManager computeManager20200601;
    private final com.microsoft.azure.management.compute.v2019_07_01.implementation.ComputeManager computeManager20190701;
    private final com.microsoft.azure.management.databoxedge.v2019_08_01.implementation.DataBoxEdgeManager dataBoxEdgeManager20190801;
    private final com.microsoft.azure.management.iothub.v2019_07_01_preview.implementation.IoTHubManager ioTHubManager20190701;
    private final com.microsoft.azure.management.eventhubs.v2018_01_01_preview.implementation.EventHubsManager eventHubsManager20180101;
    private final com.microsoft.azure.management.monitor.v2018_01_01.implementation.MonitorManager monitorManager20180101;
    private final com.microsoft.azure.management.monitor.v2017_05_01_preview.implementation.MonitorManager monitorManager20170501preview;
    private final com.microsoft.azure.management.monitor.v2015_04_01.implementation.MonitorManager monitorManager20150401;
    private final com.microsoft.azure.management.keyvault.v2019_09_01.implementation.KeyVaultManager keyVaultManager20190901;
    private final com.microsoft.azure.management.network.v2018_11_01.implementation.NetworkManager networkManager20181101;
    private final com.microsoft.azure.management.dns.v2016_04_01.implementation.NetworkManager dnsManager20160401;
    private final com.microsoft.azure.management.resources.v2019_10_01.implementation.ResourcesManager resourceManager20191001;
    private final com.microsoft.azure.management.storage.v2019_06_01.implementation.StorageManager storageManager20190601;
    private final com.microsoft.azure.management.appservice.v2018_02_01.implementation.AppServiceManager appServiceManager20180201;
    private final com.microsoft.azure.management.appservice.v2016_09_01.implementation.AppServiceManager appServiceManager20160901;
    private final com.microsoft.azure.management.appservice.v2016_03_01.implementation.AppServiceManager appServiceManager20160301;
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
         * @return Entry point to subscription manager.
         */
        com.microsoft.azure.management.resources.v2016_06_01.implementation.Manager subscriptionsManager();

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
         * returned by {@link Authenticated#subscriptionsManager()} will be selected.
         * @return an authenticated Azure client configured to work with the default subscription
         * @throws CloudException exception thrown from Azure
         * @throws IOException exception thrown from serialization/deserialization
         */
        Azure withDefaultSubscription() throws CloudException, IOException;

        /**
         * @return Entry point to authorization manager.
         */
        AuthorizationManager authorizationManager();
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
        public com.microsoft.azure.management.resources.v2016_06_01.implementation.Manager subscriptionsManager() {
            return subscriptionManager20160601;
        }

        @Override
        public AuthorizationManager authorizationManager() {
            return authorizationManager20150701;
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
                PagedList<Subscription> subs = this.subscriptionsManager().subscriptions().list();
                if (!subs.isEmpty()) {
                    return withSubscription(subs.get(0).subscriptionId());
                } else {
                    return withSubscription(null);
                }
            }
        }
    }

    /**
     * @return the subscription id.
     */
    public String subscriptionId() {
        return subscriptionId;
    }

    /**
     * @return Entry point to locks manager.
     */
    public LocksManager locksManager() {
        return this.locksManager20160901;
    }

    /**
     * @return Entry point to policy manager.
     */
    public PolicyManager policyManager() {
        return this.policyManager20161201;
    }

    /**
     * @return Entry point to authorization manager.
     */
    public AuthorizationManager authorizationManager() {
        return this.authenticated.authorizationManager();
    }

    /**
     * @return Entry point to commerce manager.
     */
    public CommerceManager commerceManager() {
        return this.commerceManager20150601;
    }

    /**
     * @return Entry point to compute manager.
     */
    public ComputeManager computeManager() {
        return this.computeManager20200601;
    }

    /**
     * @return Entry point to disk manager.
     */
    public com.microsoft.azure.management.compute.v2019_07_01.implementation.ComputeManager diskManager() {
        return this.computeManager20190701;
    }

    /**
     * @return Entry point to data box edge manager.
     */
    public DataBoxEdgeManager dataBoxEdgeManager() {
        return this.dataBoxEdgeManager20190801;
    }

    /**
     * @return Entry point to iot hub manager.
     */
    public IoTHubManager ioTHubManager() {
        return this.ioTHubManager20190701;
    }

    /**
     * @return Entry point to event hubs manager.
     */
    public EventHubsManager eventHubsManager() {
        return this.eventHubsManager20180101;
    }

    /**
     * @return Entry point to metrics manager.
     */
    public MonitorManager metricsManager() {
        return this.monitorManager20180101;
    }

    /**
     * @return Entry point to diagnostics manager.
     */
    public com.microsoft.azure.management.monitor.v2017_05_01_preview.implementation.MonitorManager diagnosticsManager() {
        return this.monitorManager20170501preview;
    }

    /**
     * @return Entry point to event categories manager.
     */
    public com.microsoft.azure.management.monitor.v2015_04_01.implementation.MonitorManager eventCategoriesManager() {
        return this.monitorManager20150401;
    }

    /**
     * @return Entry point to key vault manager.
     */
    public KeyVaultManager keyVaultManager() {
        return this.keyVaultManager20190901;
    }

    /**
     * @return Entry point to network manager.
     */
    public NetworkManager networkManager() {
        return this.networkManager20181101;
    }

    /**
     * @return Entry point to dns manager.
     */
    public com.microsoft.azure.management.dns.v2016_04_01.implementation.NetworkManager dnsManager() {
        return this.dnsManager20160401;
    }

    /**
     * @return Entry point to subscriptions manager.
     */
    public Manager subscriptionsManager() {
        return this.authenticated.subscriptionsManager();
    }

    /**
     * @return Entry point to resource manager.
     */
    public ResourcesManager resourceManager() {
        return this.resourceManager20191001;
    }

    /**
     * @return Entry point to storage manager.
     */
    public StorageManager storageManager() {
        return this.storageManager20190601;
    }

    /**
     * @return Entry point to app service manager.
     */
    public AppServiceManager appServiceManager() {
        return this.appServiceManager20180201;
    }

    /**
     * @return Entry point to app service plan manager.
     */
    public com.microsoft.azure.management.appservice.v2016_09_01.implementation.AppServiceManager appServicePlanManager() {
        return this.appServiceManager20160901;
    }

    /**
     * @return Entry point to app service provider manager.
     */
    public com.microsoft.azure.management.appservice.v2016_03_01.implementation.AppServiceManager appServiceProviderManager() {
        return this.appServiceManager20160301;
    }

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
        locksManager20160901 = com.microsoft.azure.management.locks.v2016_09_01.implementation.LocksManager.authenticate(restClient, subscriptionId);
        policyManager20161201 = com.microsoft.azure.management.policy.v2016_12_01.implementation.PolicyManager.authenticate(restClient, subscriptionId);
        commerceManager20150601 = com.microsoft.azure.management.commerce.v2015_06_01_preview.implementation.CommerceManager.authenticate(restClient, subscriptionId);
        computeManager20200601 = com.microsoft.azure.management.compute.v2020_06_01.implementation.ComputeManager.authenticate(restClient, subscriptionId);
        computeManager20190701 = com.microsoft.azure.management.compute.v2019_07_01.implementation.ComputeManager.authenticate(restClient, subscriptionId);
        dataBoxEdgeManager20190801 = com.microsoft.azure.management.databoxedge.v2019_08_01.implementation.DataBoxEdgeManager.authenticate(restClient, subscriptionId);
        ioTHubManager20190701 = com.microsoft.azure.management.iothub.v2019_07_01_preview.implementation.IoTHubManager.authenticate(restClient, subscriptionId);
        eventHubsManager20180101 = com.microsoft.azure.management.eventhubs.v2018_01_01_preview.implementation.EventHubsManager.authenticate(restClient, subscriptionId);
        monitorManager20180101 = com.microsoft.azure.management.monitor.v2018_01_01.implementation.MonitorManager.authenticate(restClient);
        monitorManager20170501preview = com.microsoft.azure.management.monitor.v2017_05_01_preview.implementation.MonitorManager.authenticate(restClient);
        monitorManager20150401 = com.microsoft.azure.management.monitor.v2015_04_01.implementation.MonitorManager.authenticate(restClient, subscriptionId);
        keyVaultManager20190901 = com.microsoft.azure.management.keyvault.v2019_09_01.implementation.KeyVaultManager.authenticate(restClient, subscriptionId);
        networkManager20181101 = com.microsoft.azure.management.network.v2018_11_01.implementation.NetworkManager.authenticate(restClient, subscriptionId);
        dnsManager20160401 = com.microsoft.azure.management.dns.v2016_04_01.implementation.NetworkManager.authenticate(restClient, subscriptionId);
        resourceManager20191001 = com.microsoft.azure.management.resources.v2019_10_01.implementation.ResourcesManager.authenticate(restClient, subscriptionId);
        storageManager20190601 = com.microsoft.azure.management.storage.v2019_06_01.implementation.StorageManager.authenticate(restClient, subscriptionId);
        appServiceManager20180201 = com.microsoft.azure.management.appservice.v2018_02_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        appServiceManager20160901 = com.microsoft.azure.management.appservice.v2016_09_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        appServiceManager20160301 = com.microsoft.azure.management.appservice.v2016_03_01.implementation.AppServiceManager.authenticate(restClient, subscriptionId);
        this.subscriptionId = subscriptionId;
        this.authenticated = authenticated;
    }
}
