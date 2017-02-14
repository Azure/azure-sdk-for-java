/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.protocol.Environment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class describes an environment in Azure.
 */
public final class AzureEnvironment implements Environment {
    /** the management portal URL. */
    private final String portalUrl;
    /** the publish settings file URL. */
    private final String publishingProfileUrl;
    /** the management service endpoint. */
    private final String managementEndpointUrl;
    /** the resource management endpoint. */
    private final String resourceManagerEndpointUrl;
    /** the sql server management endpoint for mobile commands. */
    private final String sqlManagementEndpointUrl;
    /** the dns suffix for sql servers. */
    private final String sqlServerHostnameSuffix;
    /** the Active Directory login endpoint. */
    private final String activeDirectoryEndpointUrl;
    /** The resource ID to obtain AD tokens for. */
    private final String activeDirectoryResourceId;
    /** the template gallery endpoint. */
    private final String galleryEndpointUrl;
    /** the Active Directory resource ID. */
    private final String activeDirectoryGraphResourceId;
    /** the Active Directory api version. */
    private final String activeDirectoryGraphApiVersion;
    /** the endpoint suffix for storage accounts. */
    private final String storageEndpointSuffix;
    /** the keyvault service dns suffix. */
    private final String keyVaultDnsSuffix;
    /** the data lake store filesystem service dns suffix. */
    private final String azureDataLakeStoreFileSystemEndpointSuffix;
    /** the data lake analytics job and catalog service dns suffix. */
    private final String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;

    /**
     * Initializes an instance of AzureEnvironment class.
     *
     * @param endpoints a map storing all the endpoint info
     */
    public AzureEnvironment(Map<String, String> endpoints) {
        this.portalUrl = endpoints.get("portalUrl");
        this.publishingProfileUrl = endpoints.get("publishingProfileUrl");
        this.managementEndpointUrl = endpoints.get("managementEndpointUrl");
        this.resourceManagerEndpointUrl = endpoints.get("resourceManagerEndpointUrl");
        this.sqlManagementEndpointUrl = endpoints.get("sqlManagementEndpointUrl");
        this.sqlServerHostnameSuffix = endpoints.get("sqlServerHostnameSuffix");
        this.activeDirectoryEndpointUrl = endpoints.get("activeDirectoryEndpointUrl");
        this.activeDirectoryResourceId = endpoints.get("activeDirectoryResourceId");
        this.galleryEndpointUrl = endpoints.get("galleryEndpointUrl");
        this.activeDirectoryGraphResourceId = endpoints.get("activeDirectoryGraphResourceId");
        this.activeDirectoryGraphApiVersion = endpoints.get("activeDirectoryGraphApiVersion");
        this.storageEndpointSuffix = endpoints.get("storageEndpointSuffix");
        this.keyVaultDnsSuffix = endpoints.get("keyVaultDnsSuffix");
        this.azureDataLakeStoreFileSystemEndpointSuffix = endpoints.get("azureDataLakeStoreFileSystemEndpointSuffix");
        this.azureDataLakeAnalyticsCatalogAndJobEndpointSuffix = endpoints.get("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix");
    }

    /**
     * Provides the settings for authentication with Azure.
     */
    public static final AzureEnvironment AZURE = new AzureEnvironment(new HashMap<String, String>() {{
        put("portalUrl", "http://go.microsoft.com/fwlink/?LinkId=254433");
        put("publishingProfileUrl", "http://go.microsoft.com/fwlink/?LinkId=254432");
        put("managementEndpointUrl", "https://management.core.windows.net");
        put("resourceManagerEndpointUrl", "https://management.azure.com/");
        put("sqlManagementEndpointUrl", "https://management.core.windows.net:8443/");
        put("sqlServerHostnameSuffix", ".database.windows.net");
        put("galleryEndpointUrl", "https://gallery.azure.com/");
        put("activeDirectoryEndpointUrl", "https://login.microsoftonline.com/");
        put("activeDirectoryResourceId", "https://management.core.windows.net/");
        put("activeDirectoryGraphResourceId", "https://graph.windows.net/");
        put("activeDirectoryGraphApiVersion", "2013-04-05");
        put("storageEndpointSuffix", ".core.windows.net");
        put("keyVaultDnsSuffix", ".vault.azure.net");
        put("azureDataLakeStoreFileSystemEndpointSuffix", "azuredatalakestore.net");
        put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "azuredatalakeanalytics.net");
    }});

    /**
     * Provides the settings for authentication with Azure China.
     */
    public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment(new HashMap<String, String>() {{
        put("portalUrl", "http://go.microsoft.com/fwlink/?LinkId=301902");
        put("publishingProfileUrl", "http://go.microsoft.com/fwlink/?LinkID=301774");
        put("managementEndpointUrl", "https://management.core.chinacloudapi.cn");
        put("resourceManagerEndpointUrl", "https://management.chinacloudapi.cn");
        put("sqlManagementEndpointUrl", "https://management.core.chinacloudapi.cn:8443/");
        put("sqlServerHostnameSuffix", ".database.chinacloudapi.cn");
        put("galleryEndpointUrl", "https://gallery.chinacloudapi.cn/");
        put("activeDirectoryEndpointUrl", "https://login.chinacloudapi.cn/");
        put("activeDirectoryResourceId", "https://management.core.chinacloudapi.cn/");
        put("activeDirectoryGraphResourceId", "https://graph.chinacloudapi.cn/");
        put("activeDirectoryGraphApiVersion", "2013-04-05");
        put("storageEndpointSuffix", ".core.chinacloudapi.cn");
        put("keyVaultDnsSuffix", ".vault.azure.cn");
        // TODO: add dns suffixes for the china cloud for datalake store and datalake analytics once they are defined.
        put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
        put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/A");
    }});

    /**
     * Provides the settings for authentication with Azure US Government.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment(new HashMap<String, String>() {{
        put("portalUrl", "https://manage.windowsazure.us");
        put("publishingProfileUrl", "https://manage.windowsazure.us/publishsettings/index");
        put("managementEndpointUrl", "https://management.core.usgovcloudapi.net");
        put("resourceManagerEndpointUrl", "https://management.usgovcloudapi.net");
        put("sqlManagementEndpointUrl", "https://management.core.usgovcloudapi.net:8443/");
        put("sqlServerHostnameSuffix", ".database.usgovcloudapi.net");
        put("galleryEndpointUrl", "https://gallery.usgovcloudapi.net/");
        put("activeDirectoryEndpointUrl", "https://login-us.microsoftonline.com/");
        put("activeDirectoryResourceId", "https://management.core.usgovcloudapi.net/");
        put("activeDirectoryGraphResourceId", "https://graph.windows.net/");
        put("activeDirectoryGraphApiVersion", "2013-04-05");
        put("storageEndpointSuffix", ".core.usgovcloudapi.net");
        put("keyVaultDnsSuffix", ".vault.usgovcloudapi.net");
        // TODO: add dns suffixes for the US government for datalake store and datalake analytics once they are defined.
        put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
        put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/A");
    }});

    /**
     * Provides the settings for authentication with Azure Germany.
     */
    public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment(new HashMap<String, String>() {{
        put("portalUrl", "http://portal.microsoftazure.de/");
        put("publishingProfileUrl", "https://manage.microsoftazure.de/publishsettings/index");
        put("managementEndpointUrl", "https://management.core.cloudapi.de");
        put("resourceManagerEndpointUrl", "https://management.microsoftazure.de");
        put("sqlManagementEndpointUrl", "https://management.core.cloudapi.de:8443/");
        put("sqlServerHostnameSuffix", ".database.cloudapi.de");
        put("galleryEndpointUrl", "https://gallery.cloudapi.de/");
        put("activeDirectoryEndpointUrl", "https://login.microsoftonline.de/");
        put("activeDirectoryResourceId", "https://management.core.cloudapi.de/");
        put("activeDirectoryGraphResourceId", "https://graph.cloudapi.de/");
        put("activeDirectoryGraphApiVersion", "2013-04-05");
        put("storageEndpointSuffix", ".core.cloudapi.de");
        put("keyVaultDnsSuffix", ".vault.microsoftazure.de");
        // TODO: add dns suffixes for the US government for datalake store and datalake analytics once they are defined.
        put("azureDataLakeStoreFileSystemEndpointSuffix", "N/A");
        put("azureDataLakeAnalyticsCatalogAndJobEndpointSuffix", "N/");
    }});

    /**
     * @return the management portal URL.
     */
    public String portal() {
        return portalUrl;
    }

    /**
     * @return the publish settings file URL.
     */
    public String publishingProfile() {
        return publishingProfileUrl;
    }

    /**
     * @return the management service endpoint.
     */
    public String managementEndpoint() {
        return managementEndpointUrl;
    }

    /**
     * @return the resource management endpoint.
     */
    public String resourceManagerEndpoint() {
        return resourceManagerEndpointUrl;
    }

    /**
     * @return the sql server management endpoint for mobile commands.
     */
    public String sqlManagementEndpoint() {
        return sqlManagementEndpointUrl;
    }

    /**
     * @return the dns suffix for sql servers.
     */
    public String sqlServerHostnameSuffix() {
        return sqlServerHostnameSuffix;
    }

    /**
     * @return the Active Directory login endpoint.
     */
    public String activeDirectoryEndpoint() {
        return activeDirectoryEndpointUrl;
    }

    /**
     * @return The resource ID to obtain AD tokens for.
     */
    public String activeDirectoryResourceId() {
        return activeDirectoryResourceId;
    }

    /**
     * @return the template gallery endpoint.
     */
    public String galleryEndpoint() {
        return galleryEndpointUrl;
    }

    /**
     * @return the Active Directory resource ID.
     */
    public String graphEndpoint() {
        return activeDirectoryGraphResourceId;
    }

    /**
     * @return the Active Directory api version.
     */
    public String activeDirectoryGraphApiVersion() {
        return activeDirectoryGraphApiVersion;
    }

    /**
     * @return the endpoint suffix for storage accounts.
     */
    public String storageEndpointSuffix() {
        return storageEndpointSuffix;
    }

    /**
     * @return the keyvault service dns suffix.
     */
    public String keyVaultDnsSuffix() {
        return keyVaultDnsSuffix;
    }

    /**
     * @return the data lake store filesystem service dns suffix.
     */
    public String azureDataLakeStoreFileSystemEndpointSuffix() {
        return azureDataLakeStoreFileSystemEndpointSuffix;
    }

    /**
     * @return the data lake analytics job and catalog service dns suffix.
     */
    public String azureDataLakeAnalyticsCatalogAndJobEndpointSuffix() {
        return azureDataLakeAnalyticsCatalogAndJobEndpointSuffix;
    }


    /**
     * The enum representing available endpoints in an environment.
     */
    public enum Endpoint implements Environment.Endpoint {
        /** Azure Resource Manager endpoint. */
        RESOURCE_MANAGER("resourceManagerEndpointUrl"),
        /** Azure Active Directory Graph APIs endpoint. */
        GRAPH("activeDirectoryGraphResourceId"),
        /** Azure SQL endpoint. */
        SQL("sqlManagementEndpointUrl"),
        /** Azure Gallery endpoint. */
        GALLERY("galleryEndpointUrl"),
        /** Active Directory authentication endpoint. */
        ACTIVE_DIRECTORY("activeDirectoryEndpointUrl"),
        /** Azure management endpoint. */
        MANAGEMENT("managementEndpointUrl");

        private String field;

        Endpoint(String value) {
            this.field = value;
        }

        @Override
        public String identifier() {
            return field;
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Get the endpoint URL for the current environment.
     *
     * @param endpoint the endpoint
     * @return the URL
     */
    public String url(Environment.Endpoint endpoint) {
        try {
            Field f = AzureEnvironment.class.getDeclaredField(endpoint.identifier());
            f.setAccessible(true);
            return (String) f.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to reflect on field " + endpoint.identifier(), e);
        }
    }
}
