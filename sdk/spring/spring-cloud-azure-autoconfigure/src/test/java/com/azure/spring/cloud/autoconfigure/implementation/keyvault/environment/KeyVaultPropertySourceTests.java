// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;


import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.IterableStream;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class KeyVaultPropertySourceTests {

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String TEST_PROPERTY_VALUE_1 = "testPropertyValue1";

    private AutoCloseable closeable;

    @Mock
    KeyVaultOperation keyVaultOperation;

    KeyVaultPropertySource keyVaultPropertySource;


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
        Map<String, String> properties = new HashMap<>();
        properties.put(TEST_PROPERTY_NAME_1, TEST_PROPERTY_VALUE_1);
        when(keyVaultOperation.refreshProperties()).thenReturn(properties);
        keyVaultPropertySource = new KeyVaultPropertySource("azure-key-vault-secret-property-source", Duration.ZERO, keyVaultOperation, true);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetPropertyNames() {
        final String[] result = keyVaultPropertySource.getPropertyNames();

        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualTo(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testGetProperty() {
        final String result = keyVaultPropertySource.getProperty(TEST_PROPERTY_NAME_1);
        assertThat(result).isEqualTo(TEST_PROPERTY_VALUE_1);
    }

    @Test
    public void setTestSpringRelaxedBindingNames() {
        Map<String, String> properties = new HashMap<>();
        properties.put(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_VALUE);
        when(keyVaultOperation.refreshProperties()).thenReturn(properties);
        KeyVaultPropertySource kvPropertySource = new KeyVaultPropertySource("KeyVault", Duration.ZERO, keyVaultOperation, false);
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
        private final SecretClient keyClient;
        private final KeyVaultSecret secret;
        private final String keyUpdatedValue;
        private static final int REFRESH_IN_SECONDS = 3;

        SecretRefreshing(CountDownLatch latchForRefreshing,
                         String propertySourceName,
                         String name,
                         String value,
                         String keyUpdatedValue) {
            this.latchForRefreshing = latchForRefreshing;
            this.propertySourceName = propertySourceName;
            this.keyClient = mock(SecretClient.class);
            this.secret = new KeyVaultSecret(name, value);
            this.secret.getProperties().setEnabled(true);
            this.keyUpdatedValue = keyUpdatedValue;
        }

        @Override
        public void run() {
            KeyVaultOperation secretOperation = getSecretOperation(keyClient, secret);
            KeyVaultPropertySource propertySource = new KeyVaultPropertySource(
                propertySourceName,
                Duration.ofSeconds(REFRESH_IN_SECONDS),
                secretOperation,
                true);
            assertThat(propertySource.getProperty(secret.getName())).isEqualTo(secret.getValue());

            updateSecretValue(secret.getName(), keyUpdatedValue, keyClient);
            try {
                TimeUnit.SECONDS.sleep(REFRESH_IN_SECONDS + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertThat(propertySource.getProperty(secret.getName())).isEqualTo(keyUpdatedValue);
            latchForRefreshing.countDown();
        }

        private KeyVaultOperation getSecretOperation(SecretClient keyClient, KeyVaultSecret secret) {
            List<SecretProperties> properties = Collections.singletonList(secret.getProperties());
            OnePageResponse<SecretProperties> secret1Response = new OnePageResponse<>(properties);
            when(keyClient.getSecret(secret.getName(), null)).thenReturn(secret);
            when(keyClient.listPropertiesOfSecrets())
                .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secret1Response))));
            return new KeyVaultOperation(keyClient, null, false);
        }

        private static void updateSecretValue(String key1, String key1UpdatedValue, SecretClient key1Client) {
            KeyVaultSecret secret1;
            secret1 = new KeyVaultSecret(key1, key1UpdatedValue);
            secret1.getProperties().setEnabled(true);
            when(key1Client.getSecret(key1, null)).thenReturn(secret1);
        }
    }

    static class OnePageResponse<T> implements PagedResponse<T> {

        List<T> properties = null;

        OnePageResponse(List<T> properties) {
            this.properties = properties;
        }

        @Override
        public IterableStream<T> getElements() {
            Flux<T> flux = Flux.fromIterable(properties);
            return new IterableStream<T>(flux);
        }

        @Override
        public String getContinuationToken() {
            return null;
        }

        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public HttpHeaders getHeaders() {
            return null;
        }

        @Override
        public HttpRequest getRequest() {
            return null;
        }

        @Override
        public void close() throws IOException {

        }
    }
}
