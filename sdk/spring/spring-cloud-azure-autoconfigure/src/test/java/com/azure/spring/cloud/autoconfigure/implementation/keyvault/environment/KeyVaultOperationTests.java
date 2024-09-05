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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultOperationTests {

    private static final List<String> SECRET_KEYS_CONFIG = Arrays.asList("key1", "key2", "key3");

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String SECRET_KEY_1 = "key1";

    private static final String SECRET_VALUE_1 = "value1";

    @Mock
    private SecretClient keyVaultClient;

    private KeyVaultOperation keyVaultOperation;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    public void setupSecretBundle(List<String> secretKeysConfig) {
        keyVaultOperation = new KeyVaultOperation(keyVaultClient, secretKeysConfig, false);
    }

    @Test
    public void caseSensitive() {
        String key1 = "KEY1";
        String value1 = "value1";
        String key2 = "Key2";
        String value2 = "value2";
        KeyVaultSecret key1Secret = new KeyVaultSecret(key1, value1);
        key1Secret.getProperties().setEnabled(true);
        KeyVaultSecret key2Secret = new KeyVaultSecret(key2, value2);
        key2Secret.getProperties().setEnabled(true);

        List<SecretProperties> properties = Arrays.asList(key1Secret.getProperties(), key2Secret.getProperties());
        OnePageResponse<SecretProperties> secretResponse = new OnePageResponse<>(properties);
        when(keyVaultClient.getSecret(key1, null)).thenReturn(key1Secret);
        when(keyVaultClient.getSecret(key2, null)).thenReturn(key2Secret);
        when(keyVaultClient.listPropertiesOfSecrets())
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secretResponse))));
        KeyVaultOperation operation = new KeyVaultOperation(keyVaultClient, null, true);
        Map<String, String> refreshedProperties = operation.refreshProperties();
        assertEquals(value1, refreshedProperties.get(key1));
        assertEquals(value2, refreshedProperties.get(key2));
    }

    @Test
    public void testGetWithNoSpecificSecretKeys() {
        KeyVaultSecret key1Secret = new KeyVaultSecret(SECRET_KEY_1, SECRET_VALUE_1);
        key1Secret.getProperties().setEnabled(true);
        List<SecretProperties> properties = Arrays.asList(key1Secret.getProperties());
        OnePageResponse<SecretProperties> secretResponse = new OnePageResponse<>(properties);
        when(keyVaultClient.getSecret(key1Secret.getName(), null)).thenReturn(key1Secret);
        when(keyVaultClient.listPropertiesOfSecrets())
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secretResponse))));
        setupSecretBundle(null);
        Map<String, String> refreshedProperties = keyVaultOperation.refreshProperties();
        assertThat(refreshedProperties.get(SECRET_KEY_1)).isEqualToIgnoringCase(SECRET_VALUE_1);
    }

    @Test
    public void testGetAndMissWhenSecretsProvided() {
        KeyVaultSecret key1Secret = new KeyVaultSecret(SECRET_KEY_1, SECRET_VALUE_1);
        KeyVaultSecret key2Secret = new KeyVaultSecret("key2", "value2");
        KeyVaultSecret key3Secret = new KeyVaultSecret("key3", "value3");
        when(keyVaultClient.getSecret(key1Secret.getName())).thenReturn(key1Secret);
        when(keyVaultClient.getSecret(key2Secret.getName())).thenReturn(key2Secret);
        when(keyVaultClient.getSecret(key3Secret.getName())).thenReturn(key3Secret);
        setupSecretBundle(SECRET_KEYS_CONFIG);
        Map<String, String> refreshedProperties = keyVaultOperation.refreshProperties();
        assertThat(refreshedProperties.get(TEST_PROPERTY_NAME_1)).isEqualToIgnoringCase(null);
    }

    @Test
    public void testGetAndHitWhenSecretsProvided() {
        KeyVaultSecret key1Secret = new KeyVaultSecret(SECRET_KEY_1, SECRET_VALUE_1);
        KeyVaultSecret key2Secret = new KeyVaultSecret("key2", "value2");
        KeyVaultSecret key3Secret = new KeyVaultSecret("key3", "value3");
        when(keyVaultClient.getSecret(key1Secret.getName())).thenReturn(key1Secret);
        when(keyVaultClient.getSecret(key2Secret.getName())).thenReturn(key2Secret);
        when(keyVaultClient.getSecret(key3Secret.getName())).thenReturn(key3Secret);
        setupSecretBundle(SECRET_KEYS_CONFIG);
        Map<String, String> refreshedProperties = keyVaultOperation.refreshProperties();
        assertThat(refreshedProperties.get(SECRET_KEY_1)).isEqualToIgnoringCase(SECRET_VALUE_1);
    }

    @Test
    public void testList() {
        //test list with no specific secret keys
        setupSecretBundle(null);
        KeyVaultSecret test1Secret = new KeyVaultSecret(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1);
        test1Secret.getProperties().setEnabled(true);
        List<SecretProperties> properties = Collections.singletonList(test1Secret.getProperties());
        OnePageResponse<SecretProperties> secretResponse = new OnePageResponse<>(properties);
        when(keyVaultClient.getSecret(test1Secret.getName(), null)).thenReturn(test1Secret);
        when(keyVaultClient.listPropertiesOfSecrets())
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secretResponse))));
        Map<String, String> refreshedProperties = keyVaultOperation.refreshProperties();
        assertThat(refreshedProperties.size()).isEqualTo(1);
        String propertyKey = refreshedProperties.keySet().iterator().next();
        assertTrue(propertyKey.equalsIgnoreCase(TEST_PROPERTY_NAME_1));

        //test list with specific secret key configs
        KeyVaultSecret key1Secret = new KeyVaultSecret(SECRET_KEY_1, SECRET_VALUE_1);
        KeyVaultSecret key2Secret = new KeyVaultSecret("key2", "value2");
        KeyVaultSecret key3Secret = new KeyVaultSecret("key3", "value3");
        when(keyVaultClient.getSecret(key1Secret.getName())).thenReturn(key1Secret);
        when(keyVaultClient.getSecret(key2Secret.getName())).thenReturn(key2Secret);
        when(keyVaultClient.getSecret(key3Secret.getName())).thenReturn(key3Secret);
        setupSecretBundle(SECRET_KEYS_CONFIG);
        refreshedProperties = keyVaultOperation.refreshProperties();
        assertThat(refreshedProperties.size()).isEqualTo(3);
        assertTrue(refreshedProperties.containsKey(SECRET_KEYS_CONFIG.get(0)));
    }

    @Test
    public void getSecretsWithoutDisabled() {
        KeyVaultSecret enableSecret = new KeyVaultSecret("key1", "value1");
        enableSecret.getProperties().setEnabled(true);

        KeyVaultSecret disableSecret = new KeyVaultSecret("key2", "value2");
        disableSecret.getProperties().setEnabled(false);

        List<SecretProperties> properties = Arrays.asList(enableSecret.getProperties(), disableSecret.getProperties());
        OnePageResponse<SecretProperties> secretResponse = new OnePageResponse<>(properties);
        when(keyVaultClient.getSecret("key1", null)).thenReturn(enableSecret);
        when(keyVaultClient.listPropertiesOfSecrets())
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secretResponse))));
        setupSecretBundle(null);
        Map<String, String> refreshedProperties = keyVaultOperation.refreshProperties();
        assertEquals(1, refreshedProperties.size());
        assertThat(refreshedProperties.get("key1")).isNotNull();
        assertThat(refreshedProperties.get("key2")).isNull();
    }

    @Test
    @Timeout(5)
    public void refreshTwoKeyVaultsSecrets() throws InterruptedException {
        CountDownLatch latchForRefreshing = new CountDownLatch(2);
        new SecretRefreshing(latchForRefreshing, "test1",
            "value1", "value1Updated").start();
        new SecretRefreshing(latchForRefreshing, "test2",
            "value2", "value2Updated").start();
        latchForRefreshing.await();
    }

    static class SecretRefreshing extends Thread {
        private final CountDownLatch latchForRefreshing;
        private final SecretClient keyClient;
        private final KeyVaultSecret secret;
        private final String keyUpdatedValue;
        private static final int REFRESH_IN_SECONDS = 3;

        SecretRefreshing(CountDownLatch latchForRefreshing,
                                String name,
                                String value,
                                String keyUpdatedValue) {
            this.latchForRefreshing = latchForRefreshing;
            this.keyClient = mock(SecretClient.class);
            this.secret = new KeyVaultSecret(name, value);
            this.secret.getProperties().setEnabled(true);
            this.keyUpdatedValue = keyUpdatedValue;
        }

        @Override
        public void run() {
            KeyVaultOperation secretOperation = getSecretOperation(keyClient, secret);
            Map<String, String> properties = secretOperation.refreshProperties();
            assertThat(properties.get(secret.getName())).isEqualTo(secret.getValue());

            updateSecretValue(secret.getName(), keyUpdatedValue, keyClient);
            try {
                TimeUnit.SECONDS.sleep(REFRESH_IN_SECONDS + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            properties = secretOperation.refreshProperties();
            assertThat(properties.get(secret.getName())).isEqualTo(keyUpdatedValue);
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

        private void updateSecretValue(String key1, String key1UpdatedValue, SecretClient key1Client) {
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
