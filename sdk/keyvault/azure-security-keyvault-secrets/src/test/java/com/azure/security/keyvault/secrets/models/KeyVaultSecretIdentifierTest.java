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
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new KeyVaultSecretIdentifier(secretId));

        assertEquals("secretId is not a valid Key Vault Secret identifier", exception.getMessage());
    }

    @Test
    void parseNullIdentifier() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new KeyVaultSecretIdentifier(null));

        assertEquals("secretId cannot be null", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithWrongCollection() {
        String secretId = "https://test-key-vault.vault.azure.net/certificates/test-secret";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new KeyVaultSecretIdentifier(secretId));

        assertEquals("secretId is not a valid Key Vault Secret identifier", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String secretId = "https://test-key-vault.vault.azure.net/secrets/test-secret/version/extra";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> new KeyVaultSecretIdentifier(secretId));

        assertEquals("secretId is not a valid Key Vault Secret identifier", exception.getMessage());
    }
}
