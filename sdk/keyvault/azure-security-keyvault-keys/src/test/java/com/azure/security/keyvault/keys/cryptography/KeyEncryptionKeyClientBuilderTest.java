// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.keys.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyEncryptionKeyClientBuilderTest {
    private static final String KEY_ID =
        "https://azure-kv-tests2.vault.azure.net/secrets/secretkey83767501/4d85413d34944863bc9747af78f8f446";

    @Test
    public void buildKeyEncryptionKey() {
        KeyEncryptionKey keyEncryptionKey = new KeyEncryptionKeyClientBuilder()
            .credential(new TestUtils.TestCredential())
            .buildKeyEncryptionKey(KEY_ID);

        assertNotNull(keyEncryptionKey);
        assertEquals(KeyEncryptionKeyClient.class.getSimpleName(), keyEncryptionKey.getClass().getSimpleName());
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new KeyEncryptionKeyClientBuilder()
            .credential(new TestUtils.TestCredential())
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildKeyEncryptionKey(KEY_ID));
    }
}
