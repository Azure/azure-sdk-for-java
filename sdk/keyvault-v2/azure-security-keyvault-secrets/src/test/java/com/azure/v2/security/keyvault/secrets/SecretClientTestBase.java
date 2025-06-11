// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.BodilessMatcher;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.utils.MockTokenCredential;
import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.CoreUtils;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SecretClientTestBase extends TestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS
        = "AZURE_KEYVAULT_TEST_SECRETS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV
        = Configuration.getGlobalConfiguration().get(AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS);

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

    private static final int MAX_RETRIES = 5;
    private static final HttpRetryOptions LIVE_RETRY_OPTIONS
        = new HttpRetryOptions().setMaxRetries(MAX_RETRIES)
            .setDelay(Duration.ofSeconds(2))
            .setMaxDelay(Duration.ofSeconds(16));

    private static final HttpRetryOptions PLAYBACK_RETRY_OPTIONS
        = new HttpRetryOptions().setMaxRetries(MAX_RETRIES).setDelay(Duration.ofMillis(1));

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    SecretClientBuilder getClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        SecretServiceVersion serviceVersion) {
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

        SecretClientBuilder builder = new SecretClientBuilder().vaultUrl(endpoint)
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

    @Test
    public abstract void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void setSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final Map<String, String> tags = Collections.singletonMap("foo", "baz");

        String resourceId = testResourceNamer.randomName(SECRET_NAME, 20);

        final KeyVaultSecret secretToSet = new KeyVaultSecret(resourceId, SECRET_VALUE).setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags)
                .setContentType("text"));

        testRunner.accept(secretToSet);
    }

    @Test
    public abstract void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void setSecretEmptyValueRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName(SECRET_NAME, 20);
        KeyVaultSecret secretToSet = new KeyVaultSecret(resourceId, "");

        testRunner.accept(secretToSet);
    }

    @Test
    public abstract void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void updateSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        final Map<String, String> tags = new HashMap<>();

        tags.put("first tag", "first value");
        tags.put("second tag", "second value");

        String resourceId = testResourceNamer.randomName("testSecretUpdate", 20);
        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        final KeyVaultSecret updatedSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void updateDisabledSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretUpdate", 20);
        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));
        final KeyVaultSecret updatedSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGet", 20);
        final KeyVaultSecret secretToGet = new KeyVaultSecret(resourceId, "testSecretGetVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToGet);
    }

    @Test
    public abstract void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getSecretSpecificVersionRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetVersion", 30);
        final KeyVaultSecret secretWithOriginalValue
            = new KeyVaultSecret(resourceId, "testSecretGetVersionVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        final KeyVaultSecret secretWithNewValue = new KeyVaultSecret(resourceId, "newVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretWithOriginalValue, secretWithNewValue);
    }

    @Test
    public abstract void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void deleteSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretDelete", 20);
        final KeyVaultSecret secretToDelete = new KeyVaultSecret(resourceId, "testSecretDeleteVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDelete);
    }

    @Test
    public abstract void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetDeleted", 25);
        final KeyVaultSecret secretToDeleteAndGet
            = new KeyVaultSecret(resourceId, "testSecretGetDeleteVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void recoverDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretRecover", 25);
        final KeyVaultSecret secretToDeleteAndRecover
            = new KeyVaultSecret(resourceId, "testSecretRecoverVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void backupSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackup
            = new KeyVaultSecret(testResourceNamer.randomName("testSecretBackup", 20), "testSecretBackupVal")
                .setProperties(
                    new SecretProperties().setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void restoreSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackupAndRestore
            = new KeyVaultSecret(testResourceNamer.randomName("testSecretRestore", 20), "testSecretRestoreVal")
                .setProperties(
                    new SecretProperties().setExpiresOn(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToBackupAndRestore);
    }

    @Test
    public abstract void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secretsToSetAndList = new HashMap<>();

        for (int i = 0; i < 2; i++) {
            String secretName = testResourceNamer.randomName("listSecret", 20);
            String secretVal = "listSecretVal" + i;
            KeyVaultSecret secret = new KeyVaultSecret(secretName, secretVal).setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

            secretsToSetAndList.put(secretName, secret);
        }

        testRunner.accept(secretsToSetAndList);
    }

    @Test
    public abstract void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listDeletedSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secretsToSetAndDelete = new HashMap<>();

        for (int i = 0; i < 3; i++) {
            String secretName = testResourceNamer.randomName("listDeletedSecretsTest", 25);
            String secretVal = "listDeletedSecretVal" + i;
            KeyVaultSecret secret = new KeyVaultSecret(secretName, secretVal).setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

            secretsToSetAndDelete.put(secretName, secret);
        }

        testRunner.accept(secretsToSetAndDelete);
    }

    @Test
    public abstract void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listSecretVersionsRunner(Consumer<List<KeyVaultSecret>> testRunner) {
        List<KeyVaultSecret> secretsToSetAndList = new ArrayList<>();
        String secretVal;
        String secretName = testResourceNamer.randomName("listSecretVersion", 20);

        for (int i = 1; i < 5; i++) {
            secretVal = "listSecretVersionVal" + i;

            secretsToSetAndList.add(new KeyVaultSecret(secretName, secretVal).setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC))));
        }

        testRunner.accept(secretsToSetAndList);
    }

    public String getEndpoint() {
        final String endpoint = Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(expectedStatusCode, Integer.parseInt(ex.getMessage()));
        }
    }

    void assertSecretEquals(KeyVaultSecret expected, KeyVaultSecret actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getProperties().getNotBefore(), actual.getProperties().getNotBefore());
    }

    public static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        for (HttpClient httpClient : getHttpClients()) {
            for (SecretServiceVersion serviceVersion : SecretServiceVersion.values()) {
                argumentsList.add(Arguments.of(httpClient, serviceVersion));
            }
        }
        return argumentsList.stream();
    }

    /**
     * Helper method to verify the error was a Http status code error and return the body of the error.
     */
    String assertRestException(Runnable exceptionThrower, Class<? extends Throwable> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(expectedExceptionType, ex.getClass());
            assertEquals(expectedStatusCode, Integer.parseInt(ex.getMessage()));
            return ex.getMessage();
        }
        return null;
    }
}
