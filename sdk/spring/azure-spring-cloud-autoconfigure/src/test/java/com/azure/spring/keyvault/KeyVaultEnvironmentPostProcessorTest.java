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

import static com.azure.spring.keyvault.KeyVaultProperties.Property.AUTHORITY_HOST;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CERTIFICATE_PATH;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CLIENT_ID;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CLIENT_KEY;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.CLIENT_SECRET;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.ENABLED;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.ORDER;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.TENANT_ID;
import static com.azure.spring.keyvault.KeyVaultProperties.Property.URI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyVaultEnvironmentPostProcessorTest {

    private KeyVaultEnvironmentPostProcessorHelper keyVaultEnvironmentPostProcessorHelper;
    private MockEnvironment environment;
    private MutablePropertySources propertySources;
    private final Map<String, Object> testProperties = new HashMap<>();

    @BeforeEach
    void setup() {
        environment = new MockEnvironment();
        testProperties.clear();
        propertySources = environment.getPropertySources();
    }

    @Test
    void testGetCredentialsWhenUsingClientAndKey() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_KEY), "mySecret");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "myid");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }

    @Test
    void testGetCredentialsWhenPFXCertConfigured() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "myid");
        testProperties.put(KeyVaultProperties.getPropertyName(CERTIFICATE_PATH), "fake-pfx-cert.pfx");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials, IsInstanceOf.instanceOf(ClientCertificateCredential.class));
    }

    @Test
    void testGetCredentialsWhenMSIEnabledInAppService() {
        testProperties.put("MSI_ENDPOINT", "fakeendpoint");
        testProperties.put("MSI_SECRET", "fakesecret");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    void testGetCredentialsWhenMSIEnabledInVMWithClientId() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    void testGetCredentialsWhenMSIEnabledInVMWithoutClientId() {
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ManagedIdentityCredential.class));
    }

    @Test
    void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    void postProcessorOrderConfigurable() {
        final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OrderedProcessConfig.class))
            .withPropertyValues(KeyVaultProperties.getPropertyName(URI) + "=fakeuri",
                                KeyVaultProperties.getPropertyName(ENABLED) + "=true");

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
    void testMultipleKeyVaults() {
        testProperties.put(KeyVaultProperties.getPropertyName(ORDER), "myvault, myvault2");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault.credential.client-id", "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault.credential.client-secret", "mySecret");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault.credential.tenant-id", "myid");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault2.credential.client-id", "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault2.credential.client-secret", "mySecret");
        testProperties.put(KeyVaultProperties.PREFIX + ".myvault2.credential.tenant-id", "myid");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials("myvault");
        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));

        final TokenCredential credentials2 = keyVaultEnvironmentPostProcessorHelper.getCredentials("myvault2");
        assertThat(credentials2, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }

    @Test
    void testGetCredentialFromKeyVaultProperties() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "aaaa-bbbb-cccc-dddd");
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_KEY), "mySecret");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "myid");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }

    @Test
    void testGetCredentialFromCommonProperties() {
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_ID), "fake-client-id");
        testProperties.put(KeyVaultProperties.getPropertyName(CLIENT_KEY), "fake-client-secret");
        testProperties.put(KeyVaultProperties.getPropertyName(TENANT_ID), "fake-tenant-id");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();

        assertThat(credentials, IsInstanceOf.instanceOf(ClientSecretCredential.class));
    }

    @Test
    void testGetPropertyValue() {
        testProperties.put("spring.cloud.azure.credential.client-id", "client1");
        testProperties.put("spring.cloud.azure.credential.tenant-id", "tenant1");
        testProperties.put("spring.cloud.azure.credential.client-certificate-path", "cert1");

        testProperties.put("spring.cloud.azure.keyvault.propertysource.credential.client-secret", "secret2");
        testProperties.put("spring.cloud.azure.keyvault.propertysource.credential.tenant-id", "tenant2");
        testProperties.put("spring.cloud.azure.keyvault.propertysource.mykeyvault.credential.client-certificate-path", "cert2");

        testProperties.put("spring.cloud.azure.keyvault.propertysource.uri", "uri1");

        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);

        String clientId = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", CLIENT_ID);
        String clientSecert = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", CLIENT_SECRET);
        String tenantId = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", TENANT_ID);
        String authorityHost = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", AUTHORITY_HOST);
        String certificatePath = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("mykeyvault", CERTIFICATE_PATH);
        String uri = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", URI);
        String order = keyVaultEnvironmentPostProcessorHelper.getPropertyValue("", ORDER);

        assertEquals("client1", clientId);
        assertEquals("secret2", clientSecert);
        assertEquals("tenant2", tenantId);
        assertNotEquals("host1", authorityHost);
        assertEquals("cert2", certificatePath);
        assertEquals("uri1", uri);
        assertNull(order);
    }

    @Configuration
    static class OrderedProcessConfig {
        static final int TEST_ORDER = KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER + 1;

        @Bean
        @Primary
        KeyVaultEnvironmentPostProcessor getProcessor() {
            final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
            processor.setOrder(TEST_ORDER);
            return processor;
        }
    }

}

