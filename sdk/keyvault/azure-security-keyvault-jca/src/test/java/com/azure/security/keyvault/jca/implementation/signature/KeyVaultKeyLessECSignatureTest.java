// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.KeyVaultEncode;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultKeyLessECSignatureTest {

    KeyVaultKeyLessECSignature keyVaultKeyLessECSignature;

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private final byte[] signedWithES256 = "fake256Value".getBytes();
    private final byte[] signedWithES384 = "fake384Value".getBytes();

    static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    @BeforeEach
    public void before() {
        System.setProperty("azure.keyvault.uri", KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeyLessECSignature = new KeyVaultKeyLessEcSha256Signature();
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
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessECSignature.engineInitVerify(publicKey));
    }

    @Test
    public void engineInitSignTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessECSignature.engineInitSign(privateKey));
    }

    @Test
    public void engineInitSignWithRandomTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessECSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessECSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessECSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() {
        keyVaultKeyLessECSignature = new KeyVaultKeyLessEcSha256Signature();
        when(keyVaultClient.getSignedWithPrivateKey(ArgumentMatchers.eq("ES256"), anyString(), ArgumentMatchers.eq(null))).thenReturn(signedWithES256);
        keyVaultKeyLessECSignature.setKeyVaultClient(keyVaultClient);
        Assertions.assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES256), keyVaultKeyLessECSignature.engineSign());

        keyVaultKeyLessECSignature = new KeyVaultKeyLessEcSha384Signature();
        keyVaultKeyLessECSignature.setKeyVaultClient(keyVaultClient);
        when(keyVaultClient.getSignedWithPrivateKey(ArgumentMatchers.eq("ES384"), anyString(), ArgumentMatchers.eq(null))).thenReturn(signedWithES384);
        assertArrayEquals(KeyVaultEncode.encodeByte(signedWithES384), keyVaultKeyLessECSignature.engineSign());
    }

}
