// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public abstract class SecretClientTestBase extends TestBase {

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

    SecretClientBuilder clientBuilder;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
        TestMode testMode = getTestMode();

        String endpoint;
        TokenCredential credential;
        if (testMode == TestMode.PLAYBACK) {
            endpoint = "http://localhost:8080";
            credential = resource -> Mono.just(new AccessToken("Some fake token", OffsetDateTime.now(ZoneOffset.UTC)
                .plus(Duration.ofMinutes(30))));
        } else {
            endpoint = System.getenv("AZURE_KEYVAULT_ENDPOINT");
            credential = new DefaultAzureCredentialBuilder().build();
        }

        Objects.requireNonNull(endpoint);
        Objects.requireNonNull(credential);

        clientBuilder = new SecretClientBuilder()
            .vaultUrl(endpoint)
            .credential(credential);

        if (testMode == TestMode.PLAYBACK) {
            clientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (testMode == TestMode.RECORD) {
            clientBuilder.addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build());
        }
    }

    @Test
    public abstract void setSecret();

    void setSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("foo", "baz");

        final KeyVaultSecret secret = new KeyVaultSecret(SECRET_NAME, SECRET_VALUE)
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags)
                .setContentType("text"));

        testRunner.accept(secret);
    }

    @Test
    public abstract void setSecretEmptyName();

    @Test
    public abstract void setSecretEmptyValue();

    void setSecretEmptyValueRunner(Consumer<KeyVaultSecret> testRunner) {
        KeyVaultSecret secret = new KeyVaultSecret(SECRET_NAME, "");
        testRunner.accept(secret);
    }

    @Test
    public abstract void setSecretNull();


    @Test
    public abstract void updateSecret();

    void updateSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final KeyVaultSecret originalSecret = new KeyVaultSecret("testSecretUpdate", "testSecretVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        final KeyVaultSecret updatedSecret = new KeyVaultSecret("testSecretUpdate", "testSecretVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        testRunner.accept(originalSecret, updatedSecret);
    }


    @Test
    public abstract void updateDisabledSecret();

    void updateDisabledSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        final KeyVaultSecret originalSecret = new KeyVaultSecret("testUpdateOfDisabledSecret", "testSecretUpdateDisabledVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));

        final KeyVaultSecret updatedSecret = new KeyVaultSecret("testUpdateOfDisabledSecret", "testSecretUpdateDisabledVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));
        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void getSecret();

    void getSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret originalSecret = new KeyVaultSecret("testSecretGet", "testSecretGetVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(originalSecret);
    }

    @Test
    public abstract void getSecretSpecificVersion();

    void getSecretSpecificVersionRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        final KeyVaultSecret secret = new KeyVaultSecret("testSecretGetVersion", "testSecretGetVersionVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        final KeyVaultSecret secretWithNewVal = new KeyVaultSecret("testSecretGetVersion", "newVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secret, secretWithNewVal);
    }

    @Test
    public abstract void getSecretNotFound();

    @Test
    public abstract void deleteSecret();

    void deleteSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToDelete = new KeyVaultSecret("testSecretDelete", "testSecretDeleteVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToDelete);
    }


    @Test
    public abstract void deleteSecretNotFound();

    @Test
    public abstract void getDeletedSecret();

    void getDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToDeleteAndGet = new KeyVaultSecret("testSecretGetDeleted", "testSecretGetDeleteVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedSecretNotFound();

    @Test
    public abstract void recoverDeletedSecret();

    void recoverDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToDeleteAndRecover = new KeyVaultSecret("testSecretRecover", "testSecretRecoverVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedSecretNotFound();

    @Test
    public abstract void backupSecret();

    void backupSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackup = new KeyVaultSecret("testSecretBackup", "testSecretBackupVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound();

    @Test
    public abstract void restoreSecret();

    void restoreSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackupAndRestore = new KeyVaultSecret("testSecretRestore", "testSecretRestoreVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToBackupAndRestore);
    }

    @Test
    public abstract void restoreSecretFromMalformedBackup();

    @Test
    public abstract void listSecrets();

    void listSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for (int i = 0; i < 30; i++) {
            secretName = "listSecret" + i;
            secretVal = "listSecretVal" + i;
            KeyVaultSecret secret = new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
            secrets.put(secretName, secret);
        }
        testRunner.accept(secrets);
    }

    @Test
    public abstract void listDeletedSecrets();

    void listDeletedSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for (int i = 0; i < 3; i++) {
            secretName = "listDeletedSecretsTest" + i;
            secretVal = "listDeletedSecretVal" + i;
            secrets.put(secretName, new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))));
        }
        testRunner.accept(secrets);
    }


    @Test
    public abstract void listSecretVersions();

    void listSecretVersionsRunner(Consumer<List<KeyVaultSecret>> testRunner) {
        List<KeyVaultSecret> secrets = new ArrayList<>();
        String secretName;
        String secretVal;
        for (int i = 1; i < 5; i++) {
            secretName = "listSecretVersion";
            secretVal = "listSecretVersionVal" + i;
            secrets.add(new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC))));
        }
        testRunner.accept(secrets);
    }

    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of
     * 200.
     *
     * @param expected Secret expected to be returned by the service
     * @param response Response returned by the service, the body should contain a Secret
     */
    static void assertSecretEquals(KeyVaultSecret expected, Response<KeyVaultSecret> response) {
        assertSecretEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    static void assertSecretEquals(KeyVaultSecret expected, Response<KeyVaultSecret> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());

        assertSecretEquals(expected, response.getValue());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
     */
    static void assertSecretEquals(KeyVaultSecret expected, KeyVaultSecret actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getProperties().getNotBefore(), actual.getProperties().getNotBefore());
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
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
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
