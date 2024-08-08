// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.KeyVaultEncode;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.KeyVaultPrivateKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyVaultKeylessEcSignatureTest {

    KeyVaultKeylessEcSignature keyVaultKeylessEcSignature;

    private final byte[] signedWithES256 = "fake256Value".getBytes();
    private final byte[] signedWithES384 = "fake384Value".getBytes();

    static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    @BeforeEach
    public void before() {
        System.setProperty("azure.keyvault.uri", KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha256Signature();
    }

    private final PublicKey publicKey = new PublicKey() {
        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    };

    private final PrivateKey privateKey = new PrivateKey() {
        @Override
        public String getAlgorithm() {
            return null;
        }

        @Override
        public String getFormat() {
            return null;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    };

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
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessEcSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() {
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null) {
            @Override
            public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
                if ("ES256".equals(digestName) && keyId == null) {
                    return signedWithES256;
                } else if ("ES384".equals(digestName) && keyId == null) {
                    return signedWithES384;
                }

                return null;
            }
        };

        KeyVaultPrivateKey keyVaultPrivateKey = new KeyVaultPrivateKey(null, null, keyVaultClient);

        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha256Signature();
        keyVaultKeylessEcSignature.engineInitSign(keyVaultPrivateKey, null);
        Assertions.assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES256), keyVaultKeylessEcSignature.engineSign());

        keyVaultKeylessEcSignature = new KeyVaultKeylessEcSha384Signature();
        keyVaultKeylessEcSignature.engineInitSign(keyVaultPrivateKey, null);
        assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES384), keyVaultKeylessEcSignature.engineSign());
    }

}
