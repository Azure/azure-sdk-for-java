// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyVaultCertificateIdentifierTest {
    @Test
    void parseWithoutVersion() throws MalformedURLException {
        String certificateId = "https://test-key-vault.vault.azure.net/certificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier =
            KeyVaultCertificateIdentifier.parse(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertNull(keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseWithVersion() throws MalformedURLException {
        String certificateId = "https://test-key-vault.vault.azure.net/certificates/test-certificate/version";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier =
            KeyVaultCertificateIdentifier.parse(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
        assertEquals("version", keyVaultCertificateIdentifier.getVersion());
    }

    @Test
    void parseForDeletedCertificate() throws MalformedURLException {
        String certificateId = "https://test-key-vault.vault.azure.net/deletedcertificates/test-certificate";
        KeyVaultCertificateIdentifier keyVaultCertificateIdentifier = KeyVaultCertificateIdentifier.parse(certificateId);

        assertEquals(certificateId, keyVaultCertificateIdentifier.getCertificateId());
        assertEquals("https://test-key-vault.vault.azure.net", keyVaultCertificateIdentifier.getVaultUrl());
        assertEquals("test-certificate", keyVaultCertificateIdentifier.getName());
    }

    @Test
    void parseInvalidIdentifierForDeletedCertificate() {
        String certificateId = "https://test-key-vault.vault.azure.net/deletedcertificates/test-certificate/version";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultCertificateIdentifier.parse(certificateId));

        assertEquals("certificateId is not a valid Key Vault Certificate identifier", exception.getMessage());
    }

    @Test
    void parseNullIdentifier() {
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultCertificateIdentifier.parse(null));

        assertEquals("certificateId cannot be null", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithWrongCollection() {
        String certificateId = "https://test-key-vault.vault.azure.net/keys/test-certificate";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultCertificateIdentifier.parse(certificateId));

        assertEquals("certificateId is not a valid Key Vault Certificate identifier", exception.getMessage());
    }

    @Test
    void parseInvalidIdentifierWithExtraSegment() {
        String certificateId = "https://test-key-vault.vault.azure.net/keys/test-certificate/version/extra-segment";
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyVaultCertificateIdentifier.parse(certificateId));

        assertEquals("certificateId is not a valid Key Vault Certificate identifier", exception.getMessage());
    }
}
