// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultSecretIdentifierTest {
    @Test
    void parseWithoutVersion() throws MalformedURLException {
        String secretId = "https://test-secret-vault.vault.azure.net/secrets/test-secret";
        KeyVaultSecretIdentifier keyVaultSecretIdentifier = KeyVaultSecretIdentifier.parse(secretId);

        assertEquals(secretId, keyVaultSecretIdentifier.getSecretId());
        assertEquals("https://test-secret-vault.vault.azure.net", keyVaultSecretIdentifier.getVaultUrl());
        assertEquals("test-secret", keyVaultSecretIdentifier.getName());
        assertNull(keyVaultSecretIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() throws MalformedURLException {
        String secretId = "https://test-secret-vault.vault.azure.net/secrets/test-secret/test-version";
        KeyVaultSecretIdentifier keyVaultSecretIdentifier = KeyVaultSecretIdentifier.parse(secretId);

        assertEquals(secretId, keyVaultSecretIdentifier.getSecretId());
        assertEquals("https://test-secret-vault.vault.azure.net", keyVaultSecretIdentifier.getVaultUrl());
        assertEquals("test-secret", keyVaultSecretIdentifier.getName());
        assertEquals("test-version", keyVaultSecretIdentifier.getVersion());
    }

    @Test
    void parseNullIdentifier() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultSecretIdentifier.parse(null));

        assertEquals("secretId cannot be null", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithWrongCollection() {
        String secretId = "https://test-secret-vault.vault.azure.net/certificates/test-secret";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultSecretIdentifier.parse(secretId));

        assertEquals("secretId is not a valid Key Vault Secret identifier", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String secretId = "https://test-secret-vault.vault.azure.net/secrets/test-secret/test-secret/extra";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultSecretIdentifier.parse(secretId));

        assertEquals("secretId is not a valid Key Vault Secret identifier", exception.getMessage());
    }
}
