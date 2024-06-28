// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
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
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
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

public abstract class SecretClientTestBase extends TestProxyTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS =
        "AZURE_KEYVAULT_TEST_SECRETS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV =
        Configuration.getGlobalConfiguration().get(AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS);

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

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

        SecretClientBuilder builder = new SecretClientBuilder()
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

    @Test
    public abstract void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void setSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final Map<String, String> tags = Collections.singletonMap("foo", "baz");

        String resourceId = testResourceNamer.randomName(SECRET_NAME, 20);

        final KeyVaultSecret secretToSet = new KeyVaultSecret(resourceId, SECRET_VALUE)
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
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
        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        final KeyVaultSecret updatedSecret = new KeyVaultSecret(resourceId, "testSecretVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setTags(tags));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void updateDisabledSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testUpdateOfDisabledSecret", 35);

        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretUpdateDisabledVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));

        final KeyVaultSecret updatedSecret = new KeyVaultSecret(resourceId, "testSecretUpdateDisabledVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));

        testRunner.accept(originalSecret, updatedSecret);
    }

    @Test
    public abstract void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGet", 20);
        final KeyVaultSecret secretToGet = new KeyVaultSecret(resourceId, "testSecretGetVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToGet);
    }

    @Test
    public abstract void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getSecretSpecificVersionRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetVersion", 30);
        final KeyVaultSecret secretWithOriginalValue = new KeyVaultSecret(resourceId, "testSecretGetVersionVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        final KeyVaultSecret secretWithNewValue = new KeyVaultSecret(resourceId, "newVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretWithOriginalValue, secretWithNewValue);
    }

    @Test
    public abstract void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void deleteSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretDelete", 20);
        final KeyVaultSecret secretToDelete = new KeyVaultSecret(resourceId, "testSecretDeleteVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDelete);
    }

    @Test
    public abstract void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetDeleted", 25);
        final KeyVaultSecret secretToDeleteAndGet = new KeyVaultSecret(resourceId, "testSecretGetDeleteVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndGet);
    }

    @Test
    public abstract void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void recoverDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretRecover", 25);
        final KeyVaultSecret secretToDeleteAndRecover = new KeyVaultSecret(resourceId, "testSecretRecoverVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndRecover);
    }

    @Test
    public abstract void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void backupSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackup =
            new KeyVaultSecret(testResourceNamer.randomName("testSecretBackup", 20), "testSecretBackupVal")
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void restoreSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackupAndRestore =
            new KeyVaultSecret(testResourceNamer.randomName("testSecretRestore", 20), "testSecretRestoreVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

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
            KeyVaultSecret secret = new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

            secretsToSetAndList.put(secretName, secret);
        }

        testRunner.accept(secretsToSetAndList);
    }

    @Test
    public abstract void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listDeletedSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secretSecretsToSetAndDelete = new HashMap<>();

        for (int i = 0; i < 3; i++) {
            String secretName = testResourceNamer.randomName("listDeletedSecretsTest", 20);
            String secretVal = "listDeletedSecretVal" + i;

            secretSecretsToSetAndDelete.put(secretName, new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))));
        }

        testRunner.accept(secretSecretsToSetAndDelete);
    }

    @Test
    public abstract void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listSecretVersionsRunner(Consumer<List<KeyVaultSecret>> testRunner) {
        List<KeyVaultSecret> secretsToSetAndList = new ArrayList<>();
        String secretVal;
        String secretName = testResourceNamer.randomName("listSecretVersion", 20);

        for (int i = 1; i < 5; i++) {
            secretVal = "listSecretVersionVal" + i;

            secretsToSetAndList.add(new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC))));
        }

        testRunner.accept(secretsToSetAndList);
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting contained in the RestResponse body.
     */
    static void assertSecretEquals(KeyVaultSecret expected, KeyVaultSecret actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getProperties().getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getProperties().getContentType(), actual.getProperties().getContentType());
        assertTagsEquals(expected.getProperties().getTags(), actual.getProperties().getTags());
    }

    static void assertTagsEquals(Map<String, String> expected, Map<String, String> actual) {
        if (expected == null) {
            assertNull(actual);
        } else if (actual == null) {
            fail("'expected' tags are null but 'actual' tags are not.");
        } else {
            assertEquals(expected.size(), actual.size());
            expected.forEach((key, value) -> assertEquals(value, actual.get(key)));
        }
    }

    public String getEndpoint() {
        final String endpoint =
            Configuration.getGlobalConfiguration().get("AZURE_KEYVAULT_ENDPOINT", "https://localhost:8080");

        Objects.requireNonNull(endpoint);

        return endpoint;
    }

    static void assertRestException(Runnable exceptionThrower,
        Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertRestException(assertThrows(expectedExceptionType, exceptionThrower::run), expectedExceptionType,
            expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType,
        int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients()
            .forEach(httpClient -> {
                Arrays.stream(SecretServiceVersion.values()).filter(SecretClientTestBase::shouldServiceVersionBeTested)
                    .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion)));
            });

        return argumentsList.stream();
    }

    /**
     * Returns whether the given service version match the rules of test framework.
     *
     * <ul>
     * <li>Using latest service version as default if no environment variable is set.</li>
     * <li>If it's set to ALL, all Service versions in {@link SecretServiceVersion} will be tested.</li>
     * <li>Otherwise, Service version string should match env variable.</li>
     * </ul>
     *
     * Environment values currently supported are: "ALL", "${version}".
     * Use comma to separate http clients want to test.
     * e.g. {@code set AZURE_TEST_SERVICE_VERSIONS = V1_0, V2_0}
     *
     * @param serviceVersion ServiceVersion needs to check.
     *
     * @return Boolean indicates whether filters out the service version or not.
     */
    private static boolean shouldServiceVersionBeTested(SecretServiceVersion serviceVersion) {
        if (CoreUtils.isNullOrEmpty(SERVICE_VERSION_FROM_ENV)) {
            return SecretServiceVersion.getLatest().equals(serviceVersion);
        }

        if (AZURE_TEST_SERVICE_VERSIONS_VALUE_ALL.equalsIgnoreCase(SERVICE_VERSION_FROM_ENV)) {
            return true;
        }

        String[] configuredServiceVersionList = SERVICE_VERSION_FROM_ENV.split(",");

        return Arrays.stream(configuredServiceVersionList).anyMatch(configuredServiceVersion ->
            serviceVersion.getVersion().equals(configuredServiceVersion.trim()));
    }
}
