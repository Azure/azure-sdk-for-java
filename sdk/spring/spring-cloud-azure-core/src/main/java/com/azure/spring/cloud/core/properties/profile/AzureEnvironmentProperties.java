// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.profile;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

/**
 * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
 */
public class AzureEnvironmentProperties implements AzureProfileOptionsProvider.AzureEnvironmentOptions {

    /**
     * Provides the environment instance for the public Azure.
     */
    public static final AzureEnvironmentProperties AZURE = new AzureEnvironmentProperties(AzureEnvironment.AZURE) {{
            setServiceBusDomainName("servicebus.windows.net");
        }};

    /**
     * Provides the environment instance for Azure China.
     */
    public static final AzureEnvironmentProperties AZURE_CHINA = new AzureEnvironmentProperties(AzureEnvironment.AZURE_CHINA) {{
            setServiceBusDomainName("servicebus.chinacloudapi.cn");
        }};

    /**
     * Provides the environment instance for Azure US Government.
     */
    public static final AzureEnvironmentProperties AZURE_US_GOVERNMENT = new AzureEnvironmentProperties(AzureEnvironment.AZURE_US_GOVERNMENT) {{
            setServiceBusDomainName("servicebus.usgovcloudapi.net");
        }};

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
     * The domain name for Service Bus.
     */
    private String serviceBusDomainName;

    /**
     * Create an {@link AzureEnvironmentProperties} instance with default value.
     */
    public AzureEnvironmentProperties() {
        this(null);
    }

    /**
     * Create an {@link AzureEnvironmentProperties} instance with environment value from {@link com.azure.core.management.AzureEnvironment}.
     * @param management The {@link com.azure.core.management.AzureEnvironment} instance.
     */
    private AzureEnvironmentProperties(com.azure.core.management.AzureEnvironment management) {
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

    @SuppressWarnings("deprecation")
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

    @Override
    public String getServiceBusDomainName() {
        return serviceBusDomainName;
    }

    /**
     * Set the domain name of Service Bus.
     * @param serviceBusDomainName the Service Bus domain name.
     */
    public void setServiceBusDomainName(String serviceBusDomainName) {
        this.serviceBusDomainName = serviceBusDomainName;
    }
}
