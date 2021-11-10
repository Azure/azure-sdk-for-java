// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_CHINA;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_GERMANY;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.AZURE_US_GOVERNMENT;
import static com.azure.spring.core.aware.AzureProfileAware.CloudType.OTHER;

/**
 * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
 */
public class KnownAzureEnvironment extends AzureEnvironment {

    private static final Logger LOGGER = LoggerFactory.getLogger(KnownAzureEnvironment.class);

    public static final KnownAzureEnvironment AZURE_CHINA_ENV = new KnownAzureEnvironment(AZURE_CHINA);
    public static final KnownAzureEnvironment AZURE_ENV = new KnownAzureEnvironment(AZURE);
    public static final KnownAzureEnvironment AZURE_GERMANY_ENV = new KnownAzureEnvironment(AZURE_GERMANY);
    public static final KnownAzureEnvironment AZURE_US_GOVERNMENT_ENV = new KnownAzureEnvironment(AZURE_US_GOVERNMENT);
    private final AzureProfileAware.CloudType type;


    public KnownAzureEnvironment(AzureProfileAware.CloudType cloudType) {
        super(convertToManagementAzureEnvironmentByType(cloudType));
        this.type = cloudType;
    }

    private static com.azure.core.management.AzureEnvironment convertToManagementAzureEnvironmentByType(
        AzureProfileAware.CloudType cloud) {
        Assert.isTrue(cloud != OTHER, "cloud type should not be other for PredefinedAzureEnvironment");
        switch (cloud) {
            case AZURE_CHINA:
                return com.azure.core.management.AzureEnvironment.AZURE_CHINA;
            case AZURE_US_GOVERNMENT:
                return com.azure.core.management.AzureEnvironment.AZURE_US_GOVERNMENT;
            case AZURE_GERMANY:
                return com.azure.core.management.AzureEnvironment.AZURE_GERMANY;
            default:
                return com.azure.core.management.AzureEnvironment.AZURE;
        }
    }

    @Override
    public com.azure.core.management.AzureEnvironment toManagementAzureEnvironment() {
        return convertToManagementAzureEnvironmentByType(this.type);
    }

    public void setPortal(String portal) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setPublishingProfile(String publishingProfile) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setManagementEndpoint(String managementEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setGalleryEndpoint(String galleryEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setStorageEndpointSuffix(String storageEndpointSuffix) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureApplicationInsightsEndpoint(String azureApplicationInsightsEndpoint) {
        LOGGER.warn("Set method is not supported in a KnownAzureEnvironment");
    }



}
