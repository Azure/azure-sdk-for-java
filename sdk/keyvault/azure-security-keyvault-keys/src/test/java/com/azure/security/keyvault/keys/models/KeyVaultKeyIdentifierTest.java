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
        String sourceId = "https://test-key-vault.vault.azure.net/keys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = new KeyVaultKeyIdentifier(sourceId);

        assertEquals(sourceId, keyVaultKeyIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
        assertNull(keyVaultKeyIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() {
        String sourceId = "https://test-key-vault.vault.azure.net/keys/test-key/version";
        KeyVaultKeyIdentifier keyVaultkeyIdentifier = new KeyVaultKeyIdentifier(sourceId);

        assertEquals(sourceId, keyVaultkeyIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultkeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultkeyIdentifier.getName());
        assertEquals("version", keyVaultkeyIdentifier.getVersion());
    }

    @Test
    void parseForDeletedKey() {
        String sourceId = "https://test-key-vault.vault.azure.net/deletedkeys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = new KeyVaultKeyIdentifier(sourceId);

        assertEquals(sourceId, keyVaultKeyIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
    }

    @Test
    void parseNullIdentifier() {
        assertThrows(NullPointerException.class, () -> new KeyVaultKeyIdentifier(null));
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String sourceId = "https://test-key-vault.vault.azure.net/keys/test-key/version/extra";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultKeyIdentifier(sourceId));
    }
}
