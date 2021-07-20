// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultKeyLessRsaSignatureTest {

    KeyVaultKeyLessRsaSignature keyVaultKeyLessRsaSignature;

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    @BeforeEach
    public void before() {
        System.setProperty("azure.keyvault.uri", KeyVaultClientTest.KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeyLessRsaSignature = new KeyVaultKeyLessRsaSignature();
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
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessRsaSignature.engineInitVerify(publicKey));
    }

    @Test
    public void engineInitSignTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessRsaSignature.engineInitSign(privateKey));
    }

    @Test
    public void engineInitSignWithRandomTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessRsaSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessRsaSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeyLessRsaSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() throws SignatureException, InvalidAlgorithmParameterException {
        keyVaultKeyLessRsaSignature = new KeyVaultKeyLessRsaSignature(keyVaultClient);
        keyVaultKeyLessRsaSignature.engineSetParameter(new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20, 1));
        when(keyVaultClient.getSignedWithPrivateKey(ArgumentMatchers.eq("PS256"), anyString(), ArgumentMatchers.eq(null))).thenReturn("fakeValue".getBytes());
        assertArrayEquals("fakeValue".getBytes(), keyVaultKeyLessRsaSignature.engineSign());
    }

}
