// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.Objects;

/**
 * The AzureProfile defines the properties related to an Azure subscription.
 */
public class AzureProfileConfigurationProperties extends AzureProfileOptionsAdapter {

    /**
     * Tenant ID for Azure resources.
     */
    private String tenantId;
    /**
     * Subscription ID to use when connecting to Azure resources.
     */
    private String subscriptionId;
    /**
     * Name of the Azure cloud to connect to.
     */
    private AzureProfileOptionsProvider.CloudType cloudType = AzureProfileOptionsProvider.CloudType.AZURE;

    private final AzureEnvironmentConfigurationProperties environment = new AzureEnvironmentConfigurationProperties(AzureEnvironment.AZURE);

    @Override
    public AzureProfileOptionsProvider.CloudType getCloudType() {
        return cloudType;
    }

    /**
     * Set the cloud type.
     * @param cloudType the cloud type.
     */
    public void setCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        this.cloudType = cloudType;

        // Explicitly call this method to merge default cloud endpoints to the environment object.
        changeEnvironmentAccordingToCloud();
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Set the tenant ID.
     * @param tenantId The tenant ID.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Set the subscription ID.
     * @param subscriptionId The subscription ID.
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public AzureEnvironmentConfigurationProperties getEnvironment() {
        return this.environment;
    }

    /**
     *
     */
    public static final class AzureEnvironmentConfigurationProperties implements AzureProfileOptionsProvider.AzureEnvironmentOptions {
        /**
         * The management portal URL.
         */
        private String portal;
        /**
         * The publishing settings file URL.
         */
        private String publishingProfile;
        /**
         * The management service endpoint.
         */
        private String managementEndpoint;
        /**
         * The resource management endpoint.
         */
        private String resourceManagerEndpoint;
        /**
         * The SQL management endpoint.
         */
        private String sqlManagementEndpoint;
        /**
         * The SQL Server hostname suffix.
         */
        private String sqlServerHostnameSuffix;
        /**
         * The gallery endpoint.
         */
        private String galleryEndpoint;
        /**
         * The Azure Active Directory endpoint to connect to.
         */
        private String activeDirectoryEndpoint;
        /**
         * The Azure Active Directory resource ID.
         */
        private String activeDirectoryResourceId;
        /**
         * The Azure Active Directory Graph endpoint.
         */
        private String activeDirectoryGraphEndpoint;
        /**
         * The Azure Active Directory Graph API version.
         */
        private String activeDirectoryGraphApiVersion;
        /**
         * The Microsoft Graph endpoint.
         */
        private String microsoftGraphEndpoint;
        /**
         * The Data Lake endpoint.
         */
        private String dataLakeEndpointResourceId;
        /**
         * The Storage endpoint suffix.
         */
        private String storageEndpointSuffix;
        /**
         * The Key Vault DNS suffix.
         */
        private String keyVaultDnsSuffix;
        /**
         * The Data Lake storage file system endpoint suffix.
         */
        private String azureDataLakeStoreFileSystemEndpointSuffix;
        /**
         * The Data Lake analytics catalog and job endpoint suffix.
         */
        private String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        /**
         * The Azure Log Analytics endpoint.
         */
        private String azureLogAnalyticsEndpoint;
        /**
         * The Azure Application Insights endpoint.
         */
        private String azureApplicationInsightsEndpoint;

        @Override
        public String getPortal() {
            return portal;
        }

        public void setPortal(String portal) {
            this.portal = portal;
        }

        @Override
        public String getPublishingProfile() {
            return publishingProfile;
        }

        public void setPublishingProfile(String publishingProfile) {
            this.publishingProfile = publishingProfile;
        }

        @Override
        public String getManagementEndpoint() {
            return managementEndpoint;
        }

        public void setManagementEndpoint(String managementEndpoint) {
            this.managementEndpoint = managementEndpoint;
        }

        @Override
        public String getResourceManagerEndpoint() {
            return resourceManagerEndpoint;
        }

        public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
            this.resourceManagerEndpoint = resourceManagerEndpoint;
        }

        @Override
        public String getSqlManagementEndpoint() {
            return sqlManagementEndpoint;
        }

        public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
            this.sqlManagementEndpoint = sqlManagementEndpoint;
        }

        @Override
        public String getSqlServerHostnameSuffix() {
            return sqlServerHostnameSuffix;
        }

        public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
            this.sqlServerHostnameSuffix = sqlServerHostnameSuffix;
        }

        @Override
        public String getGalleryEndpoint() {
            return galleryEndpoint;
        }

        public void setGalleryEndpoint(String galleryEndpoint) {
            this.galleryEndpoint = galleryEndpoint;
        }

        @Override
        public String getActiveDirectoryEndpoint() {
            return activeDirectoryEndpoint;
        }

        public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
            this.activeDirectoryEndpoint = activeDirectoryEndpoint;
        }

        @Override
        public String getActiveDirectoryResourceId() {
            return activeDirectoryResourceId;
        }

        public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
            this.activeDirectoryResourceId = activeDirectoryResourceId;
        }

        @Override
        public String getActiveDirectoryGraphEndpoint() {
            return activeDirectoryGraphEndpoint;
        }

        public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
            this.activeDirectoryGraphEndpoint = activeDirectoryGraphEndpoint;
        }

        @Override
        public String getActiveDirectoryGraphApiVersion() {
            return activeDirectoryGraphApiVersion;
        }

        public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
            this.activeDirectoryGraphApiVersion = activeDirectoryGraphApiVersion;
        }

        @Override
        public String getMicrosoftGraphEndpoint() {
            return microsoftGraphEndpoint;
        }

        public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
            this.microsoftGraphEndpoint = microsoftGraphEndpoint;
        }

        @Override
        public String getDataLakeEndpointResourceId() {
            return dataLakeEndpointResourceId;
        }

        public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
            this.dataLakeEndpointResourceId = dataLakeEndpointResourceId;
        }

        @Override
        public String getStorageEndpointSuffix() {
            return storageEndpointSuffix;
        }

        public void setStorageEndpointSuffix(String storageEndpointSuffix) {
            this.storageEndpointSuffix = storageEndpointSuffix;
        }

        @Override
        public String getKeyVaultDnsSuffix() {
            return keyVaultDnsSuffix;
        }

        public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
            this.keyVaultDnsSuffix = keyVaultDnsSuffix;
        }

        @Override
        public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
            return azureDataLakeStoreFileSystemEndpointSuffix;
        }

        public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
            this.azureDataLakeStoreFileSystemEndpointSuffix = azureDataLakeStoreFileSystemEndpointSuffix;
        }

        @Override
        public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
            return azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
            this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        @Override
        public String getAzureLogAnalyticsEndpoint() {
            return azureLogAnalyticsEndpoint;
        }

        public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
            this.azureLogAnalyticsEndpoint = azureLogAnalyticsEndpoint;
        }

        @Override
        public String getAzureApplicationInsightsEndpoint() {
            return azureApplicationInsightsEndpoint;
        }

        public void setAzureApplicationInsightsEndpoint(String azureApplicationInsightsEndpoint) {
            this.azureApplicationInsightsEndpoint = azureApplicationInsightsEndpoint;
        }

        private AzureEnvironmentConfigurationProperties(com.azure.core.management.AzureEnvironment azureEnvironment) {
            if (azureEnvironment == null) {
                return;
            }
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

        @Override
        public AzureProfileOptionsProvider.AzureEnvironmentOptions fromAzureManagementEnvironment(AzureEnvironment environment) {
            return new AzureEnvironmentConfigurationProperties(environment);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AzureEnvironmentConfigurationProperties that = (AzureEnvironmentConfigurationProperties) o;
            return Objects.equals(portal, that.portal)
                    && Objects.equals(publishingProfile, that.publishingProfile)
                    && Objects.equals(managementEndpoint, that.managementEndpoint)
                    && Objects.equals(resourceManagerEndpoint, that.resourceManagerEndpoint)
                    && Objects.equals(sqlManagementEndpoint, that.sqlManagementEndpoint)
                    && Objects.equals(sqlServerHostnameSuffix, that.sqlServerHostnameSuffix)
                    && Objects.equals(galleryEndpoint, that.galleryEndpoint)
                    && Objects.equals(activeDirectoryEndpoint, that.activeDirectoryEndpoint)
                    && Objects.equals(activeDirectoryResourceId, that.activeDirectoryResourceId)
                    && Objects.equals(activeDirectoryGraphEndpoint, that.activeDirectoryGraphEndpoint)
                    && Objects.equals(activeDirectoryGraphApiVersion, that.activeDirectoryGraphApiVersion)
                    && Objects.equals(microsoftGraphEndpoint, that.microsoftGraphEndpoint)
                    && Objects.equals(dataLakeEndpointResourceId, that.dataLakeEndpointResourceId)
                    && Objects.equals(storageEndpointSuffix, that.storageEndpointSuffix)
                    && Objects.equals(keyVaultDnsSuffix, that.keyVaultDnsSuffix)
                    && Objects.equals(azureDataLakeStoreFileSystemEndpointSuffix,
                    that.azureDataLakeStoreFileSystemEndpointSuffix)
                    && Objects.equals(azureDataLakeAnalyticsCatalogAndJobEndpointSuffix,
                    that.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix)
                    && Objects.equals(azureLogAnalyticsEndpoint, that.azureLogAnalyticsEndpoint)
                    && Objects.equals(azureApplicationInsightsEndpoint, that.azureApplicationInsightsEndpoint);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AzureProfileConfigurationProperties)) return false;
        AzureProfileConfigurationProperties that = (AzureProfileConfigurationProperties) o;
        return Objects.equals(tenantId, that.tenantId)
                && Objects.equals(subscriptionId, that.subscriptionId)
                && cloudType == that.cloudType && Objects.equals(environment, that.environment);
    }

}
