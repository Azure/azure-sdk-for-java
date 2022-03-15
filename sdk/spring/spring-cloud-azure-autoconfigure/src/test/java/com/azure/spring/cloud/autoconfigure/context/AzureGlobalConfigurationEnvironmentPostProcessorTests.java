// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.time.Duration;
import java.util.Properties;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_AUTHORITY_HOST;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLOUD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_PASSWORD;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_TENANT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_USERNAME;
import static com.azure.core.util.Configuration.PROPERTY_NO_PROXY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureGlobalConfigurationEnvironmentPostProcessorTests {

    @Test
    void springPropertyShouldHaveValueIfAzureCoreEnvSet() {
        PropertiesPropertySource propertiesPropertySource = buildTestProperties(PROPERTY_AZURE_CLIENT_ID, "test-client-id");

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        assertEquals("test-client-id", environment.getProperty(PROPERTY_AZURE_CLIENT_ID));
        assertEquals("test-client-id", environment.getProperty("spring.cloud.azure.credential.client-id"));
    }

    @Test
    void springPropertyShouldHaveValueIfAzureKeyVaultEnvSet() {
        PropertiesPropertySource propertiesPropertySource = buildTestProperties("AZURE_KEYVAULT_ENDPOINT", "test-endpoint");

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        assertEquals("test-endpoint", environment.getProperty("AZURE_KEYVAULT_ENDPOINT"));
        assertEquals("test-endpoint", environment.getProperty("spring.cloud.azure.keyvault.secret.endpoint"));
        assertEquals("test-endpoint", environment.getProperty("spring.cloud.azure.keyvault.certificate.endpoint"));
    }

    @Test
    void springPropertyShouldHaveValueIfAzureEventHubsEnvSet() {
        PropertiesPropertySource propertiesPropertySource = buildTestProperties("AZURE_EVENT_HUBS_CONNECTION_STRING", "test-connection-string");

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        assertEquals("test-connection-string", environment.getProperty("AZURE_EVENT_HUBS_CONNECTION_STRING"));
        assertEquals("test-connection-string", environment.getProperty("spring.cloud.azure.eventhubs.connection-string"));
    }

    @Test
    void azureCoreEnvShouldNotBeTakenIfSpringPropertiesSet() {
        Properties properties = new Properties();
        properties.put(PROPERTY_AZURE_CLIENT_ID, "core-client-id");
        properties.put("spring.cloud.azure.credential.client-id", "spring-client-id");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        assertEquals("core-client-id", environment.getProperty(PROPERTY_AZURE_CLIENT_ID));
        assertEquals("spring-client-id", environment.getProperty("spring.cloud.azure.credential.client-id"));
    }

    @Test
    void azureSdkEnvShouldNotBeTakenIfSpringPropertiesSet() {
        Properties properties = new Properties();
        properties.put("AZURE_KEYVAULT_ENDPOINT", "sdk-endpoint");
        properties.put("spring.cloud.azure.keyvault.secret.endpoint", "spring-endpoint");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);

        assertEquals("sdk-endpoint", environment.getProperty("AZURE_KEYVAULT_ENDPOINT"));
        assertEquals("spring-endpoint", environment.getProperty("spring.cloud.azure.keyvault.secret.endpoint"));
    }

    @Test
    void azureCoreEnvShouldBindCorrect() {
        Properties properties = new Properties();
        properties.put(PROPERTY_AZURE_CLIENT_ID, "core-client-id");
        properties.put(PROPERTY_AZURE_CLIENT_SECRET, "core-client-secret");
        properties.put(PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, "core-client-cert-path");
        properties.put(PROPERTY_AZURE_USERNAME, "core-username");
        properties.put(PROPERTY_AZURE_PASSWORD, "core-password");
        properties.put(PROPERTY_AZURE_TENANT_ID, "core-tenant-id");
        properties.put(PROPERTY_AZURE_SUBSCRIPTION_ID, "core-sub-id");
        properties.put(PROPERTY_AZURE_CLOUD, "other");
        properties.put(PROPERTY_AZURE_AUTHORITY_HOST, "aad");
        properties.put(PROPERTY_AZURE_REQUEST_RETRY_COUNT, 3);
        properties.put(PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, "headers");
        properties.put(PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, 1000);
        properties.put(PROPERTY_AZURE_REQUEST_READ_TIMEOUT, 2000);
        properties.put(PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, 3000);
        properties.put(PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, 4000);
        properties.put(PROPERTY_NO_PROXY, "localhost");

        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);
        AzureGlobalProperties globalProperties = Binder.get(environment).bind(AzureGlobalProperties.PREFIX, AzureGlobalProperties.class).get();

        assertEquals("core-client-id", globalProperties.getCredential().getClientId());
        assertEquals("core-client-secret", globalProperties.getCredential().getClientSecret());
        assertEquals("core-client-cert-path", globalProperties.getCredential().getClientCertificatePath());
        assertEquals("core-username", globalProperties.getCredential().getUsername());
        assertEquals("core-password", globalProperties.getCredential().getPassword());
        assertEquals("core-tenant-id", globalProperties.getProfile().getTenantId());
        assertEquals("core-sub-id", globalProperties.getProfile().getSubscriptionId());
        assertEquals(AzureProfileOptionsProvider.CloudType.OTHER, globalProperties.getProfile().getCloudType());
        assertEquals("aad", globalProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(3, globalProperties.getRetry().getExponential().getMaxRetries());
        assertEquals(3, globalProperties.getRetry().getFixed().getMaxRetries());
        assertEquals(HttpLogDetailLevel.HEADERS, globalProperties.getClient().getHttp().getLogging().getLevel());
        assertEquals(Duration.ofSeconds(1), globalProperties.getClient().getHttp().getConnectTimeout());
        assertEquals(Duration.ofSeconds(2), globalProperties.getClient().getHttp().getReadTimeout());
        assertEquals(Duration.ofSeconds(3), globalProperties.getClient().getHttp().getWriteTimeout());
        assertEquals(Duration.ofSeconds(4), globalProperties.getClient().getHttp().getResponseTimeout());
        assertEquals("localhost", globalProperties.getProxy().getHttp().getNonProxyHosts());

    }

    @Test
    void azureClientIdFromEnv() {
        Properties properties = new Properties();
        properties.put(PROPERTY_AZURE_CLIENT_ID, "client-id-from-env");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);
        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);
        AzureGlobalProperties globalProperties = Binder.get(environment)
            .bind(AzureGlobalProperties.PREFIX, AzureGlobalProperties.class).get();
        assertEquals("client-id-from-env", globalProperties.getCredential().getClientId());
        assertNull(globalProperties.getCredential().getUsername());
    }

    @Test
    void azureClientIdFromUserConfig() {
        Properties properties = new Properties();
        properties.put(PROPERTY_AZURE_CLIENT_ID, "client-id-from-env");
        properties.put(AzureGlobalProperties.PREFIX + ".credential.client-id", "custom-client-id");
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);
        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);
        AzureGlobalProperties globalProperties = Binder.get(environment)
            .bind(AzureGlobalProperties.PREFIX, AzureGlobalProperties.class).get();
        assertEquals("custom-client-id", globalProperties.getCredential().getClientId());
        assertNull(globalProperties.getCredential().getUsername());
    }

    @Test
    void azureSdkEnvShouldBindCorrect() {
        Properties properties = new Properties();
        properties.put("AZURE_KEYVAULT_ENDPOINT", "test-endpoint");
        properties.put("AZURE_EVENT_HUBS_CONNECTION_STRING", "test-connection-str");

        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("test-properties", properties);

        ConfigurableEnvironment environment = getEnvironment(propertiesPropertySource);
        AzureEventHubsProperties eventHubsProperties = Binder.get(environment).bind(AzureEventHubsProperties.PREFIX, AzureEventHubsProperties.class).get();
        AzureKeyVaultSecretProperties keyVaultSecretProperties = Binder.get(environment).bind(AzureKeyVaultSecretProperties.PREFIX, AzureKeyVaultSecretProperties.class).get();

        assertEquals("test-connection-str", eventHubsProperties.getConnectionString());
        assertEquals("test-endpoint", keyVaultSecretProperties.getEndpoint());
    }


    private PropertiesPropertySource buildTestProperties(String key, String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return new PropertiesPropertySource("test-properties", properties);
    }


    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource) {
        return getEnvironment(propertiesPropertySource, null);
    }

    private ConfigurableEnvironment getEnvironment(PropertiesPropertySource propertiesPropertySource,
                                                   EnvironmentPostProcessor environmentPostProcessor) {

        ConfigurableEnvironment environment = new StandardServletEnvironment();

        if (propertiesPropertySource != null) {
            environment.getPropertySources().addFirst(propertiesPropertySource);
        }

        if (environmentPostProcessor == null) {
            environmentPostProcessor = new AzureGlobalConfigurationEnvironmentPostProcessor(new DeferredLog());
        }

        environmentPostProcessor.postProcessEnvironment(environment, null);

        return environment;
    }

}
