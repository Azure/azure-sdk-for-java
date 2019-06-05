package com.azure.keyvault;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.keyvault.models.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public abstract class SecretClientTestBase extends TestBase {

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String CRUD_SECRET_NAME = "crudSecret";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";
    private static final int MAX_SECRETS = 4;
    private static final int PAGELIST_MAX_SECRETS = 3;


    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
    }

    <T> T clientSetup(Function<TokenCredential, T> clientBuilder) {
        final String endpoint = interceptorManager.isPlaybackMode()
            ? "http://localhost:8080"
            : System.getenv("AZURE_KEYVAULT_ENDPOINT");

        final String tenantId = System.getenv("MICROSOFT_AD_TENANT_ID");
        final String clientId = System.getenv("ARM_CLIENT_ID");
        final String clientKey = System.getenv("ARM_CLIENT_KEY");

        Objects.requireNonNull(endpoint, "AZURE_KEYVAULT_ENDPOINT expected to be set.");
        Objects.requireNonNull(tenantId, "ARM_CLIENT_ID expected to be set.");
        Objects.requireNonNull(clientId, "ARM_CLIENT_KEY expected to be set.");
        Objects.requireNonNull(tenantId, "MICROSOFT_AD_TENANT_ID expected to be set.");


        TokenCredential credential = new TokenCredential() {
            @Override
            public Mono<String> getTokenAsync(String resource) {
                String authority = "https://login.microsoftonline.com/{tenantId}";
                String auth = authority.replace("{tenantId}", tenantId);
                KeyvaultCredentials creds =  new KeyvaultCredentials(auth,clientId,
                        clientKey, "https://vault.azure.net");
                return Mono.just(creds.authorizationHeaderValue("a"));
            }
        };

        T client;
            String authority = "https://login.microsoftonline.com/{tenantId}";
            String auth = authority.replace("{tenantId}", tenantId);
            client = clientBuilder.apply(credential);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void setSecret();

    void setSecretRunner(Consumer<Secret> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final Secret secret = new Secret(SECRET_NAME, SECRET_VALUE)
            .expires(OffsetDateTime.of(2050,1,30,0,0,0,0, ZoneOffset.UTC))
            .notBefore(OffsetDateTime.of(2000,1,30,12,59, 59, 0, ZoneOffset.UTC))
            .tags(tags)
            .contentType("text");

        testRunner.accept(secret);
    }

    @Test
    public abstract void setSecretEmptyName();

    @Test
    public abstract void setSecretEmptyValue();

    void setSecretEmptyValueRunner(Consumer<Secret> testRunner) {
        Secret secret = new Secret(SECRET_NAME, "");
        testRunner.accept(secret);
    }

    @Test public abstract void setSecretNull();


    @Test
    public abstract void updateSecret();

    void updateSecretRunner(BiConsumer<Secret, Secret> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final Secret originalSecret = new Secret("testSecret1", "testSecretVal")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC))
                .tags(tags);

        final Secret updatedSecret = new Secret("testSecret1", "testSecretVal")
                .expires(OffsetDateTime.of(2060,5,25,0,0,0,0,ZoneOffset.UTC))
                .tags(tags);

        testRunner.accept(originalSecret, updatedSecret);
    }


    @Test
    public abstract void updateDisabledSecret();

    void updateDisabledSecretRunner(BiConsumer<Secret, Secret> testRunner) {

        final Map<String, String> tags = new HashMap<>();

        final Secret originalSecret = new Secret("testSecret2", "testSecretVal2")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC))
                .enabled(false);

        final Secret updatedSecret = new Secret("testSecret2", "testSecretVal2")
                .expires(OffsetDateTime.of(2060,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void getSecret();

    void getSecretRunner(Consumer<Secret> testRunner) {
        final Secret originalSecret = new Secret("testSecret4", "testSecretVal4")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(originalSecret);
    }

    @Test
    public abstract void getSecretSpecificVersion();

    void getSecretSpecificVersionRunner(BiConsumer<Secret, Secret> testRunner) {
        final Secret secret = new Secret("testSecret3", "testSecretVal3")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        final Secret secretWithNewVal = new Secret("testSecret3", "newVal")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(secret, secretWithNewVal);
    }

    @Test
    public abstract void getSecretNotFound();

    @Test
    public abstract void deleteSecret();

    void deleteSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDelete = new Secret("testSecret5", "testSecretVal5")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));

        testRunner.accept(secretToDelete);
    }


    @Test
    public abstract void deleteSecretNotFound();

    @Test
    public abstract void getDeletedSecret();

    void getDeletedSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDeleteAndGet = new Secret("testSecret6", "testSecretVal6")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(secretToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedSecretNotFound();

    @Test
    public abstract void recoverDeletedSecret();

    void recoverDeletedSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDeleteAndRecover = new Secret("testSecret7", "testSecretVal7")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(secretToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedSecretNotFound();

    @Test
    public abstract void backupSecret();

    void backupSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToBackup = new Secret("testSecret8", "testSecretVal8")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound();

    @Test
    public abstract void restoreSecret();

    void restoreSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToBackupAndRestore = new Secret("testSecret9", "testSecretVal9")
                .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC));
        testRunner.accept(secretToBackupAndRestore);
    }

    @Test
    public abstract void restoreSecretFromMalformedBackup();

    @Test
    public abstract void listSecrets();

    void listSecretsRunner(Consumer<HashMap<String, Secret>> testRunner) {
        HashMap<String, Secret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for(int i = 0; i < 10; i++){
            secretName = "listSecret" + i;
            secretVal = "listSecretVal" + i;
            secrets.put(secretName, new Secret(secretName, secretVal)
                    .expires(OffsetDateTime.of(2050,5,25,0,0,0,0,ZoneOffset.UTC)));

        }
        testRunner.accept(secrets);
    }

//    @Test
//    public abstract void listDeletedSecrets();
//
//    @Test
//    public abstract void listSecretVersions();
//
//    @Test
//    public abstract void listSecretVersionsNotFound();


    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of 200.
     *
     * @param expected Secret expected to be returned by the service
     * @param response Response returned by the service, the body should contain a Secret
     */
    static void assertSecretEquals(Secret expected, Response<Secret> response) {
        assertSecretEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    static void assertSecretEquals(Secret expected, Response<Secret> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.statusCode());

        assertSecretEquals(expected, response.value());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
     */
    static void assertSecretEquals(Secret expected, Secret actual) {
        if (expected != null && actual != null) {
           // actual = cleanResponse(expected, actual);
        }
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.value(), actual.value());
        assertEquals(expected.expires(), actual.expires());
        assertEquals(expected.notBefore(), actual.notBefore());
    }

    public String getEndpoint(){
        String endpoint = System.getenv("AZURE_KEYVAULT_ENDPOINT");
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

    public void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
