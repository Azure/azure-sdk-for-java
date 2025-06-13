// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import io.clientcore.core.credential.TokenCredential;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.policy.ExponentialBackoffOptions;
import io.clientcore.core.http.policy.FixedDelayOptions;
import io.clientcore.core.http.policy.HttpRetryOptions;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.BodilessMatcher;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestRequestMatcher;
import com.azure.v2.core.test.utils.MockTokenCredential;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.CoreUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.keys.KeyClientBuilder;
import com.azure.v2.security.keyvault.keys.KeyServiceVersion;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyCurveName;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public abstract class CryptographyClientTestBase extends TestBase {
    private static final String AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_KEYVAULT_TEST_KEYS_SERVICE_VERSIONS);

    private static final int MAX_RETRIES = 5;
    private static final HttpRetryOptions LIVE_RETRY_OPTIONS
        = new HttpRetryOptions(new ExponentialBackoffOptions().setMaxRetries(MAX_RETRIES)
            .setBaseDelay(Duration.ofSeconds(2))
            .setMaxDelay(Duration.ofSeconds(16)));

    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientTestBase.class);

    private static final HttpRetryOptions PLAYBACK_RETRY_OPTIONS
        = new HttpRetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    KeyClientBuilder getKeyClientBuilder(HttpClient httpClient, String endpoint,
        KeyServiceVersion serviceVersion) {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        } else {
            credential = new MockTokenCredential();

            List<TestRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyClientBuilder builder = new KeyClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(serviceVersion)
            .credential(credential)
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    CryptographyClientBuilder getCryptographyClientBuilder(HttpClient httpClient,
        CryptographyServiceVersion serviceVersion) {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        } else {
            credential = new MockTokenCredential();

            List<TestRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        CryptographyClientBuilder builder = new CryptographyClientBuilder().serviceVersion(serviceVersion)
            .credential(credential)
            .httpClient(httpClient);

        if (interceptorManager.isPlaybackMode()) {
            return builder.retryOptions(PLAYBACK_RETRY_OPTIONS);
        } else {
            builder.retryOptions(LIVE_RETRY_OPTIONS);

            return interceptorManager.isRecordMode()
                ? builder.addPolicy(interceptorManager.getRecordPolicy())
                : builder;
        }
    }

    static CryptographyClient initializeCryptographyClient(JsonWebKey key) {
        return new CryptographyClientBuilder().jsonWebKey(key).buildClient();
    }

    @Test
    public abstract void encryptDecryptRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion)
        throws Exception;

    @Test
    public abstract void encryptDecryptRsaLocal() throws Exception;

    void encryptDecryptRsaRunner(Consumer<KeyPair> testRunner) throws Exception {
        testRunner.accept(getWellKnownKey());
    }

    @Test
    public abstract void encryptDecryptAes(HttpClient httpClient, CryptographyServiceVersion serviceVersion)
        throws Exception;

    @Test
    public abstract void encryptDecryptAesLocal() throws Exception;

    void encryptDecryptAesRunner(Consumer<JsonWebKey> testRunner) throws Exception {
        testRunner.accept(getTestJsonWebKey(256));
        testRunner.accept(getTestJsonWebKey(128));
    }

    @Test
    public abstract void signVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion)
        throws Exception;

    void signVerifyEcRunner(Consumer<SignVerifyEcData> testRunner) throws Exception {
        Map<KeyCurveName, SignatureAlgorithm> curveToSignature = new HashMap<>();
        Map<KeyCurveName, String> curveToSpec = new HashMap<>();
        Map<KeyCurveName, String> messageDigestAlgorithm = new HashMap<>();

        curveToSignature.put(KeyCurveName.P_256, SignatureAlgorithm.ES256);
        curveToSignature.put(KeyCurveName.P_384, SignatureAlgorithm.ES384);
        curveToSignature.put(KeyCurveName.P_521, SignatureAlgorithm.ES512);
        curveToSignature.put(KeyCurveName.P_256K, SignatureAlgorithm.ES256K);

        curveToSpec.put(KeyCurveName.P_256, "secp256r1");
        curveToSpec.put(KeyCurveName.P_384, "secp384r1");
        curveToSpec.put(KeyCurveName.P_521, "secp521r1");
        curveToSpec.put(KeyCurveName.P_256K, "secp256k1");

        messageDigestAlgorithm.put(KeyCurveName.P_256, "SHA-256");
        messageDigestAlgorithm.put(KeyCurveName.P_384, "SHA-384");
        messageDigestAlgorithm.put(KeyCurveName.P_521, "SHA-512");
        messageDigestAlgorithm.put(KeyCurveName.P_256K, "SHA-256");

        List<KeyCurveName> curveList = new ArrayList<>();

        curveList.add(KeyCurveName.P_256);
        curveList.add(KeyCurveName.P_384);
        curveList.add(KeyCurveName.P_521);
        curveList.add(KeyCurveName.P_256K);

        for (KeyCurveName curve : curveList) {
            testRunner.accept(new SignVerifyEcData(curve, curveToSignature, curveToSpec, messageDigestAlgorithm));
        }
    }

    protected static class SignVerifyEcData {
        private final KeyCurveName curve;
        private final Map<KeyCurveName, SignatureAlgorithm> curveToSignature;
        private final Map<KeyCurveName, String> curveToSpec;
        private final Map<KeyCurveName, String> messageDigestAlgorithm;

        public SignVerifyEcData(KeyCurveName curve, Map<KeyCurveName, SignatureAlgorithm> curveToSignature,
            Map<KeyCurveName, String> curveToSpec, Map<KeyCurveName, String> messageDigestAlgorithm) {
            this.curve = curve;
            this.curveToSignature = curveToSignature;
            this.curveToSpec = curveToSpec;
            this.messageDigestAlgorithm = messageDigestAlgorithm;
        }

        public KeyCurveName getCurve() {
            return curve;
        }

        public Map<KeyCurveName, SignatureAlgorithm> getCurveToSignature() {
            return curveToSignature;
        }

        public Map<KeyCurveName, String> getCurveToSpec() {
            return curveToSpec;
        }

        public Map<KeyCurveName, String> getMessageDigestAlgorithm() {
            return messageDigestAlgorithm;
        }
    }

    @Test
    public abstract void signDataVerifyEcLocal() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    @Test
    public abstract void wrapUnwrapRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion)
        throws Exception;

    @Test
    public abstract void wrapUnwrapRsaLocal() throws Exception;

    void wrapUnwrapRsaRunner(Consumer<KeyPair> testRunner) throws Exception {
        testRunner.accept(getWellKnownKey());
    }

    @Test
    public abstract void signVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion)
        throws Exception;

    @Test
    public abstract void signVerifyRsaLocal() throws Exception;

    void signVerifyRsaRunner(Consumer<KeyPair> testRunner) throws Exception {
        testRunner.accept(getWellKnownKey());
    }

    static KeyPair getWellKnownKey() {
        return getWellKnownKey(2048);
    }

    static KeyPair getWellKnownKey(int keySize) {
        // well-known key helps with test stability when running against actual service
        // known good key means that tests won't fail because of flakey key generation
        // taken from https://tools.ietf.org/html/rfc3447#appendix-A.1.1
        return TestHelper.getWellKnownKey(keySize);
    }

    void encryptDecryptAesWithParametersRunner(Consumer<JsonWebKey> testRunner, EncryptionAlgorithm algorithm)
        throws Exception {
        JsonWebKey key = getTestJsonWebKey(256);

        CryptographyClient cryptographyClient = initializeCryptographyClient(key);

        byte[] plaintext = "Hello world".getBytes();
        byte[] iv = "1234567890123456".getBytes();
        byte[] aad = "Authenticate me".getBytes();

        EncryptParameters encryptParameters = null;

        if (algorithm == EncryptionAlgorithm.A128CBC) {
            encryptParameters = EncryptParameters.createA128CbcParameters(plaintext, iv);
        } else if (algorithm == EncryptionAlgorithm.A192CBC) {
            encryptParameters = EncryptParameters.createA192CbcParameters(plaintext, iv);
        } else if (algorithm == EncryptionAlgorithm.A256CBC) {
            encryptParameters = EncryptParameters.createA256CbcParameters(plaintext, iv);
        } else if (algorithm == EncryptionAlgorithm.A128CBCPAD) {
            encryptParameters = EncryptParameters.createA128CbcPadParameters(plaintext, iv);
        } else if (algorithm == EncryptionAlgorithm.A192CBCPAD) {
            encryptParameters = EncryptParameters.createA192CbcPadParameters(plaintext, iv);
        } else if (algorithm == EncryptionAlgorithm.A256CBCPAD) {
            encryptParameters = EncryptParameters.createA256CbcPadParameters(plaintext, iv);
        }

        EncryptResult encryptResult = cryptographyClient.encrypt(encryptParameters);
        EncryptionAlgorithm resultAlgorithm = encryptParameters.getAlgorithm();
        DecryptParameters decryptParameters = null;

        if (resultAlgorithm == EncryptionAlgorithm.A128CBC) {
            decryptParameters = DecryptParameters.createA128CbcParameters(encryptResult.getCipherText(), iv);
        } else if (resultAlgorithm == EncryptionAlgorithm.A192CBC) {
            decryptParameters = DecryptParameters.createA192CbcParameters(encryptResult.getCipherText(), iv);
        } else if (resultAlgorithm == EncryptionAlgorithm.A256CBC) {
            decryptParameters = DecryptParameters.createA256CbcParameters(encryptResult.getCipherText(), iv);
        } else if (resultAlgorithm == EncryptionAlgorithm.A128CBCPAD) {
            decryptParameters = DecryptParameters.createA128CbcPadParameters(encryptResult.getCipherText(), iv);
        } else if (resultAlgorithm == EncryptionAlgorithm.A192CBCPAD) {
            decryptParameters = DecryptParameters.createA192CbcPadParameters(encryptResult.getCipherText(), iv);
        } else if (resultAlgorithm == EncryptionAlgorithm.A256CBCPAD) {
            decryptParameters = DecryptParameters.createA256CbcPadParameters(encryptResult.getCipherText(), iv);
        }

        DecryptResult decryptResult = cryptographyClient.decrypt(decryptParameters);

        assertArrayEquals(plaintext, decryptResult.getPlainText());
    }

    private static JsonWebKey getTestJsonWebKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        keyGen.init(keySize);

        SecretKey secretKey = keyGen.generateKey();

        List<KeyOperation> keyOperations = new ArrayList<>();
        keyOperations.add(KeyOperation.ENCRYPT);
        keyOperations.add(KeyOperation.DECRYPT);

        return JsonWebKey.fromAes(secretKey, keyOperations).setId("testKey");
    }

    public String getEndpoint() {
        final String endpoint = runManagedHsmTest
            ? Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT", "https://localhost:8080")
            : Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    public void sleep(long millis) {
        sleepIfRunningAgainstService(millis);
    }
}
