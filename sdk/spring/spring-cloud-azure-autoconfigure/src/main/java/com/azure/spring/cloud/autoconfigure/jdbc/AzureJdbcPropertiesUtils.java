// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Store the constants for customized Azure properties with Kafka.
 */
public final class AzureJdbcPropertiesUtils {

    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    static final String CREDENTIAL_PREFIX = "azure.credential.";
    static final String PROFILE_PREFIX = "azure.profile.";
    static final String ENVIRONMENT_PREFIX = PROFILE_PREFIX + "environment.";

    public static void convertAzurePropertiesToConfigMap(AzureJdbcProperties source,
                                                         Map<String, String> target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(m.getter.apply(source)).to(p -> target.putIfAbsent(m.propertyKey, p));
        }
    }

    public static void convertConfigMapToAzureProperties(Map<String, ?> source,
                                                         AzureJdbcProperties target) {
        for (Mapping m : Mapping.values()) {
            PROPERTY_MAPPER.from(source.get(m.propertyKey)).to(p -> m.setter.accept(target, (String) p));
        }
    }

    enum Mapping {

        clientCertificatePassword(CREDENTIAL_PREFIX + "client-certificate-password",
            p -> p.getCredential().getClientCertificatePassword(),
            (p, s) -> p.getCredential().setClientCertificatePassword(s)),

        clientCertificatePath(CREDENTIAL_PREFIX + "client-certificate-path",
            p -> p.getCredential().getClientCertificatePath(),
            (p, s) -> p.getCredential().setClientCertificatePath(s)),

        clientId(CREDENTIAL_PREFIX + "client-id",
            p -> p.getCredential().getClientId(),
            (p, s) -> p.getCredential().setClientId(s)),

        clientSecret(CREDENTIAL_PREFIX + "client-secret",
            p -> p.getCredential().getClientSecret(),
            (p, s) -> p.getCredential().setClientSecret(s)),

        managedIdentityEnabled(CREDENTIAL_PREFIX + "managed-identity-enabled",
            p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
            (p, s) -> p.getCredential().setManagedIdentityEnabled(Boolean.valueOf(s))),

        password(CREDENTIAL_PREFIX + "password",
            p -> p.getCredential().getPassword(),
            (p, s) -> p.getCredential().setPassword(s)),

        username(CREDENTIAL_PREFIX + "username",
            p -> p.getCredential().getUsername(),
            (p, s) -> p.getCredential().setUsername(s)),

        cloudType(PROFILE_PREFIX + "cloud-type", p -> p.getProfile().getCloudType().name(),
            (p, s) -> p.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.get(s))),

        activeDirectoryEndpoint(ENVIRONMENT_PREFIX + "active-directory-endpoint",
            p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryEndpoint(s)),

        activeDirectoryGraphApiVersion(ENVIRONMENT_PREFIX + "active-directory-graph-api-version",
            p -> p.getProfile().getEnvironment().getActiveDirectoryGraphApiVersion(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion(s)),

        activeDirectoryGraphEndpoint(ENVIRONMENT_PREFIX + "active-directory-graph-endpoint",
            p -> p.getProfile().getEnvironment().getActiveDirectoryGraphEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryGraphEndpoint(s)),

        activeDirectoryResourceId(ENVIRONMENT_PREFIX + "active-directory-resource-id",
            p -> p.getProfile().getEnvironment().getActiveDirectoryResourceId(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryResourceId(s)),

        azureApplicationInsightsEndpoint(ENVIRONMENT_PREFIX + "azure-application-insights-endpoint",
            p -> p.getProfile().getEnvironment().getAzureApplicationInsightsEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setAzureApplicationInsightsEndpoint(s)),

        azureDataLakeAnalyticsCatalogAndJobEndpointSuffix(ENVIRONMENT_PREFIX + "azure-data-lake-analytics-catalog-and-job-endpoint-suffix",
            p -> p.getProfile().getEnvironment().getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(s)),

        azureDataLakeStoreFileSystemEndpointSuffix(ENVIRONMENT_PREFIX + "azure-data-lake-store-file-system-endpoint-suffix",
            p -> p.getProfile().getEnvironment().getAzureDataLakeStoreFileSystemEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setAzureDataLakeStoreFileSystemEndpointSuffix(s)),

        azureLogAnalyticsEndpoint(ENVIRONMENT_PREFIX + "azure-log-analytics-endpoint",
            p -> p.getProfile().getEnvironment().getAzureLogAnalyticsEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setAzureLogAnalyticsEndpoint(s)),

        dataLakeEndpointResourceId(ENVIRONMENT_PREFIX + "data-lake-endpoint-resource-id",
            p -> p.getProfile().getEnvironment().getDataLakeEndpointResourceId(),
            (p, s) -> p.getProfile().getEnvironment().setDataLakeEndpointResourceId(s)),

        galleryEndpoint(ENVIRONMENT_PREFIX + "gallery-endpoint",
            p -> p.getProfile().getEnvironment().getGalleryEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setGalleryEndpoint(s)),

        keyVaultDnsSuffix(ENVIRONMENT_PREFIX + "key-vault-dns-suffix",
            p -> p.getProfile().getEnvironment().getKeyVaultDnsSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setKeyVaultDnsSuffix(s)),

        managementEndpoint(ENVIRONMENT_PREFIX + "management-endpoint",
            p -> p.getProfile().getEnvironment().getManagementEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setManagementEndpoint(s)),

        microsoftGraphEndpoint(ENVIRONMENT_PREFIX + "microsoft-graph-endpoint",
            p -> p.getProfile().getEnvironment().getMicrosoftGraphEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setMicrosoftGraphEndpoint(s)),

        portal(ENVIRONMENT_PREFIX + "portal",
            p -> p.getProfile().getEnvironment().getPortal(),
            (p, s) -> p.getProfile().getEnvironment().setPortal(s)),

        publishingProfile(ENVIRONMENT_PREFIX + "publishing-profile",
            p -> p.getProfile().getEnvironment().getPublishingProfile(),
            (p, s) -> p.getProfile().getEnvironment().setPublishingProfile(s)),

        resourceManagerEndpoint(ENVIRONMENT_PREFIX + "resource-manager-endpoint",
            p -> p.getProfile().getEnvironment().getResourceManagerEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setResourceManagerEndpoint(s)),

        sqlManagementEndpoint(ENVIRONMENT_PREFIX + "sql-management-endpoint",
            p -> p.getProfile().getEnvironment().getSqlManagementEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setSqlManagementEndpoint(s)),

        sqlServerHostnameSuffix(ENVIRONMENT_PREFIX + "sql-server-hostname-suffix",
            p -> p.getProfile().getEnvironment().getSqlServerHostnameSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setSqlServerHostnameSuffix(s)),

        storageEndpointSuffix(ENVIRONMENT_PREFIX + "storage-endpoint-suffix",
            p -> p.getProfile().getEnvironment().getStorageEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setStorageEndpointSuffix(s)),

        subscriptionId(PROFILE_PREFIX + "subscription-id",
            p -> p.getProfile().getSubscriptionId(),
            (p, s) -> p.getProfile().setSubscriptionId(s)),

        tenantId(PROFILE_PREFIX + "tenant-id",
            p -> p.getProfile().getTenantId(),
            (p, s) -> p.getProfile().setTenantId(s));

        private String propertyKey;
        private Function<AzureProperties, String> getter;
        private BiConsumer<AzureJdbcProperties, String> setter;

        Mapping(String propertyKey, Function<AzureProperties, String> getter, BiConsumer<AzureJdbcProperties,
            String> setter) {
            this.propertyKey = propertyKey;
            this.getter = getter;
            this.setter = setter;
        }

        String propertyKey() {
            return propertyKey;
        }

        Function<AzureProperties, String> getter() {
            return getter;
        }

        BiConsumer<AzureJdbcProperties, String> setter() {
            return setter;
        }

    }

}
