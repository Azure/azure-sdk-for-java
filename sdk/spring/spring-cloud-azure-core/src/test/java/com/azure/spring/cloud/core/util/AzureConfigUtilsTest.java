// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.core.util;

import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.CLIENT_CERTIFICATE_PASSWORD;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.CLIENT_CERTIFICATE_PATH;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.CLIENT_ID;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.CLIENT_SECRET;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.MANAGED_IDENTITY_ENABLED;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.PASSWORD;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.USERNAME;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.CLOUD_TYPE;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.ACTIVE_DIRECTORY_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.ACTIVE_DIRECTORY_GRAPH_API_VERSION;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.ACTIVE_DIRECTORY_GRAPH_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.ACTIVE_DIRECTORY_RESOURCE_ID;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_APPLICATION_INSIGHTS_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_LOG_ANALYTICS_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.DATA_LAKE_ENDPOINT_RESOURCE_ID;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.GALLERY_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.KEY_VAULT_DNS_SUFFIX;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.MANAGEMENT_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.MICROSOFT_GRAPH_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.PORTAL;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.PUBLISHING_PROFILE;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.RESOURCE_MANAGER_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.SQL_MANAGEMENT_ENDPOINT;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.SQL_SERVER_HOSTNAME_SUFFIX;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.STORAGE_ENDPOINT_SUFFIX;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.SUBSCRIPTION_ID;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.TENANT_ID;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.convertAzurePropertiesToConfigMap;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.convertConfigMapToAzureProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureConfigUtilsTest {
    private static final String CLIENT_CERTIFICATE_PASSWORD_VALUE = "azure.credential.client-certificate-password";
    private static final String CLIENT_CERTIFICATE_PATH_VALUE = "azure.credential.client-certificate-path";
    private static final String CLIENT_ID_VALUE = "azure.credential.client-id";
    private static final String CLIENT_SECRET_VALUE = "azure.credential.client-secret";
    private static final String MANAGED_IDENTITY_ENABLED_VALUE = "true";
    private static final String PASSWORD_VALUE = "azure.credential.password";
    private static final String USERNAME_VALUE = "azure.credential.username";
    private static final String CLOUD_TYPE_VALUE = "AZURE_CHINA";
    private static final String ACTIVE_DIRECTORY_ENDPOINT_VALUE = "azure.profile.environment.active-directory-endpoint";
    private static final String ACTIVE_DIRECTORY_GRAPH_API_VERSION_VALUE = "azure.profile.environment"
        + ".active-directory-graph-api-version";
    private static final String ACTIVE_DIRECTORY_GRAPH_ENDPOINT_VALUE = "azure.profile.environment"
        + ".active-directory-graph-endpoint";
    private static final String ACTIVE_DIRECTORY_RESOURCE_ID_VALUE = "azure.profile.environment.active-directory-resource-id";
    private static final String AZURE_APPLICATION_INSIGHTS_ENDPOINT_VALUE = "azure.profile.environment"
        + ".azure-application-insights-endpoint";
    private static final String AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX_VALUE = "azure.profile.environment"
        + ".azure-data-lake-analytics-catalog-and-job-endpoint-suffix";
    private static final String AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX_VALUE = "azure.profile.environment"
        + ".azure-data-lake-store-file-system-endpoint-suffix";
    private static final String AZURE_LOG_ANALYTICS_ENDPOINT_VALUE = "azure.profile.environment.azure-log-analytics-endpoint";
    private static final String DATA_LAKE_ENDPOINT_RESOURCE_ID_VALUE = "azure.profile.environment"
        + ".data-lake-endpoint-resource-id";
    private static final String GALLERY_ENDPOINT_VALUE = "azure.profile.environment.gallery-endpoint";
    private static final String KEY_VAULT_DNS_SUFFIX_VALUE = "azure.profile.environment.key-vault-dns-suffix";
    private static final String MANAGEMENT_ENDPOINT_VALUE = "azure.profile.environment.management-endpoint";
    private static final String MICROSOFT_GRAPH_ENDPOINT_VALUE = "azure.profile.environment.microsoft-graph-endpoint";
    private static final String PORTAL_VALUE = "azure.profile.environment.portal";
    private static final String PUBLISHING_PROFILE_VALUE = "azure.profile.environment.publishing-profile";
    private static final String RESOURCE_MANAGER_ENDPOINT_VALUE = "azure.profile.environment.resource-manager-endpoint";
    private static final String SQL_MANAGEMENT_ENDPOINT_VALUE = "azure.profile.environment.sql-management-endpoint";
    private static final String SQL_SERVER_HOSTNAME_SUFFIX_VALUE = "azure.profile.environment.sql-server-hostname-suffix";
    private static final String STORAGE_ENDPOINT_SUFFIX_VALUE = "azure.profile.environment.storage-endpoint-suffix";
    private static final String SUBSCRIPTION_ID_VALUE = "azure.profile.subscription-id";
    private static final String TENANT_ID_VALUE = "azure.profile.tenant-id";
    
    private final Map<String, Object> configs = new HashMap<>();
    
    @BeforeEach
    void setup() {
        configs.clear();
        configs.put(CLIENT_CERTIFICATE_PASSWORD, CLIENT_CERTIFICATE_PASSWORD_VALUE);
        configs.put(CLIENT_CERTIFICATE_PATH, CLIENT_CERTIFICATE_PATH_VALUE);
        configs.put(CLIENT_ID, CLIENT_ID_VALUE);
        configs.put(CLIENT_SECRET, CLIENT_SECRET_VALUE);
        configs.put(MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED_VALUE);
        configs.put(PASSWORD, PASSWORD_VALUE);
        configs.put(USERNAME, USERNAME_VALUE);
        configs.put(CLOUD_TYPE, CLOUD_TYPE_VALUE);
        configs.put(ACTIVE_DIRECTORY_ENDPOINT, ACTIVE_DIRECTORY_ENDPOINT_VALUE);
        configs.put(ACTIVE_DIRECTORY_GRAPH_API_VERSION, ACTIVE_DIRECTORY_GRAPH_API_VERSION_VALUE);
        configs.put(ACTIVE_DIRECTORY_GRAPH_ENDPOINT, ACTIVE_DIRECTORY_GRAPH_ENDPOINT_VALUE);
        configs.put(ACTIVE_DIRECTORY_RESOURCE_ID, ACTIVE_DIRECTORY_RESOURCE_ID_VALUE);
        configs.put(AZURE_APPLICATION_INSIGHTS_ENDPOINT, AZURE_APPLICATION_INSIGHTS_ENDPOINT_VALUE);
        configs.put(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX,
            AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX_VALUE);
        configs.put(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX,
            AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX_VALUE);
        configs.put(AZURE_LOG_ANALYTICS_ENDPOINT, AZURE_LOG_ANALYTICS_ENDPOINT_VALUE);
        configs.put(DATA_LAKE_ENDPOINT_RESOURCE_ID, DATA_LAKE_ENDPOINT_RESOURCE_ID_VALUE);
        configs.put(GALLERY_ENDPOINT, GALLERY_ENDPOINT_VALUE);
        configs.put(KEY_VAULT_DNS_SUFFIX, KEY_VAULT_DNS_SUFFIX_VALUE);
        configs.put(MANAGEMENT_ENDPOINT, MANAGEMENT_ENDPOINT_VALUE);
        configs.put(MICROSOFT_GRAPH_ENDPOINT, MICROSOFT_GRAPH_ENDPOINT_VALUE);
        configs.put(PORTAL, PORTAL_VALUE);
        configs.put(PUBLISHING_PROFILE, PUBLISHING_PROFILE_VALUE);
        configs.put(RESOURCE_MANAGER_ENDPOINT, RESOURCE_MANAGER_ENDPOINT_VALUE);
        configs.put(SQL_MANAGEMENT_ENDPOINT, SQL_MANAGEMENT_ENDPOINT_VALUE);
        configs.put(SQL_SERVER_HOSTNAME_SUFFIX, SQL_SERVER_HOSTNAME_SUFFIX_VALUE);
        configs.put(STORAGE_ENDPOINT_SUFFIX, STORAGE_ENDPOINT_SUFFIX_VALUE);
        configs.put(SUBSCRIPTION_ID, SUBSCRIPTION_ID_VALUE);
        configs.put(TENANT_ID, TENANT_ID_VALUE);
    }
    
    @Test
    void testConvertConfigMapToAzureProperties() {
        AzureThirdPartyServiceProperties properties = new AzureThirdPartyServiceProperties();
        convertConfigMapToAzureProperties(configs, properties);
        assertEquals(CLIENT_CERTIFICATE_PASSWORD_VALUE, properties.getCredential().getClientCertificatePassword());
        assertEquals(CLIENT_CERTIFICATE_PATH_VALUE, properties.getCredential().getClientCertificatePath());
        assertEquals(CLIENT_ID_VALUE, properties.getCredential().getClientId());
        assertEquals(CLIENT_SECRET_VALUE, properties.getCredential().getClientSecret());
        assertTrue(properties.getCredential().isManagedIdentityEnabled());
        assertEquals(PASSWORD_VALUE, properties.getCredential().getPassword());
        assertEquals(USERNAME_VALUE, properties.getCredential().getUsername());
        assertEquals(AzureProfileOptionsProvider.CloudType.AZURE_CHINA, properties.getProfile().getCloudType());
        assertEquals(ACTIVE_DIRECTORY_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(ACTIVE_DIRECTORY_GRAPH_API_VERSION_VALUE, properties.getProfile().getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals(ACTIVE_DIRECTORY_GRAPH_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getActiveDirectoryGraphEndpoint());
        assertEquals(ACTIVE_DIRECTORY_RESOURCE_ID_VALUE, properties.getProfile().getEnvironment().getActiveDirectoryResourceId());
        assertEquals(AZURE_APPLICATION_INSIGHTS_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getAzureApplicationInsightsEndpoint());
        assertEquals(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX_VALUE, properties.getProfile().getEnvironment().getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix());
        assertEquals(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX_VALUE, properties.getProfile().getEnvironment().getAzureDataLakeStoreFileSystemEndpointSuffix());
        assertEquals(AZURE_LOG_ANALYTICS_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getAzureLogAnalyticsEndpoint());
        assertEquals(DATA_LAKE_ENDPOINT_RESOURCE_ID_VALUE, properties.getProfile().getEnvironment().getDataLakeEndpointResourceId());
        assertEquals(GALLERY_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getGalleryEndpoint());
        assertEquals(KEY_VAULT_DNS_SUFFIX_VALUE, properties.getProfile().getEnvironment().getKeyVaultDnsSuffix());
        assertEquals(MANAGEMENT_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getManagementEndpoint());
        assertEquals(MICROSOFT_GRAPH_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint());
        assertEquals(PORTAL_VALUE, properties.getProfile().getEnvironment().getPortal());
        assertEquals(PUBLISHING_PROFILE_VALUE, properties.getProfile().getEnvironment().getPublishingProfile());
        assertEquals(RESOURCE_MANAGER_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getResourceManagerEndpoint());
        assertEquals(SQL_MANAGEMENT_ENDPOINT_VALUE, properties.getProfile().getEnvironment().getSqlManagementEndpoint());
        assertEquals(SQL_SERVER_HOSTNAME_SUFFIX_VALUE, properties.getProfile().getEnvironment().getSqlServerHostnameSuffix());
        assertEquals(STORAGE_ENDPOINT_SUFFIX_VALUE, properties.getProfile().getEnvironment().getStorageEndpointSuffix());
        assertEquals(SUBSCRIPTION_ID_VALUE, properties.getProfile().getSubscriptionId());
        assertEquals(TENANT_ID_VALUE, properties.getProfile().getTenantId());
    }
    
    @Test
    void testConvertAzurePropertiesToConfigMapWithCustomValues() {
        AzureThirdPartyServiceProperties properties = new AzureThirdPartyServiceProperties();
        Map<String, String> configs = new HashMap<>();
        configs.put(CLIENT_CERTIFICATE_PASSWORD_VALUE, "test");
        convertConfigMapToAzureProperties(this.configs, properties);
        convertAzurePropertiesToConfigMap(properties, configs);

        assertEquals("test", configs.get(CLIENT_CERTIFICATE_PASSWORD));
        assertEquals(CLIENT_CERTIFICATE_PATH_VALUE, configs.get(CLIENT_CERTIFICATE_PATH));
    }

    @Test
    void testConvertAzurePropertiesToConfigMapWithoutCustomValues() {
        AzureThirdPartyServiceProperties properties = new AzureThirdPartyServiceProperties();
        Map<String, String> configs = new HashMap<>();
        convertConfigMapToAzureProperties(this.configs, properties);
        convertAzurePropertiesToConfigMap(properties, configs);

        assertEquals(CLIENT_CERTIFICATE_PASSWORD_VALUE, configs.get(CLIENT_CERTIFICATE_PASSWORD));
        assertEquals(CLIENT_CERTIFICATE_PATH_VALUE, configs.get(CLIENT_CERTIFICATE_PATH));
        assertEquals(CLIENT_ID_VALUE, configs.get(CLIENT_ID));
        assertEquals(CLIENT_SECRET_VALUE, configs.get(CLIENT_SECRET));
        assertEquals(MANAGED_IDENTITY_ENABLED_VALUE, configs.get(MANAGED_IDENTITY_ENABLED));
        assertEquals(PASSWORD_VALUE, configs.get(PASSWORD));
        assertEquals(USERNAME_VALUE, configs.get(USERNAME));
        assertEquals(CLOUD_TYPE_VALUE, configs.get(CLOUD_TYPE));
        assertEquals(ACTIVE_DIRECTORY_ENDPOINT_VALUE, configs.get(ACTIVE_DIRECTORY_ENDPOINT));
        assertEquals(ACTIVE_DIRECTORY_GRAPH_API_VERSION_VALUE, configs.get(ACTIVE_DIRECTORY_GRAPH_API_VERSION));
        assertEquals(ACTIVE_DIRECTORY_GRAPH_ENDPOINT_VALUE, configs.get(ACTIVE_DIRECTORY_GRAPH_ENDPOINT));
        assertEquals(ACTIVE_DIRECTORY_RESOURCE_ID_VALUE, configs.get(ACTIVE_DIRECTORY_RESOURCE_ID));
        assertEquals(AZURE_APPLICATION_INSIGHTS_ENDPOINT_VALUE, configs.get(AZURE_APPLICATION_INSIGHTS_ENDPOINT));
        assertEquals(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX_VALUE, configs.get(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX));
        assertEquals(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX_VALUE, configs.get(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX));
        assertEquals(AZURE_LOG_ANALYTICS_ENDPOINT_VALUE, configs.get(AZURE_LOG_ANALYTICS_ENDPOINT));
        assertEquals(DATA_LAKE_ENDPOINT_RESOURCE_ID_VALUE, configs.get(DATA_LAKE_ENDPOINT_RESOURCE_ID));
        assertEquals(GALLERY_ENDPOINT_VALUE, configs.get(GALLERY_ENDPOINT));
        assertEquals(KEY_VAULT_DNS_SUFFIX_VALUE, configs.get(KEY_VAULT_DNS_SUFFIX));
        assertEquals(MANAGEMENT_ENDPOINT_VALUE, configs.get(MANAGEMENT_ENDPOINT));
        assertEquals(MICROSOFT_GRAPH_ENDPOINT_VALUE, configs.get(MICROSOFT_GRAPH_ENDPOINT));
        assertEquals(PORTAL_VALUE, configs.get(PORTAL));
        assertEquals(PUBLISHING_PROFILE_VALUE, configs.get(PUBLISHING_PROFILE));
        assertEquals(RESOURCE_MANAGER_ENDPOINT_VALUE, configs.get(RESOURCE_MANAGER_ENDPOINT));
        assertEquals(SQL_MANAGEMENT_ENDPOINT_VALUE, configs.get(SQL_MANAGEMENT_ENDPOINT));
        assertEquals(SQL_SERVER_HOSTNAME_SUFFIX_VALUE, configs.get(SQL_SERVER_HOSTNAME_SUFFIX));
        assertEquals(STORAGE_ENDPOINT_SUFFIX_VALUE, configs.get(STORAGE_ENDPOINT_SUFFIX));
        assertEquals(SUBSCRIPTION_ID_VALUE, configs.get(SUBSCRIPTION_ID));
        assertEquals(TENANT_ID_VALUE, configs.get(TENANT_ID));
    }

}
