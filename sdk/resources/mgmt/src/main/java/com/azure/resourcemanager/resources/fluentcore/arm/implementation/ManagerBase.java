// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.ResourceManager;

/**
 * Base class for Azure resource managers.
 */
public abstract class ManagerBase {

    private ResourceManager resourceManager;
    private final String subscriptionId;
    private final AzureEnvironment environment;
    protected final HttpPipeline httpPipeline;
    private SdkContext sdkContext;

    protected ManagerBase(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        this.httpPipeline = httpPipeline;
        if (httpPipeline != null) {
            this.resourceManager = ResourceManager.authenticate(httpPipeline, profile)
                    .withSdkContext(sdkContext)
                    .withDefaultSubscription();
        }
        this.subscriptionId = profile.subscriptionId();
        this.environment = profile.environment();
        this.sdkContext = sdkContext;
    }

    /**
     * @return the ID of the subscription the manager is working with
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @return the Azure environment the manager is working with
     */
    public AzureEnvironment environment() {
        return this.environment;
    }

    protected final void withResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * @return the {@link ResourceManager} associated with this manager
     */
    public ResourceManager resourceManager() {
        return this.resourceManager;
    }

    /**
     * @return the {@link HttpPipeline} associated with this manager
     */
    public HttpPipeline httpPipeline() {
        return this.httpPipeline;
    }

    /**
     * @return the {@link SdkContext} associated with this manager
     */
    public SdkContext sdkContext() {
        return this.sdkContext;
    }
}
