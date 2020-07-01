// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.appplatform.implementation.SpringServicesImpl;
import com.azure.resourcemanager.appplatform.models.SpringServices;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure App Platform management. */
public final class AppPlatformManager extends Manager<AppPlatformManager, AppPlatformManagementClient> {
    // Collections
    private SpringServices springServices;
    /**
     * Get a Configurable instance that can be used to create AppPlatformManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new AppPlatformManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of AppPlatformManager that exposes app platform resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the AppPlatformManager
     */
    public static AppPlatformManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of AppPlatformManager that exposes app platform resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the AppPlatformManager
     */
    public static AppPlatformManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of AppPlatformManager that exposes app platform resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the AppPlatformManager
     */
    public static AppPlatformManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new AppPlatformManager(httpPipeline, profile, sdkContext);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of AppPlatformManager that exposes app platform resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing AppPlatform management API entry points that work across subscriptions
         */
        AppPlatformManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public AppPlatformManager authenticate(TokenCredential credential, AzureProfile profile) {
            return AppPlatformManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private AppPlatformManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new AppPlatformManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
    }

    /** @return Entry point for Spring Service management API. */
    public SpringServices springServices() {
        if (springServices == null) {
            springServices = new SpringServicesImpl(this);
        }
        return springServices;
    }
}
