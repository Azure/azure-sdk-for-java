// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.KeyVaultEncode;
import com.azure.security.keyvault.jca.KeyVaultJcaPropertyNames;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.KeyVaultPrivateKey;
import com.azure.security.keyvault.jca.implementation.MockKeyVaultClient;
import com.azure.security.keyvault.jca.implementation.mocking.MockPrivateKey;
import com.azure.security.keyvault.jca.implementation.mocking.MockPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultKeylessEcSignatureTest {

    KeyVaultKeylessEcSignature keyVaultKeylessEcSignature;

    private KeyVaultClient keyVaultClient;

    private final byte[] signedWithES256 = "fake256Value".getBytes();
    private final byte[] signedWithES384 = "fake384Value".getBytes();
    private final PublicKey publicKey = new MockPublicKey();
    private final PrivateKey privateKey = new MockPrivateKey();

    static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    private String previousKeyVaultUri;

    @BeforeEach
    public void before() {
        previousKeyVaultUri = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha256Signature();
    }

    @AfterEach
    public void after() {
        restoreKeyVaultUri(previousKeyVaultUri);
    }

    private static void restoreKeyVaultUri(String value) {
        if (value == null) {
            System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        } else {
            System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, value);
        }
    }

    @Test
    public void engineInitVerifyTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineInitVerify(publicKey));
    }

    @Test
    public void engineInitSignTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineInitSign(privateKey));
    }

    @Test
    public void engineInitSignWithRandomTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessEcSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessEcSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() {
        keyVaultClient = new MockKeyVaultClient() {
            @Override
            public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
                return "ES256".equals(digestName) ? signedWithES256 : null;
            }
        };
        KeyVaultPrivateKey keyVaultPrivateKey = new KeyVaultPrivateKey("algorithm", "kid") {
            @Override
            public KeyVaultClient getKeyVaultClient() {
                return keyVaultClient;
            }
        };
        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha256Signature();
        keyVaultKeylessEcSignature.engineInitSign(keyVaultPrivateKey, null);
        Assertions.assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES256),
            keyVaultKeylessEcSignature.engineSign());

        keyVaultClient = new MockKeyVaultClient() {
            @Override
            public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
                return "ES384".equals(digestName) ? signedWithES384 : null;
            }
        };
        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha384Signature();
        keyVaultKeylessEcSignature.engineInitSign(keyVaultPrivateKey, null);
        assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES384), keyVaultKeylessEcSignature.engineSign());
    }

}
