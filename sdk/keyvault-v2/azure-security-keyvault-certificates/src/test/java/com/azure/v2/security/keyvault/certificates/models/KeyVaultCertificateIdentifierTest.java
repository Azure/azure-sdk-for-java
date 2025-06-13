// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultCertificateIdentifierTest {
    @Test
    void parseWithoutVersion() {
        String sourceId = "https://test-key-vault.vault.azure.net/certificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(sourceId);

        assertEquals(sourceId, keyVaultCertificateIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertNull(keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() {
        String sourceId = "https://test-key-vault.vault.azure.net/certificates/test-certificate/version";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(sourceId);

        assertEquals(sourceId, keyVaultCertificateIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertEquals("version", keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseForDeletedCertificate() {
        String sourceId = "https://test-key-vault.vault.azure.net/deletedcertificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(sourceId);

        assertEquals(sourceId, keyVaultCertificateIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
    }

    @Test
    void parseNullIdentifier() {
        assertThrows(NullPointerException.class, () -> new KeyVaultCertificateIdentifier(null));
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String sourceId = "https://test-key-vault.vault.azure.net/certificates/test-certificate/version/extra-segment";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(sourceId));
    }

    @Test
    void parseInvalidIdentifierWithWrongCollection() {
        String sourceId = "https://test-key-vault.vault.azure.net/keys/test-key";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(sourceId));
    }

    @Test
    void parseEmptyIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(""));
    }

    @Test
    void parseInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier("not-a-url"));
    }

    @Test
    void parseMalformedUrl() {
        String sourceId = "https://test-key-vault.vault.azure.net/";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(sourceId));
    }

    @Test
    void parseUrlWithQueryParameters() {
        String sourceId = "https://test-key-vault.vault.azure.net/certificates/test-certificate?api-version=7.3";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(sourceId);

        assertEquals(sourceId, keyVaultCertificateIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertNull(keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseUrlWithFragment() {
        String sourceId = "https://test-key-vault.vault.azure.net/certificates/test-certificate#fragment";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(sourceId);

        assertEquals(sourceId, keyVaultCertificateIdentifier.getSourceId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertNull(keyVaultCertificateIdentifier.getVersion());
    }
}
