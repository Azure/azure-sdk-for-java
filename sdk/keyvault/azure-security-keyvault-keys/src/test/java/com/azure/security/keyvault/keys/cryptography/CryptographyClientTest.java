// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.InvalidAlgorithmParameterException;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
//import java.security.Provider;
//import java.security.Security;
//import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;
import java.util.Random;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;

public class CryptographyClientTest extends CryptographyClientTestBase {
    private KeyClient client;
    private HttpPipeline pipeline;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void initializeKeyClient(HttpClient httpClient) {
        pipeline = getHttpPipeline(httpClient);
        client = new KeyClientBuilder()
            .pipeline(pipeline)
            .vaultUrl(getEndpoint())
            .buildClient();
    }

    private CryptographyClient initializeCryptographyClient(String keyId, HttpClient httpClient, CryptographyServiceVersion serviceVersion) {
        pipeline = getHttpPipeline(httpClient);
        return new CryptographyClientBuilder()
            .pipeline(pipeline)
            .serviceVersion(serviceVersion)
            .keyIdentifier(keyId)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void encryptDecryptRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKey", 20);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] ciphertext = cryptoClient.encrypt(algorithm, plaintext).getCipherText();
                byte[] decryptedText = serviceClient.decrypt(algorithm, ciphertext, Context.NONE).block().getPlainText();

                assertArrayEquals(decryptedText, plaintext);

                ciphertext = serviceClient.encrypt(algorithm, plaintext, Context.NONE).block().getCipherText();
                decryptedText = cryptoClient.decrypt(algorithm, ciphertext).getPlainText();

                assertArrayEquals(decryptedText, plaintext);
            }
        });
    }

    @Test
    public void encryptDecryptRsaLocal() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair, Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT));
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void wrapUnwrapRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKeyWrapUnwrap", 25);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP, KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plaintext).getEncryptedKey();
                byte[] decryptedKey =
                    serviceClient.unwrapKey(algorithm, encryptedKey, Context.NONE).block().getKey();

                assertArrayEquals(decryptedKey, plaintext);

                encryptedKey =
                    serviceClient.wrapKey(algorithm, plaintext, Context.NONE).block().getEncryptedKey();
                decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plaintext);
            }

        });
    }

    @Test
    public void wrapUnwrapRsaLocal() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair, Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY));
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKeySignVerify", 25);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            for (SignatureAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] signature = cryptoClient.signData(algorithm, plaintext).getSignature();
                Boolean verifyStatus = serviceClient.verifyData(algorithm, plaintext, signature, Context.NONE).block().isValid();

                assertTrue(verifyStatus);

                signature = serviceClient.signData(algorithm, plaintext, Context.NONE).block().getSignature();
                verifyStatus = cryptoClient.verifyData(algorithm, plaintext, signature).isValid();

                assertTrue(verifyStatus);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // TODO: Uncomment after fixing https://github.com/Azure/azure-sdk-for-java/issues/21677
        /*initializeKeyClient(httpClient);
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
        String algorithmName = "EC";
        Provider[] providers = Security.getProviders();
        Provider provider = null;

        for (Provider currentProvider: providers) {
            if (currentProvider.containsValue(algorithmName)) {
                provider = currentProvider;

                break;
            }
        }

        if (provider == null) {
            for (Provider currentProvider : providers) {
                System.out.println(currentProvider.getName());
            }

            fail(String.format("No suitable security provider for algorithm %s was found.", algorithmName));
        }

        for (KeyCurveName crv : curveList) {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName, provider);
            ECGenParameterSpec gps = new ECGenParameterSpec(curveToSpec.get(crv));
            generator.initialize(gps);
            KeyPair keyPair = generator.generateKeyPair();

            JsonWebKey key = JsonWebKey.fromEc(keyPair, provider);
            String keyName = testResourceNamer.randomName("testEcKey" + crv.toString(), 20);
            KeyVaultKey imported = client.importKey(keyName, key);
            CryptographyClient cryptoClient = initializeCryptographyClient(imported.getId(), httpClient, serviceVersion);
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            byte[] plaintext = new byte[100];
            new Random(0x1234567L).nextBytes(plaintext);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plaintext).getSignature();

            Boolean verifyStatus = serviceClient.verifyData(curveToSignature.get(crv), plaintext, signature, Context.NONE).block().isValid();
            assertTrue(verifyStatus);

            signature = serviceClient.signData(curveToSignature.get(crv), plaintext, Context.NONE).block().getSignature();
            verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plaintext, signature).isValid();
            if (!interceptorManager.isPlaybackMode()) {
                assertTrue(verifyStatus);
            }
        }*/
    }

    @Test
    public void signVerifyEcLocal() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // TODO: Uncomment after fixing https://github.com/Azure/azure-sdk-for-java/issues/21677
        /*Map<KeyCurveName, SignatureAlgorithm> curveToSignature = new HashMap<>();
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
        String algorithmName = "EC";
        Provider[] providers = Security.getProviders();
        Provider provider = null;

        for (Provider currentProvider: providers) {
            if (currentProvider.containsValue(algorithmName)) {
                provider = currentProvider;

                break;
            }
        }

        if (provider == null) {
            for (Provider currentProvider : providers) {
                System.out.println(currentProvider.getName());
            }

            fail(String.format("No suitable security provider for algorithm %s was found.", algorithmName));
        }

        for (KeyCurveName crv : curveList) {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName, provider);
            ECGenParameterSpec gps = new ECGenParameterSpec(curveToSpec.get(crv));
            generator.initialize(gps);
            KeyPair keyPair = generator.generateKeyPair();

            JsonWebKey key = JsonWebKey.fromEc(keyPair, provider, Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

            byte[] plainText = new byte[100];
            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature = cryptoClient.signData(curveToSignature.get(crv), plainText).getSignature();

            Boolean verifyStatus = cryptoClient.verifyData(curveToSignature.get(crv), plainText, signature).isValid();
            assertTrue(verifyStatus);
        }*/
    }

    @Test
    public void encryptDecryptAes128CbcLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA128CbcParameters(plaintext, iv);

        encryptDecryptAesCbc(128, encryptParameters);
    }

    @Test
    public void encryptDecryptAes192CbcLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA192CbcParameters(plaintext, iv);

        encryptDecryptAesCbc(256, encryptParameters);
    }

    @Test
    public void encryptDecryptAes256CbcLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA256CbcParameters(plaintext, iv);

        encryptDecryptAesCbc(256, encryptParameters);
    }

    @Test
    public void encryptDecryptAes128CbcPadLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA128CbcPadParameters(plaintext, iv);

        encryptDecryptAesCbc(128, encryptParameters);
    }

    @Test
    public void encryptDecryptAes192CbcPadLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA192CbcPadParameters(plaintext, iv);

        encryptDecryptAesCbc(192, encryptParameters);
    }

    @Test
    public void encryptDecryptAes256CbcPadLocal() throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        EncryptParameters encryptParameters = EncryptParameters.createA256CbcPadParameters(plaintext, iv);

        encryptDecryptAesCbc(256, encryptParameters);
    }
}
