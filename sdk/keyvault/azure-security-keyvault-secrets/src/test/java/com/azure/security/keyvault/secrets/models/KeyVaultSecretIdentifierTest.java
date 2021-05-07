// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultSecretIdentifierTest {
    @Test
    void parseWithoutVersion() {
        String secretId = "https://test-key-vault.vault.azure.net/secrets/test-secret";
        KeyVaultSecretIdentifier keyVaultSecretIdentifier = new KeyVaultSecretIdentifier(secretId);

        assertEquals(secretId, keyVaultSecretIdentifier.getSecretId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultSecretIdentifier.getVaultUrl());
        assertEquals("test-secret", keyVaultSecretIdentifier.getName());
        assertNull(keyVaultSecretIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() {
        String secretId = "https://test-key-vault.vault.azure.net/secrets/test-secret/version";
        KeyVaultSecretIdentifier keyVaultSecretIdentifier = new KeyVaultSecretIdentifier(secretId);

        assertEquals(secretId, keyVaultSecretIdentifier.getSecretId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultSecretIdentifier.getVaultUrl());
        assertEquals("test-secret", keyVaultSecretIdentifier.getName());
        assertEquals("version", keyVaultSecretIdentifier.getVersion());
    }

    @Test
    void parseForDeletedSecret() {
        String secretId = "https://test-key-vault.vault.azure.net/deletedsecrets/test-secret";
        KeyVaultSecretIdentifier keyVaultSecretIdentifier = new KeyVaultSecretIdentifier(secretId);

        assertEquals(secretId, keyVaultSecretIdentifier.getSecretId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultSecretIdentifier.getVaultUrl());
        assertEquals("test-secret", keyVaultSecretIdentifier.getName());
    }

    @Test
    void parseInvalidIdentifierForDeletedSecret() {
        String secretId = "https://test-key-vault.vault.azure.net/deletedsecrets/test-secret/version";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultSecretIdentifier(secretId));
    }

    @Test
    void parseNullIdentifier() {
        assertThrows(NullPointerException.class, () -> new KeyVaultSecretIdentifier(null));
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String secretId = "https://test-key-vault.vault.azure.net/secrets/test-secret/version/extra";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultSecretIdentifier(secretId));
    }
}
