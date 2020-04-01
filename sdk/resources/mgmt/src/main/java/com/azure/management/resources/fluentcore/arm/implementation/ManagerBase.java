/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.implementation;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.implementation.ResourceManager;

/**
 * Base class for Azure resource managers.
 */
public abstract class ManagerBase {

    private ResourceManager resourceManager;
    private final String subscriptionId;
    protected final RestClient restClient;
    private SdkContext sdkContext;

    protected ManagerBase(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        this.restClient = restClient;
        if (restClient != null) {
            this.resourceManager = ResourceManager.authenticate(restClient)
                    .withSdkContext(sdkContext)
                    .withSubscription(subscriptionId);
        }
        this.subscriptionId = subscriptionId;
        this.sdkContext = sdkContext;
    }

    /**
     * @return the ID of the subscription the manager is working with
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    protected final void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * @return the {@link ResourceManager} associated with this manager
     */
    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    /**
     * @return the {@link RestClient} associated with this manager
     */
    public RestClient getRestClient() {
        return this.restClient;
    }

    /**
     * @return the {@link SdkContext} associated with this manager
     */
    public SdkContext getSdkContext() {
        return this.sdkContext;
    }
}
