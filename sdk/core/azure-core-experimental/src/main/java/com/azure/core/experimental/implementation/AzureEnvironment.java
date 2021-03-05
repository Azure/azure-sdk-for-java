// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import com.azure.core.experimental.http.policy.ArmChallengeAuthenticationPolicy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class describes an environment in Azure.
 * This is a temporary impl class to support {@link ArmChallengeAuthenticationPolicy} until it
 * moves to azure-resource-manager package.
 */
public final class AzureEnvironment {
    public static final String KEY_VAULT_DNS_SUFFIX = "keyVaultDnsSuffix";
    public static final String STORAGE_ENDPOINT_SUFFIX = "storageEndpointSuffix";
    public static final String AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX = "azureDataLakeStoreFileSystemEndpointSuffix";
    public static final String AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX = "azureDataLakeAnalyticsCatalogAndJobEndpointSuffix";
    public static final String PUBLISHING_PROFILE_URL = "publishingProfileUrl";
    public static final String MANAGEMENT_ENDPOINT_URL = "managementEndpointUrl";
    public static final String RESOURCE_MANAGER_ENDPOINT_URL = "resourceManagerEndpointUrl";
    public static final String SQL_MANAGEMENT_ENDPOINT_URL = "sqlManagementEndpointUrl";
    public static final String SQL_SERVER_HOSTNAME_SUFFIX = "sqlServerHostnameSuffix";
    public static final String GALLERY_ENDPOINT_URL = "galleryEndpointUrl";
    public static final String ACTIVE_DIRECTORY_ENDPOINT_URL = "activeDirectoryEndpointUrl";
    public static final String ACTIVE_DIRECTORY_RESOURCE_ID = "activeDirectoryResourceId";
    public static final String ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID = "activeDirectoryGraphResourceId";
    public static final String MICROSOFT_GRAPH_RESOURCE_ID = "microsoftGraphResourceId";
    public static final String DATA_LAKE_ENDPOINT_RESOURCE_ID = "dataLakeEndpointResourceId";
    public static final String ACTIVE_DIRECTORY_GRAPH_API_VERSION = "activeDirectoryGraphApiVersion";
    public static final String AZURE_LOG_ANALYTICS_RESOURCE_ID = "azureLogAnalyticsResourceId";
    public static final String AZURE_APPLICATION_INSIGHTS_RESOURCE_ID = "azureApplicationInsightsResourceId";
    public static final String PORTAL_URL = "portalUrl";

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
    public static final AzureEnvironment AZURE = new AzureEnvironment(new HashMap<String, String>() {
        {
            put(PORTAL_URL, "http://go.microsoft.com/fwlink/?LinkId=254433");
            put(PUBLISHING_PROFILE_URL, "http://go.microsoft.com/fwlink/?LinkId=254432");
            put(MANAGEMENT_ENDPOINT_URL, "https://management.core.windows.net/");
            put(RESOURCE_MANAGER_ENDPOINT_URL, "https://management.azure.com/");
            put(SQL_MANAGEMENT_ENDPOINT_URL, "https://management.core.windows.net:8443/");
            put(SQL_SERVER_HOSTNAME_SUFFIX, ".database.windows.net");
            put(GALLERY_ENDPOINT_URL, "https://gallery.azure.com/");
            put(ACTIVE_DIRECTORY_ENDPOINT_URL, "https://login.microsoftonline.com/");
            put(ACTIVE_DIRECTORY_RESOURCE_ID, "https://management.core.windows.net/");
            put(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID, "https://graph.windows.net/");
            put(MICROSOFT_GRAPH_RESOURCE_ID, "https://graph.microsoft.com/");
            put(DATA_LAKE_ENDPOINT_RESOURCE_ID, "https://datalake.azure.net/");
            put(ACTIVE_DIRECTORY_GRAPH_API_VERSION, "2013-04-05");
            put(STORAGE_ENDPOINT_SUFFIX, ".core.windows.net");
            put(KEY_VAULT_DNS_SUFFIX, ".vault.azure.net");
            put(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX, "azuredatalakestore.net");
            put(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX, "azuredatalakeanalytics.net");
            put(AZURE_LOG_ANALYTICS_RESOURCE_ID, "https://api.loganalytics.io/");
            put(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID, "https://api.applicationinsights.io/");
        }
    });

    /**
     * Provides the settings for authentication with Azure China.
     */
    public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment(new HashMap<String, String>() {
        {
            put(PORTAL_URL, "http://go.microsoft.com/fwlink/?LinkId=301902");
            put(PUBLISHING_PROFILE_URL, "http://go.microsoft.com/fwlink/?LinkID=301774");
            put(MANAGEMENT_ENDPOINT_URL, "https://management.core.chinacloudapi.cn/");
            put(RESOURCE_MANAGER_ENDPOINT_URL, "https://management.chinacloudapi.cn/");
            put(SQL_MANAGEMENT_ENDPOINT_URL, "https://management.core.chinacloudapi.cn:8443/");
            put(SQL_SERVER_HOSTNAME_SUFFIX, ".database.chinacloudapi.cn");
            put(GALLERY_ENDPOINT_URL, "https://gallery.chinacloudapi.cn/");
            put(ACTIVE_DIRECTORY_ENDPOINT_URL, "https://login.chinacloudapi.cn/");
            put(ACTIVE_DIRECTORY_RESOURCE_ID, "https://management.core.chinacloudapi.cn/");
            put(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID, "https://graph.chinacloudapi.cn/");
            put(MICROSOFT_GRAPH_RESOURCE_ID, "https://microsoftgraph.chinacloudapi.cn/");
            // TODO: add resource id for the china cloud for datalake once it is defined.
            put(DATA_LAKE_ENDPOINT_RESOURCE_ID, "N/A");
            put(ACTIVE_DIRECTORY_GRAPH_API_VERSION, "2013-04-05");
            put(STORAGE_ENDPOINT_SUFFIX, ".core.chinacloudapi.cn");
            put(KEY_VAULT_DNS_SUFFIX, ".vault.azure.cn");
            // TODO: add dns suffixes for the china cloud for datalake store and datalake analytics once they are
            //  defined.
            put(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_LOG_ANALYTICS_RESOURCE_ID, "N/A");
            put(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID, "N/A");
        }
    });

    /**
     * Provides the settings for authentication with Azure US Government.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment(new HashMap<String, String>() {
        {
            put(PORTAL_URL, "https://manage.windowsazure.us");
            put(PUBLISHING_PROFILE_URL, "https://manage.windowsazure.us/publishsettings/index");
            put(MANAGEMENT_ENDPOINT_URL, "https://management.core.usgovcloudapi.net/");
            put(RESOURCE_MANAGER_ENDPOINT_URL, "https://management.usgovcloudapi.net/");
            put(SQL_MANAGEMENT_ENDPOINT_URL, "https://management.core.usgovcloudapi.net:8443/");
            put(SQL_SERVER_HOSTNAME_SUFFIX, ".database.usgovcloudapi.net");
            put(GALLERY_ENDPOINT_URL, "https://gallery.usgovcloudapi.net/");
            put(ACTIVE_DIRECTORY_ENDPOINT_URL, "https://login.microsoftonline.us/");
            put(ACTIVE_DIRECTORY_RESOURCE_ID, "https://management.core.usgovcloudapi.net/");
            put(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID, "https://graph.windows.net/");
            put(MICROSOFT_GRAPH_RESOURCE_ID, "https://graph.microsoft.us/");
            // TODO: add resource id for the US government for datalake once it is defined.
            put(DATA_LAKE_ENDPOINT_RESOURCE_ID, "N/A");
            put(ACTIVE_DIRECTORY_GRAPH_API_VERSION, "2013-04-05");
            put(STORAGE_ENDPOINT_SUFFIX, ".core.usgovcloudapi.net");
            put(KEY_VAULT_DNS_SUFFIX, ".vault.usgovcloudapi.net");
            // TODO: add dns suffixes for the US government for datalake store and datalake analytics once they are
            //  defined.
            put(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_LOG_ANALYTICS_RESOURCE_ID, "https://api.loganalytics.us/");
            put(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID, "N/A");
        }
    });

    /**
     * Provides the settings for authentication with Azure Germany.
     */
    public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment(new HashMap<String, String>() {
        {
            put(PORTAL_URL, "http://portal.microsoftazure.de/");
            put(PUBLISHING_PROFILE_URL, "https://manage.microsoftazure.de/publishsettings/index");
            put(MANAGEMENT_ENDPOINT_URL, "https://management.core.cloudapi.de/");
            put(RESOURCE_MANAGER_ENDPOINT_URL, "https://management.microsoftazure.de/");
            put(SQL_MANAGEMENT_ENDPOINT_URL, "https://management.core.cloudapi.de:8443/");
            put(SQL_SERVER_HOSTNAME_SUFFIX, ".database.cloudapi.de");
            put(GALLERY_ENDPOINT_URL, "https://gallery.cloudapi.de/");
            put(ACTIVE_DIRECTORY_ENDPOINT_URL, "https://login.microsoftonline.de/");
            put(ACTIVE_DIRECTORY_RESOURCE_ID, "https://management.core.cloudapi.de/");
            put(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID, "https://graph.cloudapi.de/");
            put(MICROSOFT_GRAPH_RESOURCE_ID, "https://graph.microsoft.de/");
            // TODO: add resource id for the germany cloud for datalake once it is defined.
            put(DATA_LAKE_ENDPOINT_RESOURCE_ID, "N/A");
            put(ACTIVE_DIRECTORY_GRAPH_API_VERSION, "2013-04-05");
            put(STORAGE_ENDPOINT_SUFFIX, ".core.cloudapi.de");
            put(KEY_VAULT_DNS_SUFFIX, ".vault.microsoftazure.de");
            // TODO: add dns suffixes for the germany cloud for datalake store and datalake analytics once they are
            //  defined.
            put(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX, "N/A");
            put(AZURE_LOG_ANALYTICS_RESOURCE_ID, "N/A");
            put(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID, "N/A");
        }
    });

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
        return endpoints.get(PORTAL_URL);
    }

    /**
     * @return the publish settings file URL.
     */
    public String getPublishingProfile() {
        return endpoints.get(PUBLISHING_PROFILE_URL);
    }

    /**
     * @return the management service endpoint.
     */
    public String getManagementEndpoint() {
        return endpoints.get(MANAGEMENT_ENDPOINT_URL);
    }

    /**
     * @return the resource management endpoint.
     */
    public String getResourceManagerEndpoint() {
        return endpoints.get(RESOURCE_MANAGER_ENDPOINT_URL);
    }

    /**
     * @return the sql server management endpoint for mobile commands.
     */
    public String getSqlManagementEndpoint() {
        return endpoints.get(SQL_MANAGEMENT_ENDPOINT_URL);
    }

    /**
     * @return The resource ID to obtain AD tokens for.
     */
    public String getActiveDirectoryResourceId() {
        return endpoints.get(ACTIVE_DIRECTORY_RESOURCE_ID);
    }

    /**
     * @return the template gallery endpoint.
     */
    public String getGalleryEndpoint() {
        return endpoints.get(GALLERY_ENDPOINT_URL);
    }

    /**
     * @return the Active Directory resource ID.
     */
    public String getGraphEndpoint() {
        return endpoints.get(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID);
    }

    /**
     * @return the Microsoft Graph resource ID.
     */
    public String getMicrosoftGraphEndpoint() {
        return endpoints.get(MICROSOFT_GRAPH_RESOURCE_ID);
    }

    /**
     * @return the Data Lake resource ID.
     */
    public String getDataLakeEndpointResourceId() {
        return endpoints.get(DATA_LAKE_ENDPOINT_RESOURCE_ID);
    }

    /**
     * @return the Active Directory api version.
     */
    public String getActiveDirectoryGraphApiVersion() {
        return endpoints.get(ACTIVE_DIRECTORY_GRAPH_API_VERSION);
    }

    /**
     * @return the endpoint suffix for storage accounts.
     */
    public String getStorageEndpointSuffix() {
        return endpoints.get(STORAGE_ENDPOINT_SUFFIX);
    }

    /**
     * @return the keyvault service dns suffix.
     */
    public String getKeyVaultDnsSuffix() {
        return endpoints.get(KEY_VAULT_DNS_SUFFIX);
    }

    /**
     * @return the data lake store filesystem service dns suffix.
     */
    public String getAzureDataLakeStoreFileSystemEndpointSuffix() {
        return endpoints.get(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX);
    }

    /**
     * @return the data lake analytics job and catalog service dns suffix.
     */
    public String getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
        return endpoints.get(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX);
    }

    /**
     * @return the log analytics endpoint.
     */
    public String getLogAnalyticsEndpoint() {
        return endpoints.get(AZURE_LOG_ANALYTICS_RESOURCE_ID);
    }

    /**
     * @return the log analytics endpoint.
     */
    public String getApplicationInsightsEndpoint() {
        return endpoints.get(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID);
    }

    /**
     * The enum representing available endpoints in an environment.
     */
    public enum Endpoint {
        /** Azure management endpoint. */
        MANAGEMENT(MANAGEMENT_ENDPOINT_URL),
        /** Azure Resource Manager endpoint. */
        RESOURCE_MANAGER(RESOURCE_MANAGER_ENDPOINT_URL),
        /** Azure SQL endpoint. */
        SQL(SQL_MANAGEMENT_ENDPOINT_URL),
        /** Azure Gallery endpoint. */
        GALLERY(GALLERY_ENDPOINT_URL),
        /** Active Directory authentication endpoint. */
        ACTIVE_DIRECTORY(ACTIVE_DIRECTORY_ENDPOINT_URL),
        /** Azure Active Directory Graph APIs endpoint. */
        GRAPH(ACTIVE_DIRECTORY_GRAPH_RESOURCE_ID),
        /** Key Vault DNS suffix. */
        KEYVAULT(KEY_VAULT_DNS_SUFFIX),
        /** Azure Data Lake Store DNS suffix. */
        DATA_LAKE_STORE(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX),
        /** Azure Data Lake Analytics DNS suffix. */
        DATA_LAKE_ANALYTICS(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX),
        /** Azure Log Analytics endpoint. */
        LOG_ANALYTICS(AZURE_LOG_ANALYTICS_RESOURCE_ID),
        /** Azure Application Insights. */
        APPLICATION_INSIGHTS(AZURE_APPLICATION_INSIGHTS_RESOURCE_ID),
        /** Microsoft Graph APIs endpoint. */
        MICROSOFT_GRAPH(MICROSOFT_GRAPH_RESOURCE_ID);

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
