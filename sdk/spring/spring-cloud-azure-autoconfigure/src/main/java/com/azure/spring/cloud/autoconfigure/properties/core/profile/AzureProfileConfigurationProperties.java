// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.profile;

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
     * Azure environment configuration properties.
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

        /**
         * The domain name for Service Bus.
         */
        private String serviceBusDomainName;

        @Override
        public String getPortal() {
            return portal;
        }

        /**
         * Sets the portal.
         *
         * @param portal The portal.
         */
        public void setPortal(String portal) {
            this.portal = portal;
        }

        @Override
        public String getPublishingProfile() {
            return publishingProfile;
        }

        /**
         * Sets the publishing profile.
         *
         * @param publishingProfile The publishing profile.
         */
        public void setPublishingProfile(String publishingProfile) {
            this.publishingProfile = publishingProfile;
        }

        @Override
        public String getManagementEndpoint() {
            return managementEndpoint;
        }

        /**
         * Sets the management endpoint.
         *
         * @param managementEndpoint The management endpoint.
         */
        public void setManagementEndpoint(String managementEndpoint) {
            this.managementEndpoint = managementEndpoint;
        }

        @Override
        public String getResourceManagerEndpoint() {
            return resourceManagerEndpoint;
        }

        /**
         * Sets the resource manager endpoint.
         *
         * @param resourceManagerEndpoint The resource manager endpoint.
         */
        public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
            this.resourceManagerEndpoint = resourceManagerEndpoint;
        }

        @Override
        public String getSqlManagementEndpoint() {
            return sqlManagementEndpoint;
        }

        /**
         * Sets the SQL management endpoint.
         *
         * @param sqlManagementEndpoint The SQL management endpoint.
         */
        public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
            this.sqlManagementEndpoint = sqlManagementEndpoint;
        }

        @Override
        public String getSqlServerHostnameSuffix() {
            return sqlServerHostnameSuffix;
        }

        /**
         * Sets the SQL server hostname suffix.
         *
         * @param sqlServerHostnameSuffix The SQL server hostname suffix.
         */
        public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
            this.sqlServerHostnameSuffix = sqlServerHostnameSuffix;
        }

        @Override
        public String getGalleryEndpoint() {
            return galleryEndpoint;
        }

        /**
         * Sets the gallery endpoint.
         *
         * @param galleryEndpoint The gallery endpoint.
         */
        public void setGalleryEndpoint(String galleryEndpoint) {
            this.galleryEndpoint = galleryEndpoint;
        }

        @Override
        public String getActiveDirectoryEndpoint() {
            return activeDirectoryEndpoint;
        }

        /**
         * Sets the active directory endpoint.
         *
         * @param activeDirectoryEndpoint The active directory endpoint.
         */
        public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
            this.activeDirectoryEndpoint = activeDirectoryEndpoint;
        }

        @Override
        public String getActiveDirectoryResourceId() {
            return activeDirectoryResourceId;
        }

        /**
         * Sets the active directory resource ID.
         *
         * @param activeDirectoryResourceId The active directory resource ID.
         */
        public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
            this.activeDirectoryResourceId = activeDirectoryResourceId;
        }

        @Override
        public String getActiveDirectoryGraphEndpoint() {
            return activeDirectoryGraphEndpoint;
        }

        /**
         * Sets the active directory graph endpoint.
         *
         * @param activeDirectoryGraphEndpoint The active directory graph endpoint.
         */
        public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
            this.activeDirectoryGraphEndpoint = activeDirectoryGraphEndpoint;
        }

        @Override
        public String getActiveDirectoryGraphApiVersion() {
            return activeDirectoryGraphApiVersion;
        }

        /**
         * Sets the active directory graph API version.
         *
         * @param activeDirectoryGraphApiVersion The active directory graph API version.
         */
        public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
            this.activeDirectoryGraphApiVersion = activeDirectoryGraphApiVersion;
        }

        @Override
        public String getMicrosoftGraphEndpoint() {
            return microsoftGraphEndpoint;
        }

        /**
         * Sets the Microsoft Graph endpoint.
         *
         * @param microsoftGraphEndpoint The Microsoft Graph endpoint.
         */
        public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
            this.microsoftGraphEndpoint = microsoftGraphEndpoint;
        }

        @Override
        public String getDataLakeEndpointResourceId() {
            return dataLakeEndpointResourceId;
        }

        /**
         * Sets the Data Lake endpoint resource ID.
         *
         * @param dataLakeEndpointResourceId The Data Lake endpoint resource ID.
         */
        public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
            this.dataLakeEndpointResourceId = dataLakeEndpointResourceId;
        }

        @Override
        public String getStorageEndpointSuffix() {
            return storageEndpointSuffix;
        }

        /**
         * Sets the Storage endpoint suffix.
         *
         * @param storageEndpointSuffix The Storage endpoint suffix.
         */
        public void setStorageEndpointSuffix(String storageEndpointSuffix) {
            this.storageEndpointSuffix = storageEndpointSuffix;
        }

        @Override
        public String getKeyVaultDnsSuffix() {
            return keyVaultDnsSuffix;
        }

        /**
         * Sets the KeyVault DNS suffix.
         *
         * @param keyVaultDnsSuffix The KeyVault DNS suffix.
         */
        public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
            this.keyVaultDnsSuffix = keyVaultDnsSuffix;
        }

        @Override
        public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
            return azureDataLakeStoreFileSystemEndpointSuffix;
        }

        /**
         * Sets the Azure Data Lake Storage file system endpoint suffix.
         *
         * @param azureDataLakeStoreFileSystemEndpointSuffix The Azure Data Lake Storage file system endpoint suffix.
         */
        public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
            this.azureDataLakeStoreFileSystemEndpointSuffix = azureDataLakeStoreFileSystemEndpointSuffix;
        }

        @Override
        public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
            return azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        /**
         * Sets the Azure Data Lake analytics catalog and job endpoint suffix.
         *
         * @param azureDataLakeAnalyticsCatalogAndJobEndpointSuffix The Azure Data Lake analytics catalog and job
         * endpoint suffix.
         */
        public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
            this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
        }

        @Override
        public String getAzureLogAnalyticsEndpoint() {
            return azureLogAnalyticsEndpoint;
        }

        /**
         * Sets the Azure log analytics endpoint.
         *
         * @param azureLogAnalyticsEndpoint The Azure log analytics endpoint.
         */
        public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
            this.azureLogAnalyticsEndpoint = azureLogAnalyticsEndpoint;
        }

        @Override
        public String getAzureApplicationInsightsEndpoint() {
            return azureApplicationInsightsEndpoint;
        }

        /**
         * Sets the Azure Application Insights endpoint.
         *
         * @param azureApplicationInsightsEndpoint The Azure Application Insights endpoint.
         */
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

        private AzureEnvironmentConfigurationProperties(com.azure.core.management.AzureEnvironment management) {
            if (management == null) {
                return;
            }
            this.portal = management.getPortal();
            this.publishingProfile = management.getPublishingProfile();
            this.managementEndpoint = management.getManagementEndpoint();
            this.resourceManagerEndpoint = management.getResourceManagerEndpoint();
            this.sqlManagementEndpoint = management.getSqlManagementEndpoint();
            this.sqlServerHostnameSuffix = management.getSqlServerHostnameSuffix();
            this.galleryEndpoint = management.getGalleryEndpoint();
            this.activeDirectoryEndpoint = management.getActiveDirectoryEndpoint();
            this.activeDirectoryResourceId = management.getActiveDirectoryResourceId();
            this.activeDirectoryGraphEndpoint = management.getGraphEndpoint();
            this.activeDirectoryGraphApiVersion = management.getActiveDirectoryGraphApiVersion();
            this.microsoftGraphEndpoint = management.getMicrosoftGraphEndpoint();
            this.dataLakeEndpointResourceId = management.getDataLakeEndpointResourceId();
            this.storageEndpointSuffix = management.getStorageEndpointSuffix();
            this.keyVaultDnsSuffix = management.getKeyVaultDnsSuffix();
            this.azureDataLakeStoreFileSystemEndpointSuffix = management.getAzureDataLakeStoreFileSystemEndpointSuffix();
            this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = management.getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix();
            this.azureLogAnalyticsEndpoint = management.getLogAnalyticsEndpoint();
            this.azureApplicationInsightsEndpoint = management.getApplicationInsightsEndpoint();
        }


        @Override
        public AzureProfileOptionsProvider.AzureEnvironmentOptions fromAzureManagementEnvironment(AzureEnvironment environment) {
            return new AzureEnvironmentConfigurationProperties(environment);
        }

    }

}
