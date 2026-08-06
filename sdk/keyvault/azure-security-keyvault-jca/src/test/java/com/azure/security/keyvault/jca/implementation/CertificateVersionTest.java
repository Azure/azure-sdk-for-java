// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CertificateVersionTest {

    @Test
    public void testCertificateVersionProperties() {
        CertificateVersion certificateVersion
            = new CertificateVersion("myalias", "certificate-data", "https://example.vault.azure.net/keys/myalias/v1",
                "https://example.vault.azure.net/secrets/myalias/v1", true, "RSA");

        Assertions.assertEquals("myalias", certificateVersion.getAlias());
        Assertions.assertEquals("certificate-data", certificateVersion.getCertificateData());
        Assertions.assertEquals("https://example.vault.azure.net/keys/myalias/v1", certificateVersion.getKeyId());
        Assertions.assertEquals("https://example.vault.azure.net/secrets/myalias/v1", certificateVersion.getSecretId());
        Assertions.assertTrue(certificateVersion.isExportable());
        Assertions.assertEquals("RSA", certificateVersion.getKeyType());
    }
}
