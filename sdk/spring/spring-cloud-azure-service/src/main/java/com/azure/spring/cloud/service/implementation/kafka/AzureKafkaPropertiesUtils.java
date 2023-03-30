// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.kafka;

import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.service.implementation.jaas.Jaas;
import com.azure.spring.cloud.service.implementation.jaas.JaasResolver;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Store the constants for customized Azure properties with Kafka.
 */
public final class AzureKafkaPropertiesUtils {
    private AzureKafkaPropertiesUtils() {
    }

    public static final String AZURE_TOKEN_CREDENTIAL = "azure.token.credential";

    private static final PropertyMapper PROPERTY_MAPPER = new PropertyMapper();
    static final String CREDENTIAL_PREFIX = "azure.credential.";
    static final String PROFILE_PREFIX = "azure.profile.";
    static final String ENVIRONMENT_PREFIX = PROFILE_PREFIX + "environment.";

    public static void copyJaasPropertyToAzureProperties(String source, AzurePasswordlessProperties target) {
        JaasResolver resolver = new JaasResolver();
        Jaas jaas = resolver.resolve(source).orElse(new Jaas(OAuthBearerLoginModule.class.getName()));
        Map<String, String> map = jaas.getOptions();
        for (AzureKafkaPasswordlessPropertiesMapping m : AzureKafkaPasswordlessPropertiesMapping.values()) {
            PROPERTY_MAPPER.from(map.get(m.propertyKey)).to(v -> m.setter.accept(target, v));
        }
    }

    public enum AzureKafkaPasswordlessPropertiesMapping {

        CLIENT_CERTIFICATE_PASSWORD(CREDENTIAL_PREFIX + "client-certificate-password",
            p -> p.getCredential().getClientCertificatePassword(),
            (p, s) -> p.getCredential().setClientCertificatePassword(s)),

        CLIENT_CERTIFICATE_PATH(CREDENTIAL_PREFIX + "client-certificate-path",
            p -> p.getCredential().getClientCertificatePath(),
            (p, s) -> p.getCredential().setClientCertificatePath(s)),

        CLIENT_ID(CREDENTIAL_PREFIX + "client-id",
            p -> p.getCredential().getClientId(),
            (p, s) -> p.getCredential().setClientId(s)),

        CLIENT_SECRET(CREDENTIAL_PREFIX + "client-secret",
            p -> p.getCredential().getClientSecret(),
            (p, s) -> p.getCredential().setClientSecret(s)),

        MANAGED_IDENTITY_ENABLED(CREDENTIAL_PREFIX + "managed-identity-enabled",
            p -> String.valueOf(p.getCredential().isManagedIdentityEnabled()),
            (p, s) -> p.getCredential().setManagedIdentityEnabled(Boolean.valueOf(s))),

        PASSWORD(CREDENTIAL_PREFIX + "password",
            p -> p.getCredential().getPassword(),
            (p, s) -> p.getCredential().setPassword(s)),

        USERNAME(CREDENTIAL_PREFIX + "username",
            p -> p.getCredential().getUsername(),
            (p, s) -> p.getCredential().setUsername(s)),

        CLOUD_TYPE(PROFILE_PREFIX + "cloud-type",
            p -> Optional.ofNullable(p.getProfile().getCloudType()).map(v -> v.name()).orElse(null),
            (p, s) -> p.getProfile().setCloudType(AzureProfileOptionsProvider.CloudType.fromString(s))),

        ACTIVE_DIRECTORY_ENDPOINT(ENVIRONMENT_PREFIX + "active-directory-endpoint",
            p -> p.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryEndpoint(s)),

        ACTIVE_DIRECTORY_GRAPH_API_VERSION(ENVIRONMENT_PREFIX + "active-directory-graph-api-version",
            p -> p.getProfile().getEnvironment().getActiveDirectoryGraphApiVersion(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryGraphApiVersion(s)),

        ACTIVE_DIRECTORY_GRAPH_ENDPOINT(ENVIRONMENT_PREFIX + "active-directory-graph-endpoint",
            p -> p.getProfile().getEnvironment().getActiveDirectoryGraphEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryGraphEndpoint(s)),

        ACTIVE_DIRECTORY_RESOURCE_ID(ENVIRONMENT_PREFIX + "active-directory-resource-id",
            p -> p.getProfile().getEnvironment().getActiveDirectoryResourceId(),
            (p, s) -> p.getProfile().getEnvironment().setActiveDirectoryResourceId(s)),

        AZURE_APPLICATION_INSIGHTS_ENDPOINT(ENVIRONMENT_PREFIX + "azure-application-insights-endpoint",
            p -> p.getProfile().getEnvironment().getAzureApplicationInsightsEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setAzureApplicationInsightsEndpoint(s)),

        AZURE_DATA_LAKE_ANALYTICS_CATALOG_AND_JOB_ENDPOINT_SUFFIX(ENVIRONMENT_PREFIX + "azure-data-lake-analytics-catalog-and-job-endpoint-suffix",
            p -> p.getProfile().getEnvironment().getAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setAzureDataLakeAnalyticsCatalogAndJobEndpointSuffix(s)),

        AZURE_DATA_LAKE_STORE_FILE_SYSTEM_ENDPOINT_SUFFIX(ENVIRONMENT_PREFIX + "azure_data_lake_store_file_system_endpoint_suffix",
            p -> p.getProfile().getEnvironment().getAzureDataLakeStoreFileSystemEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setAzureDataLakeStoreFileSystemEndpointSuffix(s)),

        AZURE_LOG_ANALYTICS_ENDPOINT(ENVIRONMENT_PREFIX + "azure_log_analytics_endpoint",
            p -> p.getProfile().getEnvironment().getAzureLogAnalyticsEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setAzureLogAnalyticsEndpoint(s)),

        DATA_LAKE_ENDPOINT_RESOURCE_ID(ENVIRONMENT_PREFIX + "data_lake_endpoint_resource_id",
            p -> p.getProfile().getEnvironment().getDataLakeEndpointResourceId(),
            (p, s) -> p.getProfile().getEnvironment().setDataLakeEndpointResourceId(s)),

        GALLERY_ENDPOINT(ENVIRONMENT_PREFIX + "gallery_endpoint",
            p -> p.getProfile().getEnvironment().getGalleryEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setGalleryEndpoint(s)),

        KEY_VAULT_DNS_SUFFIX(ENVIRONMENT_PREFIX + "key_vault_dns_suffix",
            p -> p.getProfile().getEnvironment().getKeyVaultDnsSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setKeyVaultDnsSuffix(s)),

        MANAGEMENT_ENDPOINT(ENVIRONMENT_PREFIX + "management_endpoint",
            p -> p.getProfile().getEnvironment().getManagementEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setManagementEndpoint(s)),

        MICROSOFT_GRAPH_ENDPOINT(ENVIRONMENT_PREFIX + "microsoft_graph_endpoint",
            p -> p.getProfile().getEnvironment().getMicrosoftGraphEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setMicrosoftGraphEndpoint(s)),

        PORTAL(ENVIRONMENT_PREFIX + "portal",
            p -> p.getProfile().getEnvironment().getPortal(),
            (p, s) -> p.getProfile().getEnvironment().setPortal(s)),

        PUBLISHING_PROFILE(ENVIRONMENT_PREFIX + "publishing_profile",
            p -> p.getProfile().getEnvironment().getPublishingProfile(),
            (p, s) -> p.getProfile().getEnvironment().setPublishingProfile(s)),

        RESOURCE_MANAGER_ENDPOINT(ENVIRONMENT_PREFIX + "resource_manager_endpoint",
            p -> p.getProfile().getEnvironment().getResourceManagerEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setResourceManagerEndpoint(s)),

        SQL_MANAGEMENT_ENDPOINT(ENVIRONMENT_PREFIX + "sql_management_endpoint",
            p -> p.getProfile().getEnvironment().getSqlManagementEndpoint(),
            (p, s) -> p.getProfile().getEnvironment().setSqlManagementEndpoint(s)),

        SQL_SERVER_HOSTNAME_SUFFIX(ENVIRONMENT_PREFIX + "sql_server_hostname_suffix",
            p -> p.getProfile().getEnvironment().getSqlServerHostnameSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setSqlServerHostnameSuffix(s)),

        STORAGE_ENDPOINT_SUFFIX(ENVIRONMENT_PREFIX + "storage_endpoint_suffix",
            p -> p.getProfile().getEnvironment().getStorageEndpointSuffix(),
            (p, s) -> p.getProfile().getEnvironment().setStorageEndpointSuffix(s)),

        SUBSCRIPTION_ID(PROFILE_PREFIX + "subscription_id",
            p -> p.getProfile().getSubscriptionId(),
            (p, s) -> p.getProfile().setSubscriptionId(s)),

        TENANT_ID(PROFILE_PREFIX + "tenant_id",
            p -> p.getProfile().getTenantId(),
            (p, s) -> p.getProfile().setTenantId(s));

        private static final List<String> PROPERTY_KEYS = buildPropertyKeys();

        public static List<String> getPropertyKeys() {
            return PROPERTY_KEYS;
        }

        private static List<String> buildPropertyKeys() {
            return Collections.unmodifiableList(Stream.of(AzureKafkaPasswordlessPropertiesMapping.values()).map(m -> m.propertyKey).collect(Collectors.toList()));
        }

        private String propertyKey;
        private Function<AzureProperties, String> getter;
        private BiConsumer<AzurePasswordlessProperties, String> setter;

        AzureKafkaPasswordlessPropertiesMapping(String propertyKey, Function<AzureProperties, String> getter, BiConsumer<AzurePasswordlessProperties,
            String> setter) {
            this.propertyKey = propertyKey;
            this.getter = getter;
            this.setter = setter;
        }

        public String propertyKey() {
            return propertyKey;
        }

        public Function<AzureProperties, String> getter() {
            return getter;
        }

        public BiConsumer<AzurePasswordlessProperties, String> setter() {
            return setter;
        }

    }

}
