// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.cosmos.implementation.CosmosDBAccountsImpl;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccounts;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure compute resource management. */
public final class CosmosManager extends Manager<CosmosManager, CosmosDBManagementClient> {
    private CosmosDBAccountsImpl databaseAccounts;
    /**
     * Get a Configurable instance that can be used to create ComputeManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new CosmosManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ComputeManager
     */
    public static CosmosManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile
     * @return the ComputeManager
     */
    public static CosmosManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the ComputeManager
     */
    public static CosmosManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new CosmosManager(httpPipeline, profile, sdkContext);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the ComputeManager
         */
        CosmosManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public CosmosManager authenticate(TokenCredential credential, AzureProfile profile) {
            return CosmosManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private CosmosManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new CosmosDBManagementClientBuilder()
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .pipeline(httpPipeline)
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
    }

    /** @return the cosmos db database account resource management API entry point */
    public CosmosDBAccounts databaseAccounts() {
        if (databaseAccounts == null) {
            databaseAccounts = new CosmosDBAccountsImpl(this);
        }
        return databaseAccounts;
    }
}
