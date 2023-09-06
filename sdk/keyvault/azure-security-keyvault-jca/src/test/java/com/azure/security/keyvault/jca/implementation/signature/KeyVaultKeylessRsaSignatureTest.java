// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.implementation.KeyVaultPrivateKey;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultKeylessRsaSignatureTest {

    KeyVaultKeylessRsaSignature keyVaultKeylessRsaSignature;

    static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private final KeyVaultPrivateKey keyVaultPrivateKey = mock(KeyVaultPrivateKey.class);

    @BeforeEach
    public void before() {
        System.setProperty("azure.keyvault.uri", KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeylessRsaSignature = new KeyVaultKeylessRsaSignature();
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
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSignature.engineInitVerify(publicKey));
    }

    @Test
    public void engineInitSignTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSignature.engineInitSign(privateKey));
    }

    @Test
    public void engineInitSignWithRandomTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() throws InvalidAlgorithmParameterException {
        keyVaultKeylessRsaSignature = new KeyVaultKeylessRsaSignature();
        when(keyVaultPrivateKey.getKeyVaultClient()).thenReturn(keyVaultClient);
        keyVaultKeylessRsaSignature.engineInitSign(keyVaultPrivateKey, null);
        keyVaultKeylessRsaSignature.engineSetParameter(new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20, 1));
        when(keyVaultClient.getSignedWithPrivateKey(ArgumentMatchers.eq("PS256"), anyString(), ArgumentMatchers.eq(null))).thenReturn("fakeValue".getBytes());
        assertArrayEquals("fakeValue".getBytes(), keyVaultKeylessRsaSignature.engineSign());
    }

    @Test
    public void engineSetParameterWithNullParameterTest() {
        keyVaultKeylessRsaSignature = new KeyVaultKeylessRsaSignature();
        assertThrows(InvalidAlgorithmParameterException.class, () -> keyVaultKeylessRsaSignature.engineSetParameter(null));
    }

    @Test
    public void engineSetParameterWithNotPSSParameterSpecTest() {
        keyVaultKeylessRsaSignature = new KeyVaultKeylessRsaSignature();
        AlgorithmParameterSpec algorithmParameterSpec = mock(AlgorithmParameterSpec.class);
        assertThrows(InvalidAlgorithmParameterException.class, () -> keyVaultKeylessRsaSignature.engineSetParameter(algorithmParameterSpec));
    }

    @Test
    public void engineSetParameterWithNullAlgorithmTest() {
        keyVaultKeylessRsaSignature = new KeyVaultKeylessRsaSignature();
        AlgorithmParameterSpec algorithmParameterSpec = new PSSParameterSpec("fake-value", "fake-value", null, 10, 10);
        assertThrows(InvalidAlgorithmParameterException.class, () -> keyVaultKeylessRsaSignature.engineSetParameter(algorithmParameterSpec));
    }
}
