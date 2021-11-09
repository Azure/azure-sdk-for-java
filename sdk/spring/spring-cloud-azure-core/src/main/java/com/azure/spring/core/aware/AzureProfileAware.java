// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

/**
 * Interface to be implemented by classes that wish to be aware of the Azure profile.
 */
public interface AzureProfileAware {

    Profile getProfile();

    /**
     * Interface to be implemented by classes that wish to describe an Azure cloud profile.
     */
    interface Profile {

        String getTenantId();

        String getSubscriptionId();

        String getCloud();

        Environment getEnvironment();

    }

    /**
     * Interface to be implemented by classes that wish to describe an Azure cloud environment.
     */
    interface Environment {

        String getPortal();

        String getPublishingProfile();

        String getManagementEndpoint();

        String getResourceManagerEndpoint();

        String getSqlManagementEndpoint();

        String getSqlServerHostnameSuffix();

        String getGalleryEndpoint();

        String getActiveDirectoryEndpoint();

        String getActiveDirectoryResourceId();

        String getActiveDirectoryGraphEndpoint();

        String getMicrosoftGraphEndpoint();

        String getDataLakeEndpointResourceId();

        String getActiveDirectoryGraphApiVersion();

        String getStorageEndpointSuffix();

        String getKeyVaultDnsSuffix();

        String getAzureDataLakeStoreFileSystemEndpointSuffix();

        String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix();

        String getAzureLogAnalyticsEndpoint();

        String getAzureApplicationInsightsEndpoint();

    }
}
