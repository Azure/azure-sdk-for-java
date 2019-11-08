// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptographyClientTest extends CryptographyClientTestBase {

    private KeyClient client;
    private HttpPipeline pipeline;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> {
                this.pipeline = pipeline;
                return new KeyClientBuilder()
                    .pipeline(pipeline)
                    .vaultUrl(getEndpoint())
                    .buildClient();
            });
        } else {
            client = clientSetup(pipeline -> {
                this.pipeline = pipeline;
                return new KeyClientBuilder()
                    .pipeline(pipeline)
                    .vaultUrl(getEndpoint())
                    .buildClient();
            });
        }
    }

    @Test
    public void encryptDecryptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = "testRsaKey";
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .keyIdentifier(importedKey.getId())
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] cipherText = cryptoClient.encrypt(algorithm, plainText).getCipherText();
                byte[] decryptedText = serviceClient.decrypt(algorithm, cipherText, Context.NONE).block().getPlainText();

                assertArrayEquals(decryptedText, plainText);

                cipherText = serviceClient.encrypt(algorithm, plainText, Context.NONE).block().getCipherText();
                decryptedText = cryptoClient.decrypt(algorithm, cipherText).getPlainText();

                assertArrayEquals(decryptedText, plainText);
            }

            client.beginDeleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }

    @Test
    public void wrapUnwraptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = "testRsaKeyWrapUnwrap";
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .keyIdentifier(importedKey.getId())
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP, KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plainText).getEncryptedKey();
                byte[] decryptedKey = serviceClient.unwrapKey(algorithm, encryptedKey, Context.NONE).block().getKey();

                assertArrayEquals(decryptedKey, plainText);

                encryptedKey = serviceClient.wrapKey(algorithm, plainText, Context.NONE).block().getEncryptedKey();
                decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plainText);
            }

            client.beginDeleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }


    @Test
    public void signVerifyRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = "testRsaKeySignVerify";
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .keyIdentifier(importedKey.getId())
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            for (SignatureAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] signature = cryptoClient.signData(algorithm, plainText).getSignature();
                Boolean verifyStatus = serviceClient.verifyData(algorithm, plainText, signature, Context.NONE).block().isValid();

                assertTrue(verifyStatus);

                signature = serviceClient.signData(algorithm, plainText, Context.NONE).block().getSignature();
                verifyStatus = cryptoClient.verifyData(algorithm, plainText, signature).isValid();

                assertTrue(verifyStatus);
            }

            client.beginDeleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
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

        List<KeyCurveName> curveList =  Arrays.asList(KeyCurveName.P_256, KeyCurveName.P_384, KeyCurveName.P_521, KeyCurveName.P_256K);
        Provider provider = Security.getProvider("SunEC");
        for (KeyCurveName crv : curveList) {

            final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", provider);
            ECGenParameterSpec gps = new ECGenParameterSpec(curveToSpec.get(crv));
            generator.initialize(gps);
            KeyPair keyPair = generator.generateKeyPair();

            JsonWebKey key = JsonWebKey.fromEc(keyPair, provider);
            String keyName = "testEcKey" + crv.toString();
            KeyVaultKey imported = client.importKey(keyName, key);
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .keyIdentifier(imported.getId())
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            byte[] plainText = new byte[100];
            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plainText).getSignature();

            Boolean verifyStatus = serviceClient.verifyData(curveToSignature.get(crv), plainText, signature, Context.NONE).block().isValid();
            assertTrue(verifyStatus);

            signature = serviceClient.signData(curveToSignature.get(crv), plainText, Context.NONE).block().getSignature();
            verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plainText, signature).isValid();
            if (!interceptorManager.isPlaybackMode()) {
                assertTrue(verifyStatus);
            }

            client.beginDeleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        }

    }

    private void pollOnKeyDeletion(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s not found \n", keyName);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s was not purged \n", keyName);
    }
}
