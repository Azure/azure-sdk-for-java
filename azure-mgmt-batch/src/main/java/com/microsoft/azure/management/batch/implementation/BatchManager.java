/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.batch.BatchAccounts;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure Batch service management.
 */
public class BatchManager extends Manager<BatchManager, BatchManagementClientImpl> {

    private BatchAccounts batchAccounts;
    private StorageManager storageManager;

    protected BatchManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new BatchManagementClientImpl(restClient).withSubscriptionId(subscriptionId));

        storageManager = StorageManager.authenticate(restClient, subscriptionId);
    }

    /**
     * Get a Configurable instance that can be used to create a BatchManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new BatchManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of a BatchManager that exposes Batch resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the BatchManager
     */
    public static BatchManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new BatchManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of a BatchManager that exposes Batch resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the BatchManager
     */
    public static BatchManager authenticate(RestClient restClient, String subscriptionId) {
        return new BatchManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of BatchManager that exposes Compute resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the BatchManager
         */
        BatchManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public BatchManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return BatchManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    /**
     * @return the batch account management API entry point
     */
    public BatchAccounts batchAccounts() {
        if (batchAccounts == null) {
            batchAccounts = new BatchAccountsImpl(this, this.storageManager);
        }

        return batchAccounts;
    }
}
