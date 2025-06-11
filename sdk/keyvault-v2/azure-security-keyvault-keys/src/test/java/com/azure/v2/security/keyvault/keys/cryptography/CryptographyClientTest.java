// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.util.logging.ClientLogger;
import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.KeyServiceVersion;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyCurveName;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.azure.v2.security.keyvault.keys.TestUtils.buildSyncAssertingClient;
import static com.azure.v2.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CryptographyClientTest extends CryptographyClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientTest.class);

    private KeyClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }    private void initializeKeyClient(HttpClient httpClient) {
        client = getKeyClientBuilder(
            buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            getEndpoint(), KeyServiceVersion.getLatest()).buildClient();
    }

    CryptographyClient initializeCryptographyClient(String keyId, HttpClient httpClient,
        CryptographyServiceVersion serviceVersion) {
        return getCryptographyClientBuilder(
            buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            serviceVersion).keyIdentifier(keyId).buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.v2.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void encryptDecryptRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);

        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKey", 20);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient
                = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5,
                EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];

                new Random(0x1234567L).nextBytes(plaintext);

                byte[] ciphertext = cryptoClient.encrypt(algorithm, plaintext).getCipherText();
                byte[] decryptedText = cryptoClient.decrypt(algorithm, ciphertext).getPlainText();

                assertArrayEquals(decryptedText, plaintext);

                ciphertext = cryptoClient.encrypt(algorithm, plaintext).getCipherText();
                decryptedText = cryptoClient.decrypt(algorithm, ciphertext).getPlainText();

                assertArrayEquals(decryptedText, plaintext);
            }
        });
    }    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.v2.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);

        signVerifyEcRunner(signVerifyEcData -> {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
                ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(signVerifyEcData.getCurveToSpec().get(signVerifyEcData.getCurve()));
                keyPairGenerator.initialize(ecGenParameterSpec);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                JsonWebKey key = JsonWebKey.fromEc(keyPair, Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
                String keyName = testResourceNamer.randomName("testEcKey", 20);
                KeyVaultKey importedKey = client.importKey(keyName, key);
                CryptographyClient cryptoClient
                    = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);

                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] digest = MessageDigest.getInstance(signVerifyEcData.getMessageDigestAlgorithm().get(signVerifyEcData.getCurve())).digest(plaintext);

                SignatureAlgorithm algorithm = signVerifyEcData.getCurveToSignature().get(signVerifyEcData.getCurve());
                SignResult signResult = cryptoClient.sign(algorithm, digest);
                assertTrue(cryptoClient.verify(algorithm, digest, signResult.getSignature()).isValid());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.v2.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void wrapUnwrapRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);

        wrapUnwrapRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKey", 20);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient
                = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP,
                KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);

                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plaintext).getEncryptedKey();
                byte[] decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plaintext);
            }
        });
    }    @Test
    public void encryptDecryptRsaLocal() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.RSA1_5,
                EncryptionAlgorithm.RSA_OAEP, EncryptionAlgorithm.RSA_OAEP_256);

            for (EncryptionAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);

                byte[] ciphertext = cryptoClient.encrypt(algorithm, plaintext).getCipherText();
                byte[] decryptedText = cryptoClient.decrypt(algorithm, ciphertext).getPlainText();

                assertArrayEquals(decryptedText, plaintext);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.v2.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void encryptDecryptAes(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        // AES operations are typically local-only in Key Vault
        encryptDecryptAesLocal();
    }

    @Test
    public void encryptDecryptAesLocal() throws Exception {
        encryptDecryptAesRunner(key -> {
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<EncryptionAlgorithm> algorithms = Arrays.asList(EncryptionAlgorithm.A128CBC,
                EncryptionAlgorithm.A192CBC, EncryptionAlgorithm.A256CBC);

            for (EncryptionAlgorithm algorithm : algorithms) {
                encryptDecryptAesWithParametersRunner(k -> {}, algorithm);
            }
        });
    }

    @Test
    public void signDataVerifyEcLocal() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        signVerifyEcRunner(signVerifyEcData -> {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
                ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(signVerifyEcData.getCurveToSpec().get(signVerifyEcData.getCurve()));
                keyPairGenerator.initialize(ecGenParameterSpec);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                JsonWebKey key = JsonWebKey.fromEc(keyPair, Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
                CryptographyClient cryptoClient = initializeCryptographyClient(key);

                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] digest = MessageDigest.getInstance(signVerifyEcData.getMessageDigestAlgorithm().get(signVerifyEcData.getCurve())).digest(plaintext);

                SignatureAlgorithm algorithm = signVerifyEcData.getCurveToSignature().get(signVerifyEcData.getCurve());
                SignResult signResult = cryptoClient.sign(algorithm, digest);
                assertTrue(cryptoClient.verify(algorithm, digest, signResult.getSignature()).isValid());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void wrapUnwrapRsaLocal() throws Exception {
        wrapUnwrapRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP,
                KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);

                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plaintext).getEncryptedKey();
                byte[] decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plaintext);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.v2.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);

        signVerifyRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKey", 20);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient
                = initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.PS256, SignatureAlgorithm.RS256);

            for (SignatureAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(plaintext);

                SignResult signResult = cryptoClient.sign(algorithm, digest);
                assertTrue(cryptoClient.verify(algorithm, digest, signResult.getSignature()).isValid());
            }
        });
    }

    @Test
    public void signVerifyRsaLocal() throws Exception {
        signVerifyRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            CryptographyClient cryptoClient = initializeCryptographyClient(key);

            List<SignatureAlgorithm> algorithms = Arrays.asList(SignatureAlgorithm.PS256, SignatureAlgorithm.RS256);

            for (SignatureAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];
                new Random(0x1234567L).nextBytes(plaintext);
                byte[] digest = MessageDigest.getInstance("SHA-256").digest(plaintext);

                SignResult signResult = cryptoClient.sign(algorithm, digest);
                assertTrue(cryptoClient.verify(algorithm, digest, signResult.getSignature()).isValid());
            }
        });
    }

    @Test
    public void encryptDecryptWithPremadeKey() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("testTag", "testValue");

        CreateEcKeyOptions ecKeyOptions = new CreateEcKeyOptions(testResourceNamer.randomName("testEcKey", 20))
            .setCurveName(KeyCurveName.P_256)
            .setKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
            .setTags(tags);

        KeyVaultKey ecKey = client.createEcKey(ecKeyOptions);
        CryptographyClient cryptoClient = initializeCryptographyClient(ecKey.getId(), getHttpClients().get(0),
            CryptographyServiceVersion.getLatest());

        byte[] plaintext = new byte[100];
        new Random(0x1234567L).nextBytes(plaintext);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(plaintext);

        SignResult signResult = cryptoClient.sign(SignatureAlgorithm.ES256, digest);
        assertTrue(cryptoClient.verify(SignatureAlgorithm.ES256, digest, signResult.getSignature()).isValid());
    }

    @Test
    public void encryptParametersRequiredParameter() {
        assertEncryptParametersException(null, new byte[16]);
        assertEncryptParametersException(EncryptionAlgorithm.A128_CBC, null);
    }

    private void assertEncryptParametersException(EncryptionAlgorithm encryptionAlgorithm, byte[] plaintext) {
        try {
            new EncryptParameters(encryptionAlgorithm, plaintext);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // Expected
        }
    }
}
