// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SecretClientTestBase extends TestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS = "AZURE_KEYVAULT_TEST_SECRETS_SERVICE_VERSIONS";
    private static final String SERVICE_VERSION_FROM_ENV = Configuration.getGlobalConfiguration().get(AZURE_TEST_KEYVAULT_SECRET_SERVICE_VERSIONS);

    private static final String SECRET_NAME = "javaSecretTemp";
    private static final String SECRET_VALUE = "Chocolate is hidden in the toothpaste cabinet";

    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";

    @Override
    protected String getTestName() {
        return "";
    }

    void beforeTestSetup() {
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            String clientId = System.getenv("ARM_CLIENTID");
            String clientKey = System.getenv("ARM_CLIENTKEY");
            String tenantId = System.getenv("AZURE_TENANT_ID");
            Objects.requireNonNull(clientId, "The client id cannot be null");
            Objects.requireNonNull(clientKey, "The client key cannot be null");
            Objects.requireNonNull(tenantId, "The tenant id cannot be null");
            credential = new ClientSecretCredentialBuilder()
                                .clientSecret(clientKey)
                                .clientId(clientId)
                                .tenantId(tenantId)
                                .build();
        }

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(SDK_NAME, SDK_VERSION,  Configuration.getGlobalConfiguration().clone(), serviceVersion));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        RetryStrategy strategy = new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16));
        policies.add(new RetryPolicy(strategy));
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, SecretAsyncClient.KEY_VAULT_SCOPE));
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    @Test
    public abstract void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void setSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("foo", "baz");
        String resourceId = generateResourceId(SECRET_NAME);
        final KeyVaultSecret secret = new KeyVaultSecret(resourceId, SECRET_VALUE)
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC))
                .setTags(tags)
                .setContentType("text"));

        testRunner.accept(secret);
    }

    @Test
    public abstract void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void setSecretEmptyValueRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = generateResourceId(SECRET_NAME);
        KeyVaultSecret secret = new KeyVaultSecret(resourceId, "");
        testRunner.accept(secret);
    }

    @Test public abstract void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion);


    @Test
    public abstract void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void updateSecretRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        String resourceId = generateResourceId("testSecretUpdate");
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
        final Map<String, String> tags = new HashMap<>();

        String resourceId = generateResourceId("testUpdateOfDisabledSecret");
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
        String resourceId = generateResourceId("testSecretGet");
        final KeyVaultSecret originalSecret = new KeyVaultSecret(resourceId, "testSecretGetVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(originalSecret);
    }

    @Test
    public abstract void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void getSecretSpecificVersionRunner(BiConsumer<KeyVaultSecret, KeyVaultSecret> testRunner) {
        String resourceId = generateResourceId("testSecretGetVersion");
        final KeyVaultSecret secret = new KeyVaultSecret(resourceId, "testSecretGetVersionVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));

        final KeyVaultSecret secretWithNewVal = new KeyVaultSecret(resourceId, "newVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secret, secretWithNewVal);
    }

    @Test
    public abstract void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void deleteSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        String resourceId = generateResourceId("testSecretDelete");
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
        String resourceId = generateResourceId("testSecretGetDeleted");
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
        String resourceId = generateResourceId("testSecretRecover");
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
        final KeyVaultSecret secretToBackup = new KeyVaultSecret(generateResourceId("testSecretBackup"), "testSecretBackupVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2060, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToBackup);
    }

    @Test
    public abstract void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void restoreSecretRunner(Consumer<KeyVaultSecret> testRunner) {
        final KeyVaultSecret secretToBackupAndRestore = new KeyVaultSecret(generateResourceId("testSecretRestore"), "testSecretRestoreVal")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.of(2080, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
        testRunner.accept(secretToBackupAndRestore);
    }

    @Test
    public abstract void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion);

    @Test
    public abstract void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for (int i = 0; i < 2; i++) {
            secretName = generateResourceId("listSecret" + i);
            secretVal = "listSecretVal" + i;
            KeyVaultSecret secret =  new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2050, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC)));
            secrets.put(secretName, secret);
        }
        testRunner.accept(secrets);
    }

    @Test
    public abstract void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listDeletedSecretsRunner(Consumer<HashMap<String, KeyVaultSecret>> testRunner) {
        HashMap<String, KeyVaultSecret> secrets = new HashMap<>();
        String secretName;
        String secretVal;
        for (int i = 0; i < 3; i++) {
            secretName = generateResourceId("listDeletedSecretsTest" + i);
            secretVal = "listDeletedSecretVal" + i;
            secrets.put(secretName, new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, 25, 0, 0, 0, 0, ZoneOffset.UTC))));
        }
        testRunner.accept(secrets);
    }


    @Test
    public abstract void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion);

    void listSecretVersionsRunner(Consumer<List<KeyVaultSecret>> testRunner) {
        List<KeyVaultSecret> secrets = new ArrayList<>();
        String secretVal;
        String secretName = generateResourceId("listSecretVersion");
        for (int i = 1; i < 5; i++) {
            secretVal = "listSecretVersionVal" + i;
            secrets.add(new KeyVaultSecret(secretName, secretVal)
                .setProperties(new SecretProperties()
                    .setExpiresOn(OffsetDateTime.of(2090, 5, i, 0, 0, 0, 0, ZoneOffset.UTC))));
        }
        testRunner.accept(secrets);
    }

    /**
     * Helper method to verify that the Response matches what was expected. This method assumes a response status of 200.
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
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    String generateResourceId(String suffix) {
        if (interceptorManager.isPlaybackMode()) {
            return suffix;
        }
        String id = UUID.randomUUID().toString();
        return suffix.length() > 0 ? id + "-" + suffix : id;
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

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
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
     * @param serviceVersion ServiceVersion needs to check
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
