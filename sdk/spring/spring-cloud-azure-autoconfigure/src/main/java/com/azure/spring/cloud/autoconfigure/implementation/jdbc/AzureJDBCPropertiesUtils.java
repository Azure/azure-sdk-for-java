// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.mysql.cj.conf.PropertySet;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * Store the constants for customized Azure properties with JDBC.
 */
public class AzureJDBCPropertiesUtils {

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

    public static TokenCredential resolveTokenCredential(Map<String ,String > map) {
        if (map == null) {
            return null;
        }

        final String tenantId = map.get(AzureJDBCPropertiesUtils.Mapping.tenantId.propertyKey());
        final String clientId = map.get(AzureJDBCPropertiesUtils.Mapping.clientId.propertyKey());
        final String clientSecret = map.get(AzureJDBCPropertiesUtils.Mapping.clientSecret.propertyKey());

        final boolean isClientIdSet = StringUtils.hasText(clientId);
        if (StringUtils.hasText(tenantId)) {

            if (isClientIdSet && StringUtils.hasText(clientSecret)) {
                return new ClientSecretCredentialBuilder().clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();
            }

            final String clientCertificatePath = map.get(AzureJDBCPropertiesUtils.Mapping.clientCertificatePath.propertyKey());
            final String clientCertificatePassword = map.get(AzureJDBCPropertiesUtils.Mapping.clientCertificatePassword.propertyKey());
            if (StringUtils.hasText(clientCertificatePath)) {
                ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder().tenantId(tenantId)
                    .clientId(clientId);

                if (StringUtils.hasText(clientCertificatePassword)) {
                    builder.pfxCertificate(clientCertificatePath, clientCertificatePassword);
                } else {
                    builder.pemCertificate(clientCertificatePath);
                }

                return builder.build();
            }
        }

        final String username = map.get(AzureJDBCPropertiesUtils.Mapping.username.propertyKey());
        final String password = map.get(AzureJDBCPropertiesUtils.Mapping.password.propertyKey());
        if (isClientIdSet && StringUtils.hasText(username)
            && StringUtils.hasText(password)) {
            return new UsernamePasswordCredentialBuilder().username(username)
                .password(password)
                .clientId(clientId)
                .tenantId(tenantId)
                .build();
        }
        final String managedIdentityEnabled = map.get(AzureJDBCPropertiesUtils.Mapping.managedIdentityEnabled.propertyKey());

        if ("true".equals(managedIdentityEnabled)) {
            ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
            if (isClientIdSet) {
                builder.clientId(clientId);
            }
            return builder.build();
        }
        return null;
    }

}
