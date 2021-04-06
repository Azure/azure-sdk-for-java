// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultKeyIdentifierTest {
    @Test
    void parseWithoutVersion() {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = new KeyVaultKeyIdentifier(keyId);

        assertEquals(keyId, keyVaultKeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
        assertNull(keyVaultKeyIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/version";
        KeyVaultKeyIdentifier keyVaultkeyIdentifier = new KeyVaultKeyIdentifier(keyId);

        assertEquals(keyId, keyVaultkeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultkeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultkeyIdentifier.getName());
        assertEquals("version", keyVaultkeyIdentifier.getVersion());
    }

    @Test
    void parseForDeletedKey() {
        String keyId = "https://test-key-vault.vault.azure.net/deletedkeys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = new KeyVaultKeyIdentifier(keyId);

        assertEquals(keyId, keyVaultKeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
    }

    @Test
    void parseInvalidIdentifierForDeletedKey() {
        String keyId = "https://test-key-vault.vault.azure.net/deletedkeys/test-key/version";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultKeyIdentifier(keyId));
    }

    @Test
    void parseNullIdentifier() {
        assertThrows(NullPointerException.class, () -> new KeyVaultKeyIdentifier(null));
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/version/extra";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultKeyIdentifier(keyId));
    }
}
