// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.storage.fluent.StorageManagementClient;
import com.azure.resourcemanager.storage.implementation.StorageManagementClientBuilder;
import com.azure.resourcemanager.storage.implementation.BlobContainersImpl;
import com.azure.resourcemanager.storage.implementation.BlobServicesImpl;
import com.azure.resourcemanager.storage.implementation.ManagementPoliciesImpl;
import com.azure.resourcemanager.storage.implementation.StorageAccountsImpl;
import com.azure.resourcemanager.storage.implementation.StorageSkusImpl;
import com.azure.resourcemanager.storage.implementation.UsagesImpl;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.resourcemanager.storage.models.StorageSkus;
import com.azure.resourcemanager.storage.models.Usages;

import java.util.Objects;

/** Entry point to Azure storage resource management. */
public final class StorageManager extends Manager<StorageManagementClient> {
    // Collections
    private StorageAccounts storageAccounts;
    private Usages storageUsages;
    private StorageSkus storageSkus;
    private BlobContainers blobContainers;
    private BlobServices blobServices;
    private ManagementPolicies managementPolicies;
    private final AuthorizationManager authorizationManager;

    /**
     * Gets the authorization manager.
     *
     * @return the authorization manager
     */
    public AuthorizationManager authorizationManager() {
        return authorizationManager;
    }

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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the StorageManager
     */
    public static StorageManager authenticate(TokenCredential credential, AzureProfile profile) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param httpPipeline the {@link HttpPipeline} configured with Azure authentication credential.
     * @param profile the profile to use
     * @return the StorageManager
     */
    public static StorageManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return new StorageManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing storage management API entry points that work across subscriptions
         */
        StorageManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public StorageManager authenticate(TokenCredential credential, AzureProfile profile) {
            return StorageManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private StorageManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(httpPipeline, profile,
            new StorageManagementClientBuilder().pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
        authorizationManager = AuthorizationManager.authenticate(httpPipeline, profile);
    }

    /**
     * Gets the API entry point of the storage account.
     *
     * @return the storage account management API entry point
     */
    public StorageAccounts storageAccounts() {
        if (storageAccounts == null) {
            storageAccounts = new StorageAccountsImpl(this, this.authorizationManager);
        }
        return storageAccounts;
    }

    /**
     * Gets the API entry point of the storage service usage.
     *
     * @return the storage service usage management API entry point
     */
    public Usages usages() {
        if (storageUsages == null) {
            storageUsages = new UsagesImpl(this);
        }
        return storageUsages;
    }

    /**
     * Gets the API entry point of the storage service SKU.
     *
     * @return the storage service SKU management API entry point
     */
    public StorageSkus storageSkus() {
        if (storageSkus == null) {
            storageSkus = new StorageSkusImpl(this);
        }
        return storageSkus;
    }

    /**
     * Gets the API entry point of the blob container.
     *
     * @return the blob container management API entry point
     */
    public BlobContainers blobContainers() {
        if (blobContainers == null) {
            blobContainers = new BlobContainersImpl(this);
        }
        return blobContainers;
    }

    /**
     * Gets the API entry point of the blob service.
     *
     * @return the blob service management API entry point
     */
    public BlobServices blobServices() {
        if (blobServices == null) {
            blobServices = new BlobServicesImpl(this);
        }
        return blobServices;
    }

    /**
     * Gets the API entry point of the management policy.
     *
     * @return the management policy API entry point
     */
    public ManagementPolicies managementPolicies() {
        if (managementPolicies == null) {
            managementPolicies = new ManagementPoliciesImpl(this);
        }
        return managementPolicies;
    }
}
