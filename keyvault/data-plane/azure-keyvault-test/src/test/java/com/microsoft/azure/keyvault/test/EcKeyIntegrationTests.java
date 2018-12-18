package com.microsoft.azure.keyvault.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.azure.keyvault.cryptography.EcKey;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.KeyVerifyResult;
import com.microsoft.azure.keyvault.requests.ImportKeyRequest;
import com.microsoft.azure.keyvault.webkey.*;
import com.microsoft.azure.management.resources.core.InterceptorManager;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.interceptors.LoggingInterceptor;
public class EcKeyIntegrationTests {

    private static TestBase.TestMode testMode = null;

    protected InterceptorManager interceptorManager = null;

    protected final static String ZERO_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected final static String ZERO_TENANT = "00000000-0000-0000-0000-000000000000";
    private static final String PLAYBACK_URI_BASE = "http://localhost:";
    private static final String PLAYBACK_VAULT = "https://test-vault.vault.azure.net";
        
    protected static String playbackUri = null;

    static KeyVaultClient keyVaultClient;
    
    static String VAULT_URI;
    

    @Rule
    public TestName testName = new TestName();

 
    
    @BeforeClass
    public static void setUp() throws Exception {
        initTestMode();
        initPlaybackUri();
    }

    @Before
    public void beforeMethod() throws Exception {

        RestClient restClient;
        ServiceClientCredentials credentials = createTestCredentials();
        interceptorManager = InterceptorManager.create(testName.getMethodName(), testMode);

        if (isRecordMode()) {
            VAULT_URI = System.getenv("VAULT_URI");
            restClient = new RestClient.Builder().withBaseUrl("https://{vaultBaseUrl}")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()).build();

            interceptorManager.addTextReplacementRule("https://management.azure.com/", playbackUri + "/");
            interceptorManager.addTextReplacementRule("https://graph.windows.net/", playbackUri + "/");
            interceptorManager.addTextReplacementRule(VAULT_URI, PLAYBACK_VAULT);
            keyVaultClient = new KeyVaultClient(restClient);
        } else { // is Playback Mode
            VAULT_URI = PLAYBACK_VAULT;
            restClient = new RestClient.Builder().withBaseUrl(playbackUri + "/")
                    .withSerializerAdapter(new AzureJacksonAdapter())
                    .withResponseBuilderFactory(new AzureResponseBuilder.Factory()).withCredentials(credentials)
                    .withLogLevel(LogLevel.NONE)
                    .withNetworkInterceptor(new LoggingInterceptor(LogLevel.BODY_AND_HEADERS))
                    .withNetworkInterceptor(interceptorManager.initInterceptor())
                    .withInterceptor(new ResourceManagerThrottlingInterceptor()).build();
            keyVaultClient = new KeyVaultClient(restClient);
        }
    }
    
    @Test
    public void testSignVerifyIntegrationES256() throws Exception {
        validateSignVerifyInterop(importTestKey("itwkk-p256", P256TestKey()), JsonWebKeySignatureAlgorithm.ES256, "SHA-256");
    }

    @Test
    public void testSignVerifyIntegrationES256K() throws Exception {
        validateSignVerifyInterop(importTestKey("itwkk-p256k", P256KTestKey()), JsonWebKeySignatureAlgorithm.ES256K, "SHA-256");
    }

    @Test
    public void testSignVerifyIntegrationES384() throws Exception {
        validateSignVerifyInterop(importTestKey("itwkk-p384", P384TestKey()), JsonWebKeySignatureAlgorithm.ES384, "SHA-384");
    }

    @Test
    public void testSignVerifyIntegrationES521() throws Exception {
        validateSignVerifyInterop(importTestKey("itwkk-p521", P521TestKey()), JsonWebKeySignatureAlgorithm.ES512, "SHA-512");
    }

    private void validateSignVerifyInterop(JsonWebKey jwk, JsonWebKeySignatureAlgorithm algorithm, String digestAlg)
            throws Exception {

        EcKey key = EcKey.fromJsonWebKey(jwk, true);

        KeyIdentifier keyId = new KeyIdentifier(jwk.kid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        MessageDigest md = MessageDigest.getInstance(digestAlg);
        md.update(plainText);
        byte[] digest = md.digest();

        // sign with both the client and the service
        byte[] clientSig = key.signAsync(digest, algorithm.toString()).get().getLeft();
        byte[] serverSig = keyVaultClient.sign(jwk.kid(), algorithm, digest).result();
        
        // verify client signature with service and vice versa
        Assert.assertTrue(keyVaultClient.verify(jwk.kid(), algorithm, digest, clientSig).value());
        Assert.assertTrue(key.verifyAsync(digest, serverSig, algorithm.toString()).get());

        key.close();
    }

    // crv   P_256
    // x     11232949079473245496693243696083285102762129989847161609854555188949850883563
    // y     1879583613806065892642092774705384015240844626261169536236224087556053896803
    // d     110376418358044062637363537183067346723507769076789115121629366563620220951085
    private static JsonWebKey P256TestKey()
    {
        return new JsonWebKey()
                    .withKty(JsonWebKeyType.EC)
                    .withKeyOps(Arrays.asList(JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY))
                    .withCrv(JsonWebKeyCurveName.P_256)
                    .withX(new BigInteger("11232949079473245496693243696083285102762129989847161609854555188949850883563").toByteArray())
                    .withY(new BigInteger("1879583613806065892642092774705384015240844626261169536236224087556053896803").toByteArray())
                    .withD(new BigInteger("110376418358044062637363537183067346723507769076789115121629366563620220951085").toByteArray());               
    } 


    // crv   P_256K
    // x     112542251246878300879834909875895196605604676102246979012590954738722135052808
    // y     6774601013471644037178985795211162469224640637200491504041212042624768103421
    // d     5788737892080795185076661111780678315827024120706807264074833863296072596641    
    private static JsonWebKey P256KTestKey()
    {
        return new JsonWebKey()
                    .withKty(JsonWebKeyType.EC)
                    .withKeyOps(Arrays.asList(JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY))
                    .withCrv(JsonWebKeyCurveName.P_256K)
                    .withX(new BigInteger("112542251246878300879834909875895196605604676102246979012590954738722135052808").toByteArray())
                    .withY(new BigInteger("6774601013471644037178985795211162469224640637200491504041212042624768103421").toByteArray())
                    .withD(new BigInteger("5788737892080795185076661111780678315827024120706807264074833863296072596641").toByteArray());               
    } 

    // crv   P_384
    // x     25940251081638606466066580153999823282664621938556856505612612711663486152226861175055792115101185005519603532468591
    // y     38849021239011943917620782277253508239698260816711858953045039688987325246933521190178660888358757011735327467604293
    // d     32295109630567236352165497564914579106522760535338683398753720328854294758072198979189259927479998588892483377447907
    private static JsonWebKey P384TestKey()
    {
        return new JsonWebKey()
                    .withKty(JsonWebKeyType.EC)
                    .withKeyOps(Arrays.asList(JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY))
                    .withCrv(JsonWebKeyCurveName.P_384)
                    .withX(new BigInteger("25940251081638606466066580153999823282664621938556856505612612711663486152226861175055792115101185005519603532468591").toByteArray())
                    .withY(new BigInteger("38849021239011943917620782277253508239698260816711858953045039688987325246933521190178660888358757011735327467604293").toByteArray())
                    .withD(new BigInteger("32295109630567236352165497564914579106522760535338683398753720328854294758072198979189259927479998588892483377447907").toByteArray());               
    } 

    // crv   P_521
    // x     6855414495738791694053590132729898471597826721317714885490415738464754554924249115378421758975070989210614663357146557161470466328735789754640064414018012235
    // y     3677272094599002495753508473603911533283562539125734660410262665439216117639982407670262587277222630266240230828668340712916997947964051679058455330395658230
    // d     1119526436113918255892609748222452225184162390267181240143092765692579316239102968513115940220551308699050504250027618944913182129917648549423125636042752861
    private static JsonWebKey P521TestKey()
    {
        return new JsonWebKey()
                    .withKty(JsonWebKeyType.EC)
                    .withKeyOps(Arrays.asList(JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY))
                    .withCrv(JsonWebKeyCurveName.P_521)
                    .withX(new BigInteger("6855414495738791694053590132729898471597826721317714885490415738464754554924249115378421758975070989210614663357146557161470466328735789754640064414018012235").toByteArray())
                    .withY(new BigInteger("3677272094599002495753508473603911533283562539125734660410262665439216117639982407670262587277222630266240230828668340712916997947964051679058455330395658230").toByteArray())
                    .withD(new BigInteger("1119526436113918255892609748222452225184162390267181240143092765692579316239102968513115940220551308699050504250027618944913182129917648549423125636042752861").toByteArray());               
    } 

    private static JsonWebKey importTestKey(String keyName, JsonWebKey jwk) throws Exception {

        KeyBundle keyBundle = keyVaultClient.importKey(VAULT_URI, keyName, jwk);

        return jwk.withKid(keyBundle.key().kid());
    }

    private static AuthenticationResult getAccessToken(String authorization, String resource) throws Exception {

        String clientId = System.getenv("arm.clientid");

        if (clientId == null) {
            throw new Exception("Please inform arm.clientid in the environment settings.");
        }

        String clientKey = System.getenv("arm.clientkey");
        String username = System.getenv("arm.username");
        String password = System.getenv("arm.password");

        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            AuthenticationContext context = new AuthenticationContext(authorization, false, service);

            Future<AuthenticationResult> future = null;

            if (clientKey != null && password == null) {
                ClientCredential credentials = new ClientCredential(clientId, clientKey);
                future = context.acquireToken(resource, credentials, null);
            }

            if (password != null && clientKey == null) {
                future = context.acquireToken(resource, clientId, username, password, null);
            }

            if (future == null) {
                throw new Exception(
                        "Missing or ambiguous credentials - please inform exactly one of arm.clientkey or arm.password in the environment settings.");
            }

            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new RuntimeException("authentication result was null");
        }
        return result;
    }

    private static ServiceClientCredentials createTestCredentials() throws Exception {
        return new KeyVaultCredentials() {

            @Override
            public String doAuthenticate(String authorization, String resource, String scope) {
                try {
                    if (isRecordMode()) {
                        AuthenticationResult authResult = getAccessToken(authorization, resource);
                        return authResult.getAccessToken();
                    } else {
                        return "";
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    @After
    public void afterMethod() throws IOException {
        interceptorManager.finalizeInterceptor();
    }

    private static void initPlaybackUri() throws IOException {
        if (isPlaybackMode()) {
            // 11080 and 11081 needs to be in sync with values in jetty.xml file
            playbackUri = PLAYBACK_URI_BASE + "11080";
        } else {
            playbackUri = PLAYBACK_URI_BASE + "1234";
        }
    }

    public static boolean isPlaybackMode() {
        if (testMode == null)
            try {
                initTestMode();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Can't init test mode.");
            }
        return testMode == TestBase.TestMode.PLAYBACK;
    }

    public static boolean isRecordMode() {
        return !isPlaybackMode();
    }

    private static void initTestMode() throws IOException {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("Record")) {
                testMode = TestBase.TestMode.RECORD;
            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
                testMode = TestBase.TestMode.PLAYBACK;
            } else {
                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
            }
        } else {
            // System.out.print("Environment variable 'AZURE_TEST_MODE' has not been set
            // yet. Using 'Playback' mode.");
            testMode = TestBase.TestMode.PLAYBACK;
        }
    }

}
