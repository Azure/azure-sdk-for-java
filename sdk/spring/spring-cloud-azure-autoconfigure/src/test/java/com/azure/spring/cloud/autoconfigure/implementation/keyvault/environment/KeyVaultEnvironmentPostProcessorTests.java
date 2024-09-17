// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

class KeyVaultEnvironmentPostProcessorTests {

    private static final String NAME_0 = "name_0";
    private static final String NAME_1 = "name_1";
    private static final String ENDPOINT_0 = "https://test0.vault.azure.net/";
    private static final String ENDPOINT_1 = "https://test1.vault.azure.net/";

    private final SpringApplication application = new SpringApplication();
    private KeyVaultEnvironmentPostProcessor processor;
    private MockEnvironment environment;
    private MutablePropertySources propertySources;
    private ConfigurableBootstrapContext context;

    @BeforeEach
    void beforeEach() {
        processor = spy(new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), null));
        environment = new MockEnvironment();
        propertySources = environment.getPropertySources();
        SecretClient secretClient = mock(SecretClient.class);
        doReturn(secretClient).when(processor).buildSecretClient(any(AzureKeyVaultSecretProperties.class));
    }

    @Test
    void testContextRegisterWithTokenCredentialRegistered() {
        context = mock(ConfigurableBootstrapContext.class);
        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(context.get(TokenCredential.class)).thenReturn(tokenCredential);
        when(context.isRegistered(TokenCredential.class)).thenReturn(true);
        processor = spy(new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), context));
        AzureKeyVaultSecretProperties secretProperties = new AzureKeyVaultSecretProperties();
        secretProperties.setEndpoint(ENDPOINT_0);

        processor.buildSecretClient(secretProperties);

        verify(context, times(1)).get(TokenCredential.class);
    }

    @Test
    void testContextRegisterWithoutTokenCredentialRegistered() {
        context = mock(ConfigurableBootstrapContext.class);
        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(context.get(TokenCredential.class)).thenReturn(tokenCredential);
        when(context.isRegistered(TokenCredential.class)).thenReturn(false);
        processor = spy(new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), context));
        AzureKeyVaultSecretProperties secretProperties = new AzureKeyVaultSecretProperties();
        secretProperties.setEndpoint(ENDPOINT_0);

        processor.buildSecretClient(secretProperties);

        verify(context, never()).get(TokenCredential.class);
    }

    @Test
    void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), null);
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.ORDER);
    }

    @Test
    void insertSinglePropertySourceTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(NAME_0));
    }

    @Test
    void insertMultiplePropertySourceTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(NAME_0));
        assertTrue(propertySources.contains(NAME_1));
    }

    @Test
    void keyVaultClientNotExistInClassPathTest() {
        try (MockedStatic<ClassUtils> classUtils = mockStatic(ClassUtils.class)) {
            classUtils.when(() -> ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient", getClass().getClassLoader()))
                    .thenReturn(false);
            environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
            environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
            environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
            environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
            processor.postProcessEnvironment(environment, application);
            assertFalse(propertySources.contains(NAME_0));
        }
    }

    @Test
    void disableAllPropertySourceTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "false");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertFalse(propertySources.contains(NAME_0));
        assertFalse(propertySources.contains(NAME_1));
    }

    @Test
    void emptyPropertySourceListTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        processor.postProcessEnvironment(environment, application);
        assertEquals(1, propertySources.size());
    }

    @Test
    void disableSpecificOnePropertySourceTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "false");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertFalse(propertySources.contains(NAME_0));
        assertTrue(propertySources.contains(NAME_1));
    }

    @Test
    void enableByDefaultTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(NAME_0));
        assertTrue(propertySources.contains(NAME_1));
    }

    @Test
    void endPointNotConfiguredTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertFalse(propertySources.contains(NAME_0));
        assertTrue(propertySources.contains(NAME_1));
    }

    @Test
    void defaultPropertySourceNameTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        assertTrue(propertySources.contains(processor.buildPropertySourceName(0)));
        assertTrue(propertySources.contains(processor.buildPropertySourceName(1)));
    }

    @Test
    void duplicatePropertySourceNameTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", "test");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", "test");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        assertThrows(IllegalStateException.class, () -> processor.postProcessEnvironment(environment, application));
    }

    @Test
    void keyVaultPropertySourceHasHighestPriorityIfEnvironmentPropertySourceNotExistTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        processor.postProcessEnvironment(environment, application);
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        assertEquals(NAME_0, iterator.next().getName());
        assertTrue(iterator.hasNext());
    }

    @Test
    void keyVaultPropertySourceHasLowerPriorityThanEnvironmentPropertySourceTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        propertySources.addFirst(new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, Collections.emptyMap()));
        processor.postProcessEnvironment(environment, application);
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        while (iterator.hasNext()) {
            PropertySource<?> propertySource = iterator.next();
            if (SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(propertySource.getName())) {
                break;
            }
        }
        assertEquals(NAME_0, iterator.next().getName());
        assertTrue(iterator.hasNext());
    }

    @Test
    void keyVaultPropertySourceOrderTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].name", NAME_1);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[1].endpoint", ENDPOINT_1);
        processor.postProcessEnvironment(environment, application);
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        assertEquals(NAME_0, iterator.next().getName());
        assertEquals(NAME_1, iterator.next().getName());
        assertTrue(iterator.hasNext());
    }

    @Test
    void globalPropertiesTakeEffectIfSpecificPropertiesNotSetTest() {
        final String globalHostname = "globalHostname";
        final String globalApplicationId = "globalApplicationId";
        final String globalTenantId = "globalTenantId";
        final String globalUsername = "globalUsername";
        final int globalMaxRetries = 1;
        environment.setProperty("spring.cloud.azure.client.application-id", globalApplicationId);
        environment.setProperty("spring.cloud.azure.credential.username", globalUsername);
        environment.setProperty("spring.cloud.azure.profile.tenant-id", globalTenantId);
        environment.setProperty("spring.cloud.azure.proxy.hostname", globalHostname);
        environment.setProperty("spring.cloud.azure.retry.fixed.max-retries", "" + globalMaxRetries);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        AzureKeyVaultSecretProperties secretProperties = processor.loadProperties(environment);
        AzureKeyVaultPropertySourceProperties properties = secretProperties.getPropertySources().get(0);
        assertEquals(globalUsername, properties.getCredential().getUsername());
        assertEquals(globalApplicationId, properties.getClient().getApplicationId());
        assertEquals(globalTenantId, properties.getProfile().getTenantId());
        assertEquals(globalHostname, properties.getProxy().getHostname());
        assertEquals(globalMaxRetries, properties.getRetry().getFixed().getMaxRetries());
    }

    @Test
    void specificPropertiesHasHigherPriorityThanGlobalPropertiesTest() {
        final String globalHostname = "globalHostname";
        final String globalApplicationId = "globalApplicationId";
        final String globalTenantId = "globalTenantId";
        final String globalUsername = "globalUsername";
        final int globalMaxRetries = 1;
        final String specificHostname = "specificHostname";
        final String specificApplicationId = "specificApplicationId";
        final String specificTenantId = "specificTenantId";
        final String specificUsername = "specificUsername";
        final int specificMaxRetries = 2;
        environment.setProperty("spring.cloud.azure.client.application-id", globalApplicationId);
        environment.setProperty("spring.cloud.azure.credential.username", globalUsername);
        environment.setProperty("spring.cloud.azure.profile.tenant-id", globalTenantId);
        environment.setProperty("spring.cloud.azure.proxy.hostname", globalHostname);
        environment.setProperty("spring.cloud.azure.retry.fixed.max-retries", "" + globalMaxRetries);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].client.application-id", specificApplicationId);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].credential.username", specificUsername);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id", specificTenantId);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].proxy.hostname", specificHostname);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].retry.fixed.max-retries", "" + specificMaxRetries);
        AzureKeyVaultSecretProperties secretProperties = processor.loadProperties(environment);
        AzureKeyVaultPropertySourceProperties properties = secretProperties.getPropertySources().get(0);
        assertEquals(specificUsername, properties.getCredential().getUsername());
        assertEquals(specificApplicationId, properties.getClient().getApplicationId());
        assertEquals(specificTenantId, properties.getProfile().getTenantId());
        assertEquals(specificHostname, properties.getProxy().getHostname());
        assertEquals(specificMaxRetries, properties.getRetry().getFixed().getMaxRetries());
    }

    @Test
    void challengeResourceVerificationEnabledCanBeSetAsFalseTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].challenge-resource-verification-enabled", "false");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        AzureKeyVaultSecretProperties secretProperties = processor.loadProperties(environment);
        AzureKeyVaultPropertySourceProperties properties = secretProperties.getPropertySources().get(0);
        assertTrue(secretProperties.isChallengeResourceVerificationEnabled());
        assertFalse(properties.isChallengeResourceVerificationEnabled());
    }

    @Test
    void challengeResourceVerificationEnabledIsSetByDefaultTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        AzureKeyVaultSecretProperties secretProperties = processor.loadProperties(environment);
        AzureKeyVaultPropertySourceProperties properties = secretProperties.getPropertySources().get(0);
        assertTrue(secretProperties.isChallengeResourceVerificationEnabled());
        assertTrue(properties.isChallengeResourceVerificationEnabled());
    }

    @Disabled("Disable it to unblock Azure Dev Ops pipeline: https://dev.azure.com/azure-sdk/public/_build/results?buildId=1434354&view=logs&j=c1fb1ddd-7688-52ac-4c5f-1467e51181f3")
    @Test
    void buildKeyVaultPropertySourceWithExceptionTest() {
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-source-enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].enabled", "true");
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].name", NAME_0);
        environment.setProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint", ENDPOINT_0);
        assertThrows(IllegalStateException.class,
                () -> new KeyVaultEnvironmentPostProcessor(new DeferredLogs(), null).postProcessEnvironment(environment, application));
    }
}

