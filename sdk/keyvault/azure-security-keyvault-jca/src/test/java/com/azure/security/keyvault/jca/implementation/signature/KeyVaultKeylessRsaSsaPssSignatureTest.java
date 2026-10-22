// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.KeyVaultJcaPropertyNames;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.KeyVaultPrivateKey;
import com.azure.security.keyvault.jca.implementation.MockKeyVaultClient;
import com.azure.security.keyvault.jca.implementation.mocking.MockPrivateKey;
import com.azure.security.keyvault.jca.implementation.mocking.MockPublicKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultKeylessRsaSsaPssSignatureTest {

    KeyVaultKeylessRsaSsaPssSignature keyVaultKeylessRsaSsaPssSignature;

    private final PublicKey publicKey = new MockPublicKey();
    private final PrivateKey privateKey = new MockPrivateKey();

    static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    private String previousKeyVaultUri;

    @BeforeEach
    public void before() {
        previousKeyVaultUri = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, KEY_VAULT_TEST_URI_GLOBAL);
        keyVaultKeylessRsaSsaPssSignature = new KeyVaultKeylessRsaSsaPssSignature();
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
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineInitVerify(publicKey));
    }

    @Test
    public void engineInitSignTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineInitSign(privateKey));
    }

    @Test
    public void engineInitSignWithRandomTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineInitSign(privateKey, null));
    }

    @Test
    public void engineVerify() {
        assertThrows(UnsupportedOperationException.class, () -> keyVaultKeylessRsaSsaPssSignature.engineVerify(null));
    }

    @Test
    public void engineSetParameterTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineSetParameter("", null));
    }

    @Test
    public void setDigestNameAndEngineSignTest() throws InvalidAlgorithmParameterException {
        KeyVaultClient keyVaultClient = new MockKeyVaultClient() {
            @Override
            public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
                return "PS256".equals(digestName) ? "fakeValue".getBytes(StandardCharsets.UTF_8) : null;
            }
        };
        KeyVaultPrivateKey keyVaultPrivateKey = new KeyVaultPrivateKey("algorithm", "kid", keyVaultClient);
        keyVaultKeylessRsaSsaPssSignature = new KeyVaultKeylessRsaSsaPssSignature();
        keyVaultKeylessRsaSsaPssSignature.engineInitSign(keyVaultPrivateKey, null);
        keyVaultKeylessRsaSsaPssSignature
            .engineSetParameter(new PSSParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, 20, 1));
        assertArrayEquals("fakeValue".getBytes(), keyVaultKeylessRsaSsaPssSignature.engineSign());
    }

    @Test
    public void engineSetParameterWithNullParameterTest() {
        keyVaultKeylessRsaSsaPssSignature = new KeyVaultKeylessRsaSsaPssSignature();
        assertThrows(InvalidAlgorithmParameterException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineSetParameter(null));
    }

    @Test
    public void engineSetParameterWithNotPSSParameterSpecTest() {
        keyVaultKeylessRsaSsaPssSignature = new KeyVaultKeylessRsaSsaPssSignature();
        AlgorithmParameterSpec algorithmParameterSpec = new AlgorithmParameterSpec() {
        };
        assertThrows(InvalidAlgorithmParameterException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineSetParameter(algorithmParameterSpec));
    }

    @Test
    public void engineSetParameterWithNullAlgorithmTest() {
        keyVaultKeylessRsaSsaPssSignature = new KeyVaultKeylessRsaSsaPssSignature();
        AlgorithmParameterSpec algorithmParameterSpec = new PSSParameterSpec("fake-value", "fake-value", null, 10, 10);
        assertThrows(InvalidAlgorithmParameterException.class,
            () -> keyVaultKeylessRsaSsaPssSignature.engineSetParameter(algorithmParameterSpec));
    }
}
