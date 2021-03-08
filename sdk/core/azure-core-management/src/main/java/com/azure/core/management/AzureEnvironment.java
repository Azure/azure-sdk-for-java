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

    /**
     * Provides the settings for authentication with Azure.
     */
    public static final AzureEnvironment AZURE = new AzureEnvironment(new HashMap<String, String>() {{
            put("portalUrl", "http://go.microsoft.com/fwlink/?LinkId=254433");
            put("publishingProfileUrl", "http://go.microsoft.com/fwlink/?LinkId=254432");
            put("managementEndpointUrl", "https://management.core.windows.net/");
            put("resourceManagerEndpointUrl", "https://management.azure.com/");
            put("sqlManagementEndpointUrl", "https://management.core.windows.net:8443/");
            put("sqlServerHostnameSuffix", ".database.windows.net");
            put("galleryEndpointUrl", "https://gallery.azure.com/");
            put("activeDirectoryEndpointUrl", "https://login.microsoftonline.com/");
            put("activeDirectoryResourceId", "https://management.core.windows.net/");
            put("activeDirectoryGraphResourceId", "https://graph.windows.net/");
            put("microsoftGraphResourceId", "https://graph.microsoft.com/");
            put("dataLakeEndpointResourceId", "https://datalake.azure.net/");
            put("activeDirectoryGraphApiVersion", "2013-04-05");
            put("storageEndpointSuffix", ".core.windows.net");
            put("keyVaultDnsSuffix", ".vault.azure.net");
            put("azureDataLakeStoreFileSystemEndpointSuffix", "azuredatalakestore.net");
            put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "azuredatalakeanalytics.net");
            put("azureLogAnalyticsResourceId", "https://api.loganalytics.io/");
            put("azureApplicationInsightsResourceId", "https://api.applicationinsights.io/");
        }});

    /**
     * Provides the settings for authentication with Azure China.
     */
    public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment(new HashMap<String, String>() {{
            put("portalUrl", "http://go.microsoft.com/fwlink/?LinkId=301902");
            put("publishingProfileUrl", "http://go.microsoft.com/fwlink/?LinkID=301774");
            put("managementEndpointUrl", "https://management.core.chinacloudapi.cn/");
            put("resourceManagerEndpointUrl", "https://management.chinacloudapi.cn/");
            put("sqlManagementEndpointUrl", "https://management.core.chinacloudapi.cn:8443/");
            put("sqlServerHostnameSuffix", ".database.chinacloudapi.cn");
            put("galleryEndpointUrl", "https://gallery.chinacloudapi.cn/");
            put("activeDirectoryEndpointUrl", "https://login.chinacloudapi.cn/");
            put("activeDirectoryResourceId", "https://management.core.chinacloudapi.cn/");
            put("activeDirectoryGraphResourceId", "https://graph.chinacloudapi.cn/");
            put("microsoftGraphResourceId", "https://microsoftgraph.chinacloudapi.cn/");
            // TODO: add resource id for the china cloud for datalake once it is defined.
            put("dataLakeEndpointResourceId", "N/A");
            put("activeDirectoryGraphApiVersion", "2013-04-05");
            put("storageEndpointSuffix", ".core.chinacloudapi.cn");
            put("keyVaultDnsSuffix", ".vault.azure.cn");
            // TODO: add dns suffixes for the china cloud for datalake store and datalake analytics once they are
            //  defined.
            put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
            put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/A");
            put("azureLogAnalyticsResourceId", "N/A");
            put("azureApplicationInsightsResourceId", "N/A");
        }});

    /**
     * Provides the settings for authentication with Azure US Government.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment(new HashMap<String, String>() {{
            put("portalUrl", "https://manage.windowsazure.us");
            put("publishingProfileUrl", "https://manage.windowsazure.us/publishsettings/index");
            put("managementEndpointUrl", "https://management.core.usgovcloudapi.net/");
            put("resourceManagerEndpointUrl", "https://management.usgovcloudapi.net/");
            put("sqlManagementEndpointUrl", "https://management.core.usgovcloudapi.net:8443/");
            put("sqlServerHostnameSuffix", ".database.usgovcloudapi.net");
            put("galleryEndpointUrl", "https://gallery.usgovcloudapi.net/");
            put("activeDirectoryEndpointUrl", "https://login.microsoftonline.us/");
            put("activeDirectoryResourceId", "https://management.core.usgovcloudapi.net/");
            put("activeDirectoryGraphResourceId", "https://graph.windows.net/");
            put("microsoftGraphResourceId", "https://graph.microsoft.us/");
            // TODO: add resource id for the US government for datalake once it is defined.
            put("dataLakeEndpointResourceId", "N/A");
            put("activeDirectoryGraphApiVersion", "2013-04-05");
            put("storageEndpointSuffix", ".core.usgovcloudapi.net");
            put("keyVaultDnsSuffix", ".vault.usgovcloudapi.net");
            // TODO: add dns suffixes for the US government for datalake store and datalake analytics once they are
            //  defined.
            put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
            put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/A");
            put("azureLogAnalyticsResourceId", "https://api.loganalytics.us/");
            put("azureApplicationInsightsResourceId", "N/A");
        }});

    /**
     * Provides the settings for authentication with Azure Germany.
     */
    public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment(new HashMap<String, String>() {{
            put("portalUrl", "http://portal.microsoftazure.de/");
            put("publishingProfileUrl", "https://manage.microsoftazure.de/publishsettings/index");
            put("managementEndpointUrl", "https://management.core.cloudapi.de/");
            put("resourceManagerEndpointUrl", "https://management.microsoftazure.de/");
            put("sqlManagementEndpointUrl", "https://management.core.cloudapi.de:8443/");
            put("sqlServerHostnameSuffix", ".database.cloudapi.de");
            put("galleryEndpointUrl", "https://gallery.cloudapi.de/");
            put("activeDirectoryEndpointUrl", "https://login.microsoftonline.de/");
            put("activeDirectoryResourceId", "https://management.core.cloudapi.de/");
            put("activeDirectoryGraphResourceId", "https://graph.cloudapi.de/");
            put("microsoftGraphResourceId", "https://graph.microsoft.de/");
            // TODO: add resource id for the germany cloud for datalake once it is defined.
            put("dataLakeEndpointResourceId", "N/A");
            put("activeDirectoryGraphApiVersion", "2013-04-05");
            put("storageEndpointSuffix", ".core.cloudapi.de");
            put("keyVaultDnsSuffix", ".vault.microsoftazure.de");
            // TODO: add dns suffixes for the germany cloud for datalake store and datalake analytics once they are
            //  defined.
            put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
            put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/A");
            put("azureLogAnalyticsResourceId", "N/A");
            put("azureApplicationInsightsResourceId", "N/A");
        }});

    /**
     * @return the entirety of the endpoints associated with the current environment.
     */
    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    /**
     * @return the list of known environments to Azure SDK.
     */
    public static List<AzureEnvironment> knownEnvironments() {
        return Arrays.asList(AZURE, AZURE_CHINA, AZURE_GERMANY, AZURE_US_GOVERNMENT);
    }

    /**
     * @return the management portal URL.
     */
    public String getPortal() {
        return endpoints.get("portalUrl");
    }

    /**
     * @return the publish settings file URL.
     */
    public String getPublishingProfile() {
        return endpoints.get("publishingProfileUrl");
    }

    /**
     * @return the management service endpoint.
     */
    public String getManagementEndpoint() {
        return endpoints.get("managementEndpointUrl");
    }

    /**
     * @return the resource management endpoint.
     */
    public String getResourceManagerEndpoint() {
        return endpoints.get("resourceManagerEndpointUrl");
    }

    /**
     * @return the sql server management endpoint for mobile commands.
     */
    public String getSqlManagementEndpoint() {
        return endpoints.get("sqlManagementEndpointUrl");
    }

    /**
     * @return the dns suffix for sql servers.
     */
    public String getSqlServerHostnameSuffix() {
        return endpoints.get("sqlServerHostnameSuffix");
    }

    /**
     * @return the Active Directory login endpoint.
     */
    public String getActiveDirectoryEndpoint() {
        return endpoints.get("activeDirectoryEndpointUrl").replaceAll("/$", "") + "/";
    }

    /**
     * @return The resource ID to obtain AD tokens for.
     */
    public String getActiveDirectoryResourceId() {
        return endpoints.get("activeDirectoryResourceId");
    }

    /**
     * @return the template gallery endpoint.
     */
    public String getGalleryEndpoint() {
        return endpoints.get("galleryEndpointUrl");
    }

    /**
     * @return the Active Directory resource ID.
     */
    public String getGraphEndpoint() {
        return endpoints.get("activeDirectoryGraphResourceId");
    }

    /**
     * @return the Microsoft Graph resource ID.
     */
    public String getMicrosoftGraphEndpoint() {
        return endpoints.get("microsoftGraphResourceId");
    }

    /**
     * @return the Data Lake resource ID.
     */
    public String getDataLakeEndpointResourceId() {
        return endpoints.get("dataLakeEndpointResourceId");
    }

    /**
     * @return the Active Directory api version.
     */
    public String getActiveDirectoryGraphApiVersion() {
        return endpoints.get("activeDirectoryGraphApiVersion");
    }

    /**
     * @return the endpoint suffix for storage accounts.
     */
    public String getStorageEndpointSuffix() {
        return endpoints.get("storageEndpointSuffix");
    }

    /**
     * @return the keyvault service dns suffix.
     */
    public String getKeyVaultDnsSuffix() {
        return endpoints.get("keyVaultDnsSuffix");
    }

    /**
     * @return the data lake store filesystem service dns suffix.
     */
    public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
        return endpoints.get("azureDataLakeStoreFileSystemEndpointSuffix");
    }

    /**
     * @return the data lake analytics job and catalog service dns suffix.
     */
    public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
        return endpoints.get("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix");
    }

    /**
     * @return the log analytics endpoint.
     */
    public String getLogAnalyticsEndpoint() {
        return endpoints.get("azureLogAnalyticsResourceId");
    }

    /**
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
        MICROSOFT_GRAPH("microsoftGraphResourceId");

        private final String field;

        Endpoint(String value) {
            this.field = value;
        }

        /**
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
