// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalCryptographyClientTest extends LocalCryptographyClientTestBase {

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }


    private LocalCryptographyClient initializeCryptographyClient(JsonWebKey key) {
        return new LocalCryptographyClientBuilder()
            .key(key)
            .buildClient();
    }

    @Test
    public void encryptDecryptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair, Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT));
            LocalCryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] cipherText = cryptoClient.encrypt(algorithm, plainText).getCipherText();
                byte[] decryptedText = cryptoClient.decrypt(algorithm, cipherText).getPlainText();

                assertArrayEquals(decryptedText, plainText);
            }
        });
    }

    @Test
    public void wrapUnwraptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair, Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
            LocalCryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plainText).getEncryptedKey();
                byte[] decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plainText);
            }

        });
    }

    @Test
    public void signVerifyEc() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Map<KeyCurveName, SignatureAlgorithm> curveToSignature = new HashMap<>();
        curveToSignature.put(KeyCurveName.P_256, SignatureAlgorithm.ES256);
        curveToSignature.put(KeyCurveName.P_384, SignatureAlgorithm.ES384);
        curveToSignature.put(KeyCurveName.P_521, SignatureAlgorithm.ES512);
        curveToSignature.put(KeyCurveName.P_256K, SignatureAlgorithm.ES256K);

        Map<KeyCurveName, String> curveToSpec = new HashMap<>();
        curveToSpec.put(KeyCurveName.P_256, "secp256r1");
        curveToSpec.put(KeyCurveName.P_384, "secp384r1");
        curveToSpec.put(KeyCurveName.P_521, "secp521r1");
        curveToSpec.put(KeyCurveName.P_256K, "secp256k1");

        List<KeyCurveName> curveList = Arrays.asList(KeyCurveName.P_256, KeyCurveName.P_384, KeyCurveName.P_521, KeyCurveName.P_256K);
        Provider provider = Security.getProvider("SunEC");
        for (KeyCurveName crv : curveList) {

            final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", provider);
            ECGenParameterSpec gps = new ECGenParameterSpec(curveToSpec.get(crv));
            generator.initialize(gps);
            KeyPair keyPair = generator.generateKeyPair();

            JsonWebKey key = JsonWebKey.fromEc(keyPair, provider, Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
            LocalCryptographyClient cryptoClient = initializeCryptographyClient(key);

            byte[] plainText = new byte[100];
            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plainText).getSignature();

            Boolean verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plainText, signature).isValid();
            assertTrue(verifyStatus);
        }

    }
}
