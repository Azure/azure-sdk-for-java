// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appservice.AppServiceCertificateOrders;
import com.azure.resourcemanager.appservice.AppServiceCertificates;
import com.azure.resourcemanager.appservice.AppServiceDomains;
import com.azure.resourcemanager.appservice.AppServicePlans;
import com.azure.resourcemanager.appservice.FunctionApps;
import com.azure.resourcemanager.appservice.WebApps;
import com.azure.resourcemanager.appservice.models.WebSiteManagementClientBuilder;
import com.azure.resourcemanager.appservice.models.WebSiteManagementClientImpl;
import com.azure.resourcemanager.authorization.implementation.GraphRbacManager;
import com.azure.resourcemanager.keyvault.implementation.KeyVaultManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.storage.StorageManager;

/** Entry point to Azure storage resource management. */
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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the StorageManager
     */
    public static AppServiceManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new AppServiceManager(httpPipeline, profile, sdkContext);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing AppService management API entry points that work across subscriptions
         */
        AppServiceManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public AppServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
            return AppServiceManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private AppServiceManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new WebSiteManagementClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
        keyVaultManager = KeyVaultManager.authenticate(httpPipeline, profile, sdkContext);
        storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
        rbacManager = GraphRbacManager.authenticate(httpPipeline, profile, sdkContext);
    }

    /** @return the Graph RBAC manager instance. */
    GraphRbacManager rbacManager() {
        return rbacManager;
    }

    /** @return the key vault manager instance. */
    KeyVaultManager keyVaultManager() {
        return keyVaultManager;
    }

    /** @return the storage manager instance. */
    StorageManager storageManager() {
        return storageManager;
    }

    /** @return the web app management API entry point */
    public WebApps webApps() {
        if (webApps == null) {
            webApps = new WebAppsImpl(this);
        }
        return webApps;
    }

    /** @return the app service plan management API entry point */
    public AppServicePlans appServicePlans() {
        if (appServicePlans == null) {
            appServicePlans = new AppServicePlansImpl(this);
        }
        return appServicePlans;
    }

    /** @return the certificate order management API entry point */
    public AppServiceCertificateOrders certificateOrders() {
        if (appServiceCertificateOrders == null) {
            appServiceCertificateOrders = new AppServiceCertificateOrdersImpl(this);
        }
        return appServiceCertificateOrders;
    }

    /** @return the certificate management API entry point */
    public AppServiceCertificates certificates() {
        if (appServiceCertificates == null) {
            appServiceCertificates = new AppServiceCertificatesImpl(this);
        }
        return appServiceCertificates;
    }

    /** @return the app service plan management API entry point */
    public AppServiceDomains domains() {
        if (appServiceDomains == null) {
            appServiceDomains = new AppServiceDomainsImpl(this);
        }
        return appServiceDomains;
    }
    /** @return the web app management API entry point */
    public FunctionApps functionApps() {
        if (functionApps == null) {
            functionApps = new FunctionAppsImpl(this);
        }
        return functionApps;
    }
}
