// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfile {

    private String tenantId;
    private String subscriptionId;
    private String cloud = "Azure"; // TODO (xiada) this name
    private AzureEnvironment environment = AzureEnvironment.AZURE;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCloud() {
        return cloud;
    }

    public void setCloud(String cloud) {
        this.cloud = cloud;
        this.environment = AzureEnvironment.fromAzureCloud(cloud);
    }

    public AzureEnvironment getEnvironment() {
        return environment;
    }

    /**
     * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
     */
    public static class AzureEnvironment {

        private static final Logger LOGGER = LoggerFactory.getLogger(AzureEnvironment.class);

        public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment(com.azure.core.management.AzureEnvironment.AZURE_CHINA);
        public static final AzureEnvironment AZURE = new AzureEnvironment(com.azure.core.management.AzureEnvironment.AZURE);
        public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment(com.azure.core.management.AzureEnvironment.AZURE_GERMANY);
        public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment(com.azure.core.management.AzureEnvironment.AZURE_US_GOVERNMENT);


        private String portal;
        private String publishingProfile;
        private String managementEndpoint;
        private String resourceManagerEndpoint;
        private String sqlManagementEndpoint;
        private String sqlServerHostnameSuffix;
        private String galleryEndpoint;
        private String activeDirectoryEndpoint;
        private String activeDirectoryResourceId;
        private String activeDirectoryGraphEndpoint;
        private String activeDirectoryGraphApiVersion;
        private String microsoftGraphEndpoint;
        private String dataLakeEndpointResourceId;
        private String storageEndpointSuffix;
        private String keyVaultDnsSuffix;
        private String azureDataLakeStoreFileSystemEndpointSuffix;
        private String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        private String azureLogAnalyticsEndpoint;
        private String azureApplicationInsightsEndpoint;

        public AzureEnvironment() {

        }

        public AzureEnvironment(com.azure.core.management.AzureEnvironment azureEnvironment) {
            this.portal = azureEnvironment.getPortal();
            this.publishingProfile = azureEnvironment.getPublishingProfile();
            this.managementEndpoint = azureEnvironment.getManagementEndpoint();
            this.resourceManagerEndpoint = azureEnvironment.getResourceManagerEndpoint();
            this.sqlManagementEndpoint = azureEnvironment.getSqlManagementEndpoint();
            this.sqlServerHostnameSuffix = azureEnvironment.getSqlServerHostnameSuffix();
            this.galleryEndpoint = azureEnvironment.getGalleryEndpoint();
            this.activeDirectoryEndpoint = azureEnvironment.getActiveDirectoryEndpoint();
            this.activeDirectoryResourceId = azureEnvironment.getActiveDirectoryResourceId();
            this.activeDirectoryGraphEndpoint = azureEnvironment.getGraphEndpoint();
            this.activeDirectoryGraphApiVersion = azureEnvironment.getActiveDirectoryGraphApiVersion();
            this.microsoftGraphEndpoint = azureEnvironment.getMicrosoftGraphEndpoint();
            this.dataLakeEndpointResourceId = azureEnvironment.getDataLakeEndpointResourceId();
            this.storageEndpointSuffix = azureEnvironment.getStorageEndpointSuffix();
            this.keyVaultDnsSuffix = azureEnvironment.getKeyVaultDnsSuffix();
            this.azureDataLakeStoreFileSystemEndpointSuffix = azureEnvironment.getAzureDataLakeStoreFileSystemEndpointSuffix();
            this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = azureEnvironment.getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix();
            this.azureLogAnalyticsEndpoint = azureEnvironment.getLogAnalyticsEndpoint();
            this.azureApplicationInsightsEndpoint = azureEnvironment.getApplicationInsightsEndpoint();
        }

        public static AzureEnvironment fromAzureCloud(String cloud) {
            if (!StringUtils.hasText(cloud)) {
                LOGGER.warn("Cloud is empty, return the default AzureEnvironment");
                return AZURE;
            }

            switch (cloud.toUpperCase(Locale.ROOT)) {
                case "AZURE_CHINA":
                    return AZURE_CHINA;
                case "AZURE_US_GOVERNMENT":
                    return AZURE_US_GOVERNMENT;
                case "AZURE_GERMANY":
                    return AZURE_GERMANY;
                default:
                    return AZURE;
            }
        }

        public Map<String, String> exportEndpointsMap() {
            return new HashMap<String, String>() {
                {
                    put("portalUrl", portal);
                    put("publishingProfileUrl", publishingProfile);
                    put("managementEndpointUrl", managementEndpoint);
                    put("resourceManagerEndpointUrl", resourceManagerEndpoint);
                    put("sqlManagementEndpointUrl", sqlManagementEndpoint);
                    put("sqlServerHostnameSuffix", sqlServerHostnameSuffix);
                    put("galleryEndpointUrl", galleryEndpoint);
                    put("activeDirectoryEndpointUrl", activeDirectoryEndpoint);
                    put("activeDirectoryResourceId", activeDirectoryResourceId);
                    put("activeDirectoryGraphResourceId", activeDirectoryGraphEndpoint);
                    put("microsoftGraphResourceId", microsoftGraphEndpoint);
                    put("dataLakeEndpointResourceId", dataLakeEndpointResourceId);
                    put("activeDirectoryGraphApiVersion", activeDirectoryGraphApiVersion);
                    put("storageEndpointSuffix", storageEndpointSuffix);
                    put("keyVaultDnsSuffix", keyVaultDnsSuffix);
                    put("azureDataLakeStoreFileSystemEndpointSuffix", azureDataLakeStoreFileSystemEndpointSuffix);
                    put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", azureDataLakeAnalyticsCatalogAndJobEndpointSuffix);
                    put("azureLogAnalyticsResourceId", azureLogAnalyticsEndpoint);
                    put("azureApplicationInsightsResourceId", azureApplicationInsightsEndpoint);
                }
            };
        }

        public String getPortal() {
            return portal;
        }

        public void setPortal(String portal) {
            this.portal = portal;
        }

        public String getPublishingProfile() {
            return publishingProfile;
        }

        public void setPublishingProfile(String publishingProfile) {
            this.publishingProfile = publishingProfile;
        }

        public String getManagementEndpoint() {
            return managementEndpoint;
        }

        public void setManagementEndpoint(String managementEndpoint) {
            this.managementEndpoint = managementEndpoint;
        }

        public String getResourceManagerEndpoint() {
            return resourceManagerEndpoint;
        }

        public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
            this.resourceManagerEndpoint = resourceManagerEndpoint;
        }

        public String getSqlManagementEndpoint() {
            return sqlManagementEndpoint;
        }

        public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
            this.sqlManagementEndpoint = sqlManagementEndpoint;
        }

        public String getSqlServerHostnameSuffix() {
            return sqlServerHostnameSuffix;
        }

        public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
            this.sqlServerHostnameSuffix = sqlServerHostnameSuffix;
        }

        public String getGalleryEndpoint() {
            return galleryEndpoint;
        }

        public void setGalleryEndpoint(String galleryEndpoint) {
            this.galleryEndpoint = galleryEndpoint;
        }

        public String getActiveDirectoryEndpoint() {
            return activeDirectoryEndpoint;
        }

        public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
            this.activeDirectoryEndpoint = activeDirectoryEndpoint;
        }

        public String getActiveDirectoryResourceId() {
            return activeDirectoryResourceId;
        }

        public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
            this.activeDirectoryResourceId = activeDirectoryResourceId;
        }

        public String getActiveDirectoryGraphEndpoint() {
            return activeDirectoryGraphEndpoint;
        }

        public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
            this.activeDirectoryGraphEndpoint = activeDirectoryGraphEndpoint;
        }

        public String getMicrosoftGraphEndpoint() {
            return microsoftGraphEndpoint;
        }

        public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
            this.microsoftGraphEndpoint = microsoftGraphEndpoint;
        }

        public String getDataLakeEndpointResourceId() {
            return dataLakeEndpointResourceId;
        }

        public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
            this.dataLakeEndpointResourceId = dataLakeEndpointResourceId;
        }

        public String getActiveDirectoryGraphApiVersion() {
            return activeDirectoryGraphApiVersion;
        }

        public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
            this.activeDirectoryGraphApiVersion = activeDirectoryGraphApiVersion;
        }

        public String getStorageEndpointSuffix() {
            return storageEndpointSuffix;
        }

        public void setStorageEndpointSuffix(String storageEndpointSuffix) {
            this.storageEndpointSuffix = storageEndpointSuffix;
        }

        public String getKeyVaultDnsSuffix() {
            return keyVaultDnsSuffix;
        }

        public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
            this.keyVaultDnsSuffix = keyVaultDnsSuffix;
        }

        public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
            return azureDataLakeStoreFileSystemEndpointSuffix;
        }

        public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
            this.azureDataLakeStoreFileSystemEndpointSuffix = azureDataLakeStoreFileSystemEndpointSuffix;
        }

        public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
            return azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
            this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        public String getAzureLogAnalyticsEndpoint() {
            return azureLogAnalyticsEndpoint;
        }

        public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
            this.azureLogAnalyticsEndpoint = azureLogAnalyticsEndpoint;
        }

        public String getAzureApplicationInsightsEndpoint() {
            return azureApplicationInsightsEndpoint;
        }

        public void setAzureApplicationInsightsEndpoint(String azureApplicationInsightsEndpoint) {
            this.azureApplicationInsightsEndpoint = azureApplicationInsightsEndpoint;
        }
    }
}
