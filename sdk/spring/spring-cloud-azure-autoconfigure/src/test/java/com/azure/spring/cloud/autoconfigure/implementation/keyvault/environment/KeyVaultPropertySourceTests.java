// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;


import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.KeyVaultSecretClientMockUtils.mockSecretClientGetSecretMethod;
import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.KeyVaultSecretClientMockUtils.mockSecretClientListPropertiesOfSecrets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class KeyVaultPropertySourceTests {

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String TEST_PROPERTY_VALUE_1 = "testPropertyValue1";

    private AutoCloseable closeable;

    @Mock
    KeyVaultOperation keyVaultOperation;

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_0 = "acme.my-project.person.first-name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_1 = "acme.myProject.person.firstName";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_2 = "acme.my_project.person.first_name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_3 = "ACME_MYPROJECT_PERSON_FIRSTNAME";

    private static final String TEST_AZURE_KEYVAULT_NAME = "acme-myproject-person-firstname";
    private static final String TEST_AZURE_KEYVAULT_VALUE = "testValue";

    private static final List<String> TEST_SPRING_RELAXED_BINDING_NAMES = Arrays.asList(
        TEST_SPRING_RELAXED_BINDING_NAME_0,
        TEST_SPRING_RELAXED_BINDING_NAME_1,
        TEST_SPRING_RELAXED_BINDING_NAME_2,
        TEST_SPRING_RELAXED_BINDING_NAME_3
    );

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetPropertyNames() {
        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(TEST_PROPERTY_NAME_1, TEST_PROPERTY_VALUE_1);
        when(keyVaultOperation.listSecrets(null)).thenReturn(List.of(keyVaultSecret));

        KeyVaultPropertySource keyVaultPropertySource = new KeyVaultPropertySource("azure-key-vault-secret-property-source", Duration.ZERO, keyVaultOperation, null, true);
        final String[] result = keyVaultPropertySource.getPropertyNames();

        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testGetProperty() {
        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(TEST_PROPERTY_NAME_1, TEST_PROPERTY_VALUE_1);
        when(keyVaultOperation.listSecrets(null)).thenReturn(List.of(keyVaultSecret));

        KeyVaultPropertySource keyVaultPropertySource = new KeyVaultPropertySource("azure-key-vault-secret-property-source", Duration.ZERO, keyVaultOperation, null, true);
        final String result = keyVaultPropertySource.getProperty(TEST_PROPERTY_NAME_1);

        assertThat(result).isEqualTo(TEST_PROPERTY_VALUE_1);
    }

    @Test
    public void caseSensitive() {
        KeyVaultSecret keyVaultSecret1 = new KeyVaultSecret("KEY1", "value1");
        KeyVaultSecret keyVaultSecret2 = new KeyVaultSecret("Key2", "value2");
        when(keyVaultOperation.listSecrets(null)).thenReturn(List.of(keyVaultSecret1, keyVaultSecret2));

        KeyVaultPropertySource keyVaultPropertySource = new KeyVaultPropertySource("azure-key-vault-secret-property-source", Duration.ZERO, keyVaultOperation, null, true);

        assertEquals("value1", keyVaultPropertySource.getProperty("KEY1"));
        assertEquals(null, keyVaultPropertySource.getProperty("key1"));
        assertEquals("value2", keyVaultPropertySource.getProperty("Key2"));
        assertEquals(null, keyVaultPropertySource.getProperty("KEY2"));
    }

    @Test
    public void setTestSpringRelaxedBindingNames() {
        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_VALUE);
        when(keyVaultOperation.listSecrets(null)).thenReturn(List.of(keyVaultSecret));
        KeyVaultPropertySource kvPropertySource = new KeyVaultPropertySource("KeyVault", Duration.ZERO, keyVaultOperation, null, false);

        TEST_SPRING_RELAXED_BINDING_NAMES
            .forEach(n -> assertThat(kvPropertySource.getProperty(n)).isEqualTo(TEST_AZURE_KEYVAULT_VALUE));
    }

    @Test
    @Timeout(5)
    public void refreshTwoKeyVaultsPropertySources() throws InterruptedException {
        CountDownLatch latchForRefreshing = new CountDownLatch(2);
        new SecretRefreshing(latchForRefreshing, "KeyVault1", "test1",
            "value1", "value1Updated").start();
        new SecretRefreshing(latchForRefreshing, "KeyVault2", "test2",
            "value2", "value2Updated").start();
        latchForRefreshing.await();
    }

    static class SecretRefreshing extends Thread {
        private final CountDownLatch latchForRefreshing;
        private final String propertySourceName;
        private final SecretClient secretClient;
        private final String secretName;
        private final String initialSecretValue;
        private final String updatedSecretValue;
        private static final int REFRESH_IN_SECONDS = 3;

        SecretRefreshing(CountDownLatch latchForRefreshing,
                         String propertySourceName,
                         String secretName,
                         String initialSecretValue,
                         String updatedSecretValue) {
            this.latchForRefreshing = latchForRefreshing;
            this.propertySourceName = propertySourceName;
            this.secretClient = mock(SecretClient.class);
            this.secretName = secretName;
            this.initialSecretValue = initialSecretValue;
            this.updatedSecretValue = updatedSecretValue;
        }

        @Override
        public void run() {
            KeyVaultOperation secretOperation = new KeyVaultOperation(secretClient);

            KeyVaultSecret initialKeyVaultSecret = mockSecretClientGetSecretMethod(secretClient, secretName, initialSecretValue);
            mockSecretClientListPropertiesOfSecrets(secretClient, initialKeyVaultSecret.getProperties());

            KeyVaultPropertySource propertySource = new KeyVaultPropertySource(
                propertySourceName,
                Duration.ofSeconds(REFRESH_IN_SECONDS),
                secretOperation,
                null,
                true);
            assertThat(propertySource.getProperty(this.secretName)).isEqualTo(initialSecretValue);

            mockSecretClientGetSecretMethod(secretClient, secretName, updatedSecretValue);
            try {
                TimeUnit.SECONDS.sleep(REFRESH_IN_SECONDS + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertThat(propertySource.getProperty(this.secretName)).isEqualTo(updatedSecretValue);
            latchForRefreshing.countDown();
        }

    }

}
