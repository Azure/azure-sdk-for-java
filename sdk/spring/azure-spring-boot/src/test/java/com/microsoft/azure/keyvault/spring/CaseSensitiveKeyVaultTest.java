// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import static com.microsoft.azure.utils.Constants.TOKEN_ACQUIRE_TIMEOUT_SECS;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class CaseSensitiveKeyVaultTest {
    @Mock
    private SecretClient keyVaultClient;

    @Test
    public void testGet() {
        final List<String> keys = Arrays.asList("key1", "Key2");

        final KeyVaultOperation keyVaultOperation = new KeyVaultOperation(
            keyVaultClient,
            "https:fake.vault.com",
            TOKEN_ACQUIRE_TIMEOUT_SECS,
            keys,
            true);

        final KeyVaultSecret key1 = new KeyVaultSecret("key1", "value1");
        when(keyVaultClient.getSecret("key1")).thenReturn(key1);
        final KeyVaultSecret key2 = new KeyVaultSecret("Key2", "Value2");
        when(keyVaultClient.getSecret("Key2")).thenReturn(key2);

        assertEquals("value1", keyVaultOperation.get("key1"));
        assertEquals("Value2", keyVaultOperation.get("Key2"));
    }
}
