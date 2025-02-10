// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class describes an environment in Azure.
 */
public final class AzureEnvironment {

    /** the map of all endpoints. */
    private final Map<String, String> endpoints;

    /**
     * Initializes an instance of AzureEnvironment class.
     *
     * @param endpoints a map storing all the endpoint info
     */
    public AzureEnvironment(Map<String, String> endpoints) {
        this.endpoints = endpoints;
    }

    private AzureEnvironment(String portalUrl, String publishingProfileUrl, String managementEndpointUrl,
        String resourceManagerEndpointUrl, String sqlManagementEndpointUrl, String sqlServerHostnameSuffix,
        String galleryEndpointUrl, String activeDirectoryEndpointUrl, String activeDirectoryResourceId,
        String activeDirectoryGraphResourceId, String microsoftGraphResourceId, String dataLakeEndpointResourceId,
        String activeDirectoryGraphApiVersion, String storageEndpointSuffix, String keyVaultDnsSuffix,
        String azureDataLakeStoreFileSystemEndpointSuffix, String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix,
        String azureLogAnalyticsResourceId, String azureApplicationInsightsResourceId, String managedHsmDnsSuffix) {
        this.endpoints = new HashMap<>((int) (20 / 0.75F));
        this.endpoints.put("portalUrl", portalUrl);
        this.endpoints.put("publishingProfileUrl", publishingProfileUrl);
        this.endpoints.put("managementEndpointUrl", managementEndpointUrl);
        this.endpoints.put("resourceManagerEndpointUrl", resourceManagerEndpointUrl);
        this.endpoints.put("sqlManagementEndpointUrl", sqlManagementEndpointUrl);
        this.endpoints.put("sqlServerHostnameSuffix", sqlServerHostnameSuffix);
        this.endpoints.put("galleryEndpointUrl", galleryEndpointUrl);
        this.endpoints.put("activeDirectoryEndpointUrl", activeDirectoryEndpointUrl);
        this.endpoints.put("activeDirectoryResourceId", activeDirectoryResourceId);
        this.endpoints.put("activeDirectoryGraphResourceId", activeDirectoryGraphResourceId);
        this.endpoints.put("microsoftGraphResourceId", microsoftGraphResourceId);
        this.endpoints.put("dataLakeEndpointResourceId", dataLakeEndpointResourceId);
        this.endpoints.put("activeDirectoryGraphApiVersion", activeDirectoryGraphApiVersion);
        this.endpoints.put("storageEndpointSuffix", storageEndpointSuffix);
        this.endpoints.put("keyVaultDnsSuffix", keyVaultDnsSuffix);
        this.endpoints.put("azureDataLakeStoreFileSystemEndpointSuffix", azureDataLakeStoreFileSystemEndpointSuffix);
        this.endpoints.put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix",
            azureDataLakeAnalyticsCatalogAndJobEndpointSuffix);
        this.endpoints.put("azureLogAnalyticsResourceId", azureLogAnalyticsResourceId);
        this.endpoints.put("azureApplicationInsightsResourceId", azureApplicationInsightsResourceId);
        this.endpoints.put("managedHsmDnsSuffix", managedHsmDnsSuffix);
    }

    /**
     * Provides the settings for authentication with Azure.
     */
    public static final AzureEnvironment AZURE = new AzureEnvironment("https://portal.azure.com",
        "http://go.microsoft.com/fwlink/?LinkId=254432", "https://management.core.windows.net/",
        "https://management.azure.com/", "https://management.core.windows.net:8443/", ".database.windows.net",
        "https://gallery.azure.com/", "https://login.microsoftonline.com/", "https://management.core.windows.net/",
        "https://graph.windows.net/", "https://graph.microsoft.com/", "https://datalake.azure.net/", "2013-04-05",
        ".core.windows.net", ".vault.azure.net", "azuredatalakestore.net", "azuredatalakeanalytics.net",
        "https://api.loganalytics.io/", "https://api.applicationinsights.io/", ".managedhsm.azure.net");

    /**
     * Provides the settings for authentication with Azure China.
     */
    public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment("https://portal.azure.cn",
        "http://go.microsoft.com/fwlink/?LinkID=301774", "https://management.core.chinacloudapi.cn/",
        "https://management.chinacloudapi.cn/", "https://management.core.chinacloudapi.cn:8443/",
        ".database.chinacloudapi.cn", "https://gallery.chinacloudapi.cn/", "https://login.chinacloudapi.cn/",
        "https://management.core.chinacloudapi.cn/", "https://graph.chinacloudapi.cn/",
        "https://microsoftgraph.chinacloudapi.cn/", "N/A", "2013-04-05", ".core.chinacloudapi.cn", ".vault.azure.cn",
        "N/A", "N/A", "N/A", "N/A", ".managedhsm.azure.cn");

    /**
     * Provides the settings for authentication with Azure US Government.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment("https://portal.azure.us",
        "https://manage.windowsazure.us/publishsettings/index", "https://management.core.usgovcloudapi.net/",
        "https://management.usgovcloudapi.net/", "https://management.core.usgovcloudapi.net:8443/",
        ".database.usgovcloudapi.net", "https://gallery.usgovcloudapi.net/", "https://login.microsoftonline.us/",
        "https://management.core.usgovcloudapi.net/", "https://graph.windows.net/", "https://graph.microsoft.us/",
        "N/A", "2013-04-05", ".core.usgovcloudapi.net", ".vault.usgovcloudapi.net", "N/A", "N/A",
        "https://api.loganalytics.us/", "N/A", ".managedhsm.usgovcloudapi.net");

    /**
     * Provides the settings for authentication with Azure Germany.
     * <p>
     * Microsoft no longer be accepting new customers or deploying any new services from Microsoft Cloud Germany.
     *
     * @deprecated Use {@link Region#GERMANY_WEST_CENTRAL} or {@link Region#GERMANY_NORTH}
     * with {@link AzureEnvironment#AZURE}.
     */
    @Deprecated
    public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment("https://portal.microsoftazure.de",
        "https://manage.microsoftazure.de/publishsettings/index", "https://management.core.cloudapi.de/",
        "https://management.microsoftazure.de/", "https://management.core.cloudapi.de:8443/", ".database.cloudapi.de",
        "https://gallery.cloudapi.de/", "https://login.microsoftonline.de/", "https://management.core.cloudapi.de/",
        "https://graph.cloudapi.de/", "https://graph.microsoft.de/", "N/A", "2013-04-05", ".core.cloudapi.de",
        ".vault.microsoftazure.de", "N/A", "N/A", "N/A", "N/A", "N/A");

    /**
     * Gets the entirety of the endpoints associated with the current environment.
     *
     * @return the entirety of the endpoints associated with the current environment.
     */
    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    /**
     * Gets the list of known environments to Azure SDK.
     *
     * @return the list of known environments to Azure SDK.
     */
    public static List<AzureEnvironment> knownEnvironments() {
        return Arrays.asList(AZURE, AZURE_CHINA, AZURE_US_GOVERNMENT);
    }

    /**
     * Gets the management portal URL.
     *
     * @return the management portal URL.
     */
    public String getPortal() {
        return endpoints.get("portalUrl");
    }

    /**
     * Gets the publishing settings file URL.
     *
     * @return the publishing settings file URL.
     */
    public String getPublishingProfile() {
        return endpoints.get("publishingProfileUrl");
    }

    /**
     * Gets the management service endpoint.
     *
     * @return the management service endpoint.
     */
    public String getManagementEndpoint() {
        return endpoints.get("managementEndpointUrl");
    }

    /**
     * Gets the resource management endpoint.
     *
     * @return the resource management endpoint.
     */
    public String getResourceManagerEndpoint() {
        return endpoints.get("resourceManagerEndpointUrl");
    }

    /**
     * Gets the sql server management endpoint for mobile commands.
     *
     * @return the sql server management endpoint for mobile commands.
     */
    public String getSqlManagementEndpoint() {
        return endpoints.get("sqlManagementEndpointUrl");
    }

    /**
     * Gets the dns suffix for sql servers.
     *
     * @return the dns suffix for sql servers.
     */
    public String getSqlServerHostnameSuffix() {
        return endpoints.get("sqlServerHostnameSuffix");
    }

    /**
     * Gets the Active Directory login endpoint.
     *
     * @return the Active Directory login endpoint.
     */
    public String getActiveDirectoryEndpoint() {
        return endpoints.get("activeDirectoryEndpointUrl").replaceAll("/$", "") + "/";
    }

    /**
     * Gets the resource ID to obtain AD tokens.
     *
     * @return The resource ID to obtain AD tokens.
     */
    public String getActiveDirectoryResourceId() {
        return endpoints.get("activeDirectoryResourceId");
    }

    /**
     * Gets the template gallery endpoint.
     *
     * @return the template gallery endpoint.
     */
    public String getGalleryEndpoint() {
        return endpoints.get("galleryEndpointUrl");
    }

    /**
     * Gets the Active Directory resource ID.
     *
     * @return the Active Directory resource ID.
     */
    public String getGraphEndpoint() {
        return endpoints.get("activeDirectoryGraphResourceId");
    }

    /**
     * Gets the Microsoft Graph resource ID.
     *
     * @return the Microsoft Graph resource ID.
     */
    public String getMicrosoftGraphEndpoint() {
        return endpoints.get("microsoftGraphResourceId");
    }

    /**
     * Gets the Data Lake resource ID.
     *
     * @return the Data Lake resource ID.
     */
    public String getDataLakeEndpointResourceId() {
        return endpoints.get("dataLakeEndpointResourceId");
    }

    /**
     * Gets the Active Directory api version.
     *
     * @return the Active Directory api version.
     */
    public String getActiveDirectoryGraphApiVersion() {
        return endpoints.get("activeDirectoryGraphApiVersion");
    }

    /**
     * Gets the endpoint suffix for storage accounts.
     *
     * @return the endpoint suffix for storage accounts.
     */
    public String getStorageEndpointSuffix() {
        return endpoints.get("storageEndpointSuffix");
    }

    /**
     * Gets the keyvault service dns suffix.
     *
     * @return the keyvault service dns suffix.
     */
    public String getKeyVaultDnsSuffix() {
        return endpoints.get("keyVaultDnsSuffix");
    }

    /**
     * Gets the managed HSM DNS suffix.
     *
     * @return the managed HSM DNS suffix.
     */
    public String getManagedHsmDnsSuffix() {
        return endpoints.get("managedHsmDnsSuffix");
    }

    /**
     * Gets the data lake store filesystem service dns suffix.
     *
     * @return the data lake store filesystem service dns suffix.
     */
    public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
        return endpoints.get("azureDataLakeStoreFileSystemEndpointSuffix");
    }

    /**
     * Gets the data lake analytics job and catalog service dns suffix.
     *
     * @return the data lake analytics job and catalog service dns suffix.
     */
    public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
        return endpoints.get("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix");
    }

    /**
     * Gets the log analytics endpoint.
     *
     * @return the log analytics endpoint.
     */
    public String getLogAnalyticsEndpoint() {
        return endpoints.get("azureLogAnalyticsResourceId");
    }

    /**
     * Gets the log analytics endpoint.
     *
     * @return the log analytics endpoint.
     */
    public String getApplicationInsightsEndpoint() {
        return endpoints.get("azureApplicationInsightsResourceId");
    }

    /**
     * The enum representing available endpoints in an environment.
     */
    public enum Endpoint {
        /** Azure management endpoint. */
        MANAGEMENT("managementEndpointUrl"),
        /** Azure Resource Manager endpoint. */
        RESOURCE_MANAGER("resourceManagerEndpointUrl"),
        /** Azure SQL endpoint. */
        SQL("sqlManagementEndpointUrl"),
        /** Azure Gallery endpoint. */
        GALLERY("galleryEndpointUrl"),
        /** Active Directory authentication endpoint. */
        ACTIVE_DIRECTORY("activeDirectoryEndpointUrl"),
        /** Azure Active Directory Graph APIs endpoint. */
        GRAPH("activeDirectoryGraphResourceId"),
        /** Key Vault DNS suffix. */
        KEYVAULT("keyVaultDnsSuffix"),
        /** Azure Data Lake Store DNS suffix. */
        DATA_LAKE_STORE("azureDataLakeStoreFileSystemEndpointSuffix"),
        /** Azure Data Lake Analytics DNS suffix. */
        DATA_LAKE_ANALYTICS("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix"),
        /** Azure Log Analytics endpoint. */
        LOG_ANALYTICS("azureLogAnalyticsResourceId"),
        /** Azure Application Insights. */
        APPLICATION_INSIGHTS("azureApplicationInsightsResourceId"),
        /** Microsoft Graph APIs endpoint. */
        MICROSOFT_GRAPH("microsoftGraphResourceId"),
        /** Managed HSM DNS suffix. */
        MANAGED_HSM("managedHsmDnsSuffix"),
        /** Storage DNS suffix. */
        STORAGE("storageEndpointSuffix");

        private final String field;

        Endpoint(String value) {
            this.field = value;
        }

        /**
         * Gets a unique identifier for the endpoint in the environment.
         *
         * @return a unique identifier for the endpoint in the environment
         */
        public String identifier() {
            return field;
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Gets the endpoint URL for the current environment.
     *
     * @param endpoint the endpoint.
     * @return the URL for the endpoint, null if no match.
     */
    public String getUrlByEndpoint(Endpoint endpoint) {
        return endpoints.get(endpoint.identifier());
    }
}
