// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.cosmos.fluent.CosmosDBManagementClient;
import com.azure.resourcemanager.cosmos.implementation.CosmosDBManagementClientBuilder;
import com.azure.resourcemanager.cosmos.implementation.CosmosDBAccountsImpl;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccounts;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/** Entry point to Azure compute resource management. */
public final class CosmosManager extends Manager<CosmosDBManagementClient> {
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
    private static CosmosManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new CosmosManager(httpPipeline, profile);
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

    private CosmosManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new CosmosDBManagementClientBuilder()
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .pipeline(httpPipeline)
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /** @return the cosmos db database account resource management API entry point */
    public CosmosDBAccounts databaseAccounts() {
        if (databaseAccounts == null) {
            databaseAccounts = new CosmosDBAccountsImpl(this);
        }
        return databaseAccounts;
    }
}
