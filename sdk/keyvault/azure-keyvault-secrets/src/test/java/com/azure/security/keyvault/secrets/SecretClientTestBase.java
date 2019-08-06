// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

public abstract class SecretClientTestBase extends TestBase {

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

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

        TokenCredential credential;

        if (interceptorManager.isPlaybackMode()) {
            credential = resource -> Mono.just(new AccessToken("Some fake token", OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofMinutes(30))));
        } else {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        T client;
        client = clientBuilder.apply(credential);

        return Objects.requireNonNull(client);
    }

    @Test
    public abstract void setSecret();

    void setSecretRunner(Consumer<Secret> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final Secret secret = new Secret(SECRET_NAME, SECRET_VALUE)
            .expires(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .notBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
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
        final Secret originalSecret = new Secret("testSecretUpdate", "testSecretVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .tags(tags);

        final Secret updatedSecret = new Secret("testSecretUpdate", "testSecretVal")
                .expires(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .tags(tags);

        testRunner.accept(originalSecret, updatedSecret);
    }


    @Test
    public abstract void updateDisabledSecret();

    void updateDisabledSecretRunner(BiConsumer<Secret, Secret> testRunner) {

        final Map<String, String> tags = new HashMap<>();

        final Secret originalSecret = new Secret("testUpdateOfDisabledSecret", "testSecretUpdateDisabledVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .enabled(false);

        final Secret updatedSecret = new Secret("testUpdateOfDisabledSecret", "testSecretUpdateDisabledVal")
                .expires(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void getSecret();

    void getSecretRunner(Consumer<Secret> testRunner) {
        final Secret originalSecret = new Secret("testSecretGet", "testSecretGetVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(originalSecret);
    }

    @Test
    public abstract void getSecretSpecificVersion();

    void getSecretSpecificVersionRunner(BiConsumer<Secret, Secret> testRunner) {
        final Secret secret = new Secret("testSecretGetVersion", "testSecretGetVersionVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        final Secret secretWithNewVal = new Secret("testSecretGetVersion", "newVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(secret, secretWithNewVal);
    }

    @Test
    public abstract void getSecretNotFound();

    @Test
    public abstract void deleteSecret();

    void deleteSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDelete = new Secret("testSecretDelete", "testSecretDeleteVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));

        testRunner.accept(secretToDelete);
    }


    @Test
    public abstract void deleteSecretNotFound();

    @Test
    public abstract void getDeletedSecret();

    void getDeletedSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDeleteAndGet = new Secret("testSecretGetDeleted", "testSecretGetDeleteVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(secretToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedSecretNotFound();

    @Test
    public abstract void recoverDeletedSecret();

    void recoverDeletedSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToDeleteAndRecover = new Secret("testSecretRecover", "testSecretRecoverVal")
                .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(secretToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedSecretNotFound();

    @Test
    public abstract void backupSecret();

    void backupSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToBackup = new Secret("testSecretBackup", "testSecretBackupVal")
                .expires(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound();

    @Test
    public abstract void restoreSecret();

    void restoreSecretRunner(Consumer<Secret> testRunner) {
        final Secret secretToBackupAndRestore = new Secret("testSecretRestore", "testSecretRestoreVal")
                .expires(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
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
        for (int i = 0; i < 30; i++) {
            secretName = "listSecret" + i;
            secretVal = "listSecretVal" + i;
            Secret secret =  new Secret(secretName, secretVal)
                    .expires(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC));
            secrets.put(secretName, secret);
        }
        testRunner.accept(secrets);
    }

    @Test
    public abstract void listDeletedSecrets();

    void listDeletedSecretsRunner(Consumer<HashMap<String, Secret>> testRunner) {
        HashMap<String, Secret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for (int i = 0; i < 3; i++) {
            secretName = "listDeletedSecretsTest" + i;
            secretVal = "listDeletedSecretVal" + i;
            secrets.put(secretName, new Secret(secretName, secretVal)
                    .expires(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        }
        testRunner.accept(secrets);
    }


    @Test
    public abstract void listSecretVersions();

    void listSecretVersionsRunner(Consumer<List<Secret>> testRunner) {
        List<Secret> secrets = new ArrayList<>();
        String secretName;
        String secretVal;
        for (int i = 1; i < 5; i++) {
            secretName = "listSecretVersion";
            secretVal = "listSecretVersionVal" + i;
            secrets.add(new Secret(secretName, secretVal)
                    .expires(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC)));
        }
        testRunner.accept(secrets);
    }

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
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.value(), actual.value());
        assertEquals(expected.expires(), actual.expires());
        assertEquals(expected.notBefore(), actual.notBefore());
    }

    public String getEndpoint() {
        final String endpoint = interceptorManager.isPlaybackMode()
                ? "http://localhost:8080"
                : System.getenv("AZURE_KEYVAULT_ENDPOINT");
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
