// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;

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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptographyClientTest extends CryptographyClientTestBase {

    private KeyClient client;
    private HttpPipeline pipeline;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void initializeKeyClient(HttpClient httpClient) {
        pipeline = getHttpPipeline(httpClient, KeyServiceVersion.getLatest());
        client = new KeyClientBuilder()
                     .pipeline(pipeline)
                     .vaultUrl(getEndpoint())
                     .buildClient();
    }

    private CryptographyClient initializeCryptographyClient(String keyId, HttpClient httpClient, CryptographyServiceVersion serviceVersion) {
        pipeline = getHttpPipeline(httpClient, serviceVersion);
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
            String keyName = generateResourceId("testRsaKey");
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            CryptographyServiceClient serviceClient = cryptoClient.getServiceClient();

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] ciphertext = cryptoClient.encrypt(algorithm, plaintext).getCipherText();
                byte[] decryptedText = serviceClient.decrypt(new DecryptParameters(algorithm, ciphertext, null, null,
                    null), Context.NONE).block().getPlainText();

                assertArrayEquals(decryptedText, plaintext);

                ciphertext = serviceClient.encrypt(new EncryptParameters(algorithm, plaintext, null, null), Context.NONE)
                    .block().getCipherText();
                decryptedText = cryptoClient.decrypt(algorithm, ciphertext).getPlainText();

                assertArrayEquals(decryptedText, plaintext);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void wrapUnwraptRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = generateResourceId("testRsaKeyWrapUnwrap");
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


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = generateResourceId("testRsaKeySignVerify");
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
        initializeKeyClient(httpClient);
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

            JsonWebKey key = JsonWebKey.fromEc(keyPair, provider);
            String keyName = generateResourceId("testEcKey" + crv.toString());
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
        }

    }
}
