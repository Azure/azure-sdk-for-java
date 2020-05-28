// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.microsoft.azure.utils.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyVaultOperationUnitTest {

    private static final List<String> SECRET_KEYS_CONFIG = Arrays.asList("key1", "key2", "key3");

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String SECRET_KEY_1 = "key1";

    private static final String FAKE_VAULT_URI = "https:fake.vault.com";

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

    public void setupSecretBundle(String id, String value, List<String> secretKeysConfig) {
        //provision for list
        when(keyVaultClient.listPropertiesOfSecrets()).thenReturn(new MockPage(new PagedFlux<>(() -> null), id));
        //provison for get
        final KeyVaultSecret secretBundle = new KeyVaultSecret(id, value);
        when(keyVaultClient.getSecret(anyString())).thenReturn(secretBundle);
        keyVaultOperation = new KeyVaultOperation(keyVaultClient,
            FAKE_VAULT_URI,
            Constants.TOKEN_ACQUIRE_TIMEOUT_SECS,
            secretKeysConfig,
            false);
    }

    @Test
    public void testGet() {
        //test get with no specific secret keys
        setupSecretBundle(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1, null);
        assertThat(keyVaultOperation.get(TEST_PROPERTY_NAME_1)).isEqualToIgnoringCase(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testGetAndMissWhenSecretsProvided() {
        //test get with specific secret key configs
        setupSecretBundle(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1, SECRET_KEYS_CONFIG);
        assertThat(keyVaultOperation.get(TEST_PROPERTY_NAME_1)).isEqualToIgnoringCase(null);
    }

    @Test
    public void testGetAndHitWhenSecretsProvided() {
        setupSecretBundle(SECRET_KEY_1, SECRET_KEY_1, SECRET_KEYS_CONFIG);
        assertThat(keyVaultOperation.get(SECRET_KEY_1)).isEqualToIgnoringCase(SECRET_KEY_1);
    }

    @Test
    public void testList() {
        //test list with no specific secret keys
        setupSecretBundle(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1, null);
        final String[] result = keyVaultOperation.getPropertyNames();
        assertThat(result.length).isEqualTo(1);
        assertThat(result[0]).isEqualToIgnoringCase(TEST_PROPERTY_NAME_1);

        //test list with specific secret key configs
        setupSecretBundle(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1, SECRET_KEYS_CONFIG);
        final String[] specificResult = keyVaultOperation.getPropertyNames();
        assertThat(specificResult.length).isEqualTo(3);
        assertThat(specificResult[0]).isEqualTo(SECRET_KEYS_CONFIG.get(0));
    }

    @Test
    public void setTestSpringRelaxedBindingNames() {
        //test list with no specific secret keys
        setupSecretBundle(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_NAME, null);

        TEST_SPRING_RELAXED_BINDING_NAMES.forEach(
            n -> assertThat(keyVaultOperation.get(n)).isEqualTo(TEST_AZURE_KEYVAULT_NAME)
        );

        //test list with specific secret key configs
        setupSecretBundle(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_NAME, Arrays.asList(TEST_AZURE_KEYVAULT_NAME));
        TEST_SPRING_RELAXED_BINDING_NAMES.forEach(
            n -> assertThat(keyVaultOperation.get(n)).isEqualTo(TEST_AZURE_KEYVAULT_NAME)
        );

        setupSecretBundle(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_NAME, SECRET_KEYS_CONFIG);
        TEST_SPRING_RELAXED_BINDING_NAMES.forEach(
            n -> assertThat(keyVaultOperation.get(n)).isEqualTo(null)
        );
    }

    class MockPage extends PagedIterable<SecretProperties> {
        private String name;

        MockPage(PagedFlux<SecretProperties> pagedFlux, String name) {
            super(pagedFlux);
            this.name = name;
        }

        /**
         * Creates instance given {@link PagedFlux}.
         *
         * @param pagedFlux to use as iterable
         */
        MockPage(PagedFlux<SecretProperties> pagedFlux) {
            super(pagedFlux);
        }

        @Override
        public void forEach(Consumer<? super SecretProperties> action) {
            action.accept(new MockSecretProperties(name));
        }
    }

    class MockSecretProperties extends SecretProperties {
        private String name;

        MockSecretProperties(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
