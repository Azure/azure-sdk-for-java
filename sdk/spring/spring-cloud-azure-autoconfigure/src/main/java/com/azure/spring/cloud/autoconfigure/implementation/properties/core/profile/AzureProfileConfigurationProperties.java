// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.properties.profile.AzureProfileOptionsAdapter;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

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
     * Name of the Azure cloud to connect to. Supported types are: AZURE, AZURE_CHINA, AZURE_US_GOVERNMENT, OTHER. The default value is `AZURE`.
     */
    private AzureProfileOptionsProvider.CloudType cloudType;

    private final AzureEnvironmentConfigurationProperties environment = new AzureEnvironmentConfigurationProperties(null);

    @Override
    public AzureProfileOptionsProvider.CloudType getCloudType() {
        return cloudType;
    }

    public void setCloudType(AzureProfileOptionsProvider.CloudType cloudType) {
        this.cloudType = cloudType;

        // Explicitly call this method to merge default cloud endpoints to the environment object.
        changeEnvironmentAccordingToCloud();
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public AzureEnvironmentConfigurationProperties getEnvironment() {
        return this.environment;
    }

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

        /**
         * The domain name for Service Bus.
         */
        private String serviceBusDomainName;

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

        @Override
        public String getServiceBusDomainName() {
            return serviceBusDomainName;
        }

        /**
         * Set the Service Bus domain name.
         * @param serviceBusDomainName the Service Bus domain name.
         */
        public void setServiceBusDomainName(String serviceBusDomainName) {
            this.serviceBusDomainName = serviceBusDomainName;
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

        @SuppressWarnings("deprecation")
        @Override
        public AzureProfileOptionsProvider.AzureEnvironmentOptions fromAzureManagementEnvironment(AzureEnvironment environment) {
            return new AzureEnvironmentConfigurationProperties(environment);
        }

    }

}
