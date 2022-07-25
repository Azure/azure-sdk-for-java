// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.implementation;

import com.mysql.cj.conf.PropertySet;
import java.util.Map;
import java.util.Properties;

/**
 * Store the constants for customized Azure properties with JDBC.
 */
public class JdbcPluginPropertiesUtils {

    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    static final String CREDENTIAL_PREFIX = "azure.credential.";
    static final String PROFILE_PREFIX = "azure.profile.";
    static final String ENVIRONMENT_PREFIX = PROFILE_PREFIX + "environment.";

    public static void convertPropertySetToConfigMap(PropertySet source, Map<String, String> target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(source.getProperty(m.propertyKey)).to(p -> target.putIfAbsent(m.propertyKey, p.getStringValue()));
        }
    }

    public static void convertPropertiesToConfigMap(Properties source, Map<String, String> target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(source.getProperty(m.propertyKey)).to(p -> target.putIfAbsent(m.propertyKey, p));
        }
    }

    public enum Mapping {
        clientCertificatePassword(CREDENTIAL_PREFIX + "client-certificate-password"),
        clientCertificatePath(CREDENTIAL_PREFIX + "client-certificate-path"),
        clientId(CREDENTIAL_PREFIX + "client-id"),
        clientSecret(CREDENTIAL_PREFIX + "client-secret"),
        managedIdentityEnabled(CREDENTIAL_PREFIX + "managed-identity-enabled"),
        password(CREDENTIAL_PREFIX + "password"),
        username(CREDENTIAL_PREFIX + "username"),
        cloudType(PROFILE_PREFIX + "cloud-type"),
        activeDirectoryEndpoint(ENVIRONMENT_PREFIX + "active-directory-endpoint"),
        activeDirectoryGraphApiVersion(ENVIRONMENT_PREFIX + "active-directory-graph-api-version"),
        activeDirectoryGraphEndpoint(ENVIRONMENT_PREFIX + "active-directory-graph-endpoint"),
        activeDirectoryResourceId(ENVIRONMENT_PREFIX + "active-directory-resource-id"),
        azureApplicationInsightsEndpoint(ENVIRONMENT_PREFIX + "azure-application-insights-endpoint"),
        azureDataLakeAnalyticsCatalogAndJobEndpointSuffix(ENVIRONMENT_PREFIX + "azure-data-lake-analytics-catalog-and-job-endpoint-suffix"),
        azureDataLakeStoreFileSystemEndpointSuffix(ENVIRONMENT_PREFIX + "azure-data-lake-store-file-system-endpoint-suffix"),
        azureLogAnalyticsEndpoint(ENVIRONMENT_PREFIX + "azure-log-analytics-endpoint"),
        dataLakeEndpointResourceId(ENVIRONMENT_PREFIX + "data-lake-endpoint-resource-id"),
        galleryEndpoint(ENVIRONMENT_PREFIX + "gallery-endpoint"),
        keyVaultDnsSuffix(ENVIRONMENT_PREFIX + "key-vault-dns-suffix"),
        managementEndpoint(ENVIRONMENT_PREFIX + "management-endpoint"),
        microsoftGraphEndpoint(ENVIRONMENT_PREFIX + "microsoft-graph-endpoint"),
        portal(ENVIRONMENT_PREFIX + "portal"),
        publishingProfile(ENVIRONMENT_PREFIX + "publishing-profile"),
        resourceManagerEndpoint(ENVIRONMENT_PREFIX + "resource-manager-endpoint"),
        sqlManagementEndpoint(ENVIRONMENT_PREFIX + "sql-management-endpoint"),
        sqlServerHostnameSuffix(ENVIRONMENT_PREFIX + "sql-server-hostname-suffix"),
        storageEndpointSuffix(ENVIRONMENT_PREFIX + "storage-endpoint-suffix"),
        subscriptionId(PROFILE_PREFIX + "subscription-id"),
        tenantId(PROFILE_PREFIX + "tenant-id");

        private String propertyKey;

        Mapping(String propertyKey) {
            this.propertyKey = propertyKey;
        }

        public String propertyKey() {
            return propertyKey;
        }

    }

}
