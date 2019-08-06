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
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyCurveName;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;

import static org.junit.Assert.*;

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
                    .endpoint(getEndpoint())
                    .buildClient();
            });
        } else {
            client = clientSetup(pipeline -> {
                this.pipeline = pipeline;
                return new KeyClientBuilder()
                    .pipeline(pipeline)
                    .endpoint(getEndpoint())
                    .buildClient();
            });
        }
    }

    @Override
    public void encryptDecryptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKey";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] cipherText = cryptoClient.encrypt(algorithm, plainText).cipherText();
                byte[] decryptedText = serviceClient.decrypt(algorithm, cipherText, Context.NONE).block().plainText();

                assertArrayEquals(decryptedText, plainText);

                cipherText = serviceClient.encrypt(algorithm, plainText, Context.NONE).block().cipherText();
                decryptedText = cryptoClient.decrypt(algorithm, cipherText).plainText();

                assertArrayEquals(decryptedText, plainText);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }

    @Override
    public void wrapUnwraptRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKeyWrapUnwrap";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP, KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plainText).encryptedKey();
                byte[] decryptedKey = serviceClient.unwrapKey(algorithm, encryptedKey, Context.NONE).block().key();

                assertArrayEquals(decryptedKey, plainText);

                encryptedKey = serviceClient.wrapKey(algorithm, plainText, Context.NONE).block().encryptedKey();
                decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).key();

                assertArrayEquals(decryptedKey, plainText);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }


    @Override
    public void signVerifyRsa() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRSA(keyPair);
            String keyName = "testRsaKeySignVerify";
            Key importedKey = client.importKey(keyName, key);
            key.kid(importedKey.id());
            key.keyOps(importedKey.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            for (SignatureAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plainText = new byte[100];
                new Random(0x1234567L).nextBytes(plainText);
                byte[] signature = cryptoClient.signData(algorithm, plainText).signature();
                Boolean verifyStatus = serviceClient.verifyData(algorithm, plainText, signature, Context.NONE).block().isValid();

                assertTrue(verifyStatus);

                signature = serviceClient.signData(algorithm, plainText, Context.NONE).block().signature();
                verifyStatus = cryptoClient.verifyData(algorithm, plainText, signature).isValid();

                assertTrue(verifyStatus);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });
    }

    @Override
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

            JsonWebKey key = JsonWebKey.fromEC(keyPair, provider);
            String keyName = "testEcKey" + crv.toString();
            Key imported = client.importKey(keyName, key);
            key.kid(imported.id());
            key.keyOps(imported.keyMaterial().keyOps());
            CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .pipeline(pipeline)
                .jsonWebKey(key)
                .buildClient();
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            byte[] plainText = new byte[100];
            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plainText).signature();

            Boolean verifyStatus = serviceClient.verifyData(curveToSignature.get(crv), plainText, signature, Context.NONE).block().isValid();
            assertTrue(verifyStatus);

            signature = serviceClient.signData(curveToSignature.get(crv), plainText, Context.NONE).block().signature();
            verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plainText, signature).isValid();
            if (!interceptorManager.isPlaybackMode()) {
                assertTrue(verifyStatus);
            }

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);
            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        }

    }

    @Override
    public void wrapUnwrapSymmetricKeyAES128Kw() {
        // Arrange
        byte[] kek = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        byte[] cek = {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF};
        byte[] ek = {0x1F, (byte) 0xA6, (byte) 0x8B, 0x0A, (byte) 0x81, 0x12, (byte) 0xB4, 0x47, (byte) 0xAE, (byte) 0xF3, 0x4B, (byte) 0xD8, (byte) 0xFB, 0x5A, 0x7B, (byte) 0x82, (byte) 0x9D, 0x3E, (byte) 0x86, 0x23, 0x71, (byte) 0xD2, (byte) 0xCF, (byte) 0xE5};

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(kek, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)))
            .buildClient();

        byte[] encrypted = cryptoClient.wrapKey(KeyWrapAlgorithm.A128KW, cek).encryptedKey();

        assertArrayEquals(ek, encrypted);

        byte[] decrypted = cryptoClient.unwrapKey(KeyWrapAlgorithm.A128KW, encrypted).key();

        assertArrayEquals(cek, decrypted);
    }

    @Override
    public void wrapUnwrapSymmetricKeyAES192Kw() {
        // Arrange
        byte[] kek = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
        byte[] cek = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] ek = { (byte) 0x96, 0x77, (byte) 0x8B, 0x25, (byte) 0xAE, 0x6C, (byte) 0xA4, 0x35, (byte) 0xF9, 0x2B, 0x5B, (byte) 0x97, (byte) 0xC0, 0x50, (byte) 0xAE, (byte) 0xD2, 0x46, (byte) 0x8A, (byte) 0xB8, (byte) 0xA1, 0x7A, (byte) 0xD8, 0x4E, 0x5D };

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(kek, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)))
            .buildClient();

        byte[] encrypted = cryptoClient.wrapKey(KeyWrapAlgorithm.A192KW, cek).encryptedKey();

        assertArrayEquals(ek, encrypted);

        byte[] decrypted = cryptoClient.unwrapKey(KeyWrapAlgorithm.A192KW, encrypted).key();

        assertArrayEquals(cek, decrypted);
    }

    @Override
    public void wrapUnwrapSymmetricKeyAES256Kw() {
        // Arrange
        byte[] kek = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
        byte[] cek = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
        byte[] ek = { 0x64, (byte) 0xE8, (byte) 0xC3, (byte) 0xF9, (byte) 0xCE, 0x0F, 0x5B, (byte) 0xA2, 0x63, (byte) 0xE9, 0x77, 0x79, 0x05, (byte) 0x81, (byte) 0x8A, 0x2A, (byte) 0x93, (byte) 0xC8, 0x19, 0x1E, 0x7D, 0x6E, (byte) 0x8A, (byte) 0xE7 };

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(kek, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)))
            .buildClient();

        byte[] encrypted = cryptoClient.wrapKey(KeyWrapAlgorithm.A256KW, cek).encryptedKey();

        assertArrayEquals(ek, encrypted);

        byte[] decrypted = cryptoClient.unwrapKey(KeyWrapAlgorithm.A256KW, encrypted).key();

        assertArrayEquals(cek, decrypted);
    }

    @Override
    public void encryptDecryptSymmetricKeyAes128CbcHmacSha256() {
        byte[] key = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f
        };
        byte[] plaintext = {(byte) 0x41, (byte) 0x20, (byte) 0x63, (byte) 0x69, (byte) 0x70, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x73, (byte) 0x79, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6d, (byte) 0x20,
            (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x6e, (byte) 0x6f, (byte) 0x74, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x72, (byte) 0x65, (byte) 0x71, (byte) 0x75,
            (byte) 0x69, (byte) 0x72, (byte) 0x65, (byte) 0x64, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x72, (byte) 0x65,
            (byte) 0x74, (byte) 0x2c, (byte) 0x20, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x69, (byte) 0x74, (byte) 0x20, (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x62,
            (byte) 0x65, (byte) 0x20, (byte) 0x61, (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x66, (byte) 0x61, (byte) 0x6c, (byte) 0x6c, (byte) 0x20, (byte) 0x69,
            (byte) 0x6e, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x68, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x73, (byte) 0x20, (byte) 0x6f, (byte) 0x66,
            (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x65, (byte) 0x6e, (byte) 0x65, (byte) 0x6d, (byte) 0x79, (byte) 0x20, (byte) 0x77, (byte) 0x69, (byte) 0x74, (byte) 0x68, (byte) 0x6f,
            (byte) 0x75, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6e, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x76, (byte) 0x65, (byte) 0x6e, (byte) 0x69, (byte) 0x65, (byte) 0x6e, (byte) 0x63, (byte) 0x65
        };
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {
            (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73
        };
        byte[] expected = {
            (byte) 0xc8, (byte) 0x0e, (byte) 0xdf, (byte) 0xa3, (byte) 0x2d, (byte) 0xdf, (byte) 0x39, (byte) 0xd5, (byte) 0xef, (byte) 0x00, (byte) 0xc0, (byte) 0xb4, (byte) 0x68, (byte) 0x83, (byte) 0x42, (byte) 0x79,
            (byte) 0xa2, (byte) 0xe4, (byte) 0x6a, (byte) 0x1b, (byte) 0x80, (byte) 0x49, (byte) 0xf7, (byte) 0x92, (byte) 0xf7, (byte) 0x6b, (byte) 0xfe, (byte) 0x54, (byte) 0xb9, (byte) 0x03, (byte) 0xa9, (byte) 0xc9,
            (byte) 0xa9, (byte) 0x4a, (byte) 0xc9, (byte) 0xb4, (byte) 0x7a, (byte) 0xd2, (byte) 0x65, (byte) 0x5c, (byte) 0x5f, (byte) 0x10, (byte) 0xf9, (byte) 0xae, (byte) 0xf7, (byte) 0x14, (byte) 0x27, (byte) 0xe2,
            (byte) 0xfc, (byte) 0x6f, (byte) 0x9b, (byte) 0x3f, (byte) 0x39, (byte) 0x9a, (byte) 0x22, (byte) 0x14, (byte) 0x89, (byte) 0xf1, (byte) 0x63, (byte) 0x62, (byte) 0xc7, (byte) 0x03, (byte) 0x23, (byte) 0x36,
            (byte) 0x09, (byte) 0xd4, (byte) 0x5a, (byte) 0xc6, (byte) 0x98, (byte) 0x64, (byte) 0xe3, (byte) 0x32, (byte) 0x1c, (byte) 0xf8, (byte) 0x29, (byte) 0x35, (byte) 0xac, (byte) 0x40, (byte) 0x96, (byte) 0xc8,
            (byte) 0x6e, (byte) 0x13, (byte) 0x33, (byte) 0x14, (byte) 0xc5, (byte) 0x40, (byte) 0x19, (byte) 0xe8, (byte) 0xca, (byte) 0x79, (byte) 0x80, (byte) 0xdf, (byte) 0xa4, (byte) 0xb9, (byte) 0xcf, (byte) 0x1b,
            (byte) 0x38, (byte) 0x4c, (byte) 0x48, (byte) 0x6f, (byte) 0x3a, (byte) 0x54, (byte) 0xc5, (byte) 0x10, (byte) 0x78, (byte) 0x15, (byte) 0x8e, (byte) 0xe5, (byte) 0xd7, (byte) 0x9d, (byte) 0xe5, (byte) 0x9f,
            (byte) 0xbd, (byte) 0x34, (byte) 0xd8, (byte) 0x48, (byte) 0xb3, (byte) 0xd6, (byte) 0x95, (byte) 0x50, (byte) 0xa6, (byte) 0x76, (byte) 0x46, (byte) 0x34, (byte) 0x44, (byte) 0x27, (byte) 0xad, (byte) 0xe5,
            (byte) 0x4b, (byte) 0x88, (byte) 0x51, (byte) 0xff, (byte) 0xb5, (byte) 0x98, (byte) 0xf7, (byte) 0xf8, (byte) 0x00, (byte) 0x74, (byte) 0xb9, (byte) 0x47, (byte) 0x3c, (byte) 0x82, (byte) 0xe2, (byte) 0xdb
        };
        byte[] authTag = {(byte) 0x65, (byte) 0x2c, (byte) 0x3f, (byte) 0xa3, (byte) 0x6b, (byte) 0x0a, (byte) 0x7c, (byte) 0x5b, (byte) 0x32, (byte) 0x19, (byte) 0xfa, (byte) 0xb3, (byte) 0xa3, (byte) 0x0b, (byte) 0xc1, (byte) 0xc4};

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(key, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
            .buildClient();

        byte[] encrypted = cryptoClient.encrypt(EncryptionAlgorithm.A128CBC_HS256, plaintext, iv, authData).cipherText();

        assertArrayEquals(expected, encrypted);

        byte[] decrypted = cryptoClient.decrypt(EncryptionAlgorithm.A128CBC_HS256, encrypted, iv, authData, authTag).plainText();

        assertArrayEquals(plaintext, decrypted);
    }

    @Override
    public void encryptDecryptSymmetricKeyAes128CbcHmacSha384() {
        // Arrange: These values are taken from Appendix B of the JWE specification at
        // https://tools.ietf.org/html/draft-ietf-jose-json-web-encryption-40#appendix-B
        byte[] key = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
            (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f};
        byte[] plaintext = {(byte) 0x41, (byte) 0x20, (byte) 0x63, (byte) 0x69, (byte) 0x70, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x73, (byte) 0x79, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6d, (byte) 0x20,
            (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x6e, (byte) 0x6f, (byte) 0x74, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x72, (byte) 0x65, (byte) 0x71, (byte) 0x75,
            (byte) 0x69, (byte) 0x72, (byte) 0x65, (byte) 0x64, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x72, (byte) 0x65,
            (byte) 0x74, (byte) 0x2c, (byte) 0x20, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x69, (byte) 0x74, (byte) 0x20, (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x62,
            (byte) 0x65, (byte) 0x20, (byte) 0x61, (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x66, (byte) 0x61, (byte) 0x6c, (byte) 0x6c, (byte) 0x20, (byte) 0x69,
            (byte) 0x6e, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x68, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x73, (byte) 0x20, (byte) 0x6f, (byte) 0x66,
            (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x65, (byte) 0x6e, (byte) 0x65, (byte) 0x6d, (byte) 0x79, (byte) 0x20, (byte) 0x77, (byte) 0x69, (byte) 0x74, (byte) 0x68, (byte) 0x6f,
            (byte) 0x75, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6e, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x76, (byte) 0x65, (byte) 0x6e, (byte) 0x69, (byte) 0x65, (byte) 0x6e, (byte) 0x63, (byte) 0x65};
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {(byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73};
        byte[] expected = {(byte) 0xea, (byte) 0x65, (byte) 0xda, (byte) 0x6b, (byte) 0x59, (byte) 0xe6, (byte) 0x1e, (byte) 0xdb, (byte) 0x41, (byte) 0x9b, (byte) 0xe6, (byte) 0x2d, (byte) 0x19, (byte) 0x71, (byte) 0x2a, (byte) 0xe5,
            (byte) 0xd3, (byte) 0x03, (byte) 0xee, (byte) 0xb5, (byte) 0x00, (byte) 0x52, (byte) 0xd0, (byte) 0xdf, (byte) 0xd6, (byte) 0x69, (byte) 0x7f, (byte) 0x77, (byte) 0x22, (byte) 0x4c, (byte) 0x8e, (byte) 0xdb,
            (byte) 0x00, (byte) 0x0d, (byte) 0x27, (byte) 0x9b, (byte) 0xdc, (byte) 0x14, (byte) 0xc1, (byte) 0x07, (byte) 0x26, (byte) 0x54, (byte) 0xbd, (byte) 0x30, (byte) 0x94, (byte) 0x42, (byte) 0x30, (byte) 0xc6,
            (byte) 0x57, (byte) 0xbe, (byte) 0xd4, (byte) 0xca, (byte) 0x0c, (byte) 0x9f, (byte) 0x4a, (byte) 0x84, (byte) 0x66, (byte) 0xf2, (byte) 0x2b, (byte) 0x22, (byte) 0x6d, (byte) 0x17, (byte) 0x46, (byte) 0x21,
            (byte) 0x4b, (byte) 0xf8, (byte) 0xcf, (byte) 0xc2, (byte) 0x40, (byte) 0x0a, (byte) 0xdd, (byte) 0x9f, (byte) 0x51, (byte) 0x26, (byte) 0xe4, (byte) 0x79, (byte) 0x66, (byte) 0x3f, (byte) 0xc9, (byte) 0x0b,
            (byte) 0x3b, (byte) 0xed, (byte) 0x78, (byte) 0x7a, (byte) 0x2f, (byte) 0x0f, (byte) 0xfc, (byte) 0xbf, (byte) 0x39, (byte) 0x04, (byte) 0xbe, (byte) 0x2a, (byte) 0x64, (byte) 0x1d, (byte) 0x5c, (byte) 0x21,
            (byte) 0x05, (byte) 0xbf, (byte) 0xe5, (byte) 0x91, (byte) 0xba, (byte) 0xe2, (byte) 0x3b, (byte) 0x1d, (byte) 0x74, (byte) 0x49, (byte) 0xe5, (byte) 0x32, (byte) 0xee, (byte) 0xf6, (byte) 0x0a, (byte) 0x9a,
            (byte) 0xc8, (byte) 0xbb, (byte) 0x6c, (byte) 0x6b, (byte) 0x01, (byte) 0xd3, (byte) 0x5d, (byte) 0x49, (byte) 0x78, (byte) 0x7b, (byte) 0xcd, (byte) 0x57, (byte) 0xef, (byte) 0x48, (byte) 0x49, (byte) 0x27,
            (byte) 0xf2, (byte) 0x80, (byte) 0xad, (byte) 0xc9, (byte) 0x1a, (byte) 0xc0, (byte) 0xc4, (byte) 0xe7, (byte) 0x9c, (byte) 0x7b, (byte) 0x11, (byte) 0xef, (byte) 0xc6, (byte) 0x00, (byte) 0x54, (byte) 0xe3};
        byte[] authTags = {(byte) 0x84, (byte) 0x90, (byte) 0xac, (byte) 0x0e, (byte) 0x58, (byte) 0x94, (byte) 0x9b, (byte) 0xfe, (byte) 0x51, (byte) 0x87, (byte) 0x5d, (byte) 0x73, (byte) 0x3f, (byte) 0x93, (byte) 0xac, (byte) 0x20,
            (byte) 0x75, (byte) 0x16, (byte) 0x80, (byte) 0x39, (byte) 0xcc, (byte) 0xc7, (byte) 0x33, (byte) 0xd7};

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(key, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
            .buildClient();

        byte[] encrypted = cryptoClient.encrypt(EncryptionAlgorithm.A192CBC_HS384, plaintext, iv, authData).cipherText();

        assertArrayEquals(expected, encrypted);

        byte[] decrypted = cryptoClient.decrypt(EncryptionAlgorithm.A192CBC_HS384, encrypted, iv, authData, authTags).plainText();

        assertArrayEquals(plaintext, decrypted);
    }

    @Override
    public void encryptDecryptSymmetricKeyAes128CbcHmacSha512() {
        // Arrange: These values are taken from Appendix B of the JWE specification at
        // https://tools.ietf.org/html/draft-ietf-jose-json-web-encryption-40#appendix-B
        byte[] key = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0a, (byte) 0x0b, (byte) 0x0c, (byte) 0x0d, (byte) 0x0e, (byte) 0x0f,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13, (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17, (byte) 0x18, (byte) 0x19, (byte) 0x1a, (byte) 0x1b, (byte) 0x1c, (byte) 0x1d, (byte) 0x1e, (byte) 0x1f,
            (byte) 0x20, (byte) 0x21, (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26, (byte) 0x27, (byte) 0x28, (byte) 0x29, (byte) 0x2a, (byte) 0x2b, (byte) 0x2c, (byte) 0x2d, (byte) 0x2e, (byte) 0x2f,
            (byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x3a, (byte) 0x3b, (byte) 0x3c, (byte) 0x3d, (byte) 0x3e, (byte) 0x3f};
        byte[] plaintext = {(byte) 0x41, (byte) 0x20, (byte) 0x63, (byte) 0x69, (byte) 0x70, (byte) 0x68, (byte) 0x65, (byte) 0x72, (byte) 0x20, (byte) 0x73, (byte) 0x79, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6d, (byte) 0x20,
            (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x6e, (byte) 0x6f, (byte) 0x74, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x72, (byte) 0x65, (byte) 0x71, (byte) 0x75,
            (byte) 0x69, (byte) 0x72, (byte) 0x65, (byte) 0x64, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x62, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x72, (byte) 0x65,
            (byte) 0x74, (byte) 0x2c, (byte) 0x20, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x69, (byte) 0x74, (byte) 0x20, (byte) 0x6d, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x20, (byte) 0x62,
            (byte) 0x65, (byte) 0x20, (byte) 0x61, (byte) 0x62, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x66, (byte) 0x61, (byte) 0x6c, (byte) 0x6c, (byte) 0x20, (byte) 0x69,
            (byte) 0x6e, (byte) 0x74, (byte) 0x6f, (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x68, (byte) 0x61, (byte) 0x6e, (byte) 0x64, (byte) 0x73, (byte) 0x20, (byte) 0x6f, (byte) 0x66,
            (byte) 0x20, (byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x65, (byte) 0x6e, (byte) 0x65, (byte) 0x6d, (byte) 0x79, (byte) 0x20, (byte) 0x77, (byte) 0x69, (byte) 0x74, (byte) 0x68, (byte) 0x6f,
            (byte) 0x75, (byte) 0x74, (byte) 0x20, (byte) 0x69, (byte) 0x6e, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x76, (byte) 0x65, (byte) 0x6e, (byte) 0x69, (byte) 0x65, (byte) 0x6e, (byte) 0x63, (byte) 0x65};
        byte[] iv = {(byte) 0x1a, (byte) 0xf3, (byte) 0x8c, (byte) 0x2d, (byte) 0xc2, (byte) 0xb9, (byte) 0x6f, (byte) 0xfd, (byte) 0xd8, (byte) 0x66, (byte) 0x94, (byte) 0x09, (byte) 0x23, (byte) 0x41, (byte) 0xbc, (byte) 0x04};
        byte[] authData = {(byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x73, (byte) 0x65, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x64, (byte) 0x20, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x69, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x20, (byte) 0x6f, (byte) 0x66, (byte) 0x20, (byte) 0x41, (byte) 0x75, (byte) 0x67, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x20,
            (byte) 0x4b, (byte) 0x65, (byte) 0x72, (byte) 0x63, (byte) 0x6b, (byte) 0x68, (byte) 0x6f, (byte) 0x66, (byte) 0x66, (byte) 0x73};
        byte[] expected = {(byte) 0x4a, (byte) 0xff, (byte) 0xaa, (byte) 0xad, (byte) 0xb7, (byte) 0x8c, (byte) 0x31, (byte) 0xc5, (byte) 0xda, (byte) 0x4b, (byte) 0x1b, (byte) 0x59, (byte) 0x0d, (byte) 0x10, (byte) 0xff, (byte) 0xbd,
            (byte) 0x3d, (byte) 0xd8, (byte) 0xd5, (byte) 0xd3, (byte) 0x02, (byte) 0x42, (byte) 0x35, (byte) 0x26, (byte) 0x91, (byte) 0x2d, (byte) 0xa0, (byte) 0x37, (byte) 0xec, (byte) 0xbc, (byte) 0xc7, (byte) 0xbd,
            (byte) 0x82, (byte) 0x2c, (byte) 0x30, (byte) 0x1d, (byte) 0xd6, (byte) 0x7c, (byte) 0x37, (byte) 0x3b, (byte) 0xcc, (byte) 0xb5, (byte) 0x84, (byte) 0xad, (byte) 0x3e, (byte) 0x92, (byte) 0x79, (byte) 0xc2,
            (byte) 0xe6, (byte) 0xd1, (byte) 0x2a, (byte) 0x13, (byte) 0x74, (byte) 0xb7, (byte) 0x7f, (byte) 0x07, (byte) 0x75, (byte) 0x53, (byte) 0xdf, (byte) 0x82, (byte) 0x94, (byte) 0x10, (byte) 0x44, (byte) 0x6b,
            (byte) 0x36, (byte) 0xeb, (byte) 0xd9, (byte) 0x70, (byte) 0x66, (byte) 0x29, (byte) 0x6a, (byte) 0xe6, (byte) 0x42, (byte) 0x7e, (byte) 0xa7, (byte) 0x5c, (byte) 0x2e, (byte) 0x08, (byte) 0x46, (byte) 0xa1,
            (byte) 0x1a, (byte) 0x09, (byte) 0xcc, (byte) 0xf5, (byte) 0x37, (byte) 0x0d, (byte) 0xc8, (byte) 0x0b, (byte) 0xfe, (byte) 0xcb, (byte) 0xad, (byte) 0x28, (byte) 0xc7, (byte) 0x3f, (byte) 0x09, (byte) 0xb3,
            (byte) 0xa3, (byte) 0xb7, (byte) 0x5e, (byte) 0x66, (byte) 0x2a, (byte) 0x25, (byte) 0x94, (byte) 0x41, (byte) 0x0a, (byte) 0xe4, (byte) 0x96, (byte) 0xb2, (byte) 0xe2, (byte) 0xe6, (byte) 0x60, (byte) 0x9e,
            (byte) 0x31, (byte) 0xe6, (byte) 0xe0, (byte) 0x2c, (byte) 0xc8, (byte) 0x37, (byte) 0xf0, (byte) 0x53, (byte) 0xd2, (byte) 0x1f, (byte) 0x37, (byte) 0xff, (byte) 0x4f, (byte) 0x51, (byte) 0x95, (byte) 0x0b,
            (byte) 0xbe, (byte) 0x26, (byte) 0x38, (byte) 0xd0, (byte) 0x9d, (byte) 0xd7, (byte) 0xa4, (byte) 0x93, (byte) 0x09, (byte) 0x30, (byte) 0x80, (byte) 0x6d, (byte) 0x07, (byte) 0x03, (byte) 0xb1, (byte) 0xf6};
        byte[] authTags = {(byte) 0x4d, (byte) 0xd3, (byte) 0xb4, (byte) 0xc0, (byte) 0x88, (byte) 0xa7, (byte) 0xf4, (byte) 0x5c, (byte) 0x21, (byte) 0x68, (byte) 0x39, (byte) 0x64, (byte) 0x5b, (byte) 0x20, (byte) 0x12, (byte) 0xbf,
            (byte) 0x2e, (byte) 0x62, (byte) 0x69, (byte) 0xa8, (byte) 0xc5, (byte) 0x6a, (byte) 0x81, (byte) 0x6d, (byte) 0xbc, (byte) 0x1b, (byte) 0x26, (byte) 0x77, (byte) 0x61, (byte) 0x95, (byte) 0x5b, (byte) 0xc5};

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(key, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
            .buildClient();

        byte[] encrypted = cryptoClient.encrypt(EncryptionAlgorithm.A256CBC_HS512, plaintext, iv, authData).cipherText();

        assertArrayEquals(expected, encrypted);

        byte[] decrypted = cryptoClient.decrypt(EncryptionAlgorithm.A256CBC_HS512, encrypted, iv, authData, authTags).plainText();

        assertArrayEquals(plaintext, decrypted);
    }

    @Override
    public void encryptDecryptSymmetricKeyaes128CbcOneBlock() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors from RFC3602 do not use padding.
        byte[] keyContent = { 0x06, (byte) 0xa9, 0x21, 0x40, 0x36, (byte) 0xb8, (byte) 0xa1, 0x5b, 0x51, 0x2e, 0x03, (byte) 0xd5, 0x34, 0x12, 0x00, 0x06 };
        byte[] plaintext = "Single block msg".getBytes();
        byte[] initializationVector = { 0x3d, (byte) 0xaf, (byte) 0xba, 0x42, (byte) 0x9d, (byte) 0x9e, (byte) 0xb4, 0x30, (byte) 0xb4, 0x22, (byte) 0xda, (byte) 0x80, 0x2c, (byte) 0x9f, (byte) 0xac, 0x41 };
        byte[] expected = { (byte) 0xe3, 0x53, 0x77, (byte) 0x9c, 0x10, 0x79, (byte) 0xae, (byte) 0xb8, 0x27, 0x08, (byte) 0x94, 0x2d, (byte) 0xbe, 0x77, 0x18, 0x1a };

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(keyContent, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
            .buildClient();

        byte[] encrypted = cryptoClient.encrypt(EncryptionAlgorithm.A128CBC, plaintext, initializationVector, null).cipherText();

        assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 16), expected);

        byte[] decrypted = cryptoClient.decrypt(EncryptionAlgorithm.A128CBC, encrypted, initializationVector, null, null).plainText();

        assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 16), plaintext);
    }

    @Override
    public void encryptDecryptSymmetricKeyaes128CbcTwoBlock() {
        // Note that AES128CBC as implemented in this library uses PKCS7 padding mode where the test
        // vectors do not use padding.
        byte[] keyContent   = { (byte) 0xc2, (byte) 0x86, 0x69, 0x6d, (byte) 0x88, 0x7c, (byte) 0x9a, (byte) 0xa0, 0x61, 0x1b, (byte) 0xbb, 0x3e, 0x20, 0x25, (byte) 0xa4, 0x5a };
        byte[] plaintext = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
        byte[] initializationVector    = { 0x56, 0x2e, 0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58 };
        byte[] expected    = { (byte) 0xd2, (byte) 0x96, (byte) 0xcd, (byte) 0x94, (byte) 0xc2, (byte) 0xcc, (byte) 0xcf, (byte) 0x8a, 0x3a, (byte) 0x86, 0x30, 0x28, (byte) 0xb5, (byte) 0xe1, (byte) 0xdc, 0x0a, 0x75, (byte) 0x86, 0x60, 0x2d, 0x25, 0x3c, (byte) 0xff, (byte) 0xf9, 0x1b, (byte) 0x82, 0x66, (byte) 0xbe, (byte) 0xa6, (byte) 0xd6, 0x1a, (byte) 0xb1 };

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
            .pipeline(pipeline)
            .jsonWebKey(JsonWebKey.fromAes(new SecretKeySpec(keyContent, "AES"))
                .kty(KeyType.OCT)
                .keyOps(Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)))
            .buildClient();

        byte[] encrypted = cryptoClient.encrypt(EncryptionAlgorithm.A128CBC, plaintext, initializationVector, null).cipherText();

        assertArrayEquals(Arrays.copyOfRange(encrypted, 0, 32), expected);

        byte[] decrypted = cryptoClient.decrypt(EncryptionAlgorithm.A128CBC, encrypted, initializationVector, null, null).plainText();

        assertArrayEquals(Arrays.copyOfRange(decrypted, 0, 32), plaintext);
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
