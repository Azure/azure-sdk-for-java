// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;
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
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setPublishingProfile(String publishingProfile) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setManagementEndpoint(String managementEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setGalleryEndpoint(String galleryEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setStorageEndpointSuffix(String storageEndpointSuffix) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }

    public void setAzureApplicationInsightsEndpoint(String azureApplicationInsightsEndpoint) {
        throw new UnsupportedOperationException("Set method is not supported in a KnownAzureEnvironment");
    }



}
