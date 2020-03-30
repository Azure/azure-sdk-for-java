/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.cosmosdb.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.cosmosdb.CosmosDBAccounts;
import com.azure.management.cosmosdb.models.CosmosDBManagementClientBuilder;
import com.azure.management.cosmosdb.models.CosmosDBManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Entry point to Azure compute resource management.
 */
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
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static CosmosDBManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credentials.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
//                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withPolicy(new ProviderRegistrationPolicy(credentials))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), subscriptionId);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static CosmosDBManager authenticate(RestClient restClient, String subscriptionId) {
        return authenticate(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @param sdkContext the sdk context
     * @return the ComputeManager
     */
    public static CosmosDBManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new CosmosDBManager(restClient, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ComputeManager
         */
        CosmosDBManager authenticate(AzureTokenCredential credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public CosmosDBManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
            return CosmosDBManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private CosmosDBManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new CosmosDBManagementClientBuilder()
                    .host(restClient.getBaseUrl().toString())
                    .pipeline(restClient.getHttpPipeline())
                    .subscriptionId(subscriptionId)
                    .build(),
                sdkContext);
    }

    /**
     * @return the cosmos db database account resource management API entry point
     */
    public CosmosDBAccounts databaseAccounts() {
        if (databaseAccounts == null) {
            databaseAccounts = new CosmosDBAccountsImpl(this);
        }
        return databaseAccounts;
    }
}