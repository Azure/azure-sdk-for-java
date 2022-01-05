// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.profile;

import com.azure.spring.core.aware.AzureProfileAware;

import java.util.HashMap;
import java.util.Map;

/**
 * The AzureEnvironment defines all properties to Azure services, such as endpoints, resource ids, etc.
 */
public class AzureEnvironment implements AzureProfileAware.Environment {

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
    private String resourceManagerEndpoint;
    private String sqlManagementEndpoint;
    private String sqlServerHostnameSuffix;
    private String galleryEndpoint;
    /**
     * The Azure Active Directory endpoint to connect to.
     */
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

    /**
     * Create an {@link AzureEnvironment} instance with default value.
     */
    public AzureEnvironment() {

    }

    /**
     * Create an {@link AzureEnvironment} instance with environment value from {@link com.azure.core.management.AzureEnvironment}.
     * @param azureEnvironment The {@link com.azure.core.management.AzureEnvironment} instance.
     */
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

    /**
     * Convert to a {@link com.azure.core.management.AzureEnvironment} instance.
     * @return The {@link com.azure.core.management.AzureEnvironment} instance.
     */
    public com.azure.core.management.AzureEnvironment toManagementAzureEnvironment() {
        return new com.azure.core.management.AzureEnvironment(exportEndpointsMap());
    }

    private Map<String, String> exportEndpointsMap() {
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

    /**
     * @return The management portal URL.
     */
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

    /**
     * @return the publishing settings file URL.
     */
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

    /**
     * @return the management service endpoint.
     */
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

    /**
     * @return the resource management endpoint.
     */
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

    /**
     * @return the sql server management endpoint for mobile commands.
     */
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

    /**
     * @return the dns suffix for sql servers.
     */
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

    /**
     * @return the template gallery endpoint.
     */
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

    /**
     * @return the Active Directory login endpoint.
     */
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

    /**
     * @return The resource ID to obtain AD tokens for.
     */
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

    /**
     * @return the Active Directory Graph endpoint.
     */
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

    /**
     * @return the Microsoft Graph endpoint.
     */
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

    /**
     * @return the Data Lake resource ID.
     */
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

    /**
     * @return the Active Directory api version.
     */
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

    /**
     * The endpoint suffix for storage accounts.
     * @return the endpoint suffix for storage accounts.
     */
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

    /**
     * @return the key vault service dns suffix.
     */
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

    /**
     * @return the data lake store filesystem service dns suffix.
     */
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

    /**
     * @return the data lake analytics job and catalog service dns suffix.
     */
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

    /**
     * @return the log analytics endpoint.
     */
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

    /**
     * @return the application insights endpoint.
     */
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
