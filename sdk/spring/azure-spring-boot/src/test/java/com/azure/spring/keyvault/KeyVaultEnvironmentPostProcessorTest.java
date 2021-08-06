// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.keyvault.KeyVaultProperties.Property.CERTIFICATE_PATH;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CLIENT_ID;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CLIENT_KEY;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.TENANT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyVaultEnvironmentPostProcessorTest {
    private KeyVaultEnvironmentPostProcessorHelper keyVaultEnvironmentPostProcessorHelper;
    private MockEnvironment environment;
    private MutablePropertySources propertySources;
    private Map<String, Object> testProperties = new HashMap<>();

    @BeforeEach
    public void setup() {
        environment = new MockEnvironment();
        testProperties.clear();
        propertySources = environment.getPropertySources();
    }

    @Test
    public void testGetCredentialsWhenUsingClientAndKey() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_KEY), "mySecret");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "myid");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }

    @Test
    public void testGetCredentialsWhenPFXCertConfigured() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "myid");
        testProperties.put(KeyVaultProperties.getPropertyName(CERTIFICATE_PATH), "fake-pfx-cert.pfx");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials, IsInstanceOf.instanceOf(ClientCertificateCredential.class));
    }

    @Test
    public void testGetCredentialsWhenMSIEnabledInAppService() {
        testProperties.put("MSI_ENDPOINT", "fakeendpoint");
        testProperties.put("MSI_SECRET", "fakesecret");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    public void testGetCredentialsWhenMSIEnabledInVMWithClientId() {
        testProperties.put("azure.keyvault.client-id", "aaaa-bbbb-cccc-dddd");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    public void testGetCredentialsWhenMSIEnabledInVMWithoutClientId() {
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    public void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    public void postProcessorOrderConfigurable() {
        final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OrderedProcessConfig.class))
            .withPropertyValues("azure.keyvault.uri=fakeuri", "azure.keyvault.enabled=true");

        contextRunner.run(context -> {
            assertThat("Configured order for KeyVaultEnvironmentPostProcessor is different with default order "
                    + "value.",
                KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER != OrderedProcessConfig.TEST_ORDER);
            assertEquals(OrderedProcessConfig.TEST_ORDER,
                context.getBean(KeyVaultEnvironmentPostProcessor.class).getOrder(), "KeyVaultEnvironmentPostProcessor"
                    + " order should be changed.");
        });
    }

    /**
     * Test the multiple key vault support.
     */
    @Test
    public void testMultipleKeyVaults() {
        testProperties.put("azure.keyvault.order", "myvault, myvault2");
        testProperties.put("azure.keyvault.myvault.client-id", "aaaa-bbbb-cccc-dddd");
        testProperties.put("azure.keyvault.myvault.client-key", "mySecret");
        testProperties.put("azure.keyvault.myvault.tenant-id", "myid");
        testProperties.put("azure.keyvault.myvault2.client-id", "aaaa-bbbb-cccc-dddd");
        testProperties.put("azure.keyvault.myvault2.client-key", "mySecret");
        testProperties.put("azure.keyvault.myvault2.tenant-id", "myid");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials("myvault");
        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));

        final TokenCredential credentials2 = keyVaultEnvironmentPostProcessorHelper.getCredentials("myvault2");
        assertThat(credentials2, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }
}

@Configuration
class OrderedProcessConfig {
    static final int TEST_ORDER = KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER + 1;

    @Bean
    @Primary
    public KeyVaultEnvironmentPostProcessor getProcessor() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
        processor.setOrder(TEST_ORDER);
        return processor;
    }
}

