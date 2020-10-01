package com.azure.security.keyvault.keys.models;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

class KeyVaultKeyIdentifierTest {
    @Test
    void parse_withoutVersion() throws MalformedURLException {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key";
        KeyVaultKeyIdentifier keyVaultKeyIdentifier = KeyVaultKeyIdentifier.parse(keyId);

        assertEquals(keyId, keyVaultKeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultKeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultKeyIdentifier.getName());
        assertNull(keyVaultKeyIdentifier.getVersion());
    }

    @Test
    void parse_withVersion() throws MalformedURLException {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/test-version";
        KeyVaultKeyIdentifier keyVaultkeyIdentifier = KeyVaultKeyIdentifier.parse(keyId);

        assertEquals(keyId, keyVaultkeyIdentifier.getKeyId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultkeyIdentifier.getVaultUrl());
        assertEquals("test-key", keyVaultkeyIdentifier.getName());
        assertEquals("test-version", keyVaultkeyIdentifier.getVersion());
    }

    @Test
    void parse_invalidIdentifier_thatIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(null));

        assertEquals("keyId cannot be null", exception.getMessage());
    }

    @Test
    void parse_invalidIdentifier_withWrongCollection() {
        String keyId = "https://test-key-vault.vault.azure.net/secrets/test-key";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(keyId));

        assertEquals("keyId is not a valid Key Vault Key identifier", exception.getMessage());
    }

    @Test
    void parse_invalidIdentifier_withExtraSegment() {
        String keyId = "https://test-key-vault.vault.azure.net/keys/test-key/test-key/extra";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> KeyVaultKeyIdentifier.parse(keyId));

        assertEquals("keyId is not a valid Key Vault Key identifier", exception.getMessage());
    }
}
