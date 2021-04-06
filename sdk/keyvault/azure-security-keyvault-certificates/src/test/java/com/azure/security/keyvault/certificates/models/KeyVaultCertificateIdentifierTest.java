// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultCertificateIdentifierTest {
    @Test
    void parseWithoutVersion() {
        String certificateId = "https://test-key-vault.vault.azure.net/certificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier =
            new KeyVaultCertificateIdentifier(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertNull(keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() {
        String certificateId = "https://test-key-vault.vault.azure.net/certificates/test-certificate/version";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier =
            new KeyVaultCertificateIdentifier(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertEquals("version", keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseForDeletedCertificate() {
        String certificateId = "https://test-key-vault.vault.azure.net/deletedcertificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = new KeyVaultCertificateIdentifier(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
    }

    @Test
    void parseInvalidIdentifierForDeletedCertificate() {
        String certificateId = "https://test-key-vault.vault.azure.net/deletedcertificates/test-certificate/version";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(certificateId));
    }

    @Test
    void parseNullIdentifier() {
        assertThrows(NullPointerException.class, () -> new KeyVaultCertificateIdentifier(null));
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String certificateId = "https://test-key-vault.vault.azure.net/keys/test-certificate/version/extra-segment";
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateIdentifier(certificateId));
    }
}
