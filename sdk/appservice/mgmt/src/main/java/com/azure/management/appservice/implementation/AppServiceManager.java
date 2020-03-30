/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.appservice.AppServiceCertificateOrders;
import com.azure.management.appservice.AppServiceCertificates;
import com.azure.management.appservice.AppServiceDomains;
import com.azure.management.appservice.AppServicePlans;
import com.azure.management.appservice.FunctionApps;
import com.azure.management.appservice.WebApps;
import com.azure.management.appservice.models.WebSiteManagementClientBuilder;
import com.azure.management.appservice.models.WebSiteManagementClientImpl;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.keyvault.implementation.KeyVaultManager;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.implementation.StorageManager;

/**
 * Entry point to Azure storage resource management.
 */
public final class AppServiceManager extends Manager<AppServiceManager, WebSiteManagementClientImpl> {
    // Managers
    private GraphRbacManager rbacManager;
    private KeyVaultManager keyVaultManager;
    private StorageManager storageManager;
    // Collections
    private WebApps webApps;
    private AppServicePlans appServicePlans;
    private AppServiceCertificateOrders appServiceCertificateOrders;
    private AppServiceCertificates appServiceCertificates;
    private AppServiceDomains appServiceDomains;
    private FunctionApps functionApps;
    private RestClient restClient;

    /**
     * Get a Configurable instance that can be used to create StorageManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new AppServiceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param credential the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain(), subscriptionId);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(RestClient restClient, String tenantId, String subscriptionId) {
        return authenticate(restClient, tenantId, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription UUID
     * @param sdkContext the sdk context
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        return new AppServiceManager(restClient, tenantId, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credential the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing AppService management API entry points that work across subscriptions
         */
        AppServiceManager authenticate(AzureTokenCredential credential, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public AppServiceManager authenticate(AzureTokenCredential credential, String subscriptionId) {
            return AppServiceManager.authenticate(buildRestClient(credential), credential.getDomain(), subscriptionId);
        }
    }

    private AppServiceManager(RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new WebSiteManagementClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .host(restClient.getBaseUrl().toString())
                        .subscriptionId(subscriptionId).build(),
                sdkContext);
        keyVaultManager = KeyVaultManager.authenticate(restClient, tenantId, subscriptionId, sdkContext);
        storageManager = StorageManager.authenticate(restClient, subscriptionId, sdkContext);
        rbacManager = GraphRbacManager.authenticate(restClient, tenantId, sdkContext);
        this.restClient = restClient;
    }

    /**
     * @return the Graph RBAC manager instance.
     */
    GraphRbacManager rbacManager() {
        return rbacManager;
    }

    /**
     * @return the key vault manager instance.
     */
    KeyVaultManager keyVaultManager() {
        return keyVaultManager;
    }

    /**
     * @return the storage manager instance.
     */
    StorageManager storageManager() {
        return storageManager;
    }

    RestClient restClient() {
        return restClient;
    }


    /**
     * @return the web app management API entry point
     */
    public WebApps webApps() {
        if (webApps == null) {
            webApps = new WebAppsImpl(this);
        }
        return webApps;
    }

    /**
     * @return the app service plan management API entry point
     */
    public AppServicePlans appServicePlans() {
        if (appServicePlans == null) {
            appServicePlans = new AppServicePlansImpl(this);
        }
        return appServicePlans;
    }

    /**
     * @return the certificate order management API entry point
     */
    public AppServiceCertificateOrders certificateOrders() {
        if (appServiceCertificateOrders == null) {
            appServiceCertificateOrders = new AppServiceCertificateOrdersImpl(this);
        }
        return appServiceCertificateOrders;
    }

    /**
     * @return the certificate management API entry point
     */
    public AppServiceCertificates certificates() {
        if (appServiceCertificates == null) {
            appServiceCertificates = new AppServiceCertificatesImpl(this);
        }
        return appServiceCertificates;
    }

    /**
     * @return the app service plan management API entry point
     */
    public AppServiceDomains domains() {
        if (appServiceDomains == null) {
            appServiceDomains = new AppServiceDomainsImpl(this);
        }
        return appServiceDomains;
    }
    /**
     * @return the web app management API entry point
     */
    public FunctionApps functionApps() {
        if (functionApps == null) {
            functionApps = new FunctionAppsImpl(this);
        }
        return functionApps;
    }
}