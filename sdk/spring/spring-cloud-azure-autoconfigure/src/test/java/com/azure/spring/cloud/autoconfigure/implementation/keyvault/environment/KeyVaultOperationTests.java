// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.KeyVaultSecretClientMockUtils.mockSecretClientGetSecretMethod;
import static com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.KeyVaultSecretClientMockUtils.mockSecretClientListPropertiesOfSecrets;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyVaultOperationTests {

    private static final List<String> SECRET_KEYS_CONFIG = Arrays.asList("key1", "key2", "key3");

    private static final String TEST_PROPERTY_NAME_1 = "testPropertyName1";

    private static final String SECRET_KEY_1 = "key1";

    private static final String SECRET_VALUE_1 = "value1";

    @Mock
    private SecretClient secretClient;

    private KeyVaultOperation keyVaultOperation;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        keyVaultOperation = new KeyVaultOperation(secretClient);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void caseSensitive() {
        String key1 = "KEY1";
        String value1 = "value1";
        String key2 = "Key2";
        String value2 = "value2";
        KeyVaultSecret keyVaultSecret1 = mockSecretClientGetSecretMethod(secretClient, key1, value1);
        KeyVaultSecret keyVaultSecret2 = mockSecretClientGetSecretMethod(secretClient, key2, value2);
        mockSecretClientListPropertiesOfSecrets(secretClient, keyVaultSecret1.getProperties(), keyVaultSecret2.getProperties());

        List<KeyVaultSecret> keyVaultSecrets = keyVaultOperation.listSecrets(null);

        assertEquals(value1, keyVaultSecrets.get(0).getValue());
        assertEquals(key1, keyVaultSecrets.get(0).getName());
        assertEquals(value2, keyVaultSecrets.get(1).getValue());
        assertEquals(key2, keyVaultSecrets.get(1).getName());
    }

    @Test
    public void testGetWithNoSpecificSecretKeys() {
        KeyVaultSecret keyVaultSecret = mockSecretClientGetSecretMethod(secretClient, SECRET_KEY_1, SECRET_VALUE_1);
        mockSecretClientListPropertiesOfSecrets(secretClient, keyVaultSecret.getProperties());

        List<KeyVaultSecret> keyVaultSecrets = keyVaultOperation.listSecrets(null);

        assertEquals(1, keyVaultSecrets.size());
        assertEquals(SECRET_KEY_1, keyVaultSecrets.get(0).getName());
        assertEquals(SECRET_VALUE_1, keyVaultSecrets.get(0).getValue());
    }

    @Test
    public void testGetAndMissWhenSecretsProvided() {
        mockSecretClientGetSecretMethod(secretClient, "key1", "value1");
        mockSecretClientGetSecretMethod(secretClient, "key2", "value2");
        mockSecretClientGetSecretMethod(secretClient, "key3", "value3");

        List<KeyVaultSecret> keyVaultSecrets = keyVaultOperation.listSecrets(List.of("key1", "key2", "key3"));

        assertEquals(3, keyVaultSecrets.size());
        assertEquals("key1", keyVaultSecrets.get(0).getName());
        assertEquals("key2", keyVaultSecrets.get(1).getName());
        assertEquals("key3", keyVaultSecrets.get(2).getName());
    }

    @Test
    public void testGetAndHitWhenSecretsProvided() {
        mockSecretClientGetSecretMethod(secretClient, SECRET_KEY_1, SECRET_VALUE_1);
        mockSecretClientGetSecretMethod(secretClient, "key2", "value2");
        mockSecretClientGetSecretMethod(secretClient, "key3", "value3");

        List<KeyVaultSecret> keyVaultSecrets = keyVaultOperation.listSecrets(SECRET_KEYS_CONFIG);

        assertEquals(3, keyVaultSecrets.size());
        assertEquals(SECRET_KEY_1, keyVaultSecrets.get(0).getName());
        assertEquals(SECRET_VALUE_1, keyVaultSecrets.get(0).getValue());
    }

    @Test
    public void getSecretsWithoutDisabled() {
        KeyVaultSecret enabledSecret = mockSecretClientGetSecretMethod(secretClient, "key1", "value1");
        KeyVaultSecret disabledSecret = mockSecretClientGetSecretMethod(secretClient, "key2", "value2", false);
        mockSecretClientListPropertiesOfSecrets(secretClient, enabledSecret.getProperties(), disabledSecret.getProperties());

        List<KeyVaultSecret> keyVaultSecrets = keyVaultOperation.listSecrets(null);

        assertEquals(1, keyVaultSecrets.size());
        assertEquals("key1", keyVaultSecrets.get(0).getName());
        assertEquals("value1", keyVaultSecrets.get(0).getValue());
    }

}
