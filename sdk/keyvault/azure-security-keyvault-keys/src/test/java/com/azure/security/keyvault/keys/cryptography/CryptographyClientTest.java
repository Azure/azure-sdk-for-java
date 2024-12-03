// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
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

import static com.azure.security.keyvault.keys.TestUtils.buildSyncAssertingClient;
import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CryptographyClientTest extends CryptographyClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientTest.class);

    private KeyClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void initializeKeyClient(HttpClient httpClient) {
        client = getKeyClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), getEndpoint(),
            null)
            .buildClient();
    }

    CryptographyClient initializeCryptographyClient(String keyId, HttpClient httpClient,
                                                    CryptographyServiceVersion serviceVersion) {
        return getCryptographyClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), serviceVersion)
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
            CryptographyClient cryptoClient =
                initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);

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
    }

    @Test
    public void encryptDecryptRsaLocal() throws Exception {
        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair, Arrays.asList(KeyOperation.ENCRYPT, KeyOperation.DECRYPT));
            CryptographyClient cryptoClient = initializeCryptographyClient(key);
            List<EncryptionAlgorithm> algorithms =
                Arrays.asList(EncryptionAlgorithm.RSA1_5, EncryptionAlgorithm.RSA_OAEP);

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
            CryptographyClient cryptoClient =
                initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            List<KeyWrapAlgorithm> algorithms = Arrays.asList(KeyWrapAlgorithm.RSA1_5, KeyWrapAlgorithm.RSA_OAEP,
                KeyWrapAlgorithm.RSA_OAEP_256);

            for (KeyWrapAlgorithm algorithm : algorithms) {
                // Test variables
                byte[] plaintext = new byte[100];

                new Random(0x1234567L).nextBytes(plaintext);

                byte[] encryptedKey = cryptoClient.wrapKey(algorithm, plaintext).getEncryptedKey();
                byte[] decryptedKey = cryptoClient.unwrapKey(algorithm, encryptedKey).getKey();

                assertArrayEquals(decryptedKey, plaintext);

                encryptedKey = cryptoClient.wrapKey(algorithm, plaintext).getEncryptedKey();
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
    public void signVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) {
        initializeKeyClient(httpClient);

        signVerifyEcRunner(signVerifyEcData -> {
            KeyCurveName curve = signVerifyEcData.getCurve();
            Map<KeyCurveName, SignatureAlgorithm> curveToSignature = signVerifyEcData.getCurveToSignature();
            Map<KeyCurveName, String> messageDigestAlgorithm = signVerifyEcData.getMessageDigestAlgorithm();
            String keyName = testResourceNamer.randomName("testEcKey" + curve.toString(), 20);
            CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions(keyName)
                .setKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
                .setCurveName(curve);
            KeyVaultKey keyVaultKey = client.createEcKey(createEcKeyOptions);
            CryptographyClient cryptographyClient =
                initializeCryptographyClient(keyVaultKey.getId(), httpClient, serviceVersion);

            try {
                byte[] data = new byte[100];

                new Random(0x1234567L).nextBytes(data);

                MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm.get(curve));

                md.update(data);

                byte[] digest = md.digest();

                SignResult signResult = cryptographyClient.sign(curveToSignature.get(curve), digest);

                Boolean verifyStatus =
                    cryptographyClient.verify(curveToSignature.get(curve), digest, signResult.getSignature()).isValid();

                assertTrue(verifyStatus);
            } catch (NoSuchAlgorithmException e) {
                fail(e);
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signDataVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) {
        initializeKeyClient(httpClient);

        signVerifyEcRunner(signVerifyEcData -> {
            KeyCurveName curve = signVerifyEcData.getCurve();
            Map<KeyCurveName, SignatureAlgorithm> curveToSignature = signVerifyEcData.getCurveToSignature();
            String keyName = testResourceNamer.randomName("testEcKey" + curve.toString(), 20);
            CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions(keyName)
                .setKeyOperations(KeyOperation.SIGN, KeyOperation.VERIFY)
                .setCurveName(curve);
            KeyVaultKey keyVaultKey = client.createEcKey(createEcKeyOptions);
            CryptographyClient cryptographyClient =
                initializeCryptographyClient(keyVaultKey.getId(), httpClient, serviceVersion);

            byte[] plaintext = new byte[100];

            new Random(0x1234567L).nextBytes(plaintext);

            byte[] signature = cryptographyClient.signData(curveToSignature.get(curve), plaintext).getSignature();

            Boolean verifyStatus =
                cryptographyClient.verifyData(curveToSignature.get(curve), plaintext, signature).isValid();

            assertTrue(verifyStatus);
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
            CryptographyClient cryptoClient =
                initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            List<SignatureAlgorithm> algorithms =
                Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            Map<SignatureAlgorithm, String> messageDigestAlgorithm = new HashMap<>();

            messageDigestAlgorithm.put(SignatureAlgorithm.RS256, "SHA-256");
            messageDigestAlgorithm.put(SignatureAlgorithm.RS384, "SHA-384");
            messageDigestAlgorithm.put(SignatureAlgorithm.RS512, "SHA-512");

            for (SignatureAlgorithm algorithm : algorithms) {
                try {
                    byte[] data = new byte[100];

                    new Random(0x1234567L).nextBytes(data);

                    MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm.get(algorithm));

                    md.update(data);

                    byte[] digest = md.digest();

                    SignResult signResult = cryptoClient.sign(algorithm, digest);
                    Boolean verifyStatus = cryptoClient.verify(algorithm, digest, signResult.getSignature()).isValid();

                    assertTrue(verifyStatus);
                } catch (NoSuchAlgorithmException e) {
                    fail(e);
                }
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signDataVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        initializeKeyClient(httpClient);

        encryptDecryptRsaRunner(keyPair -> {
            JsonWebKey key = JsonWebKey.fromRsa(keyPair);
            String keyName = testResourceNamer.randomName("testRsaKeySignVerify", 25);
            KeyVaultKey importedKey = client.importKey(keyName, key);
            CryptographyClient cryptoClient =
                initializeCryptographyClient(importedKey.getId(), httpClient, serviceVersion);
            List<SignatureAlgorithm> algorithms =
                Arrays.asList(SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512);

            for (SignatureAlgorithm algorithm : algorithms) {
                byte[] plaintext = new byte[100];

                new Random(0x1234567L).nextBytes(plaintext);

                byte[] signature = cryptoClient.signData(algorithm, plaintext).getSignature();
                Boolean verifyStatus = cryptoClient.verifyData(algorithm, plaintext, signature).isValid();

                assertTrue(verifyStatus);
            }
        });
    }

    @Test
    public void signDataVerifyEcLocal() {
        signVerifyEcRunner(signVerifyEcData -> {
            KeyPair keyPair;
            Provider provider = null;

            try {
                String algorithmName = "EC";
                Provider[] providers = Security.getProviders();

                for (Provider currentProvider : providers) {
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

                final KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName, provider);
                ECGenParameterSpec spec =
                    new ECGenParameterSpec(signVerifyEcData.getCurveToSpec().get(signVerifyEcData.getCurve()));

                generator.initialize(spec);

                keyPair = generator.generateKeyPair();
            } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
                // Could not generate a KeyPair from the given JsonWebKey.
                // It's likely this happened for key curve secp256k1, which is not supported on Java 16+.
                LOGGER.log(LogLevel.VERBOSE, () -> "Failed to generate key pair from JsonWebKey.", e);

                return;
            }

            JsonWebKey jsonWebKey =
                JsonWebKey.fromEc(keyPair, provider, Arrays.asList(KeyOperation.SIGN, KeyOperation.VERIFY));
            KeyCurveName curve = signVerifyEcData.getCurve();
            Map<KeyCurveName, SignatureAlgorithm> curveToSignature = signVerifyEcData.getCurveToSignature();
            CryptographyClient cryptographyClient = initializeCryptographyClient(jsonWebKey);

            byte[] plainText = new byte[100];

            new Random(0x1234567L).nextBytes(plainText);

            byte[] signature =
                cryptographyClient.signData(curveToSignature.get(curve), plainText).getSignature();
            Boolean verifyStatus =
                cryptographyClient.verifyData(curveToSignature.get(curve), plainText, signature).isValid();

            assertTrue(verifyStatus);
        });
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
