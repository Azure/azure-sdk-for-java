// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.test.TestBase;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
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
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SecretClientTestBase extends TestBase {    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

    void beforeTestSetup() {
        KeyVaultCredentialPolicy.clearCache();
    }    SecretClientBuilder getClientBuilder(HttpClient httpClient, String testTenantId, String endpoint,
        SecretServiceVersion serviceVersion) throws IOException {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
        } else {
            credential = request -> new AccessToken("mockToken", OffsetDateTime.now().plusHours(2));

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new CustomMatcher().setComparingBodies(false)
                .setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
                .setExcludedHeaders(Arrays.asList("Authorization", "Accept-Language")));
            interceptorManager.addMatchers(customMatchers);
        }

        SecretClientBuilder builder = new SecretClientBuilder().endpoint(endpoint)
            .serviceVersion(serviceVersion)
            .credential(credential)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isRecordMode()) {
            return builder.addHttpPipelinePolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    @Test
    public abstract void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void setSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final Map<String, String> tags = Collections.singletonMap("foo", "baz");

        String resourceId = testResourceNamer.randomName(SECRET_NAME, 20);

        final KeyVaultSecret secretToSet = new KeyVaultSecret(resourceId, SECRET_VALUE).setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags)
                .setContentType("text"));

        testRunner.accept(secretToSet);
    }    @Test
    public abstract void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;    @Test
    public abstract void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void setSecretEmptyValueRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName(SECRET_NAME, 20);
        KeyVaultSecret secretToSet = new KeyVaultSecret(resourceId, "");

        testRunner.accept(secretToSet);
    }    @Test
    public abstract void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

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
    }    @Test
    public abstract void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void updateDisabledSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretUpdate", 20);
        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))
                .setEnabled(false));
        final KeyVaultSecret updatedSecret = new KeyVaultSecret(resourceId, "testSecretVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(originalSecret, updatedSecret);
    }    @Test
    public abstract void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void getSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGet", 20);
        final KeyVaultSecret secretToGet = new KeyVaultSecret(resourceId, "testSecretGetVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToGet);
    }    @Test
    public abstract void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void getSecretSpecificVersionRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetVersion", 30);
        final KeyVaultSecret secretWithOriginalValue
            = new KeyVaultSecret(resourceId, "testSecretGetVersionVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        final KeyVaultSecret secretWithNewValue = new KeyVaultSecret(resourceId, "newVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretWithOriginalValue, secretWithNewValue);
    }    @Test
    public abstract void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void deleteSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretDelete", 20);
        final KeyVaultSecret secretToDelete = new KeyVaultSecret(resourceId, "testSecretDeleteVal").setProperties(
            new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDelete);
    }    @Test
    public abstract void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void getDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretGetDeleted", 25);
        final KeyVaultSecret secretToDeleteAndGet
            = new KeyVaultSecret(resourceId, "testSecretGetDeleteVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndGet);
    }    @Test
    public abstract void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void recoverDeletedSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = testResourceNamer.randomName("testSecretRecover", 25);
        final KeyVaultSecret secretToDeleteAndRecover
            = new KeyVaultSecret(resourceId, "testSecretRecoverVal").setProperties(
                new SecretProperties().setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToDeleteAndRecover);
    }    @Test
    public abstract void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void backupSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackup
            = new KeyVaultSecret(testResourceNamer.randomName("testSecretBackup", 20), "testSecretBackupVal")
                .setProperties(
                    new SecretProperties().setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToBackup);
    }    @Test
    public abstract void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    void restoreSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackupAndRestore
            = new KeyVaultSecret(testResourceNamer.randomName("testSecretRestore", 20), "testSecretRestoreVal")
                .setProperties(
                    new SecretProperties().setExpiresOn(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        testRunner.accept(secretToBackupAndRestore);
    }    @Test
    public abstract void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

    @Test
    public abstract void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

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
    }    @Test
    public abstract void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

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
    }    @Test
    public abstract void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion) throws IOException;

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
    }    /**
     * Returns a stream of arguments that includes all eligible {@link HttpClient HttpClients}.
     *
     * @return A stream of {@link HttpClient HTTP clients} to test.
     */
    static Stream<Arguments> createHttpClients() {
        return TestBase.getHttpClients().map(Arguments::of);
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
