// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.core.implementation.util;

import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.Map;

/**
 * Store the constants for customized Azure properties in other third party services.
 */
public final class AzureIdentityCustomConfigUtils {
    private AzureIdentityCustomConfigUtils() {
    }

    public static final String CLIENT_CERTIFICATE_PASSWORD = "azure.credential.client-certificate-password";
    public static final String CLIENT_CERTIFICATE_PATH = "azure.credential.client-certificate-path";
    public static final String CLIENT_ID = "azure.credential.client-id";
    public static final String CLIENT_SECRET = "azure.credential.client-secret";
    public static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";
    public static final String PASSWORD = "azure.credential.password";
    public static final String USERNAME = "azure.credential.username";
    public static final String CLOUD_TYPE = "azure.profile.cloud-type";
    public static final String ACTIVE_DIRECTORY_ENDPOINT = "azure.profile.environment.active-directory-endpoint";
    public static final String ACTIVE_DIRECTORY_GRAPH_API_VERSION = "azure.profile.environment"
        + ".active-directory-graph-api-version";
    public static final String ACTIVE_DIRECTORY_GRAPH_ENDPOINT = "azure.profile.environment"
        + ".active-directory-graph-endpoint";
    public static final String ACTIVE_DIRECTORY_RESOURCE_ID = "azure.profile.environment.active-directory-resource-id";
    public static final String AZURE_APPLICATION_INSIGHTS_ENDPOINT = "azure.profile.environment"
        + ".azure-application-insights-endpoint";
    public static final String AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX = "azure.profile.environment"
        + ".azure-data-lake-analytics-catalog-and-job-endpoint-suffix";
    public static final String AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX = "azure.profile.environment"
        + ".azure-data-lake-store-file-system-endpoint-suffix";
    public static final String AZURE_LOG_ANALYTICS_ENDPOINT = "azure.profile.environment.azure-log-analytics-endpoint";
    public static final String DATA_LAKE_ENDPOINT_RESOURCE_ID = "azure.profile.environment"
        + ".data-lake-endpoint-resource-id";
    public static final String GALLERY_ENDPOINT = "azure.profile.environment.gallery-endpoint";
    public static final String KEY_VAULT_DNS_SUFFIX = "azure.profile.environment.key-vault-dns-suffix";
    public static final String MANAGEMENT_ENDPOINT = "azure.profile.environment.management-endpoint";
    public static final String MICROSOFT_GRAPH_ENDPOINT = "azure.profile.environment.microsoft-graph-endpoint";
    public static final String PORTAL = "azure.profile.environment.portal";
    public static final String PUBLISHING_PROFILE = "azure.profile.environment.publishing-profile";
    public static final String RESOURCE_MANAGER_ENDPOINT = "azure.profile.environment.resource-manager-endpoint";
    public static final String SQL_MANAGEMENT_ENDPOINT = "azure.profile.environment.sql-management-endpoint";
    public static final String SQL_SERVER_HOSTNAME_SUFFIX = "azure.profile.environment.sql-server-hostname-suffix";
    public static final String STORAGE_ENDPOINT_SUFFIX = "azure.profile.environment.storage-endpoint-suffix";
    public static final String SUBSCRIPTION_ID = "azure.profile.subscription-id";
    public static final String TENANT_ID = "azure.profile.tenant-id";
    public static final String AZURE_TOKEN_CREDENTIAL = "azure.token.credential";

    public static void convertConfigMapToAzureProperties(Map<String, ?> source,
                                                         AzureThirdPartyServiceProperties target) {
        PropertyMapper propertyMapper = new PropertyMapper();
        propertyMapper.from(source.get(CLIENT_CERTIFICATE_PASSWORD)).to(prop -> target.getCredential().setClientCertificatePassword((String) prop));
        propertyMapper.from(source.get(CLIENT_CERTIFICATE_PATH)).to(prop -> target.getCredential().setClientCertificatePath((String) prop));
        propertyMapper.from(source.get(CLIENT_ID)).to(prop -> target.getCredential().setClientId((String) prop));
        propertyMapper.from(source.get(CLIENT_SECRET)).to(prop -> target.getCredential().setClientSecret((String) prop));
        propertyMapper.from(source.get(MANAGED_IDENTITY_ENABLED)).to(prop -> target.getCredential().setManagedIdentityEnabled(Boolean.valueOf((String) prop)));
        propertyMapper.from(source.get(PASSWORD)).to(prop -> target.getCredential().setPassword((String) prop));
        propertyMapper.from(source.get(USERNAME)).to(prop -> target.getCredential().setUsername((String) prop));
        propertyMapper.from(source.get(CLOUD_TYPE)).to(prop -> target.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.get((String) prop)));
        propertyMapper.from(source.get(ACTIVE_DIRECTORY_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setActiveDirectoryEndpoint((String) prop));
        propertyMapper.from(source.get(ACTIVE_DIRECTORY_GRAPH_API_VERSION)).to(prop -> target.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion((String) prop));
        propertyMapper.from(source.get(ACTIVE_DIRECTORY_GRAPH_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setActiveDirectoryGraphEndpoint((String) prop));
        propertyMapper.from(source.get(ACTIVE_DIRECTORY_RESOURCE_ID)).to(prop -> target.getProfile().getEnvironment().setActiveDirectoryResourceId((String) prop));
        propertyMapper.from(source.get(AZURE_APPLICATION_INSIGHTS_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setAzureApplicationInsightsEndpoint((String) prop));
        propertyMapper.from(source.get(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX)).to(prop -> target.getProfile().getEnvironment().setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix((String) prop));
        propertyMapper.from(source.get(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX)).to(prop -> target.getProfile().getEnvironment().setAzureDataLakeStoreFileSystemEndpointSuffix((String) prop));
        propertyMapper.from(source.get(AZURE_LOG_ANALYTICS_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setAzureLogAnalyticsEndpoint((String) prop));
        propertyMapper.from(source.get(DATA_LAKE_ENDPOINT_RESOURCE_ID)).to(prop -> target.getProfile().getEnvironment().setDataLakeEndpointResourceId((String) prop));
        propertyMapper.from(source.get(GALLERY_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setGalleryEndpoint((String) prop));
        propertyMapper.from(source.get(KEY_VAULT_DNS_SUFFIX)).to(prop -> target.getProfile().getEnvironment().setKeyVaultDnsSuffix((String) prop));
        propertyMapper.from(source.get(MANAGEMENT_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setManagementEndpoint((String) prop));
        propertyMapper.from(source.get(MICROSOFT_GRAPH_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setMicrosoftGraphEndpoint((String) prop));
        propertyMapper.from(source.get(PORTAL)).to(prop -> target.getProfile().getEnvironment().setPortal((String) prop));
        propertyMapper.from(source.get(PUBLISHING_PROFILE)).to(prop -> target.getProfile().getEnvironment().setPublishingProfile((String) prop));
        propertyMapper.from(source.get(RESOURCE_MANAGER_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setResourceManagerEndpoint((String) prop));
        propertyMapper.from(source.get(SQL_MANAGEMENT_ENDPOINT)).to(prop -> target.getProfile().getEnvironment().setSqlManagementEndpoint((String) prop));
        propertyMapper.from(source.get(SQL_SERVER_HOSTNAME_SUFFIX)).to(prop -> target.getProfile().getEnvironment().setSqlServerHostnameSuffix((String) prop));
        propertyMapper.from(source.get(STORAGE_ENDPOINT_SUFFIX)).to(prop -> target.getProfile().getEnvironment().setStorageEndpointSuffix((String) prop));
        propertyMapper.from(source.get(SUBSCRIPTION_ID)).to(prop -> target.getProfile().setSubscriptionId((String) prop));
        propertyMapper.from(source.get(TENANT_ID)).to(prop -> target.getProfile().setTenantId((String) prop));
    }

    public static void convertAzurePropertiesToConfigMap(AzureThirdPartyServiceProperties source,
                                                         Map<String, String> target) {
        PropertyMapper propertyMapper = new PropertyMapper();
        propertyMapper.from(source.getCredential().getClientCertificatePassword()).to(prop -> target.putIfAbsent(CLIENT_CERTIFICATE_PASSWORD, prop));
        propertyMapper.from(source.getCredential().getClientCertificatePath()).to(prop -> target.putIfAbsent(CLIENT_CERTIFICATE_PATH, prop));
        propertyMapper.from(source.getCredential().getClientId()).to(prop -> target.putIfAbsent(CLIENT_ID, prop));
        propertyMapper.from(source.getCredential().getClientSecret()).to(prop -> target.putIfAbsent(CLIENT_SECRET, prop));
        propertyMapper.from(source.getCredential().getClientCertificatePath()).to(prop -> target.putIfAbsent(CLIENT_CERTIFICATE_PATH, prop));
        propertyMapper.from(source.getCredential().isManagedIdentityEnabled()).to(prop -> target.putIfAbsent(MANAGED_IDENTITY_ENABLED, String.valueOf(prop)));
        propertyMapper.from(source.getCredential().getPassword()).to(prop -> target.putIfAbsent(PASSWORD, prop));
        propertyMapper.from(source.getCredential().getUsername()).to(prop -> target.putIfAbsent(USERNAME, prop));
        propertyMapper.from(source.getProfile().getCloudType()).to(prop -> target.putIfAbsent(CLOUD_TYPE, prop.name()));
        propertyMapper.from(source.getProfile().getEnvironment().getActiveDirectoryEndpoint()).to(prop -> target.putIfAbsent(ACTIVE_DIRECTORY_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getActiveDirectoryGraphApiVersion()).to(prop -> target.putIfAbsent(ACTIVE_DIRECTORY_GRAPH_API_VERSION, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getActiveDirectoryGraphEndpoint()).to(prop -> target.putIfAbsent(ACTIVE_DIRECTORY_GRAPH_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getActiveDirectoryResourceId()).to(prop -> target.putIfAbsent(ACTIVE_DIRECTORY_RESOURCE_ID, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getAzureApplicationInsightsEndpoint()).to(prop -> target.putIfAbsent(AZURE_APPLICATION_INSIGHTS_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix()).to(prop -> target.putIfAbsent(AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getAzureDataLakeStoreFileSystemEndpointSuffix()).to(prop -> target.putIfAbsent(AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getAzureLogAnalyticsEndpoint()).to(prop -> target.putIfAbsent(AZURE_LOG_ANALYTICS_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getDataLakeEndpointResourceId()).to(prop -> target.putIfAbsent(DATA_LAKE_ENDPOINT_RESOURCE_ID, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getGalleryEndpoint()).to(prop -> target.putIfAbsent(GALLERY_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getKeyVaultDnsSuffix()).to(prop -> target.putIfAbsent(KEY_VAULT_DNS_SUFFIX, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getManagementEndpoint()).to(prop -> target.putIfAbsent(MANAGEMENT_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getMicrosoftGraphEndpoint()).to(prop -> target.putIfAbsent(MICROSOFT_GRAPH_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getPortal()).to(prop -> target.putIfAbsent(PORTAL, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getPublishingProfile()).to(prop -> target.putIfAbsent(PUBLISHING_PROFILE, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getResourceManagerEndpoint()).to(prop -> target.putIfAbsent(RESOURCE_MANAGER_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getSqlManagementEndpoint()).to(prop -> target.putIfAbsent(SQL_MANAGEMENT_ENDPOINT, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getSqlServerHostnameSuffix()).to(prop -> target.putIfAbsent(SQL_SERVER_HOSTNAME_SUFFIX, prop));
        propertyMapper.from(source.getProfile().getEnvironment().getStorageEndpointSuffix()).to(prop -> target.putIfAbsent(STORAGE_ENDPOINT_SUFFIX, prop));
        propertyMapper.from(source.getProfile().getSubscriptionId()).to(prop -> target.putIfAbsent(SUBSCRIPTION_ID, prop));
        propertyMapper.from(source.getProfile().getTenantId()).to(prop -> target.putIfAbsent(TENANT_ID, prop));
    }

}
