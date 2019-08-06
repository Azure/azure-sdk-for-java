// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.*;
import com.azure.core.implementation.http.policy.spi.HttpPolicyProviders;
import com.azure.core.test.TestBase;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.implementation.AzureKeyVaultConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public abstract class CryptographyClientTestBase extends TestBase {
    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        final String endpoint = interceptorManager.isPlaybackMode()
                ? "http://localhost:8080"
                : System.getenv("AZURE_KEYVAULT_ENDPOINT");

        TokenCredential credential;
        HttpClient httpClient;

        String tenantId = System.getenv("AZURE_TENANT_ID");
        String clientId = System.getenv("AZURE_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
        if (!interceptorManager.isPlaybackMode()) {
            assertNotNull(tenantId);
            assertNotNull(clientId);
            assertNotNull(clientSecret);
        }

        if (interceptorManager.isPlaybackMode()) {
            credential = resource -> Mono.just(new AccessToken("Some fake token", OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofMinutes(30))));
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(AzureKeyVaultConfiguration.SDK_NAME, AzureKeyVaultConfiguration.SDK_VERSION,  ConfigurationManager.getConfiguration().clone()));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        policies.add(new BearerTokenAuthenticationPolicy(credential, CryptographyAsyncClient.KEY_VAULT_SCOPE));
        policies.addAll(policies);
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
            policies.add(interceptorManager.getRecordPolicy());
        } else {
            httpClient = HttpClient.createDefault().wiretap(true);
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void encryptDecryptRsa() throws Exception;

    void encryptDecryptRsaRunner(Consumer<KeyPair> testRunner) throws Exception {
        final Map<String, String> tags = new HashMap<>();

        testRunner.accept(getWellKnownKey());
    }

    @Test
    public abstract void signVerifyEc() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    @Test
    public abstract void wrapUnwraptRsa() throws Exception;

    @Test
    public abstract void signVerifyRsa() throws Exception;

    @Test
    public abstract void wrapUnwrapSymmetricKeyAES128Kw();

    @Test
    public abstract void wrapUnwrapSymmetricKeyAES192Kw();

    @Test
    public abstract void wrapUnwrapSymmetricKeyAES256Kw();

    @Test
    public abstract void encryptDecryptSymmetricKeyAes128CbcHmacSha256();

    @Test
    public abstract void encryptDecryptSymmetricKeyAes128CbcHmacSha384();

    @Test
    public abstract void encryptDecryptSymmetricKeyAes128CbcHmacSha512();

    @Test
    public abstract void encryptDecryptSymmetricKeyaes128CbcOneBlock();

    @Test
    public abstract void encryptDecryptSymmetricKeyaes128CbcTwoBlock();

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


    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode()
                ? "http://localhost:8080"
                : "https://cameravault.vault.azure.net";
            //    : System.getenv("AZURE_KEYVAULT_ENDPOINT");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a HttpRequestException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).response().statusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    public void sleepInRecordMode(long millis) {
        if (interceptorManager.isPlaybackMode()) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
