// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class KeyVaultOperationUnitTest {

    private static final List<String> SECRET_KEYS_CONFIG = Arrays.asList("key1", "key2", "key3");

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String SECRET_KEY_1 = "key1";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_0 = "acme.my-project.person.first-name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_1 = "acme.myProject.person.firstName";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_2 = "acme.my_project.person.first_name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_3 = "ACME_MYPROJECT_PERSON_FIRSTNAME";

    private static final String TEST_AZURE_KEYVAULT_NAME = "acme-myproject-person-firstname";

    private static final List<String> TEST_SPRING_RELAXED_BINDING_NAMES = Arrays.asList(
        TEST_SPRING_RELAXED_BINDING_NAME_0,
        TEST_SPRING_RELAXED_BINDING_NAME_1,
        TEST_SPRING_RELAXED_BINDING_NAME_2,
        TEST_SPRING_RELAXED_BINDING_NAME_3
    );

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
        keyVaultOperation = new KeyVaultOperation(
            keyVaultClient,
            0,
            secretKeysConfig,
            false);
    }

    @Test
    public void testGetWithNoSpecficSecretKeys() {
        setupSecretBundle(null);

        final LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("testpropertyname1", TEST_PROPERTY_NAME_1);
        keyVaultOperation.setProperties(properties);

        assertThat(keyVaultOperation.getProperty(TEST_PROPERTY_NAME_1)).isEqualToIgnoringCase(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testGetAndMissWhenSecretsProvided() {
        setupSecretBundle(SECRET_KEYS_CONFIG);

        final LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        properties.put("key3", "value3");
        keyVaultOperation.setProperties(properties);

        assertThat(keyVaultOperation.getProperty(TEST_PROPERTY_NAME_1)).isEqualToIgnoringCase(null);
    }

    @Test
    public void testGetAndHitWhenSecretsProvided() {
        when(keyVaultClient.getSecret("key1")).thenReturn(new KeyVaultSecret("key1", "key1"));
        when(keyVaultClient.getSecret("key2")).thenReturn(new KeyVaultSecret("key2", "key2"));
        when(keyVaultClient.getSecret("key3")).thenReturn(new KeyVaultSecret("key3", "key3"));

        setupSecretBundle(SECRET_KEYS_CONFIG);

        assertThat(keyVaultOperation.getProperty(SECRET_KEY_1)).isEqualToIgnoringCase(SECRET_KEY_1);
    }

    @Test
    public void testList() {
        //test list with no specific secret keys
        setupSecretBundle(null);
        final LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1);
        keyVaultOperation.setProperties(properties);
        final String[] result = keyVaultOperation.getPropertyNames();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualToIgnoringCase(TEST_PROPERTY_NAME_1);

        //test list with specific secret key configs
        when(keyVaultClient.getSecret("key1")).thenReturn(new KeyVaultSecret("key1", "key1"));
        when(keyVaultClient.getSecret("key2")).thenReturn(new KeyVaultSecret("key2", "key2"));
        when(keyVaultClient.getSecret("key3")).thenReturn(new KeyVaultSecret("key3", "key3"));
        setupSecretBundle(SECRET_KEYS_CONFIG);
        final String[] specificResult = keyVaultOperation.getPropertyNames();
        assertThat(specificResult.length).isEqualTo(3);
        assertThat(specificResult[0]).isEqualTo(SECRET_KEYS_CONFIG.get(0));
    }

    @Test
    public void setTestSpringRelaxedBindingNames() {
        //test list with no specific secret keys
        setupSecretBundle(null);
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("acme-myproject-person-firstname", TEST_AZURE_KEYVAULT_NAME);
        keyVaultOperation.setProperties(properties);
        TEST_SPRING_RELAXED_BINDING_NAMES
            .forEach(n -> assertThat(keyVaultOperation.getProperty(n)).isEqualTo(TEST_AZURE_KEYVAULT_NAME));

        //test list with specific secret key configs
        setupSecretBundle(Arrays.asList(TEST_AZURE_KEYVAULT_NAME));
        properties = new LinkedHashMap<>();
        properties.put(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_NAME);
        keyVaultOperation.setProperties(properties);
        TEST_SPRING_RELAXED_BINDING_NAMES
            .forEach(n -> assertThat(keyVaultOperation.getProperty(n)).isEqualTo(TEST_AZURE_KEYVAULT_NAME));

        setupSecretBundle(SECRET_KEYS_CONFIG);
        properties = new LinkedHashMap<>();
        properties.put("key1", "key1");
        properties.put("key2", "key2");
        properties.put("key3", "key3");
        keyVaultOperation.setProperties(properties);
        TEST_SPRING_RELAXED_BINDING_NAMES.forEach(n -> assertThat(keyVaultOperation.getProperty(n)).isEqualTo(null));
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
        assertThat(keyVaultOperation.getPropertyNames().length == 1);
        assertThat(keyVaultOperation.getProperty("key1")).isNotNull();
        assertThat(keyVaultOperation.getProperty("key2")).isNull();

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
