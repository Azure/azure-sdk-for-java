// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public abstract class CryptographyClientTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientTestBase.class);

    protected boolean isHsmEnabled = false;
    protected boolean runManagedHsmTest = false;

    private static final int MAX_RETRIES = 5;
    private static final RetryOptions LIVE_RETRY_OPTIONS = new RetryOptions(new ExponentialBackoffOptions()
        .setMaxRetries(MAX_RETRIES)
        .setBaseDelay(Duration.ofSeconds(2))
        .setMaxDelay(Duration.ofSeconds(16)));

    private static final RetryOptions PLAYBACK_RETRY_OPTIONS =
        new RetryOptions(new FixedDelayOptions(MAX_RETRIES, Duration.ofMillis(1)));

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    KeyClientBuilder getKeyClientBuilder(HttpClient httpClient, String endpoint, KeyServiceVersion serviceVersion) {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
        } else  {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyClientBuilder builder = new KeyClientBuilder()
            .vaultUrl(endpoint)
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
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
        } else {
            credential = new MockTokenCredential();

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
        }

        CryptographyClientBuilder builder = new CryptographyClientBuilder()
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

    static CryptographyClient initializeCryptographyClient(JsonWebKey key) {
        return new CryptographyClientBuilder()
            .jsonWebKey(key)
            .buildClient();
    }

    @Test
    public abstract void encryptDecryptRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception;

    @Test
    public abstract void encryptDecryptRsaLocal() throws Exception;

    void encryptDecryptRsaRunner(Consumer<KeyPair> testRunner) throws Exception {
        testRunner.accept(getWellKnownKey());
    }

    @Test
    public abstract void encryptDecryptAes128CbcLocal() throws Exception;

    @Test
    public abstract void encryptDecryptAes192CbcLocal() throws Exception;

    @Test
    public abstract void encryptDecryptAes256CbcLocal() throws Exception;

    @Test
    public abstract void encryptDecryptAes128CbcPadLocal() throws Exception;

    @Test
    public abstract void encryptDecryptAes192CbcPadLocal() throws Exception;

    @Test
    public abstract void encryptDecryptAes256CbcPadLocal() throws Exception;

    @Test
    public abstract void signVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    @Test
    public abstract void signDataVerifyEc(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    void signVerifyEcRunner(Consumer<SignVerifyEcData> testRunner) {
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

        Map<KeyCurveName, String> messageDigestAlgorithm = new HashMap<>();

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
                                Map<KeyCurveName, String> curveToSpec,
                                Map<KeyCurveName, String> messageDigestAlgorithm) {
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
    public abstract void wrapUnwrapRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception;

    @Test
    public abstract void wrapUnwrapRsaLocal() throws Exception;

    @Test
    public abstract void signVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception;

    @Test
    public abstract void signDataVerifyRsa(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception;

    private static KeyPair getWellKnownKey() throws Exception {
        BigInteger modulus = new BigInteger("27266783713040163753473734334021230592631652450892850648620119914958066181400432364213298181846462385257448168605902438305568194683691563208578540343969522651422088760509452879461613852042845039552547834002168737350264189810815735922734447830725099163869215360401162450008673869707774119785881115044406101346450911054819448375712432746968301739007624952483347278954755460152795801894283389540036131881712321193750961817346255102052653789197325341350920441746054233522546543768770643593655942246891652634114922277138937273034902434321431672058220631825053788262810480543541597284376261438324665363067125951152574540779");
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = new BigInteger("10466613941269075477152428927796086150095892102279802916937552172064636326433780566497000814207416485739683286961848843255766652023400959086290344987308562817062506476465756840999981989957456897020361717197805192876094362315496459535960304928171129585813477132331538577519084006595335055487028872410579127692209642938724850603554885478763205394868103298473476811627231543504190652483290944218004086457805431824328448422034887148115990501701345535825110962804471270499590234116100216841170344686381902328362376624405803648588830575558058257742073963036264273582756620469659464278207233345784355220317478103481872995809");
        BigInteger primeP = new BigInteger("175002941104568842715096339107566771592009112128184231961529953978142750732317724951747797764638217287618769007295505214923187971350518217670604044004381362495186864051394404165602744235299100790551775147322153206730562450301874236875459336154569893255570576967036237661594595803204808064127845257496057219227");
        BigInteger primeQ = new BigInteger("155807574095269324897144428622185380283967159190626345335083690114147315509962698765044950001909553861571493035240542031420213144237033208612132704562174772894369053916729901982420535940939821673277140180113593951522522222348910536202664252481405241042414183668723338300649954708432681241621374644926879028977");
        BigInteger primeExponentP = new BigInteger("79745606804504995938838168837578376593737280079895233277372027184693457251170125851946171360348440134236338520742068873132216695552312068793428432338173016914968041076503997528137698610601222912385953171485249299873377130717231063522112968474603281996190849604705284061306758152904594168593526874435238915345");
        BigInteger primeExponentQ = new BigInteger("80619964983821018303966686284189517841976445905569830731617605558094658227540855971763115484608005874540349730961777634427740786642996065386667564038755340092176159839025706183161615488856833433976243963682074011475658804676349317075370362785860401437192843468423594688700132964854367053490737073471709030801");
        BigInteger crtCoefficient = new BigInteger("2157818511040667226980891229484210846757728661751992467240662009652654684725325675037512595031058612950802328971801913498711880111052682274056041470625863586779333188842602381844572406517251106159327934511268610438516820278066686225397795046020275055545005189953702783748235257613991379770525910232674719428");
        KeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeySpec privateKeySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
    }

    static void encryptDecryptAesCbc(int keySize, EncryptParameters encryptParameters) throws NoSuchAlgorithmException {
        byte[] plaintext = "My16BitPlaintext".getBytes();
        byte[] iv = "My16BytesTestIv.".getBytes();
        CryptographyClient cryptographyClient = initializeCryptographyClient(getTestJsonWebKey(keySize));
        EncryptResult encryptResult = cryptographyClient.encrypt(encryptParameters, Context.NONE);
        EncryptionAlgorithm algorithm = encryptParameters.getAlgorithm();
        DecryptParameters decryptParameters = null;

        if (algorithm == EncryptionAlgorithm.A128CBC) {
            decryptParameters = DecryptParameters.createA128CbcParameters(encryptResult.getCipherText(), iv);
        } else if (algorithm == EncryptionAlgorithm.A192CBC) {
            decryptParameters = DecryptParameters.createA192CbcParameters(encryptResult.getCipherText(), iv);
        } else if (algorithm == EncryptionAlgorithm.A256CBC) {
            decryptParameters = DecryptParameters.createA256CbcParameters(encryptResult.getCipherText(), iv);
        } else if (algorithm == EncryptionAlgorithm.A128CBCPAD) {
            decryptParameters = DecryptParameters.createA128CbcPadParameters(encryptResult.getCipherText(), iv);
        } else if (algorithm == EncryptionAlgorithm.A192CBCPAD) {
            decryptParameters = DecryptParameters.createA192CbcPadParameters(encryptResult.getCipherText(), iv);
        } else if (algorithm == EncryptionAlgorithm.A256CBCPAD) {
            decryptParameters = DecryptParameters.createA256CbcPadParameters(encryptResult.getCipherText(), iv);
        }

        DecryptResult decryptResult = cryptographyClient.decrypt(decryptParameters, Context.NONE);

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
