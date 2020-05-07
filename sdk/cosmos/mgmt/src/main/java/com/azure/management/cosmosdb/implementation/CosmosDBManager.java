// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.cosmosdb.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.management.cosmosdb.CosmosDBAccounts;
import com.azure.management.cosmosdb.models.CosmosDBManagementClientBuilder;
import com.azure.management.cosmosdb.models.CosmosDBManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure compute resource management. */
public final class CosmosDBManager extends Manager<CosmosDBManager, CosmosDBManagementClientImpl> {
    private CosmosDBAccountsImpl databaseAccounts;
    /**
     * Get a Configurable instance that can be used to create ComputeManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new CosmosDBManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ComputeManager
     */
    public static CosmosDBManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile
     * @return the ComputeManager
     */
    public static CosmosDBManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
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
    public static CosmosDBManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new CosmosDBManager(httpPipeline, profile, sdkContext);
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
        CosmosDBManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public CosmosDBManager authenticate(TokenCredential credential, AzureProfile profile) {
            return CosmosDBManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private CosmosDBManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new CosmosDBManagementClientBuilder()
                .host(profile.environment().getResourceManagerEndpoint())
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
