/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.BlobContainers;
import com.azure.management.storage.BlobServices;
import com.azure.management.storage.ManagementPolicies;
import com.azure.management.storage.StorageAccounts;
import com.azure.management.storage.StorageSkus;
import com.azure.management.storage.Usages;
import com.azure.management.storage.models.StorageManagementClientBuilder;
import com.azure.management.storage.models.StorageManagementClientImpl;

/**
 * Entry point to Azure storage resource management.
 */
public final class StorageManager extends Manager<StorageManager, StorageManagementClientImpl> {
    // Collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;
    private StorageSkus storageSkus;
    private BlobContainers blobContainers;
    private BlobServices blobServices;
    private ManagementPolicies managementPolicies;

    /**
     * Get a Configurable instance that can be used to create StorageManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new StorageManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param credential     the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static StorageManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), subscriptionId);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient     the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static StorageManager authenticate(RestClient restClient, String subscriptionId) {
        return authenticate(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient     the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @param SdkContext     the sdk context
     * @return the StorageManager
     */
    public static StorageManager authenticate(RestClient restClient, String subscriptionId, SdkContext SdkContext) {
        return new StorageManager(restClient, subscriptionId, SdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credentials    the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing storage management API entry points that work across subscriptions
         */
        StorageManager authenticate(AzureTokenCredential credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public StorageManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
            return StorageManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private StorageManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(restClient,
                subscriptionId,
                new StorageManagementClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .host(restClient.getBaseUrl().toString())
                        .subscriptionId(subscriptionId)
                        .build(),
                sdkContext);
    }

    /**
     * @return the storage account management API entry point
     */
    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(this);
        }
        return storageAccounts;
    }

    /**
     * @return the storage service usage management API entry point
     */
    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(this);
        }
        return storageUsages;
    }

    /**
     * @return the storage service SKU management API entry point
     */
    public StorageSkus storageSkus() {
        if (storageSkus == null) {
            storageSkus = new StorageSkusImpl(this);
        }
        return storageSkus;
    }

    /**
     * @return the blob container management API entry point
     */
    public BlobContainers blobContainers() {
        if (blobContainers == null) {
            blobContainers = new BlobContainersImpl(this);
        }
        return blobContainers;
    }

    /**
     * @return the blob service management API entry point
     */
    public BlobServices blobServices() {
        if (blobServices == null) {
            blobServices = new BlobServicesImpl(this);
        }
        return blobServices;
    }

    /**
     * @return the management policy management API entry point
     */
    public ManagementPolicies managementPolicies() {
        if (managementPolicies == null) {
            managementPolicies = new ManagementPoliciesImpl(this);
        }
        return managementPolicies;
    }
}