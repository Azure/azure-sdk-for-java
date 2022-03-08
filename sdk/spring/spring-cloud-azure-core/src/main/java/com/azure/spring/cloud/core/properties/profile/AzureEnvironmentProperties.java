// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.profile;

import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

/**
 * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
 */
public class AzureEnvironmentProperties implements AzureProfileOptionsProvider.AzureEnvironmentOptions {

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
     * The Azure Active Directory resource id.
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
     * Create an {@link AzureEnvironmentProperties} instance with default value.
     */
    public AzureEnvironmentProperties() {

    }

    /**
     * Create an {@link AzureEnvironmentProperties} instance with environment value from {@link com.azure.core.management.AzureEnvironment}.
     * @param azureEnvironment The {@link com.azure.core.management.AzureEnvironment} instance.
     */
    private AzureEnvironmentProperties(com.azure.core.management.AzureEnvironment azureEnvironment) {
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
    public AzureEnvironmentProperties fromAzureManagementEnvironment(com.azure.core.management.AzureEnvironment environment) {
        return new AzureEnvironmentProperties(environment);
    }

    @Override
    public String getPortal() {
        return portal;
    }

    /**
     * Set the management portal URL.
     * @param portal The management portal URL.
     */
    public void setPortal(String portal) {
        this.portal = portal;
    }

    @Override
    public String getPublishingProfile() {
        return publishingProfile;
    }

    /**
     * Set the publishing settings file URL.
     * @param publishingProfile the publishing settings file URL.
     */
    public void setPublishingProfile(String publishingProfile) {
        this.publishingProfile = publishingProfile;
    }

    @Override
    public String getManagementEndpoint() {
        return managementEndpoint;
    }

    /**
     * Set the management service endpoint.
     * @param managementEndpoint the management service endpoint.
     */
    public void setManagementEndpoint(String managementEndpoint) {
        this.managementEndpoint = managementEndpoint;
    }

    @Override
    public String getResourceManagerEndpoint() {
        return resourceManagerEndpoint;
    }

    /**
     * Set the resource management endpoint.
     * @param resourceManagerEndpoint the resource management endpoint.
     */
    public void setResourceManagerEndpoint(String resourceManagerEndpoint) {
        this.resourceManagerEndpoint = resourceManagerEndpoint;
    }

    @Override
    public String getSqlManagementEndpoint() {
        return sqlManagementEndpoint;
    }

    /**
     * Set the sql server management endpoint for mobile commands.
     * @param sqlManagementEndpoint the sql server management endpoint for mobile commands.
     */
    public void setSqlManagementEndpoint(String sqlManagementEndpoint) {
        this.sqlManagementEndpoint = sqlManagementEndpoint;
    }

    @Override
    public String getSqlServerHostnameSuffix() {
        return sqlServerHostnameSuffix;
    }

    /**
     * Set the dns suffix for sql servers.
     * @param sqlServerHostnameSuffix the dns suffix for sql servers.
     */
    public void setSqlServerHostnameSuffix(String sqlServerHostnameSuffix) {
        this.sqlServerHostnameSuffix = sqlServerHostnameSuffix;
    }

    @Override
    public String getGalleryEndpoint() {
        return galleryEndpoint;
    }

    /**
     * Set the template gallery endpoint.
     * @param galleryEndpoint the template gallery endpoint.
     */
    public void setGalleryEndpoint(String galleryEndpoint) {
        this.galleryEndpoint = galleryEndpoint;
    }

    @Override
    public String getActiveDirectoryEndpoint() {
        return activeDirectoryEndpoint;
    }

    /**
     * Set the Active Directory login endpoint.
     * @param activeDirectoryEndpoint the Active Directory login endpoint.
     */
    public void setActiveDirectoryEndpoint(String activeDirectoryEndpoint) {
        this.activeDirectoryEndpoint = activeDirectoryEndpoint;
    }

    @Override
    public String getActiveDirectoryResourceId() {
        return activeDirectoryResourceId;
    }

    /**
     * Set the resource ID to obtain AD tokens for.
     * @param activeDirectoryResourceId The resource ID to obtain AD tokens for.
     */
    public void setActiveDirectoryResourceId(String activeDirectoryResourceId) {
        this.activeDirectoryResourceId = activeDirectoryResourceId;
    }

    @Override
    public String getActiveDirectoryGraphEndpoint() {
        return activeDirectoryGraphEndpoint;
    }

    /**
     * Set the Active Directory Graph endpoint.
     * @param activeDirectoryGraphEndpoint the Active Directory Graph endpoint.
     */
    public void setActiveDirectoryGraphEndpoint(String activeDirectoryGraphEndpoint) {
        this.activeDirectoryGraphEndpoint = activeDirectoryGraphEndpoint;
    }

    @Override
    public String getMicrosoftGraphEndpoint() {
        return microsoftGraphEndpoint;
    }

    /**
     * Set the Microsoft Graph endpoint.
     * @param microsoftGraphEndpoint the Microsoft Graph endpoint.
     */
    public void setMicrosoftGraphEndpoint(String microsoftGraphEndpoint) {
        this.microsoftGraphEndpoint = microsoftGraphEndpoint;
    }

    @Override
    public String getDataLakeEndpointResourceId() {
        return dataLakeEndpointResourceId;
    }

    /**
     * Set the Data Lake resource ID.
     * @param dataLakeEndpointResourceId the Data Lake resource ID.
     */
    public void setDataLakeEndpointResourceId(String dataLakeEndpointResourceId) {
        this.dataLakeEndpointResourceId = dataLakeEndpointResourceId;
    }

    @Override
    public String getActiveDirectoryGraphApiVersion() {
        return activeDirectoryGraphApiVersion;
    }

    /**
     * Set the Active Directory api version.
     * @param activeDirectoryGraphApiVersion the Active Directory api version.
     */
    public void setActiveDirectoryGraphApiVersion(String activeDirectoryGraphApiVersion) {
        this.activeDirectoryGraphApiVersion = activeDirectoryGraphApiVersion;
    }

    @Override
    public String getStorageEndpointSuffix() {
        return storageEndpointSuffix;
    }

    /**
     * Set the endpoint suffix for storage accounts.
     * @param storageEndpointSuffix the endpoint suffix for storage accounts.
     */
    public void setStorageEndpointSuffix(String storageEndpointSuffix) {
        this.storageEndpointSuffix = storageEndpointSuffix;
    }

    @Override
    public String getKeyVaultDnsSuffix() {
        return keyVaultDnsSuffix;
    }

    /**
     * Set the key vault service dns suffix.
     * @param keyVaultDnsSuffix the key vault service dns suffix.
     */
    public void setKeyVaultDnsSuffix(String keyVaultDnsSuffix) {
        this.keyVaultDnsSuffix = keyVaultDnsSuffix;
    }

    @Override
    public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
        return azureDataLakeStoreFileSystemEndpointSuffix;
    }

    /**
     * Set the data lake store filesystem service dns suffix.
     * @param azureDataLakeStoreFileSystemEndpointSuffix the data lake store filesystem service dns suffix.
     */
    public void setAzureDataLakeStoreFileSystemEndpointSuffix(String azureDataLakeStoreFileSystemEndpointSuffix) {
        this.azureDataLakeStoreFileSystemEndpointSuffix = azureDataLakeStoreFileSystemEndpointSuffix;
    }

    @Override
    public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
        return azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
    }

    /**
     * Set the data lake analytics job and catalog service dns suffix.
     * @param azureDataLakeAnalyticsCatalogAndJobEndpointSuffix the data lake analytics job and catalog service dns suffix.
     */
    public void setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix) {
        this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
    }

    @Override
    public String getAzureLogAnalyticsEndpoint() {
        return azureLogAnalyticsEndpoint;
    }

    /**
     * Set the log analytics endpoint.
     * @param azureLogAnalyticsEndpoint the log analytics endpoint.
     */
    public void setAzureLogAnalyticsEndpoint(String azureLogAnalyticsEndpoint) {
        this.azureLogAnalyticsEndpoint = azureLogAnalyticsEndpoint;
    }

    @Override
    public String getAzureApplicationInsightsEndpoint() {
        return azureApplicationInsightsEndpoint;
    }

    /**
     * Set the application insights endpoint.
     * @param azureApplicationInsightsEndpoint the application insights endpoint.
     */
    public void setAzureApplicationInsightsEndpoint(String azureApplicationInsightsEndpoint) {
        this.azureApplicationInsightsEndpoint = azureApplicationInsightsEndpoint;
    }

}
