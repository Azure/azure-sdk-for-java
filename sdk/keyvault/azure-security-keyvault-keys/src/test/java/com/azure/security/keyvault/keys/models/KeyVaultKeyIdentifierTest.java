// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultKeyIdentifierTest {
    @Test
    void parseWithoutVersion() throws MalformedURLException {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = KeyVaultKeyIdentifier.parse(keyId);

        assertEquals(keyId, keyVaultKeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
        assertNull(keyVaultKeyIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() throws MalformedURLException {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/version";
        KeyVaultKeyIdentifier keyVaultkeyIdentifier = KeyVaultKeyIdentifier.parse(keyId);

        assertEquals(keyId, keyVaultkeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultkeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultkeyIdentifier.getName());
        assertEquals("version", keyVaultkeyIdentifier.getVersion());
    }

    @Test
    void parseForDeletedKey() throws MalformedURLException {
        String keyId = "https://test-key-vault.vault.azure.net/deletedkeys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = KeyVaultKeyIdentifier.parse(keyId);

        assertEquals(keyId, keyVaultKeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
    }

    @Test
    void parseInvalidIdentifierForDeletedKey() {
        String keyId = "https://test-key-vault.vault.azure.net/deletedkeys/test-key/version";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(keyId));

        assertEquals("keyId is not a valid Key Vault Key identifier", exception.getMessage());
    }

    @Test
    void parseNullIdentifier() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(null));

        assertEquals("keyId cannot be null", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithWrongCollection() {
        String keyId = "https://test-key-vault.vault.azure.net/secrets/test-key";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(keyId));

        assertEquals("keyId is not a valid Key Vault Key identifier", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/version/extra";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(keyId));

        assertEquals("keyId is not a valid Key Vault Key identifier", exception.getMessage());
    }
}
